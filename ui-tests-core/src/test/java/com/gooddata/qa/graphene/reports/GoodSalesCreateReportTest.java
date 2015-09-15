package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.name;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.utils.http.RestUtils;

public class GoodSalesCreateReportTest extends GoodSalesAbstractTest {

    private static final String INAPPLICABLE_ATTR_MESSAGE = "Attributes that are unavailable may lead to"
            + " nonsensical results if used to break down the metric(s) in this report."
            + " To select an unavailable attribute anyway, hold Shift while clicking its name.";

    private static final String INVALID_DATA_REPORT_MESSAGE =
            "Report not computable due to improper metric definition";

    private static final String WRONG_STATE_FILTER_MESSAGE = "Please confirm or cancel your changes in the"
            + " Slice and Dice dialog box before proceeding.";

    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    private static final String ACTIVITY_TYPE = "Activity Type";
    private static final String PRODUCT = "Product";

    private static final String REPORT_NOT_COMPUTABLE_MESSAGE =
            "Report not computable due to improper metric definition";

    private static final String ATTRIBUTE_LIMIT_MESSAGE = "You have reached the limit of attributes in report.";

    private static final String METRIC_LIMIT_MESSAGE = "You have reached the limit of metrics in report.";

    private Project project;
    private MetadataService mdService;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-create-report";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createReportWithInapplicableAttribute() {
        initReportCreation();

        reportPage.initPage()
            .openWhatPanel()
            .selectMetric(NUMBER_OF_ACTIVITIES)
            .openHowPanel();
        sleepTightInSeconds(1);
        reportPage.selectAttribute(PRODUCT);

        assertThat(getErrorMessage(), startsWith(INAPPLICABLE_ATTR_MESSAGE));
        assertThat(reportPage.getTooltipMessageOfAttribute(PRODUCT), equalTo(
                "Product is unavailable due to the metric(s) in this report. Use Shift + click to override."));

        String invalidDataReportMessage = reportPage.selectInapplicableAttribute(PRODUCT)
            .doneSndPanel()
            .getInvalidDataReportMessage();

