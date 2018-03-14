package com.gooddata.qa.graphene.utils;

import com.gooddata.qa.utils.browser.BrowserUtils;
import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

public final class ElementUtils {

    public static final By BY_BUBBLE_CONTENT = By.cssSelector(".bubble-content .content");

    private ElementUtils() {
    }

    /**
     * Check if element is currently present in DOM
     * @see <a href="http://stackoverflow.com/questions/7991522/selenium-webdriver-test-if-element-is-present">SO: selenium-webdriver-test-if-element-is-present</a>
     * @param locatorKey By element for location
     * @param context context to search
     * @return
     */
    public static boolean isElementPresent(By locatorKey, SearchContext context) {
        return context.findElements(locatorKey).size() > 0;
    }

    public static boolean isElementVisible(By locatorKey, SearchContext context) {
        if (!isElementPresent(locatorKey, context)) return false;

        return context.findElement(locatorKey).isDisplayed();
    }

    public static boolean isElementVisible(WebElement element) {
        return element.isDisplayed();
    }

    public static void scrollElementIntoView(WebElement element, WebDriver browser) {
        BrowserUtils.runScript(browser, "arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Get texts of all elements matching given By selector under specified context
     * @param selector selector to match items
     * @param context search context
     * @return array of strings with element texts
     */
    public static List<String> getElementTexts(By selector, SearchContext context) {
        return getElementTexts(context.findElements(selector));
    }

    public static List<String> getElementTexts(Collection<WebElement> elements) {
        return elements.stream()
            .map(WebElement::getText)
            .collect(toList());
    }

    public static List<String> getElementTexts(Collection<WebElement> elements, Function<WebElement, WebElement> func) {
        return elements.stream()
            .map(func::apply)
            .map(WebElement::getText)
            .collect(toList());
    }

    public static String getTooltipFromElement(By locator, WebDriver browser) {
        return getTooltipFromElement(waitForElementVisible(locator, browser), browser);
    }

    public static String getTooltipFromElement(WebElement element, WebDriver browser) {
        makeSureNoPopupVisible();
        new Actions(browser).moveToElement(element).perform();

        return getBubbleMessage(browser);
    }

    public static String getBubbleMessage(WebDriver browser) {
        return waitForElementVisible(BY_BUBBLE_CONTENT, browser).getText();
    }

    public static void clickElementByVisibleLocator(SearchContext searchContext, By... bySelectors) {
        Function<WebDriver, Boolean> visibilityOfElement = browser ->
                Stream.of(bySelectors).anyMatch(by -> isElementVisible(by, searchContext));
        Graphene.waitGui().until(visibilityOfElement);

        Stream.of(bySelectors)
                .filter(by -> isElementVisible(by, searchContext))
                .map(by -> waitForElementVisible(by, searchContext))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible element found"))
                .click();
    }

    public static void makeSureNoPopupVisible() {
        makeSureNoPopupVisible(BY_BUBBLE_CONTENT);
    }

    public static void makeSureNoPopupVisible(By popupElement) {
        WebDriver browser = BrowserUtils.getBrowserContext();

        // Move outside HTML body at position (-1, -1) to make sure no popup displayed
        new Actions(browser).moveToElement(browser.findElement(By.tagName("body")), -1, -1).perform();

        Function<WebDriver, Boolean> isDismissed = context -> !isElementVisible(popupElement, context);
        Graphene.waitGui().until(isDismissed);
    }
}
