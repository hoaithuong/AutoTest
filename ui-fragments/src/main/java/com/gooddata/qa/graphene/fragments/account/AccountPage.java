package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static org.openqa.selenium.By.id;

import com.gooddata.qa.browser.BrowserUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;

public class AccountPage extends AbstractFragment {

    private static final By PERSONAL_INFORMATION_DIALOG_LOCATOR = By.cssSelector(".userAccountForm");
    private static final By CHANGE_PASSWORD_DIALOG_LOCATOR = By.cssSelector(".changePasswordDialog");
    private static final By REGIONAL_NUMBER_FORMATTING_DIALOG_LOCATOR = By
            .cssSelector(".c-regionalNumberFormattingDialog");

    private static final By CONFIRM_DELETE_ACCOUNT_BUTTON_LOCATOR = By.cssSelector(".s-btn-delete");
    private static final By CANCEL_CONFIRMATION_DIALOG_BUTTON_LOCATOR = By
            .cssSelector(".s-btn-cancel.yui3-c-button-showInline");

    @FindBy(css = ".personalInformation a")
    private WebElement personalInformationLink;

    @FindBy(css = ".password a")
    private WebElement changePasswordLink;

    @FindBy(css = ".regionalNumberFormatting a")
    private WebElement regionalNumberFormattingLink;

    @FindBy(css = ".projects a")
    private WebElement activeProjectsLink;

    @FindBy(css = ".deleteAccount a")
    private WebElement deleteYourAccountLink;

    @FindBy(css = ".changeLanguage a")
    private WebElement changeLanguageLink;

    public static final AccountPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AccountPage.class, waitForElementVisible(id("accountSettingsMenu"), context));
    }

    public PersonalInfoDialog openPersonalInfoDialog() {
        waitForElementVisible(personalInformationLink).click();
        return Graphene.createPageFragment(PersonalInfoDialog.class,
                waitForElementVisible(PERSONAL_INFORMATION_DIALOG_LOCATOR, browser));
    }

    public ChangePasswordDialog openChangePasswordDialog() {
        //Click action on element does not affect sometimes, so switch to use java script executor.
        BrowserUtils.runScript(browser, "arguments[0].click();", waitForElementVisible(changePasswordLink));
        return Graphene.createPageFragment(ChangePasswordDialog.class,
                waitForElementVisible(CHANGE_PASSWORD_DIALOG_LOCATOR, browser));
    }

    public RegionalNumberFormattingDialog openRegionalNumberFormattingDialog() {
        waitForElementVisible(regionalNumberFormattingLink).click();
        return Graphene.createPageFragment(RegionalNumberFormattingDialog.class,
                waitForElementVisible(REGIONAL_NUMBER_FORMATTING_DIALOG_LOCATOR, browser));
    }

    public ProjectsPage openActiveProjectsPage() {
        waitForElementVisible(activeProjectsLink).click();
        waitForProjectsPageLoaded(browser);
        return ProjectsPage.getInstance(browser);
    }

    public void deleteAccount() {
        waitForElementVisible(deleteYourAccountLink).click();
        waitForElementVisible(CONFIRM_DELETE_ACCOUNT_BUTTON_LOCATOR, browser).click();
    }

    public AccountPage tryDeleteAccountButDiscard() {
        waitForElementVisible(deleteYourAccountLink).click();
        waitForElementVisible(CANCEL_CONFIRMATION_DIALOG_BUTTON_LOCATOR, browser).click();
        return this;
    }

    public void changeLanguage(String lang) {
        waitForElementVisible(changeLanguageLink).click();
        ChangeLanguageDialog.getInstance(browser).selectLanguage(lang).saveChange();
        // there is a count-down dialog and it will be disappeared in 5 sec, then page will be reloaded
        sleepTightInSeconds(7);
        waitForFragmentVisible(this);
    }
}
