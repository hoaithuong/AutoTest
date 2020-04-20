package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class EditDateDimensionDialog extends AbstractFragment {
    private static final String EDIT_DATE_DIMENSION_DIALOG = "edit-date-dimension-dialog";

    @FindBy(className = "gd-input-field")
    WebElement inputField;

    @FindBy(className = "s-save_changes")
    WebElement saveChange;

    public static EditDateDimensionDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                EditDateDimensionDialog.class, waitForElementVisible(className(EDIT_DATE_DIMENSION_DIALOG), searchContext));
    }

    public void changeDateURN(String newURN) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(inputField).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newURN).build().perform();
        saveChange.click();
        waitForFragmentNotVisible(this);
    }
}