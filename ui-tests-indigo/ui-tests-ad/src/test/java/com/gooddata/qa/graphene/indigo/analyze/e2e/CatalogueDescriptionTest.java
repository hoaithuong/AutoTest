package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class CatalogueDescriptionTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Catalogue-Description-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_metric_info_bubble_when_hovering_the_info_icon() {
        assertTrue(initAnalysePage().getCatalogPanel()
                .getMetricDescription(METRIC_NUMBER_OF_ACTIVITIES)
                .contains("SELECT COUNT(Activity)"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_attribute_info_bubble_when_hovering_the_info_icon() {
        assertTrue(initAnalysePage().getCatalogPanel()
                .getAttributeDescription(ATTR_ACTIVITY_TYPE)
                .contains("Email"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_dataset_of_fact() {
        assertTrue(initAnalysePage().getCatalogPanel()
                .getFactDescription(FACT_AMOUNT)
                .contains("OpportunitySnapshot"));
    }
}
