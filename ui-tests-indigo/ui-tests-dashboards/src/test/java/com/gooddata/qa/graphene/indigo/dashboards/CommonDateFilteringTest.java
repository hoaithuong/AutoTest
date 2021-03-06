package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;

public class CommonDateFilteringTest extends AbstractDashboardTest {

    private static String UNRELATED_DATE_INSIGHT = "Unrelated-Date-Insight";
    private static String TEST_INSIGHT = "Test-Insight";
    private static String LAST_7_DAYS = "Last 7 days";
    private static String ALL_TIME = "All time";
    private static String THIS_MONTH = "This month";
    private static String INSIGHT_USING_DATE_FILTER = "Insight-Using-Date-Filter";
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Common-Date-Filter-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        getMetricCreator().createNumberOfActivitiesMetric();
        // create an insight without using date filter
        addWidgetToWorkingDashboard(createInsightWidget(new InsightMDConfiguration(TEST_INSIGHT,
                ReportType.COLUMN_CHART).setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(
                getMdService().getObj(getProject(), Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES)))))));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void makeNoChangeOnUnrelatedDateInsight() throws ParseException, JSONException, IOException {
        String insightUri = createUnrelatedDateInsight();
        indigoRestRequest.createVisualizationWidget(insightUri, UNRELATED_DATE_INSIGHT);
        indigoDashboardsPage
            .addInsightToCreateANewRow(UNRELATED_DATE_INSIGHT, TEST_INSIGHT, Widget.FluidLayoutPosition.BOTTOM)
            .saveEditModeWithWidgets();

        try {
            List<String> expectedValues = initIndigoDashboardsPageWithWidgets().switchToEditMode()
                    .selectWidgetByHeadline(Insight.class, UNRELATED_DATE_INSIGHT).getChartReport().getDataLabels();

            indigoDashboardsPage.selectDateFilterByName(LAST_7_DAYS).waitForWidgetsLoading();

            assertEquals(indigoDashboardsPage.getWidgetByHeadline(Insight.class, UNRELATED_DATE_INSIGHT).getChartReport()
                    .getDataLabels(), expectedValues);
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(insightUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void makeNoChangeOnDisabledDateDatasetFilterInsight() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectDateFilterByName(ALL_TIME)
                .waitForWidgetsLoading().selectWidgetByHeadline(Insight.class, TEST_INSIGHT);
        assertFalse(indigoDashboardsPage.getConfigurationPanel().disableDateFilter().isDateDataSetDropdownVisible(),
                "The insight date dataset is not disabled");

        List<String> expectedValues = initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .selectLastWidget(Insight.class).getChartReport().getDataLabels();

        indigoDashboardsPage.selectDateFilterByName(LAST_7_DAYS).waitForWidgetsLoading()
                .selectWidgetByHeadline(Insight.class, TEST_INSIGHT);

        takeScreenshot(browser, "Not-Apply-Common-Date-Filer-If-Date-Dataset-Is-Disabled", getClass());
        assertEquals(indigoDashboardsPage.getWidgetByHeadline(Insight.class, TEST_INSIGHT).getChartReport()
                .getDataLabels(), expectedValues, "The insight is affected by common date filter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void overrideFilterIfInsightUsingSameDateDataset() throws ParseException, JSONException, IOException {
        createInsightUsingDateFilter(INSIGHT_USING_DATE_FILTER);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(INSIGHT_USING_DATE_FILTER)
                    .getConfigurationPanel().selectDateDataSetByName(DATE_DATASET_CREATED);

            indigoDashboardsPage.selectDateFilterByName(ALL_TIME).waitForWidgetsLoading();
            takeScreenshot(browser, "Override-Date-Filter-If-Insight-Uses-Same-Date-Dataset", getClass());
            assertEquals(indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER)
                    .getChartReport().getDataLabels(), singletonList("154,271"),
                "Common date filter does not override insight's filter");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(INSIGHT_USING_DATE_FILTER));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineFiltersIfInsightUsingDifferentDateDataset()
            throws JSONException, IOException, ParseException {
        createInsightUsingDateFilter(INSIGHT_USING_DATE_FILTER);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(INSIGHT_USING_DATE_FILTER)
                    .getConfigurationPanel().selectDateDataSetByName(DATE_DATASET_ACTIVITY);

            indigoDashboardsPage.selectDateFilterByName(ALL_TIME).waitForWidgetsLoading();
            takeScreenshot(browser, "Combine-Filters-If-Insight-Uses-Different-Date-Dataset", getClass());
            assertEquals(indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER)
                    .getChartReport().getDataLabels(), singletonList("23,673"),
                "Combination of common & insight date filer is not correct");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(INSIGHT_USING_DATE_FILTER));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void keepDateFilterAfterEditingChartType() throws JSONException, IOException, ParseException {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter()
                .saveInsight(INSIGHT_USING_DATE_FILTER);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(INSIGHT_USING_DATE_FILTER);
            indigoDashboardsPage.getConfigurationPanel().enableDateFilter().selectDateDataSetByName(DATE_DATASET_ACTIVITY);
            indigoDashboardsPage.waitForWidgetsLoading().selectDateFilterByName(LAST_7_DAYS)
                    .waitForWidgetsLoading().saveEditModeWithWidgets();
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), LAST_7_DAYS,
                    "The expected filter value has not been selected");

            AnalysisPage page = initAnalysePage().openInsight(INSIGHT_USING_DATE_FILTER).waitForReportComputing()
                    .changeReportType(ReportType.BAR_CHART).waitForReportComputing().saveInsight();
            assertEquals(page.getChartReport().getChartType(), ReportType.BAR_CHART.getLabel(),
                    "The chart type is not changed");

            assertEquals(initIndigoDashboardsPageWithWidgets().switchToEditMode().getDateFilterSelection(),
                    LAST_7_DAYS, "Common date filter is changed");

            indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER);
            assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_ACTIVITY,
                    "Date dataset is changed");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(INSIGHT_USING_DATE_FILTER));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void keepDateFilterAfterEditingPeriodFilterOnAD() throws JSONException, IOException, ParseException {
        AnalysisPage page =
                initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter().waitForReportComputing();
        page.getFilterBuckets().configDateFilter(LAST_7_DAYS)
                .getRoot().click(); // close panel
        page.getFilterBuckets().changeDateDimension(DATE_DATASET_ACTIVITY, DATE_DATASET_CREATED);
        page.saveInsight(INSIGHT_USING_DATE_FILTER);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(INSIGHT_USING_DATE_FILTER);
            indigoDashboardsPage.getConfigurationPanel().selectDateDataSetByName(DATE_DATASET_CREATED);
            indigoDashboardsPage.waitForWidgetsLoading().selectDateFilterByName(THIS_MONTH).waitForWidgetsLoading()
                    .saveEditModeWithWidgets();
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), THIS_MONTH,
                    "The expected filter value has not been selected");

            page = initAnalysePage().openInsight(INSIGHT_USING_DATE_FILTER).waitForReportComputing();
            page.getFilterBuckets().configDateFilter(ALL_TIME);
            page.saveInsight().waitForReportComputing();
            assertTrue(page.getFilterBuckets().getDateFilterText().contains(ALL_TIME),
                    "The date period is not changed");

            assertEquals(initIndigoDashboardsPageWithWidgets().switchToEditMode().getDateFilterSelection(),
                    THIS_MONTH, "Common date filter is changed");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(INSIGHT_USING_DATE_FILTER));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void keepDateFilterAfterEditingDateDimensionOnAD() throws ParseException, JSONException, IOException {
        createInsightUsingDateFilter(INSIGHT_USING_DATE_FILTER);

        try {
            assertEquals(
                    initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(INSIGHT_USING_DATE_FILTER)
                            .getConfigurationPanel().getSelectedDataSet(),
                    DATE_DATASET_CREATED, "Date dataset on is not correct");
            indigoDashboardsPage.selectDateFilterByName(ALL_TIME).saveEditModeWithWidgets();

            AnalysisPage page = initAnalysePage().openInsight(INSIGHT_USING_DATE_FILTER).waitForReportComputing();
            page.getFilterBuckets().changeDateDimension(DATE_DATASET_CREATED, DATE_DATASET_ACTIVITY);
            page.waitForReportComputing().saveInsight();

            assertTrue(page.getFilterBuckets().getDateFilterText().contains(DATE_DATASET_ACTIVITY),
                    "Date dimension is not changed");

            assertEquals(
                    initIndigoDashboardsPageWithWidgets().switchToEditMode()
                        .getWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER).getChartReport()
                        .getDataLabels(), singletonList("31,766"),
                    "Changing date dimension on AD makes no impact to the insight on dashboard");

            indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER);
            assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_CREATED,
                    "Date dataset on dashboard is affected by changing date filter on AD");

        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(INSIGHT_USING_DATE_FILTER));
        }
    }

    private String createUnrelatedDateInsight() throws ParseException, JSONException, IOException {
        String accountUri = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_ACCOUNT));
        Metric nonRelatedMetric = getMdService().createObj(getProject(),
                new Metric("Unrelated-Date-Metric", "SELECT COUNT([" + accountUri + "])", "#,##0"));

        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(UNRELATED_DATE_INSIGHT, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(nonRelatedMetric))));
    }

    private void createInsightUsingDateFilter(String insight) throws ParseException {
        AnalysisPage page = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter();

        page.getFilterBuckets().changeDateDimension(DATE_DATASET_ACTIVITY, DATE_DATASET_CREATED);
        page.waitForReportComputing().getFilterBuckets()
                .getRoot().click(); // close panel
        page.getFilterBuckets().configDateFilter("01/01/2012", "12/31/2012");

        assertEquals(page.waitForReportComputing().getChartReport().getDataLabels(), singletonList("23,673"),
                "The chart does not render correctly");

        page.saveInsight(insight);
    }
}
