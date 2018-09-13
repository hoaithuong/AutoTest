package com.gooddata.qa.graphene.indigo.user;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.user.UserStates;
import com.gooddata.qa.graphene.fragments.indigo.user.UserManagementPage;

public class RestartableTransactionTest extends AbstractUITest {

    @Test(groups = {"precondition"})
    public void userLogin() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"userLogin"}, groups = {"precondition"})
    public void turnOnUserManagementFlag() {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)),
                testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, true);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void testRestartableTransaction() {
        initDashboardsPage();
        UserManagementPage userManagementPage = initUserManagementPage();

        for (int i = 0; i < 30; i++) {
            // Need sleep here to increase way the error can happen

            userManagementPage.filterUserState(UserStates.ACTIVE);
            sleepTightInSeconds(2);
            userManagementPage.selectAllUserEmails();
            sleepTightInSeconds(2);
            userManagementPage.deactivateUsers();
            checkError();

            sleepTightInSeconds(2);
            userManagementPage.filterUserState(UserStates.DEACTIVATED);
            sleepTightInSeconds(2);
            userManagementPage.selectAllUserEmails();
            sleepTightInSeconds(2);
            userManagementPage.activateUsers();
            checkError();
        }
    }

    private void checkError() {
        final WebElement e = waitForElementVisible(By.className("gd-message"), browser);
        assertThat(e.getAttribute("class"), not(containsString("error")));
        // dismiss the message to avoid catching it as old one next time
        try {
            e.findElement(By.cssSelector(".gd-message-dismiss-container")).click();
        } catch (Exception ex) {
            // ignore any exception here, most likely the element disappeared by itself
            System.out.println("Unable to dismiss message: " + ex.getClass().getSimpleName());
        }
        waitForElementNotPresent(e);
    }
}
