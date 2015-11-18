package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.mail.ImapClient;

public class RegisterAndDeleteUserAccountTest extends AbstractUITest {

    private static final By NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE = By.
            cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .title");
    private static final By CLOSE_DIALOG_BUTTON_LOCATOR = By.cssSelector(".s-btn-close");

    private static final String DEMO_PROJECT = "GoodSales";
    private static final String GOODDATA_PRODUCT_TRIAL_PROJECT = "GoodData Product Trial";

    private static final String INVALID_EMAIL = "johndoe@yahoocom";
    private static final String INVALID_PASSWORD = "aaaaaa";
    private static final String INVALID_PHONE_NUMBER = "12345678901234567890";

    private static final String EXISTED_EMAIL_ERROR_MESSAGE = "This email address is already in use.";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "This is not a valid email address.";
    private static final String FIELD_MISSING_ERROR_MESSAGE = "Field is required.";

    private static final String INVALID_PASSWORD_ERROR_MESSAGE = "Password too short."
            + " Minimum length is 7 characters.";

    private static final String INVALID_PHONE_NUMBER_ERROR_MESSAGE = "This is not a valid phone number.";

    private static final String ACTIVATION_SUCCESS_MESSAGE = "ACCOUNT ACTIVATED"
            + "\nYour account has been successfully activated!";

    private static final String ALREADY_ACTIVATED_MESSAGE = "ALREADY ACTIVATED"
            + "\nThis registration has already been activated. Please log in below.";

    private static final String NOT_FULLY_ACTIVATED_MESSAGE = "Your account has not yet been fully activated. "
            + "Please click the activation link in the confirmation email sent to you.";

    private static final String REGISTRATION_USER = "gd.accregister@gmail.com";
    private static final String REGISTRATION_USER_PASSWORD = "changeit";

    private static final String NEED_ACTIVATE_ACCOUNT_MESSAGE = "Activate your account before inviting users";

    private String activationLink;

    private RegistrationForm registrationForm;

    private ImapClient imapClient;

    @BeforeClass
    public void initData() {
        String registrationString = String.valueOf(System.currentTimeMillis());
        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(REGISTRATION_USER)
                .withPassword(REGISTRATION_USER_PASSWORD)
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Tech");

        imapHost = testParams.loadProperty("imap.host");
        imapUser = REGISTRATION_USER;
        imapPassword = REGISTRATION_USER_PASSWORD;

        imapClient = new ImapClient(imapHost, imapUser, imapPassword);
    }

    @Test(groups = PROJECT_INIT_GROUP, priority = 1)
    public void selectLoginLink() {
        initRegistrationPage();

        registrationPage.selectLoginLink();
        waitForElementVisible(loginFragment.getRoot());

        loginFragment.openRegistrationPage();
        waitForElementVisible(registrationPage.getRoot());
    }

