package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.reports.report.ExportXLSXDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;

public class EmbeddedDashboard extends DashboardsPage {

    public static final By LOCATOR = By.id("root");

    private static final By BY_DASHBOARD_LOADED = By.cssSelector("#p-projectDashboardPage.s-displayed, .s-dashboardLoaded");
    private static final By BY_EDIT_BUTTON = By.className("s-editButton");

    public static final EmbeddedDashboard getInstance(SearchContext context) {
        return Graphene.createPageFragment(EmbeddedDashboard.class, waitForElementVisible(LOCATOR, context));
    }

    public boolean isEditButtonVisible() {
        return isElementVisible(BY_EDIT_BUTTON, getRoot());
    }

    public static void waitForDashboardLoaded(SearchContext searchContext) {
        waitForElementVisible(BY_DASHBOARD_LOADED, searchContext);
    }

    public EmbeddedReportPage openEmbeddedReportPage() {
        editDashboard().expandReportMenu();

        waitForElementVisible(By.className("s-btn-new_report"), browser).click();

        EmbeddedReportPage.waitForPageLoaded(browser);

        return Graphene.createPageFragment(EmbeddedReportPage.class,
                waitForElementVisible(EmbeddedReportPage.LOCATOR, browser));
    }

    public ReportsPage openEmbeddedReportsPage() {
        editDashboard().expandReportMenu();

        waitForElementVisible(By.className("s-btn-manage"), browser).click();
        return ReportsPage.getInstance(browser);
    }

    public String exportDashboardToXLSX(String dashboardName) {
        int exportingTextDisplayedTimeoutInSeconds = 600;
        openEditExportEmbedMenu().select(ExportFormat.DASHBOARD_XLSX.getLabel());
        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.confirmExport();

        waitForElementVisible(BY_EXPORTING_PANEL, browser);
        String fileName = dashboardName + " " + exportXLSXDialog.getExportDashboardFormat() + ".xlsx";
        sleepTightInSeconds(3);
        waitForElementNotPresent(BY_EXPORTING_PANEL, exportingTextDisplayedTimeoutInSeconds);
        sleepTightInSeconds(3);

        return fileName;
    }

    @Override
    public DashboardEditBar editDashboard() {
        if (!isElementPresent(BY_DASHBOARD_EDIT_BAR, browser)) {
            openEditExportEmbedMenu().select("Edit");
        }

        return getDashboardEditBar();
    }

    @Override
    public DashboardScheduleDialog showDashboardScheduleDialog() {
        waitForDashboardLoaded(browser);
        waitForElementVisible(scheduleButton).click();
        waitForElementVisible(scheduleDialog.getRoot());
        return scheduleDialog;
    }
}
