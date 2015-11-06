package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EditModeTest extends DashboardWithWidgetsTest {

    @BeforeClass(alwaysRun = true)
    public void before(ITestContext context) {
        super.before();
        boolean isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
        addUsersWithOtherRoles = !isMobileRunning;
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkEditButtonPresent() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkEditButtonPresent", getClass());

        assertTrue(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkEditButtonMissing() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkEditButtonMissing", getClass());

        assertFalse(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkViewerCannotEditDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            assertFalse(initIndigoDashboardsPageWithWidgets().isEditButtonVisible());

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkEditorCanEditDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            assertTrue(initIndigoDashboardsPageWithWidgets().isEditButtonVisible());

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void testNavigateToIndigoDashboardWithoutLogin() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            openUrl(getIndigoDashboardsPageUri());
            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            if (canAccessGreyPage(browser)) {
                signIn(true, UserRoles.ADMIN);
            } else {
                waitForFragmentVisible(loginFragment).login(testParams.getUser(), testParams.getPassword(), true);
                waitForElementVisible(className("gd-header-account"), browser);
            }
        }
    }
}
