package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertFalse;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricsBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-item")
    protected List<MetricConfiguration> metrics;

    public MetricConfiguration getMetricConfiguration(final String metric) {
        return waitForCollectionIsNotEmpty(metrics).stream()
            .filter(input -> metric.equals(input.getHeader()))
            .findFirst()
            .get();
    }

    public MetricConfiguration getLastMetricConfiguration() {
        return waitForCollectionIsNotEmpty(metrics).get(metrics.size()-1);
    }

    public WebElement get(final String name) {
        return metrics.stream()
            .filter(input -> name.equals(input.getHeader()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find metric: " + name))
            .getRoot();
    }

    public List<String> getItemNames() {
        return metrics.stream()
            .map(MetricConfiguration::getHeader)
            .collect(toList());
    }

    public List<String> getItemHeaderAndSequenceNumber() {
        return metrics.stream()
                .map(MetricConfiguration::getHeaderAndSequenceNumber)
                .collect(toList());
    }

    public MetricsBucket expandMeasureConfigurationPanel() {
        if (isConfigurationPanelCollapsed()) {
            getRoot().click();
            waitForElementAttributeNotContainValue(getRoot(), "class", "bucket-collapsed");
        }
        assertFalse(isConfigurationPanelCollapsed(), "Measure Configuration Panel Bucket should be expanded");
        return this;
    }

    private boolean isConfigurationPanelCollapsed() {
        return getRoot().getAttribute("class").contains("bucket-collapsed");
    }
}
