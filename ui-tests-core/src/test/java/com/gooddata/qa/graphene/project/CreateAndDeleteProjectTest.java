package com.gooddata.qa.graphene.project;

import static com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar.getCurrentProjectName;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class CreateAndDeleteProjectTest extends AbstractProjectTest {

    private static final String FIRST_EDITED_PROJECT_NAME = "Project rename first";
    private static final String SECOND_EDITED_PROJECT_NAME = "Project rename second";

    private String fisrtProjectId;
    private String secondProjectId;

    private String invitedAdminUser;
    private String invitedAdminUserPassword;

    @Test(dependsOnMethods = {"createProject"})
    public void initData() {
        fisrtProjectId = testParams.getProjectId();
        projectTitle = "Project-create-and-delete-test";

        invitedAdminUser = testParams.getEditorUser();
        invitedAdminUserPassword = testParams.getEditorPassword();
    }

    @Test(dependsOnMethods = {"initData"})
    public void createProjectByRestApi() throws ParseException, JSONException, IOException {
        openUrl(PAGE_GDC_PROJECTS);
        assertEquals(waitForFragmentVisible(gpProject).getDwhDriverSelected(), ProjectDriver.POSTGRES.getValue());

        secondProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), projectTitle,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
    }

    @Test(dependsOnMethods = {"createProjectByRestApi"})
    public void renameProjectByOwner() {
        projectsPage.goToProject(fisrtProjectId);
        waitForDashboardPageLoaded(browser);

        initProjectsAndUsersPage();
        projectAndUsersPage.renameProject(FIRST_EDITED_PROJECT_NAME);
        assertEquals(projectAndUsersPage.getProjectName(), FIRST_EDITED_PROJECT_NAME);
        assertEquals(getCurrentProjectName(browser), FIRST_EDITED_PROJECT_NAME);

        initProjectsPage();
        assertEquals(projectsPage.getProjectNameFrom(fisrtProjectId), FIRST_EDITED_PROJECT_NAME);
    }

    @Test(dependsOnMethods = {"renameProjectByOwner"})
    public void renameProjectByInvitedAdminUser() throws ParseException, IOException, JSONException {
        addUserToProject(invitedAdminUser, UserRoles.ADMIN);
        logout();
        loginFragment.login(invitedAdminUser, invitedAdminUserPassword, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        initProjectsPage();
        projectsPage.goToProject(fisrtProjectId);
        waitForDashboardPageLoaded(browser);

        initProjectsAndUsersPage();
        projectAndUsersPage.renameProject(SECOND_EDITED_PROJECT_NAME);
        assertEquals(projectAndUsersPage.getProjectName(), SECOND_EDITED_PROJECT_NAME);
        assertEquals(getCurrentProjectName(browser), SECOND_EDITED_PROJECT_NAME);

        initProjectsPage();
        assertEquals(projectsPage.getProjectNameFrom(fisrtProjectId), SECOND_EDITED_PROJECT_NAME);
    }

    @Test(dependsOnMethods = { "renameProjectByInvitedAdminUser" })
    public void deleteProjectByInvitedAdminUser() {
        try {
            initProjectsAndUsersPage();
            assertTrue(projectAndUsersPage.isDeleteButtonEnabled(), "Delete button is not enabled");

            projectAndUsersPage.tryDeleteProjectButDiscard();
            initProjectsPage();
            assertTrue(projectsPage.isProjectDisplayed(fisrtProjectId),
                    "Project is still deleted after discard Delete project dialog");

            initProjectsAndUsersPage();
            projectAndUsersPage.deteleProject();
            assertFalse(projectsPage.isProjectDisplayed(fisrtProjectId), "Project is still not deleted");

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to delete project", this.getClass());
            throw e;

        } finally {
            testParams.setProjectId(secondProjectId);
        }
    }

}
