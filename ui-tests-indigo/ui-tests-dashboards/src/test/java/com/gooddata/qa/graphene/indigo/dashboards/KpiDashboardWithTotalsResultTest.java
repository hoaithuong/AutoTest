package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.fixture.ResourceManagement;
import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.entity.visualization.TotalsBucket;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class KpiDashboardWithTotalsResultTest extends AbstractDashboardTest {

    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Insight has attribute and measure";
    private static final String INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE = "Insight has date attribute and measure";
    private static final String INSIGHT_IS_UPLOADED_DATA = "Insight is uploaded data";
    private static final String INSIGHT_IS_ON_MOBILE = "Insight is on mobile";
    private static final String KPI_DASHBOARD = "KPI dashboard";
    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";

    private String insight;
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Totals-Result-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = "createProject", groups = {"desktop", "mobile"})
    public void prepareInsights() throws IOException {
        insight = createSimpleInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE,
            METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT, AggregationItem.MAX);
        createSimpleInsight(INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE,
            METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY, AggregationItem.MAX);

        MeasureBucket measureBucket = MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES));
        CategoryBucket categoryBucket = CategoryBucket.createCategoryBucket(getAttribute(ATTR_DEPARTMENT), Type.ATTRIBUTE);
        TotalsBucket totalsBucket = TotalsBucket.createTotals(measureBucket, categoryBucket, AggregationItem.MAX);
        String insightWidget = createInsightWidget(new InsightMDConfiguration(INSIGHT_IS_ON_MOBILE, ReportType.TABLE)
            .setMeasureBucket(singletonList(measureBucket))
            .setCategoryBucket(singletonList(categoryBucket))
            .setTotalsBucket(singletonList(totalsBucket)));
        indigoRestRequest.addTotalResults(INSIGHT_IS_ON_MOBILE, singletonList(totalsBucket));

        indigoRestRequest.createAnalyticalDashboard(singletonList(insightWidget), "KPI Dashboard Mobile");
    }

    @Test(dependsOnMethods = "prepareInsights", groups = "desktop")
    public void placeTableHasTotalsOnKD() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage()
            .addDashboard()
            .addInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
            .changeDashboardTitle(KPI_DASHBOARD);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.addInsight(INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE).getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading().addAttributeFilter(ATTR_DEPARTMENT).getAttributeFiltersPanel()
            .getLastFilter().clearAllCheckedValues().selectByNames("Direct Sales");

        CheckUtils.checkRedBar(browser);

        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        Screenshots.takeScreenshot(browser, "place tables have totals on KD", getClass());

        assertTrue(pivotTableReport.containsGrandTotals(), "Grand Totals should be rendered as well");
        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);

        pivotTableReport = indigoDashboardsPage.getLastWidget(Insight.class).getPivotTableReport();
        assertTrue(pivotTableReport.containsGrandTotals(), "Grand Totals should be rendered as well");
        expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "47,459"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = {"placeTableHasTotalsOnKD"}, groups = "desktop")
    public void exportAndImportProject() throws Throwable {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(true, true, true, statusPollingCheckIterations);
        String workingProjectId = testParams.getProjectId();
        String targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE");

        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard(KPI_DASHBOARD).waitForWidgetsLoading();
            Screenshots.takeScreenshot(browser, "export import project has totals result in " + KPI_DASHBOARD, getClass());

            List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054"));
            assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
                .getGrandTotalsContent(), expectedValues);
            expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "47,459"));
            assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getPivotTableReport()
                .getGrandTotalsContent(), expectedValues);
        } finally {
            testParams.setProjectId(workingProjectId);
        }
    }

    @Test(dependsOnMethods = {"prepareInsights"}, groups = "desktop")
    public void partialExportAndImportInsight() throws Throwable {
        String exportToken = exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);
        String workingProjectId = testParams.getProjectId();
        String targetProjectId = createProjectUsingFixture("Copy of " + projectTitle, ResourceManagement.ResourceTemplate.GOODSALES);

        testParams.setProjectId(targetProjectId);
        try {
            ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(ADMIN)), testParams.getProjectId());
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_PIVOT_TABLE, true);

            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
            Screenshots.takeScreenshot(browser, "partial export import project has totals result in " +
                INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, getClass());
            List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054"));
            assertEquals(analysisPage.getPivotTableReport().getGrandTotalsContent(), expectedValues);
        } finally {
            testParams.setProjectId(workingProjectId);
        }
    }

    @Test(dependsOnGroups = "createProject", groups = "desktop")
    public void uploadDataTest() throws IOException {
        CsvFile payroll = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"));

        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        getMdService().createObj(getProject(),
            new Metric(METRIC_AMOUNT, format("SELECT SUM([%s])", getFactByIdentifier("fact.csv_payroll.amount").getUri()),
            DEFAULT_CURRENCY_METRIC_FORMAT));
        createSimpleInsight(INSIGHT_IS_UPLOADED_DATA, METRIC_AMOUNT, "Education", AggregationItem.MIN);

        //Edit Payroll data and upload
        payroll
            .rows("Wood", "Alfredo", "Partial College", "Store", "Ranch Foodz", "Texas", "Austin", "2007-12-01", "10000")
            .saveToDisc(testParams.getCsvFolder());

        initDataUploadPage()
            .getMyDatasetsTable()
            .getDataset(payroll.getDatasetNameOfFirstUpload())
            .clickUpdateButton()
            .pickCsvFile(payroll.getFilePath())
            .clickUploadButton();
        DataPreviewPage.getInstance(browser).triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        //Check updated
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_IS_UPLOADED_DATA).getPivotTableReport();
        takeScreenshot(browser, "uploaded payroll dataset", getClass());
        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MIN.getRowName(), "$10,000.00"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    @Test(dependsOnMethods = "prepareInsights", groups = "mobile")
    public void checkKpiDashboardHasTotalsResultOnMobile() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().selectKpiDashboard("KPI Dashboard Mobile");

        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForAlertsLoaded().getLastWidget(Insight.class).getPivotTableReport();
        Screenshots.takeScreenshot(browser, "check Kpi dashboard has totals results on mobile", getClass());
        assertTrue(pivotTableReport.containsGrandTotals(), "Grand Totals should be displayed");

        List<List<String>> expectedValues = singletonList(asList(AggregationItem.MAX.getRowName(), "101,054"));
        assertEquals(pivotTableReport.getGrandTotalsContent(), expectedValues);
    }

    private String createSimpleInsight(String title, String metric, String attribute, AggregationItem type) throws IOException {
        MeasureBucket measureBucket = MeasureBucket.createSimpleMeasureBucket(getMetric(metric));
        CategoryBucket categoryBucket = CategoryBucket.createCategoryBucket(getAttribute(attribute), Type.ATTRIBUTE);
        TotalsBucket totalsBucket = TotalsBucket.createTotals(measureBucket, categoryBucket, type);
        indigoRestRequest.createInsight(new InsightMDConfiguration(title, ReportType.TABLE)
                .setMeasureBucket(singletonList(measureBucket))
                .setCategoryBucket(singletonList(categoryBucket))
                .setTotalsBucket(singletonList(totalsBucket)));
        return indigoRestRequest.addTotalResults(title, singletonList(totalsBucket));
    }

    private Metric getMetric(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }

    private Attribute getAttribute(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title));
    }
}
