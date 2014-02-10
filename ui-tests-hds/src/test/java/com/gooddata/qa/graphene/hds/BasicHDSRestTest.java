package com.gooddata.qa.graphene.hds;

import com.gooddata.qa.graphene.fragments.greypages.hds.StorageFragment;
import com.gooddata.qa.graphene.fragments.greypages.hds.StorageUsersFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = { "hds" }, description = "Basic verification of hds restapi in GD platform")
public class BasicHDSRestTest extends AbstractHDSTest {
	
	private String storageUrl;
	private String userCreatedByUrl;
	private String userCreatedById;

	private String testUserId;
    private String testUserLogin;
	
	private static final String STORAGE_TITLE = "HDS storage";
	public static final String UPDATED_STORAGE_TITLE = STORAGE_TITLE + " updated";
	private static final String STORAGE_DESCRIPTION = "HDS description";
	public static final String UPDATED_STORAGE_DESCRIPTION = STORAGE_DESCRIPTION + " updated";
	private static final String STORAGE_COPY_OF = "/gdc/dss/instances/${storageId}";

    private static final String NEW_USER_ROLE = "dataAdmin";
    private static final String NEW_USER_UPDATED_ROLE = "admin";
    
    @FindBy(tagName="form")
	private StorageFragment storageForm;

    @FindBy(tagName = "form")
    private StorageUsersFragment storageUsersForm;
	
	@BeforeClass
	public void initStartPage() {
		startPage = PAGE_GDC_STORAGES;
		testUserId = loadProperty("hds.storage.test.user.id");
		testUserLogin = loadProperty("hds.storage.test.user.login");
	}
	
	@Test(groups = {"hdsInit"})
	public void resourceStoragesNotAvailableForAnonymous() throws JSONException {
		waitForElementPresent(BY_GP_PRE_JSON);
		assertTrue(browser.getCurrentUrl().contains("gdc/account/token"), "Redirect to /gdc/account/token wasn't done for anonymous user");
	}

	@Test(groups = {"hdsInit"}, dependsOnMethods = { "resourceStoragesNotAvailableForAnonymous" })
	public void resourceStoragesAvailable() throws JSONException {
		validSignInWithDemoUser(true);
		
		loadPlatformPageBeforeTestMethod();
		JSONObject json = loadJSON();
		assertTrue(json.getJSONObject("dssInstances").has("items"), "DSS instances with items array is not available");
		takeScreenshot(browser, "hds-base-resource", this.getClass());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void gpFormsAvailable() {
		waitForElementPresent(storageForm.getRoot());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void hdsResourceLinkNotAvailableAtBasicResource() {
		openUrl(PAGE_GDC);
		assertEquals(browser.getTitle(), "GoodData API root");
		assertTrue(browser.findElements(By.partialLinkText("dssInstances")).size() == 0, "DSS instances link is present at basic /gdc resource");
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void verifyDefaultResource() throws JSONException {
		verifyStoragesResourceJSON();
	}
	
	/** ===================== Section with valid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void verifyStorageCreateFormPresentWithTrailingSlash() throws JSONException {
		openUrl(PAGE_GDC_STORAGES + "/");
		waitForElementVisible(storageForm.getRoot());
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorage() throws JSONException, InterruptedException {
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
		storageUrl = storageForm.createStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, dssAuthorizationToken, null);
	}
	
	@Test(dependsOnMethods = {"createStorage"})
	public void verifyDefaultResourceAfterStorageCreated() throws JSONException {
		verifyStoragesResourceJSON();
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void verifyStorageUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + storageUrl + "/");
		waitForElementVisible(storageForm.getRoot());
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void verifyStorageEnabled() throws JSONException {
		verifyStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, "ENABLED");
		verifyStorageUsers();
	}

	@Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void updateStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storageForm.updateStorage(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION);
		assertTrue(storageForm.verifyValidEditStorageForm(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION), "Edit form doesn't contain expected values");
		takeScreenshot(browser, "hds-updated-storage", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateStorage", "removeUserFromStorageByLogin"}, alwaysRun = true)
	public void deleteStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		StorageFragment storage = createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
		storage.deleteStorageSuccess();
	}
	
	/** ===================== Section with invalid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutTitle() throws JSONException {
		createInvalidStorage(null, STORAGE_DESCRIPTION, dssAuthorizationToken, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutDescription() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, null, dssAuthorizationToken, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutAuthToken() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, null, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithNonexistentAuthToken() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, "nonexistentAuthToken", null, "Project group with name 'nonexistentAuthToken' does not exists.");
	}

	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithNonDssAuthToken() throws JSONException {
		// use non-dss-enabled authorization token to create new dss
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, authorizationToken, null, "DSS project group source is missing");
	}

	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithInvalidCopyOfURI() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, dssAuthorizationToken, STORAGE_COPY_OF, "Malformed request");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyTitle() throws JSONException {
		invalidUpdateOfStorage(null, STORAGE_DESCRIPTION, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyDescription() throws JSONException {
		invalidUpdateOfStorage(STORAGE_TITLE, null, "Validation failed");
	}

    /** ===================== Section with valid storage users cases ============ */
    
	@Test(dependsOnMethods = {"verifyStorageEnabled"})
    public void addUserToStorage() throws JSONException, InterruptedException {
        openStorageUsersUrl();
        assertTrue(testUserId != null, "Missing test user ID - provide property 'hds.storage.test.user.id'");
        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_ROLE, getTestUserProfileUri(), null, true);
        takeScreenshot(browser, "hds-add-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }
	
	@Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void verifyStorageUsersAddFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + getStorageUsersUrl() + "/");
		waitForElementVisible(storageUsersForm.getRoot());
	}
	
	@Test(dependsOnMethods = { "addUserToStorage" })
	public void verifyStorageUsersUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getAddedUserUrlWithHost() + "/");
		waitForElementVisible(storageUsersForm.getRoot());
	}

