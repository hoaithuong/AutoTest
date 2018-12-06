package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.isShortendTilteDesignByCss;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gooddata.qa.graphene.utils.ElementUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.WaitUtils;

public class ChartReport extends AbstractFragment {

    public static final String LEGEND_ITEM = ".viz-legend .series .series-item";
    public static final String LEGEND_ITEM_NAME = LEGEND_ITEM + " .series-name";
    public static final String LEGEND_ITEM_ICON = LEGEND_ITEM + " .series-icon";
    private static final String LEGEND_COLOR_ATTRIBUTE = "style";

    @FindBy(css = ".highcharts-series *")
    private List<WebElement> trackers;

    @FindBy(css = ".highcharts-markers *")
    private List<WebElement> markers;

    @FindBy(css = LEGEND_ITEM_ICON)
    private List<WebElement> legendIcons;

    @FindBy(css = LEGEND_ITEM_NAME)
    private List<WebElement> legendNames;

    @FindBy(css = "div.highcharts-tooltip")
    private WebElement tooltip;

    @FindBy(css = ".highcharts-data-labels tspan")
    private List<WebElement> dataLabels;

    @FindBy(css = ".highcharts-axis-labels text[text-anchor = 'middle']")
    private List<WebElement> axisLabels;

    @FindBy(css = ".highcharts-xaxis-labels text[text-anchor = 'middle'], .highcharts-xaxis-labels text[text-anchor = 'end']")
    private List<WebElement> xAxisLabels;

    private static final By BY_X_AXIS_TITLE = By.className("highcharts-xaxis-title");
    private static final By BY_Y_AXIS_TITLE = By.className("highcharts-yaxis-title");

    public boolean isColumnHighlighted(Pair<Integer, Integer> position) {
        WebElement element = getTracker(position.getLeft(), position.getRight());
        final String fillBeforeHover = element.getAttribute("fill");
        getActions().moveToElement(element).moveByOffset(1, 1).perform();
        final String fillAfterHover = element.getAttribute("fill");
        return !Objects.equals(fillAfterHover, fillBeforeHover);
    }

    public void clickOnElement(Pair<Integer, Integer> position) {
        WebElement element = getTracker(position.getLeft(), position.getRight());
        // Because geckodriver follows W3C and moves the mouse pointer from the centre of the screen,
        // Move the mouse pointer to the top-left corner of the fragment before moving to the specific Element
        ElementUtils.moveToElementActions(getRoot(), 0, 0).moveToElement(element)
                .moveByOffset(1, 1).click().perform();
    }

