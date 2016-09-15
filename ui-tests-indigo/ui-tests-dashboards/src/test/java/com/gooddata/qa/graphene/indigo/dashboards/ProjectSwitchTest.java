package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class ProjectSwitchTest extends GoodSalesAbstractDashboardTest {
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 10);
    private static final String NEW_PROJECT_NAME = "New-project-switch-" + UNIQUE_ID;

    private String currentProjectId;
    private String newProjectId;

    @Override
    protected void prepareSetupProject() throws Throwable {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        // note that during project creation, dwh driver name is appended to project title
        projectTitle = "Project-switch-" + UNIQUE_ID;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void getMoreProject() {
        currentProjectId = testParams.getProjectId();

        newProjectId = ProjectRestUtils.createProject(getGoodDataClient(), NEW_PROJECT_NAME,
                projectTemplate, testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                testParams.getProjectEnvironment());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"switchProject", "desktop", "mobile"})
    public void switchProjectWithFeatureFlagDisabled() {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), newProjectId,
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, false);

        initIndigoDashboardsPageWithWidgets().switchProject(NEW_PROJECT_NAME);
        waitForDashboardPageLoaded(browser);

        takeScreenshot(browser, "User-is-directed-to-dashboard-when-feature-flag-disabled", getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"switchProject", "desktop", "mobile"})
    public void switchProjectsTest() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), newProjectId,
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);

        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "switchProjectsTest-initial", getClass());
        assertEquals(indigoDashboardsPage.getCurrentProjectName(), projectTitle);

        indigoDashboardsPage.switchProject(NEW_PROJECT_NAME).getSplashScreen();
        takeScreenshot(browser, "switchProjectsTest-switched", getClass());
        assertEquals(indigoDashboardsPage.getCurrentProjectName(), NEW_PROJECT_NAME);

        indigoDashboardsPage.switchProject(projectTitle).waitForDashboardLoad();
        takeScreenshot(browser, "switchProjectsTest-switched-back", getClass());
        assertEquals(indigoDashboardsPage.getCurrentProjectName(), projectTitle);
    }

    @Test(dependsOnGroups = {"switchProject"}, groups = {"desktop", "mobile"})
    public void checkLastVisitedProject() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), newProjectId,
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);

        initIndigoDashboardsPageWithWidgets()
                .switchProject(NEW_PROJECT_NAME)
                .getSplashScreen();

        logout();
        signIn(false, UserRoles.ADMIN);

        takeScreenshot(browser, "Last-visited-project-is-updated-with-project-" + NEW_PROJECT_NAME, getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));

        testParams.setProjectId(newProjectId);
        try {
            initProjectsAndUsersPage()
                .deteleProject();
            waitForProjectsPageLoaded(browser);

        } finally {
            testParams.setProjectId(currentProjectId);
        }

        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser,
                "User-is-directed-to-Kpi-Dashboard-correctly-after-deleting-another-project", getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void switchProjectWithEmbededDashboardUser() throws JSONException, ParseException, IOException {
        String embeddedDashboardUser = createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
        GoodData goodDataClient = getGoodDataClient(embeddedDashboardUser, testParams.getPassword());
        String newProjectId = "";

        try {
            newProjectId = ProjectRestUtils.createBlankProject(goodDataClient, NEW_PROJECT_NAME,
                    testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                    testParams.getProjectEnvironment());

            ProjectRestUtils.setFeatureFlagInProject(goodDataClient, newProjectId,
                    ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);

            logout();
            signInAtUI(embeddedDashboardUser, testParams.getPassword());
            testParams.setProjectId(newProjectId);

            initIndigoDashboardsPage().switchProject(projectTitle);
            waitForProjectsPageLoaded(browser);

            takeScreenshot(browser, "Embeded-dashboard-user-cannot-access-Kpi-Dashboard", getClass());
            assertThat(browser.getCurrentUrl(), containsString("cannotAccessWorkbench"));

        } finally {
            testParams.setProjectId(currentProjectId);
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);

            if(!newProjectId.isEmpty()) {
                ProjectRestUtils.deleteProject(goodDataClient, newProjectId);
            }
        }
    }
}
