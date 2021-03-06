package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.fragments.account.InviteUserDialog.INVITE_USER_DIALOG_LOCATOR;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.fragments.account.LostPasswordPage.PASSWORD_HINT;

import java.io.IOException;

import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.InviteUserDialog;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;

public class RegisterAndDeleteUserAccountTest extends AbstractUITest {

    private static final By NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE = By.
            cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .title");
    private static final By CLOSE_DIALOG_BUTTON_LOCATOR = By.cssSelector(".s-btn-close");
    private static final String SHORT_PASSWORD_ERROR_MESSAGE = "The password must have at least 7 characters.";
    private static final String COMMONLY_PASSWORD_ERROR_MESSAGE = "Given password is commonly used.";
    private static final String SEQUENTIAL_PASSWORD_ERROR_MESSAGE = "Sequential and repeated characters are "
            + "not allowed in passwords.";
    private static final String PASSWORD_CONTAINS_LOGIN = "Password contains login which is forbidden.";
    private static final String INVALID_EMAIL = "johndoe@yahoocom";
    private static final String INVALID_PHONE_NUMBER = "12345678901234567890";
    private static final String EXISTED_EMAIL_ERROR_MESSAGE = "This email address is already in use.";
    private static final String INVALID_EMAIL_ERROR_MESSAGE = "This is not a valid email address.";
    private static final String FIELD_MISSING_ERROR_MESSAGE = "Field is required.";
    private static final String INVALID_PHONE_NUMBER_ERROR_MESSAGE = "This is not a valid phone number.";
    private static final String ACTIVATION_SUCCESS_MESSAGE = "Account Activated"
            + "\nYour account has been successfully activated!";
    // due to cl-10948, change message when re-click on activation account link
    private static final String ALREADY_ACTIVATED_MESSAGE = "This activation link is not valid."
            + "\nRegister again or log in to your account.";
    private static final By LOG_IN_YOUR_ACCOUNT_LINK = By.cssSelector(".login-message a");
    private static final String NOT_FULLY_ACTIVATED_MESSAGE = "Your account has not yet been fully activated. "
            + "Please click the activation link in the confirmation email sent to you.";
    private static final String NEED_ACTIVATE_ACCOUNT_MESSAGE = "Activate your account before inviting users";

    private String registrationUser;
    private String activationLink;

    private RegistrationForm registrationForm;

    @BeforeClass(alwaysRun = true)
    public void initData() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");

        registrationUser = generateEmail(imapUser);

