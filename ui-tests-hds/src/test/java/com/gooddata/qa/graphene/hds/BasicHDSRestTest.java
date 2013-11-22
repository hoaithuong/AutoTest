package com.gooddata.qa.graphene.hds;

import com.gooddata.qa.graphene.fragments.greypages.hds.StorageFragment;
import com.gooddata.qa.graphene.fragments.greypages.hds.StorageUsersFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.AssertJUnit.assertEquals;

@Test(groups = { "hds" }, description = "Basic verification of hds restapi in GD platform")
public class BasicHDSRestTest extends AbstractHDSTest {
	
	private String storageUrl;
	
	private static final String STORAGE_TITLE = "HDS storage";
	private static final String STORAGE_DESCRIPTION = "HDS description";
	private static final String STORAGE_COPY_OF = "/gdc/storages/${storageId}";

    private static final String NEW_USER_ROLE = "reader"; // todo change once DSS roles are modified
    private static final String NEW_USER_UPDATED_ROLE = "owner"; // todo change once DSS roles are modified
    private static final String NEW_USER_ID = "015ba7e9b00ff2f1d1af252cf5bd29fb"; // todo load from properties file
    private static final String NEW_USER_PROFILE_URI = "/gdc/account/profile/" + NEW_USER_ID;

    @FindBy(tagName="form")
	private StorageFragment storageForm;

    @FindBy(tagName = "form")
    private StorageUsersFragment storageUsersForm;
	
	@BeforeClass
	public void initStartPage() {
		startPage = PAGE_GDC_STORAGES;
	}
	
	@Test(groups = {"hdsInit"})
	public void resourceStoragesNotAvailableForAnonymous() throws JSONException {
		waitForElementPresent(BY_GP_PRE_JSON);
		Assert.assertTrue(browser.getCurrentUrl().contains("gdc/account/token"), "Redirect to /gdc/account/token wasn't done for anonymous user");
	}

