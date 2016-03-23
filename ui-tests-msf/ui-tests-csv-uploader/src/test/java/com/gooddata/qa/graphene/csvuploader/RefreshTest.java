package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;

public class RefreshTest extends HappyUploadTest {

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshFromList() {
        updateCsv(PAYROLL_REFRESH, PAYROLL.getDatasetNameOfFirstUpload(), true);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshFromDetail() {
        final Dataset dataset = initDataUploadPage()
                .getMyDatasetsTable()
                .getDataset(PAYROLL.getDatasetNameOfFirstUpload());

        updateCsvInDetailPage(PAYROLL_REFRESH, dataset, true);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkSetHeaderHiddenWhenUpdate() {
        initDataUploadPage();

        waitForFragmentVisible(datasetsListPage)
            .getMyDatasetsTable()
            .getDataset(PAYROLL.getDatasetNameOfFirstUpload())
            .clickUpdateButton()
            .pickCsvFile(PAYROLL_REFRESH.getFilePath())
            .clickUploadButton();

        assertTrue(DataPreviewPage.getInstance(browser).isSetHeaderLinkHidden());
        takeScreenshot(browser, "set-header-button-hidden-" + PAYROLL_REFRESH.getFileName(), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshWithIncorrectMetadata() {
        final String badUpdateCsvFileName = "payroll.refresh.bad.csv";

        final FileUploadDialog fileUploadDialog = initDataUploadPage()
            .getMyDatasetsTable()
            .getDataset(PAYROLL.getDatasetNameOfFirstUpload())
            .clickUpdateButton();

        fileUploadDialog.pickCsvFile(getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + badUpdateCsvFileName))
            .clickUploadButton();

        final List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();

        assertThat(backendValidationErrors,
                hasItems(format("Update from file \"%s\" failed. "
                        + "Number, type, and order of the columns do not match the dataset. "
                        + "Check the dataset structure.", badUpdateCsvFileName)));

        assertEquals(fileUploadDialog.getLinkInBackendValidationError(),
                format(DATASET_LINK, testParams.getHost(), testParams.getProjectId(),
                        getDatasetId(PAYROLL.getDatasetNameOfFirstUpload())));
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCancelCsvRefresh() {
        final Dataset dataset = initDataUploadPage()
            .getMyDatasetsTable()
            .getDataset(PAYROLL.getDatasetNameOfFirstUpload());

        dataset.clickUpdateButton()
            .pickCsvFile(PAYROLL_REFRESH.getFilePath())
            .clickUploadButton();
        DataPreviewPage.getInstance(browser).cancelTriggerIntegration();

        waitForFragmentVisible(datasetsListPage);

        final DatasetDetailPage datasetDetailPage = waitForFragmentVisible(dataset)
            .openDetailPage();

        datasetDetailPage.clickUpdateButton()
            .pickCsvFile(PAYROLL_REFRESH.getFilePath())
            .clickUploadButton();
        DataPreviewPage.getInstance(browser).cancelTriggerIntegration();

        waitForFragmentVisible(datasetDetailPage);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"}, alwaysRun = true)
    public void addOtherAdminToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getAdminUser(), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"addOtherAdminToProject"})
    public void checkAdminUpdateDataOfOthers() throws JSONException {
        try {
            final String datasetName = PAYROLL.getDatasetNameOfFirstUpload();
            log.info("datasetName by owner: " + datasetName);

            logout();
            signInAtGreyPages(testParams.getAdminUser(), testParams.getAdminPassword());

            final Dataset dataset = initDataUploadPage()
                    .getOthersDatasetsTable()
                    .getDataset(datasetName);

            dataset.clickUpdateButton()
                .pickCsvFile(PAYROLL_REFRESH.getFilePath())
                .clickUploadButton();
            DataPreviewPage.getInstance(browser).triggerIntegration();
            Dataset.waitForDatasetLoaded(browser);

            assertEquals(DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText(),
                    format(SUCCESSFUL_DATA_MESSAGE, datasetName));

            updateCsvInDetailPage(PAYROLL_REFRESH, dataset, false);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
