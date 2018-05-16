package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

import java.util.List;
import java.util.stream.Collectors;

public class AvailableValuesConfigPanel extends AbstractFragment {

    @FindBy(className = "availaleDescription")
    private WebElement availableValuesDescriptions;

    @FindBy(css = ".availaleDescription a")
    private WebElement moreInfoLink;

    @FindBy(className = "addMetricButton")
    private WebElement addMetricButton;

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    @FindBy(className = "yui3-c-useavailableitempanel")
    private List<WebElement> selectedMetrics;

    public String getAvailableValuesDescriptions() {
        return waitForElementVisible(availableValuesDescriptions).getText();
    }

    public String getMoreInfoText() {
        return waitForElementVisible(moreInfoLink).getText();
    }

    public boolean isAddMetricButtonVisible() {
        return isElementVisible(addMetricButton);
    }

    public boolean isAddMetricButtonEnabled() {
        return !waitForElementPresent(addMetricButton).getAttribute("class").contains("disabled");
    }

    public SelectItemPopupPanel openMetricPickerDropDown() {
        waitForElementVisible(addMetricButton).click();
        return SelectItemPopupPanel.getInstance(browser);
    }

    public AvailableValuesConfigPanel selectMetrics(List<String> metricNames) {
        metricNames.forEach(metricName -> selectMetric(metricName));
        return this;
    }

    public AvailableValuesConfigPanel selectMetric(String metricName) {
        openMetricPickerDropDown().searchAndSelectItem(metricName);
        // wait for metric name and delete button displaying. Otherwise, the next add metric action could be failed
        // https://jira.intgdc.com/browse/QA-7153
        WebElement metricElement = waitForElementVisible(By.cssSelector(String.format(".metricName [title='%s']", metricName)), this.getRoot());
        waitForElementVisible(By.className("deleteMetricButton"), metricElement.findElement(BY_PARENT).findElement(BY_PARENT));
        return this;
    }

    public List<String> getSelectedMetrics() {
        return getElementTexts(selectedMetrics);
    }

    public String getTooltipFromIHiddenMetric(String selectedMetric) {
        return getSelectedMetric(selectedMetric).findElement(By.className("icon-unlisted")).getAttribute("title");
    }

    public String getTooltipFromSelectedMetric(String selectedMetric) {
        return getSelectedMetric(selectedMetric).findElement(By.cssSelector(".deleted .label")).getAttribute("title");
    }

    private WebElement getSelectedMetric(String selectedMetric) {
        return selectedMetrics.stream().filter(metric -> selectedMetric.equals(metric.getText())).findFirst().get();
    }
}
