package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.*;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.report.Attribute;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ReportVisualizer extends AbstractFragment {

    private static final String XPATH_REPORT_VISUALIZATION_TYPE = "//div[contains(@class, 's-enabled')]/div[contains(@class, 'c-chartType') and ./span[@title='${type}']]";

    private static final String XPATH_SND_FOLDER = "//div[@title='${SnDFolderName}']";

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    private static final String SND_UNREACHABLE_CLASS = "sndUnReachable";

    @FindBy(css = ".s-snd-AttributesContainer .element .metricName")
    private List<WebElement> howAttributes;

    @FindBy(css = ".sndMetric .metricName")
    private List<WebElement> whatMetrics;

    @FindBy(css = ".c-metricDetailDrillStep button")
    private WebElement addDrillStepButton;

    @FindBy(css = ".s-btn-filter_this_attribute")
    private WebElement filterAttributeButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorWhatArea')]/button")
    private WebElement whatButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorHowArea')]/button")
    private WebElement howButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button")
    private WebElement filterButton;

    @FindBy(xpath = "//label[@class='sndMetricFilterLabel']/../input")
    private WebElement metricFilterInput;

    @FindBy(xpath = "//label[@class='sndAttributeFilterLabel']/../input")
    private WebElement attributeFilterInput;

    @FindBy
    private WebElement reportVisualizationContainer;

    @FindBy(xpath = "//button[contains(@class,'sndCreateMetric')]")
    private WebElement createMetricButton;

    @FindBy(xpath = "//select[contains(@class,'s-sme-fnSelect')]")
    private Select metricOperationSelect;

    @FindBy(xpath = "//select[contains(@class,'s-sme-objSelect')]")
    private Select performOperationSelect;

    @FindBy(xpath = "//input[contains(@class,'s-sme-global')]")
    private WebElement addToGlobalInput;

    @FindBy(xpath = "//input[contains(@class,'s-sme-title')]")
    private WebElement metricTitleInput;

    @FindBy(xpath = "//button[contains(@class,'s-sme-addButton')]")
    private WebElement addMetricButton;

    @FindBy(xpath="//input[contains(@class,'newFolder')]") 
    private WebElement snDFolderNameInput;

    @FindBy(xpath="//select[contains(@class,'s-sme-folder')]") 
    private Select folderOption;

    private String selectedFactLocator ="//select[contains(@class,'s-sme-objSelect')]/option[text()='${factName}']";

    private static final By EMPTY_DATA_REPORT_HELP = By.id("emptyDataReportHelp");

    public ReportVisualizer selectWhatArea(List<WhatItem> what) {
        waitForElementVisible(whatButton).click();

        for (WhatItem item : what) {
            searchAndWaitForItemReloaded(metricFilterInput, item.getMetric(), whatMetrics);
            selectAndConfigureMetric(item);
        }
        return this;
    }

    private void selectAndConfigureMetric(WhatItem what) {
        for (WebElement metric : whatMetrics) {
            if (!what.getMetric().equals(metric.getText().trim()))
                continue;

            metric.findElement(By.cssSelector("input")).click();
            sleepTightInSeconds(2);

            if (what.getDrillStep() == null)
                break;

            metric.click();
            waitForElementVisible(addDrillStepButton).click();

            WebElement popupElement = waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser);
            SelectItemPopupPanel popupPanel = Graphene.createPageFragment(SelectItemPopupPanel.class, popupElement);
            popupPanel.searchAndSelectItem(what.getDrillStep());
            break;
        }
    }

    public WebElement getMetric(String name) {
        for (WebElement metric : whatMetrics) {
            if (name.equals(metric.getText().trim())) {
                return metric;
            }
        }
        return null;
    }

    public void selectHowArea(List<HowItem> how) {
        waitForElementVisible(howButton).click();

        for (HowItem howItem : how) {
            WebElement attribute = selectAttributeWithPosition(howItem.getAttribute(), howItem.getPosition());
            filterHowAttribute(attribute, howItem);
        }
    }

    public void clickOnHow() {
        waitForElementVisible(howButton).click();
    }

    public boolean isGreyedOutAttribute(String attribute) {
        searchAndWaitForItemReloaded(attributeFilterInput, attribute, howAttributes);
        return findAttribute(attribute).findElement(BY_PARENT)
                .getAttribute("class").contains(SND_UNREACHABLE_CLASS);
    }

    public String getDataReportHelpMessage() {
        return waitForElementVisible(EMPTY_DATA_REPORT_HELP, browser).findElement(By.className("alert")).getText();
    }

    private void filterHowAttribute(WebElement attribute, HowItem howItem) {
        List<String> values = howItem.getFilterValues();
        if (values.isEmpty())
            return;

        attribute.click();
        waitForElementVisible(filterAttributeButton).click();
        SelectItemPopupPanel panel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(By.cssSelector(".c-attributeElementsFilterEditor"), browser));
        for (String item : values) {
            panel.searchAndSelectEmbedItem(item);
        }
    }

    private WebElement selectAttributeWithPosition(Attribute attribute, HowItem.Position position) {
        String attributeName = attribute.getName();
        selectAttribute(attributeName);

        sleepTight(500);
        WebElement attributeElement = findAttribute(attributeName);
        sleepTightInSeconds(2);
        WebElement attributePositionElement = waitForElementVisible(attributeElement.findElement(By.cssSelector("div")));
        String attributeClass =  attributePositionElement.getAttribute("class");

        if (!attributeClass.contains(position.getCssClass())) {
            attributePositionElement.click();
        }

        return attributeElement;
    }

    private void selectAttribute(String attribute) {
        searchAndWaitForItemReloaded(attributeFilterInput, attribute, howAttributes);
        findAttribute(attribute).findElement(By.cssSelector("input")).click();
    }

    private WebElement findAttribute(final String attribute) {
        return Iterables.find(howAttributes, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return attribute.equals(input.getText().trim());
            }
        });
    }

    public void addSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact,
            String metricName, boolean addToGlobal) {
        waitForElementVisible(whatButton).click();

        waitForElementVisible(createMetricButton).click();
        waitForElementVisible(metricOperationSelect).selectByVisibleText(metricOperation.name());
        waitForCollectionIsNotEmpty(waitForElementVisible(performOperationSelect).getOptions());
        waitForElementVisible(performOperationSelect).selectByVisibleText(metricOnFact);

        if (metricName != null) {
            waitForElementVisible(metricTitleInput).clear();
            metricTitleInput.sendKeys(metricName);
        }
        if (addToGlobal) waitForElementVisible(addToGlobalInput).click();
        waitForElementVisible(addMetricButton).click();
    }

    public ReportVisualizer finishReportChanges() {
        // When webapp do a lot of CRUD things, its rendering job will work slowly,
        // so need a short time to wait in case like this
        sleepTightInSeconds(2);

        waitForElementVisible(By.cssSelector("form.sndFooterForm > button.s-btn-done"), browser).sendKeys(Keys.ENTER);
        return this;
    }

    public void selectReportVisualisation(ReportTypes reportVisualizationType) {
        By icon = By.xpath(XPATH_REPORT_VISUALIZATION_TYPE.replace("${type}", reportVisualizationType.getName()));
        waitForElementVisible(icon, browser);
        reportVisualizationContainer.findElement(icon).click();
        waitForElementVisible(By.id(reportVisualizationType.getContainerTabId()), browser);
    }

    public void createSnDFolder(String metricOnFact, String folderName) {
        waitForElementVisible(whatButton).click();
        waitForElementVisible(createMetricButton).click();
        By selectedFactOption = By.xpath(selectedFactLocator.replace(
                "${factName}", metricOnFact));
        waitForElementVisible(selectedFactOption, browser);
        waitForElementVisible(performOperationSelect).selectByVisibleText(
                metricOnFact);
        waitForElementVisible(addToGlobalInput).click();
        waitForElementVisible(folderOption).selectByVisibleText(
                "Create New Folder");
        waitForElementVisible(snDFolderNameInput).sendKeys(folderName);
        waitForElementVisible(addMetricButton).click();
        By snDFolder = By.xpath(XPATH_SND_FOLDER.replace("${SnDFolderName}",
                folderName));
        waitForElementVisible(snDFolder, browser);
    }

    private void searchAndWaitForItemReloaded(WebElement input, String searchItem,
            final List<WebElement> itemsShouldBeReloaded) {
        waitForElementVisible(input).clear();
        input.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        sleepTightInSeconds(1);
        waitForCollectionIsEmpty(itemsShouldBeReloaded);

        input.clear();
        input.sendKeys(searchItem);
        sleepTightInSeconds(1);
        waitForCollectionIsNotEmpty(itemsShouldBeReloaded);
    }

}