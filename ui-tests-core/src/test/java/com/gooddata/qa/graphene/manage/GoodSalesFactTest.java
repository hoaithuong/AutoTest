package com.gooddata.qa.graphene.manage;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;

@Test(groups = {"GoodSalesFacts"}, description = "Tests for GoodSales project (view and edit fact functionality) in GD platform")
public class GoodSalesFactTest extends ObjectAbstractTest {

    private String factFolder;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-fact";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "object-tests" })
    public void initialize() throws JSONException {
        name = "Amount";
        this.factFolder = "Stage History";
        description = "Graphene test on view and modify Fact";
        tagName = "Graphene-test";
    }

    @Test(dependsOnMethods = {"initialize"}, groups = { "object-tests" })
    public void factAggregationsTest() {
        initObject(name);
        for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
            factDetailPage.createSimpleMetric(metricType, name);
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = { "object-tests" })
    public void changeFactFolderTest() {
        initObject(name);
        factDetailPage.changeFactFolder(factFolder);
    }

    @Override
    public void initObject(String factName) {
        initFactPage();
        factsTable.selectObject(factName);
    }
}