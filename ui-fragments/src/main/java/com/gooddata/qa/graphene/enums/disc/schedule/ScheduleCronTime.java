package com.gooddata.qa.graphene.enums.disc.schedule;

public enum ScheduleCronTime {
    EVERY_15_MINUTES("*/15 * * * *") {
        @Override
        public String getExpression() {
            return expression;
        }
    },
    EVERY_30_MINUTES("*/30 * * * *") {
        @Override
        public String getExpression() {
            return expression;
        }
    },
    EVERY_HOUR,
    EVERY_DAY,
    EVERY_WEEK,
    CRON_EXPRESSION,
    AFTER;

    public String expression;

    private ScheduleCronTime(String expression) {
        this.expression = expression;
    }

    private ScheduleCronTime() {
    }

    public String getExpression() {
        throw new UnsupportedOperationException("Cron time expression is not supported for " + this.name());
    }

    @Override
    public String toString() {
        return this.name().toLowerCase().replaceAll("_", " ");
    }
}
