package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

import java.util.Arrays;

public class AttributeFiltersTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Filters-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_reset_search_results_after_closing() {
        beforeEach();

        WebElement filter = analysisPage.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE);
        filter.click();

        AttributeFilterPickerPanel panel = AttributeFilterPickerPanel.getInstance(browser);
        panel.searchForText("asdf");

        sleepTightInSeconds(1);
        waitForElementVisible(cssSelector(".gd-list-noResults"), browser);

        panel.discard();

        filter.click();

        sleepTightInSeconds(3); // need buffer time for Selenium to refresh element values
        assertEquals(AttributeFilterPickerPanel.getInstance(browser).getItemNames().size(), 4);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_be_possible_to_add_and_remove_attribute_from_filters_bucket() {
        beforeEach();

        analysisPage.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE);

        // try to drag a duplicate attribute filter
        WebElement attribute = analysisPage.getCatalogPanel()
                .searchAndGet(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE);
        assertTrue(analysisPage.tryToDrag(attribute, analysisPage.getStacksBucket().getInvitation())
            .removeFilter(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void should_not_allow_moving_other_buckets_items_to_filters_bucket() {
        beforeEach();

        assertTrue(analysisPage.addAttribute(ATTR_ACTIVITY_TYPE)
            .removeFilter(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .isEmpty());

        assertTrue(analysisPage.tryToDrag(analysisPage.getAttributesBucket().getFirst(),
                analysisPage.getFilterBuckets().getInvitation())
            .getFilterBuckets()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"disabling-Apply-button"}, description = "covered by TestCafe")
    public void should_disable_apply_button_if_nothing_changed() {
        assertTrue(beforeEachDisablingApplyButton()
                .getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"disabling-Apply-button"}, description = "covered by TestCafe")
    public void should_disable_apply_button_if_nothing_is_selected() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.uncheckAllCheckbox();
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"disabling-Apply-button"}, description = "covered by TestCafe")
    public void should_disable_apply_button_if_everything_is_unselected() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.uncheckAllCheckbox();
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"disabling-Apply-button"}, description = "covered by TestCafe")
    public void should_not_disable_apply_button_if_selection_is_inverted() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.select("Email");

        analysisPage.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE).click();
        panel.uncheckAllCheckbox();
        panel.searchForText("Email");
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"disabling-Apply-button"}, description = "covered by TestCafe")
    public void should_not_disable_apply_button_if_single_item_is_filtered() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();

        panel.uncheckAllCheckbox();
        panel.selectItem("Email");
        assertFalse(panel.getApplyButton().getAttribute("class").contains("disabled"),
                "Apply button should be disabled");

        panel.getApplyButton().click();
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText(ATTR_ACTIVITY_TYPE)), Arrays.asList(ATTR_ACTIVITY_TYPE, "Email"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"disabling-Apply-button"}, description = "covered by TestCafe")
    public void should_disable_apply_button_if_selection_is_in_different_order() {
        AttributeFilterPickerPanel panel = beforeEachDisablingApplyButton();
        panel.select("Email", "In Person Meeting");

        analysisPage.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE).click();
        panel.uncheckAllCheckbox();
        panel.searchForText("Email");
        panel.searchForText("In Person Meeting");
        assertTrue(panel.getApplyButton()
                .getAttribute("class")
                .contains("disabled"));
    }

    private void beforeEach() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addFilter(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing();
    }

    private AttributeFilterPickerPanel beforeEachDisablingApplyButton() {
        beforeEach();

        analysisPage.getFilterBuckets().getFilter(ATTR_ACTIVITY_TYPE).click();
        return AttributeFilterPickerPanel.getInstance(browser);
    }
}
