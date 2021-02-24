package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightFromKDDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.*;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel.LogicalOperator.LESS_THAN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.*;

public class KPIDashboardsEditInsightFromKDTest extends AbstractDashboardTest {
    private String hyperlinkType = "GDC.link";
    private String dashboardID = "#dashboard_id";
    private final String UNDO_MESSAGE = "Undo button should be ";
    private final String REDO_MESSAGE = "Redo button should be ";
    private final String RESET_MESSAGE = "Reset button should be ";
    private final String CANCEL_MESSAGE = "Cancel button should be ";
    private final String COLUMN_INSIGHT = "Existing Insight";
    private final String DASHBOARD_HAS_TABLE = "Dashboard Table";
    private final String DASHBOARD_HAS_TABLE_EMBEDDED = "Dashboard Table Embedded";
    private final String PIVOT_TABLE = "Pivot Table";
    private final String PIVOT_TABLE_EMBEDDED = "Pivot Table Embedded";
    private final String DASHBOARD_HAS_INSIGHT = "Dashboard Insight";
    private final String DASHBOARD_WITHOUT_TITLE_TEST = "Without title";
    private final String DASHBOARD_WITH_TITLE_TEST = "With title";
    private final String COLUMN_CHART = "Column Chart";
    private final String PIVOT_TABLE_TEST_WITHOUT_NAME = "Table test without title";
    private final String PIVOT_TABLE_TEST_WITH_NAME = "Pivot test with title";
    private final String PIVOT_TABLE_RENAME = "Rename Table";
    private final String ADAM_BRADLEY = "Adam Bradley";

    private final String TABLE_HAS_MEASURES_ROWS_COLUMNS = "Table has measure rows and columns";
    private final String COLUMN_CHART_MEASURES_STACKBY_VIEWBY = "Column chart only measures, stackby and viewby";
    private final String HEAT_MAP_MEASURES_ROW_COLUMN = "Heat map only measures, row and column";
    private final String LINE_CHART_MEASURES_TRENDBY_SEGMENTBY = "Line chart only measures, trendby and segmentby";
    private final String BULLET_CHART_MEASURES_VIEWBYS = "Bullet chart only measures and two viewbys";

    private final String DASHBOARD_TABLE_HAS_MEASURES_ROWS_COLUMNS = "Has table";
    private final String DASHBOARD_COLUMN_CHART_MEASURES_STACKBY_VIEWBY = "Has columnChart";
    private final String DASHBOARD_HEAT_MAP_MEASURES_ROW_COLUMN = "Has Heatmap";
    private final String DASHBOARD_LINE_CHART_MEASURES_TRENDBY_SEGMENTBY = "Has LineChart";
    private final String DASHBOARD_BULLET_CHART_MEASURES_VIEWBYS = "Has BulletChart";



    private String today;
    private IndigoRestRequest indigoRestRequest;
    private InsightConfigurationPanel insightConfigurationPanel;
    ProjectRestRequest projectRestRequest;
    AttributeRestRequest attributeRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createAmountBOPMetric();
        getMetricCreator().createBestCaseMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_NEW_INSIGHT, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, true);

        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest.setDrillDown(ATTR_SALES_REP, getAttributeDisplayFormUri(ATTR_DEPARTMENT));
        attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_REGION, hyperlinkType);

        createInsight(COLUMN_INSIGHT, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_HAS_MEASURES_ROWS_COLUMNS, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE),
            Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_IS_ACTIVE, CategoryBucket.Type.COLUMNS)));
        createColumnChart(COLUMN_CHART_MEASURES_STACKBY_VIEWBY, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW),
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createHeatMap(HEAT_MAP_MEASURES_ROW_COLUMN, METRIC_AMOUNT, asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW),
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createLineChart(LINE_CHART_MEASURES_TRENDBY_SEGMENTBY, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.TREND),
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.SEGMENT)));
        createBulletChart(BULLET_CHART_MEASURES_VIEWBYS, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_BEST_CASE, asList(
            Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW), Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @DataProvider(name = "tableData")
    public Object[][] getPivotTable() {
        return new Object[][]{
            {
                DASHBOARD_TABLE_HAS_MEASURES_ROWS_COLUMNS,TABLE_HAS_MEASURES_ROWS_COLUMNS,
                asList("false","true"), asList("Direct Sales", "Inside Sales"),
                asList("$17,474,336.77", "$23,361.41","$60,840,366.32","$34,411.97", "$10,542,759.65","$10,196.09", "$27,767,993.80","$12,627.56")}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the creating new table on KD")
    public void editInsightFromKD() {
        initIndigoDashboardsPage().addDashboard().addInsight(COLUMN_INSIGHT).selectWidgetByHeadline(Insight.class, COLUMN_INSIGHT);
        insightConfigurationPanel = indigoDashboardsPage.getInsightConfigurationPanel();
        AnalysisPage editInsight = insightConfigurationPanel.clickEditInsight();
        editInsight.removeColumn(ATTR_REGION).addAttribute(ATTR_REGION).saveInsight();

        SaveInsightFromKDDialog saveInsightFromKDDialogs = SaveInsightFromKDDialog.getInstance(browser);
        saveInsightFromKDDialogs.createCopy("abc");

        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();

        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "01/01/201sdkfsndfk2–12/31/2012");
    }

    private String createInsight(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.TABLE)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createColumnChart(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.COLUMN_CHART)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createHeatMap(String insightTitle, String metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.HEAT_MAP)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricsTitle))))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createLineChart(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.LINE_CHART)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createBulletChart(String insightTitle, String metricsTitle, String metricsSecondaryTitle, String metricTertiaryTitle, List<Pair<String,
        CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.BULLET_CHART)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricsTitle)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(metricsSecondaryTitle), MeasureBucket.Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(metricTertiaryTitle), MeasureBucket.Type.TERTIARY_MEASURES)))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }
}
