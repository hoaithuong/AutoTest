package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;

public class EmbeddedDashboardSavedViewLocalizationTest extends GoodSalesAbstractLocalizationTest {

    private static final String DASHBOARD_NAME = "Saved view";

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-embeded-dashboard-saved-view-localization-test";
    }

    @Test(dependsOnMethods = {"createAndUsingTestUser"}, groups = {"precondition"})
    public void initEmbeddedDashboardUri() {
        initDashboardsPage()
            .addNewDashboard(DASHBOARD_NAME);

        embeddedUri = initDashboardsPage()
            .selectDashboard(DASHBOARD_NAME)
            .openEmbedDashboardDialog()
            .getPreviewURI()
            .replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnMethods = {"initEmbeddedDashboardUri"}, groups = {"precondition"})
    public void turnSavedViewOn() {
        EmbeddedDashboard dashboard = initEmbeddedDashboard();
        dashboard.editDashboard()
            .turnSavedViewOption(true);
        dashboard.getDashboardEditBar().saveDashboard();
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkSavedViewDialog() {
        EmbeddedDashboard dashboard = initEmbeddedDashboard();
        dashboard.getSavedViewWidget()
            .openSavedViewMenu();
        checkLocalization(browser);

        dashboard.editDashboard()
            .addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, ATTR_ACCOUNT)
            .saveDashboard();

        dashboard.getFirstFilter()
            .changeAttributeFilterValue("14 West");

        dashboard.getSavedViewWidget()
            .openSavedViewMenu()
            .saveCurrentView("hello");
    }

    @Test(dependsOnMethods = {"checkSavedViewDialog"}, groups = {"i18n"})
    public void checkRenameSavedView() {
        SavedViewWidget savedViewWidget = initEmbeddedDashboard().getSavedViewWidget().openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        savedViewPopupMenu.openContextMenuOfSavedView("hello");
        savedViewPopupMenu.getSavedFiltersContextMenu().openRenameDialog();
        checkLocalization(browser);
    }

    @Test(dependsOnMethods = {"checkRenameSavedView"}, groups = {"i18n"})
    public void checkDeleteSavedView() {
        SavedViewWidget savedViewWidget = initEmbeddedDashboard().getSavedViewWidget().openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        savedViewPopupMenu.openContextMenuOfSavedView("hello");
        savedViewPopupMenu.getSavedFiltersContextMenu().openDeleteDialog();
        checkLocalization(browser);
    }
}
