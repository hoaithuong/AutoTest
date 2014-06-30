/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public abstract class AbstractZendeskCheckTest extends AbstractConnectorsCheckTest {

    protected static final By BY_INPUT_API_URL = By.name("apiUrl");

    protected String zendeskUploadUser;
    protected String zendeskUploadUserPassword;
    protected String zendeskApiUrl;

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testZendeskIntegrationConfiguration() throws InterruptedException, JSONException {
        openUrl(getIntegrationUri());
        // go to page with integration settings (differs for Zendesk3/4)
        String settingsUrl = openZendeskSettingsUrl();
        JSONObject json = greyPageUtils.loadJSON();
        assertEquals(json.getJSONObject("settings").getString("apiUrl"), "null",
                String.format("%s API URL was not set to expected value", connectorType.getName()));

        // zendesk specific configuration of API Url (with specific upload user)
        greyPageUtils.signInAtGreyPages(zendeskUploadUser, zendeskUploadUserPassword);
        browser.get(settingsUrl);
        waitForElementPresent(BY_INPUT_API_URL).sendKeys(zendeskApiUrl);
        Graphene.guardHttp(waitForElementPresent(greyPageUtils.BY_GP_BUTTON_SUBMIT)).click();
        json = greyPageUtils.loadJSON();
        assertEquals(json.getJSONObject("settings").getString("apiUrl"), zendeskApiUrl,
                String.format("%s API URL was not set to expected value", connectorType.getName()));
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testZendeskIntegrationConfiguration"})
    public void testZendeskIntegration() throws InterruptedException, JSONException {
        // sign in back with demo user
        validSignInWithDemoUser(true);
        // process schedule
        scheduleIntegrationProcess(integrationProcessCheckLimit, 0);
    }

    public abstract String openZendeskSettingsUrl();
}
