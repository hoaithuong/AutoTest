package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricDetailsPage extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]")
    private WebElement maql;

    @FindBy(xpath = "//span[contains(@class,'metric_format')]")
    private WebElement metricFormat;

    public String getMAQL(String metricName) {
	return waitForElementVisible(maql).getText();
    }

    public String getMetricFormat(String metricName) {
	return waitForElementVisible(metricFormat).getText();
    }

    public void checkCreatedMetric(String metricName, String expectedMaql,
	    String expectedFormat) {
	Assert.assertEquals(getMAQL(metricName), expectedMaql,
		"Metric is not created properly");
	Assert.assertEquals(getMetricFormat(metricName), expectedFormat,
		"Metric format is not set properly");
    }
}
