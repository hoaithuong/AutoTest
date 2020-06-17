package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Fact;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.md.Restriction;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class NonProductionDatasetTest extends AbstractDashboardTest {

    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";
    private static final String PAYROLL_DATASET = "Payroll";

    private static final String WITHOUT_DATE_CSV_PATH = "/" + UPLOAD_CSV + "/without.date.csv";
    private static final String WITHOUT_DATE_DATASET = "Without Date";

    private static final String DATASET_CONTAINING_11_DATES_CSV_PATH = "/" + UPLOAD_CSV + "/11dates.yyyy.csv";
    private static final String DATASET_CONTAINING_11_DATES = "11dates Yyyy";

    private static final String FACT_AMOUNT = "Amount";

    private static final String METRIC_AMOUNT_SUM = "Amount Sum";
    private static final String METRIC_DEPARTMENT_COUNT = "Department Count";

    private static final String ATTRIBUTE_DEPARTMENT = "Department";
    private static final String ATTRIBUTE_RECORDS_OF_PAYROLL = "Records of Payroll";

    private static final String DATASET_PAYDATE = "Paydate";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        // create empty project
        projectTitle = "Non-Production-Dataset-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void uploadCsvFile() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + PAYROLL_DATASET + "-dataset", getClass());
    }

    @Test(dependsOnMethods = {"uploadCsvFile"}, groups = {"precondition"})
    public void createTestMetrics() {
        final String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));

        getMdService().createObj(getProject(),
                new Metric(METRIC_AMOUNT_SUM,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        Formatter.DEFAULT.toString()));

        final String departmentUri = getMdService()
                .getObjUri(getProject(), Attribute.class, title(ATTRIBUTE_DEPARTMENT));
        final String recordsOfPayrollUri = getMdService()
                .getObjUri(getProject(), Attribute.class, title(ATTRIBUTE_RECORDS_OF_PAYROLL));

        getMdService().createObj(getProject(),
                new Metric(METRIC_DEPARTMENT_COUNT,
                        MetricTypes.COUNT.getMaql()
                                .replaceFirst("__attr__", format("[%s]", departmentUri))
                                .replaceFirst("__attr__", format("[%s]", recordsOfPayrollUri)),
                        Formatter.DEFAULT.toString()));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"basic-test"})
    public void testMeasureOptions() {
        assertEquals(initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .dragAddKpiPlaceholder()
            .getConfigurationPanel()
            .getMetricSelect()
            .getValues(), asList(METRIC_AMOUNT_SUM, METRIC_DEPARTMENT_COUNT), "The measure options are not correct");
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"basic-test"})
    public void testDatasetOptions() {
        assertEquals(initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .dragAddKpiPlaceholder()
            .getConfigurationPanel()
            .selectMetricByName(METRIC_AMOUNT_SUM)
            .getDataSets(), singletonList(DATASET_PAYDATE));
    }

    @Test(dependsOnGroups = {"basic-test"})
    public void saveKpiUsingNonProductionData() throws JSONException, IOException {
        final String expectedKpiValue = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT_SUM)
                .dataSet(DATASET_PAYDATE)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .build())
            .selectDateFilterByName("All time")
            .getWidgetByHeadline(Kpi.class, METRIC_AMOUNT_SUM)
            .getValue();

        IndigoDashboardsPage.getInstance(browser).saveEditModeWithWidgets();
        try {
            takeScreenshot(browser, "Test-Save-Kpi-Using-Non-Production-Data", getClass());
            assertEquals(IndigoDashboardsPage.getInstance(browser)
                    .getWidgetByHeadline(Kpi.class, METRIC_AMOUNT_SUM).getValue(), expectedKpiValue,
                    "The saved kpi value is not correct");
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(
                    indigoRestRequest.getAnalyticalDashboards().get(0));
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void selectSingleDateDimensionAsDefault() throws JSONException, IOException {
        String insight = "Insight-without-date-filter";

        AnalysisPage page = initAnalysePage();
        page.getCatalogPanel().changeDataset(PAYROLL_DATASET);
        page.addMetric("Amount", FieldType.FACT).waitForReportComputing().saveInsight(insight);

        try {
            initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(insight);
            assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), "Paydate",
                    "The only date dimension is not selected");
            assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetSelectCollapsed(),
                    "The date dataset is not collapsed");
        } finally {
            indigoRestRequest.deleteObjectsUsingCascade(
                    indigoRestRequest.getInsightUri(insight));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testScrollBarAppearenceOnDateDataset() throws JSONException, IOException {
        String insight = "Insight-Relating-11-dates";
        uploadCSV(getFilePathFromResource(DATASET_CONTAINING_11_DATES_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + DATASET_CONTAINING_11_DATES + "-dataset", getClass());

        AnalysisPage page = initAnalysePage();
        page.getCatalogPanel().changeDataset(DATASET_CONTAINING_11_DATES);
        page.addMetric("Number", FieldType.FACT).waitForReportComputing().saveInsight(insight);

        try {
            DateDimensionSelect dropDown = initIndigoDashboardsPage().getSplashScreen().startEditingWidgets()
                    .addInsight(insight).getConfigurationPanel().openDateDataSet();

            assertTrue(dropDown.getValues().size() > 10, "The number of dimensions is not correct");
            assertTrue(dropDown.isScrollable(), "The scroll bar is not present");
        } finally {
            indigoRestRequest.deleteObjectsUsingCascade(
                    indigoRestRequest.getInsightUri(insight));
            initDataUploadPage().getMyDatasetsTable().getDataset(DATASET_CONTAINING_11_DATES).clickDeleteButton()
                    .clickDelete();
            Dataset.waitForDatasetLoaded(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-10287: Cannot add KPIs that doesn't relate to date")
    public void addKpiUsingDatasetWithoutDate() {
        uploadCSV(getFilePathFromResource(WITHOUT_DATE_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + WITHOUT_DATE_DATASET + "-dataset", getClass());

        try {
            String metric = "Metric-Using-Dataset-Without-Date";
            getMdService().createObj(getProject(), new Metric(metric,
                    MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]",
                            getMdService().getObjUri(getProject(), Fact.class, Restriction.title("Censusarea")))),
                    "#,##0.00"));

            initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().dragAddKpiPlaceholder()
                    .getConfigurationPanel().selectMetricByName(metric);

            indigoDashboardsPage.waitForWidgetsLoading();
            takeScreenshot(browser, "Add-Kpi-Using-Dataset-Without-Date", getClass());

            assertEquals(indigoDashboardsPage.getLastWidget(Kpi.class).getValue(), "7,070,658.41");
            assertFalse(indigoDashboardsPage.getConfigurationPanel().isDateDataSetDropdownVisible(),
                    "Date Dataset panel is visible.");
        } finally {
            initDataUploadPage().getMyDatasetsTable().getDataset(WITHOUT_DATE_DATASET).clickDeleteButton()
                    .clickDelete();
            Dataset.waitForDatasetLoaded(browser);
        }
    }
}
