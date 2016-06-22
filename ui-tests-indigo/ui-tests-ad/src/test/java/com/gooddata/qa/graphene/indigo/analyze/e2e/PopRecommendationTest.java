package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class PopRecommendationTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Error-States-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_apply__period_over_period__recommendation() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(
                ".adi-chart-container .highcharts-series [fill=\"rgb(172,220,254)\"]"), browser));

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).apply();

        analysisPageReact.waitForReportComputing();

        assertThat(waitForElementVisible(cssSelector(
                ".adi-chart-container .highcharts-legend-item tspan"), browser).getText(),
                containsString("# of Activities - previous year"));
        assertTrue(isElementPresent(cssSelector(
                ".adi-chart-container .highcharts-series [fill=\"rgb(20,178,226)\"]"), browser));

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_honor_period_change_for__period_over_period() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select("This month").apply();

        analysisPageReact.waitForReportComputing();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_widget_after_apply() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser));
        RecommendationContainer container = Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        container.<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPageReact.waitForReportComputing();
        assertFalse(isElementPresent(cssSelector(
                ".adi-chart-container .highcharts-series [fill=\"rgb(172,220,254)\"]"), browser));

        container.<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).apply();

        analysisPageReact.waitForReportComputing();
        assertThat(waitForElementVisible(cssSelector(
                ".adi-chart-container .highcharts-legend-item tspan"), browser).getText(),
                containsString(METRIC_NUMBER_OF_ACTIVITIES + " - previous year"));
        assertTrue(isElementPresent(cssSelector(
                ".adi-chart-container .highcharts-series [fill=\"rgb(20,178,226)\"]"), browser));

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_pop_checkbox_if_date_and_attribute_are_moved_to_bucket() {
        MetricConfiguration configuration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(configuration.isPopEnabled());

        analysisPageReact.replaceAttribute(ATTR_ACTIVITY_TYPE);

        assertFalse(configuration.isPopEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_the_recommendation_if_something_in_stack_bucket() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_the_recommendation_if_date_in_categories_and_something_in_stack_bucket() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .addStack(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_recommendations_if_categories_empty_and_something_in_stack_bucket() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addStack(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
        assertTrue(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
//        enable with CL-9443
//        assertTrue(isElementPresent(cssSelector(".s-recommendation-comparison"), browser));
    }
}
