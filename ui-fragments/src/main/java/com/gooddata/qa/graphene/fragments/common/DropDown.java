package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DropDown extends AbstractFragment {

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    @FindBy(css = ".searchfield input")
    private WebElement searchInput;

    @FindBy(css = ".gd-list-view-item span")
    private List<WebElement> items;

    public void selectItem(String name) {
        tryToSelectItem(name);
        waitForElementNotVisible(this.getRoot());
    }

    public void tryToSelectItem(String name) {
        for (WebElement e : items) {
            if (!name.equals(e.getText().trim()))
                continue;

            e.click();
            break;
        }
    }

    public DropDown searchItem(String name) {
        waitForElementVisible(this.getRoot());

        waitForElementVisible(searchInput).clear();
        searchInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(items);

        searchInput.clear();
        searchInput.sendKeys(name);
        waitForCollectionIsNotEmpty(items);
        return this;
    }

    public void searchAndSelectItem(String name) {
        searchItem(name);
        selectItem(name);
    }

    public void selectFirstItem() {
        waitForCollectionIsNotEmpty(items);
        items.get(0).click();
    }
}
