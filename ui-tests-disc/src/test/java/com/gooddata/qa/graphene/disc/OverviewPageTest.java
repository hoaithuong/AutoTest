package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.enums.DISCOverviewProjectStates;
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.UserRoles;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class OverviewPageTest extends AbstractSchedulesTests {

	protected static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";
	protected static final String DISC_OVERVIEW_RUNNING_STATE = "admin/disc/#/overview/ALL/RUNNING";

	@BeforeClass
	public void initProperties() throws InterruptedException {
		zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
		projectTitle = "Disc-test-overview-page";
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkEmptyFailedState() throws InterruptedException {
		openOverviewPage();
		checkFilteredOutProject(DISCOverviewProjectStates.FAILED, projectTitle,
				testParams.getProjectId());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkEmptyRunningState() throws InterruptedException {
		openOverviewPage();
		checkFilteredOutProject(DISCOverviewProjectStates.RUNNING, projectTitle,
				testParams.getProjectId());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkEmptyScheduledState() throws InterruptedException {
		openOverviewPage();
		checkFilteredOutProject(DISCOverviewProjectStates.SCHEDULED, projectTitle,
				testParams.getProjectId());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkEmptySucessfulState() throws InterruptedException {
		openOverviewPage();
		checkFilteredOutProject(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
				testParams.getProjectId());
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkFailedStateNumber() throws JSONException, InterruptedException {
		checkStateNumber(DISCOverviewProjectStates.FAILED, projectTitle, testParams.getProjectId(),
				"Check Failed State Number", "errorGraph");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkRunningStateNumber() throws JSONException, InterruptedException {
		checkStateNumber(DISCOverviewProjectStates.RUNNING, projectTitle,
				testParams.getProjectId(), "Check Running State Number", "longTimeRunningGraph");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkSuccessfulStateNumber() throws JSONException, InterruptedException {
		checkStateNumber(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
				testParams.getProjectId(), "Check Successful State Number", "successfulGraph");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkScheduledStateNumber() throws JSONException, InterruptedException {
		Map<String, String> additionalProjects = createMultipleProjects(
				"Disc-test-scheduled-state", 1);
		try {
			prepareDataForScheduledStateTests(projectTitle, testParams.getProjectId(),
					additionalProjects, null, null, "Check Scheduled State Number");
			openOverviewPage();
			checkStateNumber(DISCOverviewProjectStates.SCHEDULED, 1);
			checkOtherStateNumbers(DISCOverviewProjectStates.SCHEDULED, projectTitle,
					testParams.getProjectId());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
			deleteProjects(additionalProjects);
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkCombinedStatesNumber() throws JSONException, InterruptedException {
		try {
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId(), "Check Combined States Number", "errorGraph", null,
					null);
			createScheduleForProcess(projectTitle, testParams.getProjectId(),
					"Check Combined States Number", "/graph/successfulGraph.grf", null, null);
			assertNewSchedule("Check Combined States Number", "successfulGraph.grf", cronTime, null);
			scheduleDetail.manualRun();
			scheduleDetail.assertLastExecutionDetails(true, true, false,
					"Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
			openOverviewPage();
			checkStateNumber(DISCOverviewProjectStates.FAILED, 1);
			checkStateNumber(DISCOverviewProjectStates.SUCCESSFUL, 1);
			checkFilteredOutProject(DISCOverviewProjectStates.RUNNING, projectTitle,
					testParams.getProjectId());
			checkFilteredOutProject(DISCOverviewProjectStates.SCHEDULED, projectTitle,
					testParams.getProjectId());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkOverviewFailedProjects() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId(), "Check Overview Failed Project", "errorGraph",
					processesMap, expectedSchedules);
			openOverviewPage();
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.FAILED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkOverviewSuccessfulProject() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
					testParams.getProjectId(), "Check Overview Successful Project",
					"successfulGraph", processesMap, expectedSchedules);
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			discOverview.selectOverviewState(DISCOverviewProjectStates.SUCCESSFUL);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.SUCCESSFUL.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(enabled = false, dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkOverviewScheduledProject() throws JSONException, InterruptedException {
		Map<String, String> additionalProjects = createMultipleProjects(
				"Disc-test-scheduled-state", 1);
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForScheduledStateTests(projectTitle, testParams.getProjectId(),
					additionalProjects, processesMap, expectedSchedules,
					"Check Overview Scheduled Project");
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			discOverview.selectOverviewState(DISCOverviewProjectStates.SCHEDULED);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.SCHEDULED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
			deleteProjects(additionalProjects);
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkOverviewRunningProject() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.RUNNING, projectTitle,
					testParams.getProjectId(), "Check Overview Running Project",
					"longTimeRunningGraph", processesMap, expectedSchedules);
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			discOverview.selectOverviewState(DISCOverviewProjectStates.RUNNING);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.RUNNING.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void restartFailedProjects() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId(), "Restart Failed Projects", "errorGraph",
					processesMap, expectedSchedules);
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			checkBulkActionForSelectedProjects(DISCOverviewProjectStates.FAILED, false,
					getProjectsMap());
			checkFilteredOutProject(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId());
			browser.get(expectedSchedules.get(
					Arrays.asList("Restart Failed Projects", "errorGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			try {
				assertTrue(scheduleDetail.isStarted());
			} catch (NoSuchElementException ex) {
				assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
			}
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void restartFailedSchedule() throws JSONException, InterruptedException {
		try {
			final Map<String, String> processesMap = new LinkedHashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			Map<String, List<String>> selectedSchedules = new HashMap<String, List<String>>();
			prepareDataForCheckingScheduleState(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId(), "Restart Failed Schedule", "errorGraph",
					processesMap, expectedSchedules);
			selectedSchedules.put(("Restart Failed Schedule 1").toUpperCase(),
					Arrays.asList("errorGraph.grf"));
			Map<List<String>, List<String>> checkedSchedules = new HashMap<List<String>, List<String>>();
			checkedSchedules.put(Arrays.asList("Restart Failed Schedule 2", "errorGraph.grf"),
					expectedSchedules.get(Arrays.asList("Restart Failed Schedule 2",
							"errorGraph.grf")));
			Map<String, String> checkedProcesses = new HashMap<String, String>();
			checkedProcesses.put("Restart Failed Schedule 2",
					processesMap.get("Restart Failed Schedule 2"));
			openOverviewPage();
			checkBulkActionForSelectedSchedules(DISCOverviewProjectStates.FAILED, false,
					selectedSchedules);
			discOverview.getFailedStateNumber();
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.FAILED.getOption(), projectTitle,
					testParams.getProjectId(), checkedProcesses, checkedSchedules);
			browser.get(expectedSchedules.get(
					Arrays.asList("Restart Failed Schedule 1", "errorGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			try {
				assertTrue(scheduleDetail.isStarted());
			} catch (NoSuchElementException ex) {
				assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
			}
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableFailedProjects() throws JSONException, InterruptedException {
		try {
			disableProjectsOnOverviewPage(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId(), "Disable Failed Projects", "errorGraph");
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableFailedSchedule() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new LinkedHashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			Map<String, List<String>> selectedSchedules = new HashMap<String, List<String>>();
			prepareDataForCheckingScheduleState(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId(), "Disable Failed Schedule", "errorGraph",
					processesMap, expectedSchedules);
			selectedSchedules.put("Disable Failed Schedule 2".toUpperCase(),
					Arrays.asList("errorGraph.grf"));
			Map<List<String>, List<String>> checkedSchedules = new HashMap<List<String>, List<String>>();
			checkedSchedules.put(Arrays.asList("Disable Failed Schedule 1", "errorGraph.grf"),
					expectedSchedules.get(Arrays.asList("Disable Failed Schedule 1",
							"errorGraph.grf")));
			openOverviewPage();
			discOverview.selectOverviewState(DISCOverviewProjectStates.FAILED);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.FAILED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
			checkBulkActionForSelectedSchedules(DISCOverviewProjectStates.FAILED, true,
					selectedSchedules);
			discOverview.selectOverviewState(DISCOverviewProjectStates.FAILED);
			waitForElementVisible(discOverviewProjects.getRoot());
			processesMap.remove("Disable Failed Schedule 2");
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.FAILED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, checkedSchedules);
			browser.get(expectedSchedules.get(
					Arrays.asList("Disable Failed Schedule 2", "errorGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			assertTrue(scheduleDetail.getEnableButton().isDisplayed());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void runSuccessfulProjects() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
					testParams.getProjectId(), "Run Successful Projects", "longTimeRunningGraph",
					processesMap, expectedSchedules);
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			checkBulkActionForSelectedProjects(DISCOverviewProjectStates.SUCCESSFUL, false,
					getProjectsMap());
			checkFilteredOutProject(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
					testParams.getProjectId());
			browser.get(expectedSchedules.get(
					Arrays.asList("Run Successful Projects", "longTimeRunningGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			assertTrue(scheduleDetail.isStarted());
			assertTrue(scheduleDetail.isInRunningState());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void runSuccessfulSchedule() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			Map<String, List<String>> selectedSchedules = new HashMap<String, List<String>>();
			prepareDataForCheckingScheduleState(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
					testParams.getProjectId(), "Run Successful Schedule", "longTimeRunningGraph",
					processesMap, expectedSchedules);
			selectedSchedules.put(("Run Successful Schedule 1").toUpperCase(),
					Arrays.asList("longTimeRunningGraph.grf"));
			Map<List<String>, List<String>> checkedSchedules = new HashMap<List<String>, List<String>>();
			checkedSchedules.put(Arrays.asList("Run Successful Schedule 2",
					"longTimeRunningGraph.grf"), expectedSchedules.get(Arrays.asList(
					"Run Successful Schedule 2", "longTimeRunningGraph.grf")));
			Map<String, String> checkedProcesses = new HashMap<String, String>();
			checkedProcesses.put("Run Successful Schedule 2",
					processesMap.get("Run Successful Schedule 2"));
			openOverviewPage();
			checkBulkActionForSelectedSchedules(DISCOverviewProjectStates.SUCCESSFUL, false,
					selectedSchedules);
			Thread.sleep(3000);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.SUCCESSFUL.getOption(), projectTitle,
					testParams.getProjectId(), checkedProcesses, checkedSchedules);
			browser.get(expectedSchedules.get(
					Arrays.asList("Run Successful Schedule 1", "longTimeRunningGraph.grf")).get(0));
			assertTrue(scheduleDetail.isStarted());
			assertTrue(scheduleDetail.isInRunningState());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableSuccessfulProjects() throws JSONException, InterruptedException {
		try {
			disableProjectsOnOverviewPage(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
					testParams.getProjectId(), "Disable Successful Projects", "successfulGraph");
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableSuccessfulSchedule() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new LinkedHashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			Map<String, List<String>> selectedSchedules = new HashMap<String, List<String>>();
			prepareDataForCheckingScheduleState(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
					testParams.getProjectId(), "Disable Successful Schedule", "successfulGraph",
					processesMap, expectedSchedules);
			selectedSchedules.put("Disable Successful Schedule 2".toUpperCase(),
					Arrays.asList("successfulGraph.grf"));
			Map<List<String>, List<String>> checkedSchedules = new HashMap<List<String>, List<String>>();
			checkedSchedules.put(Arrays.asList("Disable Successful Schedule 1",
					"successfulGraph.grf"), expectedSchedules.get(Arrays.asList(
					"Disable Successful Schedule 1", "successfulGraph.grf")));
			openOverviewPage();
			discOverview.selectOverviewState(DISCOverviewProjectStates.SUCCESSFUL);
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.SUCCESSFUL.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
			processesMap.remove("Disable Successful Schedule 2");
			checkBulkActionForSelectedSchedules(DISCOverviewProjectStates.SUCCESSFUL, true,
					selectedSchedules);
			discOverview.selectOverviewState(DISCOverviewProjectStates.SUCCESSFUL);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.SUCCESSFUL.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, checkedSchedules);
			browser.get(expectedSchedules.get(
					Arrays.asList("Disable Successful Schedule 2", "successfulGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			assertTrue(scheduleDetail.getEnableButton().isDisplayed());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void stopRunningProjects() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForCheckingProjectState(DISCOverviewProjectStates.RUNNING, projectTitle,
					testParams.getProjectId(), "Stop Running Projects", "longTimeRunningGraph",
					processesMap, expectedSchedules);
			String scheduleDetailUrl = browser.getCurrentUrl();
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			checkBulkActionForSelectedProjects(DISCOverviewProjectStates.RUNNING, false,
					getProjectsMap());
			checkOtherStateNumbers(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId());
			browser.get(scheduleDetailUrl);
			waitForElementVisible(scheduleDetail.getRoot());
			Thread.sleep(3000);
			expectedSchedules.put(Arrays
					.asList("Stop Running Projects", "longTimeRunningGraph.grf"), Arrays.asList(
					browser.getCurrentUrl(), scheduleDetail.getExecutionRuntime(),
					scheduleDetail.getLastExecutionDate(), scheduleDetail.getLastExecutionTime(),
					scheduleDetail.getExecutionError()));
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			discOverview.selectOverviewState(DISCOverviewProjectStates.FAILED);
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.FAILED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void stopRunningSchedule() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new LinkedHashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			Map<String, List<String>> selectedSchedules = new HashMap<String, List<String>>();
			prepareDataForCheckingScheduleState(DISCOverviewProjectStates.RUNNING, projectTitle,
					testParams.getProjectId(), "Stop Running Schedule", "longTimeRunningGraph",
					processesMap, expectedSchedules);
			selectedSchedules.put("Stop Running Schedule 2".toUpperCase(),
					Arrays.asList("longTimeRunningGraph.grf"));
			Map<List<String>, List<String>> checkedSchedules1 = new HashMap<List<String>, List<String>>();
			checkedSchedules1.put(Arrays.asList("Stop Running Schedule 1",
					"longTimeRunningGraph.grf"), expectedSchedules.get(Arrays.asList(
					"Stop Running Schedule 1", "longTimeRunningGraph.grf")));
			Map<String, String> processesMap1 = new HashMap<String, String>();
			processesMap1.put("Stop Running Schedule 1",
					processesMap.get("Stop Running Schedule 1"));
			Map<String, String> processesMap2 = new HashMap<String, String>();
			processesMap2.put("Stop Running Schedule 2",
					processesMap.get("Stop Running Schedule 2"));
			openOverviewPage();
			discOverview.selectOverviewState(DISCOverviewProjectStates.RUNNING);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.RUNNING.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
			checkBulkActionForSelectedSchedules(DISCOverviewProjectStates.RUNNING, false,
					selectedSchedules);
			discOverview.selectOverviewState(DISCOverviewProjectStates.RUNNING);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.RUNNING.getOption(), projectTitle,
					testParams.getProjectId(), processesMap1, checkedSchedules1);
			browser.get(expectedSchedules.get(
					Arrays.asList("Stop Running Schedule 2", "longTimeRunningGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			Thread.sleep(1000);
			Map<List<String>, List<String>> checkedSchedules2 = new HashMap<List<String>, List<String>>();
			checkedSchedules2.put(Arrays.asList("Stop Running Schedule 2",
					"longTimeRunningGraph.grf"), Arrays.asList(browser.getCurrentUrl(),
					scheduleDetail.getExecutionRuntime(), scheduleDetail.getLastExecutionDate(),
					scheduleDetail.getLastExecutionTime(), scheduleDetail.getExecutionError()));
			openOverviewPage();
			discOverview.selectOverviewState(DISCOverviewProjectStates.FAILED);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.FAILED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap2, checkedSchedules2);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableRunningProjects() throws JSONException, InterruptedException {
		try {
			disableProjectsOnOverviewPage(DISCOverviewProjectStates.RUNNING, projectTitle,
					testParams.getProjectId(), "Disable Running Projects", "longTimeRunningGraph");
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableRunningSchedule() throws JSONException, InterruptedException {
		try {
			Map<String, String> processesMap = new LinkedHashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			Map<String, List<String>> selectedSchedules = new HashMap<String, List<String>>();
			prepareDataForCheckingScheduleState(DISCOverviewProjectStates.RUNNING, projectTitle,
					testParams.getProjectId(), "Disable Running Schedule", "longTimeRunningGraph",
					processesMap, expectedSchedules);
			selectedSchedules.put("Disable Running Schedule 2".toUpperCase(),
					Arrays.asList("longTimeRunningGraph.grf"));
			Map<List<String>, List<String>> checkedSchedules = new HashMap<List<String>, List<String>>();
			checkedSchedules.put(Arrays.asList("Disable Running Schedule 1",
					"longTimeRunningGraph.grf"), expectedSchedules.get(Arrays.asList(
					"Disable Running Schedule 1", "longTimeRunningGraph.grf")));
			openOverviewPage();
			discOverview.selectOverviewState(DISCOverviewProjectStates.RUNNING);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.RUNNING.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
			checkBulkActionForSelectedSchedules(DISCOverviewProjectStates.RUNNING, true,
					selectedSchedules);
			discOverview.selectOverviewState(DISCOverviewProjectStates.RUNNING);
			waitForElementVisible(discOverviewProjects.getRoot());
			processesMap.remove("Disable Running Schedule 2");
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.RUNNING.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, checkedSchedules);
			browser.get(expectedSchedules.get(
					Arrays.asList("Disable Running Schedule 2", "longTimeRunningGraph.grf")).get(0));
			waitForElementVisible(scheduleDetail.getRoot());
			assertTrue(scheduleDetail.getEnableButton().isDisplayed());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
		}
	}

	@Test(enabled = false, dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void disableScheduledProjects() throws JSONException, InterruptedException {
		Map<String, String> additionalProjects = createMultipleProjects(
				"Disc-test-scheduled-state", 1);
		try {
			prepareDataForScheduledStateTests(projectTitle, testParams.getProjectId(),
					additionalProjects, null, null, "Disable Scheduled Projects");
			openOverviewPage();
			checkStateNumber(DISCOverviewProjectStates.SCHEDULED, 1);
			checkBulkAction(DISCOverviewProjectStates.SCHEDULED, true);
			checkOtherStateNumbers(DISCOverviewProjectStates.ALL, projectTitle,
					testParams.getProjectId());
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
			deleteProjects(additionalProjects);
		}
	}

	@Test(enabled = false, dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void stopScheduledProjects() throws JSONException, InterruptedException {
		Map<String, String> additionalProjects = createMultipleProjects(
				"Disc-test-scheduled-state", 1);
		try {
			Map<String, String> processesMap = new HashMap<String, String>();
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			prepareDataForScheduledStateTests(projectTitle, testParams.getProjectId(),
					additionalProjects, processesMap, expectedSchedules, "Stop Scheduled Projects");
			String scheduleDetailUrl = browser.getCurrentUrl();
			openOverviewPage();
			checkStateNumber(DISCOverviewProjectStates.SCHEDULED, 1);
			checkBulkAction(DISCOverviewProjectStates.SCHEDULED, false);
			checkOtherStateNumbers(DISCOverviewProjectStates.FAILED, projectTitle,
					testParams.getProjectId());
			browser.get(scheduleDetailUrl);
			waitForElementVisible(scheduleDetail.getRoot());
			Thread.sleep(3000);
			expectedSchedules.put(
					Arrays.asList("Stop Scheduled Projects", "longTimeRunningGraph.grf"),
					Arrays.asList(browser.getCurrentUrl(), null,
							scheduleDetail.getLastExecutionDate(),
							scheduleDetail.getLastExecutionTime(),
							scheduleDetail.getExecutionError()));
			openOverviewPage();
			waitForElementVisible(discOverview.getRoot());
			discOverview.selectOverviewState(DISCOverviewProjectStates.FAILED);
			discOverviewProjects.assertOverviewProcesses(
					DISCOverviewProjectStates.STOPPED.getOption(), projectTitle,
					testParams.getProjectId(), processesMap, expectedSchedules);
		} finally {
			openProjectDetailByUrl(testParams.getProjectId());
			projectDetailPage.deleteAllProcesses();
			deleteProjects(additionalProjects);
		}
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkProjectsNotAdminInFailedState() throws ParseException, IOException,
			JSONException, InterruptedException {
		checkProjectNotAdmin(DISCOverviewProjectStates.FAILED, projectTitle,
				testParams.getProjectId(), "Check Failed State Number", "errorGraph");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkProjectsNotAdminInSucessfulState() throws ParseException, IOException,
			JSONException, InterruptedException {
		checkProjectNotAdmin(DISCOverviewProjectStates.SUCCESSFUL, projectTitle,
				testParams.getProjectId(), "Check Overview Successful Project", "successfulGraph");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "project-overview" })
	public void checkProjectsNotAdminInRunningState() throws ParseException, IOException,
			JSONException, InterruptedException {
		checkProjectNotAdmin(DISCOverviewProjectStates.RUNNING, projectTitle,
				testParams.getProjectId(), "Check Overview Running Project", "longTimeRunningGraph");

	}

	@Test(dependsOnGroups = { "project-overview" }, groups = { "tests" })
	public void test() throws JSONException {
		successfulTest = true;
	}

	protected void checkFilteredOutProject(DISCOverviewProjectStates state, String projectName,
			String projectId) throws InterruptedException {
		discOverview.selectOverviewState(state);
		waitForElementVisible(discOverviewProjects.getRoot());
		if (discOverview.assertOverviewStateNumber(state, 0))
			discOverviewProjects.assertOverviewEmptyState(state);
		else
			assertNull(discOverviewProjects.getOverviewProjectDetail(projectName, projectId, true));
	}

	protected void checkFilteredProject(DISCOverviewProjectStates state, String projectName,
			String projectId) throws InterruptedException {
		discOverview.selectOverviewState(state);
		waitForElementVisible(discOverviewProjects.getRoot());
		for (int i = 0; i < 10 && discOverviewProjects.getRoot().getText().isEmpty(); i++)
			Thread.sleep(1000);
		assertNotNull(discOverviewProjects.getOverviewProjectDetail(projectName, projectId, true));
	}

	protected void checkStateNumber(DISCOverviewProjectStates state, int number)
			throws InterruptedException {
		assertTrue(discOverview.assertOverviewStateNumber(state, number));
	}

	protected void checkOtherStateNumbers(DISCOverviewProjectStates state, String projectName,
			String projectId) throws InterruptedException {
		List<DISCOverviewProjectStates> projectStateToCheck = Arrays.asList(
				DISCOverviewProjectStates.FAILED, DISCOverviewProjectStates.RUNNING,
				DISCOverviewProjectStates.SCHEDULED, DISCOverviewProjectStates.SUCCESSFUL);
		for (DISCOverviewProjectStates projectState : projectStateToCheck) {
			if (projectState != state) {
				if (state != DISCOverviewProjectStates.SCHEDULED)
					checkFilteredOutProject(projectState, projectName, projectId);
				if (state == DISCOverviewProjectStates.SCHEDULED
						&& projectState != DISCOverviewProjectStates.RUNNING) {
					checkFilteredOutProject(projectState, projectName, projectId);
				}
			}
		}
	}

	protected void openOverviewPage() throws InterruptedException {
		openUrl(DISC_OVERVIEW_PAGE);
		for (int i = 0; i < 5 && discOverview.getFailedStateNumber().isEmpty(); i++)
			Thread.sleep(1000);
		waitForElementVisible(discOverviewProjects.getRoot());
	}

	public void checkBulkAction(DISCOverviewProjectStates state, boolean isDisable)
			throws InterruptedException {
		discOverview.selectOverviewState(state);
		waitForElementVisible(discOverviewProjects.getRoot());
		discOverviewProjects.checkAllProjects();
		discOverviewProjects.bulkAction(state, isDisable);
	}

	public void checkBulkActionForSelectedProjects(DISCOverviewProjectStates state,
			boolean isDisable, Map<String, String> projectsMap) throws InterruptedException {
		discOverview.selectOverviewState(state);
		waitForElementVisible(discOverviewProjects.getRoot());
		discOverviewProjects.checkOnSelectedProjects(projectsMap);
		discOverviewProjects.bulkAction(state, isDisable);
	}

	public void checkBulkActionForSelectedSchedules(DISCOverviewProjectStates state,
			boolean isDisable, Map<String, List<String>> selectedSchedules)
			throws InterruptedException {
		discOverview.selectOverviewState(state);
		waitForElementVisible(discOverviewProjects.getRoot());
		discOverviewProjects.checkOnSelectedSchedules(projectTitle, testParams.getProjectId(),
				selectedSchedules);
		discOverviewProjects.bulkAction(state, isDisable);
	}

	public void prepareDataForScheduledStateTests(String projectName, String projectId,
			Map<String, String> additionalProjects, Map<String, String> processesMap,
			Map<List<String>, List<String>> expectedSchedules, String processNameFormat)
			throws JSONException, InterruptedException {
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_15_MINUTES.getCronTime(), null);
		openProjectDetailPage(projectName, projectId);
		deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
				processNameFormat,
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		if (processesMap != null)
			processesMap.put(processNameFormat, browser.getCurrentUrl());
		for (Entry<String, String> project : additionalProjects.entrySet()) {
			openProjectDetailPage(project.getKey(), project.getValue());
			for (int i = 1; i < 10; i++) {
				deployInProjectDetailPage(project.getKey(), project.getValue(), "Basic",
						DISCProcessTypes.GRAPH, processNameFormat + " " + i,
						Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
								"successfulGraph.grf"), true);
			}
			for (int i = 1; i < 10; i++) {
				createScheduleForProcess(project.getKey(), project.getValue(), processNameFormat
						+ " " + i, "/graph/longTimeRunningGraph.grf", cronTime, null);
				assertNewSchedule(processNameFormat + " " + i, "longTimeRunningGraph.grf",
						cronTime, null);
				scheduleDetail.manualRun();
				scheduleDetail.isInRunningState();
			}
		}
		openProjectDetailPage(projectName, projectId);
		createScheduleForProcess(projectName, projectId, processNameFormat,
				"/graph/longTimeRunningGraph.grf", cronTime, null);
		assertNewSchedule(processNameFormat, "longTimeRunningGraph.grf", cronTime, null);
		scheduleDetail.manualRun();
		if (expectedSchedules != null)
			expectedSchedules.put(Arrays.asList(processNameFormat, "longTimeRunningGraph.grf"),
					Arrays.asList(browser.getCurrentUrl()));
	}

	public void checkStateNumber(DISCOverviewProjectStates state, String projectName,
			String projectId, String processName, String graphName) throws JSONException,
			InterruptedException {
		try {
			prepareDataForCheckingProjectState(state, projectName, projectId, processName,
					graphName, null, null);
			openOverviewPage();
			checkStateNumber(state, 1);
			checkOtherStateNumbers(state, projectName, projectId);
		} finally {
			openProjectDetailPage(projectName, projectId);
			projectDetailPage.deleteAllProcesses();
		}
	}

	public void prepareDataForCheckingProjectState(DISCOverviewProjectStates state,
			String projectName, String projectId, String processName, String graphName,
			Map<String, String> processesMap, Map<List<String>, List<String>> expectedSchedules)
			throws JSONException, InterruptedException {
		openProjectDetailPage(projectName, projectId);
		deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
				processName,
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		if (processesMap != null)
			processesMap.put(processName, browser.getCurrentUrl());
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
		createScheduleForProcess(projectName, projectId, processName, "/graph/" + graphName
				+ ".grf", null, null);
		assertNewSchedule(processName, graphName + ".grf", cronTime, null);
		scheduleDetail.manualRun();
		if (state == DISCOverviewProjectStates.RUNNING) {
			assertTrue(scheduleDetail.isInRunningState());
			if (expectedSchedules != null)
				expectedSchedules.put(Arrays.asList(processName, graphName + ".grf"),
						Arrays.asList(browser.getCurrentUrl()));
		} else {
			boolean isSuccessful = (state == DISCOverviewProjectStates.SUCCESSFUL ? true : false);
			scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, "Basic/graph/"
					+ graphName + ".grf", DISCProcessTypes.GRAPH, 5);
			if (expectedSchedules != null)
				expectedSchedules.put(Arrays.asList(processName, graphName + ".grf"), Arrays
						.asList(browser.getCurrentUrl(), scheduleDetail.getExecutionRuntime(),
								scheduleDetail.getLastExecutionDate(),
								scheduleDetail.getLastExecutionTime(),
								scheduleDetail.getExecutionError()));
		}
	}

	public void prepareDataForCheckingScheduleState(DISCOverviewProjectStates state,
			String projectName, String projectId, String processName, String graphName,
			Map<String, String> processesMap, Map<List<String>, List<String>> expectedSchedules)
			throws JSONException, InterruptedException {
		openProjectDetailPage(projectName, projectId);
		deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
				processName + " 1",
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		processesMap.put(processName + " 1", browser.getCurrentUrl());
		deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
				processName + " 2",
				Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf", "successfulGraph.grf"),
				true);
		processesMap.put(processName + " 2", browser.getCurrentUrl());
		Pair<String, List<String>> cronTime = Pair.of(
				ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
		createScheduleForProcess(projectName, projectId, processName + " 1", "/graph/" + graphName
				+ ".grf", null, null);
		assertNewSchedule(processName + " 1", graphName + ".grf", cronTime, null);
		scheduleDetail.manualRun();
		if (state == DISCOverviewProjectStates.RUNNING) {
			assertTrue(scheduleDetail.isInRunningState());
			if (expectedSchedules != null)
				expectedSchedules.put(Arrays.asList(processName + " 1", graphName + ".grf"),
						Arrays.asList(browser.getCurrentUrl()));
		} else {
			boolean isSuccessful = (state == DISCOverviewProjectStates.SUCCESSFUL ? true : false);
			scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, "Basic/graph/"
					+ graphName + ".grf", DISCProcessTypes.GRAPH, 5);
			if (expectedSchedules != null)
				expectedSchedules.put(Arrays.asList(processName + " 1", graphName + ".grf"), Arrays
						.asList(browser.getCurrentUrl(), scheduleDetail.getExecutionRuntime(),
								scheduleDetail.getLastExecutionDate(),
								scheduleDetail.getLastExecutionTime(),
								scheduleDetail.getExecutionError()));
		}
		createScheduleForProcess(projectName, projectId, processName + " 2", "/graph/" + graphName
				+ ".grf", null, null);
		assertNewSchedule(processName + " 2", graphName + ".grf", cronTime, null);
		scheduleDetail.manualRun();
		if (state == DISCOverviewProjectStates.RUNNING) {
			assertTrue(scheduleDetail.isInRunningState());
			if (expectedSchedules != null)
				expectedSchedules.put(Arrays.asList(processName + " 2", graphName + ".grf"),
						Arrays.asList(browser.getCurrentUrl()));
		} else {
			boolean isSuccessful = (state == DISCOverviewProjectStates.SUCCESSFUL ? true : false);
			scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, "Basic/graph/"
					+ graphName + ".grf", DISCProcessTypes.GRAPH, 5);
			if (expectedSchedules != null)
				expectedSchedules.put(Arrays.asList(processName + " 2", graphName + ".grf"), Arrays
						.asList(browser.getCurrentUrl(), scheduleDetail.getExecutionRuntime(),
								scheduleDetail.getLastExecutionDate(),
								scheduleDetail.getLastExecutionTime(),
								scheduleDetail.getExecutionError()));
		}
	}

	public void checkProjectNotAdmin(DISCOverviewProjectStates state, String projectName,
			String projectId, String processName, String graphName) throws InterruptedException,
			JSONException, ParseException, IOException {
		try {
			Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
			openProjectDetailPage(projectName, projectId);
			deployInProjectDetailPage(projectName, projectId, "Basic", DISCProcessTypes.GRAPH,
					processName, Arrays.asList("errorGraph.grf", "longTimeRunningGraph.grf",
							"successfulGraph.grf"), true);
			Map<String, String> processesMap = new HashMap<String, String>();
			processesMap.put(processName, browser.getCurrentUrl());
			Pair<String, List<String>> cronTime = Pair.of(
					ScheduleCronTimes.CRON_EVERYHOUR.getCronTime(), null);
			createScheduleForProcess(projectName, projectId, processName, "/graph/" + graphName
					+ ".grf", null, null);
			assertNewSchedule(processName, graphName + ".grf", cronTime, null);
			scheduleDetail.manualRun();
			if (state == DISCOverviewProjectStates.RUNNING)
				assertTrue(scheduleDetail.isInRunningState());
			else {
				boolean isSuccessful = state == DISCOverviewProjectStates.SUCCESSFUL ? true : false;
				scheduleDetail.assertLastExecutionDetails(isSuccessful, true, false, "Basic/graph/"
						+ graphName + ".grf", DISCProcessTypes.GRAPH, 5);
			}
			expectedSchedules.put(Arrays.asList(processName, graphName + ".grf"),
					Arrays.asList(browser.getCurrentUrl()));
			addUsersWithOtherRolesToProject();
			openUrl(PAGE_PROJECTS);
			logout();
			signIn(false, UserRoles.VIEWER);
			openUrl(DISC_OVERVIEW_PAGE);
			discOverview.selectOverviewState(state);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.checkProjectNotAdmin(state.getOption(), projectName, projectId,
					processesMap, expectedSchedules);
			openUrl(PAGE_PROJECTS);
			logout();
			signIn(false, UserRoles.EDITOR);
			openUrl(DISC_OVERVIEW_PAGE);
			discOverview.selectOverviewState(state);
			waitForElementVisible(discOverviewProjects.getRoot());
			discOverviewProjects.checkProjectNotAdmin(state.getOption(), projectName, projectId,
					processesMap, expectedSchedules);
		} finally {
			openUrl(PAGE_PROJECTS);
			logout();
			signIn(false, UserRoles.ADMIN);
			openProjectDetailByUrl(projectId);
			projectDetailPage.deleteAllProcesses();
		}
	}

	protected void disableProjectsOnOverviewPage(DISCOverviewProjectStates state,
			String projectName, String projectId, String processName, String graphName)
			throws JSONException, InterruptedException {
		Map<List<String>, List<String>> expectedSchedules = new HashMap<List<String>, List<String>>();
		prepareDataForCheckingProjectState(state, projectName, projectId, processName, graphName,
				null, expectedSchedules);
		openOverviewPage();
		waitForElementVisible(discOverview.getRoot());
		checkBulkActionForSelectedProjects(state, true, getProjectsMap());
		checkOtherStateNumbers(DISCOverviewProjectStates.ALL, projectName, projectId);
		browser.get(expectedSchedules.get(Arrays.asList(processName, graphName + ".grf")).get(0));
		waitForElementVisible(scheduleDetail.getRoot());
		assertTrue(scheduleDetail.getEnableButton().isDisplayed());
	}
}
