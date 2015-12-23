package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class ManipulateWidgetsTest extends DashboardWithWidgetsTest {

    private static final String TEST_HEADLINE = "Test headline";
    private static final String KPI_HINT_FOR_EDIT_NAME_COLOR = "rgba(255, 255, 204, 1)";
    private static final String LONG_NAME_METRIC = "# test metric with longer name is shortened";
    private static final String PATTERN_OF_METRIC_NAME = "is shortened";

    @BeforeClass(alwaysRun = true)
    public void before() {
        super.before();
        validateAfterClass = true;
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkEditModeCancelNoChanges() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .selectDateFilterByName(DATE_FILTER_ALL_TIME)
            .switchToEditMode()
            .selectKpi(0);

        String kpiHeadline = selectedKpi.getHeadline();
        String kpiValue = selectedKpi.getValue();

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
        assertEquals(selectedKpi.getValue(), kpiValue);

        takeScreenshot(browser, "checkEditModeCancelNoChanges", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndDiscard() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        String kpiHeadline = selectedKpi.getHeadline();
        String modifiedHeadline = generateUniqueHeadlineTitle();

        selectedKpi.setHeadline(modifiedHeadline);

        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndAbortCancel() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        String kpiHeadline = selectedKpi.getHeadline();
        String modifiedHeadline = generateUniqueHeadlineTitle();

        selectedKpi.setHeadline(modifiedHeadline);

        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .cancelClick();

        assertEquals(selectedKpi.getHeadline(), modifiedHeadline);
        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndSave() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        String uniqueHeadline = generateUniqueHeadlineTitle();
        selectedKpi.setHeadline(uniqueHeadline);

        indigoDashboardsPage.saveEditModeWithKpis();

        assertEquals(selectedKpi.getHeadline(), uniqueHeadline);

        takeScreenshot(browser, "checkKpiTitleChangeAndSave-" + uniqueHeadline, getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        selectedKpi.setHeadline("");

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(AMOUNT)
            .selectDateDimensionByName(DATE_CREATED);

        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, AMOUNT);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(LOST)
            .selectDateDimensionByName(DATE_CREATED);
        assertNotEquals(selectedKpi.getHeadline(), metricHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(AMOUNT)
            .selectDateDimensionByName(DATE_CREATED);

        selectedKpi.setHeadline(TEST_HEADLINE);
        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, TEST_HEADLINE);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(AMOUNT)
            .selectDateDimensionByName(DATE_CREATED);

        assertEquals(selectedKpi.getHeadline(), TEST_HEADLINE);

        takeScreenshot(browser, "checkKpiTitlePersistenceWhenMetricChange-" + TEST_HEADLINE, getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDeleteKpiConfirmAndSave() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        int kpisCountAfterAdd = kpisCount + 1;

        indigoDashboardsPage
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .build())
            .saveEditModeWithKpis();

        assertEquals(kpisCountAfterAdd, indigoDashboardsPage.getKpisCount());
        assertEquals(kpisCountAfterAdd, initIndigoDashboardsPageWithWidgets().getKpisCount());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage.saveEditModeWithKpis();

        assertEquals(kpisCount, initIndigoDashboardsPageWithWidgets().getKpisCount());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDeleteKpiConfirmAndDiscard() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        indigoDashboardsPage
            .switchToEditMode()
            .deleteKpi(0)
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .cancelEditMode()
            .waitForDialog()
            .submitClick();

        assertEquals(kpisCount, indigoDashboardsPage.getKpisCount());
        assertEquals(kpisCount, initIndigoDashboardsPageWithWidgets().getKpisCount());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void testCancelAddingWidget() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        indigoDashboardsPage
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .build())
            .cancelEditMode()
            .waitForDialog()
            .submitClick();

        assertEquals(indigoDashboardsPage.getKpisCount(), kpisCount);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiShowHintForEditableName() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .selectKpi(0);

        takeScreenshot(browser, "Kpi does not show hint before hover to headline", this.getClass());
        assertFalse(kpi.hasHintForEditName(), "Kpi shows hint although headline is not hovered");

        String hintColor = kpi.hoverToHeadline();
        takeScreenshot(browser, "Kpi shows hint for editable name when hover to headline", this.getClass());
        assertEquals(hintColor, KPI_HINT_FOR_EDIT_NAME_COLOR);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkMetricWithLongerNameWillBeShortened() {
        createMetric(LONG_NAME_METRIC, "SELECT 1", "#,##0");

        MetricSelect metricSelect = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .clickAddWidget()
                .getConfigurationPanel()
                .getMetricSelect();
        takeScreenshot(browser, "Metric with longer name", getClass());
        assertTrue(metricSelect.hasItem(LONG_NAME_METRIC), "Item: " + LONG_NAME_METRIC + " not exist in list");

        metricSelect.searchByName(PATTERN_OF_METRIC_NAME);
        assertTrue(metricSelect.isNameShortened(LONG_NAME_METRIC), "The metric still displays full name");

        String metricTooltip = metricSelect.getTooltip(LONG_NAME_METRIC);
        takeScreenshot(browser, "Metric tooltip when partial searching", getClass());
        assertEquals(metricTooltip, LONG_NAME_METRIC);

        metricSelect.searchByName(LONG_NAME_METRIC);
        assertTrue(metricSelect.isNameShortened(LONG_NAME_METRIC), "The metric still displays full name");

        metricTooltip = metricSelect.getTooltip(LONG_NAME_METRIC);
        takeScreenshot(browser, "Metric tooltip when searching", getClass());
        assertEquals(metricTooltip, LONG_NAME_METRIC);
    }
}
