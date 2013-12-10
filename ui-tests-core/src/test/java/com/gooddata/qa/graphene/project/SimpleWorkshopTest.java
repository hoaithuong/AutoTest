package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardReportOneNumber;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "projectSimpleWS" }, description = "Tests for simple workshop test in GD platform")
public class SimpleWorkshopTest extends SimpleProjectAbstractTest {
	
	private String csvFilePath;
	
	@BeforeClass
	public void initProperties() {
		csvFilePath = loadProperty("csvFilePath");
		projectTitle = "simple-project-ws";
	}
	
	@Test(dependsOnMethods = { "createSimpleProject" }, groups = { "simpleTests" })
	public void uploadData() throws InterruptedException {
		uploadSimpleCSV(csvFilePath + "/payroll.csv", "simple-ws");
	}
	
	@Test(dependsOnMethods = { "uploadData" }, groups = { "simpleTests" })
	public void addNewTabs() throws InterruptedException {
		addNewTabOnDashboard("Default dashboard", "workshop", "simple-ws");
	}
	
	@Test(dependsOnMethods = { "uploadData" }, groups = "simpleTests")
	public void createBasicReport() throws InterruptedException {
		initReportsPage();
		reportsPage.startCreateReport();
		waitForAnalysisPageLoaded();
		waitForElementVisible(reportPage.getRoot());
		List<String> what = new ArrayList<String>();
		what.add("Sum of Amount");
		reportPage.createReport("Headline test", ReportTypes.HEADLINE, what, null);
		Screenshots.takeScreenshot(browser, "simple-ws-headline-report", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createBasicReport" }, groups = { "simpleTests" })
	public void addReportOnDashboardTab() throws InterruptedException {
		initDashboardsPage();
		dashboardsPage.getTabs().openTab(1);
		waitForDashboardPageLoaded();
		dashboardsPage.editDashboard();
		dashboardsPage.getDashboardEditBar().addReportToDashboard("Headline test");
		dashboardsPage.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "simple-ws-headline-report-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "addReportOnDashboardTab" }, groups = { "simpleTests" })
	public void verifyHeadlineReport() {
		initDashboardsPage();
		//Assert.assertEquals(1, dashboardsPage.getContent().getNumberOfReports(), "Report is not available on dashboard");
		System.out.println("reports: " + dashboardsPage.getContent().getNumberOfReports());
		DashboardReportOneNumber report = Graphene.createPageFragment(DashboardReportOneNumber.class, dashboardsPage.getContent().getReport(0).getRoot());
		Assert.assertEquals(report.getValue(), "7,252,542.63", "Invalid value in headline report");
		Assert.assertEquals(report.getDescription(), "Sum of Amount", "Invalid description in headline report");
		successfulTest = true;
	}
}
