package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SplashScreen;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SplashScreenTest extends DashboardsTest {

    private static final String SPLASH_SCREEN_MOBILE_MESSAGE = "To set up a KPI dashboard, head to your desktop and make your browser window wider.";

    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dateDimension(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DRILL_TO_OUTLOOK)
        .build();

    @BeforeClass(alwaysRun = true)
    public void before(ITestContext context) {
        super.before();
        boolean isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
        addUsersWithOtherRoles = !isMobileRunning;
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop", "empty-state"})
    public void checkNewProjectWithoutKpisFallsToSplashCreen() {
        initIndigoDashboardsPage()
                .getSplashScreen();

        takeScreenshot(browser, "checkNewProjectWithoutKpisFallsToSplashCreen", getClass());
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboard() {
        setupKpiFromSplashScreen(kpi);

        takeScreenshot(browser, "checkCreateNewKpiDashboard", getClass());

        teardownKpiWithDashboardDelete();
    }

    @Test(dependsOnMethods = {"checkDeleteDashboardButtonMissingOnUnsavedDashboard"},
            groups = {"desktop","empty-state"})
    public void checkEnterCreateNewKpiDashboardAndCancel() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        indigoDashboardsPage
                .waitForDashboardLoad()
                .addWidget(kpi)
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        takeScreenshot(browser, "checkEnterCreateNewKpiDashboardAndCancel", getClass());

        indigoDashboardsPage
                .getSplashScreen()
                .startEditingWidgets();

        assertEquals(indigoDashboardsPage.getKpisCount(), 0);
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboardRemoveAndCreateAgain() {
        setupKpiFromSplashScreen(kpi);
        teardownKpiWithDashboardDelete();

        // do not use setupKpi here - it refreshes the page
        // this is a test case without page refresh
        indigoDashboardsPage
                .getSplashScreen()
                .startEditingWidgets();
        indigoDashboardsPage
                .addWidget(kpi)
                .saveEditModeWithKpis();

        takeScreenshot(browser, "checkCreateNewKpiDashboardRemoveAndCreateAgain", getClass());

        // do not use teardownKpi here - it refreshes the page
        // this is a test case without page refresh
        indigoDashboardsPage
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
        setupKpiFromSplashScreen(kpi);

        indigoDashboardsPage
                .switchToEditMode()
                .deleteDashboard(false);

        indigoDashboardsPage
                .waitForEditingControls()
                .waitForSplashscreenMissing();

        takeScreenshot(browser, "checkDeleteDashboardWithCancelAndConfirm-cancel", getClass());

        indigoDashboardsPage
                .deleteDashboard(true);

        indigoDashboardsPage.getSplashScreen();

        takeScreenshot(browser, "checkDeleteDashboardWithCancelAndConfirm-confirm", getClass());
    }

    @Test(dependsOnMethods = {"checkNewProjectWithoutKpisFallsToSplashCreen"}, groups = {"desktop", "empty-state"})
    public void checkDeleteDashboardButtonMissingOnUnsavedDashboard() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        indigoDashboardsPage.waitForEditingControls();
        assertFalse(indigoDashboardsPage.isDeleteButtonVisible());

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
            initDashboardsPage();

            logout();
            signIn(true, UserRoles.VIEWER);

            initIndigoDashboardsPage()
                .getSplashScreen()
                .waitForCreateKpiDashboardButtonMissing();

        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop", "empty-state"})
    public void checkEditorCanCreateDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(true, UserRoles.EDITOR);

            initIndigoDashboardsPage()
                .getSplashScreen()
                .waitForCreateKpiDashboardButtonVisible();

        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }
}
