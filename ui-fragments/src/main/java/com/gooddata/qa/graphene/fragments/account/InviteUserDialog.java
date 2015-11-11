package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.mail.ImapUtils.getMessageWithExpectedReceivedTime;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessageWithExpectedCount;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class InviteUserDialog extends AbstractFragment {

    public static final By INVITE_USER_DIALOG_LOCATOR = By.cssSelector(".c-invitationDialog");

    @FindBy
    private WebElement invitationEmailAddresses;

    @FindBy
    private Select invitationRole;

    @FindBy
    private WebElement invitationPersonalMessage;

    @FindBy(css = ".s-btn-invite")
    private WebElement inviteButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    public String inviteUsers(ImapClient imapClient, String emailSubject, UserRoles role,
            String message, String...emails) throws MessagingException, IOException {
        int expectedMessageCount = getMessageWithExpectedReceivedTime(imapClient,
                GDEmails.INVITATION, emailSubject, 0).size();

        inviteUsers(role, message, emails);

        return getInvitationLink(imapClient, emailSubject, expectedMessageCount);
    }

    public void cancelInvite() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private InviteUserDialog enterEmails(String... emails) {
        waitForElementVisible(invitationEmailAddresses).sendKeys(Joiner.on(",").join(emails));
        return this;
        
    }

    private InviteUserDialog selectRole(UserRoles role) {
        waitForElementVisible(invitationRole).selectByVisibleText(role.getName());
        return this;
    }

    private InviteUserDialog enterMessage(String message) {
        if(Objects.isNull(message)) {
            return this;
        }
        waitForElementVisible(invitationPersonalMessage).sendKeys(message);
        return this;
    }

    private void inviteUsers(UserRoles role, String message, String... emails) {
        if (emails.length == 0) {
            throw new IllegalArgumentException("Must provide at least 1 email.");
        }

        enterEmails(emails).selectRole(role).enterMessage(message);
        waitForElementVisible(inviteButton).click();
    }

    private String getInvitationLink(ImapClient imapClient, String emailSubject, int expectedMessageCount)
            throws MessagingException, IOException {
        Collection<Message> messages = waitForMessageWithExpectedCount(imapClient, GDEmails.INVITATION,
                emailSubject, expectedMessageCount);
        Message invitationMessage = Iterables.getLast(messages);
        String messageBody = ImapClient.getEmailBody(invitationMessage);
        int beginIndex = messageBody.indexOf("/p/");
        return messageBody.substring(beginIndex, messageBody.indexOf("\n", beginIndex));
    }
}
