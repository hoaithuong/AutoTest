package com.gooddata.qa.graphene.manage;

import static org.openqa.selenium.By.id;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;

public class GoodSalesManageObjectsTest extends ManageObjectsAbstractTest {

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-manage-objects";
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjetcs", "fact"})
    public void viewFactsTable() {
        viewSortObjectsTable(ObjectTypes.FACT, factsList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("Days to Close", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Duration", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        ObjectsTable factsTable = ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser);
        this.checkTagObjects(factsTable, taggedObjects, tagsMap, Arrays.asList("Days to Close"));
        checkObjectLinkInTable(factsTable, "Opp. Close (Date)");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjects", "attribute"})
    public void viewAttributesTable() {
        viewSortObjectsTable(ObjectTypes.ATTRIBUTE, attributesList);
        List<String> filteredObjectsList = Arrays.asList("Day of Week (Mon-Sun) (Activity)",
                "Day of Week (Mon-Sun) (Closed)", "Day of Week (Mon-Sun) (Created)",
                "Day of Week (Mon-Sun) (Snapshot)", "Day of Week (Mon-Sun) (Timeline)");
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("day", Arrays.asList("2", "17px"));
        tagsMap.put("eu", Arrays.asList("3", "15px"));
        ObjectsTable attributesTable = ObjectsTable.getInstance(id(ObjectTypes.ATTRIBUTE.getObjectsTableID()), browser);
        this.filterObjectByTagList(attributesTable, filteredObjectsList, tagsMap);
        List<String> filteredObjectsCloud = Arrays.asList("Week (Mon-Sun) (Activity)",
                "Week (Mon-Sun) (Closed)", "Week (Mon-Sun) (Created)", "Week (Mon-Sun) (Snapshot)",
                "Week (Mon-Sun) (Timeline)");
        tagsMap.put("year", Arrays.asList("7", "19px"));
        this.filterObjectByTagCloud(attributesTable, filteredObjectsCloud, tagsMap);
        checkObjectLinkInTable(attributesTable, "Day of Week (Mon-Sun) (Activity)");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjetcs", "metric"})
    public void viewMetricsTable() {
        viewSortObjectsTable(ObjectTypes.METRIC, metricsList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("# of Open Opps.", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Expected % of Goal", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        ObjectsTable metricsTable = ObjectsTable.getInstance(id(ObjectTypes.METRIC.getObjectsTableID()), browser);
        this.checkTagObjects(metricsTable, taggedObjects, tagsMap, Arrays.asList("# of Open Opps."));
        checkObjectLinkInTable(metricsTable, "# of Opportunities [BOP]");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjetcs", "variable"})
    public void viewVariablesTable() {
        ObjectsTable variablesTable = openObjectsTable(ObjectTypes.VARIABLE)
            .assertTableHeader()
            .sortObjectsTable(ObjectsTable.SORT_DESC, variablesList)
            .sortObjectsTable(ObjectsTable.SORT_ASC, variablesList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("Quota", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Status", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(variablesTable, taggedObjects, tagsMap, Arrays.asList("Quota"));
        checkObjectLinkInTable(variablesTable, "Status");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"moveObjects", "fact"})
    public void moveFactsBetweenFolders() {
        List<String> movedObjects = Arrays.asList("Amount", "Days to Close", "Duration", "Probability", 
                "Velocity", "Timeline (Date)");
        moveObjectsBetweenFolders(ObjectTypes.FACT, movedObjects, "2", "Stage History");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"moveObjects", "attribute"})
    public void moveAttributesBetweenFolders() {
        List<String> movedObjects = Arrays.asList("Activity", "Priority", "Product", "Region");
        moveObjectsBetweenFolders(ObjectTypes.ATTRIBUTE, movedObjects, "2", "Activity");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"moveObjects", "metric"})
    public void moveMetricsBetweenFolders() {
        List<String> movedObjects = Arrays.asList("Amount [BOP]", "Best Case [BOP]", "Probability [BOP]", 
                "_Close [EOP]");
        moveObjectsBetweenFolders(ObjectTypes.METRIC, movedObjects, "2", "_System");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2, groups = {"deleteObjects", "fact"})
    public void deleteFacts() {
        List<String> deletedObjects = Arrays.asList("Amount", "Days to Close", "Duration", "Probability", 
                "Velocity", "Timeline (Date)");
        deleteObjectsTable(deletedObjects, factsList, ObjectTypes.FACT);
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2, groups = {"deleteObjects", "attribute"})
    public void deleteAttributes() {
        List<String> deletedObjects = Arrays.asList("Activity", "Department",
                "Month of Quarter (Created)", "Priority", "Product", "Region");
        deleteObjectsTable(deletedObjects, attributesList, ObjectTypes.ATTRIBUTE);
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2, groups = {"deleteObjects", "metric"})
    public void deleteMetrics() {
        List<String> deletedObjects = Arrays.asList("Amount [BOP]", "Best Case [BOP]", "Probability [BOP]", 
                "_Close [EOP]");
        deleteObjectsTable(deletedObjects, metricsList, ObjectTypes.METRIC);
    }
}