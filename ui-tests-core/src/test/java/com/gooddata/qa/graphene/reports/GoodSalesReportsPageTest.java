package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;

public class GoodSalesReportsPageTest extends GoodSalesAbstractTest {

    private static final String TAG_REPORT = "New Lost [Drill-In]";
    private static final String TAG_NAME = "GDC";
    private static final String CURRENT_SALES_FOLDER = "Current Sales";
    private static final String FAVORITES_FOLDER = "Favorites";
    private static final String ACTIVITY_REPORTS_FOLDER = "Activity Reports";
    private static final String ALL_FOLDER = "All";

    @Test(dependsOnGroups = {"createProject"})
    public void addTagToReport() {
        initReportsPage().openReport(TAG_REPORT);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).addTag(TAG_NAME);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyReportsPage() {
        ReportsPage reportsPage = initReportsPage();
        assertTrue(isEqualCollection(reportsPage.getAllFolderNames(),
                asList(ALL_FOLDER, FAVORITES_FOLDER, "My Reports", "Unsorted", ACTIVITY_REPORTS_FOLDER,
                        CURRENT_SALES_FOLDER, "Leaderboards", "Opportunity Historicals", "Outlook Headlines",
                        "Velocity Reports", "Waterfall Analysis", "What's Changed", "_Drill Reports")));
        assertTrue(isEqualCollection(reportsPage.getGroupByVisibility(), asList("Time", "Author", "Report Name",
                "Folders")));
    }

    @Test(dependsOnMethods = {"addTagToReport"})
    public void verifyTagReport() {
        ReportsPage reportsPage = initReportsPage();
        waitForFragmentVisible(reportsPage).openFolder(ALL_FOLDER);
        assertTrue(reportsPage.isTagCloudVisible());
        assertTrue(reportsPage.getReportsCount() > 1);
        assertEquals(reportsPage.filterByTag(TAG_NAME).getReportsCount(), 1);

        reportsPage.openFolder(CURRENT_SALES_FOLDER);
        assertFalse(reportsPage.isTagCloudVisible());
        assertEquals(reportsPage.getReportsCount(), 3);

        reportsPage.openFolder(ALL_FOLDER);
        assertTrue(reportsPage.deselectAllTags().getReportsCount() > 1);
    }

    @Test(dependsOnMethods = {"verifyTagReport"})
    public void moveReports() {
        ReportsPage reportsPage = initReportsPage();
        waitForFragmentVisible(reportsPage).openFolder(CURRENT_SALES_FOLDER);
        assertEquals(reportsPage.getReportsCount(), 3);

        reportsPage.openFolder(ALL_FOLDER);
        reportsPage.moveReportsToFolder(CURRENT_SALES_FOLDER, TAG_REPORT);
        reportsPage.openFolder(CURRENT_SALES_FOLDER);

        //sometimes the tag selection is cached from previous testcase and make checking number of reports failed
        //so we need to deselect all tags for sure
        reportsPage.deselectAllTags();

        assertEquals(reportsPage.getReportsCount(), 4);

        assertEquals(reportsPage.moveReportsToFolderByDragDrop(ACTIVITY_REPORTS_FOLDER, TAG_REPORT)
                .getReportsCount(), 3);
        reportsPage.openFolder(ACTIVITY_REPORTS_FOLDER);
        assertEquals(reportsPage.getReportsCount(), 6);
    }

    @Test(dependsOnMethods = {"addTagToReport"})
    public void favoriteReport() {
        ReportsPage reportsPage = initReportsPage();
        waitForFragmentVisible(reportsPage).openFolder(ALL_FOLDER);
        waitForFragmentVisible(reportsPage).addFavorite(TAG_REPORT);

        waitForFragmentVisible(reportsPage).openFolder(FAVORITES_FOLDER);
        assertEquals(reportsPage.getReportsCount(), 1);
    }
}
