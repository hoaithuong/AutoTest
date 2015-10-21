package com.gooddata.qa.graphene.fragments.profile;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class UserProfilePage extends AbstractFragment {

    @FindBy(css = ".fullname")
    private WebElement fullname;

    @FindBy(css = ".email a")
    private WebElement email;

    @FindBy(css = ".phone div.value")
    private WebElement phone;

    @FindBy(css = ".company div.value")
    private WebElement company;

    @FindBy(css = ".usersTable")
    private UserVariableTable userVariableTable;

    @FindBy(css = ".item")
    private List<WebElement> recentActivityItems;

    public PersonalInfo getUserInfo() {
        return new PersonalInfo()
                .withFullName(waitForElementVisible(fullname).getText())
                .withEmail(waitForElementVisible(email).getText())
                .withCompany(waitForElementVisible(company).getText())
                .withPhoneNumber(waitForElementVisible(phone).getText());
    }

    public List<String> getAllUserVariables() {
        return waitForFragmentVisible(userVariableTable).getAllItems();
    }

    public int getRecentActivityItems() {
        return recentActivityItems.size();
    }

    public boolean isItemDisplayedInRecentActivity(final String itemName) {
        return recentActivityItems.stream()
                .map(e -> e.findElement(By.cssSelector(".title")))
                .map(WebElement::getText)
                .filter(e -> e.equals(itemName))
                .findFirst()
                .isPresent();
    }

    public class UserVariableTable extends AbstractTable {
        private List<String> getAllItems() {
            return rows.stream()
                    .map(e -> e.findElement(BY_LINK).getText())
                    .collect(Collectors.toList());
        }
    }
}
