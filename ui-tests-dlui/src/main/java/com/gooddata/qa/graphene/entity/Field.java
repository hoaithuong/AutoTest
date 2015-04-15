package com.gooddata.qa.graphene.entity;

import org.openqa.selenium.By;

public class Field {

    private String name;
    private FieldTypes type;

    public Field() {}

    public Field(String name, FieldTypes type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public FieldTypes getType() {
        return type;
    }

    public enum FieldTypes {
        ALL("all data"),
        ATTRIBUTE("attributes"),
        FACT("facts"),
        DATE("dates"),
        LABLE_HYPERLINK("labels & hyperlinks");

        private String filterName;

        private FieldTypes(String filterName) {
            this.filterName = filterName;
        }

        public By getFilterBy() {
            return By.xpath(String.format("//a[text()='%s']", this.filterName));
        }
    }
}
