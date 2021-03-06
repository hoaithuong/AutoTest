package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import java.util.Collection;
import java.util.List;

import com.gooddata.qa.graphene.enums.DateGranularity;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

/**
 * s-date-dimension-switch renamed to s-date-dataset-switch
 */
public class AttributesBucket extends AbstractBucket {

    private static final By BY_DATE_DATASET_SELECT = By.className("adi-date-dataset-switch");
    private static final By BY_DATE_GRANULARITY_SELECT = By.className("adi-date-granularity-switch");
    private static final By BY_VIEW_BY_WARNING = className("adi-stack-warn");
    private static final String BUCKET_WITH_WARN_MESSAGE = "bucket-with-warn-message";

    public List<String> getItemNames() {
        return getElementTexts(items, e -> e.findElement(BY_HEADER));
    }

    public void changeGranularity(DateGranularity dateGranularity) {
        getDateGranularitySelect().selectByName(dateGranularity.toString());
    }

    public String getSelectedGranularity() {
        return getDateGranularitySelect().getRoot().getText();
    }

    public Collection<String> getAllGranularities() {
        return getDateGranularitySelect().getValues();
    }

    public String getSelectedDimensionSwitch() {
        return getDateDatasetSelect().getRoot().getText();
    }

    public void changeDateDimension(String switchDimension) {
        getDateDatasetSelect().selectByName(switchDimension);
    }

    public WebElement getFirst() {
        return items.get(0);
    }

    public WebElement get(final String name) {
        return items.stream()
                .filter(e -> name.equals(e.findElement(BY_HEADER).getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find attribute: " + name));
    }

    @Override
    public String getWarningMessage() {
        return waitForElementVisible(BY_VIEW_BY_WARNING, getRoot()).getText().trim();
    }

    public DateDimensionSelect getDateDatasetSelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(BY_DATE_DATASET_SELECT, browser));
    }

    private DateDimensionSelect getDateGranularitySelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(BY_DATE_GRANULARITY_SELECT, browser));
    }

    public String getAttributeName() {
        if (isEmpty()) {
            return "";
        }
        return waitForElementVisible(BY_HEADER, getAttributeItem()).getText().trim();
    }

    private WebElement getAttributeItem() {
        return waitForElementVisible(items.get(0));
    }

    public boolean isDisabled() {
        return getRoot().getAttribute("class").contains(BUCKET_WITH_WARN_MESSAGE);
    }
}