    @Test(groups = PROJECT_INIT_GROUP, priority = 2)
    public void registerUserWithInvalidValue() {
        initRegistrationPage();

        registrationPage.fillInRegistrationForm(new RegistrationForm())
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        registrationPage.fillInRegistrationForm(registrationForm)
                .enterEmail(INVALID_EMAIL)
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), INVALID_EMAIL_ERROR_MESSAGE);

        registrationPage.fillInRegistrationForm(registrationForm)
                .enterPassword(INVALID_PASSWORD)
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), INVALID_PASSWORD_ERROR_MESSAGE);

        registrationPage.fillInRegistrationForm(registrationForm)
                .enterPhoneNumber(INVALID_PHONE_NUMBER)
                .submitForm();
        assertEquals(registrationPage.getErrorMessage(), INVALID_PHONE_NUMBER_ERROR_MESSAGE);
    }

    @Test(groups = PROJECT_INIT_GROUP, priority = 3)
    public void loginAsUnverifiedUserAfterRegistering()
            throws ParseException, JSONException, IOException, MessagingException {
        initRegistrationPage();
        activationLink = registrationPage.registerNewUser(imapClient, registrationForm);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        openProject(GOODDATA_PRODUCT_TRIAL_PROJECT);

        initProjectsAndUsersPage();
        assertFalse(projectAndUsersPage.isEmailingDashboardsTabDisplayed(),
                "Emailing Dashboards tab is still displayed");

        projectAndUsersPage.clickInviteUserButton();
        assertFalse(isElementPresent(INVITE_USER_DIALOG_LOCATOR, browser));
        assertEquals(waitForElementVisible(NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE, browser).getText(),
                NEED_ACTIVATE_ACCOUNT_MESSAGE);
        takeScreenshot(browser, "Need activate account before inviting users", getClass());
        waitForElementVisible(CLOSE_DIALOG_BUTTON_LOCATOR, browser).click();

        UserProfilePage userProfilePage = projectAndUsersPage.openUserProfile(REGISTRATION_USER);
        assertEquals(userProfilePage.getUserRole(), "", "Unverified admin should not show role");
        takeScreenshot(browser, "Unverified user has no role", this.getClass());

        logout();
        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, false);
        assertEquals(getPageErrorMessage(), NOT_FULLY_ACTIVATED_MESSAGE);

        openUrl(activationLink);
        waitForElementVisible(loginFragment.getRoot());
        assertEquals(loginFragment.getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        initProjectsAndUsersPage();
        assertTrue(projectAndUsersPage.isEmailingDashboardsTabDisplayed(),
                "Emailing Dashboards tab is not displayed");

        projectAndUsersPage.clickInviteUserButton();
        InviteUserDialog inviteUserDialog = Graphene.createPageFragment(InviteUserDialog.class,
              waitForElementVisible(INVITE_USER_DIALOG_LOCATOR, browser));
        takeScreenshot(browser, "Active user can invite users", getClass());
        inviteUserDialog.cancelInvite();

        userProfilePage =  projectAndUsersPage.openUserProfile(REGISTRATION_USER);
        assertEquals(userProfilePage.getUserRole(), UserRoles.ADMIN.getName());
    }

    @Test(groups = PROJECT_INIT_GROUP, priority = 4)
    public void registerNewUser() throws MessagingException, IOException, ParseException, JSONException {
        deleteUserIfExist(getRestApiClient(), REGISTRATION_USER);

        initRegistrationPage();
        activationLink = registrationPage.registerNewUser(imapClient, registrationForm);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        openUrl(activationLink);
        waitForElementVisible(loginFragment.getRoot());
        assertEquals(loginFragment.getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);
        takeScreenshot(browser, "register user successfully", this.getClass());

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        openProject(DEMO_PROJECT);
        assertFalse(dashboardsPage.isEditButtonPresent(), "Dashboard can be edited in Goodsales Demo project");
    }

    @Test(dependsOnGroups = PROJECT_INIT_GROUP)
    public void openAtivationLinkAfterRegistration() {
        openUrl(activationLink);
        waitForElementVisible(loginFragment.getRoot());
        assertEquals(loginFragment.getNotificationMessage(), ALREADY_ACTIVATED_MESSAGE);

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
    }

    @Test(dependsOnGroups = PROJECT_INIT_GROUP)
    public void registerUserWithEmailOfExistingAccount() {
        initRegistrationPage();

        registrationPage.registerNewUser(registrationForm);
        assertEquals(registrationPage.getErrorMessage(), EXISTED_EMAIL_ERROR_MESSAGE);
    }

    @Test(dependsOnMethods = "registerUserWithEmailOfExistingAccount")
    public void deleteUserAccount() throws JSONException {
        initAccountPage();

        accountPage.tryDeleteAccountButDiscard();
        logout();
        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        initAccountPage();
        accountPage.deleteAccount();
        waitForElementVisible(loginFragment.getRoot());

        loginFragment.login(REGISTRATION_USER, REGISTRATION_USER_PASSWORD, false);
        loginFragment.checkInvalidLogin();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        deleteUserIfExist(getRestApiClient(), REGISTRATION_USER);
    }

    private void openProject(String projectName) {
        initProjectsPage();

        List<String> projectIds = waitForFragmentVisible(projectsPage).getProjectsIds(projectName);
        projectsPage.goToProject(projectIds.get(0));
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(projectIds.get(0));
    }

    private void deleteUserIfExist(RestApiClient restApiClient, String userEmail)
            throws ParseException, JSONException, IOException {
        JSONObject userProfile = RestUtils.getUserProfileByEmail(restApiClient, userEmail);
        if (Objects.nonNull(userProfile)) {
            String userProfileUri = userProfile.getJSONObject("links").getString("self");
            RestUtils.deleteUser(restApiClient, userProfileUri);
        }
    }

    private String getPageErrorMessage() {
        return waitForElementVisible(By.xpath("//*[@class='message is-error']/p[1]"), browser).getText();
    }

}