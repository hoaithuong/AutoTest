package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCOverviewProjectStates;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class DISCOverview extends DISCOverviewProjects {

	@FindBy(css = ".ait-overview-field-failed .ait-overview-state")
	private WebElement failedState;

	@FindBy(css = ".ait-overview-field-failed .ait-overview-state-count")
	private WebElement failedStateNumber;

	@FindBy(css = ".ait-overview-field-running .ait-overview-state")
	private WebElement runningState;

	@FindBy(css = ".ait-overview-field-running .ait-overview-state-count")
	private WebElement runningStateNumber;

	@FindBy(css = ".ait-overview-field-scheduled .ait-overview-state")
	private WebElement scheduledState;

	@FindBy(css = ".ait-overview-field-scheduled .ait-overview-state-count")
	private WebElement scheduledStateNumber;

	@FindBy(css = ".ait-overview-field-successful .ait-overview-state")
	private WebElement successfulState;

	@FindBy(css = ".ait-overview-field-successful .ait-overview-state-count")
	private WebElement successfulStateNumber;

	@FindBy(css = ".s-btn-discard")
	private WebElement discardButton;

	public void waitForStateNumber(WebElement stateNumber) throws InterruptedException {
		for (int i = 0; i < 5 && stateNumber.getText().isEmpty(); i++)
			Thread.sleep(1000);
	}

	public void selectOverviewState(DISCOverviewProjectStates state) throws InterruptedException {
		WebElement stateTitle = null;
		WebElement stateNumber = null;
		switch (state) {
		case FAILED:
			stateTitle = failedState;
			stateNumber = failedStateNumber;
			break;
		case RUNNING:
			stateTitle = runningState;
			stateNumber = runningStateNumber;
			break;
		case SCHEDULED:
			stateTitle = scheduledState;
			stateNumber = scheduledStateNumber;
			break;
		case SUCCESSFUL:
			stateTitle = successfulState;
			stateNumber = successfulStateNumber;
			break;
		}
		waitForElementVisible(stateTitle).click();
		Thread.sleep(1000);
		waitForStateNumber(stateNumber);
	}
	
	public String getState(DISCOverviewProjectStates state) {
		WebElement stateTitle = null;
		switch (state) {
		case FAILED:
			stateTitle = failedState;
			break;
		case RUNNING:
			stateTitle = runningState;
			break;
		case SCHEDULED:
			stateTitle = scheduledState;
			break;
		case SUCCESSFUL:
			stateTitle = successfulState;
			break;
		}
		return waitForElementVisible(stateTitle).getText();
	}
	
	public String getStateNumber(DISCOverviewProjectStates state) throws InterruptedException {
		WebElement stateNumber = null;
		switch (state) {
		case FAILED:
			stateNumber = failedStateNumber;
			break;
		case RUNNING:
			stateNumber = runningStateNumber;
			break;
		case SCHEDULED:
			stateNumber = scheduledStateNumber;
			break;
		case SUCCESSFUL:
			stateNumber = successfulStateNumber;
			break;
		}
		waitForElementVisible(stateNumber);
		waitForStateNumber(stateNumber);
		return stateNumber.getText();
	}

	public boolean assertOverviewStateNumber(DISCOverviewProjectStates state, int number)
			throws InterruptedException {
		assertTrue(state.getOption().equalsIgnoreCase(getState(state)));
		return getStateNumber(state).equals(String.valueOf(number));
	}
}
