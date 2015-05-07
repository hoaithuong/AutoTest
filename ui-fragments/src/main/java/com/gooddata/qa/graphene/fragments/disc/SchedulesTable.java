package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class SchedulesTable extends AbstractTable {

    private final static By BY_SCHEDULE_TITLE = By
            .cssSelector(".ait-process-schedule-list-item-title");
    private final static By BY_SCHEDULE_CRON = By.cssSelector(".schedule-cron-cell");

    public WebElement getScheduleTitle(String scheduleName) {
        return getScheduleElement(scheduleName, BY_SCHEDULE_TITLE);
    }

    public WebElement getScheduleCron(String executableName) {
        return getScheduleElement(executableName, BY_SCHEDULE_CRON);
    }

    public WebElement getSchedule(String executableName) {
        for (int i = 0; i < this.getNumberOfRows(); i++) {
            if (waitForElementVisible(BY_SCHEDULE_TITLE, getRow(i)).getText().equals(executableName))
                return getRow(i);
        }
        return null;
    }

    private WebElement getScheduleElement(String executableName, By selector) {
        for (int i = 0; i < this.getNumberOfRows(); i++) {
            if (waitForElementVisible(BY_SCHEDULE_TITLE, getRow(i)).getText().equals(executableName))
                return waitForElementVisible(selector, getRow(i));
        }
        return null;
    }
}
