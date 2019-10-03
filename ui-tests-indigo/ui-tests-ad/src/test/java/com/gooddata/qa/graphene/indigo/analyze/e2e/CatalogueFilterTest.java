package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class CatalogueFilterTest extends AbstractAdE2ETest {

    private String metrics = ".type-metric";
    private String attributes = ".type-attribute";
    private String facts = ".type-fact";
    private String dates = ".type-date";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Catalogue-Filter-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shows_all_items_for_all_data_filter() {
        initAnalysePage().getCataloguePanel().filterCatalog(CatalogFilterType.ALL);
        expectVisible(dates, attributes, metrics, facts);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shows_only_metrics_and_facts_for_metrics_filter() {
        initAnalysePage().getCataloguePanel().filterCatalog(CatalogFilterType.MEASURES);
        expectVisible(metrics, facts);
        expectHidden(dates, attributes);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shows_only_date_and_attributes_for_attributes_filter() {
        initAnalysePage().getCataloguePanel().filterCatalog(CatalogFilterType.ATTRIBUTES);
        expectVisible(dates, attributes);
        expectHidden(metrics, facts);
    }

    private void expectVisible(String... fields) {
        Stream.of(fields).forEach(field ->
            assertTrue(isElementPresent(cssSelector(".s-catalogue " + field), browser)));
    }

    private void expectHidden(String... fields) {
        Stream.of(fields).forEach(field ->
            assertFalse(isElementPresent(cssSelector(".s-catalogue " + field), browser)));
    }
}
