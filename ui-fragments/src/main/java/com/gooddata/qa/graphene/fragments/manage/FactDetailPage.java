package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;

public class FactDetailPage extends ObjectPropertiesPage {

    @FindBy(id = "objectUsedInContainer")
    private WebElement objectUsedInContainer;

    @FindBy(xpath = "//div[@id = 'objectUsedInContainer']/div[@class = 'mezzo']/a")
    private List<WebElement> usedInMetricList;

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]/a")
    private WebElement factLink;

    private final String metricButtonLocator =
            "//td[@class='fceName' and text()='${metricType}']/../td[@class='fceList']/button";
    private final String createdMetricLocator =
            "//td[@class='fceName' and text()='${metricType}']/../td[@class='fceList']/a";
    private final String metricLinkLocator =
            "//div[@id = 'objectUsedInContainer']/a[text() = '${metricName}']";

    public static final FactDetailPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(FactDetailPage.class, waitForElementVisible(LOCATOR, context));
    }

    public String createSimpleMetric(SimpleMetricTypes metricType, String factName) {
        waitForObjectPageLoaded(browser);
        String operation = metricType.getLabel();
        By metricButton = By.xpath(metricButtonLocator.replace("${metricType}", operation));
        waitForElementVisible(metricButton, browser).click();
        waitForElementNotPresent(metricButton);
        String operationInElement =
                operation.substring(0, 1).toUpperCase() + operation.substring(1).toLowerCase();
        String expectedMetricName = String.format("%s [%s]", factName, operationInElement);
        By createdMetric = By.xpath(createdMetricLocator.replace("${metricType}", operation));
        waitForElementVisible(createdMetric, browser);
        assertEquals(browser.findElement(createdMetric).getText(), expectedMetricName,
                "Metric is not created properly");
        verifyUsedInMetric(metricType, factName, expectedMetricName);
        waitForElementVisible(factLink).click();

        return expectedMetricName;
    }

    public void verifyUsedInMetric(SimpleMetricTypes metricType, String factName, String metricName) {
        waitForElementVisible(objectUsedInContainer);
        for (WebElement elem : usedInMetricList) {
            assertTrue(elem.getText() == metricName,
                    "Metric is not created properly and not listed on Used in Metrics");
        }
        By metricLink = By.xpath(metricLinkLocator.replace("${metricName}", metricName));
        waitForElementVisible(metricLink, browser).click();
        String expectedMaql = String.format("SELECT %s(%s)", metricType, factName);
        String expectedFormat = "#,##0.00";
        assertTrue(MetricDetailsPage.getInstance(browser).isMetricCreatedSuccessfully(expectedMaql, expectedFormat),
                "Metric is created unsuccessfully");
    }
}
