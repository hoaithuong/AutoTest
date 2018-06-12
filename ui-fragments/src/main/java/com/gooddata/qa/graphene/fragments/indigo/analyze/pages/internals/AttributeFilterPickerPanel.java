package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import java.util.List;
import java.util.stream.Stream;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.common.AbstractPicker;

public class AttributeFilterPickerPanel extends AbstractPicker {

    @FindBy(xpath = "//label[@class='input-checkbox-label' and input[contains(@class,'gd-checkbox-selection')]]")
    private WebElement selectAllLabel;

    @FindBy(css = ".gd-checkbox-selection")
    private WebElement selectAllCheckbox;
    
    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-apply")
    private WebElement applyButton;

    private static final By CLEAR_SEARCH_TEXT_SHORTCUT = className("gd-input-icon-clear");

    public static AttributeFilterPickerPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(AttributeFilterPickerPanel.class,
                waitForElementVisible(className("adi-attr-filter-picker"), context));
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".s-filter-item";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return ".gd-input-search input";
    }

    @Override
    protected void waitForPickerLoaded() {
        waitForElementNotPresent(cssSelector(".filter-items-loading"));
    }

    @Override
    protected void clearSearchText() {
        if (isElementPresent(CLEAR_SEARCH_TEXT_SHORTCUT, getRoot())) {
            waitForElementVisible(CLEAR_SEARCH_TEXT_SHORTCUT, getRoot()).click();
            return;
        }

        super.clearSearchText();
    }

    public void select(String... values) {
        waitForPickerLoaded();
        if (values.length == 1 && "All".equals(values[0])) {
            selectAll();
            return;
        }

        uncheckAllCheckbox();
        Stream.of(values).forEach(this::selectItem);
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void selectAll() {
        checkAllCheckbox();
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void checkAllCheckbox() {
        waitForElementVisible(selectAllLabel);
        if (selectAllCheckbox.getAttribute("class").contains("checkbox-indefinite")) {
            selectAllCheckbox.click(); // this will select all
        }

        if (!selectAllCheckbox.isSelected()) {
            selectAllCheckbox.click();
        }
    }

    public void uncheckAllCheckbox() {
        waitForElementVisible(selectAllLabel);
        if (selectAllCheckbox.getAttribute("class").contains("checkbox-indefinite")) {
            selectAllCheckbox.click(); // this will select all
        }

        if (selectAllCheckbox.isSelected()) {
            selectAllCheckbox.click();
        }
    }

    public AttributeFilterPickerPanel selectItem(String item) {
        searchForText(item);
        getElement(format("[title='%s']", item))
            .click();
        return this;
    }

    public String getId(final String item) {
        return Stream.of(getElement(format("[title='%s']", item))
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];
    }

    public void discard() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void assertPanel() {
        waitForElementVisible(selectAllLabel);
        waitForElementVisible(applyButton);
        waitForElementVisible(cancelButton);
    }

    public List<String> getItemNames() {
        return getElementTexts(getElements(), e -> e.findElement(tagName("span")));
    }

    public WebElement getApplyButton() {
        return waitForElementVisible(applyButton);
    }
}
