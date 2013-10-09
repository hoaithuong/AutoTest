package com.gooddata.qa.graphene.fragments.greypages.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ConnectorFragment extends AbstractGreyPagesFragment {
	
	@FindBy
	private WebElement projectTemplateUri;
	
	@FindBy
	private WebElement active;
	
	@FindBy(xpath="div[@class='submit']/input")
	private WebElement submitIntegrationButton;
	
	public void createIntegration(String template) throws JSONException {
		waitForElementVisible(this.projectTemplateUri);
		this.projectTemplateUri.sendKeys(template);
		Graphene.guardHttp(submitIntegrationButton).click();
		Assert.assertTrue(browser.getCurrentUrl().endsWith("integration"), "Integration was created");
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("integration").getBoolean("active"));
		System.out.println("Integration created...");
	}
	
	public void disableIntegration() throws JSONException {
		waitForElementVisible(active);
		if (active.isSelected()) active.click();
		Graphene.waitGui().until().element(active).is().not().selected();
		Graphene.guardHttp(submitIntegrationButton).click();
		JSONObject json = loadJSON();
		Assert.assertFalse(json.getJSONObject("integration").getBoolean("active"), "Integration wasn't disabled");
		System.out.println("Integration disabled...");
	}
}
