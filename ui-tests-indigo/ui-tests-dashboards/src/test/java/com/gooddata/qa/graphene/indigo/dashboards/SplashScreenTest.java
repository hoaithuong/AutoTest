package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringMissingInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.io.IOException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SplashScreen;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class SplashScreenTest extends AbstractDashboardTest {

    private static final String SPLASH_SCREEN_MOBILE_MESSAGE = "To set up a KPI dashboard, head to your desktop and make your browser window wider.";

    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(METRIC_AMOUNT)
        .dataSet(DATE_DATASET_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .build();

    private String dashboardOnlyUser;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "empty-state"})
    public void checkNewProjectWithoutKpisFallsToSplashScreen() {
        initIndigoDashboardsPage().getSplashScreen();

        takeScreenshot(browser, "checkNewProjectWithoutKpisFallsToSplashScreen", getClass());
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboard() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        takeScreenshot(browser, "checkCreateNewKpiDashboard", getClass());

        new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteAnalyticalDashboard(getWorkingDashboardUri());
    }

    @Test(dependsOnMethods = {"checkDeleteDashboardButtonMissingOnUnsavedDashboard"},
            groups = {"desktop","empty-state"})
    public void checkEnterCreateNewKpiDashboardAndCancel() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .waitForDashboardLoad()
                .addKpi(kpi)
                .cancelEditModeWithChanges();

        takeScreenshot(browser, "checkEnterCreateNewKpiDashboardAndCancel", getClass());

        assertEquals(waitForFragmentVisible(indigoDashboardsPage)
                .getSplashScreen()
                .startEditingWidgets()
                .getKpisCount(), 0);
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboardRemoveAndCreateAgain() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        waitForFragmentVisible(indigoDashboardsPage).switchToEditMode().getLastWidget(Kpi.class).delete();

        indigoDashboardsPage.saveEditModeWithoutWidgets();

        // do not use setupKpi here - it refreshes the page
        // this is a test case without page refresh
        waitForFragmentVisible(indigoDashboardsPage)
                .getSplashScreen()
                .startEditingWidgets()
                .addKpi(kpi)
                .saveEditModeWithWidgets();

        takeScreenshot(browser, "checkCreateNewKpiDashboardRemoveAndCreateAgain", getClass());

        // do not use teardownKpi here - it refreshes the page
        // this is a test case without page refresh
        waitForFragmentVisible(indigoDashboardsPage).switchToEditMode().getLastWidget(Kpi.class).delete();

        indigoDashboardsPage
                .saveEditModeWithoutWidgets()
                .getSplashScreen();
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkDeleteDashboardWithCancelAndConfirm() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .deleteDashboard(false);

        waitForFragmentVisible(indigoDashboardsPage)
                .waitForEditingControls()
                .waitForSplashscreenMissing();

        takeScreenshot(browser, "checkDeleteDashboardWithCancelAndConfirm-cancel", getClass());

        waitForFragmentVisible(indigoDashboardsPage)
                .deleteDashboard(true);

        indigoDashboardsPage.getSplashScreen();

        takeScreenshot(browser, "checkDeleteDashboardWithCancelAndConfirm-confirm", getClass());
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkDefaultDateFilterWhenCreatingDashboard() {
        String dateFilterDefault = DATE_FILTER_THIS_MONTH;
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        String dateFilterSelection = initIndigoDashboardsPageWithWidgets()
                .waitForDateFilter()
                .getSelection();

        takeScreenshot(browser, "checkDefaultDateFilterWhenCreatingDashboard-" + dateFilterDefault, getClass());
        assertEquals(dateFilterSelection, dateFilterDefault);

        DateFilter dateFilterAfterRefresh = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        takeScreenshot(browser, "Default date interval when refresh Indigo dashboard page-" + dateFilterDefault, getClass());
        assertEquals(dateFilterAfterRefresh.getSelection(), dateFilterDefault);

        waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .deleteDashboard(true);

        indigoDashboardsPage.getSplashScreen();
    }

    @Test(dependsOnMethods = {"checkNewProjectWithoutKpisFallsToSplashScreen"}, groups = {"desktop", "empty-state"})
    public void checkDeleteDashboardButtonMissingOnUnsavedDashboard() {
        assertFalse(initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .waitForEditingControls()
                .isDeleteButtonVisible());

        takeScreenshot(browser, "checkDeleteDashboardButtonMissingOnUnsavedDashboard", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkCreateNewKpiDashboardNotAvailableOnMobile() {
        SplashScreen splashScreen = initIndigoDashboardsPage().getSplashScreen();
        String mobileMessage = splashScreen.getMobileMessage();

        assertEquals(mobileMessage, SPLASH_SCREEN_MOBILE_MESSAGE);
        splashScreen.waitForCreateKpiDashboardButtonMissing();

        takeScreenshot(browser, "checkCreateNewKpiDashboardNotAvailableOnMobile", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile", "empty-state"})
    public void checkViewerCannotCreateDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            // viewer accessing "dashboards" with no kpi dashboards created should be redirected
            openUrl(getIndigoDashboardsPageUri());

            // check that we are not on dashboards page
            // instead of that, viewer user should have been switch to OLD dashboard page
            waitForElementVisible(By.className("s-displayed"), browser, 300);
            waitForStringMissingInUrl("/dashboards");

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkDashboardOnlyUserCannotAccessDashboard() throws JSONException {
        logout();
        signInAtUI(dashboardOnlyUser, testParams.getPassword());

        try {
            openUrl(getIndigoDashboardsPageUri());
            //after signInAtUI method, the projects.html is displaying, and openUrl(getIndigoDashboardsPageUri()) 
            //is expected to redirected to projects.html as well. So we need to sleep in seconds to be sure the 
            //correct projects page displayed.
            sleepTightInSeconds(3);

            // With Dashboard Only role, user cannot access to Indigo dashboard
            // page of project and automatically directed to Projects.html page
            waitForProjectsPageLoaded(browser);

            takeScreenshot(browser, "Dashboard-only-user-cannot-access-Kpi-dashboard", getClass());
            assertThat(ProjectsPage.getInstance(browser).getAlertMessage(), 
                    containsString("You have been invited to a dashboard. Your access privileges only extend to "
                            + "external, embedded dashboards. Please contact your administrator to get the "
                            + "dashboard web address."));
        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "empty-state"})
    public void checkEditorCanCreateDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initIndigoDashboardsPage()
                .getSplashScreen()
                .waitForCreateKpiDashboardButtonVisible();

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "empty-state"})
    public void checkCannotSaveNewEmptyDashboard() throws JSONException {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .selectDateFilterByName(DATE_FILTER_THIS_QUARTER);

        takeScreenshot(browser, "checkCannotSaveNewEmptyDashboard", getClass());
        assertFalse(indigoDashboardsPage.isSaveEnabled());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
        dashboardOnlyUser = createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
    }
}
