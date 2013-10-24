package com.gooddata.qa.graphene.project;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "GoodSalesSchedules" }, description = "Tests for GoodSales project (email schedules functionality) in GD platform")
public class GoodSalesEmailSchedulesTest extends GoodSalesAbstractTest {
	
	private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "schedules" })
	public void verifyEmptySchedules() {
		EmailSchedulePage schedulesPage = initEmailSchedulesPage();
		Assert.assertEquals(schedulesPage.getNumberOfSchedules(), 0, "There are some not expected schedules");
		Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
	}
	
	@Test(dependsOnMethods = { "verifyEmptySchedules" }, groups = { "schedules" })
	public void createDashboardSchedule() {
		EmailSchedulePage schedulesPage = initEmailSchedulesPage();
		schedulesPage.scheduleNewDahboardEmail(user, "UI-Graphene-core-Dashboard", "Scheduled email test - dashboard.", "Outlook");
		checkRedBar();
		Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "verifyEmptySchedules" }, groups = { "schedules" })
	public void createReportSchedule() {
		EmailSchedulePage schedulesPage = initEmailSchedulesPage();
		schedulesPage.scheduleNewReportEmail(user, "UI-Graphene-core-Report", "Scheduled email test - report.", "Activities by Type", ExportFormat.ALL);
		checkRedBar();
		Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
	}
	
	@Test(groups = { "lastTest" }, dependsOnGroups = { "schedules" })
	public void verifyCreatedSchedules() {
		EmailSchedulePage schedulesPage = initEmailSchedulesPage();
		Assert.assertEquals(schedulesPage.getNumberOfSchedules(), 2, "2 schedules weren't created properly");
		Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
		successfulTest = true;
	}
	
	private EmailSchedulePage initEmailSchedulesPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|emailSchedulePage");
		waitForSchedulesPageLoaded();
		waitForElementNotVisible(BY_SCHEDULES_LOADING);
		return Graphene.createPageFragment(EmailSchedulePage.class, browser.findElement(BY_SCHEDULES_PAGE_PANEL));
	}

}
