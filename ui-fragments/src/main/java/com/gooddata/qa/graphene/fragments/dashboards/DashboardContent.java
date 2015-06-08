package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.CssUtils.simplifyText;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.AbstractReport;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DashboardContent extends AbstractFragment {

    @FindBy(css = ".c-collectionWidget:not(.gdc-hidden) .yui3-c-reportdashboardwidget")
    private List<WebElement> reports;

    @FindBy(css = ".geo-content-wrapper")
    private List<DashboardGeoChart> geoCharts;

    @FindBy(css = ".c-collectionWidget:not(.gdc-hidden) .yui3-c-filterdashboardwidget")
    private List<WebElement> filters;

    private static final By REPORT_TITLE_LOCATOR = By.cssSelector(".yui3-c-reportdashboardwidget-reportTitle");

    public int getNumberOfReports() {
        return reports.size();
    }

    private static String REPORT_IMAGE_LOCATOR = "//div[contains(@class, '${reportName}')]//img";

    public <T extends AbstractReport> T getReport(int reportIndex, Class<T> clazz) {
        return createPageFragment(clazz, reports.get(reportIndex));
    }

    public <T extends AbstractReport> T getReport(final String name, Class<T> clazz) {
        return createPageFragment(clazz, Iterables.find(reports, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                WebElement title = input.findElement(REPORT_TITLE_LOCATOR).findElement(BY_LINK);
                return name.equals(title.getAttribute("title"));
            }
        }));
    }

    public <T extends AbstractReport> T getLatestReport(Class<T> clazz) {
        return getReport(reports.size() - 1, clazz);
    }

    public List<DashboardGeoChart> getGeoCharts() {
        return geoCharts;
    }

    public DashboardGeoChart getGeoChart(int geoChartIndex) {
        return getGeoCharts().get(geoChartIndex);
    }

    public WebElement getImageFromReport(String reportName){
        By eleBy =  By.xpath(REPORT_IMAGE_LOCATOR.replace("${reportName}", simplifyText(reportName)));
        return waitForElementVisible(eleBy, browser);
     }

    public List<FilterWidget> getFilters() {
        return Lists.newArrayList(Collections2.transform(filters, new Function<WebElement, FilterWidget>() {
            @Override
            public FilterWidget apply(WebElement input) {
                return createPageFragment(FilterWidget.class, input);
            }
        }));
    }

    public FilterWidget getFirstFilter() {
        return createPageFragment(FilterWidget.class, filters.get(0));
    }

    public FilterWidget getFilterWidget(final String condition) {
        WebElement filter = Iterables.find(filters, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains("s-" + condition);
            }
        }, null);

        if (filter == null) {
            return null;
        }

        return createPageFragment(FilterWidget.class, filter);
    }
}
