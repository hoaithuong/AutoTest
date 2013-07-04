package com.gooddata.qa.graphene.fragments.reports;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportsList extends AbstractFragment {
	
	private static final By BY_REPORT_LABEL = By.xpath("h3/a");
	
	@FindBy(css="div.report")
	private List<WebElement> reports;
	
	public List<WebElement> getReports() {
		return reports;
	}
	
	/**
	 * Method to get number of reports
	 * 
	 * @return number of reports
	 */
	public int getNumberOfReports() {
		return reports.size();
	}

	/**
	 * Method for opening report
	 * 
	 * @param i - report index
	 */
	public void openReport(int i) {
		getReportWebElement(i).findElement(BY_REPORT_LABEL).click();
	}
	
	/**
	 * Method for opening report
	 * 
	 * @param reportName - report name
	 */
	public void openReport(String reportName) {
		for (int i = 0; i < reports.size(); i++) {
			if (getReportLabel(i).equals(reportName)) {
				openReport(i);
				return;
			}
		}
		Assert.fail("Folder with given name does not exist!");
	}
	
	/**
	 * Method to get label of report with given index
	 * 
	 * @param i - report index
	 * @return label of report with given index 
	 */
	public String getReportLabel(int i) {
		WebElement elem = getReportWebElement(i).findElement(BY_REPORT_LABEL);
		return elem.getText();
	}
	
	/**
	 * Method to get link of report with given index
	 * 
	 * @param i - report index
	 * @return link of report with given index
	 */
	public String getReportLink(int i) {
		WebElement elem = getReportWebElement(i).findElement(BY_REPORT_LABEL);
		return elem.getAttribute("href");
	}
	
	/**
	 * Method to get all report labels
	 * 
	 * @return List<String> with all report labels
	 */
	public List<String> getAllReportLabels() {
		List<String> reportLabels = new ArrayList<String>();
		for (int i = 0; i < reports.size(); i++) {
			reportLabels.add(getReportLabel(i));
		}
		return reportLabels;
	}
	
	private WebElement getReportWebElement(int i) {
		return reports.get(i);
	}

}
