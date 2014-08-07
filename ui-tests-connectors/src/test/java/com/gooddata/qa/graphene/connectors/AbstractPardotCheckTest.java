package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.UserRoles;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public abstract class AbstractPardotCheckTest extends AbstractConnectorsCheckTest {

    protected static final By BY_INPUT_PARDOT_ACCOUNT_ID = By.name("accountId");

    protected String pardotAccountId;
    protected String pardotUploadUser;
    protected String pardotUploadUserPassword;

    @BeforeClass
    public void loadRequiredProperties() {
        pardotAccountId = testParams.loadProperty("connectors.pardot.accountId");
        pardotUploadUser = testParams.loadProperty("connectors.pardot.uploadUser");
        pardotUploadUserPassword = testParams.loadProperty("connectors.pardot.uploadUserPassword");

        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Pardot Analytics", new String[]{
                "Marketing KPIs", "Contribution", "Prospects", "Opportunities", "and more"
        });
        expectedDashboardsAndTabs.put("Prospects Only", new String[]{
                "Marketing KPIs", "Prospects", "Leaderboard", "and more"
        });

        projectCreateCheckIterations = 120;
        integrationProcessCheckLimit = 240;
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testPardotIntegrationConfiguration() throws InterruptedException, JSONException {
        openUrl(getIntegrationUri());
        // go to page with integration settings
        String settingsUrl = gotoIntegrationSettings();

        // pardot specific configuration of API Url (with specific upload user)
        signInAtGreyPages(pardotUploadUser, pardotUploadUserPassword);
        browser.get(settingsUrl);
        waitForElementVisible(BY_INPUT_PARDOT_ACCOUNT_ID, browser).sendKeys(pardotAccountId);
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT, browser)).click();
        JSONObject json = loadJSON();
        assertEquals(json.getJSONObject("settings").getString("accountId"), pardotAccountId,
                "Pardot accountId was not set to expected value");
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testPardotIntegrationConfiguration"})
    public void testPardotIntegration() throws InterruptedException, JSONException {
        // sign in back with demo user
        signIn(true, UserRoles.ADMIN);
        // process schedule
        scheduleIntegrationProcess(integrationProcessCheckLimit, 0);
    }

}
