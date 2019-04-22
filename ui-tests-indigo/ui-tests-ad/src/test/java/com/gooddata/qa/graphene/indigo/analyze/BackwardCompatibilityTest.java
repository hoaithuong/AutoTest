package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class BackwardCompatibilityTest extends AbstractProjectTest {

    private static final String STAGING3 = "staging3.intgdc.com";
    private static final String STAGING2 = "staging2.intgdc.com";
    private static final String STAGING = "staging.intgdc.com";

    @Override
    public void initProperties() {
        super.initProperties();
        log.info("these tests are meaningful when performing testing on created project");
        testParams.setReuseProject(true);
        switch(testParams.getHost()) {
            case STAGING3:
                testParams.setProjectId("kf8tobvrdszda3xocsptnjdjf7xxyexs");
                return;
            case STAGING2:
                testParams.setProjectId("u7yus6202jxbsbjl5ijba87x00n0swmf");
                return;
            case STAGING:
                testParams.setProjectId("nqixedz4xxwsy461sghtyy9grz084288");
                return;
            default:
                System.out.println("Test just runs on staging, staging2 and staging3");
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testWithPoP() {
        if (!isOnStagingCluster()) {
            throw new SkipException("Test just runs on Staging, Staging2 and Staging3");
        }
        final String insightUri = format("analyze/#/%s/80514/edit", testParams.getProjectId());
        final String avgAmount = "Amount Tr [Avg]";
        final String avgAmountAgo = "Amount Tr [Avg] - SP year ago";
        openUrl(insightUri);
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(avgAmountAgo, avgAmount));
        analysisPage.waitForReportComputing();
        assertThat(analysisPage.getPivotTableReport().getHeaders(), hasItems(avgAmountAgo, avgAmount));
    }

    @Test(dependsOnGroups = "createProject")
    public void openSavedInsight() {
        if (!isOnStagingCluster()) {
            throw new SkipException("Test just runs on Staging, Staging2 and Staging3");
        }
        final List<String> insights = asList("3123", "date range", "headline_1", "pop_renamed", "pop_cũ + filter att",
                "area", "pie chart  too many", "RT_Pie: Restricted with CA based on other CA");

        AnalysisPage analysisPage = initAnalysePage();
        for (String insight: insights) {
            analysisPage.openInsight(insight).waitForReportComputing();
            assertFalse(analysisPage.getPageHeader().isSaveButtonEnabled(),
                    "Save button should be disabled with saved insight");
        }
    }

    private boolean isOnStagingCluster() {
        if (testParams.getHost().equals(STAGING3) || testParams.getHost().equals(STAGING2) ||
                testParams.getHost().equals(STAGING)) {
            return true;
        }
        System.out.println("Test just runs on Staging, Staging2 and Staging3");
        return false;
    }
}
