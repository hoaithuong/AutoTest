package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ContentDatasourceContainer extends AbstractFragment {
    public static final String CONTENT_CLASS = "create-or-edit-connection-container";
    public final String VALUE_PARAM = "generic-datasource-info-row-%s-value";
    public final String PARAMETER_ROW = "generic-datasource-info-row-%s-key";

    @FindBy(className = "s-save")
    private WebElement saveButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "report-title")
    private WebElement connectionTitle;

    @FindBy(className = "datasource-heading-container-id-value")
    private WebElement dataSourceId;

    @FindBy(className = "datasource-heading-name")
    private WebElement dataSourceName;

    @FindBy(className = "alias-input")
    private WebElement dataSourceAlias;

    @FindBy(className = "generic-data-source-alias-value")
    private WebElement aliasValue;

    @FindBy(className = "provider-label-value")
    private WebElement datasourceType;

    @FindBy(className = "generic-parameter-info")
    private List<WebElement> parameterInfo;

    @FindBy(className = "s-copy_reference")
    private WebElement copyReferenceBtn;

    @FindBy(className = "s3-datasource-info-row-bucket-value")
    private WebElement bucketValue;

    @FindBy(className = "s3-datasource-info-row-accessKey-value")
    private WebElement accessKeyValue;

    @FindBy(className = "s3-datasource-info-row-secretKey-value")
    private WebElement secretKeyValue;

    @FindBy(className = "s3-datasource-info-row-serverSideEncryption-value")
    private WebElement encryptionValue;

    @FindBy(className = "s3-datasource-info-row-region-value")
    private WebElement regionValue;

    @FindBy(className = "alias-required-message")
    private WebElement aliasMessage;


    public static final ContentDatasourceContainer getInstance(SearchContext context) {
        return Graphene.createPageFragment(ContentDatasourceContainer.class, waitForElementVisible(className(CONTENT_CLASS), context));
    }

    public ConnectionDetail getConnectionDetail() {
        return ConnectionDetail.getInstance(browser);
    }

    public ConnectionConfiguration getConnectionConfiguration() {
        return ConnectionConfiguration.getInstance(browser);
    }

    public S3Configuration getS3ConnectionConfiguration() {
        return S3Configuration.getInstance(browser);
    }

    public DatasourceHeading getDatasourceHeading() {
        return DatasourceHeading.getInstance(browser);
    }

    public void addConnectionTitle(String title) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(connectionTitle).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                .sendKeys(Keys.DELETE).sendKeys(title).build().perform();
    }

    public void addAliasTitle(String aliasTitle) {
        dataSourceAlias.clear();
        dataSourceAlias.sendKeys(aliasTitle);
    }

    public void clickSavebutton() {
        waitForElementVisible(saveButton).click();
    }

    public void clickCancelButton () {
        waitForElementVisible(cancelButton).click();
    }

    public String getDataSourceId() {
        return waitForElementVisible(dataSourceId).getText();
    }

    public String getDataSourceName() {
        return waitForElementVisible(dataSourceName).getText();
    }

    public String getDataSourceAlias() {
        return waitForElementVisible(aliasValue).getText();
    }

    public String getDatasourceType() {
        return waitForElementVisible(datasourceType).getText();
    }

    public String getCreatedKeyParamValue(String key) {
        return browser.findElement(className(String.format(PARAMETER_ROW, key))).getText();
    }

    public String getCreatedValueParamValue(String key) {
        return browser.findElement(className(String.format(VALUE_PARAM, key))).getText();
    }

    public WebElement hoverOnParameterRow(String keyParameter) {
        WebElement keyParamElement = parameterInfo.stream().filter(el -> el.getText().contains(keyParameter)).findFirst().get();
        getActions().moveToElement(keyParamElement).pause(1000).click().build().perform();
        return keyParamElement;
    }

    public String getCopyReferenceParam(String keyParameter) {
        try {
            hoverOnParameterRow(keyParameter).findElement(By.className("s-copy_reference")).click();
        } catch (Exception elCannotClick) {
            log.info("--- Cannot click on copy reference button, need to re-click again ---");
            sleepTightInSeconds(3);
            hoverOnParameterRow(keyParameter).findElement(By.className("s-copy_reference")).click();
        }
        return waitForElementVisible(By.className("content"), browser).getText();
    }

    public String getBucketValue() {
        return waitForElementVisible(bucketValue).getText();
    }

    public String getAccessKeyValue() {
        return waitForElementVisible(accessKeyValue).getText();
    }

    public String getSecretKeyValue() {
        return waitForElementVisible(secretKeyValue).getText();
    }

    public String getEncryptionValue() {
        return waitForElementVisible(encryptionValue).getText();
    }

    public String getRegionValue() {
        return waitForElementVisible(regionValue).getText();
    }

    public boolean isAliasErrorMessageDisplay() {
        return isElementVisible(aliasMessage);
    }

    public String getAliasErrorMessage() {
        return waitForElementVisible(aliasMessage).getText();
    }
}
