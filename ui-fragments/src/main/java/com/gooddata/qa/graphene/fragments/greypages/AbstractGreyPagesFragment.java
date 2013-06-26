package com.gooddata.qa.graphene.fragments.greypages;

import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractGreyPagesFragment extends AbstractFragment {
	
	protected static final By BY_GP_FORM = By.tagName("form");
	protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
	protected static final By BY_GP_PRE_JSON = By.tagName("pre");
	protected static final By BY_GP_LINK = By.tagName("a");
	protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");
	
	protected JSONObject loadJSON() throws JSONException {
		WebDriver browser = GrapheneContext.getProxy();
		waitForElementPresent(BY_GP_PRE_JSON);
		return new JSONObject(browser.findElement(BY_GP_PRE_JSON).getText());
	}

}
