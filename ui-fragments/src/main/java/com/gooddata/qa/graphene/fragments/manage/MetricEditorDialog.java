package com.gooddata.qa.graphene.fragments.manage;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.enums.AggregationMetricTypes;
import com.gooddata.qa.graphene.enums.FilterMetricTypes;
import com.gooddata.qa.graphene.enums.GranularityMetricTypes;
import com.gooddata.qa.graphene.enums.LogicalMetricTypes;
import com.gooddata.qa.graphene.enums.NumericMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricEditorDialog extends AbstractFragment {

    @FindBy(css = "div.shareTemplate")
    private WebElement shareMetric;

    @FindBy(css = "div.differenceTemplate")
    private WebElement differenceMetric;

    @FindBy(css = "div.ratioTemplate")
    private WebElement ratioMetric;

    @FindBy(css = "div.customMetric")
    private WebElement customMetric;

    @FindBy(css = "ul.elementList")
    private WebElement elementList;

    private String selectedMetricLocator = "//ul[@class='elementList']/li[text()='${metricName}']";

    private String selectedAttrFolderLocator = "//ul[@class='elementList']/li[@class='category' and text()='${attrFolder}']";

    private String selectedAttrLocator = "//ul[@class='elementList']/li[text()='${attr}']";

    private String selectedAttrValueLocator = "//ul[@class='elementList']/li[text()='${attrValue}']";

    @FindBy(css = "input.text")
    private WebElement metricNameInput;

    @FindBy(xpath = "//form/div[@class='primary controls']/button[contains(@class,'add')]")
    private WebElement addButton;

    @FindBy(xpath = "//form/div[@class='primary controls']/button[contains(@class,'cancel')]")
    private WebElement cancelButton;

    private String metricLinkLocator = "${metricType}";

    @FindBy(css = "div.maqlReferenceGuideHelp")
    private WebElement maqlReferenceGuideHelp;

    @FindBy(xpath = "//a[contains(@class,'metrics') and text()='...']")
    private WebElement placeHolderMetric;

    @FindBy(xpath = "//a[contains(@class,'attributes') and text()='...']")
    private WebElement placeHolderAttr;

    @FindBy(xpath = "//a[contains(@class,'attributeElements') and text()='...']")
    private WebElement placeHolderAttrElements;

    @FindBy(xpath = "//a[contains(@class,'facts') and text()='...']")
    private WebElement placeHolderFact;

    private String selectedFactLocator = "//ul[@class='elementList']/li[text()='${fact}']";

    @FindBy(xpath = "//div[contains(@class,'MAQLEditorElementsMenu')]/div[@class='header']/button[@class='confirm']")
    private WebElement addSelectedButton;

    @FindBy(xpath = "//label[@class='metricName']/input")
    private WebElement customMetricNameInput;

    @FindBy(xpath = "//button[contains(@class,'editor')]/span[text()='Add']")
    private WebElement customAddButton;

    @FindBy(xpath = "//div[@class='listContainer']/div/ul[@class='elementList']/li[@class='category']")
    private WebElement customCategoryList;

    @FindBy(xpath = "//button[text()='Create Metric']")
    private WebElement createMetricButton;

    @FindBy(css = ".s-btn-edit")
    private WebElement editButton;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;

    @FindBy(tagName = "iframe")
    private WebElement metricEditorPopup;

    @FindBy(id = "metricsTable")
    private ObjectsTable metricsTable;

    @FindBy(id = "p-objectPage")
    private MetricDetailsPage metricDetailPage;

    @FindBy(xpath = "//a[@href='#maqldoc_tab1']/em[text()='Aggregation']")
    private WebElement aggregationTab;

    @FindBy(xpath = "//a[@href='#maqldoc_tab2']/em[text()='Numeric']")
    private WebElement numericTab;

    @FindBy(xpath = "//a[@href='#maqldoc_tab3']/em[text()='Granularity']")
    private WebElement GranularityTab;

    @FindBy(xpath = "//a[@href='#maqldoc_tab4']/em[text()='Filters']")
    private WebElement FiltersTab;

    @FindBy(xpath = "//a[@href='#maqldoc_tab5']/em[text()='Logical']")
    private WebElement LogicalTab;

    public void createShareMetric(String metricName, String usedMetric,
	    String attrFolder, String attr) throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(shareMetric).click();
	By selectedMetric = By.xpath(selectedMetricLocator.replace(
		"${metricName}", usedMetric));
	waitForElementVisible(selectedMetric).click();
	By selectedAttrFolder = By.xpath(selectedAttrFolderLocator.replace(
		"${attrFolder}", attrFolder));
	waitForElementVisible(selectedAttrFolder).click();
	By selectedAttr = By
		.xpath(selectedAttrLocator.replace("${attr}", attr));
	waitForElementVisible(selectedAttr).click();
	waitForElementVisible(metricNameInput).clear();
	metricNameInput.sendKeys(metricName);
	waitForElementVisible(addButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
	String expectedMaql = "SELECT " + usedMetric + " / (SELECT "
		+ usedMetric + " BY " + attr + ", ALL OTHER WITHOUT PF)";
	String expectedFormat = "#,##0.00";
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectObject(metricName);
	waitForObjectPageLoaded();
	metricDetailPage.checkCreatedMetric(metricName, expectedMaql, expectedFormat);
    }

    public void createDifferentMetric(String metricName, String usedMetric,
	    String attrFolder, String attr, String attrValue)
	    throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(differenceMetric).click();
	By selectedMetric = By.xpath(selectedMetricLocator.replace(
		"${metricName}", usedMetric));
	waitForElementVisible(selectedMetric).click();
	By selectedAttrFolder = By.xpath(selectedAttrFolderLocator.replace(
		"${attrFolder}", attrFolder));
	waitForElementVisible(selectedAttrFolder).click();
	By selectedAttr = By
		.xpath(selectedAttrLocator.replace("${attr}", attr));
	waitForElementVisible(selectedAttr).click();
	By selectedAttrValue = By.xpath(selectedAttrValueLocator.replace(
		"${attrValue}", attrValue));
	waitForElementVisible(selectedAttrValue).click();
	waitForElementVisible(metricNameInput).clear();
	metricNameInput.sendKeys(metricName);
	waitForElementVisible(addButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
	String expectedMaql = "SELECT " + usedMetric + " - (SELECT "
		+ usedMetric + " BY ALL " + attr + " WHERE " + attr + " IN ("
		+ attrValue + ") WITHOUT PF)";
	String expectedFormat = "#,##0.00";
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectObject(metricName);
	waitForObjectPageLoaded();
	metricDetailPage.checkCreatedMetric(metricName, expectedMaql, expectedFormat);
    }

    public void createRatioMetric(String metricName, String usedMetric1,
	    String usedMetric2) throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(ratioMetric).click();
	By selectedMetric1 = By.xpath(selectedMetricLocator.replace(
		"${metricName}", usedMetric1));
	waitForElementVisible(selectedMetric1).click();
	By selectedMetric2 = By.xpath(selectedMetricLocator.replace(
		"${metricName}", usedMetric2));
	waitForElementVisible(selectedMetric2).click();
	waitForElementVisible(metricNameInput).clear();
	metricNameInput.sendKeys(metricName);
	waitForElementVisible(addButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
	String expectedMaql = "SELECT " + usedMetric1 + " / " + usedMetric2;
	String expectedFormat = "#,##0.00";
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectObject(metricName);
	waitForObjectPageLoaded();
	metricDetailPage.checkCreatedMetric(metricName, expectedMaql, expectedFormat);
    }

    public void createAggregationMetric(AggregationMetricTypes metricType,
	    String metricName, Map<String, String> data)
	    throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(customMetric).click();
	waitForElementVisible(maqlReferenceGuideHelp);
	waitForElementVisible(aggregationTab).click();
	By metricLink = By.linkText(metricLinkLocator.replace("${metricType}",
		metricType.getlabel()));
	waitForElementVisible(metricLink).click();
	switch (metricType) {
	case AVG:
	case RUNAVG:
	case MAX:
	case RUNMAX:
	case MIN:
	case RUNMIN:
	case SUM:
	case RUNSUM:
	case MEDIAN:
	case VAR:
	case VARP:
	case PERCENTILE:
	    selectFacts(data, 1);
	    break;
	case COUNT:
	    selectAttributes(data, 2);
	    break;
	case CORREL:
	case COVAR:
	case COVARP:
	case INTERCEPT:
	case RSQ:
	case SLOPE:
	case STDEV:
	case STDEVP:
	    selectFacts(data, 2);
	    break;
	default:
	    break;
	}
	waitForElementVisible(customMetricNameInput).sendKeys(metricName);
	waitForElementVisible(customAddButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createNumericMetric(NumericMetricTypes metricType,
	    String metricName, Map<String, String> data)
	    throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(customMetric).click();
	waitForElementVisible(maqlReferenceGuideHelp);
	waitForElementVisible(numericTab).click();
	By metricLink = By.linkText(metricLinkLocator.replace("${metricType}",
		metricType.getlabel()));
	waitForElementVisible(metricLink).click();
	selectMetrics(data, 1);
	waitForElementVisible(customMetricNameInput).sendKeys(metricName);
	waitForElementVisible(customAddButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createGranularityMetric(GranularityMetricTypes metricType,
	    String metricName, Map<String, String> data)
	    throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(customMetric).click();
	waitForElementVisible(maqlReferenceGuideHelp);
	waitForElementVisible(GranularityTab).click();
	By metricLink = By.linkText(metricLinkLocator.replace("${metricType}",
		metricType.getlabel()));
	waitForElementVisible(metricLink).click();
	switch (metricType) {
	case BY:
	case BY_ALL_ATTRIBUTE:
	case BY_ATTR_ALL_OTHER:
	    selectMetrics(data, 2);
	    selectAttributes(data, 1);
	    break;
	case FOR_NEXT:
	case FOR_PREVIOUS:
	case FOR_NEXT_PERIOD:
	case FOR_PREVIOUS_PERIOD:
	case BY_ALL_EXCEPT:
	    selectMetrics(data, 1);
	    selectAttributes(data, 1);
	    break;
	case BY_ALL:
	    selectMetrics(data, 2);
	    break;
	case WITHIN:
	    selectMetrics(data, 1);
	    selectFacts(data, 1);
	    break;
	default:
	    break;
	}
	waitForElementVisible(customMetricNameInput).sendKeys(metricName);
	waitForElementVisible(customAddButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createLogicalMetric(LogicalMetricTypes metricType,
	    String metricName, Map<String, String> data)
	    throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(customMetric).click();
	waitForElementVisible(maqlReferenceGuideHelp);
	waitForElementVisible(LogicalTab).click();
	By metricLink = By.linkText(metricLinkLocator.replace("${metricType}",
		metricType.getlabel()));
	waitForElementVisible(metricLink).click();
	switch (metricType) {
	case AND:
	case OR:
	    selectMetrics(data, 1);
	    selectAttributes(data, 2);
	    selectAttrElements(data, 2);
	    break;
	case NOT:
	    selectMetrics(data, 1);
	    selectAttributes(data, 1);
	    selectAttrElements(data, 1);
	    break;
	case CASE:
	    selectMetrics(data, 4);
	    break;
	case IF:
	    selectMetrics(data, 3);
	    break;
	default:
	    break;
	}
	waitForElementVisible(customMetricNameInput).sendKeys(metricName);
	waitForElementVisible(customAddButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createFilterMetric(FilterMetricTypes metricType,
	    String metricName, Map<String, String> data)
	    throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
	waitForElementVisible(customMetric).click();
	waitForElementVisible(maqlReferenceGuideHelp);
	waitForElementVisible(FiltersTab).click();
	By metricLink = By.linkText(metricLinkLocator.replace("${metricType}",
		metricType.getlabel()));
	waitForElementVisible(metricLink).click();
	switch (metricType) {
	case EQUAL:
	    selectMetrics(data, 1);
	    selectAttributes(data, 1);
	    selectAttrElements(data, 1);
	    break;
	case BETWEEN:
	case NOT_BETWEEN:
	case IN:
	case NOT_IN:
	    selectMetrics(data, 1);
	    selectAttributes(data, 1);
	    selectAttrElements(data, 2);
	    break;
	case TOP:
	    selectFacts(data, 2);
	    selectMetrics(data, 1);
	    break;
	case BOTTOM:
	    selectFacts(data, 2);
	    selectAttributes(data, 1);
	    break;
	case WITHOUT_PF:
	    selectMetrics(data, 2);
	    selectAttributes(data, 1);
	    break;
	default:
	    break;
	}
	waitForElementVisible(customMetricNameInput).sendKeys(metricName);
	waitForElementVisible(customAddButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void selectAttributes(Map<String, String> data, int count) {
	for (int i = 0; i < count; i++) {
	    String attr = data.get("attribute" + i);
	    String attrFolder = data.get("attrFolder" + i);
	    waitForElementVisible(placeHolderAttr).click();
	    By selectedAttrFolder = By.xpath(selectedAttrFolderLocator.replace(
		    "${attrFolder}", attrFolder));
	    waitForElementVisible(selectedAttrFolder).click();
	    By selectedAttr = By.xpath(selectedAttrLocator.replace("${attr}",
		    attr));
	    waitForElementVisible(selectedAttr).click();
	    waitForElementVisible(addSelectedButton).click();
	    waitForElementNotPresent(selectedAttr);
	    waitForElementVisible(customCategoryList);
	}
    }

    public void selectAttrElements(Map<String, String> data, int count) {
	for (int i = 0; i < count; i++) {
	    String attr = data.get("attribute" + i);
	    String attrFolder = data.get("attrFolder" + i);
	    String attrValue = data.get("attrValue" + i);
	    waitForElementVisible(placeHolderAttrElements).click();
	    By selectedAttrFolder = By.xpath(selectedAttrFolderLocator.replace(
		    "${attrFolder}", attrFolder));
	    waitForElementVisible(selectedAttrFolder).click();
	    By selectedAttr = By.xpath(selectedAttrLocator.replace("${attr}",
		    attr));
	    waitForElementVisible(selectedAttr).click();
	    By selectedAttrEl = By.xpath(selectedAttrValueLocator.replace(
		    "${attrValue}", attrValue));
	    waitForElementVisible(selectedAttrEl).click();
	    waitForElementVisible(addSelectedButton).click();
	    waitForElementNotPresent(selectedAttrEl);
	    waitForElementVisible(customCategoryList);
	}
    }

    public void selectFacts(Map<String, String> data, int count) {
	for (int i = 0; i < count; i++) {
	    String fact = data.get("fact" + i);
	    waitForElementVisible(placeHolderFact).click();
	    By selectedFact = By.xpath(selectedFactLocator.replace("${fact}",
		    fact));
	    waitForElementVisible(selectedFact).click();
	    waitForElementVisible(addSelectedButton).click();
	    waitForElementNotPresent(selectedFact);
	    waitForElementVisible(customCategoryList);
	}
    }

    public void selectMetrics(Map<String, String> data, int count) {
	for (int i = 0; i < count; i++) {
	    String metric = data.get("metric" + i);
	    waitForElementVisible(placeHolderMetric).click();
	    By selectedMetric = By.xpath(selectedMetricLocator.replace(
		    "${metricName}", metric));
	    waitForElementVisible(selectedMetric).click();
	    waitForElementVisible(addSelectedButton).click();
	    waitForElementNotPresent(selectedMetric);
	    waitForElementVisible(customCategoryList);
	}
    }

}
