package com.gooddata.qa.graphene.fragments;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DataSourceList extends AbstractFragment {

    private By BY_DATASET = By
            .xpath("//.[contains(@class, 'dataset')]/../.[not(contains(@class, 'is-none'))]");
    private By BY_DROPRIGHT_ICON = By.cssSelector(".icon-dropright");
    private By BY_DROPDOWN_ICON = By.cssSelector(".icon-dropdown");

    private String XPATH_SOURCE = "//.[@class='source-title' and text()='${datasource}']/../.";
    private String XPATH_DATASET =
            "//.[contains(@class, 'dataset')]//label[text()='${dataset}']/../../.";
    private String XPATH_FIELD = "//.[text()='${fieldName}']";
    private String XPATH_FIELDS =
            "//.[contains(@class, 'dataset')]//label[text()='${dataset}']//../..//li[not(contains(@class, 'is-none'))]";


    public void clickOnFields(DataSource dataSource, Dataset dataset, boolean isChecked,
            Field... fields) {
        WebElement dataSourceElement = selectDataSource(dataSource);
        WebElement datasetElement = selectDataset(dataSourceElement, dataset);
        for (Field field : fields) {
            clickOnField(datasetElement, field, isChecked);
        }
    }

    public void chechAvailableDataSource(DataSource dataSource,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        WebElement datasourceElement = selectDataSource(dataSource);
        assertEquals(datasourceElement.findElements(BY_DATASET).size(),
                datasetInSpecificFilter.size());
        checkAvailableDatasets(datasourceElement, datasetInSpecificFilter, fieldType);
    }

    private void clickOnField(WebElement datasetElement, Field field, boolean isChecked) {
        final WebElement fieldElement =
                waitForElementVisible(
                        By.xpath(XPATH_FIELD.replace("${fieldName}", field.getName())),
                        datasetElement);
        fieldElement.click();
        if (isChecked)
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return fieldElement.getAttribute("class").contains("is-strong");
                }
            });
        else
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return !fieldElement.getAttribute("class").contains("is-strong");
                }
            });
    }


    private void checkAvailableDatasets(WebElement dataSourceElement,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        for (Dataset dataset : datasetInSpecificFilter) {
            WebElement datasetElement = selectDataset(dataSourceElement, dataset);
            List<Field> fieldsInSpecificFilter = dataset.getFieldsInSpecificFilter(fieldType);
            assertEquals(
                    datasetElement.findElements(
                            By.xpath(XPATH_FIELDS.replace("${dataset}", dataset.getName()))).size(),
                    fieldsInSpecificFilter.size(), "Incorrect number of fields in dataset: "
                            + dataset.getName());
            checkAvailableFieldsOfDataset(datasetElement, fieldsInSpecificFilter);
        }
    }

    private void checkAvailableFieldsOfDataset(final WebElement datasetElement,
            Collection<Field> fields) {
        assertTrue(Iterables.all(fields, new Predicate<Field>() {

            @Override
            public boolean apply(Field field) {
                return datasetElement.findElements(
                        By.xpath(XPATH_FIELD.replace("${fieldName}", field.getName()))).size() == 1;
            }
        }));
    }

    private WebElement selectDataSource(DataSource dataSource) {
        WebElement dataSourceElement =
                waitForElementVisible(
                        By.xpath(XPATH_SOURCE.replace("${datasource}", dataSource.getName())),
                        browser);
        expandElement(dataSourceElement);

        return dataSourceElement;
    }

    private WebElement selectDataset(WebElement dataSourceElement, Dataset dataset) {
        WebElement datasetElement =
                waitForElementVisible(
                        By.xpath(XPATH_DATASET.replace("${dataset}", dataset.getName())),
                        dataSourceElement);
        expandElement(datasetElement);

        return datasetElement;
    }

    private void expandElement(WebElement element) {
        if (!element.findElements(BY_DROPRIGHT_ICON).isEmpty())
            waitForElementPresent(BY_DROPRIGHT_ICON, element).click();;
        waitForElementVisible(BY_DROPDOWN_ICON, element);
    }
}
