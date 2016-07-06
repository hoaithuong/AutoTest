package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class KpiPopTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkKpiPopInMobile() {
        Kpi amount = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(METRIC_AMOUNT);
        assertFalse(amount.hasPopSection());

        Kpi lost = waitForFragmentVisible(indigoDashboardsPage).getKpiByHeadline(METRIC_LOST);
        assertTrue(lost.hasPopSection());

        Kpi numberOfActivities = indigoDashboardsPage.getKpiByHeadline(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(numberOfActivities.hasPopSection());

        assertEquals(numberOfActivities.getPopSection().getChangeTitle(), "change");
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. period");

        // When project is created by REST API (and not using SplashScreen)
        // "All time" is the initial filter --> switch to "This month"
        indigoDashboardsPage
                .waitForDateFilter()
                .selectByName(DATE_FILTER_THIS_MONTH);

        takeScreenshot(browser, "checkKpiPopInMobile-thisMonth", getClass());

        assertEquals(lost.getPopSection().getChangeTitle(), "change");
        assertEquals(lost.getPopSection().getPeriodTitle(), "prev. year");

        numberOfActivities = indigoDashboardsPage.getKpiByHeadline(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(numberOfActivities.hasPopSection());

        assertEquals(numberOfActivities.getPopSection().getChangeTitle(), "change");
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. month");
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasPopSection() {
        Kpi justAddedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_CREATED)
                .build())
            .selectLastKpi();

        assertTrue(justAddedKpi.hasPopSection());

        Kpi lastKpi = indigoDashboardsPage.saveEditModeWithWidgets().getLastKpi();

        takeScreenshot(browser, "checkNewlyAddedKpiHasPopSection", getClass());
        assertTrue(lastKpi.hasPopSection());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiWithoutComparison() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_CREATED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .build())
            .selectLastKpi();

        assertFalse(kpi.hasPopSection());

        waitForFragmentVisible(indigoDashboardsPage)
            .saveEditModeWithWidgets();

        Kpi lastKpi = indigoDashboardsPage.getLastKpi();

        takeScreenshot(browser, "checkKpiWithoutComparison", getClass());
        assertFalse(lastKpi.hasPopSection());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithWidgets();
    }

    @DataProvider(name = "popProvider")
    public Object[][] popProvider() {
        return new Object[][] {
            // comparison type, date filter, prev. title for the date filter
            {Kpi.ComparisonType.LAST_YEAR, DATE_FILTER_THIS_MONTH, "prev. year"},
            {Kpi.ComparisonType.PREVIOUS_PERIOD, DATE_FILTER_THIS_MONTH, "prev. month"},
            {Kpi.ComparisonType.LAST_YEAR, DATE_FILTER_THIS_QUARTER, "prev. year"},
            {Kpi.ComparisonType.PREVIOUS_PERIOD, DATE_FILTER_THIS_QUARTER, "prev. quarter"},
            {Kpi.ComparisonType.LAST_YEAR, DATE_FILTER_ALL_TIME, "prev. year"},
            {Kpi.ComparisonType.PREVIOUS_PERIOD, DATE_FILTER_ALL_TIME, "prev. period"}
        };
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, dataProvider = "popProvider", groups = {"desktop"})
    public void checkKpiPopSection(Kpi.ComparisonType comparisonType, String dateFilter, String expectedPeriodTitle) {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_CREATED)
                .comparison(comparisonType.toString())
                .build())
            .saveEditModeWithWidgets();

        Kpi kpi = waitForFragmentVisible(indigoDashboardsPage).getLastKpi();

        indigoDashboardsPage.selectDateFilterByName(dateFilter);

        takeScreenshot(browser, "checkKpiPopSection-" + comparisonType + "-" + dateFilter + "-" + expectedPeriodTitle, getClass());
        assertEquals(kpi.getPopSection().getChangeTitle(), "change");
        assertEquals(kpi.getPopSection().getPeriodTitle(), expectedPeriodTitle);

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithWidgets();
    }
}
