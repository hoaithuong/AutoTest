package com.gooddata.qa.graphene.enums.project;

public enum ProjectFeatureFlags {

    HIDE_DASHBOARD_SCHEDULE("hideDashboardSchedule"),
    DASHBOARD_SCHEDULE_RECIPIENTS("dashboardScheduleRecipients"),
    DISPLAY_USER_MANAGEMENT("displayUserManagement"),
    NPS_STATUS("npsStatus"),
    ANALYTICAL_DESIGNER("analyticalDesigner"),
    ENABLE_CSV_UPLOADER("enableCsvUploader"),
    ENABLE_ANALYTICAL_DASHBOARDS("enableAnalyticalDashboards"),
    DISABLE_SAVED_FILTERS("disableSavedFilters"),
    ENABLE_CHANGE_LANGUAGE("enableChangeLanguage"),
    HIDE_KPI_ALERT_LINK("hideKPIAlertLinks"),
    FISCAL_CALENDAR_ENABLED("fiscalCalendarEnabled"),
    DASHBOARD_ACCESS_CONTROL("dashboardAccessControlEnabled"),
    USE_AVAILABLE_ENABLED("useAvailableEnabled"),
    ENABLE_ETL_COMPONENT("enableEtlComponent"),
    CONTROL_EXECUTION_CONTEXT_ENABLED("controlExecutionContextEnabled"),
    EXPORT_TO_XLSX_ENABLED("exportToXLSXEnabled"),
    CELL_MERGED_BY_DEFAULT("cellMergedByDefault"),
    ACTIVE_FILTERS_BY_DEFAULT("activeFiltersByDefault"),
    REPORT_HEADER_PAGING_ENABLED("reportHeaderPagingEnabled"),
    ENABLE_ANALYTICAL_DESIGNER_EXPORT("enableAnalyticalDesignerExport"),
    ENABLE_ACTIVE_FILTER_CONTEXT("enableActiveFilterContext"),
    ENABLE_METRIC_DATE_FILTER("enableMetricDateFilter"),
    ENABLE_CUSTOM_COLOR_PICKER("enableCustomColorPicker"),
    IS_REDIRECTED_FOR_ONE_PROJECT("isRedirectedForOneProject"),
    CASCADING_FILTERS_BOOSTING_ENABLE("cascadingFiltersBoostingEnabled"),
    ENABLE_KPI_DASHBOARD_EXTENDED_DATE_FILTERS("enableKPIDashboardExtendedDateFilters"),
    ENABLE_KPI_DASHBOARD_WEEK_FILTERS("enableKPIDashboardWeekFilters"),
    ENABLE_LAYOUTS_DASHBOARD("enableLayouts"),
    ENABLE_CATALOG_GROUPING("enableCatalogGrouping");

    private final String featureFlag;

    private ProjectFeatureFlags(String featureFlag) {
        this.featureFlag = featureFlag;
    }

    public String getFlagName() {
        return this.featureFlag;
    }
}
