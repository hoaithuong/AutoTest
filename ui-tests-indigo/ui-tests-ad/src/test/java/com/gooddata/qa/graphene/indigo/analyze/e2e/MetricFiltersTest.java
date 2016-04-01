package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration.AttributeFilterPicker;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class MetricFiltersTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Metric-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_filter_metric_by_attribute() {
        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ACTIVITY_TYPE, "Email")
            .getFilterText(), ACTIVITY_TYPE + ": Email");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_filter_metric_by_unavailable_attribute() {
        List<String> attributes = analysisPage.addMetric(NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_LOST_OPPS)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getAllAttributesInViewPort();

        assertTrue(attributes.contains(DEPARTMENT));
        assertFalse(attributes.contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_remove_filter() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ACTIVITY_TYPE, "Email")
            .removeFilter();
        assertFalse(isElementPresent(cssSelector(".s-filter-button"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_show_tooltip() {
        String description = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getDescription(ACTIVITY_TYPE);
        assertTrue(description.contains(ACTIVITY_TYPE));
        assertTrue(description.contains("Attribute"));
        assertTrue(description.contains("Email"));
        assertTrue(description.contains("In Person Meeting"));
        assertTrue(description.contains("Phone Call"));
        assertTrue(description.contains("Web Meeting"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_restore_filter_creation() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .selectAttribute(ACTIVITY_TYPE);

        analysisPage.undo()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(isElementPresent(cssSelector(".s-filter-button"), browser));

        assertEquals(analysisPage.redo()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .getFilterText(), ACTIVITY_TYPE + ": All");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_restore_attribute_elements_settings() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ACTIVITY_TYPE, "Email");

        analysisPage.undo()
            .redo()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        waitForElementVisible(cssSelector(".s-filter-button"), browser).click();

        // Check the attribute filter dropdown status
        // is revived correctly. i.e check 2nd attribute element
        // is not selected.
        waitForElementNotPresent(className("filter-items-loading"));
        assertTrue(isElementPresent(cssSelector(".s-filter-item[title='In Person Meeting']:not(.is-selected)"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_total_count_in_attribute_filter_label_correctly() {
        String labelCount = ".s-attribute-filter-label .s-total-count";

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .selectAttribute(ACTIVITY_TYPE);
        assertFalse(isElementPresent(cssSelector(labelCount), browser));

        AttributeFilterPicker panel = Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(MetricConfiguration.BY_ATTRIBUTE_FILTER_PICKER, browser));

        panel.clear()
            .selectItems("Email")
            .apply();
        assertTrue(isElementPresent(cssSelector(labelCount), browser));

        waitForElementVisible(cssSelector(".s-filter-button"), browser).click();
        panel.selectAll()
            .apply();
        assertFalse(isElementPresent(cssSelector(labelCount), browser));
    }
}
