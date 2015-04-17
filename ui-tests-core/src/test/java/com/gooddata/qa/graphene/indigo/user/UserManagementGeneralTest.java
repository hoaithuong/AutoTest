package com.gooddata.qa.graphene.indigo.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.user.GroupDialog;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.UserStates;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;

import static java.util.Arrays.asList;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;

public class UserManagementGeneralTest extends AbstractProjectTest {

    private boolean canAccessUserManagementByDefault;
    private String group1;
    private String group2;
    private String group3;
    private String adminUser;
    private String editorUser;
    private String viewerUser;
    private List<String> group1Group2List;
    private List<String> allUserEmails;

    private static final String FEATURE_FLAG = ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT.getFlagName();
    private static final String CHANGE_GROUP_SUCCESSFUL_MESSAGE = "Group membership successfully changed.";
    private static final String DEACTIVATE_SUCCESSFUL_MESSAGE = "The selected users have been deactivated.";
    private static final String ACTIVATE_SUCCESSFUL_MESSAGE = "The selected users have been activated.";
    private static final String CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE =
            "All users except for yours have been deactivated. You cannot deactivate yourself.";
    private static final String CHANGE_ROLE_SUCCESSFUL_MESSAGE =
            "The role of selected users has been changed to %s.";
    private static final String CHANGE_ROLE_FAILED_MESSAGE = "You cannot change your role to %s.";
    private static final String INVITE_USER_SUCCESSFUL_MESSAGE = "Users successfully invited.";
    private static final String INVALID_EMAIL_MESSAGE = "\"%s\" is not a valid email address.";
    private static final String EXSITING_USER_MESSAGE = "User %s is already in this project.";
    private static final String EMPTY_GROUP_STATE_MESSAGE = "This group is empty";
    private static final String NO_ACTIVE_INVITATIONS_MESSAGE = "No active invitations";
    private static final String NO_DEACTIVATED_USER_MESSAGE = "There are no deactivated users";
    private static final String EXISTING_USER_GROUP_MESSAGE = 
            "Choose a different name for your group. %s already exists.";

