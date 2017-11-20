package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_SALES_REPS_BY_WON_AND_LOST;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DrillToHiddenDashboardTabTest extends GoodSalesAbstractTest {

    private final String PRIVATE_DASHBOARD = "Private Dashboard";
    private final String PUBLIC_DASHBOARD = "Public Dashboard";

    private final String TAB_ON_PRIVATE_DASHBOARD = "Tab On Private Dashboard";
    private final String TAB_ON_PUBLIC_DASHBOARD = "Tab On Public Dashboard";

    private final String DRILLING_GROUP = "Dashboards";

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }
    
    @Test(dependsOnMethods = {"createProject"})
    public void initDashboardDrillingToHiddenTab() throws IOException, JSONException {
        String reportOnPublicDash = createAmountByProductReport();
        String reportOnPrivateDash = createTopSalesRepsByWonAndLostReport();

        Dashboard privateDash = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(PRIVATE_DASHBOARD);
            dashboard.addTab(initDashboardTab(TAB_ON_PRIVATE_DASHBOARD,
                    singletonList(createReportItem(reportOnPrivateDash))));
        }).build();

        Dashboard publicDash = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(PUBLIC_DASHBOARD);
            dashboard.addTab(initDashboardTab(TAB_ON_PUBLIC_DASHBOARD,
                    singletonList(createReportItem(reportOnPublicDash))));
        }).build();

        for (Dashboard dashboard : asList(privateDash, publicDash)) {
            DashboardsRestUtils.createDashboard(getRestApiClient(), testParams.getProjectId(), dashboard.getMdObject());
        }

        initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).publishDashboard(true);
        dashboardsPage.editDashboard();
        dashboardsPage.getContent().getLatestReport(TableReport.class).addDrilling(
                Pair.of(singletonList(ATTR_PRODUCT), TAB_ON_PRIVATE_DASHBOARD), DRILLING_GROUP);
        dashboardsPage.saveDashboard();

        dashboardsPage.selectDashboard(PRIVATE_DASHBOARD).publishDashboard(false);
    }

    @Test(dependsOnMethods = {"initDashboardDrillingToHiddenTab"})
    public void testDrillReportToHiddenTab() throws JSONException {
        signIn(true, UserRoles.EDITOR);
        try {
            initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).getContent()
                    .getLatestReport(TableReport.class)
                    .drillOnFirstValue(CellType.ATTRIBUTE_VALUE)
                    .waitForLoaded();

            assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class).getReportTiTle(),
                    REPORT_TOP_SALES_REPS_BY_WON_AND_LOST);
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardDrillingToHiddenTab"})
    public void testHiddenDashboardTabOnDrillingDialog() throws JSONException {
        signIn(true, UserRoles.EDITOR);
        try {
            initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).editDashboard();

            DrillingConfigPanel drillingConfigPanel =
                    WidgetConfigPanel
                            .openConfigurationPanelFor(
                                    dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot(),
                                    browser)
                            .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class);

            assertFalse(drillingConfigPanel.isValueOnRightButton(PRIVATE_DASHBOARD, DRILLING_GROUP),
                    PRIVATE_DASHBOARD + " is on setting panel");
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }
}
