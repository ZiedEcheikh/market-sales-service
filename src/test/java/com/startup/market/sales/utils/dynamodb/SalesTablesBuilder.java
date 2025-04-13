package com.startup.market.sales.utils.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

public class SalesTablesBuilder {

	public List<AttributeDefinition> saleTableAttributes() {
		final AttributeDefinition attributeHashKey = AttributeDefinition.builder()
				.attributeName("id")
				.attributeType(ScalarAttributeType.S)
				.build();
		final AttributeDefinition attributeStatus = AttributeDefinition.builder()
				.attributeName("status")
				.attributeType(ScalarAttributeType.S)
				.build();
		return List.of(attributeHashKey, attributeStatus);
	}

	public List<KeySchemaElement> tableSaleKeys() {
		final KeySchemaElement hashKeySaleID = KeySchemaElement.builder()
				.attributeName("id")
				.keyType(KeyType.HASH)
				.build();
		return List.of(hashKeySaleID);
	}

	public List<GlobalSecondaryIndex> tableSaleIndexes() {
		final KeySchemaElement hashKeySaleStatus = KeySchemaElement.builder()
				.attributeName("status")
				.keyType(KeyType.HASH)
				.build();
		final GlobalSecondaryIndex statusIndex = GlobalSecondaryIndex.builder()
				.indexName("status_index")
				.keySchema(hashKeySaleStatus)
				.provisionedThroughput(ProvisionedThroughput.builder()
						.readCapacityUnits(10L)
						.writeCapacityUnits(10L)
						.build())
				.projection(Projection.builder()
						.projectionType("INCLUDE")
						.nonKeyAttributes("id", "title", "description", "start_date", "end_date", "create_at")
						.build())
				.build();
		return List.of(statusIndex);
	}


	public List<AttributeDefinition> metadataTableAttributes() {
		final AttributeDefinition attributeHashKey = AttributeDefinition.builder()
				.attributeName("id")
				.attributeType(ScalarAttributeType.S)
				.build();
		return List.of(attributeHashKey);
	}

	public List<KeySchemaElement> metadataTableKeys() {
		final KeySchemaElement hashKeyID = KeySchemaElement.builder()
				.attributeName("id")
				.keyType(KeyType.HASH)
				.build();
		return List.of(hashKeyID);
	}
}
