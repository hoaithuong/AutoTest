package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

/**
 * resetButton now has s-clear css class
 */
public class AnalysisPageHeaderReact extends AbstractFragment {

    @FindBy(className = "s-clear")
    private WebElement resetButton;

    @FindBy(className = "s-export-to-report")
    private WebElement exportToReportButton;

    @FindBy(className = "s-undo")
    private WebElement undoButton;

    @FindBy(className = "s-redo")
    private WebElement redoButton;

    public void resetToBlankState() {
        waitForElementVisible(resetButton).click();
    }

    public void exportReport() {
        waitForElementVisible(exportToReportButton).click();
    }

    public boolean isExportButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(exportToReportButton));
    }

    public void undo() {
        waitForElementVisible(undoButton).click();
    }

    public void redo() {
        waitForElementVisible(redoButton).click();
    }

    public boolean isUndoButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(undoButton));
    }

    public boolean isRedoButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(redoButton));
    }

    public boolean isResetButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(resetButton));
    }

    public WebElement getResetButton() {
        return waitForElementVisible(resetButton);
    }

    public String getExportButtonTooltipText() {
        getActions().moveToElement(exportToReportButton).perform();
        return waitForElementVisible(cssSelector(".gd-bubble .content"), browser).getText().trim();
    }

    private boolean isElementDisabled(WebElement element) {
        return element.getAttribute("class").contains("disabled");
    }
}