    @Test(dependsOnMethods = "addUserToStorage")
    public void verifyAddedUser() throws JSONException {
        verifyUser(NEW_USER_ROLE, "hds-added-user");
    }

    @Test(dependsOnMethods = "verifyAddedUser")
    public void updateUser() throws JSONException {
        browser.get(getAddedUserUrlWithHost());
        storageUsersForm.verifyValidUpdateUserForm(NEW_USER_ROLE, getTestUserProfileUri());
        storageUsersForm.fillUpdateUserForm(NEW_USER_UPDATED_ROLE, getTestUserProfileUri());
        takeScreenshot(browser, "hds-update-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }
    
    @Test(dependsOnMethods = "updateUser")
    public void verifyUpdatedUser() throws JSONException {
        verifyUser(NEW_USER_UPDATED_ROLE, "hds-updated-user");
    }

    @Test(dependsOnMethods = {"verifyUpdatedUser"})
    public void removeUserFromStorage() throws Exception {
        browser.get(getAddedUserUrlWithHost());
        final StorageUsersFragment deleteFragment =
                createPageFragment(StorageUsersFragment.class, browser.findElement(BY_GP_FORM_SECOND));
        deleteFragment.verifyValidDeleteUserForm();
        deleteFragment.deleteUser();
        final JSONObject jsonObject = loadJSON();
        assertEquals(browser.getCurrentUrl(), getBasicRootUrl() + storageUrl + "/users");
        assertEquals(1, jsonObject.getJSONObject("users").getJSONArray("items").length());
    }

    @Test(dependsOnMethods = {"removeUserFromStorage"})
    public void addUserToStorageByLogin() throws JSONException, InterruptedException {
        openStorageUsersUrl();
        assertTrue(testUserId != null, "Missing test user ID - provide property 'hds.storage.test.user.id'");
        assertTrue(testUserLogin != null, "Missing test user login - provide property 'hds.storage.test.user.login'");
        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_ROLE, null, testUserLogin, true);
        takeScreenshot(browser, "hds-add-user-filled-form-login", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }

    @Test(dependsOnMethods = "addUserToStorageByLogin")
    public void verifyAddedUserByLogin() throws JSONException {
        verifyAddedUser();
    }

    @Test(dependsOnMethods = {"verifyAddedUserByLogin"})
    public void removeUserFromStorageByLogin() throws Exception {
        removeUserFromStorage();
    }
    
    /** ===================== Section with invalid storage users cases ============ */
    
    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addUserWithEmptyProfileAndLogin() throws JSONException, InterruptedException {
        invalidUserAssignment(null, null, NEW_USER_ROLE, "One (and only one) of \"profile\" or \"login\" must be " +
            "provided.");
	}

    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addUserWithBothProfileAndLogin() throws JSONException, InterruptedException {
        invalidUserAssignment(getTestUserProfileUri(), testUserLogin, NEW_USER_ROLE,
                "One (and only one) of \"profile\" or \"login\" must be provided.");
	}
    
    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addNonExistingUser() throws JSONException, InterruptedException {
        invalidUserAssignment("/gdc/account/profile/nonexisting", null, NEW_USER_ROLE, "User 'nonexisting' has not been found");
	}
    
    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addUserWithInvalidURI() throws JSONException, InterruptedException {
        invalidUserAssignment("/invalid/uri", null, NEW_USER_ROLE, "Validation failed");
	}

    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addUserWithInvalidLogin() throws JSONException, InterruptedException {
        invalidUserAssignment(null, "asdfasdfa", NEW_USER_ROLE, "must be a string matching the regular expression \".+@.+\\..+\"]]");
	}
    
    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addUserWithInvalidURI2() throws JSONException, InterruptedException {
        invalidUserAssignment("/gdc/account/profile", null, NEW_USER_ROLE, "Validation failed");
	}
    
    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addUserWithInvalidURI3() throws JSONException, InterruptedException {
        invalidUserAssignment("/gdc/account/profile/", null, NEW_USER_ROLE, "Validation failed");
	}
	
    @Test(dependsOnMethods = {"verifyStorageEnabled"})
	public void addExistingUserToStorage() throws JSONException, InterruptedException {
        invalidUserAssignment(userCreatedByUrl, null, NEW_USER_ROLE, "User '" + userCreatedById + "' already exists in storage '");
	}

    /** ===================== Section with invalid storage state cases ============ */

	@Test(dependsOnMethods = { "deleteStorage" })
	public void verifyDeletedStorage() throws JSONException, InterruptedException {
		openStorageUrl();
		verifyStorage(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION, "DELETED");
	}

	@Test(dependsOnMethods = { "verifyDeletedStorage" })
	public void deleteDeletedStorage() throws JSONException, InterruptedException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		StorageFragment storage = createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
		storage.deleteStorage();
		verifyStorageNotReady(storageUrl);
	}

	@Test(dependsOnMethods = { "deleteDeletedStorage" })
	public void getUsersInDeletedStorage() throws JSONException {
		browser.get(getBasicRootUrl() + getStorageUsersUrl());
		verifyStorageNotReady(getStorageUsersUrl());
	}

	@Test(dependsOnMethods = { "deleteDeletedStorage" })
	public void getJdbcInfoInDeletedStorage() throws JSONException {
		browser.get(getBasicRootUrl() + getStorageJdbcUrl());
		verifyStorageNotReady(getStorageJdbcUrl());
	}

	@Test(dependsOnMethods = { "deleteDeletedStorage" })
	public void updateDeletedStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidEditStorageForm(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storageForm.updateStorage(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION);
		verifyStorageNotReady(storageUrl);
	}

    // TODO add next invalid cases when permissions are implemented

    /** ===================== HELP methods ================= */
	
	private void openStorageUrl() {
		browser.get(getBasicRootUrl() + storageUrl);
		waitForElementVisible(storageForm.getRoot());
		waitForElementPresent(BY_GP_PRE_JSON);
	}

    private void openStorageUsersUrl() {
        browser.get(getBasicRootUrl() + getStorageUsersUrl());
        waitForElementVisible(storageUsersForm.getRoot());
        waitForElementPresent(BY_GP_PRE_JSON);
    }
    
    private String getStorageUsersUrl() {
    	return storageUrl + "/users";
    }

	private String getStorageJdbcUrl() {
    	return storageUrl + "/jdbc";
    }
    
    private String getTestUserProfileUri() {
    	return "/gdc/account/profile/" + testUserId;
    }

    private String getAddedUserUrl() {
        return getStorageUsersUrl() + "/" + testUserId;
    }
    
    private String getAddedUserUrlWithHost() {
        return getBasicRootUrl() + getAddedUserUrl();
    }

    private void createInvalidStorage(String title, String description, String authorizationToken, String copyOf, String expectedErrorMessage) throws JSONException {
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
		storageForm.fillCreateStorageForm(title, description, authorizationToken, copyOf);
		verifyErrorMessage(expectedErrorMessage, PAGE_GDC_STORAGES);
	}

    private void verifyUser(final String role, final String screenshotName) throws JSONException {
        browser.get(getAddedUserUrlWithHost());

        final JSONObject json = loadJSON();
        takeScreenshot(browser, screenshotName, this.getClass());
        final JSONObject userObject = json.getJSONObject("user");
        assertEquals(role, userObject.getString("role"));
        assertEquals(getTestUserProfileUri(), userObject.getString("profile"));
        assertEquals(getAddedUserUrl(), userObject.getJSONObject("links").getString("self"));
        assertEquals(storageUrl + "/users", userObject.getJSONObject("links").getString("parent"));
    }
    
    private void invalidUserAssignment(String userProfile, String login, String role, String expectedErrorMessage) throws JSONException, InterruptedException {
    	openStorageUsersUrl();
    	storageUsersForm.verifyValidAddUserForm();
		storageUsersForm.fillAddUserToStorageForm(role, userProfile, login, false);
		verifyErrorMessage(expectedErrorMessage, storageUrl + "/users");
	}
    
    private void invalidUpdateOfStorage(String title, String description, String expectedErrorMessage) throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storageForm.updateStorage(title, description);
		verifyErrorMessage(expectedErrorMessage, storageUrl);
	}
	