	@Test(groups = {"hdsInit"}, dependsOnMethods = { "resourceStoragesNotAvailableForAnonymous" })
	public void resourceStoragesAvailable() throws JSONException {
		validSignInWithDemoUser(true);
		
		loadPlatformPageBeforeTestMethod();
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("storages").has("items"), "storages with items array is not available");
		takeScreenshot(browser, "hds-base-resource", this.getClass());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void gpFormsAvailable() {
		waitForElementPresent(storageForm.getRoot());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void hdsResourceLinkNotAvailableAtBasicResource() {
		openUrl(PAGE_GDC);
		Assert.assertEquals(browser.getTitle(), "GoodData API root");
		Assert.assertTrue(browser.findElements(By.partialLinkText("storages")).size() == 0, "Storages link is present at basic /gdc resource");
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
		Assert.assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
		storageUrl = storageForm.createStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, authorizationToken, null);
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
	public void verifyStorage() throws JSONException {
		openStorageUrl();
		takeScreenshot(browser, "hds-simple-storage", this.getClass());
		JSONObject json = loadJSON();
		Assert.assertTrue(json.has("storage"), "Storage element isn't present");
		JSONObject storage = json.getJSONObject("storage");
		Assert.assertTrue(storage.getString("title").equals(STORAGE_TITLE), "Storage title doesn't match");
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storage.getString("description").equals(STORAGE_DESCRIPTION), "Storage description doesn't match");
		//Assert.assertTrue(storage.getString("authorizationToken").equals(authorizationToken), "Storage authorizationToken doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("self").equals(storageUrl), "Storage self link doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("users").equals(storageUrl + "/users"), "Storage users link doesn't match");
		Assert.assertTrue(storage.getString("status").equals("ENABLED"), "Storage isn't enabled");
		String createdByUrl = storage.getString("createdBy");
		String updatedByUrl = storage.getString("updatedBy");
		Assert.assertEquals(updatedByUrl, createdByUrl, "Storage createdBy and updatedBy attributes do not match");
		String createdDate = storage.getString("created");
		String updatedDate = storage.getString("updated");
		Assert.assertEquals(updatedDate, createdDate, "Storage created and updated dates do not match");
		browser.get(getBasicRootUrl() + storageUrl + "/users");
		JSONObject jsonUsers = loadJSON();
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("parent").equals(storageUrl), "Storage users parent link doesn't match");
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("self").equals(storageUrl + "/users"), "Storage users self link doesn't match");
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").length() == 1, "Number of users doesn't match");
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").getJSONObject(0).getJSONObject("user").getString("profile").equals(createdByUrl), "Creator in users doesn't match with creator in storage");
		browser.get(getBasicRootUrl() + createdByUrl);
		JSONObject jsonUser = loadJSON();
		Assert.assertTrue(jsonUser.getJSONObject("accountSetting").getString("login").equals(user), "Login of user in profile doesn't match");
	}
	
	@Test(dependsOnMethods = { "verifyStorage" })
	public void updateStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, "Some description."), "Edit form doesn't contain current values");
		storageForm.updateStorage(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated");
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated"), "Edit form doesn't contain expected values");
		takeScreenshot(browser, "hds-updated-storage", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateStorage", "removeUserFromStorage"}, alwaysRun = true)
	public void deleteStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		StorageFragment storage = createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
		storage.deleteStorage();
	}
	
	/** ===================== Section with invalid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutTitle() throws JSONException {
		createInvalidStorage(null, STORAGE_DESCRIPTION, authorizationToken, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutDescription() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, null, authorizationToken, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutAuthToken() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, null, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithInvalidCopyOfURI() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, authorizationToken, STORAGE_COPY_OF, "Malformed request");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyTitle() throws JSONException {
		invalidUpdateOfStorage(null, STORAGE_DESCRIPTION, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyDescription() throws JSONException {
		invalidUpdateOfStorage(STORAGE_TITLE, null, "Validation failed");
	}

    /** ===================== Section with storage users ============ */
    // todo remove dependency on updateStorage
    @Test(dependsOnMethods = {"verifyStorage", "updateStorage"})
    public void addUserToStorage() {
        openStorageUsersUrl();

        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_ROLE, NEW_USER_PROFILE_URI);
        takeScreenshot(browser, "hds-add-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }

    @Test(dependsOnMethods = "addUserToStorage")
    public void verifyAddedUser() throws JSONException {
        verifyUser(NEW_USER_ROLE, "hds-added-user");
    }

    @Test(dependsOnMethods = "verifyAddedUser")
    public void updateUser() throws JSONException {
        browser.get(getAddedUserUrlWithHost());
        storageUsersForm.verifyValidUpdateUserForm(NEW_USER_ROLE, NEW_USER_PROFILE_URI);
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_UPDATED_ROLE, NEW_USER_PROFILE_URI);
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

    /** ===================== HELP methods ================= */
	
	private void openStorageUrl() {
		browser.get(getBasicRootUrl() + storageUrl);
		waitForElementVisible(storageForm.getRoot());
		waitForElementPresent(BY_GP_PRE_JSON);
	}

    private void openStorageUsersUrl() {
        browser.get(getBasicRootUrl() + storageUrl + "/users");
        waitForElementVisible(storageForm.getRoot());
        waitForElementPresent(BY_GP_PRE_JSON);

    }

    private String getAddedUserUrl() {
        return storageUrl + "/users/" + NEW_USER_ID;
    }

    private String getAddedUserUrlWithHost() {
        return getBasicRootUrl() + getAddedUserUrl();
    }

    private void createInvalidStorage(String title, String description, String authorizationToken, String copyOf, String expectedErrorMessage) throws JSONException {
		waitForElementVisible(storageForm.getRoot());
		Assert.assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
		storageForm.fillCreateStorageForm(title, description, authorizationToken, copyOf);
		verifyErrorMessage(expectedErrorMessage, PAGE_GDC_STORAGES);
	}

    private void verifyUser(final String role, final String screenshotName) throws JSONException {
        browser.get(getAddedUserUrlWithHost());

        final JSONObject json = loadJSON();
        takeScreenshot(browser, screenshotName, this.getClass());
        final JSONObject userObject = json.getJSONObject("user");
        assertEquals(role, userObject.getString("role"));
        assertEquals(NEW_USER_PROFILE_URI, userObject.getString("profile"));
        assertEquals(getAddedUserUrl(), userObject.getJSONObject("links").getString("self"));
        assertEquals(storageUrl + "/users", userObject.getJSONObject("links").getString("parent"));
    }
	
	private void invalidUpdateOfStorage(String title, String description, String expectedErrorMessage) throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, "Some description."), "Edit form doesn't contain current values");
		storageForm.updateStorage(title, description);
		verifyErrorMessage(expectedErrorMessage, storageUrl);
	}
	
	private void verifyErrorMessage(String messageSubstring, String expectedPage) throws JSONException {
		Assert.assertTrue(browser.getCurrentUrl().endsWith(expectedPage), "Browser was redirected at another page");
		JSONObject json = loadJSON();
		String errorMessage = json.getJSONObject("error").getString("message");
		Assert.assertTrue(errorMessage.contains(messageSubstring), "Another error message present: " + errorMessage + ", expected message substring: " + messageSubstring);
	}
	
	private void verifyStoragesResourceJSON() throws JSONException {
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("storages").has("items"), "storages with items array is not available");
		JSONArray storagesItems = json.getJSONObject("storages").getJSONArray("items");
		if (storagesItems.length() > 0) {
			JSONObject firstStorage = storagesItems.getJSONObject(0).getJSONObject("storage");
			Assert.assertTrue(firstStorage.has("title"), "Storage title isn't present");
			Assert.assertTrue(firstStorage.has("description"), "Storage description isn't present");
			Assert.assertTrue(firstStorage.has("authorizationToken"), "Storage authorizationToken isn't present");
			Assert.assertTrue(firstStorage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
			Assert.assertTrue(firstStorage.getJSONObject("links").has("self"), "Storage self link isn't present");
			Assert.assertTrue(firstStorage.getJSONObject("links").has("users"), "Storage users link isn't present");
			Assert.assertTrue(firstStorage.has("status"), "Storage status isn't present");
		}
		Assert.assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("parent").endsWith("gdc"), "Parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("self").substring(1).equals(PAGE_GDC_STORAGES), "Storages self link doesn't match");
	}
}
