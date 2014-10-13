package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class SchedulesTable extends AbstractTable {

	public enum ScheduleStatus {
		OK, ERROR, SCHEDULED, DISABLED, UNSCHEDULED, RUNNING
	}

	private final static By BY_SCHEDULE_TITLE = By.cssSelector(".schedule-title-cell");
	private final static By BY_SCHEDULE_CRON = By.cssSelector(".schedule-cron-cell");

	public WebElement getScheduleTitle(String executableName) {
		return getScheduleElement(executableName, BY_SCHEDULE_TITLE);
	}

	public WebElement getScheduleCron(String executableName) {
		return getScheduleElement(executableName, BY_SCHEDULE_CRON);
	}

	public WebElement getScheduleElement(String executableName, By selector) {
		for (int i = 0; i < this.getNumberOfRows(); i++) {
			if (getRow(i).findElement(BY_SCHEDULE_TITLE).getText().equals(executableName))
				return getRow(i).findElement(selector);
		}
		return null;
	}

	public WebElement getSchedule(String executableName) {
		for (int i = 0; i < this.getNumberOfRows(); i++) {
			if (getRow(i).findElement(BY_SCHEDULE_TITLE).getText().equals(executableName))
				return getRow(i);
		}
		return null;
	}
}
