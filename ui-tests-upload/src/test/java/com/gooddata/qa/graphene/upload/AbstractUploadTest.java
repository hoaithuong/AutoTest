package com.gooddata.qa.graphene.upload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

public class AbstractUploadTest extends AbstractProjectTest {

	protected String csvFilePath;

	protected Map<String, String[]> expectedDashboardsAndTabs;
	protected Map<String, String[]> emptyDashboardsAndTabs;

	protected static final By BY_INPUT = By
			.xpath("//input[contains(@class, 'has-error')]");
	protected static final By BY_BUBBLE = By
			.xpath("//div[contains(@class, 'bubble-negative') and contains(@class, 'isActive')]");
	protected static final By BY_BUBBLE_CONTENT = By
			.xpath("//div[@class='content']");

	@FindBy(xpath = "//div[@id='gridContainerTab']")
	protected TableReport report;

	@FindBy(css = "button.s-btn-load")
	protected WebElement loadButton;

	@FindBy(xpath = "//div[@class='message is-error s-uploadIndex-error is-visible']")
	protected WebElement errorMessageElement;

	private static final By BY_EMPTY_DATASET = By
			.xpath("//div[@id='dataPage-empty-dataSets']");

	@FindBy
	protected WebElement uploadFile;

	@BeforeClass
	public void initProperties() {
		csvFilePath = loadProperty("csvFilePath");
		projectTitle = "simple-project-upload";

		expectedDashboardsAndTabs = new HashMap<String, String[]>();
		expectedDashboardsAndTabs.put("Default dashboard",
				new String[] { "Your Sample Reports" });
		emptyDashboardsAndTabs = new HashMap<String, String[]>();
		emptyDashboardsAndTabs.put("Default dashboard",
				new String[] { "First Tab" });
	}

	protected void prepareReport(String reportName, ReportTypes reportType,
			List<String> what, List<String> how) throws InterruptedException {
		initReportsPage();
		reportsPage.startCreateReport();
		waitForAnalysisPageLoaded();
		waitForElementVisible(reportPage.getRoot());
		assertNotNull(reportPage, "Report page not initialized!");
		reportPage.createReport(reportName, reportType, what, how);
	}

