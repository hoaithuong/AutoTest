package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class LoginFragment extends AbstractFragment {

    @FindBy
    private WebElement email;

    @FindBy
    private WebElement password;

    @FindBy(css = ".s-login-button")
    private WebElement signInButton;

    private static final String ERROR_CLASS = "has-error";

    public void login(String username, String password, boolean validLogin) {
        waitForElementVisible(this.email).clear();
        waitForElementVisible(this.password).clear();
        this.email.sendKeys(username);
        this.password.sendKeys(password);
        if (validLogin) {
            Graphene.guardAjax(signInButton).click();
            waitForElementNotVisible(this.getRoot());
            waitForElementNotVisible(email);
        } else {
            signInButton.click();
        }
    }

    public boolean allLoginElementsAvailable() {
        return email.isDisplayed() && password.isDisplayed() && signInButton.isDisplayed();
    }

    public void checkEmailInvalid() {
        Graphene.waitAjax().until().element(email).attribute("class").contains(ERROR_CLASS);
    }

    public void checkPasswordInvalid() {
        Graphene.waitAjax().until().element(password).attribute("class").contains(ERROR_CLASS);
    }

    public void checkInvalidLogin() {
        checkEmailInvalid();
        checkPasswordInvalid();
    }
}
