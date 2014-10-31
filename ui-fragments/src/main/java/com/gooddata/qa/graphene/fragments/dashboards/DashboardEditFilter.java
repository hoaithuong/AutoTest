package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

/**
 * This fragment is for editing filter when dashboard in edit mode
 *
 */
public class DashboardEditFilter extends AbstractFragment{

    @FindBy(xpath = "//div[contains(@class,'s-active-tab')]//div[contains(@class,'yui3-c-dashboardwidget-editMode')]")
    private List<WebElement> filters;

    @FindBy(xpath = "//div[contains(@class,'s-dashboardwidget-toolbar')]")
    private ToolbarPanel toolbarPanel;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-tabtimefiltereditor-content')]")
    private TimeFilterEditorPanel timeFilterEditorPanel;

    /**
     * return time filter in dashboard
     * 
     * @return
     */
    public WebElement getTimeFilter() {
        for (WebElement filter : filters) {
            if (filter.getAttribute("class").contains("s-filter-time")) return filter;
        }
        return null;
    }

    /**
     * return attribute filter in dashboard base on its name
     * 
     * @param attribute
     * @return
     */
    public WebElement getAttributeFilter(String attribute) {
        for (WebElement filter : filters) {
            if (filter.getAttribute("class").contains("s-" + attribute)) return filter;
        }
        return null;
    }

    /**
     * open toolbar panel for editing filter
     * 
     * @param filter
     * @see ToolbarPanel
     */
    public void openToolbarPanel(WebElement filter) {
        waitForElementVisible(filter).click();
        waitForElementVisible(toolbarPanel.getRoot());
    }

    /**
     * delete filter in dashboard
     * 
     * @param timeOrAttribute
     * @throws InterruptedException
     */
    public void deleteFilter(String timeOrAttribute) throws InterruptedException {
        WebElement filter = "time".equals(timeOrAttribute) ? getTimeFilter() : getAttributeFilter(timeOrAttribute);
        openToolbarPanel(filter);
        waitForElementVisible(toolbarPanel.removeButton).click();
        Thread.sleep(1000);
        Assert.assertFalse(isDashboardContainsFilter(timeOrAttribute));
    }

    /**
     * check if dashboard contains time filter of not
     * 
     * @param timeOrAttribute
     * @return
     */
    public boolean isDashboardContainsFilter(String timeOrAttribute) {
        for (WebElement filter : filters) {
            if (filter.getAttribute("class").contains(String.format("s-%s", "time".equals(timeOrAttribute) ? "filter-time" : timeOrAttribute)))
                return true;
        }
        return false;
    }

    /**
     * Change type of time filter: Day, Week, Month, Quarter, Year
     * 
     * Just support basic cases
     * 
     * @param   type
     */
    public void changeTypeOfTimeFilter(String type) {
        openToolbarPanel(getTimeFilter());
        waitForElementVisible(toolbarPanel.editButton).click();
        waitForElementVisible(By.xpath(String.format(TimeFilterEditorPanel.TYPE, type)), browser).click();
        waitForElementVisible(timeFilterEditorPanel.applyButton).click();
        waitForElementNotVisible(timeFilterEditorPanel.getRoot());
    }

    /**
     * Toolbar panel is opened when clicking on filter when dashboard in edit mode
     *
     */
    private static class ToolbarPanel extends AbstractFragment {

        @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-config')]")
        private WebElement configureButton;

        @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-edit')]")
        private WebElement editButton;

        @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-remove')]")
        private WebElement removeButton;
    }

    /**
     * panel for editing time filter
     *
     */
    private static class TimeFilterEditorPanel extends AbstractFragment {

        static final String TYPE = "//span[text()='%s']";

        @FindBy(xpath = "//div[contains(@class,'timefiltereditor')]//button[contains(@class,'s-btn-apply')]")
        private WebElement applyButton;
    }
}
