package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkBlueBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForReportsPageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.google.common.base.Predicate;

public class GoodSalesSaveReportTest extends GoodSalesAbstractTest {

    private static final String VERSION_REPORT = "Version Report";
    private static final String VERSION_REPORT_2 = VERSION_REPORT + "(2)";
    private static final String STAGE_DURATION_DRILL_IN = "Stage Duration [Drill-In]";
    private static final String QTD_GOAL = "QTD Goal";
    private static final String TOTAL_LOST = "Total Lost [hl]";
    private static final String TOTAL_WON = "Total Won [hl]";

    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    private static final String ACTIVITY_TYPE = "Activity Type";
    private static final String IS_WON = "Is Won?";
    private static final String ACCOUNT = "Account";

    private static final String UNSORTED = "Unsorted";

    private static final By UNSAVED_CHANGES_DONT_SAVE_BUTTON =
            cssSelector(".s-unsaved-changes-dialog .s-btn-don_t_save");
    private static final By UNSAVED_CHANGES_CANCEL_BUTTON = cssSelector(".s-unsaved-changes-dialog .s-btn-cancel");
    private static final By UNSAVED_CHANGES_SAVE_BUTTON = cssSelector(".s-unsaved-changes-dialog .s-btn-save");
    private static final By CREATE_REPORT_BUTTON = cssSelector(".s-saveReportDialog .s-btn-create");
    private static final By WARNING_DIALOG_SAVE_BUTTON = cssSelector(".c-dashboardUsageWarningDialog .s-btn-save");

    @BeforeClass(alwaysRun = true)
    public void before() {
        projectTitle = "GoodSales-save-report-test";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void openUpToDateReport() {
        createReport(new UiReportDefinition().withName(VERSION_REPORT).withWhats(NUMBER_OF_ACTIVITIES),
                "openUpToDateReport");
        initPulsePage();
        waitForFragmentVisible(pulsePage).openUptoDateReport(VERSION_REPORT);
        int versionsCount = waitForFragmentVisible(reportPage).getVersionsCount();
        takeScreenshot(browser, "openUpToDateReport - get versions", getClass());
        assertThat(versionsCount, equalTo(1));
    }

    @Test(dependsOnMethods = {"openUpToDateReport"})
    public void workWithOldVersion() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(VERSION_REPORT);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).getVisualiser()
            .selectHowArea(asList(new HowItem(ACTIVITY_TYPE)))
            .finishReportChanges();
        int versionsCount = waitForReportLoading().saveReport().getVersionsCount();
        takeScreenshot(browser, "workWithOldVersion - get versions", getClass());
        assertThat(versionsCount, equalTo(2));

