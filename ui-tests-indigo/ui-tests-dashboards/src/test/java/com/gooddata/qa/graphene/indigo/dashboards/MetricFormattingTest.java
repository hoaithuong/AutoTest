package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.addWidgetToAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascade;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboards;
import static java.util.Collections.singletonList;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MetricFormattingTest extends AbstractDashboardTest {

    private static final String PERCENT_OF_GOAL = "% of Goal";

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @DataProvider(name = "formattingProvider")
    public Object[][] formattingProvider() {
        return new Object[][] {
            {Formatter.BARS, null, true},
            {Formatter.GDC, "GDC154,271.00", false},
            {Formatter.DEFAULT, "154,271.00", false},
            {Formatter.TRUNCATE_NUMBERS, "$154.3 K", false},
            {Formatter.COLORS, "$154,271", false},
            {Formatter.UTF_8, Formatter.UTF_8.toString(), false}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "formattingProvider", groups = {"desktop"})
    public void testCustomMetricFormatting(Formatter format, String expectedValue, boolean compareFormat)
            throws ParseException, JSONException, IOException {
        String customFormatMetricName = "Custom format metric";
        String customFormatMetricMaql = "SELECT 154271";
        Metric customFormatMetric = getMdService().createObj(getProject(),
                new Metric(customFormatMetricName, customFormatMetricMaql, format.toString()));

        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addKpi(new KpiConfiguration.Builder()
                        .metric(customFormatMetricName)
                        .dataSet(DATE_DATASET_CREATED)
                        .build())
                .saveEditModeWithWidgets();

        try {
            Kpi lastKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);

            String screenshot = "testCustomMetricFormatting-" + format.name();
            takeScreenshot(browser, screenshot, getClass());

            String kpiValue = lastKpi.getValue();
            if (compareFormat) {
                assertTrue(format.toString().contains(kpiValue));
            } else {
                assertEquals(kpiValue, expectedValue);
            }

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), customFormatMetric.getUri());
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkXssInMetricName(ITestContext context) throws JSONException, IOException {
        String xssHeadline = "<script>alert('Hi')</script>";
        String xssMetricName = "<button>" + PERCENT_OF_GOAL + "</button>";
        String xssMetricMaql = "SELECT 1";
        Metric xssMetric = getMdService().createObj(getProject(), new Metric(xssMetricName, xssMetricMaql, "#,##0.00"));
        final String projectId = testParams.getProjectId();
        final String dashboardUri = getAnalyticalDashboards(getRestApiClient(), projectId).get(0);

        final String kpiUri = createKpiUsingRest(
                new KpiMDConfiguration.Builder()
                    .title(xssMetricName)
                    .metric(xssMetric.getUri())
                    .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                    .comparisonType(ComparisonType.NO_COMPARISON)
                    .comparisonDirection(ComparisonDirection.NONE)
                    .build()
        );
        addWidgetToAnalyticalDashboard(getRestApiClient(), projectId, dashboardUri, kpiUri);

        try {
            Kpi lastKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);

            takeScreenshot(browser, "xss-in-metric-name", getClass());
            assertEquals(lastKpi.getHeadline(), xssMetricName);

            if (Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"))) return;

            Kpi selectedKpi = indigoDashboardsPage
                .switchToEditMode()
                .selectLastWidget(Kpi.class);

            selectedKpi.setHeadline(xssHeadline);

            takeScreenshot(browser, "change-headline-with-xss-name", getClass());
            assertEquals(selectedKpi.getHeadline(), xssHeadline);

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), projectId, xssMetric.getUri());
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkXssInMetricFormat() throws ParseException, JSONException, IOException {
        String xssFormatMetricName = "<button>" + PERCENT_OF_GOAL + "</button>";
        String xssFormatMetricMaql = "SELECT 1";
        Metric xssFormatMetric = getMdService().createObj(getProject(),
                new Metric(xssFormatMetricName, xssFormatMetricMaql, "<button>#,##0.00</button>"));
        final String projectId = testParams.getProjectId();
        final String dashboardUri = getAnalyticalDashboards(getRestApiClient(), projectId).get(0);

        final String kpiUri = createKpiUsingRest(
                new KpiMDConfiguration.Builder()
                    .title(xssFormatMetricName)
                    .metric(xssFormatMetric.getUri())
                    .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                    .comparisonType(ComparisonType.NO_COMPARISON)
                    .comparisonDirection(ComparisonDirection.NONE)
                    .build()
        );
        addWidgetToAnalyticalDashboard(getRestApiClient(), projectId, dashboardUri, kpiUri);

        try {
            Kpi lastKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);

            takeScreenshot(browser, "xss-in-metric-format", getClass());
            assertEquals(lastKpi.getValue(), "<button>1.00</button>");

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), projectId, xssFormatMetric.getUri());
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkKpiStateWithNoDataMetric() throws JSONException, IOException {
        String invalidMetricName = "No data metric";
        String invalidMetricMaql = "SELECT 1 where 2 = 3";
        Metric invalidMetric = getMdService().createObj(getProject(),
                new Metric(invalidMetricName, invalidMetricMaql, "#,##0.00"));
        final String projectId = testParams.getProjectId();
        final String dashboardUri = getAnalyticalDashboards(getRestApiClient(), projectId).get(0);

        final String kpiUri = createKpiUsingRest(
                new KpiMDConfiguration.Builder()
                    .title(invalidMetricName)
                    .metric(invalidMetric.getUri())
                    .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                    .comparisonType(ComparisonType.NO_COMPARISON)
                    .comparisonDirection(ComparisonDirection.NONE)
                    .build()
        );
        addWidgetToAnalyticalDashboard(getRestApiClient(), projectId, dashboardUri, kpiUri);

        try {
            Kpi lastKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);

            takeScreenshot(browser, "checkKpiStateWithNoDataMetric", getClass());
            assertTrue(lastKpi.isEmptyValue());

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), projectId, invalidMetric.getUri());
        }
    }
}
