package com.gooddata.qa.graphene.utils;

import com.google.common.collect.Iterables;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class UrlParserUtils {
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

    public static String getProjectId(String url) {
        String urlSuffix = url.split("projects/")[1];
        return urlSuffix.split("/")[0];
    }

    public static String getObjdUri(String currentUrl) {
        return Stream.of(currentUrl.split("\\|"))
                .filter(part -> part.matches("/gdc/md/.*/obj/.*"))
                .findFirst()
                .get();
        
    }

    public static String getObjId(final String currentUrl) {
        return Iterables.getLast(asList(getObjdUri(currentUrl).split("/")));
    }
}