        String registrationString = String.valueOf(System.currentTimeMillis());
        registrationForm = new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(registrationUser)
                .withPassword(testParams.getPassword())
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Government");
    }

    @Test(groups = {"sanity"})
    public void verifyEnvironment() {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Register New User is not tested on PI or Production environment");
        }
    }

    @Test
    public void selectLoginLink() {
        initRegistrationPage()
            .selectLoginLink()
            .registerNewAccount();
    }

    @Test(dependsOnMethods = "verifyEnvironment")
    public void registerUserWithInvalidPasswordValidation() throws ParseException, JSONException {
        final SoftAssert softAssert = new SoftAssert();

        RegistrationPage registrationPage = initRegistrationPage();
        softAssert.assertEquals(registrationPage.getPasswordHint(), PASSWORD_HINT);

        String registrationEmail = generateEmail(registrationUser);

        registrationPage
                .fillInRegistrationForm(registrationForm)
                .enterEmail(registrationEmail)
                .enterPassword("aaaaaa")
                .agreeRegistrationLicense()
                .submitForm()
                .waitForRegistrationNotSuccessfully();
        takeScreenshot(browser, "Error-message-for-short-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(),
                SHORT_PASSWORD_ERROR_MESSAGE);

        registrationPage
                .enterPassword("12345678")
                .enterSpecialCaptcha()
                .submitForm()
                .waitForRegistrationNotSuccessfully();
        takeScreenshot(browser, "Error-message-for-commonly-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(),
                COMMONLY_PASSWORD_ERROR_MESSAGE);

        registrationPage
                .enterPassword("aaaaaaaa")
                .enterSpecialCaptcha()
                .submitForm()
                .waitForRegistrationNotSuccessfully();
        takeScreenshot(browser, "Error-message-for-sequential-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(),
                SEQUENTIAL_PASSWORD_ERROR_MESSAGE);

        registrationPage.enterPassword(registrationEmail)
                .enterSpecialCaptcha()
                .submitForm()
                .waitForRegistrationNotSuccessfully();
        takeScreenshot(browser, "Error-message-for-login-password-shows", getClass());
        softAssert.assertEquals(registrationPage.getErrorMessage(), PASSWORD_CONTAINS_LOGIN);

        softAssert.assertAll();
    }

    @Test(dependsOnMethods = "verifyEnvironment")
    public void registerUserWithInvalidValue() throws ParseException, JSONException {
        initRegistrationPage()
            .fillInRegistrationForm(new RegistrationForm())
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
            .fillInRegistrationForm(registrationForm)
            .enterEmail(INVALID_EMAIL)
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), INVALID_EMAIL_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
            .fillInRegistrationForm(registrationForm)
            .enterPhoneNumber(INVALID_PHONE_NUMBER)
            .submitForm();
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), INVALID_PHONE_NUMBER_ERROR_MESSAGE);
    }

    @Test(dependsOnMethods = "verifyEnvironment")
    public void loginAsUnverifiedUserAfterRegistering()
            throws ParseException, JSONException, IOException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        activationLink = doActionWithImapClient(
                imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));
        try {
            waitForProjectsPageLoaded(browser);

            createProjectByGreyPage("Empty Project", null);
            assertFalse(initProjectsAndUsersPage().isEmailingDashboardsTabDisplayed(),
                    "Emailing Dashboards tab is still displayed");

            ProjectAndUsersPage.getInstance(browser).clickInviteUserButton();
            assertFalse(isElementPresent(INVITE_USER_DIALOG_LOCATOR, browser), "Invite user dialog shouldn't be present");
            assertEquals(waitForElementVisible(NEED_ACTIVATE_ACCOUNT_DIALOG_TITLE, browser).getText(),
                    NEED_ACTIVATE_ACCOUNT_MESSAGE);
            takeScreenshot(browser, "Need activate account before inviting users", getClass());
            waitForElementVisible(CLOSE_DIALOG_BUTTON_LOCATOR, browser).click();

            UserProfilePage userProfilePage = ProjectAndUsersPage.getInstance(browser).openUserProfile(registrationUser);
            assertEquals(userProfilePage.getUserRole(), "", "Unverified admin should not show role");
            takeScreenshot(browser, "Unverified user has no role", this.getClass());

            logout()
                    .login(registrationUser, testParams.getPassword(), false);
            assertEquals(getPageErrorMessage(), NOT_FULLY_ACTIVATED_MESSAGE);

            openUrl(activationLink);
            assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

            openUrl(PAGE_LOGIN);
            LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), true);
            waitForDashboardPageLoaded(browser);

            assertTrue(initProjectsAndUsersPage().isEmailingDashboardsTabDisplayed(),
                    "Emailing Dashboards tab is not displayed");

            ProjectAndUsersPage.getInstance(browser).clickInviteUserButton();
            InviteUserDialog inviteUserDialog = Graphene.createPageFragment(InviteUserDialog.class,
                    waitForElementVisible(INVITE_USER_DIALOG_LOCATOR, browser));
            takeScreenshot(browser, "Active user can invite users", getClass());
            inviteUserDialog.cancelInvite();

            userProfilePage = ProjectAndUsersPage.getInstance(browser).openUserProfile(registrationUser);
            assertEquals(userProfilePage.getUserRole(), UserRoles.ADMIN.getName());
        } finally {
            logout();
        }
    }

    @Test(groups = {"sanity"}, dependsOnMethods = "verifyEnvironment")
    public void registerNewUser() throws IOException, ParseException, JSONException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        activationLink = doActionWithImapClient(
                imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));
        waitForProjectsPageLoaded(browser);

        openUrl(activationLink);
        LoginFragment.waitForPageLoaded(browser);
        try {

            takeScreenshot(browser, "register user successfully", this.getClass());
            assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);

            LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), true);
            waitForProjectsPageLoaded(browser);
        } finally {
            logout();
        }
    }

    @Test(dependsOnMethods = {"verifyEnvironment"})
    public void openActivationLinkAfterRegistration() throws IOException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        activationLink = doActionWithImapClient(
                    imapClient -> initRegistrationPage().registerNewUserSuccessfully(imapClient, registrationForm));
        openUrl(activationLink);
        try {
            assertEquals(LoginFragment.getInstance(browser).getNotificationMessage(), ACTIVATION_SUCCESS_MESSAGE);
            logout();

            openUrl(activationLink);
            String messageInfoLoginPage = waitForElementVisible(By.cssSelector(".login-message"), browser).getText();
            assertEquals(messageInfoLoginPage, ALREADY_ACTIVATED_MESSAGE);

            waitForElementVisible(LOG_IN_YOUR_ACCOUNT_LINK, browser).click();

            LoginFragment.getInstance(browser).login(registrationUser, testParams.getPassword(), true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        } finally {
            logout();
        }
    }

    @Test(dependsOnMethods = {"verifyEnvironment"})
    public void registerUserWithEmailOfExistingAccount() throws IOException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);

        initRegistrationPage().registerNewUserSuccessfully(registrationForm);
        waitForProjectsPageLoaded(browser);
        initRegistrationPage()
                .fillInRegistrationForm(new RegistrationForm())
                .enterEmail(generateEmail(registrationUser))
                .enterSpecialCaptcha()
                .agreeRegistrationLicense()
                .submitForm();

        takeScreenshot(browser, "Verification-on-un-registered-email-show-nothing-when-missing-other-fields", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Captcha input shows error but expected is not");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser).enterEmail(registrationUser).submitForm();

        takeScreenshot(browser, "Verification-on-registered-email-show-nothing-when-missing-other-fields", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Captcha input shows error but expected is not");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser).enterCaptcha("aaaaa").submitForm();

        takeScreenshot(browser, "Email-and-captcha-field-show-nothing-when-enter-wrong-captcha", getClass());
        assertFalse(RegistrationPage.getInstance(browser).isEmailInputError(), "Email input shows error but expected is not");
        assertFalse(RegistrationPage.getInstance(browser).isCaptchaInputError(), "Error not show on captcha input");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), FIELD_MISSING_ERROR_MESSAGE);

        RegistrationPage.getInstance(browser)
                .fillInRegistrationForm(registrationForm)
                .enterSpecialCaptcha()
                .submitForm()
                .waitForRegistrationNotSuccessfully();

        takeScreenshot(browser, "Error-message-displays-when-register-user-with-an-existed-email", getClass());
        assertTrue(RegistrationPage.getInstance(browser).isEmailInputError(), "Error not show on email input");
        assertEquals(RegistrationPage.getInstance(browser).getErrorMessage(), EXISTED_EMAIL_ERROR_MESSAGE);
    }

    @Test(dependsOnMethods = "verifyEnvironment")
    public void deleteUserAccount() throws JSONException, IOException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);
        initRegistrationPage().registerNewUserSuccessfully(registrationForm);
        waitForProjectsPageLoaded(browser);

        createProjectByGreyPage("Empty Project", null);
        initAccountPage().tryDeleteAccountButDiscard().deleteAccount();

        LoginFragment loginFragment = LoginFragment.getInstance(browser);
        loginFragment.login(registrationUser, testParams.getPassword(), false);
        loginFragment.checkInvalidLogin();
    }

    @Test(dependsOnMethods = "verifyEnvironment")
    public void deleteUserWithoutActivationTwice() throws IOException {
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);
        initRegistrationPage().registerNewUserSuccessfully(registrationForm);
        waitForProjectsPageLoaded(browser);

        createProjectByGreyPage("Empty Project", null);
        initAccountPage().deleteAccount();
        LoginFragment loginPage = LoginFragment.getInstance(browser);
        loginPage.login(registrationUser, testParams.getPassword(), false);
        loginPage.checkInvalidLogin();

        initRegistrationPage().registerNewUserSuccessfully(registrationForm);
        waitForProjectsPageLoaded(browser);
        createProjectByGreyPage("Empty Project", null);
        initAccountPage().deleteAccount();

        CheckUtils.checkRedBar(browser);
        takeScreenshot(browser, "deleteUserWithoutActivationTwice", getClass());
        loginPage.login(registrationUser, testParams.getPassword(), false);
        loginPage.checkInvalidLogin();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) return;
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .deleteUserByEmail(testParams.getUserDomain(), registrationUser);
    }

    private String getPageErrorMessage() {
        return waitForElementVisible(By.xpath("//*[@class='login-message is-error']/p[1]"), browser).getText();
    }
}
