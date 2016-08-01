package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DeployForm extends AbstractFragment {

    private static final String INVALID_PROCESS_NAME_ERROR = "A process name is required";

    private static final String INVALID_PACKAGE_ERROR =
            "A zip file is required. The file must be smaller than 5 MB.";

    private static final By BY_FILE_INPUT_ERROR = By.cssSelector(".select-zip .zip-name-text input");
    private static final By BY_BUBBLE_ERROR = By.cssSelector("div.bubble-negative.isActive div.content");
    private static final String XPATH_PROCESS_TYPE_OPTION = "//select/option[@value='${processType}']";

    @FindBy(css = "div.deploy-process-dialog-area")
    private WebElement deployProcessDialog;

    @FindBy(css = "div.select-zip>div>input")
    private WebElement zipFileInput;

    @FindBy(css = ".deploy-process-dialog-area h3:last-of-type + * input")
    private WebElement processNameInput;

    @FindBy(css = "div.deploy-process-button-area button.button-positive")
    private WebElement deployConfirmButton;

    @FindBy(css = ".git-radio input")
    private WebElement gitChoice;

    @FindBy(css = "input[placeholder]")
    private WebElement gitInput;

    public void deployProcess(String gitRubyPath, String processName) {
        waitForElementVisible(getRoot());
        waitForElementVisible(gitChoice).click();
        setGitPath(gitRubyPath);
        setProcessName(processName);
        waitForElementVisible(deployConfirmButton).click();
        System.out.println("Deploy progress is finished!");
    }

    public void deployProcess(String zipFile, ProcessTypes processType, String processName) {
        tryToDeployProcess(zipFile, processType, processName);
        System.out.println("Deploy progress is finished!");
    }

    public void tryToDeployProcess(String zipFile, ProcessTypes processType, String processName) {
        waitForElementVisible(getRoot());
        setDeployProcessInput(zipFile, processType, processName);
        waitForElementVisible(deployConfirmButton).click();
    }

    public void redeployProcess(String zipFile, ProcessTypes processType, String processName) {
        tryToDeployProcess(zipFile, processType, processName);
        System.out.println("Re-deploy progress is finished!");
    }

    public boolean isCorrectInvalidPackageError() {
        return INVALID_PACKAGE_ERROR.equals(getErrorBubble().getText());
    }

    public boolean isCorrectInvalidProcessNameError() {
        getProcessName().click();
        return INVALID_PROCESS_NAME_ERROR.equals(getErrorBubble().getText());
    }

    public WebElement getDeployProcessDialog() {
        return waitForElementVisible(deployProcessDialog);
    }

    public void setDeployProcessInput(String zipFilePath, ProcessTypes processType, String processName) {
        setZipFile(zipFilePath);
        setProcessType(processType);
        setProcessName(processName);
    }

    public boolean inputFileHasError() {
        return waitForElementVisible(deployProcessDialog).findElement(BY_FILE_INPUT_ERROR).getAttribute("class")
                .contains("has-error");
    }

    public boolean isGitStoreError() {
        return waitForElementVisible(gitInput).getAttribute("class").contains("has-error");
    }

    public boolean inputProcessNameHasError() {
        return waitForElementVisible(processNameInput).getAttribute("class").contains("has-error");
    }

    public WebElement getDeployConfirmButton() {
        return waitForElementVisible(deployConfirmButton);
    }

    private void setZipFile(String zipFilePath) {
        zipFileInput.sendKeys(zipFilePath);
    }

    private void setProcessType(ProcessTypes processType) {
        if (processType != ProcessTypes.DEFAULT)
            deployProcessDialog.findElement(
                    By.xpath(XPATH_PROCESS_TYPE_OPTION.replace("${processType}", processType.name()))).click();
    }

    private void setProcessName(String processName) {
        processNameInput.clear();
        processNameInput.sendKeys(processName);
    }

    private void setGitPath(String gitPath) {
        waitForElementVisible(gitInput).clear();
        gitInput.sendKeys(gitPath);
    }

    private WebElement getProcessName() {
        return waitForElementVisible(processNameInput);
    }

    private WebElement getErrorBubble() {
        return waitForElementVisible(BY_BUBBLE_ERROR, browser);
    }

}
