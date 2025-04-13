package com.startup.market.sales.items;

import com.fasterxml.jackson.annotation.JsonInclude;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbAtomicCounter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetaDataItem {

    private String identify;

    private Long incrementId;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getIdentify() {
        return identify;
    }

    public void setIdentify(final String identify) {
        this.identify = identify;
    }

    @DynamoDbAtomicCounter
    @DynamoDbAttribute("increment_id")
    public Long getIncrementId() {
        return incrementId;
    }

    public void setIncrementId(final Long incrementId) {
        this.incrementId = incrementId;
    }

}
