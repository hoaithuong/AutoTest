package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

public class DatasetDetailTest extends HappyUploadTest {
    
    private static final CsvFile PAYROLL_FILE = CsvFile.PAYROLL;
    private static String PAYROLL_DATASET_NAME = PAYROLL_FILE.getDatasetNameOfFirstUpload();
    private static final long PAYROLL_FILE_SIZE_MINIMUM = 476L;

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkCsvDatasetDetail() {
        openDatasetDetailsPage();

        waitForFragmentVisible(csvDatasetDetailPage);

        takeScreenshot(browser, DATASET_DETAIL_PAGE_NAME, getClass());

        checkCsvDatasetDetail(PAYROLL_FILE, PAYROLL_DATASET_NAME);

        csvDatasetDetailPage.clickBackButton();

        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void checkDatasetDetailPage() {
        openDatasetDetailsPage();

        waitForFragmentVisible(csvDatasetDetailPage).downloadTheLatestCsvFileUpload();
        final File downloadedCsvFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                                        + PAYROLL_FILE.getFileName());
        Predicate<WebDriver> fileDownloadComplete =
                browser -> downloadedCsvFile.length() > PAYROLL_FILE_SIZE_MINIMUM;
        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).pollingEvery(10, TimeUnit.SECONDS)
                .until(fileDownloadComplete);
        log.info("Download file size: " + downloadedCsvFile.length());
        log.info("Download file path: " + downloadedCsvFile.getPath());
        log.info("Download file name: " + downloadedCsvFile.getName());

        String createdDateTime =
                csvDatasetDetailPage.getCreatedDateTime().replaceAll("Created by.*on\\s+", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Today at' h:mm a");
        try {
            formatter.parse(createdDateTime);
        } catch (DateTimeParseException e) {
            Assert.fail("Incorrect format of created date time: " + createdDateTime);
        }
    }

    private void openDatasetDetailsPage() {
        initDataUploadPage();
        datasetsListPage.clickDatasetDetailButton(PAYROLL_DATASET_NAME);
    }
}
