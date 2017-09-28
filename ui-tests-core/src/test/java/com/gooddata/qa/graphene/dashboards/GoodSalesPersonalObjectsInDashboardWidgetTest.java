package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardAddWidgetPanel;

public class GoodSalesPersonalObjectsInDashboardWidgetTest extends GoodSalesAbstractTest {

    private String personalMetric;
    private String personalReport;

    @BeforeClass(alwaysRun = true)
    public void before() {
        projectTitle = "GoodSales-Personal-Objects-In-Dashboard-Widget";
        personalReport = "[Personal] Report";
        personalMetric = "[Personal] Share %";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"pre-condition"})
    public void createPersonalMetric() {
        personalMetric = "[Personal] Share %";
        waitForFragmentVisible(initMetricPage()).createShareMetric(personalMetric, METRIC_AMOUNT, ATTR_YEAR_SNAPSHOT);
    }

    @Test(dependsOnMethods = {"createPersonalMetric"}, groups = {"pre-condition"})
    public void createPersonalReport() {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName(personalReport)
            .withWhats(personalMetric);
        createReport(reportDefinition, personalReport);
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"pre-condition"})
    public void testPersonalObjects() throws JSONException {
        String dashboardName = "Personal dashboard";
        logout();
        signIn(false, UserRoles.EDITOR);

        try {
            initDashboardsPage();
            dashboardsPage.addNewDashboard(dashboardName);
            dashboardsPage.editDashboard();

            checkCannotFindPersonalReport();
            Stream.of(WidgetTypes.KEY_METRIC, WidgetTypes.KEY_METRIC_WITH_TREND, WidgetTypes.GEO_CHART)
                .forEach(this::checkCannotFindPersonalMetric);

            dashboardsPage.getDashboardEditBar().cancelDashboard();

        } finally {
            dashboardsPage.deleteDashboard();

            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private void checkCannotFindPersonalReport() {
        waitForElementVisible(className("s-btn-report"), browser).click();
        waitForElementVisible(cssSelector(".searchfield input"), browser).sendKeys(personalReport);
        assertTrue(isElementPresent(className("gd-list-view-noResults"), browser));
    }

    private void checkCannotFindPersonalMetric(WidgetTypes type) {
        waitForElementVisible(className("s-btn-widget"), browser).click();

        Graphene.createPageFragment(DashboardAddWidgetPanel.class,
                waitForElementVisible(DashboardAddWidgetPanel.LOCATOR, browser))
                .initWidget(type);

        waitForElementVisible(className("s-btn-select_metric___"), browser).click();
        waitForElementVisible(cssSelector(".gdc-picker:not(.gdc-hidden) .s-search-field input"), browser)
            .sendKeys(personalMetric);
        assertEquals(waitForElementVisible(className("emptyMessage"), browser).getText(),
                "No metrics match your search criteria");
    }
}
