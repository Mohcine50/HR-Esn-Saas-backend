package com.shegami.hr_saas.shared.constants;


import lombok.experimental.FieldNameConstants;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailConstant {

    // Constants
    public static final String EXCHANGE_NAME = "notification.topic";
    public static final String QUEUE_NAME = "notification.email.queue";
    public static final String ROUTING_KEY = "notification.email.#";

    // Dead Letter Constants
    public static final String DLQ_EXCHANGE_NAME = "notification.dlx";
    public static final String DLQ_QUEUE_NAME = "notification.email.dlq";
    public static final String DLQ_ROUTING_KEY = "notification.email.dlq";

}
