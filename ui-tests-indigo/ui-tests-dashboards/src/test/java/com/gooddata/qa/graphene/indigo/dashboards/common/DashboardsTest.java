package com.gooddata.qa.graphene.indigo.dashboards.common;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;

public abstract class DashboardsTest extends DashboardsGeneralTest {

    public static final String AMOUNT = "Amount";
    public static final String ACCOUNT = "Account";
    public static final String STAT_REGION = "stat_region";
    public static final String LOST = "Lost";
    public static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    public static final String PERCENT_OF_GOAL = "% of Goal";

    public static final String DATE_CREATED = "Created";
    public static final String DATE_CLOSED = "Closed";
    public static final String DATE_ACTIVITY = "Activity";
    public static final String DATE_SNAPSHOT = "Snapshot";

    public static final String DATE_FILTER_ALL_TIME = "All time";
    public static final String DATE_FILTER_THIS_MONTH = "This month";
    public static final String DATE_FILTER_LAST_MONTH = "Last month";
    public static final String DATE_FILTER_THIS_QUARTER = "This quarter";
    public static final String DATE_FILTER_LAST_QUARTER = "Last quarter";
    public static final String DATE_FILTER_THIS_YEAR = "This year";
    public static final String DATE_FILTER_LAST_YEAR = "Last year";

    public static final String DRILL_TO_OUTLOOK = "Outlook";
    public static final String DRILL_TO_WHATS_CHANGED = "What's Changed";
    public static final String DRILL_TO_WATERFALL_ANALYSIS = "Waterfall Analysis";

    protected void setupKpiFromSplashScreen(KpiConfiguration config) {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        indigoDashboardsPage
                .waitForDashboardLoad()
                .addWidget(config)
                .saveEditModeWithKpis();
    }

    protected void teardownKpiWithDashboardDelete() {
        if (initIndigoDashboardsPageWithWidgets().isEditButtonVisible()) {
            initIndigoDashboardsPageWithWidgets()
                .switchToEditMode();
        }

        initIndigoDashboardsPageWithWidgets()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
                .saveEditModeWithoutKpis()
                .getSplashScreen();
    }

}
