package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
/**
 * Kpi - key performance indicator widget
 */
public class Kpi extends AbstractFragment {
    // TODO: when having more widget types, separate, keep "Add widget" in mind
    public static final String MAIN_CLASS = "dash-item";
    public static final String KPI_CSS_SELECTOR = "." + MAIN_CLASS + ":not(.is-placeholder)";
    public static final String KPI_POP_SECTION_CLASS = "kpi-pop-section";
    public static final String KPI_ALERT_BUTTON_CLASS = "dash-item-action-alert";
    public static final String KPI_HAS_SET_ALERT_BUTTON = "has-set-alert";
    public static final String KPI_IS_EMPTY_VALUE = "is-empty-value";
    public static final String KPI_IS_ERROR_VALUE = "is-error-value";
    public static final String KPI_ALERT_DIALOG_CLASS = "kpi-alert-dialog";

    public static final String WIDGET_LOADING_CLASS = "widget-loading";
    public static final String CONTENT_LOADING_CLASS = "content-loading";

    public static final By IS_WIDGET_LOADING = By.cssSelector("." + MAIN_CLASS + " ." + WIDGET_LOADING_CLASS);
    public static final By IS_CONTENT_LOADING = By.cssSelector("." + MAIN_CLASS + " ." + CONTENT_LOADING_CLASS);
    public static final By IS_NOT_EDITABLE = By.cssSelector("." + MAIN_CLASS + " .kpi:not(.is-editable)");
    public static final By ALERT_DIALOG = By.className(KPI_ALERT_DIALOG_CLASS);

    @FindBy(css = ".dash-item-action-delete")
    protected WebElement deleteButton;

    @FindBy(className = KPI_ALERT_BUTTON_CLASS)
    private WebElement alertButton;

    @FindBy(css = ".kpi-headline > h3")
    private WebElement headline;

    @FindBy(css = ".kpi-headline > h3 .inplaceedit")
    private WebElement headlineInplaceEdit;

    @FindBy(css = ".kpi-headline > h3 textarea")
    private WebElement headlineTextarea;

    @FindBy(css = ".kpi-value")
    private WebElement value;

    @FindBy(className = KPI_POP_SECTION_CLASS)
    private KpiPopSection popSection;

    @FindBy(className = CONTENT_LOADING_CLASS)
    private WebElement contentLoading;

    public String getHeadline() {
        return waitForElementVisible(headline).getText();
    }

    public void clearHeadline() {
        waitForElementVisible(headlineInplaceEdit).click();

        // hit backspace multiple times, because .clear()
        // event does not trigger onchange event
        // https://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebElement.html#clear%28%29
        waitForElementVisible(headlineTextarea);
        int headlineLength = headlineInplaceEdit.getText().length();
        for (int i = 0; i < headlineLength; i++) {
            headlineTextarea.sendKeys(Keys.BACK_SPACE);
        }
    }

    public void setHeadline(String newHeadline) {
        clearHeadline();
        headlineTextarea.sendKeys(newHeadline);
        headlineTextarea.sendKeys(Keys.ENTER);

        waitForElementVisible(headlineInplaceEdit);
    }

    public String getValue() {
        return waitForElementPresent(value).getText();
    }

    public String getTooltipOfValue() {
        return waitForElementPresent(value).getAttribute("title");
    }

    public Kpi clickKpiValue() {
        waitForElementPresent(value).click();

        return this;
    }

    public boolean hasPopSection() {
        By thisMetric = By.className(KPI_POP_SECTION_CLASS);

        return isElementPresent(thisMetric, root);
    }

    public KpiPopSection getPopSection() {
        return waitForFragmentVisible(popSection);
    }

    public void clickKpiDeleteButton() {
        waitForElementVisible(deleteButton).click();
    }

    public Kpi waitForAlertButtonVisible() {
        waitForElementVisible(alertButton);

        return this;
    }

    public Kpi waitForAlertButtonNotVisible() {
        waitForElementNotVisible(alertButton);

        return this;
    }

    public boolean hasAlertDialogOpen() {
        return isElementPresent(ALERT_DIALOG, browser);
    }

    public boolean hasSetAlert() {
        return isElementPresent(By.className(KPI_HAS_SET_ALERT_BUTTON), this.getRoot());
    }

    public boolean isEmptyValue() {
        return isElementPresent(By.className(KPI_IS_EMPTY_VALUE), this.getRoot());
    }

    public boolean isErrorValue() {
        return isElementPresent(By.className(KPI_IS_ERROR_VALUE), this.getRoot());
    }

    public KpiAlertDialog openAlertDialog() {
        if (!hasAlertDialogOpen()) {
            hoverAndClickKpiAlertButton();
        }

        return Graphene.createPageFragment(KpiAlertDialog.class,
                waitForElementVisible(KpiAlertDialog.LOCATOR, browser));
    }

    public Kpi hoverAndClickKpiAlertButton() {
        Actions action = new Actions(browser);
        action.moveToElement(value).moveToElement(alertButton).click().build().perform();

        return this;
    }

    public void waitForLoading() {
        waitForElementVisible(contentLoading);
    }

    public enum ComparisonType {
        NO_COMPARISON("none", "No comparison"),
        LAST_YEAR("lastYear", "Same period in previous year"),
        PREVIOUS_PERIOD("previousPeriod", "Previous period");

        private String jsonKey;
        private String uiText;

        private ComparisonType(String jsonKey, String uiText) {
            this.jsonKey = jsonKey;
            this.uiText = uiText;
        }

        public String getJsonKey() {
            return jsonKey;
        }

        @Override
        public String toString() {
            return uiText;
        }
    }

    public enum ComparisonDirection {
        NONE,
        GOOD("growIsGood"),
        BAD("growIsBad");

        private String text;

        private ComparisonDirection(String text) {
            this.text = text;
        }

        private ComparisonDirection() {
            text = "";
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
