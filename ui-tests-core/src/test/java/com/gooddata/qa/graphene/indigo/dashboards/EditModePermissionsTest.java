package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.enums.UserRoles;
import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

public class EditModePermissionsTest extends DashboardsGeneralTest {

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"userTests"})
    public void checkViewerCannotEditDashboard() throws JSONException, InterruptedException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.VIEWER);

            initDashboardsPage();
            initIndigoDashboardsPage();

            assertEquals(indigoDashboardsPage.checkIfEditButtonIsPresent(), false);
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"userTests"})
    public void checkEditorCanEditDashboard() throws JSONException, InterruptedException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.EDITOR);

            initDashboardsPage();
            initIndigoDashboardsPage();

            assertTrue(indigoDashboardsPage.checkIfEditButtonIsPresent());
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"userTests"})
    public void testNavigateToIndigoDashboardWithoutLogin() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            openUrl(PAGE_INDIGO_DASHBOARDS);
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver browser) {
                    return browser.getCurrentUrl().contains(ACCOUNT_PAGE);
                }
            });
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }
}
