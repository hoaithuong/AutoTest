package com.gooddata.qa.graphene.csvuploader;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import java.util.List;

public class CsvUploaderTest extends AbstractCsvUploaderTest {

    private static final CsvFile PAYROLL_FILE = CsvFile.PAYROLL;
    private static String PAYROLL_DATASET_NAME = PAYROLL_FILE.getDatasetNameOfFirstUpload();

    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage();
        datasetsListPage.waitForHeaderVisible();
        datasetsListPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage();
        datasetsListPage.waitForEmptyStateLoaded();

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "empty"), getClass());

        System.out.println("Empty state message: " + datasetsListPage.getEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"checkEmptyState"})
    public void checkCsvUploadHappyPath() throws Exception {
        checkCsvUpload(PAYROLL_FILE, this::uploadCsv, true);
        PAYROLL_DATASET_NAME = getNewDataset(PAYROLL_FILE);

        assertThat("Dataset with name '" + PAYROLL_DATASET_NAME + "' wasn't found in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                hasItem(PAYROLL_DATASET_NAME));

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "uploading-dataset", PAYROLL_DATASET_NAME), getClass());

        waitForDatasetStatus(PAYROLL_DATASET_NAME, SUCCESSFUL_STATUS_MESSAGE_REGEX);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "dataset-uploaded", PAYROLL_DATASET_NAME), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDatasetDetail() {
        initDataUploadPage();
        datasetsListPage.clickDatasetDetailButton(PAYROLL_DATASET_NAME);

        waitForFragmentVisible(csvDatasetDetailPage);

        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        csvDatasetDetailPage.clickBackButton();

        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"checkCsvRefreshFromDetail"})
    public void checkDeleteCsvDataset() throws Exception {
        initDataUploadPage();

        removeDataset(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        checkForDatasetRemoved(PAYROLL_DATASET_NAME);
        removeDatasetFromUploadHistory(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, PAYROLL_DATASET_NAME, "dataset-deleted"), getClass());
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvBadFormat() throws Exception {
        CsvFile fileToUpload = CsvFile.BAD_STRUCTURE;
        checkCsvUpload(fileToUpload, this::uploadBadCsv, false);

        String datasetName = fileToUpload.getDatasetNameOfFirstUpload();
        assertThat("Dataset with name '" + datasetName + "' should not be in datasets list.",
                datasetsListPage.getMyDatasetsTable().getDatasetNames(),
                not(hasItem(datasetName)));
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkNoFactAndNumericColumnNameCsvConfig() throws Exception {
        initDataUploadPage();
        uploadFile(PAYROLL_FILE);
        waitForFragmentVisible(dataPreviewPage);

        final String factColumnName = "Amount";
        final String numericColumnName = "42";

        dataPreviewPage.getDataPreviewTable().changeColumnType(factColumnName, DataPreviewTable.ColumnType.ATTRIBUTE);
        assertThat(dataPreviewPage.getPreviewPageErrorMassage(),
                containsString("At least one column must contain numbers"));

        dataPreviewPage.getDataPreviewTable().changeColumnName(factColumnName, numericColumnName);
        assertThat(dataPreviewPage.getDataPreviewTable().getColumnError(numericColumnName),
                containsString("The column name cannot begin with a numerical character"));
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshFromList() {
        initDataUploadPage();

        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(CsvFile.PAYROLL.getDatasetNameOfFirstUpload()).click();

        refreshCsv(CsvFile.PAYROLL_REFRESH);

        waitForDatasetStatus(CsvFile.PAYROLL.getDatasetNameOfFirstUpload(), SUCCESSFUL_STATUS_MESSAGE_REGEX);
    }

    @Test(dependsOnMethods = {"checkCsvDatasetDetail"})
    public void checkCsvRefreshFromDetail() {
        initDataUploadPage();

        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(CsvFile.PAYROLL.getDatasetNameOfFirstUpload()).click();

        waitForFragmentVisible(csvDatasetDetailPage).clickRefreshButton();

        refreshCsv(CsvFile.PAYROLL_REFRESH);

        waitForFragmentVisible(csvDatasetDetailPage);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvRefreshWithIncorrectMetadata() {
        initDataUploadPage();

        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(CsvFile.PAYROLL.getDatasetNameOfFirstUpload()).click();

        doUploadFromDialog(CsvFile.PAYROLL_REFRESH_BAD);

        final List<String> validationErrors = waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();

        assertTrue(!validationErrors.isEmpty(), "Missing validation error for refresh with incorrect metadata.");
    }

    private void refreshCsv(CsvFile refreshData) {
        doUploadFromDialog(refreshData);

        waitForFragmentVisible(dataPreviewPage);

        takeScreenshot(browser, toScreenshotName(DATA_PREVIEW_PAGE, refreshData.getFileName()), getClass());

        dataPreviewPage.triggerIntegration();
    }
}
