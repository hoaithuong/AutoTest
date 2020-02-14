package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesOvertimeComparisonTest extends AbstractAnalyseTest {

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyWeekGranularityToHideSamePeriodComparison() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();

        AttributesBucket attributeBucket = analysisPage.getAttributesBucket();
        attributeBucket.changeGranularity(DateGranularity.WEEK_SUN_SAT);

        MetricsBucket metricsBucket = analysisPage.waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration();
        Screenshots.takeScreenshot(browser, "applyWeekGranularityToHidePopComparison-apply-week-granularity", getClass());

        assertFalse(analysisPage.getFilterBuckets().openDateFilterPickerPanel().isCompareTypeEnabled(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR),
                "same period last year comparison should be disabled");

        attributeBucket.changeGranularity(DateGranularity.MONTH);

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser, "applyWeekGranularityToHidePopComparison-apply-month-granularity", getClass());

        assertTrue(analysisPage.getFilterBuckets().openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .isCompareTypeEnabled(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR),
                "same period comparison state is not enabled after removing week granularity");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyWeekGranularityToHideCompareRecommendation() {
        initAnalysePage().addMetric(METRIC_SNAPSHOT_BOP);

        RecommendationContainer container = Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        container.getRecommendation(RecommendationStep.SEE_TREND).apply();

        AttributesBucket attributesBucket = analysisPage.waitForReportComputing().getAttributesBucket();
        attributesBucket.changeGranularity(DateGranularity.WEEK_SUN_SAT);

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser,
                "applyWeekGranularityToHideCompareRecommendation-hide-recommendation", getClass());

        assertFalse(isElementVisible(RecommendationContainer.LOCATOR, browser), "Recommendation container is not empty");

        attributesBucket.changeGranularity(DateGranularity.MONTH);

        analysisPage.waitForReportComputing();
        Screenshots.takeScreenshot(browser,
                "applyWeekGranularityToHideCompareRecommendation-show-recommendation", getClass());

        assertTrue(container.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation is not visible after changing to month granularity");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enableSamePeriodComparisonToHideWeekGranularity() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();

        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        assertFalse(analysisPage.waitForReportComputing().getAttributesBucket().getAllGranularities()
                .stream().anyMatch(DateGranularity.WEEK_SUN_SAT.toString()::equals), "week granularity is not hidden");

        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .applyCompareType(CompareTypeDropdown.CompareType.NOTHING);

        analysisPage.waitForReportComputing();

        assertTrue(analysisPage.waitForReportComputing().getAttributesBucket().getAllGranularities()
                .stream().anyMatch(DateGranularity.WEEK_SUN_SAT.toString()::equals), "week granularity is not displayed");
    }
}
