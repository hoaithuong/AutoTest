package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.utils.CssUtils;
import com.google.common.base.Predicate;

public class DrillingConfigPanel extends AbstractFragment {

    @FindBy(className = "s-btn-add_drilling")
    private WebElement drillingButton;

    @FindBy(className = "s-btn-add_more___")
    private WebElement addMoreButton;

    @FindBy(className = "s-btn-select_metric___attribute___")
    private WebElement selectMetricAttributeButton;

    @FindBy(className = "s-btn-select_attribute___report___dashboard")
    private WebElement selectAttributeReportDashboardButton;
    
    @FindBy(className = "yui3-drillitempanel")
    private List<WebElement> drillItemPanelList;

    public void addDrilling(Pair<List<String>, String> pairs, String group) {
        int currentDrillItemPanelSize = drillItemPanelList.size();
        waitForElementVisible(isAddDrillingButtonVisible() ? drillingButton : addMoreButton).click();

        Predicate<WebDriver> newDrillItemPanelAdded =
                browser -> drillItemPanelList.size() == currentDrillItemPanelSize + 1;
        Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(newDrillItemPanelAdded);

        //if there are more than 2 drill items, the scroll bar displays.
        if (drillItemPanelList.size() > 2) {
            scrollElementIntoView(waitForElementVisible(selectMetricAttributeButton), browser);
        }
        waitForElementVisible(selectMetricAttributeButton).click();

        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return input.findElements(SelectItemPopupPanel.LOCATOR).size() > 1;
            }
        });

        SelectItemPopupPanel popupPanel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                browser.findElements(SelectItemPopupPanel.LOCATOR).get(1));

        for (String item : pairs.getLeft()) {
            popupPanel.searchAndSelectItem(item).submitPanel();
        }
        waitForElementVisible(selectAttributeReportDashboardButton).click();
        waitForElementVisible(popupPanel.getRoot());
        popupPanel.changeGroup(group);
        popupPanel.searchAndSelectItem(pairs.getRight()).submitPanel();
    }

    public void addDrilling(Pair<List<String>, String> pairs) {
        addDrilling(pairs, "Attributes");
    }

    public void editDrilling(Pair<List<String>, String> oldDrilling,
            Pair<List<String>, String> newDrilling, String group) {
        SelectItemPopupPanel popupPanel = null;
        String btnSelector = null;

        if (!oldDrilling.getLeft().equals(newDrilling.getLeft())) {
            btnSelector = ".s-btn";
            for (String item: oldDrilling.getLeft()) {
                btnSelector = btnSelector + "-" + CssUtils.simplifyText(item);
            }

            waitForElementVisible(By.cssSelector(btnSelector), browser).click();
            popupPanel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                    browser.findElements(SelectItemPopupPanel.LOCATOR).get(1));
            waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser);

            for (String item : newDrilling.getLeft()) {
                popupPanel.searchAndSelectItem(item).submitPanel();
            }
        }

        if (!oldDrilling.getRight().equals(newDrilling.getRight())) {
            btnSelector = ".s-btn" + "-" + CssUtils.simplifyText(oldDrilling.getRight());
            waitForElementVisible(By.cssSelector(btnSelector), browser).click();
            if (popupPanel == null) {
                popupPanel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                        browser.findElements(SelectItemPopupPanel.LOCATOR).get(1));
            } else {
                waitForElementVisible(popupPanel.getRoot());
            }
            popupPanel.changeGroup(group);
            popupPanel.searchAndSelectItem(newDrilling.getRight()).submitPanel();
        }
    }

    public void deleteDrilling(List<String> drillSourceName) {
        String btnSelector = ".s-btn";
        for (String item: drillSourceName) {
            btnSelector = btnSelector + "-" + CssUtils.simplifyText(item);
        }
        waitForElementVisible(By.cssSelector(btnSelector), browser).findElement(BY_PARENT)
            .findElement(By.className("deleteButton")).click();
    }

    private boolean isAddDrillingButtonVisible() {
        try {
            return drillingButton.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
