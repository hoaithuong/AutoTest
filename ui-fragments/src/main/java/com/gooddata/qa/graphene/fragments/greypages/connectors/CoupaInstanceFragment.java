package com.gooddata.qa.graphene.fragments.greypages.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class CoupaInstanceFragment extends AbstractGreyPagesFragment {
	
	@FindBy
	private WebElement name;
	
	@FindBy
	private WebElement apiUrl;
	
	@FindBy
	private WebElement apiKey;
	
	@FindBy(xpath="div[@class='submit']/input")
	private WebElement createCoupaInstanceButton;
	
	public void createCoupaInstance(String name, String apiUrl, String apiKey) throws JSONException {
		waitForElementVisible(this.name);
		this.name.sendKeys(name);
		this.apiUrl.sendKeys(apiUrl);
		this.apiKey.sendKeys(apiKey);
		Graphene.guardHttp(createCoupaInstanceButton).click();
		JSONObject json = loadJSON();
		Assert.assertEquals(json.getJSONObject("coupaInstance").getString("apiUrl"), apiUrl);
		System.out.println("Coupa instance with apiURL: " + apiUrl + "was created");
	}
}
