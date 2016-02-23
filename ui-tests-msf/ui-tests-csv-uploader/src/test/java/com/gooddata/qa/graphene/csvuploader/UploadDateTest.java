package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.DateFormat;
import com.gooddata.qa.graphene.utils.Sleeper;

/**
 * Reference the document to know the supported date formats
 * https://help.gooddata.com/display/doc/Add+Data+from+a+File+to+a+Project#AddDatafromaFiletoaProject-SupportedDateFormats
 */
public class UploadDateTest extends AbstractCsvUploaderTest {

    private static final CsvFile DATE_YYYY_FILE = new CsvFile("24dates.yyyy",
            asList("Date 1", "Date 2", "Date 3", "Date 4", "Date 5", "Date 6", "Date 7", "Date 8", "Date 9",
                    "Date 10", "Date 11", "Date 12", "Date 13", "Date 14", "Date 15", "Date 16", "Date 17",
                    "Date 18", "Date 19", "Date 20", "Date 21", "Date 22", "Date 23", "Date 24", "Number"),
            asList("Date (Month.Day.Year)", "Date (Day.Month.Year)", "Date (Year.Month.Day)",
                    "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                    "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)", "Date (Month Day Year)",
                    "Date (Day Month Year)", "Date (Year Month Day)", "Date (Month.Day.Year)", "Date (Day.Month.Year)",
                    "Date (Year.Month.Day)", "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                    "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)", "Date (Month Day Year)",
                    "Date (Day Month Year)", "Date (Year Month Day)", "Measure"), 100);

    @DataProvider(name = "dateDataProvider")
    public Object[][] dateDataProvider() {
        return new Object[][]{
                {DATE_YYYY_FILE},
                {new CsvFile("24dates.yy",
                        asList("Date 1", "Date 2", "Date 3", "Date 4", "Date 5", "Date 6", "Date 7", "Date 8",
                                "Date 9", "Date 10", "Date 11", "Date 12", "Date 13", "Date 14", "Date 15",
                                "Date 16", "Date 17", "Date 18", "Date 19", "Date 20", "Date 21", "Date 22",
                                "Date 23", "Date 24", "Number"),
                        asList("Date (Month.Day.Year)", "Date (Day.Month.Year)", "Date (Year.Month.Day)",
                                "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                                "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)",
                                "Date (Month Day Year)", "Date (Day Month Year)", "Date (Year Month Day)", 
                                "Date (Month.Day.Year)", "Date (Day.Month.Year)", "Date (Year.Month.Day)",
                                "Date (Month/Day/Year)", "Date (Day/Month/Year)", "Date (Year/Month/Day)",
                                "Date (Month-Day-Year)", "Date (Day-Month-Year)", "Date (Year-Month-Day)",
                                "Date (Month Day Year)", "Date (Day Month Year)", "Date (Year Month Day)", "Measure"),
                        100)},
                {new CsvFile("date.yyyymmdd.yymmdd",
                        asList("Date 1", "Date 2", "Number"),
                        asList("Date (YearMonthDay)", "Date (YearMonthDay)", "Measure"), 100)},
                {new CsvFile("unsupported.date.formats",
                        asList("Date 1", "Date 2", "Date 3", "Date 4", "Date 5", "Date 6", "Date 7", "Date 8",
                                "Date 9", "Date 10", "Date 11", "Date 12", "Date 13", "Date 14", "Date 15",
                                "Date 16", "Date 17", "Number"),
                        asList("Measure", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute",
                                "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Attribute",
                                "Attribute", "Attribute", "Attribute", "Attribute", "Attribute", "Measure"),
                        100)},
                {new CsvFile("8ambiguous.dates.starting.with.year", asList("Date 1", "Date 2", "Date 3", "Date 4",
                        "Date 5", "Date 6", "Date 7", "Date 8", "Number"),
                        asList("Date (Year-Month-Day)", "Date (Year/Month/Day)", "Date (Year.Month.Day)",
                                "Date (Year Month Day)", "Date (Year-Month-Day)", "Date (Year/Month/Day)",
                                "Date (Year.Month.Day)", "Date (Year Month Day)", "Measure"),
                        10)}
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "dateDataProvider")
    public void uploadDateDatasetWithDifferentFormats(CsvFile fileToUpload) {
        initDataUploadPage();
        uploadFile(fileToUpload);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        takeScreenshot(browser, toScreenshotName("data-preview", fileToUpload.getFileName()), getClass());
        assertThat(dataPreviewTable.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
        dataPreviewPage.triggerIntegration();
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Sleeper.sleepTightInSeconds(5);
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
        waitForFragmentVisible(csvDatasetDetailPage);
        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
    }

    @DataProvider(name = "ambiguousDateDataProvider")
    public Object[][] ambiguousDateDataProvider() {
        return new Object[][]{
                {new CsvFile("6ambiguous.dates.yearday.dayyear",
                        asList("Date 1", "Date 2", "Date 3", "Date 4", "Date 5", "Date 6", "Number"),
                        asList("Date", "Date", "Date", "Date", "Date", "Date", "Measure"), 100),
                        asList(DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_DOT,
                                DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_DOT,
                                DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_SLASH,
                                DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                                DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN,
                                DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN),
                        2},
                {new CsvFile("6ambiguous.dates.monthday.daymotnh",
                        asList("Date 1", "Date 2", "Date 3", "Date 4", "Date 5", "Date 6", "Number"),
                        asList("Date", "Date", "Date", "Date", "Date", "Date", "Measure"), 10),
                        asList(DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_DOT,
                                DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN,
                                DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                                DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_DOT,
                                DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_HYPHEN,
                                DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_SLASH),
                        2},
                {new CsvFile("4ambiguous.dates.3formats",
                        asList("Date 1", "Date 2", "Date 3", "Date 4", "Number"),
                        asList("Date", "Date", "Date", "Date", "Measure"), 10),
                        asList(DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_DOT,
                              DateFormat.YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN,
                              DateFormat.DAY_MONTH_YEAR_SEPARATED_BY_SLASH,
                              DateFormat.MONTH_DAY_YEAR_SEPARATED_BY_SPACE),
                        3},
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "ambiguousDateDataProvider")
    public void uploadAmbigousDateDataset(CsvFile fileToUpload, List<DateFormat> dateFormats, int dateFormatCounts) {
        initDataUploadPage();
        uploadFile(fileToUpload);
        DataPreviewTable dataPreviewTable = waitForFragmentVisible(dataPreviewPage).getDataPreviewTable();
        takeScreenshot(browser, toScreenshotName("data-preview", fileToUpload.getFileName()), getClass());
        assertThat(dataPreviewTable.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(dataPreviewTable.getColumnTypes(), contains(fileToUpload.getColumnTypes().toArray()));
        assertThat(dataPreviewPage.getPreviewPageErrorMessage(),
                containsString("Fill in or correct the names and types for highlighted columns"));
        assertTrue(dataPreviewPage.isIntegrationButtonDisabled(),
                "Add data button should be disabled when column names are invalid");
        int indexColumn = 0;
        for (DateFormat dateFormat : dateFormats) {
            assertEquals(dataPreviewTable.getColumnDateFormatCount(indexColumn), dateFormatCounts,
                    "Wrong ambiguous formats");
            dataPreviewTable.changeColumnDateFormat(indexColumn, dateFormat);
            indexColumn++;
        }
        assertFalse(dataPreviewPage.hasPreviewPageErrorMessage(), "Error in preview page should not be shown");

        dataPreviewPage.triggerIntegration();
        String datasetName = getNewDataset(fileToUpload);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Sleeper.sleepTightInSeconds(5);
        datasetsListPage.getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
        waitForFragmentVisible(csvDatasetDetailPage);
        List<String> columnTypes = dateFormats.stream()
                .map(DateFormat::getColumnType)
                .collect(toList());
        columnTypes.add("Measure");
        assertThat(csvDatasetDetailPage.getColumnNames(), contains(fileToUpload.getColumnNames().toArray()));
        assertThat(csvDatasetDetailPage.getColumnTypes(), contains(columnTypes.toArray()));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void updateDateDataset() {
        initDataUploadPage();
        checkCsvUpload(DATE_YYYY_FILE, this::uploadCsv, true);
        String datasetName = getNewDataset(DATE_YYYY_FILE);
        waitForDatasetName(datasetName);
        waitForDatasetStatus(datasetName, SUCCESSFUL_STATUS_MESSAGE_REGEX);
        Sleeper.sleepTightInSeconds(5);
        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();
        refreshCsv(DATE_YYYY_FILE, datasetName, true);
        waitForFragmentVisible(datasetsListPage);

        datasetsListPage.getMyDatasetsTable().getDatasetRefreshButton(datasetName).click();
        final CsvFile dateInvalidYYYY = new CsvFile("24dates.yyyy.invalid");
        doUploadFromDialog(dateInvalidYYYY);

        List<String> backendValidationErrors =
                waitForFragmentVisible(fileUploadDialog).getBackendValidationErrors();
        assertThat(backendValidationErrors,
                hasItems(format("Update from file \"%s\" failed. "
                        + "Number, type, and order of the columns do not match the dataset. "
                        + "Check the dataset structure.", dateInvalidYYYY.getFileName())));
    }
}