    public void clickOnDataLabel(Pair<Integer, Integer> position) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot().findElements(
                By.cssSelector(String.format(".highcharts-data-labels.highcharts-series-%s g", position.getLeft()))));
        WebElement element = list.get(position.getRight());
        getActions().moveToElement(element).moveByOffset(1, 1).click().perform();
    }

    //Some type charts don't exist legend will return Zero
    public int getLegendIndex(String legendName) {
        List<WebElement> elements = getRoot()
                .findElements(By.className("series-item"));
        if(elements.isEmpty()) {
            return 0;
        }
        List<String> texts = elements.stream().map(element -> element.getText()).collect(Collectors.toList());
        return texts.indexOf(legendName);
    }

    //Some type charts don't exist axis will return empty
    public String getYaxisTitle() {
        List<WebElement> yAxisTitle = getRoot().findElements(BY_Y_AXIS_TITLE);
        if (yAxisTitle.isEmpty()) {
            return "";
        }
        return yAxisTitle.get(0).getText();
    }

    //Some type charts don't exist axis will return empty
    public String getXaxisTitle() {
        List<WebElement> xAxisTitle = getRoot().findElements(BY_X_AXIS_TITLE);
        if (xAxisTitle.isEmpty()) {
            return "";
        }
        return xAxisTitle.get(0).getText();
    }

    public int getTrackersCount() {
        if (isLineChart()) {
            return waitForCollectionIsNotEmpty(trackers).size();
        }

        return (int) waitForCollectionIsNotEmpty(trackers).stream()
            .map(e -> e.getAttribute("height"))
            .map(Integer::parseInt)
            .filter(i -> i > 0)
            .count();
    }

    public int getMarkersCount() {
        return waitForCollectionIsNotEmpty(markers).size();
    }

    public List<List<String>> getTooltipTextOnTrackerByIndex(int index) {
        displayTooltipOnTrackerByIndex(index);
        return getTooltipText();
    }

    public boolean isShortenTooltipTextOnTrackerByIndex(int index, int width) {
        displayTooltipOnTrackerByIndex(index);
        return isShortendTilteDesignByCss(waitForElementVisible(tooltip.findElement(By.className("title"))), width);
    }

    public boolean isLegendVisible() {
        return !legendNames.isEmpty();
    }

    public boolean areLegendsHorizontal() {
        return isElementVisible(By.cssSelector(".viz-legend.position-top"), browser);
    }

    public boolean areLegendsVertical() {
        return isElementVisible(By.cssSelector(".viz-legend.position-right"), browser);
    }

    public boolean isShortenNameInLegend(String measureName, int width) {
        WebElement measure = legendNames.stream().filter(measures -> measures.getText().equals(measureName)).findFirst().get();
        return isShortendTilteDesignByCss(measure, width);
    }

    public List<String> getLegends() {
        return waitForCollectionIsNotEmpty(legendNames).stream()
            .map(e -> e.getText())
            .collect(toList());
    }

    public List<String> getLegendColors() {
        return waitForCollectionIsNotEmpty(legendIcons).stream()
            .map(e -> e.getAttribute(LEGEND_COLOR_ATTRIBUTE))
            .map(e -> e.replaceAll(".*background-color: ([^;]*);.*", "$1").replace(" ", ""))
            .collect(toList());
    }

    public List<String> getDataLabels() {
        return getLabels(dataLabels);
    }

    public List<String> getAxisLabels() {
        // Axis labels will be empty in case report has no attribute.
        if (axisLabels.isEmpty())
            return Collections.emptyList();

        return getLabels(axisLabels);
    }

    public List<String> getXaxisLabels() {
        // Axis labels will be empty in case report has no attribute.
        if (xAxisLabels.isEmpty()) {
            return Collections.emptyList();
        }
        return getLabels(xAxisLabels);
    }

    public String getChartType() {
        return Stream.of(getRoot().getAttribute("class").split("\\s+"))
                .filter(e -> e.contains("s-visualization-"))
                .map(e -> e.replace("s-visualization-", ""))
                .findFirst()
                .get();
    }

    private boolean isLineChart() {
        return getRoot().getAttribute("class").contains("visualization-line");
    }

    private List<String> getLabels(Collection<WebElement> labels) {
        // all labels need to be visible before getting text
        // without this it's specially unstable on embedded AD
        labels.stream().forEach(WaitUtils::waitForElementVisible);
        return getElementTexts(labels);
    }

    private void displayTooltipOnTrackerByIndex(int index) {
        checkIndex(index);
        getActions().moveToElement(trackers.get(index)).moveByOffset(1, 1).perform();
        if (isLineChart())
            waitForElementVisible(markers.get(index)).click();
        waitForElementVisible(tooltip);
    }

    private List<List<String>> getTooltipText() {
        return waitForCollectionIsNotEmpty(tooltip.findElements(By.cssSelector("tr"))).stream()
                .map(row -> asList(row.findElement(By.cssSelector(".title")).getText(),
                        row.findElement(By.cssSelector(".value")).getText()))
                .collect(Collectors.toList());
    }

    private void checkIndex(int index) {
        waitForCollectionIsNotEmpty(trackers);
        if (index < 0 || index >= trackers.size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private WebElement getTracker(int xAxis, int yAxis) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
                .findElements(By.cssSelector(String.format(".highcharts-series-%s.highcharts-tracker rect," +
                        ".highcharts-series-%s.highcharts-tracker path", xAxis, xAxis))));
        return list.get(yAxis);
    }
}
