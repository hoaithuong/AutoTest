package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class DragRecommendationsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Drag-Recommendations-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_render_column_chart_after_a_metric_is_dragged_to_main_recommendation() {
        // D&D the first metric to the initial metric recommendation
        analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-canvas"), browser))
                .waitForReportComputing();

        // should get a single column chart (in switchable visualization "column/bar" ~ "bar")
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .visualization-column .s-property-y.s-id-metricvalues"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_render_date_sliced_metric_column_chart_after_a_metric_is_dragged_to_the_overtime_recommendation() {
        String quarterYearActivityLabel = ".s-id-" + getAttributeDisplayFormIdentifier("Quarter/Year (Activity)", "Short");
        // D&D the first metric to the metric overtime recommendation
        analysisPageReact.drag(analysisPageReact.getCataloguePanel().searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC),
                () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
                .waitForReportComputing();

        // Check bucket items
        assertTrue(analysisPageReact.getMetricsBucket().getItemNames().contains(METRIC_NUMBER_OF_ACTIVITIES));
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));

        // should get a column sliced by the Quarter attribute on the X axis
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .visualization-column .s-property-x" + quarterYearActivityLabel), browser));
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .visualization-column .s-property-y.s-id-metricvalues"), browser));
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .visualization-column .s-property-where" + quarterYearActivityLabel), browser));
        assertTrue(isElementPresent(cssSelector(
                ".adi-components .visualization-column .s-property-where.s-where-___between____3_0__"), browser));

        analysisPageReact.resetToBlankState();

        // check that filter to the last four quarters is now disabled again
        assertFalse(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-where"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_render_attribute_elements_table_after_an_attribute_is_dragged_to_main_recommendation() {
        // D&D the first metric to the metric overtime recommendation
        analysisPageReact.drag(analysisPageReact.getCataloguePanel().getDate(),
                () -> waitForElementVisible(cssSelector(".s-recommendation-attribute-canvas"), browser))
                .waitForReportComputing();

        // Check bucket items
        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));

        // should get a table sliced by the Quarter attribute on the X axis
        assertTrue(isElementPresent(cssSelector(".adi-components .dda-table-component .s-id-" +
                getAttributeDisplayFormIdentifier("Year (Activity)")), browser));
    }
}
