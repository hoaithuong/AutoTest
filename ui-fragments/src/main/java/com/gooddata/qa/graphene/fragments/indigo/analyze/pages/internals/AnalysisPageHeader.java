package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import java.util.function.Function;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;

/**
 * resetButton now has s-clear css class
 */
public class AnalysisPageHeader extends AbstractFragment {

    @FindBy(className = "s-clear")
    private WebElement resetButton;

    @FindBy(className = EXPORT_BUTTON_CLASS)
    private WebElement exportToReportButton;

    @FindBy(className = OPTIONS_BUTTON_CLASS)
    private WebElement optionsButton;

    @FindBy(className = "s-undo")
    private WebElement undoButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-redo")
    private WebElement redoButton;

    @FindBy(className = "s-open")
    private WebElement openButton;

    @FindBy(className = "s-save")
    private WebElement saveButton;

    @FindBy(className = SAVE_AS_CLASS)
    private WebElement saveAsButton;

    @FindBy(className = "s-report-title")
    private WebElement insightTitle;

    private static final String SAVE_AS_CLASS= "s-save_as_new";
    private static final String EXPORT_BUTTON_CLASS= "s-export-to-report";
    private static final String OPTIONS_BUTTON_CLASS= "s-options-button";

    public void resetToBlankState() {
        waitForElementVisible(resetButton).click();
    }

    public void exportReport() {
        final int numberOfWindows = browser.getWindowHandles().size();
        waitForElementVisible(exportToReportButton).click();

        //make sure the new window is displayed to prevent unexpected errors
        Function<WebDriver, Boolean> hasNewWindow = browser -> browser.getWindowHandles().size() == numberOfWindows + 1;
        Graphene.waitGui().until(hasNewWindow);
    }

    public OptionalExportMenu clickOptionsButton() {
        waitForElementVisible(optionsButton).click();
        return Graphene.createPageFragment(OptionalExportMenu.class,
            waitForElementVisible(By.className("s-gd-export-menu-overlay"), browser));
    }

    public boolean isExportButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(exportToReportButton));
    }

    public boolean isExportButtonDisabled() {
        return isElementDisabled(waitForElementVisible(exportToReportButton));
    }

    public boolean isExportButtonPresent() {
        return isElementPresent(className(EXPORT_BUTTON_CLASS), getRoot());
    }

    public AnalysisPageHeader undo() {
        waitForElementVisible(undoButton).click();
        return this;
    }

    public AnalysisPageHeader redo() {
        waitForElementVisible(redoButton).click();
        return this;
    }

    public AnalysisPageHeader cancel() {
        waitForElementVisible(cancelButton).click();
        return this;
    }

    public boolean isUndoButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(undoButton));
    }

    public boolean isRedoButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(redoButton));
    }

    public boolean isRedoButtonDisabled() {
        return isElementDisabled(waitForElementVisible(redoButton));
    }

    public boolean isCancelButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(cancelButton));
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

    public boolean isSaveButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(saveButton));
    }

    public boolean isSaveAsButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(saveAsButton));
    }

    public boolean isOpenButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(openButton));
    }

    public boolean isSaveAsPresent() {
        return isElementPresent(className(SAVE_AS_CLASS), browser);
    }

    public AnalysisInsightSelectionPanel getInsightSelectionPanel() {
        return AnalysisInsightSelectionPanel.getInstance(browser);
    }

    public AnalysisInsightSelectionPanel expandInsightSelection() {
        if (!isInsightSelectionExpanded()) {
            openButton.click();
        }

        return getInsightSelectionPanel().waitForLoading();
    }

    public AnalysisPageHeader setInsightTitle(final String title) {
        waitForElementVisible(insightTitle).click();
        final WebElement textArea = waitForElementVisible(insightTitle.findElement(tagName("textarea")));
        textArea.sendKeys(title, Keys.ENTER); //make sure the title is applied

        if (!title.trim().isEmpty()) {
            Function<WebDriver, Boolean> savedButtonEnabled = driver -> isSaveButtonEnabled();
            Graphene.waitGui().until(savedButtonEnabled);
        }

        return this;
    }

    public String getInsightTitle() {
        return waitForElementVisible(insightTitle).getText();
    }

    public boolean isInsightTitleEditable() {
        waitForElementVisible(insightTitle).click();
        return isElementPresent(tagName("textarea"), insightTitle);
    }

    public void saveInsight() {
        waitForElementEnabled(saveButton).click();
    }

    public void saveInsight(final String insight) {
        waitForElementEnabled(saveButton).click();
        saveWorkingInsight(insight);
    }

    public SaveInsightDialog saveWithoutSubmitting(final String insight) {
        waitForElementEnabled(saveButton).click();
        return SaveInsightDialog.getInstance(browser).enterName(insight);
    }

    public void saveInsightAs(final String insight) {
        waitForElementEnabled(saveAsButton).click();
        saveWorkingInsight(insight);
    }

    public boolean isUnsavedMessagePresent() {
        return isElementPresent(className("unsaved-notification"), getRoot());
    }

    public boolean isBlankState() {
        return isOpenButtonEnabled()
                && !isUnsavedMessagePresent()
                && !isSaveAsPresent()
//                && !isResetButtonEnabled() TODO: CL-9830 Clear button is not reset after clear saved insight
                && !isSaveButtonEnabled()
//                && !isExportButtonEnabled() TODO: SD-183: Export in Analytical Designer (AD)
                && getInsightTitle().equals("Untitled insight");
    }

    public void waitForOpenEnabled() {
        waitForElementEnabled(openButton);
    }

    public boolean hasLockedIcon() {
        waitForElementVisible(insightTitle);
        return isElementVisible(cssSelector(".report-title-wrapper .icon-lock"), browser);
    }

    private void saveWorkingInsight(final String insight) {
        SaveInsightDialog.getInstance(browser).save(insight);
    }

    private boolean isInsightSelectionExpanded() {
        return waitForElementVisible(openButton).getAttribute("class").contains("is-dropdown-open");
    }

    private boolean isElementDisabled(WebElement element) {
        return element.getAttribute("class").contains("disabled");
    }
}
