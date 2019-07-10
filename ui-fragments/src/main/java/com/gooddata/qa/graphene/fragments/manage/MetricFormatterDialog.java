package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricFormatterDialog extends AbstractFragment {

    @FindBy(className = "container-close")
    private WebElement closeButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    @FindBy(className = "formatter-preset-image-bars")
    private WebElement barsFormatter;

    @FindBy(className = "formatter-preset-image-default")
    private WebElement defaultFormatter;

    @FindBy(className = "formatter-preset-image-large")
    private WebElement truncateLargeNumbersFormatter;

    @FindBy(className = "formatter-preset-image-colors")
    private WebElement colorsFormatter;

    public static final By LOCATOR = By.className("c-formatterDialog");

    public void changeFormat(Formatter format) {
        waitForElementVisible(getPresetFormatterFrom(format)).click();
        submit();
    }

    public void changeFormatButDiscard(Formatter format) {
        waitForElementVisible(getPresetFormatterFrom(format)).click();
        discard();
    }

    private WebElement getPresetFormatterFrom(Formatter format) {
        switch (format) {
            case BARS:
                return barsFormatter;
            case TRUNCATE_NUMBERS:
                return truncateLargeNumbersFormatter;
            case COLORS:
                return colorsFormatter;
            default:
                return defaultFormatter;
        }
    }

    private void submit() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void discard() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public static enum Formatter {
        DEFAULT("#,##0.00"),
        GDC("GDC#,##0.00"),
        BARS(new StringBuilder("[>=9][color=2190c0]██████████;")
            .append("[>=8][color=2190c0]█████████░;")
            .append("[>=7][color=2190c0]████████░░;")
            .append("[>=6][color=2190c0]███████░░░;")
            .append("[>=5][color=2190c0]██████░░░░;")
            .append("[>=4][color=2190c0]█████░░░░░;")
            .append("[>=3][color=2190c0]████░░░░░░;")
            .append("[>=2][color=2190c0]███░░░░░░░;")
            .append("[>=1][color=2190c0]██░░░░░░░░;")
            .append("[color=2190c0]█░░░░░░░░░")
            .toString()),
        TRUNCATE_NUMBERS(new StringBuilder("[>=1000000000]$#,,,.0 B;")
            .append("[<=-1000000000]-$#,,,.0 B;")
            .append("[>=1000000]$#,,.0 M;")
            .append("[<=-1000000]-$#,,.0 M;")
            .append("[>=1000]$#,.0 K;")
            .append("[<=-1000]-$#,.0 K;")
            .append("$#,##0")
            .toString()),
        COLORS(new StringBuilder("[<0][red]$#,#.##;")
            .append("[<1000][blue]$#,#.##;")
            .append("[>=1000][green]$#,#.##")
            .toString()),
        UTF_8("#'##0.00 kiểm tra nghiêm khắc"),
        COLORS_FORMAT("[RED]#,##0.00"),
        BACKGROUND_COLOR_FORMAT("[RED][backgroundColor=aff8ef]#,##0.00"),
        XSS("<button>#,##0.00</button>"),
        NULL_VALUE("#'##0,00 formatted; [=null] null value!"),
        LONG("$#,##0,00 long format long format long format long format long format long format long format");

        private String text;

        private Formatter(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
