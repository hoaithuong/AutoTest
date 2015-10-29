package com.gooddata.qa.graphene.enums.dashboard;

public enum WidgetTypes {

    KEY_METRIC("Key metric"),
    KEY_METRIC_WITH_TREND("Key metric w/"),
    GEO_CHART("Geo chart"),
    SCATTER_EXPLORER("Scatter explorer"),
    INTERACTIVE_REPORT("Interactive report");

    private final String label;

    private WidgetTypes(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
