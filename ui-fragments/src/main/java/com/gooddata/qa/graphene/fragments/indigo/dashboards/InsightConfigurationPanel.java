package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisInsightSelectionPanel;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

public class InsightConfigurationPanel extends AbstractFragment {

    private static final String INSIGHT_CONFIGURATION_PANEL_ROOT = ".s-gd-configuration-bubble .insight-configuration";

    @FindBy(className = "icon-settings")
    private WebElement configurationSelect;

    @FindBy(className = "icon-interaction")
    private WebElement interactionSelect;

    @FindBy(className = "s-options-menu-edit-insight")
    private WebElement editInsight;

    @FindBy(className = "s-delete-insight-item")
    private WebElement removeInsight;

    public static InsightConfigurationPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(InsightConfigurationPanel.class,
                waitForElementVisible(By.cssSelector(INSIGHT_CONFIGURATION_PANEL_ROOT), context));
    }

    public ConfigurationPanel clickConfiguration() {
        waitForElementVisible(configurationSelect).click();
        return ConfigurationPanel.getInstance(browser);
    }

    public ConfigurationPanel clickInteraction() {
        waitForElementVisible(interactionSelect).click();
        return ConfigurationPanel.getInstance(browser);
    }

    public AnalysisPage clickEditInsight() {
        waitForElementVisible(editInsight).click();
        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
        return AnalysisPage.getInstance(browser);
    }

    public InsightConfigurationPanel clickRemoveInsight() {
        waitForElementVisible(removeInsight).click();
        return this;
    }
}
