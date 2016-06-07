package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.google.common.base.Predicate;

public class MetricConfigPanel extends AbstractFragment {

    @FindBy(xpath = "//*[contains(@class,'metricRow')]//*[./*[.='Metric']]/following-sibling::button")
    private WebElement metricSelect;

    @FindBy(xpath = "//*[contains(@class,'metricRow')]//*[./*[.='by']]/following-sibling::button")
    private WebElement dateDimensionSelect;

    @FindBy(css = ".whenFilter button")
    private WebElement whenFilter;

    @FindBy(css = ".periodSelect button")
    private WebElement periodSelect;

    @FindBy(css = ".linkExternalFilter input")
    private WebElement linkExternalFilter;

    public void selectMetric(String metric, String... dateDimension) {
        waitForElementVisible(metricSelect).click();
        Predicate<WebDriver> popupDisplayed = browser -> browser.findElements(SelectItemPopupPanel.LOCATOR).size() > 1;
        Graphene.waitGui().until(popupDisplayed);

        Graphene.createPageFragment(SelectItemPopupPanel.class,
                browser.findElements(SelectItemPopupPanel.LOCATOR).get(1))
                .searchAndSelectEmbedItem(metric);

        if (dateDimension.length == 0) return;
        waitForElementVisible(dateDimensionSelect).click();
        Graphene.waitGui().until(popupDisplayed);
        Graphene.createPageFragment(SelectItemPopupPanel.class,
                browser.findElements(SelectItemPopupPanel.LOCATOR).get(1))
                .searchAndSelectEmbedItem(dateDimension[0]);
    }

    public boolean isWhenDropdownVisibled() {
        return waitForElementPresent(whenFilter).isDisplayed() && waitForElementPresent(periodSelect).isDisplayed();
    }

    public boolean isWhenDropdownEnabled() {
        return isElementEnabled(waitForElementPresent(whenFilter)) &&
                isElementEnabled(waitForElementPresent(periodSelect));
    }

    public boolean isLinkExternalFilterVisible() {
        return waitForElementPresent(linkExternalFilter).isDisplayed();
    }

    public boolean isLinkExternalFilterSelected() {
        return waitForElementPresent(linkExternalFilter).isSelected();
    }

    private boolean isElementEnabled(WebElement element) {
        return !element.getAttribute("class").contains("disabled");
    }
}
