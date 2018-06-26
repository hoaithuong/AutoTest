package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

import java.util.Arrays;

public class NoncommonDateTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Noncommon-Date-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void date_dimension_in_chart_should_reflect_currently_selected_dimension() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        analysisPage.waitForReportComputing();
        assertThat(waitForElementVisible(cssSelector(".highcharts-xaxis-title tspan"), browser).getText(),
                containsString("Created"));

        analysisPage.getAttributesBucket()
            .changeDateDimension("Activity");
        analysisPage.waitForReportComputing();
        assertThat(waitForElementVisible(cssSelector(".highcharts-xaxis-title tspan"), browser).getText(),
                containsString("Activity"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enable_date_dimension_selects_correctly() {
        WebElement dateFilter = initAnalysePage().addDateFilter()
            .getFilterBuckets()
            .getDateFilter();

        dateFilter.click();
        assertFalse(isElementPresent(cssSelector(".adi-date-dataset-select-dropdown button.disabled"), browser));
        dateFilter.click();

        analysisPage.addDate();
        assertFalse(isElementPresent(cssSelector(".s-date-dataset-switch button.disabled"), browser));

        // date dimension picker in select is now disabled
        dateFilter.click();
        assertTrue(isElementPresent(cssSelector(".adi-date-dataset-select-dropdown button.disabled"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void keeps_date_dimensions_in_categories_in_sync_with_the_filter() {
        initAnalysePage().addDateFilter()
            .addDate()
            .getAttributesBucket()
            .changeDateDimension("Created");

        assertThat(parseFilterText(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText()),
                contains("Created", "All time"));

        // update filter to last quarter
        analysisPage.getFilterBuckets()
            .configDateFilter("Last quarter");
        assertThat(parseFilterText(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText()),
                contains("Created", "Last quarter"));

        // switch date dimension to Foundation Date
        analysisPage.getAttributesBucket()
            .changeDateDimension("Activity");

        // check that filter is kept switched to last quarter, but in switched date dimension
        assertThat(parseFilterText(waitForElementVisible(cssSelector(".s-filter-button span"), browser).getText()),
                contains("Activity", "Last quarter"));
    }
}
