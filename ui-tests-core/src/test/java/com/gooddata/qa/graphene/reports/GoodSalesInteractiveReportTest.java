package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.fragments.reports.report.InteractiveReportWidget.ChartType.BAR_CHART;
import static com.gooddata.qa.graphene.fragments.reports.report.InteractiveReportWidget.ChartType.LINE_CHART;
import static com.gooddata.qa.graphene.fragments.reports.report.InteractiveReportWidget.ChartType.AREA_CHART;

import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.report.InteractiveReportWidget;

@Test(groups = {"interactiveReportTest"}, description = "Test Interactive Report on dashboard")
public class GoodSalesInteractiveReportTest extends GoodSalesAbstractTest {

    private static final String DASHBOARD_NAME = "Interactive Report";

    @FindBy(xpath = "//iframe[contains(@src,'iaa/interactive_report')]")
    private InteractiveReportWidget interactiveReport;

    private static final long expectedDashboardExportSize = 39000L;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-interactive-report";
    }
    
    @Test(dependsOnMethods = {"createProject"})
    public void verifyTooLargeLineChart() throws InterruptedException {
        verifyTooLargeChartReport(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyTooLargeAreaChart() throws InterruptedException {
        verifyTooLargeChartReport(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyTooLargeBarChart() throws InterruptedException {
        verifyTooLargeChartReport(BAR_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyInvalidConfigurationLineChart() throws InterruptedException {
        verifyInvalidConfigurationChartReport(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyInvalidConfigurationAreaChart() throws InterruptedException {
        verifyInvalidConfigurationChartReport(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyInvalidConfigurationBarChart() throws InterruptedException {
        verifyInvalidConfigurationChartReport(BAR_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyLineChart() throws InterruptedException {
        verifyChartReport(LINE_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyAreaChart() throws InterruptedException {
        verifyChartReport(AREA_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyBarChart() throws InterruptedException {
        verifyChartReport(BAR_CHART);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workingOnColor() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            interactiveReport.enableChartColor()
                             .configureColor("Stage", "Status");
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);

            assertTrue(interactiveReport.isColorAppliedOnChart() &&
                       interactiveReport.isChartTableContainsHeader("Status") &&
                       interactiveReport.areTableValuesInSpecificRowMatchedChartLegendNames("Status"),
                       String.format("There is something wrong when applying color attribute '%s' to report!", "Status"));
    
            int totalTableRows = interactiveReport.getTotalTableRows();
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", totalTableRows));
    
            assertTrue(interactiveReport.clickOnSeries(0)
                                        .areTrackersSelectedWhenClickOnSeries(BAR_CHART, 0),
                       String.format("Trackers are not selected when click on series index [%d]!", 0));
    
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("6 selected out of %d total", totalTableRows));
    
            assertTrue(interactiveReport.resetTrackerSelection()
                                        .isTrackerSelectionReset(BAR_CHART),
                       "Chart is not reset!");
    
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", totalTableRows));
    
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            interactiveReport.disableChartColor();
            Thread.sleep(2000);
    
            assertTrue(interactiveReport.isChartSeriesReset(),
                       "Chart series is not reset!");
    
            assertFalse(interactiveReport.isChartTableContainsHeader("Status"),
                        "Explorer table still contains column 'Status'!");
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workingOnTabularExplorer() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            interactiveReport.enableAbilityToAddMoreTableAttributes()
                             .addMoreTableAttributes("Product", "Product");
            assertTrue(interactiveReport.isChartTableContainsHeader("Product Name"),
                       "Explorer table does not contains column 'Product Name'!");
    
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);
    
            assertTrue(interactiveReport.isChartTableContainsHeader("Product Name"),
                       "Explorer table does not contains column 'Product Name'!");
    
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            interactiveReport.deleteTableAttribute("Product");
            Thread.sleep(2000);
    
            assertFalse(interactiveReport.isChartTableContainsHeader("Product Name"),
                        "Explorer table still contains column 'Product Name'!");
    
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);
    
            assertFalse(interactiveReport.isChartTableContainsHeader("Product Name"),
                        "Explorer table still contains column 'Product Name'!");
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workingOnExplorerTitle() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            interactiveReport.changeReportTitle("New report title");
            Thread.sleep(1000);
            interactiveReport.changeReportSubtitle("New report subtitle");
            Thread.sleep(1000);
            verifyReportTitleAndSubtitleInEditMode();
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(1000);
            verifyReportTitleAndSubtitleInViewMode();
    
            dashboardsPage.editDashboard();
            Thread.sleep(1000);
            interactiveReport.changeReportTitle("You will not see this title");
            interactiveReport.changeReportSubtitle("You will not see this subtitle");
            Thread.sleep(1000);
            dashboardEditBar.cancelDashboard();
            Thread.sleep(2000);
            verifyReportTitleAndSubtitleInViewMode();
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void fillterOutAllValues() throws InterruptedException {
        try{
            addReportInNewDashboard(BAR_CHART);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Stage Name");
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);

            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            assertEquals(interactiveReport.getTotalTableRows(), 8);
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", 8));

            dashboardsPage.getFirstFilter().changeAttributeFilterValue("Interest");
            Thread.sleep(2000);

            assertEquals(interactiveReport.getTotalTableRows(), 1);
            assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                         String.format("%d total", 1));
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void shareAndExportDashboard() throws InterruptedException {
        try {
            addReportInNewDashboard(BAR_CHART);

            browser.navigate().to(dashboardsPage.embedDashboard().getPreviewURI());
            waitForElementVisible(interactiveReport.getRoot());
            Thread.sleep(2000);
            testDisplaying(BAR_CHART);

            Thread.sleep(2000);
            initDashboardsPage();
            String exportedDashboardName = dashboardsPage.exportDashboardTab(0);
            verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
    
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    private void addReportInNewDashboard(InteractiveReportWidget.ChartType type) throws InterruptedException {
        Thread.sleep(2000);
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard(DASHBOARD_NAME + System.currentTimeMillis());
        dashboardsPage.editDashboard();
        Thread.sleep(1000);
        dashboardEditBar.initInteractiveReportWidget();
        Thread.sleep(1000);
        interactiveReport.selectChartType(type)
                         .configureYAxis("Sales Figures", "Amount")
                         .configureXAxisWithValidAttribute("Stage", "Stage Name");
        Thread.sleep(1000);
        dashboardEditBar.saveDashboard();
        Thread.sleep(2000);
    }

    private void verifyChartReport(InteractiveReportWidget.ChartType type)
            throws InterruptedException {
        try {
            addReportInNewDashboard(type);
            testDisplaying(type);
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    private void testDisplaying(InteractiveReportWidget.ChartType type) {
        assertTrue(interactiveReport.hoverOnTracker(0)
                                    .isTooltipVisible(),
                   "Tooltip is not visible!");

        int totalTableRows = interactiveReport.getTotalTableRows();
        assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                     String.format("%d total", totalTableRows));

        assertTrue(interactiveReport.clickOnTracker(0)
                                    .isTrackerSelected(type, 0),
                   String.format("Tracker index [%d] is not selected!", 0));

        assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                     String.format("1 selected out of %d total", totalTableRows));

        assertTrue(interactiveReport.resetTrackerSelection()
                                    .isTrackerSelectionReset(type),
                   "Chart is not reset!");

        assertEquals(interactiveReport.getTotalTableRowsFromTableFooter(),
                     String.format("%d total", totalTableRows));
    }

    private void verifyReportTitleAndSubtitleInViewMode() {
        verifyReportTitleAndSubtitle(false);
    }

    private void verifyReportTitleAndSubtitleInEditMode() {
        verifyReportTitleAndSubtitle(true);
    }

    private void verifyReportTitleAndSubtitle(boolean editMode) {
        assertEquals(interactiveReport.getReportTitle(editMode), "New report title");
        assertEquals(interactiveReport.getReportSubtitle(editMode), "New report subtitle");
    }

    private void verifyTooLargeChartReport(InteractiveReportWidget.ChartType type)
            throws InterruptedException {
        try {
            Thread.sleep(2000);
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.addNewDashboard(DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            dashboardEditBar.initInteractiveReportWidget();
            Thread.sleep(2000);
            interactiveReport.selectChartType(type)
                             .configureYAxis("Sales Figures", "Amount")
                             .configureXAxisWithValidAttribute("Account", "Account");
            assertTrue(interactiveReport.isChartAlertMessageVisible(),
                       "'Too many data points' message is not shown!");
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);
            assertTrue(interactiveReport.isChartAlertMessageVisible(),
                       "'Too many data points' message is not shown!");
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }

    private void verifyInvalidConfigurationChartReport(InteractiveReportWidget.ChartType type)
            throws InterruptedException {
        try {
            Thread.sleep(2000);
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.addNewDashboard(DASHBOARD_NAME);
            Thread.sleep(2000);
            dashboardsPage.editDashboard();
            Thread.sleep(2000);
            dashboardEditBar.initInteractiveReportWidget();
            Thread.sleep(2000);
            interactiveReport.selectChartType(type)
                             .configureYAxis("Sales Figures", "Amount")
                             .configureXAxisWithInvalidAttribute("Activity", "Activity");
            assertTrue(interactiveReport.isChartErrorMessageVisible(),
                       "'Invalid configuration' message is not shown!");
            Thread.sleep(1000);
            dashboardEditBar.saveDashboard();
            Thread.sleep(2000);
            assertTrue(interactiveReport.isChartErrorMessageVisible(),
                       "'Invalid configuration' message is not shown!");
        } finally {
            dashboardsPage.deleteDashboard();
            Thread.sleep(2000);
        }
    }
}
