package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.fragments.reports.filter.AttributeFilterFragment;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.FilterFragment;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;

public class GoodSalesManipulationFilterReportTest extends GoodSalesAbstractTest {
    private static final By FILTER_PICKER_LOCATOR = By.className("newFilterPicker");

    private static final String REPORT_NAME = "Manipulation-filter";
    private static final String VARIABLE_NAME = "NVariable";

    private static final String METRIC_AMOUNT = "Amount";
    private static final String ATTR_STAGE_NAME = "Stage Name";
    private static final String ATTR_YEAR = "Year (Snapshot)";

    private String filterDescription;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-manipulation-filter-report-test";
    }

    @Test(dependsOnMethods = "createProject")
    public void createReport() {
        createReport(new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats(METRIC_AMOUNT)
                .withHows(ATTR_STAGE_NAME)
                .withHows(new HowItem(ATTR_YEAR, Position.TOP)),
                "Manipulation-filter-report");
    }

    @Test(dependsOnMethods = "createProject")
    public void addNumericVariable() {
        initVariablePage();
        variablePage.createVariable(new NumericVariable(VARIABLE_NAME).withDefaultNumber(500));
    }

    @Test(dependsOnMethods = {"createReport", "addNumericVariable"})
    public void addNumericVariableFilter() {
        Collection<String> variables = initReport().openFilterPanel()
                .clickAddFilter()
                .openPromptFilterFragment()
                .getVariables();
        assertThat(variables, not(hasItem(VARIABLE_NAME)));
    }

    @Test(dependsOnMethods = "createReport")
    public void editExistingFilter() {
        initReport().addFilter(FilterItem.Factory.createAttributeFilter(ATTR_YEAR, "2010"));
        waitForReportLoaded();

        filterDescription = ATTR_YEAR + " is 2010";
        assertThat(reportPage.getFilters(), hasItem(filterDescription));

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.<AttributeFilterFragment> openExistingFilter(filterDescription, FilterFragment.ATTRIBUTE_FILTER)
                .searchAndSelectAttributeValue("2011")
                .apply();
        waitForReportLoaded();

        filterDescription += ", 2011";
        assertThat(reportPage.getFilters(), hasItem(filterDescription));

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = "editExistingFilter")
    public void cancelAddingAnotherFilter() {
        initReport().openFilterPanel()
                .clickAddFilter()
                .openPromptFilterFragment()
                .cancel();
        waitForElementNotVisible(FILTER_PICKER_LOCATOR);

        reportPage.openFilterPanel()
                .clickAddFilter()
                .openPromptFilterFragment()
                .goBack();
        waitForElementVisible(FILTER_PICKER_LOCATOR, browser);
    }

    @Test(dependsOnMethods = "editExistingFilter")
    public void checkFilterKeepingAfterDeleteMetricOrAttribute() {
        initMetricPage();

        metricPage.openMetricDetailPage(METRIC_AMOUNT);
        waitForFragmentVisible(metricDetailPage).deleteMetric();

        initAttributePage();
        attributePage.initAttribute(ATTR_STAGE_NAME);
        attributeDetailPage.deleteAttribute();

        initReport();
        assertThat(reportPage.getFilters(), hasItem(filterDescription));
    }

    @Test(dependsOnMethods = "editExistingFilter")
    public void deleteExistingFilter() {
        initReport();

        boolean isFilterFocused = reportPage.hoverMouseToExistingFilter(filterDescription);
        assertTrue(isFilterFocused, "Filter description is not underlined when hover to");

        reportPage.deleteExistingFilter(filterDescription);
        assertThat(reportPage.getFilters(), not(hasItem(filterDescription)));
    }

    private ReportPage initReport() {
        initReportsPage();
        reportsPage.getReportsList().openReport(REPORT_NAME);
        waitForAnalysisPageLoaded(browser);

        return waitForFragmentVisible(reportPage);
    }

    private void waitForReportLoaded() {
        reportPage.getTableReport()
                .waitForReportLoading();
    }
}