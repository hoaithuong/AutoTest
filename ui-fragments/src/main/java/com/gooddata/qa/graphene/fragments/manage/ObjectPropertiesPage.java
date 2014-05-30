package com.gooddata.qa.graphene.fragments.manage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ObjectPropertiesPage extends AbstractFragment {
    @FindBy(css = ".s-name")
    private WebElement objectNameIpe;

    @FindBy(xpath = "//div[contains(@class,'s-name-ipe-editor')]//input[@class = 'ipeEditor']")
    protected WebElement objectNameInput;

    @FindBy(xpath = "//div[contains(@class,'s-name-ipe-editor')]//button[text() = 'Save']")
    private WebElement objectNameSave;

    @FindBy(css = ".s-description-ipe-placeholder")
    private WebElement descriptionIpePlaceholder;

    @FindBy(css = ".s-description")
    private WebElement descriptionIpe;

    @FindBy(xpath = "//div[contains(@class,'s-description-ipe-editor')]//input[@class = 'ipeEditor']")
    private WebElement descriptionInput;

    @FindBy(xpath = "//div[contains(@class,'s-description-ipe-editor')]//button[text() = 'Save']")
    private WebElement descriptionSave;

    @FindBy(xpath = "//span[text() = 'Add Tags']")
    private WebElement addTagButton;

    @FindBy(xpath = "//div[contains(@class,'s-btn-ipe-editor')]//input[@class = 'ipeEditor']")
    private WebElement tagInput;

    @FindBy(xpath = "//div[contains(@class,'s-btn-ipe-editor')]//button[text() = 'Add']")
    private WebElement tagAddButton;

    @FindBy(xpath = "//div[@class = 'tag']")
    private List<WebElement> tagList;

    @FindBy(xpath = "//button[contains(@class, 's-btn-change_folder')]")
    private WebElement changeFolderButton;

    @FindBy(xpath = "//span[contains(@class,'loadingWheel') and not(contains(@class,'hidden'))]")
    private WebElement loadingWheelFolder;

    @FindBy(xpath = "//p[@class = 'folderText']/a")
    private WebElement locatedInFolder;

    private final String folderLocator = "//div[@class = 'autocompletion']/div[@class = 'suggestions']/ul/li[text() = '${folder}']";

    public void changeObjectFolder(String newFolderName) {
	waitForElementVisible(changeFolderButton).click();
	By objectFolder = By.xpath(folderLocator.replace("${folder}",
		newFolderName));
	waitForElementVisible(objectFolder).click();
	waitForElementVisible(loadingWheelFolder);
	waitForElementNotPresent(loadingWheelFolder);
	assertEquals(locatedInFolder.getText(), newFolderName,
		"Change folder doesn't work properly");
    }

    public String changeObjectName(String newObjectName) {
	waitForElementVisible(objectNameIpe).click();
	waitForElementVisible(objectNameInput).clear();
	objectNameInput.sendKeys(newObjectName);
	waitForElementVisible(objectNameSave).click();
	waitForElementNotVisible(objectNameInput);
	assertEquals(objectNameIpe.getText(), newObjectName,
		"Change name doesn't work properly");
	return newObjectName;
    }

    public void addDescription(String description) {
	waitForElementVisible(descriptionIpePlaceholder).click();
	waitForElementVisible(descriptionInput).sendKeys(description);
	waitForElementVisible(descriptionSave).click();
	waitForElementNotVisible(descriptionInput);
	assertEquals(descriptionIpe.getText(), description,
		"Add description doesn't work properly");
    }

    public void addTag(String tagName) {
	int tagCountBefore = tagList.size();
	waitForElementVisible(addTagButton).click();
	waitForElementVisible(tagInput).sendKeys(tagName);
	waitForElementVisible(tagAddButton).click();
	waitForElementNotVisible(tagInput);
	int tagWords = 1;
	for (int i = 0; i < tagName.trim().length(); i++) {
	    if (tagName.charAt(i) == ' ' && tagName.charAt(i + 1) != ' ') {
		tagWords++;
	    }
	}
	int tagCountAfter = tagList.size();
	assertEquals(tagCountAfter, tagCountBefore + tagWords,
		"Add tag doesn't work properly");
	verifyTagElements(tagName);
    }

    public void verifyTagElements(String tagName){
	String[] tagNameList = tagName.split("\\s+");
	boolean tagVisible = false;
	int matchingTag = 0;
	for (int i = 0; i < tagNameList.length; i++) {
	    for (WebElement elem : tagList) {
		if (waitForElementVisible(elem).getAttribute("title")
			.equalsIgnoreCase(tagNameList[i])) {
		    matchingTag++;
		}
	    }
	    if (matchingTag == tagNameList.length) {
		tagVisible = true;
	    }
	}
	assertTrue(tagVisible, "Add tag doesn't work properly");
    }
    
    public void verifyAllPropertiesAtOnce (String newObjectName, String description, String tagName){
	verifyTagElements(tagName);
	assertEquals(waitForElementVisible(objectNameIpe).getText(), newObjectName,
		"Change name doesn't work properly");
	assertEquals(waitForElementVisible(descriptionIpe).getText(), description,
		"Add description doesn't work properly");
    }
}
