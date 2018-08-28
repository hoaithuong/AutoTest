package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration.AttributeFilterPicker;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class MetricFiltersTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Metric-Filters-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_filter_metric_by_attribute() {
        assertEquals(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ATTR_ACTIVITY_TYPE, "Email")
            .getFilterText(), ATTR_ACTIVITY_TYPE + ": Email");
    }

    @Test(dependsOnGroups = {"createProject"},
            enabled = false,
            description = "https://jira.intgdc.com/browse/AQE-1233?focusedCommentId=642059&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-642059")
    public void should_not_be_possible_to_filter_metric_by_unavailable_attribute() {
        List<String> attributes = initAnalysePage().addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_LOST_OPPS)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getAllAttributesInViewPort();

        assertTrue(attributes.contains(ATTR_DEPARTMENT));
        assertFalse(attributes.contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_remove_filter() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ATTR_ACTIVITY_TYPE, "Email")
            .removeFilter();
        assertFalse(isElementPresent(cssSelector(".s-filter-button"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_show_tooltip() {
        String description = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getDescription(ATTR_ACTIVITY_TYPE);
        assertTrue(description.contains(ATTR_ACTIVITY_TYPE));
        assertTrue(description.contains("Attribute"));
        assertTrue(description.contains("Email"));
        assertTrue(description.contains("In Person Meeting"));
        assertTrue(description.contains("Phone Call"));
        assertTrue(description.contains("Web Meeting"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_restore_filter_creation() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .selectAttribute(ATTR_ACTIVITY_TYPE);

        analysisPage.undo()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertFalse(isElementPresent(cssSelector(".s-filter-button"), browser));

        assertTrue(analysisPage.redo()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .getFilterText().contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_restore_attribute_elements_settings() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ATTR_ACTIVITY_TYPE, "Email");

        analysisPage.undo()
            .redo()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        waitForElementVisible(cssSelector(".s-filter-button"), browser).click();

        // Check the attribute filter dropdown status
        // is revived correctly. i.e check 2nd attribute element
        // is not selected.
        waitForElementNotPresent(className("s-dropdown-loading"));
        assertTrue(isElementPresent(cssSelector(".s-filter-item[title='In Person Meeting']:not(.is-selected)"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_total_count_in_attribute_filter_label_correctly() {
        String labelCount = ".s-attribute-filter-label .s-total-count";

        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .selectAttribute(ATTR_ACTIVITY_TYPE);
        assertEquals(waitForElementVisible(cssSelector(labelCount), browser).getText(), "All");

        AttributeFilterPicker panel = Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(MetricConfiguration.BY_ATTRIBUTE_FILTER_PICKER, browser));

        panel.clear()
            .selectItems("Email", "Phone Call")
            .apply();
        assertTrue(isElementPresent(cssSelector(labelCount), browser));

        waitForElementVisible(cssSelector(".s-filter-button"), browser).click();
        panel.selectAll()
            .apply();
        assertEquals(waitForElementVisible(cssSelector(labelCount), browser).getText(), "All");
    }
}
