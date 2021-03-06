package com.gooddata.qa.graphene.entity.visualization;

import java.util.List;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;

import java.util.ArrayList;

public class InsightMDConfiguration {

    private String title;
    private ReportType type;
    private List<MeasureBucket> measureBuckets = new ArrayList<MeasureBucket>();
    private List<CategoryBucket> categoryBuckets = new ArrayList<CategoryBucket>();
    private List<TotalsBucket> totalsBuckets = new ArrayList<TotalsBucket>();
    private List<FilterAttribute> filterAttribute;
    private FilterDate filterDate;

    public InsightMDConfiguration(String title, ReportType type) {
        this.title = title;
        this.type = type;
    }

    public InsightMDConfiguration setMeasureBucket(List<MeasureBucket> measureBuckets) {
        this.measureBuckets = measureBuckets;
        return this;
    }

    public InsightMDConfiguration setCategoryBucket(List<CategoryBucket> categoryBuckets) {
        this.categoryBuckets = categoryBuckets;
        return this;
    }

    public InsightMDConfiguration setTotalsBucket(List<TotalsBucket> totalsBuckets) {
        this.totalsBuckets = totalsBuckets;
        return this;
    }

    public InsightMDConfiguration setFilter(List<FilterAttribute> filterAttribute) {
        this.filterAttribute = filterAttribute;
        return this;
    }

    public InsightMDConfiguration setDateFilter(FilterDate filterDate) {
        this.filterDate = filterDate;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ReportType getType() {
        return type;
    }

    public List<CategoryBucket> getCategoryBuckets() {
        return categoryBuckets;
    }

    public List<MeasureBucket> getMeasureBuckets() {
        return measureBuckets;
    }

    public List<TotalsBucket> getTotalsBuckets() {
        return totalsBuckets;
    }

    public List<FilterAttribute> getFilters() { return filterAttribute; }

    public FilterDate getDateFilter() { return filterDate; }
}