        reportPage.openVersion(1);
        checkBlueBar(browser);
        waitForAnalysisPageLoaded(browser);
        assertTrue(reportPage.getVisualiser().verifyOldVersionState());
        reportPage.setReportName(VERSION_REPORT_2);
        sleepTightInSeconds(3);
        reportPage.revertToCurrentVersion();
        assertTrue(waitForReportLoading().hasUnsavedVersion());
        takeScreenshot(browser, "workWithOldVersion - hasUnsavedVersion", getClass());
        assertThat(reportPage.saveReport().getVersionsCount(), equalTo(3));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void saveTooLargeReport() {
        createReport(new UiReportDefinition().withName("R1").withWhats("Amount").withHows("Opp. Snapshot"),
                "saveTooLargeReport");
        waitForAnalysisPageLoaded(browser);
        assertTrue(waitForFragmentVisible(reportPage).isReportTooLarge());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void leaveUnsavedChangesInNewReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(UNSORTED);
        waitForReportsPageLoaded(browser);
        int currentReportsCount = reportsPage.getReportsList().getNumberOfReports();

        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        sleepTightInSeconds(3);
        reportPage.getVisualiser()
            .selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)))
            .finishReportChanges();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_DONT_SAVE_BUTTON, browser).click();

        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder("Unsorted");
        waitForReportsPageLoaded(browser);
        assertThat(reportsPage.getReportsList().getNumberOfReports(), equalTo(currentReportsCount));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelLeavingUnsavedChangesInNewReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(UNSORTED);
        waitForReportsPageLoaded(browser);

        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        sleepTightInSeconds(3);
        reportPage.getVisualiser()
            .selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)))
            .finishReportChanges();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_CANCEL_BUTTON, browser).click();
        waitForFragmentVisible(reportPage);
    }

    @Test(dependsOnMethods = {"openUpToDateReport"})
    public void leaveAndDontSaveOldReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(VERSION_REPORT);
        waitForAnalysisPageLoaded(browser);
        int versionCount = waitForFragmentVisible(reportPage).getVersionsCount();
        reportPage.getVisualiser()
            .selectHowArea(asList(new HowItem(ACCOUNT)))
            .finishReportChanges();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_DONT_SAVE_BUTTON, browser).click();

        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(VERSION_REPORT);
        assertThat(waitForFragmentVisible(reportPage).getVersionsCount(), equalTo(versionCount));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void leaveAndSaveNewReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(UNSORTED);
        waitForReportsPageLoaded(browser);
        int currentReportsCount = reportsPage.getReportsList().getNumberOfReports();

        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        sleepTightInSeconds(3);
        String reportName = "Leave & Save";
        reportPage.setReportName(reportName);
        reportPage.getVisualiser()
            .selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)))
            .finishReportChanges();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_SAVE_BUTTON, browser).click();
        waitForElementVisible(CREATE_REPORT_BUTTON, browser).click();

        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(UNSORTED);
        waitForReportsPageLoaded(browser);
        assertThat(reportsPage.getReportsList().getNumberOfReports(), equalTo(currentReportsCount + 1));
        reportsPage.getReportsList().openReport(reportName);
    }

    @Test(dependsOnMethods = {"openUpToDateReport"})
    public void leaveAndSaveOldReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(TOTAL_LOST);
        waitForAnalysisPageLoaded(browser);
        int versionCount = waitForFragmentVisible(reportPage).getVersionsCount();
        reportPage.getVisualiser()
            .selectHowArea(asList(new HowItem(ACCOUNT)))
            .finishReportChanges();
        waitForReportLoading();

        moveToAnotherPage();
        waitForElementVisible(UNSAVED_CHANGES_SAVE_BUTTON, browser).click();
        waitForElementVisible(WARNING_DIALOG_SAVE_BUTTON, browser).click();

        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(TOTAL_LOST);
        assertThat(waitForFragmentVisible(reportPage).getVersionsCount(), equalTo(versionCount + 1));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelComputingInNewReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        sleepTightInSeconds(3);
        reportPage.setReportName("R1");
        reportPage.getVisualiser()
            .selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)))
            .finishReportChanges();

        if (tryCancelReportComputing()) {
            assertThat(reportPage.getExecuteProgressStatus(), equalTo("Report computation canceled."));

            reportPage.recompute();
            waitForReportLoading().createReport();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelComputingOldReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(STAGE_DURATION_DRILL_IN);
        if (tryCancelReportComputing()) {
            assertThat(reportPage.getExecuteProgressStatus(), equalTo("Report computation canceled."));
            reportPage.recompute();
        }
    }

    @Test(dependsOnMethods = {"workWithOldVersion"})
    public void cancelComputingOldVersionOfReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(VERSION_REPORT_2);
        waitForAnalysisPageLoaded(browser);
        reportPage.openVersion(1);

        if (tryCancelReportComputing()) {
            assertThat(reportPage.getExecuteProgressStatus(), equalTo("Report computation canceled."));
        }
    }

    @Test(dependsOnMethods = {"workWithOldVersion"})
    public void saveAsReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(VERSION_REPORT_2);
        waitForAnalysisPageLoaded(browser);
        reportPage.saveAsReport();
        waitForReportLoading();
        sleepTightInSeconds(2);

        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport("Copy of " + VERSION_REPORT_2);
        assertThat(waitForFragmentVisible(reportPage).getVersionsCount(), equalTo(1));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void saveReportPlacedOnDashboard() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(TOTAL_LOST);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).getVisualiser()
            .selectHowArea(asList(new HowItem(IS_WON)))
            .finishReportChanges();
        waitForReportLoading();
        reportPage.saveReport();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void saveAsReportPlacedOnDashboard() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(QTD_GOAL);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).getVisualiser()
            .selectHowArea(asList(new HowItem(IS_WON)))
            .finishReportChanges();
        waitForReportLoading();
        reportPage.saveAsReport();
        sleepTightInSeconds(3);

        ApplicationHeaderBar.goToReportsPage(browser);
        waitForReportsPageLoaded(browser);
        waitForFragmentVisible(reportsPage).getReportsList().openReport("Copy of " + QTD_GOAL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelReportPlacedOnDashboard() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder("All");
        reportsPage.getReportsList().openReport(TOTAL_WON);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).getVisualiser()
            .selectHowArea(asList(new HowItem(IS_WON)))
            .finishReportChanges();
        waitForReportLoading();
        reportPage.cancelSaveUsedReport();
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
}