        assertThat(invalidDataReportMessage, equalTo(INVALID_DATA_REPORT_MESSAGE));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void tryToAddFilterInWrongState() {
        initReportCreation();

        reportPage.initPage()
            .openWhatPanel()
            .selectMetric(NUMBER_OF_ACTIVITIES);
        sleepTightInSeconds(1);
        reportPage.openFilterPanel();

        String wrongStateFilterMessage = waitForElementVisible(cssSelector(".c-infoDialog .message"), browser)
                .getText().trim();
        assertThat(wrongStateFilterMessage, equalTo(WRONG_STATE_FILTER_MESSAGE));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void cancelCreatingReport() {
        initReportCreation();

        reportPage.initPage()
            .openWhatPanel()
            .selectMetric(NUMBER_OF_ACTIVITIES)
            .openHowPanel()
            .selectAttribute(ACTIVITY_TYPE)
            .doneSndPanel()
            .selectReportVisualisation(ReportTypes.TABLE)
            .clickSaveReport()
            .cancelCreateReportInDialog();

        waitForFragmentVisible(reportPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void switchBetweenFolderAndTagView() {
        initReportCreation();

        reportPage.initPage()
            .openHowPanel()
            .switchViewToTags();
        sleepTightInSeconds(2);

        assertThat(reportPage.loadAllViewGroups(),
                equalTo(asList("date", "day", "eu", "month", "quarter", "week", "year")));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void loadAllAttributesInFilterPanel() {
        initReportCreation();

        reportPage.initPage()
            .openHowPanel()
            .selectAttribute("Date (Snapshot)");

        waitForElementVisible(cssSelector(".s-btn-filter_this_attribute"), browser).click();
        waitForElementVisible(cssSelector(".switchLabel ~ .hyperlinkOn a"), browser).click();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createReportWithNonDefaultAttributeLabel() {
        initReportCreation();

        assertThat(reportPage.initPage()
            .openHowPanel()
            .selectAttribute("Stage Name")
            .changeDisplayLabel("Order")
            .doneSndPanel()
            .getTableReport()
            .getAttributeElements(), equalTo(asList("101", "102", "103", "104", "105", "106", "107", "108")));

    }

    @Test(dependsOnMethods = {"createProject"})
    public void createFilterBeforeAddingMetricAttribute() {
        initReportCreation();

        reportPage.initPage()
            .addFilter(FilterItem.Factory.createListValuesFilter(ACTIVITY_TYPE, "Email"))
            .openHowPanel()
            .selectAttribute(ACTIVITY_TYPE)
            .doneSndPanel();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void testFilterNotRemoveWhenAttributeRemovedFromHow() {
        initReportCreation();

        assertThat(reportPage.initPage()
            .openHowPanel()
            .selectAttribute(ACTIVITY_TYPE)
            .doneSndPanel()
            .addFilter(FilterItem.Factory.createListValuesFilter(ACTIVITY_TYPE, "Email"))
            .getFilters().size(), equalTo(1));

        assertThat(reportPage.openHowPanel()
            .deselectAttribute(ACTIVITY_TYPE)
            .doneSndPanel()
            .getFilters().size(), equalTo(1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initGoodDataClient() {
        GoodData goodDataClient = getGoodDataClient();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        mdService = goodDataClient.getMetadataService();
    }

    @Test(dependsOnMethods = {"initGoodDataClient"})
    public void createReportNotComputable() throws ParseException, JSONException, IOException {
        String metricName = "test-metric";
        String reportName = "Test Report";

        Fact amount = mdService.getObj(project, Fact.class, Restriction.title("Amount"));
        Metric testMetric = mdService.createObj(project,
                new Metric(metricName, "SELECT SUM([" + amount.getUri() + "])", "#,##0"));
        Attribute yearSnapshot = mdService.getObj(project, Attribute.class, Restriction.title("Year (Snapshot)"));

        ReportDefinition definition = GridReportDefinitionContent.create(
                        reportName,
                        singletonList("metricGroup"),
                        singletonList(new AttributeInGrid(yearSnapshot.getDefaultDisplayForm().getUri())),
                        singletonList(new GridElement(testMetric.getUri(), metricName)));
        definition = mdService.createObj(project, definition);
        mdService.createObj(project, new Report(definition.getTitle(), definition));

        RestUtils.changeMetricExpression(getRestApiClient(), testMetric.getUri(),
                "SELECT ["+ amount.getUri() + "]");
        initReportsPage();
        reportsPage.getReportsList().openReport(reportName);
        waitForAnalysisPageLoaded(browser);

        assertThat(reportPage.getInvalidDataReportMessage(),
                equalTo(REPORT_NOT_COMPUTABLE_MESSAGE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLimitAttributes() {
        initReportCreation();

        reportPage.initPage()
            .openHowPanel();
        asList("Account", "Activity", "Activity Type", "Department", "Forecast Category", "Is Active?",
                "Is Closed?", "Is Task?", "Is Won?", "Opp. Snapshot", "Opportunity", "Priority", "Product",
                "Region", "Sales Rep", "Stage History", "Stage Name", "Status", "Date (Activity)",
                "Date (Closed)").stream().forEach(reportPage::selectAttribute);

        reportPage.selectAttribute("Date (Snapshot)");
        assertThat(getErrorMessage(), startsWith(ATTRIBUTE_LIMIT_MESSAGE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLimitMetrics() {
        initReportCreation();

        reportPage.initPage()
            .openWhatPanel();
        asList("# of Activities", "# of Lost Opps.", "# of Open Opps.", "# of Opportunities",
                "# of Opportunities [BOP]", "# of Won Opps.", "% of Goal", "Amount", "Avg. Amount", "Avg. Won",
                "Best Case", "Days until Close", "Expected", "Lost", "Expected + Won", "Quota", "Stage Duration",
                "Stage Velocity", "Win Rate", "Won").stream().forEach(reportPage::selectMetric);

        reportPage.selectMetric("Expected + Won vs. Quota");
        assertThat(getErrorMessage(), startsWith(METRIC_LIMIT_MESSAGE));
    }

    private String getErrorMessage() {
        try {
            return waitForElementVisible(name("reportEditorForm"), browser).getText().trim();
        } finally {
            waitForElementVisible(cssSelector("[name=reportEditorForm] .s-btn-close"), browser).click();
        }
    }
}
