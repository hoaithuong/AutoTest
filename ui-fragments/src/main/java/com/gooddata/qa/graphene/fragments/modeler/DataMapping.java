package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;

public class DataMapping extends AbstractFragment {
    private static final By DATA_MAPPING = By.className("model-mapping");

    @FindBy(className = "fixedDataTableRowLayout_rowWrapper")
    List<WebElement> rows;

    public static DataMapping getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataMapping.class, waitForElementVisible(DATA_MAPPING, searchContext));
    }

    public WebElement getRowByName(String labelName, String type) {
        WebElement row = rows.stream()
                .filter(input -> input.findElements(By.className(type)).size() > 0)
                .filter(input -> input.findElement(By.className(type)).getText().equals(labelName))
                .findFirst()
                .get();
        return row;
    }

    public String getSourceColumnByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        WebElement sourceColumn = row.findElement(By.className("s-editable-label"));
        return sourceColumn.getText();
    }

    public String getSourceTypeByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        if (type == "date") {
            return row.findElement(By.cssSelector(".model-mapping-source-type .s-editable-label")).getText();
        } else {
           return row.findElement(By.className("model-mapping-source-type")).getText();
        }
    }

    public  DataMapping editDateFormatByName(String name, String newFormat) {
        WebElement row = getRowByName(name, SOURCE_TYPE.REFERENCE.getName());
        WebElement format = row.findElement(By.className(".model-mapping-source-type .s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(format).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newFormat).sendKeys(Keys.ENTER).build().perform();
        return this;
    }

    public DataMapping editSourceColumnByName(String name, String type, String newName) {
        WebElement row = getRowByName(name, type);
        WebElement sourceColumn = row.findElement(By.className("s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(sourceColumn).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newName).sendKeys(Keys.ENTER).build().perform();
        return this;
    }

    public List<String> getDropdownSourceColumnByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        row.findElement(By.className(type)).click();
        Actions driverActions = new Actions(browser);
        WebElement sourceType = row.findElement(By.className("s-editable-label"));
        driverActions.moveToElement(sourceType).click().build().perform();
        return OverlayWrapper.getInstanceByIndex(browser, 1).getIndigoTableDropDown().getListDropdownOption();
    }

    public List<String> getDropdownDateFormatByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        Actions driverActions = new Actions(browser);
        WebElement sourceType = row.findElement(By.cssSelector(".model-mapping-source-type .s-editable-label"));
        driverActions.moveToElement(sourceType).click().build().perform();
        return OverlayWrapper.getInstanceByIndex(browser, 1).getIndigoTableDropDown().getListDropdownOption();
    }

    public enum SOURCE_TYPE{
        ATTRIBUTE("attribute"),
        LABEL("label"),
        FACT("fact"),
        REFERENCE("reference");

        private final String type;

        private SOURCE_TYPE(String type) {
            this.type = type;
        }

        public String getName() {
            return type;
        }
    }
}
