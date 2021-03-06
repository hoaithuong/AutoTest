package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ZoomMenu extends AbstractFragment {
    private static final By ZOOM = By.className("gdc-ldm-zoom");

    @FindBy(className = "zoom-in-button")
    private WebElement zoomInBtn;

    @FindBy(className = "zoom-out-button")
    private WebElement zoomOutBtn;

    @FindBy(className = "zoom-value-button")
    private WebElement zoomValueBtn;

    public static final ZoomMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ZoomMenu.class, waitForElementVisible(ZOOM, searchContext));
    }

    public String getCurrentZoomValue() {
        return zoomValueBtn.getText();
    }

    public void clickZoomInBtn() {
        waitForElementVisible(zoomInBtn).click();
    }

    public void clickZoomOutBtn() {
        waitForElementVisible(zoomOutBtn).click();
    }

    public SelectZoomValue clickZoomValueBtn() {
        waitForElementVisible(zoomValueBtn).click();
        return SelectZoomValue.getInstance(browser);
    }

    public static class SelectZoomValue extends AbstractFragment {

        private static final By ZOOM_POPUP_MENU = By.className("zoom-popup-menu");

        @FindBy(className = "gd-menu-item")
        private List<WebElement> items;
        
        public static SelectZoomValue getInstance(SearchContext searchContext) {
            return Graphene.createPageFragment(SelectZoomValue.class,
                    waitForElementVisible(ZOOM_POPUP_MENU, searchContext));
        }

        public List<String> getZoomValueList() {
            waitForElementVisible(ZOOM_POPUP_MENU, browser);
            return items.stream().map(el -> el.getText()).collect(Collectors.toList());
        }

        public SelectZoomValue selectZoomValue(String zoomValue) {
            items.stream().filter(e -> zoomValue.equals(e.getText())).findFirst().get().click();
            return this;
        }
    }
}
