package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkBlueBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_DATE_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_SALES_REPS_BY_WON_AND_LOST;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.java.Builder;
import com.google.common.base.Predicate;

public class GoodSalesSaveReportTest extends GoodSalesAbstractTest {

    private static final String VERSION_REPORT = "Version Report";
    private static final String VERSION_REPORT_2 = VERSION_REPORT + "(2)";

    private static final String DASHBOARD_HAS_ONE_REPORT = "Dashboard has one report";
    private static final String UNSORTED = "Unsorted";
    private static final String ALL = "All";

    private static final By UNSAVED_CHANGES_DONT_SAVE_BUTTON =
            cssSelector(".s-unsaved-changes-dialog .s-btn-don_t_save");
    private static final By UNSAVED_CHANGES_CANCEL_BUTTON = cssSelector(".s-unsaved-changes-dialog .s-btn-cancel");
    private static final By UNSAVED_CHANGES_SAVE_BUTTON = cssSelector(".s-unsaved-changes-dialog .s-btn-save");
    private static final By CREATE_REPORT_BUTTON = cssSelector(".s-saveReportDialog .s-btn-create");
    private static final By WARNING_DIALOG_SAVE_BUTTON = cssSelector(".c-dashboardUsageWarningDialog .s-btn-save");

    private String activitiesByTypeReport;
    private String amountByProductReport;
    private String amountByDateClosedReport;
    private String topSalesRepsByWonAndLostReport;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-save-report-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        createAmountMetric();
        createNumberOfActivitiesMetric();
        createReport(new UiReportDefinition().withName(VERSION_REPORT).withWhats(METRIC_NUMBER_OF_ACTIVITIES),
                "openUpToDateReport");
        activitiesByTypeReport = createActivitiesByTypeReport();
        amountByProductReport = createAmountByProductReport();
        amountByDateClosedReport = createAmountByDateClosedReport();
        topSalesRepsByWonAndLostReport = createTopSalesRepsByWonAndLostReport();
        // create dashboard contains report which will be used in test case 
        // to check some validations.
        DashboardsRestUtils.createDashboard(getRestApiClient(), testParams.getProjectId(),
                initDashboardHavingReport(DASHBOARD_HAS_ONE_REPORT).getMdObject());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void workWithOldVersion() {
        initReportsPage()
            .openReport(VERSION_REPORT)
            .initPage()
            .openHowPanel()
            .selectAttribute(ATTR_ACTIVITY_TYPE)
            .doneSndPanel();
        int versionsCount = waitForReportLoading().saveReport().getVersionsCount();
        takeScreenshot(browser, "workWithOldVersion - get versions", getClass());
        assertThat(versionsCount, equalTo(2));

        reportPage.openVersion(1);
        checkBlueBar(browser);
        waitForAnalysisPageLoaded(browser);
        assertTrue(reportPage.verifyOldVersionState());
        reportPage.setReportName(VERSION_REPORT_2);
        sleepTightInSeconds(3);
        reportPage.revertToCurrentVersion();
        assertTrue(waitForReportLoading().hasUnsavedVersion());
        takeScreenshot(browser, "workWithOldVersion - hasUnsavedVersion", getClass());
        assertThat(reportPage.saveReport().getVersionsCount(), equalTo(3));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveTooLargeReport() {
        createReport(new UiReportDefinition().withName("R1").withWhats("Amount").withHows("Opp. Snapshot"),
                "saveTooLargeReport");
        waitForAnalysisPageLoaded(browser);
        assertTrue(waitForFragmentVisible(reportPage).isReportTooLarge());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void leaveUnsavedChangesInNewReport() {
        int currentReportsCount = initReportsPage().openFolder(UNSORTED).getReportsCount();

        ReportsPage.getInstance(browser)
            .startCreateReport()
            .initPage()
            .openWhatPanel()
            .selectMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .doneSndPanel();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_DONT_SAVE_BUTTON, browser).click();
        waitForDataPageLoaded(browser);

        assertThat(initReportsPage().openFolder(UNSORTED).getReportsCount(), equalTo(currentReportsCount));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelLeavingUnsavedChangesInNewReport() {
        initReportsPage()
            .openFolder(UNSORTED)
            .startCreateReport()
            .initPage()
            .openWhatPanel()
            .selectMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .doneSndPanel();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_CANCEL_BUTTON, browser).click();
        waitForFragmentVisible(reportPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void leaveAndDontSaveOldReport() {
        initReportsPage().openReport(VERSION_REPORT);
        waitForAnalysisPageLoaded(browser);
        int versionCount = waitForFragmentVisible(reportPage).getVersionsCount();
        reportPage.openHowPanel()
            .selectAttribute(ATTR_ACCOUNT)
            .doneSndPanel();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_DONT_SAVE_BUTTON, browser).click();
        waitForDataPageLoaded(browser);

        initReportsPage().openReport(VERSION_REPORT);
        assertThat(waitForFragmentVisible(reportPage).getVersionsCount(), equalTo(versionCount));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void leaveAndSaveNewReport() {
        int currentReportsCount = initReportsPage().openFolder(UNSORTED).getReportsCount();
        String reportName = "Leave & Save";

        ReportsPage.getInstance(browser)
            .startCreateReport()
            .initPage()
            .setReportName(reportName)
            .openWhatPanel()
            .selectMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .doneSndPanel();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_SAVE_BUTTON, browser).click();
        waitForElementVisible(CREATE_REPORT_BUTTON, browser).click();
        waitForDataPageLoaded(browser);

        assertThat(initReportsPage().openFolder(UNSORTED).getReportsCount(), equalTo(currentReportsCount + 1));
        ReportsPage.getInstance(browser).openReport(reportName);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void leaveAndSaveOldReport() {
        int versionCount = initReportsPage()
            .openFolder(ALL)
            .openReport(REPORT_ACTIVITIES_BY_TYPE)
            .getVersionsCount();

        reportPage.openHowPanel()
            .selectAttribute(ATTR_ACCOUNT)
            .doneSndPanel();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_SAVE_BUTTON, browser).click();
        waitForElementVisible(WARNING_DIALOG_SAVE_BUTTON, browser).click();
        waitForDataPageLoaded(browser);

        initReportsPage().openReport(REPORT_ACTIVITIES_BY_TYPE);
        assertThat(waitForFragmentVisible(reportPage).getVersionsCount(), equalTo(versionCount + 1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelComputingInNewReport() {
        initReportsPage()
            .startCreateReport()
            .initPage()
            .setReportName("R1")
            .openWhatPanel()
            .selectMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .doneSndPanel();

        if (tryCancelReportComputing()) {
            assertThat(reportPage.getExecuteProgressStatus(), equalTo("Report computation canceled."));

            reportPage.recompute();
            waitForReportLoading()
                .finishCreateReport();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelComputingOldReport() {
        initReportsPage()
            .openFolder(ALL)
            .openReport(REPORT_AMOUNT_BY_PRODUCT);
        if (tryCancelReportComputing()) {
            assertThat(reportPage.getExecuteProgressStatus(), equalTo("Report computation canceled."));
            reportPage.recompute();
        }
    }

    @Test(dependsOnMethods = {"workWithOldVersion"})
    public void cancelComputingOldVersionOfReport() {
        initReportsPage()
            .openReport(VERSION_REPORT_2)
            .openVersion(1);

        if (tryCancelReportComputing()) {
            assertThat(reportPage.getExecuteProgressStatus(), equalTo("Report computation canceled."));
        }
    }

    @Test(dependsOnMethods = {"workWithOldVersion"})
    public void saveAsReport() {
        initReportsPage()
            .openReport(VERSION_REPORT_2)
            .saveAsReport();
        waitForReportLoading();
        sleepTightInSeconds(2);

        initReportsPage().openReport("Copy of " + VERSION_REPORT_2);
        assertThat(waitForFragmentVisible(reportPage).getVersionsCount(), equalTo(1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveReportPlacedOnDashboard() {
        initReportsPage()
            .openFolder(ALL)
            .openReport(REPORT_ACTIVITIES_BY_TYPE)
            .openHowPanel()
            .selectAttribute(ATTR_IS_CLOSED)
            .doneSndPanel();
        waitForReportLoading();
        reportPage.clickSaveReport().confirmSaveReport().waitForReportSaved();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveAsReportPlacedOnDashboard() {
        initReportsPage()
            .openFolder(ALL)
            .openReport(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST)
            .openHowPanel()
            .selectAttribute(ATTR_IS_WON)
            .doneSndPanel();
        waitForReportLoading();
        reportPage.saveAsReport();
        sleepTightInSeconds(3);

        ApplicationHeaderBar.goToReportsPage(browser).openReport("Copy of " + REPORT_TOP_SALES_REPS_BY_WON_AND_LOST);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelReportPlacedOnDashboard() {
        initReportsPage()
            .openFolder(ALL)
            .openReport(REPORT_AMOUNT_BY_DATE_CLOSED)
            .openHowPanel()
            .selectAttribute(ATTR_DATE_CREATED)
            .doneSndPanel();
        waitForReportLoading();
        reportPage.clickSaveReport().cancelSaveReport();
    }

    private void moveToAnotherPage() {
        ApplicationHeaderBar.goToManagePage(browser);
        sleepTightInSeconds(2);
        takeScreenshot(browser, "unsaved change dialog", getClass());
    }

    private ReportPage waitForReportLoading() {
        sleepTightInSeconds(1);
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver browser) {
                return !browser.findElement(id("reportContainerTab")).getAttribute("class")
                        .contains("processingReport");
            }
        });
        return reportPage;
    }

    private boolean tryCancelReportComputing() {
        if (reportPage.tryCancelComputing()) {
            takeScreenshot(browser, "cancel report computing", getClass());
            return true;
        }

        System.out.println("Failed to cancel report computing because report render too fast"
                + " or Selenium too slow to catch it");
        System.out.println("Skip cancel report computing test!");
        return false;
    }

    private Dashboard initDashboardHavingReport(String name) {
        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(name);
            dash.addTab(Builder.of(Tab::new)
                    .with(tab -> tab.setTitle(REPORT_ACTIVITIES_BY_TYPE))
                    .with(tab -> tab.addItems(Arrays.asList(
                            createReportItem(activitiesByTypeReport),
                            createReportItem(amountByProductReport),
                            createReportItem(amountByDateClosedReport),
                            createReportItem(topSalesRepsByWonAndLostReport)
                    )))
                    .build());
        }).build();
    }
}
