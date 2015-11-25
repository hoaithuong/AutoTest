package com.gooddata.qa.graphene.utils;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForStringInUrl;
import static java.lang.String.format;
import org.openqa.selenium.WebDriver;

public class NavigateUtils {
    public static void replaceInUrl(WebDriver browser, String target, String replacement) {
        waitForStringInUrl(target);

        String currentUrl = browser.getCurrentUrl();
        String replacedUrl = currentUrl.replace(target, replacement);

        // wait for some time so that calling browser.get() really gets the new page
        // (another option is to call .get() multiple times until it changes current url)
        Sleeper.sleepTightInSeconds(5);

        browser.get(replacedUrl);
        System.out.println(format("Changed url from %s to %s", currentUrl, replacedUrl));
    }
}
