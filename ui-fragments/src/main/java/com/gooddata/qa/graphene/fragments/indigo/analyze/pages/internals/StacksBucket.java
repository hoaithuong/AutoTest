package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class StacksBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-invitation")
    private WebElement bucketInvitation;

    private static final String BUCKET_WITH_WARN_MESSAGE = "bucket-with-warn-message";

    public static final String CSS_SELECTOR = ".s-bucket-stack, .s-bucket-segment";

    public boolean isDisabled() {
        return getRoot().getAttribute("class").contains(BUCKET_WITH_WARN_MESSAGE);
    }

    public String getAttributeName() {
        if (isEmpty()) {
            return "";
        }
        return waitForElementVisible(BY_HEADER, get()).getText().trim();
    }

    public WebElement get() {
        return waitForElementVisible(items.get(0));
    }
}
