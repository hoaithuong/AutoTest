package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;

public class EmbeddedDashboard extends DashboardsPage {

    public static final By LOCATOR = By.id("root");

    private static final By BY_DASHBOARD_LOADED = By.cssSelector("#p-projectDashboardPage.s-displayed, .s-dashboardLoaded");
    private static final By BY_EDIT_BUTTON = By.className("s-editButton");

    public boolean isEditButtonVisible() {
        return isElementVisible(BY_EDIT_BUTTON, getRoot());
    }

    public static void waitForDashboardLoaded(SearchContext searchContext) {
        waitForElementVisible(BY_DASHBOARD_LOADED, searchContext);
    }

    public EmbeddedReportPage openEmbeddedReportPage() {
        editDashboard().clickReportMenuButton();

        waitForElementVisible(By.className("s-btn-new_report"), browser).click();

        EmbeddedReportPage.waitForPageLoaded(browser);

        return Graphene.createPageFragment(EmbeddedReportPage.class,
                waitForElementVisible(EmbeddedReportPage.LOCATOR, browser));
    }

    public ReportsPage openEmbeddedReportsPage() {
        editDashboard().clickReportMenuButton();

        waitForElementVisible(By.className("s-btn-manage"), browser).click();
        return Graphene.createPageFragment(ReportsPage.class, waitForElementVisible(ReportsPage.LOCATOR, browser));
    }

    @Override
    public DashboardEditBar editDashboard() {
        if (!isElementPresent(BY_DASHBOARD_EDIT_BAR, browser)) {
            waitForElementVisible(BY_EDIT_BUTTON, browser).click();
        }

        return getDashboardEditBar();
    }
}