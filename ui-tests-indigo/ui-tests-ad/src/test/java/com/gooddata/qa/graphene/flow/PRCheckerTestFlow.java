package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.analyze.e2e.*;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.io.IOException;

public class PRCheckerTestFlow {

    public static void main(String[] args) throws IOException {
        TestsRegistry.getInstance()
            .register(EmptyCatalogueTest.class)
            .register(RecommendationsWithoutDateDimensionTest.class)
            .register(AttributeFiltersTest.class)
            .register(BucketsTest.class)
            .register(ErrorStatesTest.class)
            .register(TableTest.class)
            .register(UndoTest.class)
            .register(AttributeBasedMetricsTest.class)
            .register(FactBasedMetricsTest.class)
            .register(StackedChartsTest.class)
            .register(ResetButtonTest.class)
            .register("testng-ad-e2e-metrics-test.xml")
            .register("testng-ad-e2e-visualization-test.xml")
            .register("testng-ad-e2e-recommendation-test.xml")
            .register("testng-ad-e2e-catalogue-test.xml")
            .register("testng-ad-e2e-date-test.xml")
            .toTextFile();
    }
}
