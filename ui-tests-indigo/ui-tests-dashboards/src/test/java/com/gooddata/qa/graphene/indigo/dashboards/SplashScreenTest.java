package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringMissingInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Arrays;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SplashScreen;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;

public class SplashScreenTest extends DashboardsTest {

    private static final String SPLASH_SCREEN_MOBILE_MESSAGE = "To set up a KPI dashboard, head to your desktop and make your browser window wider.";

    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(METRIC_AMOUNT)
        .dataSet(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DASH_TAB_OUTLOOK)
        .build();

    @BeforeClass(alwaysRun = true)
    public void before(ITestContext context) {
        super.before();
        boolean isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
        addUsersWithOtherRoles = !isMobileRunning;
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop", "empty-state"})
    public void checkNewProjectWithoutKpisFallsToSplashCreen() {
        initIndigoDashboardsPage().getSplashScreen();

        takeScreenshot(browser, "checkNewProjectWithoutKpisFallsToSplashCreen", getClass());
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboard() {
        setupKpisFromSplashScreen(Arrays.asList(kpi));

        takeScreenshot(browser, "checkCreateNewKpiDashboard", getClass());

        teardownKpiWithDashboardDelete();
    }

    @Test(dependsOnMethods = {"checkDeleteDashboardButtonMissingOnUnsavedDashboard"},
            groups = {"desktop","empty-state"})
    public void checkEnterCreateNewKpiDashboardAndCancel() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        waitForFragmentVisible(indigoDashboardsPage)
                .waitForDashboardLoad()
                .addWidget(kpi)
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        takeScreenshot(browser, "checkEnterCreateNewKpiDashboardAndCancel", getClass());

        waitForFragmentVisible(indigoDashboardsPage)
                .getSplashScreen()
                .startEditingWidgets();

        assertEquals(indigoDashboardsPage.getKpisCount(), 0);
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboardRemoveAndCreateAgain() {
        setupKpisFromSplashScreen(Arrays.asList(kpi));
        teardownKpiWithDashboardDelete();

        // do not use setupKpi here - it refreshes the page
        // this is a test case without page refresh
        waitForFragmentVisible(indigoDashboardsPage)
                .getSplashScreen()
                .startEditingWidgets();
        indigoDashboardsPage
                .addWidget(kpi)
                .saveEditModeWithKpis();

        takeScreenshot(browser, "checkCreateNewKpiDashboardRemoveAndCreateAgain", getClass());

        // do not use teardownKpi here - it refreshes the page
        // this is a test case without page refresh
        waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .clickLastKpiDeleteButton()
                .waitForDialog()
                .submitClick();
        indigoDashboardsPage
                .saveEditModeWithoutKpis()
                .getSplashScreen();
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkDeleteDashboardWithCancelAndConfirm() {
        setupKpisFromSplashScreen(Arrays.asList(kpi));

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

        setupKpisFromSplashScreen(Arrays.asList(kpi));

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

    @Test(dependsOnMethods = {"checkNewProjectWithoutKpisFallsToSplashCreen"}, groups = {"desktop", "empty-state"})
    public void checkDeleteDashboardButtonMissingOnUnsavedDashboard() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        assertFalse(indigoDashboardsPage.waitForEditingControls().isDeleteButtonVisible());

        takeScreenshot(browser, "checkDeleteDashboardButtonMissingOnUnsavedDashboard", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"mobile"})
    public void checkCreateNewKpiDashboardNotAvailableOnMobile() {
        SplashScreen splashScreen = initIndigoDashboardsPage().getSplashScreen();
        String mobileMessage = splashScreen.getMobileMessage();

        assertEquals(mobileMessage, SPLASH_SCREEN_MOBILE_MESSAGE);
        splashScreen.waitForCreateKpiDashboardButtonMissing();

        takeScreenshot(browser, "checkCreateNewKpiDashboardNotAvailableOnMobile", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop", "empty-state"})
    public void checkViewerCannotCreateDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            // viewer accessing "dashboards" with no kpi dashboards created should be redirected
            openUrl(getIndigoDashboardsPageUri());

            // check that we are not on dashboards page
            waitForElementVisible(By.className("s-displayed"), browser);
            waitForStringMissingInUrl("/dashboards");

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop", "empty-state"})
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

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop", "empty-state"})
    public void checkCannotSaveNewEmptyDashboard() throws JSONException {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();
        waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_THIS_QUARTER);

        takeScreenshot(browser, "checkCannotSaveNewEmptyDashboard", getClass());
        assertFalse(indigoDashboardsPage.isSaveEnabled());
    }
}
