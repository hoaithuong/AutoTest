package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AnalysisPageHeader extends AbstractFragment {

    @FindBy(css = ".s-btn-reset")
    private WebElement resetButton;

    @FindBy(css = ".s-btn-open_as_report")
    private WebElement exportToReportButton;

    @FindBy(css = ".s-undo")
    private WebElement undoButton;

    @FindBy(css = ".s-redo")
    private WebElement redoButton;

    private static final String DISABLED = "disabled";

    public void resetToBlankState() {
        waitForElementVisible(resetButton).click();
    }

    public void exportReport() {
        waitForElementVisible(exportToReportButton).click();
    }

    public boolean isExportToReportButtonEnable() {
        return !waitForElementVisible(exportToReportButton).getAttribute("class").contains(DISABLED);
    }

    public void undo() {
        waitForElementVisible(undoButton).click();
    }

    public void redo() {
        waitForElementVisible(redoButton).click();
    }

    public boolean isUndoButtonEnabled() {
        return !waitForElementVisible(undoButton).getAttribute("class").contains(DISABLED);
    }

    public boolean isRedoButtonEnabled() {
        return !waitForElementVisible(redoButton).getAttribute("class").contains(DISABLED);
    }
}