	private void verifyErrorMessage(String messageSubstring, String expectedPage) throws JSONException {
		assertTrue(browser.getCurrentUrl().endsWith(expectedPage), "Browser was redirected at another page");
		JSONObject json = loadJSON();
		String errorMessage = json.getJSONObject("error").getString("message");
		assertTrue(errorMessage.contains(messageSubstring), "Another error message present: " + errorMessage + ", expected message substring: " + messageSubstring);
	}
	
	private void verifyStoragesResourceJSON() throws JSONException {
		JSONObject json = loadJSON();
		assertTrue(json.getJSONObject("dssInstances").has("items"), "DSS instances with items array is not available");
		JSONArray storagesItems = json.getJSONObject("dssInstances").getJSONArray("items");
		if (storagesItems.length() > 0) {
			JSONObject firstStorage = storagesItems.getJSONObject(0).getJSONObject("dssInstance");
			assertTrue(firstStorage.has("title"), "DSS instance title isn't present");
			assertTrue(firstStorage.has("description"), "DSS instance description isn't present");
			assertTrue(firstStorage.has("authorizationToken"), "DSS instance authorizationToken isn't present");
			assertTrue(firstStorage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "DSS instance parent link doesn't match");
			assertTrue(firstStorage.getJSONObject("links").has("self"), "DSS instance self link isn't present");
			assertTrue(firstStorage.getJSONObject("links").has("users"), "DSS instance users link isn't present");
			assertTrue(firstStorage.has("status"), "DSS instance status isn't present");
		}
		assertTrue(json.getJSONObject("dssInstances").getJSONObject("links").getString("parent").endsWith("gdc"), "Parent link doesn't match");
		assertTrue(json.getJSONObject("dssInstances").getJSONObject("links").getString("self").substring(1).equals(PAGE_GDC_STORAGES), "DSS instances self link doesn't match");
	}

