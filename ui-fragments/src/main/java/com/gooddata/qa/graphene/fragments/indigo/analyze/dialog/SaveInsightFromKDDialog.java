package com.gooddata.qa.graphene.fragments.indigo.analyze.dialog;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.cssSelector;

public class SaveInsightFromKDDialog extends AbstractFragment {

    @FindBy(className = "gd-input-field")
    private WebElement nameTextBox;

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitButton;

//    @FindBy(css = ".input-radio-label input:not([value*='update'])")
    @FindBy(css = ".gd-dialog-content div:last-child")
    private WebElement createCopyOption;

    public static final String ROOT_SELECTOR = ".s-save-report-from-kd-dialog";

    public static SaveInsightFromKDDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveInsightFromKDDialog.class,
                waitForElementVisible(cssSelector(ROOT_SELECTOR), searchContext));
    }

    public boolean isSaveInsightDialogDisplay() {
        return isElementVisible(cssSelector(ROOT_SELECTOR), browser);
    }

    public void saveInsight() {
        waitForElementVisible(submitButton).click();
        waitForElementNotVisible(submitButton);
    }

    public void createCopy(final String name) {
        waitForElementVisible(createCopyOption).click();
        enterName(name).clickSubmitButton();
        waitForFragmentNotVisible(this);
    }

    public void clickSubmitButton() {
        waitForElementEnabled(submitButton).click();
        waitForElementNotVisible(submitButton);
    }

    public SaveInsightFromKDDialog enterName(final String name) {
        waitForElementVisible(nameTextBox).clear();
        nameTextBox.sendKeys(name);
        return this;
    }

    public String getName() {
        return waitForElementVisible(nameTextBox).getText();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(cancelButton);
    }

    public boolean isSubmitButtonDisabled() {
        return submitButton.getAttribute("class").contains("disabled");
    }

}
