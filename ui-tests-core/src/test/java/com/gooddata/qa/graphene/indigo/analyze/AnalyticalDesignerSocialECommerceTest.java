package com.gooddata.qa.graphene.indigo.analyze;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.ReportType;

public class AnalyticalDesignerSocialECommerceTest extends AdLegacyAbstractTest {

    private static final String ORDERS = "% Orders";
    private static final String CHECKOUTS = "% Checkouts";
    private static final String ENGAGEMENT = "Engagement";
    private static final String FOLLOWERS = "Followers";
    private static final String DEVICE = "Device";
    private static final String CHANNEL = "Channel";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/SocialECommerceDemo/1";
        projectTitle = "Indigo-Social-E-Commerce-test";

        metric1 = ORDERS;
        metric2 = CHECKOUTS;
        metric3 = ENGAGEMENT;
        metric4 = FOLLOWERS;
        attribute1 = DEVICE;
        attribute2 = CHANNEL;
        attribute3 = DEVICE;

        notAvailableAttribute = "Company";
    }

    @Test(dependsOnGroups = {"init"}, groups = {EXPLORE_PROJECT_DATA_GROUP})
    public void exploreAttribute() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DEVICE).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Browser\n")
                .append("Mobile\n")
                .append("Tablet\n");
        assertEquals(analysisPage.getAttributeDescription(DEVICE), expected.toString());
    }

    @Test(dependsOnGroups = {"init"}, groups = {EXPLORE_PROJECT_DATA_GROUP})
    public void exploreMetric() {
        initAnalysePage();

        StringBuilder expected = new StringBuilder(ORDERS).append("\n")
                .append("Field Type\n")
                .append("Metric\n")
                .append("Defined As\n")
                .append("select Orders/(Visits+Cart Additions+Checkouts+Orders)\n");
        assertEquals(analysisPage.getMetricDescription(ORDERS), expected.toString());
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void filterOnAttribute() {
        filterOnAttribute(DEVICE + ": Mobile, Tablet", "Mobile", "Tablet");
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnGroups = {"init"}, groups = {CHART_REPORT_GROUP}, enabled = false)
    public void verifyChartReport() {
        ReportDefinition reportDefinition = new ReportDefinition()
            .withMetrics(ORDERS)
            .withCategories(DEVICE)
            .withType(ReportType.BAR_CHART)
            .withFilters(DATE);

        verifyChartReport(reportDefinition, Arrays.asList(Arrays.asList(DEVICE, "Tablet"),
                Arrays.asList(ORDERS, "11%")));
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnGroups = {"init"}, groups = {TABLE_REPORT_GROUP}, enabled = false)
    public void verifyTableReportContent() {
        ReportDefinition reportDefinition = new ReportDefinition()
            .withMetrics("Engagement")
            .withCategories(DEVICE)
            .withType(ReportType.TABLE)
            .withFilters("Campaign", CHANNEL, DATE);

        verifyTableReportContent(reportDefinition, Arrays.asList("DEVICE", "ENGAGEMENT"),
                Arrays.asList(Arrays.asList("Browser", "18,504.00"),
                Arrays.asList("Mobile", "16,345.00"),
                Arrays.asList("Tablet", "15,872.00")));
    }
}
