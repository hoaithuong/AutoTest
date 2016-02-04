package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.openqa.selenium.Point;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class RecommendationsWithoutDateDimensionTest extends AbstractAdE2ETest {

    private static final String MAQL_PATH = "/customer/customer.maql";
    private static final String UPLOADINFO_PATH = "/customer/upload_info.json";
    private static final String CSV_PATH = "/customer/customer.csv";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = "";
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Recommendations-Without-Date-Dimension-E2E-Test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupProject"})
    public void setupProject() throws JSONException, IOException, URISyntaxException {
        if (testParams.isReuseProject()) {
            log.info("No need to setup data in reuse project.");
            return;
        }

        setupMaql(MAQL_PATH);
        setupData(CSV_PATH, UPLOADINFO_PATH);
    }

    @Test(dependsOnGroups = {"init"})
    public void trending_recommendation_should_not_be_visible() {
        initAnalysePageByUrl();

        assertFalse(analysisPage.getCataloguePanel().getFieldNamesInViewPort().contains(DATE));
        analysisPage.addMetric(AMOUNT, FieldType.FACT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-recommendation-comparison"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void metric_with_period_recommendation_should_not_be_visible() {
        initAnalysePageByUrl();

        analysisPage.addMetric(AMOUNT, FieldType.FACT)
            .addAttribute("id")
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-recommendation-contribution"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void trending_shortcut_should_not_appear() {
        initAnalysePageByUrl();

        analysisPage.startDrag(analysisPage.getCataloguePanel().searchAndGet(AMOUNT, FieldType.FACT));

        try {
            assertTrue(isElementPresent(cssSelector(".s-recommendation-metric-canvas"), browser));
            assertFalse(isElementPresent(cssSelector(".s-recommendation-metric-over-time-canvas"), browser));
        } finally {
            analysisPage.stopDrag(new Point(-1, -1));
        }
    }
}
