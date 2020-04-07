package com.gooddata.qa.graphene.enums;

public enum GDEmails {
    NO_REPLY("no-reply@gooddata.com", 5),
    NOREPLY("noreply@gooddata.com", 10),
    NOREPLY_SALES_FORCE("noreply@salesforce.com", 5),
    INVITATION("invitation@gooddata.com", 10),
    REGISTRATION("registration@gooddata.com", 10);

    private String emailAddress;
    private int maxWaitingTimeInMinute;

    GDEmails(String emailAddress, int maxWaitingTimeInMinute) {
        this.emailAddress = emailAddress;
        this.maxWaitingTimeInMinute = maxWaitingTimeInMinute;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public int getMaxWaitingTimeInMinute() {
        return this.maxWaitingTimeInMinute;
    }
}