	protected void deleteDataset(String datasetName)
			throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|dataSet");
		waitForElementVisible(datasetsTable.getRoot());
		waitForDataPageLoaded();
		datasetsTable.selectObject(datasetName);
		datasetDetailPage.deleteDataset();
		waitForDataPageLoaded();
	}

	protected void deleteDashboard() throws InterruptedException {
		initDashboardsPage();
		Thread.sleep(3000);
		dashboardsPage.deleteDashboard();
		waitForDashboardPageLoaded();
		verifyProjectDashboardsAndTabs(true, emptyDashboardsAndTabs, false);
	}

	protected void checkAttributeName(String attributeName)
			throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
		waitForElementVisible(attributesTable.getRoot());
		waitForDataPageLoaded();
		System.out.println("Check attribute name is displayed well.");
		assertTrue(attributesTable.selectObject(attributeName));
	}

	protected void selectFileToUpload(String fileName)
			throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
		openUrl(PAGE_UPLOAD);
		waitForElementVisible(upload.getRoot());
		upload.uploadFile(csvFilePath + fileName + ".csv");
	}
	
	protected void checkErrorColumn(UploadColumns uploadColumns, int columnIndex, boolean hasBubble, String bubbleMessage){
		assertTrue(uploadColumns.getColumns().get(columnIndex).findElement(BY_INPUT)
				.isDisplayed());
		System.out.print("Border of field name turn to red.");
		if (hasBubble)
			{
			assertEquals(uploadColumns.getColumns().get(columnIndex).findElement(BY_BUBBLE)
					.findElement(BY_BUBBLE_CONTENT).getText(), bubbleMessage);
			System.out.print("System shows error message: " + bubbleMessage);
			};
	}
	
	protected void uploadInvalidCSVFile(String fileName, String errorTitle, String errorMessage, String errorSupport) throws InterruptedException{
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
		openUrl(PAGE_UPLOAD);
		waitForElementVisible(upload.getRoot());
		String filePath = csvFilePath + fileName + ".csv";
		System.out.println("Going to upload file: " + filePath);
		waitForElementPresent(uploadFile).sendKeys(filePath);
		if (!waitForElementVisible(errorMessageElement).isDisplayed()){
			Thread.sleep(30000);
		}
		Screenshots.takeScreenshot(browser, "check-incorrect-csv-file-upload",
				this.getClass());
		if (errorTitle != null){
			assertEquals(errorMessageElement.findElement(
						By.cssSelector(".s-uploadIndex-errorTitle")).getText(), errorTitle);
		}
		if (errorMessage != null){
			assertEquals(errorMessageElement.findElement(
							By.cssSelector(".s-uploadIndex-errorMessage")).getText(), errorMessage);
		}
		if(errorSupport != null){
			assertEquals(errorMessageElement.findElement(
							By.cssSelector(".s-uploadIndex-errorSupport"))
							.getText(), errorSupport);
		}
	}

	protected void assertMetricValuesInReport(List<Integer> metricIndexs,
			List<Float> metricValues, List<Double> expectedMetricValues) {
		int index = 0;
		for (int metricIndex : metricIndexs) {
			assertEquals(metricValues.get(metricIndex).doubleValue(),
					expectedMetricValues.get(index));
			index++;
		}
	}

	protected void assertEmptyMetricInReport(List<Integer> metricIndexs,
			List<Float> metricValues) {
		for (int metricIndex : metricIndexs) {
			assertEquals(metricValues.get(metricIndex).doubleValue(), 
					0.0);
		}
	}

	protected void assertAttributeElementsInReport(
			List<Integer> attributeIndexs, List<String> attributeElements,
			List<String> expectedAttribueElements) {
		int index = 0;
		for (int attributeIndex : attributeIndexs) {
			assertEquals(attributeElements.get(attributeIndex).toString(),
					expectedAttribueElements.get(index).toString());
			index++;
		}
	}

	public void uploadFileAndClean(String fileName) throws InterruptedException {
		try {
			uploadSimpleCSV(csvFilePath + fileName + ".csv", "simple-upload-"
					+ fileName);
			verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs,
					false);
		} finally {
			deleteDashboard();
			deleteDataset(fileName);
			waitForElementVisible(BY_EMPTY_DATASET);
		}
	}

	protected void cleanDashboardAndDatasets(List<String> datasets)
			throws InterruptedException {
		deleteDashboard();
		for (String dataset : datasets) {
			deleteDataset(dataset);
		}
		assertTrue(dataPage.getRoot().findElement(BY_EMPTY_DATASET)
				.isDisplayed());
	}

	protected void uploadDifferentDateFormat(String uploadFileName)
			throws InterruptedException {
		selectFileToUpload(uploadFileName);
		Screenshots.takeScreenshot(browser, "different-date-format-csv-upload-"
				+ uploadFileName, this.getClass());
		UploadColumns uploadColumns = upload.getUploadColumns();
		assertEquals(uploadColumns.getNumberOfColumns(), 9);
		List<Integer> columnIndexs = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
		List<String> guessedDataTypes = Arrays.asList("TEXT", "TEXT", "TEXT",
				"TEXT", "TEXT", "TEXT", "TEXT", "DATE", "NUMBER");
		List<String> expectedColumnNames = Arrays.asList("Lastname",
				"Firstname", "Education", "Position", "Store", "State",
				"County", "Paydate", "Amount");
		upload.assertColumnsType(uploadColumns, columnIndexs, guessedDataTypes);
		upload.assertColumnsName(uploadColumns, columnIndexs,
				expectedColumnNames);
		upload.confirmloadCsv();
		waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"));
		verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, true);
		Screenshots.takeScreenshot(browser, uploadFileName + "-dashboard",
				this.getClass());

		// Check Date in report
		List<String> what = new ArrayList<String>();
		what.add("Sum of Amount");
		List<String> how = new ArrayList<String>();
		how.add("Month/Year");
		prepareReport("Report with " + uploadFileName, ReportTypes.TABLE, what,
				how);
		List<String> attributeElements = report.getAttributeInGrid();
		Screenshots.takeScreenshot(browser, "report-with-" + uploadFileName,
				this.getClass());
		System.out.println("Check the date format in report!");
		List<Integer> attributeIndex = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8,
				9, 10, 11, 12);
		List<String> expectedAttributeElements = Arrays.asList("Jan 2006",
				"Feb 2006", "Mar 2006", "Apr 2006", "May 2006", "Jun 2006",
				"Jul 2006", "Aug 2006", "Sep 2006", "Oct 2006", "Nov 2006",
				"Dec 2006", "Jan 2007");
		assertAttributeElementsInReport(attributeIndex, attributeElements,
				expectedAttributeElements);
		System.out.println("Date format with " + uploadFileName
				+ " is displayed well in report!");
	}
}
