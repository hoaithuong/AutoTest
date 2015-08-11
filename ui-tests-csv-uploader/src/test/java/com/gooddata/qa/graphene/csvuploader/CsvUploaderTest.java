package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.SourcesListPage;
import org.json.JSONException;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.graphene.utils.AdsHelper;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.gooddata.warehouse.Warehouse;

import java.util.function.Function;

public class CsvUploaderTest extends AbstractMSFTest {

    private static final String DATA_UPLOAD_PAGE_URI = "data/#/project/%s/sources";
    private static final String CSV_FILE_NAME = "payroll.csv";
    /** This csv file has incorrect column count (one more than expected) on the line number 2. */
    private static final String BAD_CSV_FILE_NAME = "payroll.bad.csv";

    private static final String UPLOAD_DIALOG_NAME = "upload-dialog";
    private static final String DATA_PAGE_NAME = "data-page";

    private AdsHelper adsHelper;

    private Warehouse ads;

    @FindBy(className = "s-sources-list")
    private SourcesListPage sourcesListPage;

    @FindBy(className = "s-upload-dialog")
    private FileUploadDialog fileUploadDialog;

    @FindBy(className = "s-data-preview")
    private DataPreviewPage dataPreviewPage;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
        adsHelper = new AdsHelper(getGoodDataClient(), getRestApiClient());
        ads = adsHelper.createAds("CSV Uploader Test ADS", dssAuthorizationToken);
    }

    @AfterClass(alwaysRun = true)
    public void tearDownOutputStage() {
        adsHelper.removeAds(ads);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage();
        sourcesListPage.waitForHeaderVisible();
        sourcesListPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage();
        sourcesListPage.waitForEmptyStateLoaded();

        takeScreenshot(browser, DATA_PAGE_NAME + "-empty", getClass());

        System.out.println("Empty state message: " + sourcesListPage.getEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"checkEmptyState"})
    public void checkCsvUploadHappyPath() throws Exception {
        adsHelper.associateAdsWithProject(ads, testParams.getProjectId());

        checkCsvUpload(CSV_FILE_NAME, this::uploadCsv, true);

        takeScreenshot(browser, DATA_PAGE_NAME + "-one-dataset-uploaded", getClass());

        final String sourceName = removeExtension(CSV_FILE_NAME);
        assertNotNull(sourcesListPage.getMySourcesTable().getSource(sourceName),
                "Source with name '" + sourceName + "' wasn't found in sources list.");
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvBadFormat() throws Exception {
        checkCsvUpload(BAD_CSV_FILE_NAME, this::uploadBadCsv, false);

        final String sourceName = removeExtension(BAD_CSV_FILE_NAME);
        assertNull(sourcesListPage.getMySourcesTable().getSource(sourceName),
                "Source with name '" + sourceName + "' should not be in sources list.");
    }

    private void checkCsvUpload(String csvFileName,
                                Function<String, Integer> uploadCsvFunction,
                                boolean newDatasetExpected) throws JSONException {
        initDataUploadPage();

        final int datasetCountBeforeUpload = sourcesListPage.getMySourcesCount();

        final int datasetCountAfterUpload = uploadCsvFunction.apply(csvFileName);

        assertEquals(datasetCountAfterUpload, newDatasetExpected ? datasetCountBeforeUpload + 1 : datasetCountBeforeUpload);
    }

    private void initDataUploadPage() {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI, testParams.getProjectId()));
        waitForFragmentVisible(sourcesListPage);
    }

    private int uploadCsv(String csvFileName) {

        uploadFile(csvFileName);

        waitForFragmentVisible(dataPreviewPage);

        dataPreviewPage.selectFact().triggerIntegration();

        //waiting for refresh data from backend
        // TODO: remove sleep while proper "progress" is implemented in data section UI
        Sleeper.sleepTightInSeconds(10);

        return waitForFragmentVisible(sourcesListPage).getMySourcesCount();
    }

    private int uploadBadCsv(String csvFileName) {

        uploadFile(csvFileName);

        // the processing should not go any further but display validation error directly in File Upload Dialog
        assertThat(fileUploadDialog.getBackendValidationErrors(), contains("csv.validations.structural.incorrect-column-count"));

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-validation-errors-" + csvFileName, getClass());

        waitForFragmentVisible(fileUploadDialog).clickCancelButton();
        waitForFragmentVisible(sourcesListPage);

        return sourcesListPage.getMySourcesCount();
    }

    private void uploadFile(String csvFileName) {
        waitForFragmentVisible(sourcesListPage).clickAddDataButton();

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-initial-state-" + csvFileName, getClass());

        waitForFragmentVisible(fileUploadDialog).pickCsvFile(getCsvFileToUpload(csvFileName));

        takeScreenshot(browser, UPLOAD_DIALOG_NAME + "-csv-file-picked-" + csvFileName, getClass());

        fileUploadDialog.clickUploadButton();
    }

    private String getCsvFileToUpload(String csvFileName) {
        return ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/" + csvFileName);
    }
}