	private void verifyStorage(final String title, final String description, final String state) throws JSONException {
		openStorageUrl();
		takeScreenshot(browser, "hds-simple-storage", this.getClass());
		JSONObject json = loadJSON();
		assertTrue(json.has("dssInstance"), "DSS instance element isn't present");
		JSONObject storage = json.getJSONObject("dssInstance");
		assertTrue(storage.getString("title").equals(title), "DSS instance title doesn't match");
		assertTrue(storage.getString("description").equals(description), "DSS instance description doesn't match");
		assertTrue(storage.getString("authorizationToken").equals(dssAuthorizationToken), "DSS instance authorizationToken doesn't match");
		assertTrue(storage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "DSS instance parent link doesn't match");
		assertTrue(storage.getJSONObject("links").getString("self").equals(storageUrl), "DSS instance self link doesn't match");
		assertTrue(storage.getJSONObject("links").getString("users").equals(storageUrl + "/users"), "DSS instance users link doesn't match");
		final String currentState = storage.getString("status");
		assertTrue(currentState.equals(state), format("DSS instance is in invalid state - %s.", currentState));
		userCreatedByUrl = storage.getString("createdBy");
		userCreatedById = userCreatedByUrl.substring(userCreatedByUrl.lastIndexOf("/") + 1);

		String updatedByUrl = storage.getString("updatedBy");
		assertEquals(updatedByUrl, userCreatedByUrl, "DSS instance createdBy and updatedBy attributes do not match");
		assertTrue(storage.has("created"), "Created time not present");
		assertTrue(storage.has("updated"), "Updated time not present");
		browser.get(getBasicRootUrl() + getStorageUsersUrl());
	}

	private void verifyStorageUsers() throws JSONException {
		JSONObject jsonUsers = loadJSON();
		assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("parent").equals(storageUrl), "DSS instance users parent link doesn't match");
		assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("self").equals(storageUrl + "/users"), "DSS instance users self link doesn't match");
		assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").length() == 1, "Number of users doesn't match");
		assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").getJSONObject(0).getJSONObject("user").getString("profile").equals(userCreatedByUrl), "Creator in users doesn't match with creator in storage");
		browser.get(getBasicRootUrl() + userCreatedByUrl);
		JSONObject jsonUser = loadJSON();
		assertTrue(jsonUser.getJSONObject("accountSetting").getString("login").equals(user), "Login of user in profile doesn't match");
	}

	private void verifyStorageNotReady(final String url) throws JSONException {
		verifyErrorMessage("Cannot perform action. DSS instance is in DELETED state.", url);
	}
}
