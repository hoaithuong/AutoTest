package com.gooddata.qa.graphene.schedules;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.report.ReportRestRequest;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.BOTTOM;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

public class GoodSalesEmailScheduleSavedViewEmbeddedDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final LocalDate now = LocalDate.now(ZoneId.of("America/Los_Angeles"));
    private static final int currentYear = now.getYear();
    private static final String currentDate = now.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    private static final String FIRST_TAB = "First Tab";
    private static final String NUMBER_OF_PAGE = "Page 1/1";
    private static final String VARIABLE_FPRODUCT = "FProduct";
    private static final String FILTER_FPRODUCT = "FPRODUCT";
    private static final String FILTER_DATE_DIMENSION = "DATE DIMENSION (SNAPSHOT)";
    private static final String FILTER_STAGE_NAME = "STAGE NAME";
    private static final String EXPLORER = "Explorer";
    private static final String GRAMMAR_PLUS = "Grammar Plus";
    private String firstDashboard = "First Dashboard";
    private String secondDashboard = "Second Dashboard";
    private String thirdDashboard = "Third Dashboard";
    private String firstReport = "First Report";
    private String secondReport = "Second Report";
    private String thirdReport = "Third Report";
    private ReportRestRequest reportRestRequest;
    private DashboardRestRequest dashboardRestRequest;
    private String promptFilterUri;
    private CommonRestRequest commonRestRequest;

    @BeforeClass
    public void setUp() {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        firstDashboard = firstDashboard + identification;
        secondDashboard = secondDashboard + identification;
        thirdDashboard = thirdDashboard + identification;

        attachmentsDirectory =
                new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Override
    protected void customizeProject() throws Throwable {
        reportRestRequest = new ReportRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId());
        getMetricCreator().createAmountMetric();
        //To make sure that select dashboard method can work
        initDashboardHasFilter("Zero Dashboard", "Zero Report");
    }

    @Test(dependsOnGroups = "createProject", groups = "schedules")
    public void signInImapUser() throws JSONException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void removeAttribute() throws IOException, MessagingException {
        initDashboardHasFilter(firstDashboard, firstReport);
        initDashboardsPage().selectDashboard(firstDashboard).editDashboard().turnSavedViewOption(true);
        dashboardsPage.saveDashboard();
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, "Short List").getPreviewURI();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        waitForScheduleMessages(firstDashboard, 1);

        ReportDefinition reportDefinition = GridReportDefinitionContent.create(firstReport,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        reportRestRequest.updateReport(firstReport, reportDefinition);

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(firstDashboard, 2), 1).split("\n"));
        //verify report
        assertThat(contents, hasItems(firstReport, "Stage Name Amount", "Short List $5,612,062.60"));
        //verify filter
        assertThat(contents, hasItems(FILTER_FPRODUCT, "All", FILTER_STAGE_NAME, "Short List",
                FILTER_DATE_DIMENSION, "2010 - 2012"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", FIRST_TAB, currentDate)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "removeAttribute")
    public void addAttribute() throws IOException, MessagingException {
        ReportDefinition reportDefinition = GridReportDefinitionContent.create(firstReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        reportRestRequest.updateReport(firstReport, reportDefinition);
        initDashboardsPage().selectDashboard(firstDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_STAGE_NAME, "Short List").getPreviewURI();

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(firstDashboard, 3), 2).split("\n"));
        //verify report
        assertThat(contents, hasItems(firstReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, hasItems(FILTER_FPRODUCT, "All", FILTER_STAGE_NAME, "Short List",
                FILTER_DATE_DIMENSION, "2010 - 2012"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", FIRST_TAB, currentDate)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void removeReportFilter() throws IOException, MessagingException {
        initDashboardHasFilter(secondDashboard, secondReport);
        initDashboardsPage().selectDashboard(secondDashboard).editDashboard().turnSavedViewOption(true);
        dashboardsPage.saveDashboard();
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_PRODUCT, EXPLORER).getPreviewURI();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        waitForScheduleMessages(secondDashboard, 1);

        ReportDefinition reportDefinition = GridReportDefinitionContent.create(secondReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        reportRestRequest.updateReport(secondReport, reportDefinition);

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(secondDashboard, 2), 1).split("\n"));
        //verify report
        assertThat(contents, hasItems(secondReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, hasItems(FILTER_FPRODUCT, EXPLORER, FILTER_STAGE_NAME, "All",
                FILTER_DATE_DIMENSION, "2010 - 2012"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", FIRST_TAB, currentDate)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "removeReportFilter")
    public void addReportFilter() throws IOException, MessagingException {
        ReportDefinition reportDefinition = GridReportDefinitionContent.create(secondReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(new Filter(format("[%s]", promptFilterUri))));
        reportRestRequest.updateReport(secondReport, reportDefinition);
        initDashboardsPage().selectDashboard(secondDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_PRODUCT, EXPLORER).getPreviewURI();

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(secondDashboard, 3), 2).split("\n"));
        //verify report
        assertThat(contents, hasItems(secondReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, hasItems(FILTER_FPRODUCT, EXPLORER, FILTER_STAGE_NAME, "All",
                FILTER_DATE_DIMENSION, "2010 - 2012"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", FIRST_TAB, currentDate)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void removeDashboardFilter() throws IOException, MessagingException {
        initDashboardHasFilter(thirdDashboard, thirdReport);
        initDashboardsPage().selectDashboard(thirdDashboard).editDashboard().turnSavedViewOption(true);
        dashboardsPage.saveDashboard();
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010")
                .selectFilterAttribute(ATTR_STAGE_NAME, "Short List")
                .getPreviewURI();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        waitForScheduleMessages(thirdDashboard, 1);

        initEmbeddedDashboard().editDashboard();
        DashboardEditWidgetToolbarPanel
                .removeWidget(embeddedDashboard.getFilterWidgetByName(FILTER_DATE_DIMENSION).getRoot(), browser);
        embeddedDashboard.saveDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(thirdDashboard, 2), 1).split("\n"));
        //verify report
        assertThat(contents, hasItems(thirdReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, hasItems(FILTER_FPRODUCT, "All", FILTER_STAGE_NAME, "All"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", FIRST_TAB, currentDate)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "removeDashboardFilter")
    public void addDashboardFilter() throws IOException, MessagingException {
        initDashboardsPage().selectDashboard(thirdDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010").getPreviewURI();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_YEAR_SNAPSHOT)
                .saveDashboard();
        embeddedDashboard.showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(thirdDashboard, 3), 2).split("\n"));
        //verify report
        assertThat(contents, hasItems(thirdReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, hasItems(FILTER_FPRODUCT, "All", FILTER_STAGE_NAME, "All",
                ATTR_YEAR_SNAPSHOT.toUpperCase(), "All"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", FIRST_TAB, currentDate)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    private void initDashboardHasFilter(String titleDashboard, String titleReport) throws IOException {
        VariableRestRequest request = new VariableRestRequest(getAdminRestClient(), testParams.getProjectId());
        promptFilterUri = request.createFilterVariable(VARIABLE_FPRODUCT,
                request.getAttributeByTitle(ATTR_PRODUCT).getUri(),
                asList(EXPLORER, GRAMMAR_PLUS));
        FilterItemContent stageNameFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent dateFilterContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                2010 - currentYear, 2012 - currentYear);
        FilterItemContent promptFilterContent = createMultipleValuesFilterBy(promptFilterUri);


        createReport(GridReportDefinitionContent.create(titleReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));

        JSONObject dashboardContent = new Dashboard()
                .setName(titleDashboard)
                .addTab(initTab(FIRST_TAB, titleReport, asList(
                        Pair.of(promptFilterContent, BOTTOM),
                        Pair.of(stageNameFilterContent, TOP_RIGHT),
                        Pair.of(dateFilterContent, RIGHT))))
                .addFilter(promptFilterContent)
                .addFilter(stageNameFilterContent)
                .addFilter(dateFilterContent)
                .getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);
    }

    private Tab initTab(String name, String report, List<Pair<FilterItemContent, ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream()
                .map(this::buildFilterItem)
                .collect(Collectors.toList());
        ReportItem reportItem = createReportItem(getReportByTitle(report).getUri(),
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));
        return new Tab().setTitle(name).addItems(Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private FilterItem buildFilterItem(Pair<FilterItemContent, ItemPosition> filterItem) {
        FilterItem filterItemContent = new FilterItem().setContentId(filterItem.getLeft().getId());
        filterItemContent.setPosition(filterItem.getRight());
        return filterItemContent;
    }
}
