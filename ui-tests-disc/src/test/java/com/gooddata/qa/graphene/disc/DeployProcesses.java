package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;

public class DeployProcesses extends AbstractDeployProcesses {


    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-deploy-process";
    }

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployCloudConnectInProjectsPage() throws JSONException, InterruptedException {
		deployInProjectsPage(getProjectsMap(), "cloudconnect", DISCProcessTypes.GRAPH,
				"CloudConnect - Projects List Page", Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployRubyInProjectsPage() throws JSONException, InterruptedException {
		deployInProjectsPage(getProjectsMap(), "ruby", DISCProcessTypes.RUBY,
				"Ruby - Projects List Page", Arrays.asList("ruby1.rb", "ruby2.rb"), true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployCloudConnectInProjectDetailPage() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.GRAPH, "CloudConnect - Project Detail Page",
				Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployRubyInProjectDetailPage() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "ruby",
				DISCProcessTypes.RUBY, "Ruby - Project Detail Page",
				Arrays.asList("ruby1.rb", "ruby2.rb"), true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void redeployProcessWithDifferentPackage() throws JSONException, InterruptedException {
		List<String> executablesList = Arrays.asList("01 - REST_GET_1.grf", "02 - REST_POST.grf",
				"03 - REST_PUT.grf", "04 - REST_DELETE.grf");
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.GRAPH, "Process to redeploy with different package",
				Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
		redeployProcess(projectTitle, testParams.getProjectId(),
				"Process to redeploy with different package", "executables",
				"Redeploy with different package", DISCProcessTypes.GRAPH, executablesList, true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void redeployProcessWithDifferentProcessType() throws JSONException,
			InterruptedException {
		List<String> executablesList = Arrays.asList("01 - REST_GET_1.grf", "02 - REST_POST.grf",
				"03 - REST_PUT.grf", "04 - REST_DELETE.grf");
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "executables",
				DISCProcessTypes.GRAPH, "Process to redeploy with different process type",
				executablesList, true);
		redeployProcess(projectTitle, testParams.getProjectId(),
				"Process to redeploy with different process type", "executables",
				"Redeploy with different process type", DISCProcessTypes.RUBY,
				Arrays.asList("ruby1.rb", "ruby2.rb"), true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void redeployProcessWithSamePackage() throws JSONException, InterruptedException {
		List<String> executablesList = Arrays.asList("01 - REST_GET_1.grf", "02 - REST_POST.grf",
				"03 - REST_PUT.grf", "04 - REST_DELETE.grf");
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "executables",
				DISCProcessTypes.GRAPH, "Process to redeploy with the same package",
				executablesList, true);
		redeployProcess(projectTitle, testParams.getProjectId(),
				"Process to redeploy with the same package", "executables",
				"Redeploy with the same package", DISCProcessTypes.GRAPH, executablesList, true);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void emptyInputErrorDeployment() throws InterruptedException {
		Map<String, String> projects = new LinkedHashMap<String, String>();
		projects.put(projectTitle, testParams.getProjectId());
		openUrl(DISC_PROJECTS_PAGE_URL);
		selectProjectsToDeployInProjectsPage(projects);
		deployForm.assertErrorOnDeployForm("", DISCProcessTypes.DEFAULT, "");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void notZipFileErrorDeployment() throws InterruptedException {
		openUrl(DISC_PROJECTS_PAGE_URL);
		selectProjectsToDeployInProjectsPage(getProjectsMap());
		deployForm.assertErrorOnDeployForm(zipFilePath + "not-zip-file.7z",
				DISCProcessTypes.DEFAULT, "Not zip file");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void tooLargeZipFileErrorDeployment() throws InterruptedException {
		openUrl(DISC_PROJECTS_PAGE_URL);
		selectProjectsToDeployInProjectsPage(getProjectsMap());
		deployForm.assertErrorOnDeployForm(zipFilePath + "too-large-file.zip",
				DISCProcessTypes.DEFAULT, "Too large file");
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployWithoutExecutablesInProjectsPage() throws JSONException, InterruptedException {
		deployInProjectsPage(getProjectsMap(), "not-executables", DISCProcessTypes.GRAPH,
				"Not Executables", null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployCloudConnectWithRubyTypeInProjectsPage() throws JSONException,
			InterruptedException {
		deployInProjectsPage(getProjectsMap(), "cloudconnect", DISCProcessTypes.RUBY,
				"CloudConnect with Ruby type", null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployRubyWithCloudConnectTypeInProjectsPage() throws JSONException,
			InterruptedException {
		deployInProjectsPage(getProjectsMap(), "ruby", DISCProcessTypes.GRAPH,
				"Ruby with CloudConnect type", null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployWithoutExecutablesInProjectDetailPage() throws JSONException,
			InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "not-executables",
				DISCProcessTypes.GRAPH, "Not Executables", null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployCloudConnectWithRubyTypeInProjectDetailPage() throws JSONException,
			InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.RUBY, "CloudConnect with Ruby type", null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void deployRubyWithCloudConnectTypeInProjectDetailPage() throws JSONException,
			InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "ruby",
				DISCProcessTypes.GRAPH, "Ruby with CloudConnect type", null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void redeployWithoutExecutables() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.GRAPH, "Process to redeploy without executables",
				Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
		redeployProcess(projectTitle, testParams.getProjectId(),
				"Process to redeploy without executables", "not-executables",
				"Redeploy without executables", DISCProcessTypes.GRAPH, null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void redeployCloudConnectWithRubyType() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "cloudconnect",
				DISCProcessTypes.GRAPH, "CloudConnect process to redeploy with Ruby type",
				Arrays.asList("DWHS1.grf", "DWHS2.grf"), true);
		redeployProcess(projectTitle, testParams.getProjectId(),
				"CloudConnect process to redeploy with Ruby type", "cloudconnect",
				"Redeploy CloudConnect process with Ruby type", DISCProcessTypes.RUBY, null, false);
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void redeployRubyWithCloudConnectType() throws JSONException, InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "ruby",
				DISCProcessTypes.RUBY, "Ruby process to redeploy with CloudConnect type",
				Arrays.asList("ruby1.rb", "ruby2.rb"), true);
		redeployProcess(projectTitle, testParams.getProjectId(),
				"Ruby process to redeploy with CloudConnect type", "ruby",
				"Redeploy with different process type", DISCProcessTypes.GRAPH, null, false);
	}
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void checkDeployDialogMessageInProjectDetail() throws InterruptedException {
		openProjectDetailPage(projectTitle, testParams.getProjectId());
		checkDeployDialogMessageInProjectDetail(projectTitle, testParams.getProjectId(), "Basic",
				DISCProcessTypes.GRAPH, "Check deploy dialog message in project detail", true);
	}
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void checkDeployDialogMessageInProjectsPage() throws InterruptedException {
		checkDeployDialogMessageInProjectsPage(getProjectsMap(), "Basic",
				DISCProcessTypes.GRAPH, "Check deploy dialog message in projects page", true);
	}
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "deploy" })
	public void checkFailedDeployMessageInProjectsPage() throws InterruptedException {
		checkDeployDialogMessageInProjectsPage(getProjectsMap(), "Basic",
				DISCProcessTypes.RUBY, "Check failed deploy dialog message in projects page", false);
	}

	@Test(dependsOnGroups = { "deploy" }, groups = { "tests" })
	public void test() throws JSONException {
		successfulTest = true;
	}
}
