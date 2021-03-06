package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.awt.AWTException;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.initColorPalette;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;

public class ApplyColorPaletteOnAnalyzePageTest extends AbstractAnalyseTest {

    private static final String SINGLE_METRIC_APPLY_COLOR_PALETTE = "Single_Metric_Apply_Color_Palette";
    private static final String MULTI_METRIC_APPLY_COLOR_PALETTE = "Multi_Metric_Apply_Color_Palette";
    private static final String NEW_INSIGHT_APPLY_COLOR_PALETTE = "New_Insight_Apply_Color_Palette";
    private static final String SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE = "Save_As_Insight_Apply_Color_Palette";
    private static final String DATE_FILTER_THIS_MONTH = "This month";
    private static List<Pair<String, ColorPalette>> listColorPalettes = Arrays.asList(Pair.of("guid1", ColorPalette.RED),
            Pair.of("guid2", ColorPalette.GREEN), Pair.of("guid3", ColorPalette.BLUE), Pair.of("guid4", ColorPalette.YELLOW));
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Apply-Color-Palette-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        getMetricCreator().createSnapshotBOPMetric();
        indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest.setColor(initColorPalette(listColorPalettes));
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsightsApplyColorsPalette() {
        AnalysisPage analysisPage = initAnalysePage();
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getMetricsBucket();
        analysisPage.saveInsight(SINGLE_METRIC_APPLY_COLOR_PALETTE);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.saveInsight(MULTI_METRIC_APPLY_COLOR_PALETTE);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testInsightWithSingleMetricApplyColorPalette() {
        ChartReport chartReport = initAnalysePage()
                .openInsight(SINGLE_METRIC_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testInsightWithMultiMetricApplyColorPalette() {
        ChartReport chartReport = initAnalysePage()
                .openInsight(MULTI_METRIC_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnGroups = "createProject")
    public void checkCreateAndSaveNewInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.COLUMN_CHART)
                .waitForReportComputing()
                .getChartReport();
        analysisPage.getPageHeader().saveInsight(NEW_INSIGHT_APPLY_COLOR_PALETTE);
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"checkCreateAndSaveNewInsightApplyColorPalette"})
    public void checkOpenAndSaveAsInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(NEW_INSIGHT_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
        analysisPage.getPageHeader().saveInsightAs(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE);
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"checkOpenAndSaveAsInsightApplyColorPalette"})
    public void checkReportInsightApplyColorPalette() throws AWTException {
        BrowserUtils.zoomBrowser(browser);
        browser.navigate().refresh();
        initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE).waitForReportComputing().exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            takeScreenshot(browser, "checkReportInsightApplyColorPalette", getClass());
            assertEquals(reportPage.waitForReportExecutionProgress().checkColorColumn(0), ColorPalette.RED.toReportFormatString());
            assertEquals(reportPage.getReportLegendColors(), asList(ColorPalette.RED.toReportFormatString(), ColorPalette.GREEN.toReportFormatString()));
        } finally {
            BrowserUtils.resetZoomBrowser(browser);
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnMethods = {"checkReportInsightApplyColorPalette"})
    public void checkUndoAndRedoInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        analysisPage.addMetric(METRIC_SNAPSHOT_BOP);
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString(), ColorPalette.BLUE.toString()));
        analysisPage.undo();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
        analysisPage.redo();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString(), ColorPalette.BLUE.toString()));
        analysisPage.getPageHeader().getResetButton();
        assertTrue(analysisPage.getPageHeader().isResetButtonEnabled());
    }

    @Test(dependsOnMethods = {"checkReportInsightApplyColorPalette"})
    public void checkEditAndRemoveInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE);
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
        analysisPage.addAttribute(ATTR_DEPARTMENT);
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
        analysisPage.reorderMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT);
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"checkEditAndRemoveInsightApplyColorPalette"})
    public void checkSwitchToBarChartInInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .changeReportType(ReportType.BAR_CHART).waitForReportComputing().getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"checkEditAndRemoveInsightApplyColorPalette"})
    public void checkSwitchToLineChartInInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .changeReportType(ReportType.LINE_CHART).waitForReportComputing().getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = {"checkSwitchToLineChartInInsightApplyColorPalette"})
    public void checkFilterPreviousPeriodInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.addDateFilter()
                .getFilterBuckets()
                .configDateFilter(DATE_FILTER_THIS_MONTH)
                .openDateFilterPickerPanel()
                .applyCompareType(CompareTypeDropdown.CompareType.PREVIOUS_PERIOD);
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.LIGHT_RED.toString(), ColorPalette.RED.toString()));
    }

    @Test(dependsOnMethods = {"checkFilterPreviousPeriodInsightApplyColorPalette"})
    public void checkFilterSamePeriodPreviousYearInsightApplyColorPalette() {
        ChartReport chartReport = initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.addDateFilter()
                .getFilterBuckets()
                .configDateFilter(DATE_FILTER_THIS_MONTH)
                .openDateFilterPickerPanel()
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.LIGHT_RED.toString(), ColorPalette.RED.toString()));
    }

    @Test(dependsOnMethods = {"checkFilterSamePeriodPreviousYearInsightApplyColorPalette"})
    public void checkDeleteInsightApplyColorPalette() {
        initAnalysePage().openInsight(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .getPageHeader()
                .expandInsightSelection()
                .getInsightItem(SAVE_AS_INSIGHT_APPLY_COLOR_PALETTE)
                .delete();
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), "Untitled insight", "Insight has been deleted ");
    }

    @Test(dependsOnMethods = {"checkDeleteInsightApplyColorPalette"})
    public void singleAndMultiInsightNotApplyColorPalette() {
        indigoRestRequest.deleteColorsPalette();
        ChartReport chartReport = initAnalysePage()
                .openInsight(MULTI_METRIC_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString()));
    }
}
