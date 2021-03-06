package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport.LEGEND_ITEM_NAME;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class OverTimeComparisonRecommendationTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Error-States-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_apply__period_over_period__recommendation() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser),
                "Recommendation comparison with period shouldn't be present");
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();


        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).apply();

        analysisPage.waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser),
                "Recommendation comparison with period shouldn't be present");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_honor_period_change_for__period_over_period() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser),
                "Recommendation comparison with period shouldn't be present");
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).select("This month").apply();

        analysisPage.waitForReportComputing();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_hide_widget_after_apply() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_SNAPSHOT_BOP)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser),
                "Recommendation metric with period shouldn't be present");
        RecommendationContainer container = Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        container.<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();

        container.<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).apply();

        analysisPage.waitForReportComputing();
        assertThat(waitForElementVisible(cssSelector(LEGEND_ITEM_NAME), browser).getText(),
                containsString(METRIC_SNAPSHOT_BOP_YEAR_AGO));

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser),
                "Recommendation metric with period shouldn't be present");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_hide_the_recommendation_if_something_in_stack_bucket() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser),
                "Recommendation metric with period shouldn't be present");
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser),
                "Recommendation comparison with period shouldn't be present");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_hide_the_recommendation_if_date_in_categories_and_something_in_stack_bucket() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .addStack(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser),
                "Recommendation metric with period shouldn't be present");
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser),
                "Recommendation comparison with period shouldn't be present");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_recommendations_if_categories_empty_and_something_in_stack_bucket() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addStack(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();

        assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-with-period"), browser),
                "Recommendation metric with period shouldn't be present");
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser),
                "Recommendation comparison with period shouldn't be present");
        assertTrue(isElementPresent(cssSelector(".s-recommendation-trending"), browser),
                "Recommendation trending should present");
//        enable with CL-9443
//        assertTrue(isElementPresent(cssSelector(".s-recommendation-comparison"), browser));
    }
}
