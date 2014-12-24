package com.gooddata.qa.graphene.fragments.disc;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class DeployForm extends AbstractFragment {

    private static final By BY_FILE_INPUT_ERROR = By
            .cssSelector(".select-zip .zip-name-text input");
    private static final By BY_INPUT_ERROR_BUBBLE = By.cssSelector(".bubble-overlay");

    private static final String XPATH_PROCESS_TYPE_OPTION =
            "//select/option[@value='${processType}']";

    @FindBy(css = "div.deploy-process-dialog-area")
    private WebElement deployProcessDialog;

    @FindByJQuery("button:contains(Deployed)")
    private WebElement deployedButton;

    @FindByJQuery("button:contains(Re-deployed)")
    private WebElement redeployedButton;

    @FindBy(xpath = "//div[@class='select-zip']/div/input")
    private WebElement zipFileInput;

    @FindBy(
            xpath = "//div[@class='deploy-process-dialog-area']/div[contains(@class, 'whole-line-text')]/div[contains(@class, 'bubble-overlay')]")
    private WebElement processNameErrorBubble;

    @FindBy(
            xpath = "//div[@class='deploy-process-dialog-area']/div[contains(@class, 'whole-line-text')]/input")
    private WebElement processNameInput;

    @FindBy(css = "div.deploy-process-button-area button.button-positive")
    private WebElement deployConfirmButton;

    public void deployProcess(String zipFile, ProcessTypes processType, String processName) {
        tryToDeployProcess(zipFile, processType, processName);
        waitForElementVisible(deployedButton);
        System.out.println("Deploy progress is finished!");
        waitForElementNotPresent(getRoot());
    }

    public void tryToDeployProcess(String zipFile, ProcessTypes processType, String processName) {
        waitForElementVisible(getRoot());
        setDeployProcessInput(zipFile, processType, processName);
        assertFalse(inputFileHasError());
        assertFalse(inputProcessNameHasError());
        waitForElementVisible(deployConfirmButton).click();
    }

    public void redeployProcess(String zipFile, ProcessTypes processType, String processName) {
        tryToDeployProcess(zipFile, processType, processName);
        waitForElementVisible(redeployedButton);
        System.out.println("Re-deploy progress is finished!");
    }

    public void assertErrorOnDeployForm(String zipFilePath, ProcessTypes processType,
            String processName) {
        setDeployProcessInput(zipFilePath, processType, processName);
        getDeployConfirmButton().click();
        if (zipFilePath.isEmpty()) {
            waitForElementVisible(getFileInputErrorBubble());
            assertTrue(inputFileHasError());
            assertEquals(getFileInputErrorBubble().getText(),
                    "A zip file is required. The file must be smaller than 1MB.");
        }
        if (processName.isEmpty()) {
            assertTrue(inputProcessNameHasError());
            getProcessName().click();
            assertEquals(getProcessNameErrorBubble().getText(), "A process name is required");
        }
    }

    public WebElement getDeployProcessDialog() {
        return waitForElementVisible(deployProcessDialog);
    }

    public void setDeployProcessInput(String zipFilePath, ProcessTypes processType,
            String processName) {
        setZipFile(zipFilePath);
        setProcessType(processType);
        setProcessName(processName);
    }

    public boolean inputFileHasError() {
        return waitForElementVisible(deployProcessDialog).findElement(BY_FILE_INPUT_ERROR)
                .getAttribute("class").contains("has-error");
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
            deployProcessDialog
                    .findElement(
                            By.xpath(XPATH_PROCESS_TYPE_OPTION.replace("${processType}",
                                    processType.name()))).click();
    }

    private void setProcessName(String processName) {
        processNameInput.clear();
        processNameInput.sendKeys(processName);
    }

    private WebElement getProcessName() {
        return waitForElementVisible(processNameInput);
    }

    private WebElement getFileInputErrorBubble() {
        return waitForElementVisible(deployProcessDialog).findElement(BY_INPUT_ERROR_BUBBLE);
    }

    private WebElement getProcessNameErrorBubble() {
        return waitForElementVisible(processNameErrorBubble);
    }
}