    private static final String INVITATION_FROM_EMAIL = "invitation@gooddata.com";
    private static final String INVITED_EMAIL = "abc@mail.com";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "User-management-general" + System.currentTimeMillis();
    }

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "initialize" })
    public void initData() throws JSONException, IOException {
        group1 = "Group1";
        group2 = "Group2";
        group3 = "Group3";
        group1Group2List = asList(group1, group2);

        adminUser = testParams.getUser();
        editorUser = testParams.getEditorUser();
        viewerUser = testParams.getViewerUser();
        allUserEmails = asList(adminUser, editorUser, viewerUser);

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        enableUserManagementFeature();
    }

    @Test(dependsOnMethods = { "initData" }, groups = { "initialize" })
    public void addUserGroups() throws JSONException, IOException {
        createUserGroups(group1, group2, group3);

        // Go to Dashboard page of new created project to use User management page of that project
        initProjectsPage();
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.addUsersToGroup(group1, adminUser);
        userManagementPage.addUsersToGroup(group2, editorUser);
        userManagementPage.addUsersToGroup(group3, viewerUser);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "userManagement" })
    public void accessFromProjectsAndUsersPage() {
        initProjectsAndUsersPage();
        projectAndUsersPage.openUserManagemtPage();
        waitForFragmentVisible(userManagementPage);
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI" })
    public void verifyUserManagementUI() throws IOException, JSONException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().cancelInvitation();
        assertEquals(userManagementPage.getUsersCount(), 3);
        userManagementPage.selectUsers(adminUser, editorUser, viewerUser);
        userManagementPage.openGroupDialog(GroupDialog.State.CREATE).closeDialog();
    }

    @Test(dependsOnGroups = { "initialize" }, groups = { "verifyUI" })
    public void verifyUserGroupsList() {
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.getAllUserGroups();
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1, group2, group3));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1, group2, group3));

        userManagementPage.filterUserState(UserStates.INVITED);
        userManagementPage.waitForEmptyGroup();
        assertEquals(userManagementPage.getStateGroupMessage(), NO_ACTIVE_INVITATIONS_MESSAGE);

        initUserManagementPage();
        userManagementPage.openSpecificGroupPage(group1);
        assertEquals(userManagementPage.getAllUserEmails(), asList(adminUser));
        assertEquals(userManagementPage.getAllUserGroups(), asList(group1, group2, group3));
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyGroupWithNoMember() throws JSONException, IOException {
        String emptyGroup = "EmptyGroup";
        String groupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), emptyGroup);
        try {
            initDashboardsPage();
            initUserManagementPage();
            userManagementPage.openSpecificGroupPage(emptyGroup);
            userManagementPage.waitForEmptyGroup();
            assertEquals(userManagementPage.getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
            userManagementPage.startAddingUser();
            waitForFragmentVisible(userManagementPage);

            // Check "Active" and "All active users" links of sidebar are selected
            assertEquals(userManagementPage.getAllSidebarActiveLinks(), asList("Active", "All active users"));
            assertTrue(compareCollections(userManagementPage.getAllUserEmails(), allUserEmails));
        } finally {
            RestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void updateGroupAfterRemoveMember() throws JSONException, IOException {
        String group = "Test Group";
        String groupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        try {
            initDashboardsPage();
            initUserManagementPage();
            userManagementPage.addUsersToGroup(group, adminUser);

            userManagementPage.openSpecificGroupPage(group);
            userManagementPage.removeUsersFromGroup(group, adminUser);

            refreshPage();
            userManagementPage.waitForEmptyGroup();
        } finally {
            RestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnMethods = { "verifyUserGroupsList" }, groups = "userManagement")
    public void verifyUngroupedUsersGroup() {
        initDashboardsPage();
        initUserManagementPage();
        removeUserFromGroups(adminUser, group1, group2, group3);

        try {
            initUngroupedUsersPage();
            assertTrue(userManagementPage.getAllUserEmails().contains(adminUser));
        } finally {
            initUserManagementPage();
            userManagementPage.addUsersToGroup(group1, adminUser);
        }
    }

    @Test(dependsOnGroups = { "verifyUI" }, groups = { "userManagement" })
    public void adminChangeGroupsMemberOf() {
        initDashboardsPage();
        initUserManagementPage();

        for (String group : group1Group2List) {
            userManagementPage.addUsersToGroup(group, adminUser, editorUser, viewerUser);
            assertEquals(userManagementPage.getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        for (String group : group1Group2List) {
            userManagementPage.openSpecificGroupPage(group);
            assertTrue(compareCollections(userManagementPage.getAllUserEmails(), allUserEmails));
        }
    }

    @Test(dependsOnMethods = { "adminChangeGroupsMemberOf" }, groups = { "userManagement" })
    public void adminChangeGroupsShared() {
        initDashboardsPage();
        initUserManagementPage();

        for (String group : group1Group2List) {
            userManagementPage.removeUsersFromGroup(group, adminUser);
            assertEquals(userManagementPage.getMessageText(), CHANGE_GROUP_SUCCESSFUL_MESSAGE);
        }

        initUserManagementPage();
        userManagementPage.addUsersToGroup(group3, adminUser);

        userManagementPage.openSpecificGroupPage(group3);
        assertTrue(userManagementPage.getAllUserEmails().contains(adminUser));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void adminRemoveUserGroup() throws ParseException, JSONException, IOException {
        String groupName = "New Group";
        String userGroupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), groupName);

        initDashboardsPage();
        initUserManagementPage();
        int userGroupsCount = userManagementPage.getUserGroupsCount();

        RestUtils.deleteUserGroup(restApiClient, userGroupUri);
        refreshPage();
        assertEquals(userManagementPage.getUserGroupsCount(), userGroupsCount - 1);
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void changeRoleOfUsers() {
        try {
            initDashboardsPage();
            initUserManagementPage();
            String adminText = UserRoles.ADMIN.getName();

            final String message = String.format(CHANGE_ROLE_SUCCESSFUL_MESSAGE, adminText);
            userManagementPage.changeRoleOfUsers(UserRoles.ADMIN, editorUser, viewerUser);
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {
                    return message.equals(userManagementPage.getMessageText());
                }
            });

            for (String email : asList(editorUser, viewerUser)) {
                assertEquals(userManagementPage.getUserRole(email), adminText);
            }
        } finally {
            userManagementPage.changeRoleOfUsers(UserRoles.EDITOR, editorUser);
            initUserManagementPage();
            userManagementPage.changeRoleOfUsers(UserRoles.VIEWER, viewerUser);
        }
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void checkUserCannotChangeRoleOfHimself() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.changeRoleOfUsers(UserRoles.EDITOR, adminUser);

        final String message = String.format(CHANGE_ROLE_FAILED_MESSAGE, UserRoles.EDITOR.getName());
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return message.equals(userManagementPage.getMessageText());
            }
        });

        refreshPage();
        assertEquals(userManagementPage.getUserRole(adminUser), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = { "inviteUserToProject" }, groups = { "userManagement" }, alwaysRun = true)
    public void checkUserCannotChangeRoleOfPendingUser() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user",
                INVITED_EMAIL);

        userManagementPage.filterUserState(UserStates.INVITED);
        refreshPage();
        userManagementPage.selectUsers(INVITED_EMAIL);
        assertFalse(userManagementPage.isChangeRoleButtonPresent());
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void inviteUserToProject() throws IOException, MessagingException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", imapUser);

        assertEquals(userManagementPage.getMessageText(), INVITE_USER_SUCCESSFUL_MESSAGE);
        userManagementPage.filterUserState(UserStates.INVITED);
        refreshPage();
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));

        activeEmailUser(projectTitle + " Invitation");
        initUserManagementPage();
        userManagementPage.filterUserState(UserStates.ACTIVE);
        assertTrue(userManagementPage.getAllUserEmails().contains(imapUser));
    }

    @Test(dependsOnMethods = { "verifyUserManagementUI" }, groups = { "userManagement" })
    public void inviteUserToProjectWithInvalidEmail() {
        for (String email : asList("abc@gooddata.c", "<button>abc</button>@gooddata.com")) {
            checkInvitedEmail(email, String.format(INVALID_EMAIL_MESSAGE, email));
        }

        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite existing editor user",
                editorUser);
        assertEquals(userManagementPage.getMessageText(), String.format(EXSITING_USER_MESSAGE, editorUser));
    }

    @Test(dependsOnGroups = { "userManagement" }, groups = { "activeUser" }, alwaysRun = true)
    public void checkUserCannotDeactivateHimself() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.deactivateUsers(adminUser);
        assertEquals(userManagementPage.getMessageText(), CAN_NOT_DEACTIVATE_HIMSELF_MESSAGE);
        assertTrue(userManagementPage.getAllUserEmails().contains(adminUser));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        userManagementPage.waitForEmptyGroup();
        assertEquals(userManagementPage.getStateGroupMessage(), NO_DEACTIVATED_USER_MESSAGE);
    }

    @Test(dependsOnMethods = { "checkUserCannotDeactivateHimself" }, groups = { "activeUser" }, alwaysRun = true)
    public void deactivateUsers() {
        List<String> emailsList = asList(editorUser, viewerUser);

        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.deactivateUsers(editorUser, viewerUser);
        assertEquals(userManagementPage.getMessageText(), DEACTIVATE_SUCCESSFUL_MESSAGE);
        assertFalse(userManagementPage.getAllUserEmails().containsAll(emailsList));

        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        assertTrue(compareCollections(userManagementPage.getAllUserEmails(), emailsList));
    }

    @Test(dependsOnMethods = { "deactivateUsers" }, groups = { "activeUser" }, alwaysRun = true)
    public void activateUsers() {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.filterUserState(UserStates.DEACTIVATED);
        refreshPage();
        userManagementPage.activateUsers(editorUser, viewerUser);
        assertEquals(userManagementPage.getMessageText(), ACTIVATE_SUCCESSFUL_MESSAGE);
        userManagementPage.waitForEmptyGroup();

        initUserManagementPage();
        assertTrue(userManagementPage.getAllUserEmails().containsAll(asList(editorUser, viewerUser)));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addNewGroup() {
        String group = "Test add group";
        checkAddingUserGroup(group);
        initUserManagementPage();
        userManagementPage.addUsersToGroup(group, adminUser);

        userManagementPage.openSpecificGroupPage(group);
        assertTrue(userManagementPage.getAllUserEmails().contains(adminUser));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addUnicodeGroupName() {
        checkAddingUserGroup("ພາສາລາວ résumé اللغة");
        checkAddingUserGroup("Tiếng Việt");
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelAddingNewGroup() {
        initDashboardsPage();
        initUserManagementPage();
        String group = "Test cancel group";
        userManagementPage.cancelCreatingNewGroup(group);
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void addExistingGroupName() {
        initDashboardsPage();
        initUserManagementPage();
        GroupDialog groupDialog = userManagementPage.openGroupDialog(GroupDialog.State.CREATE);
        groupDialog.enterGroupName(group1);
        assertFalse(groupDialog.isSubmitButtonVisible());
        assertEquals(groupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, group1));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroup() throws JSONException, IOException {
        String group = "Rename group test";
        String renamedGroup = group + " renamed";
        String groupUri = RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);

        try {
            initDashboardsPage();
            initUserManagementPage();
            userManagementPage.openSpecificGroupPage(group);

            GroupDialog editGroupDialog = userManagementPage.openGroupDialog(GroupDialog.State.EDIT);
            editGroupDialog.verifyStateOfDialog(GroupDialog.State.EDIT);
            assertEquals(editGroupDialog.getGroupNameText(), group);

            userManagementPage.renameUserGroup(renamedGroup);
            assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(renamedGroup));
            assertEquals(userManagementPage.getUserPageTitle(), renamedGroup);
        } finally {
            RestUtils.deleteUserGroup(restApiClient, groupUri);
        }
    }

    @Test(dependsOnGroups = "verifyUI")
    public void renameUserGroupWithExistingName() throws JSONException, IOException {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openSpecificGroupPage(group1);

        GroupDialog editGroupDialog = userManagementPage.openGroupDialog(GroupDialog.State.EDIT);
        editGroupDialog.enterGroupName(group2);
        assertFalse(editGroupDialog.isSubmitButtonVisible());
        assertEquals(editGroupDialog.getErrorMessage(), String.format(EXISTING_USER_GROUP_MESSAGE, group2));
    }

    @Test(dependsOnGroups = "verifyUI")
    public void cancelRenamingUserGroup() {
        String group = "Cancel group name";
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.openSpecificGroupPage(group1);
        userManagementPage.cancelRenamingUserGroup(group);

        userManagementPage.getAllSidebarActiveLinks().contains(group1);
        assertEquals(userManagementPage.getUserPageTitle(), group1 + " " + userManagementPage.getUsersCount());
        assertFalse(userManagementPage.getAllUserGroups().contains(group));
    }

    @Test(dependsOnGroups = { "activeUser" }, alwaysRun = true)
    public void turnOffUserManagementFeature() throws InterruptedException, IOException, JSONException {
        disableUserManagementFeature();
    }

    private void enableUserManagementFeature() throws IOException, JSONException {
        canAccessUserManagementByDefault = RestUtils.isFeatureFlagEnabled(getRestApiClient(), FEATURE_FLAG);
        if (!canAccessUserManagementByDefault) {
            RestUtils.setFeatureFlags(getRestApiClient(),
                    FeatureFlagOption.createFeatureClassOption(FEATURE_FLAG, true));
        }
    }

    private void disableUserManagementFeature() throws IOException, JSONException {
        if (!canAccessUserManagementByDefault) {
            RestUtils.setFeatureFlags(getRestApiClient(),
                    FeatureFlagOption.createFeatureClassOption(FEATURE_FLAG, false));
        }
    }

    private <T> boolean compareCollections(Collection<T> collectionA, Collection<T> collectionB) {
        if (collectionA.size() != collectionB.size())
            return false;

        return collectionA.containsAll(collectionB) && collectionB.containsAll(collectionA);
    }

    private void checkInvitedEmail(String email, String expectedMessage) {
        initDashboardsPage();
        initUserManagementPage();
        userManagementPage.openInviteUserDialog().invitePeople(UserRoles.ADMIN, "Invite new admin user", email);
        assertEquals(userManagementPage.openInviteUserDialog().getErrorMessage(), expectedMessage);
    }

    private void activeEmailUser(String mailTitle) throws IOException, MessagingException {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        openInvitationEmailLink(getEmailContent(imapClient, mailTitle));
        waitForFragmentVisible(dashboardsPage);
        System.out.println("Dashboard page is loaded.");
    }

    private void openInvitationEmailLink(String mailContent) {
        // Get index of invitation id at email content
        int index = mailContent.indexOf("/p/") + 3;
        // Get invitation id (having the length is 32 chars) and open active link
        openUrl("p/" + mailContent.substring(index, index + 32));
    }

    private String getEmailContent(final ImapClient imapClient, final String mailTitle) throws IOException,
            MessagingException {
        final List<Message> messages = new ArrayList<Message>();
        // Add all current messages with the same title before waiting new message from inbox
        messages.addAll(Arrays.asList(imapClient.getMessagesFromInbox(INVITATION_FROM_EMAIL, mailTitle)));
        // Save begin size of message list
        final int currentSize = messages.size();

        Graphene.waitGui().withTimeout(10, TimeUnit.MINUTES)
                          .pollingEvery(10, TimeUnit.SECONDS)
                          .withMessage("Waiting for messages ..." + mailTitle)
                          .until(new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver input) {
                        messages.addAll(Arrays.asList(imapClient.getMessagesFromInbox(INVITATION_FROM_EMAIL,
                                mailTitle)));
                        // New message arrived when new size of message list > begin size
                        return messages.size() > currentSize;
                    }
                });
        System.out.println("The message arrived");
        return messages.get(messages.size() - 1).getContent().toString().trim();
    }

    private void createUserGroups(String... groupNames) throws JSONException, IOException {
        for (String group : groupNames) {
            RestUtils.addUserGroup(restApiClient, testParams.getProjectId(), group);
        }
    }

    private void refreshPage() {
        browser.navigate().refresh();
        waitForFragmentVisible(userManagementPage);
    }

    private void removeUserFromGroups(String user, String... groups) {
        for (String group : groups) {
            userManagementPage.removeUsersFromGroup(group, user);
        }
    }

    private void checkAddingUserGroup(String group) {
        initDashboardsPage();
        initUserManagementPage();

        userManagementPage.openGroupDialog(GroupDialog.State.CREATE)
            .verifyStateOfDialog(GroupDialog.State.CREATE);
        userManagementPage.createNewGroup(group);

        userManagementPage.waitForEmptyGroup();
        assertEquals(userManagementPage.getStateGroupMessage(), EMPTY_GROUP_STATE_MESSAGE);
        assertTrue(userManagementPage.getAllUserGroups().contains(group));
        assertTrue(userManagementPage.getAllSidebarActiveLinks().contains(group));
    }
}
