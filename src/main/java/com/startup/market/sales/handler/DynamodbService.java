package com.startup.market.sales.handler;

import com.startup.market.sales.items.MetaDataItem;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DynamodbService {

	public CompletableFuture<Boolean> createTable(final DynamoDbAsyncClient ddb,
			final String tableName,
			final List<AttributeDefinition> attributes,
			final List<KeySchemaElement> keys,
			final List<GlobalSecondaryIndex> indexes) {
		final CreateTableRequest.Builder builder = CreateTableRequest.builder();

		builder.attributeDefinitions(attributes)
				.keySchema(keys);

		if (indexes != null) {
			builder.globalSecondaryIndexes(indexes);
		}
		final CreateTableRequest request = builder
				.provisionedThroughput(ProvisionedThroughput.builder()
						.readCapacityUnits(10L)
						.writeCapacityUnits(10L)
						.build())
				.tableName(tableName)
				.build();

		return ddb.createTable(request)
				.thenComposeAsync(createTableResponse -> tableExists(ddb, tableName));
	}

	public CompletableFuture<Boolean> tableExists(final DynamoDbAsyncClient ddb, final String tableName) {
		final DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
				.tableName(tableName).build();
		return ddb.describeTable(describeTableRequest)
				.handle((response, exception) -> {
					if (exception != null) {
						return false;
					}
					return true;
				});
	}


	public CompletableFuture<Void> saveMetaData(final DynamoDbEnhancedAsyncClient enhancedClient,
			final String dynamodbTableName, final MetaDataItem metaData) {
		final DynamoDbAsyncTable<MetaDataItem> table = enhancedClient
				.table(dynamodbTableName, TableSchema.fromBean(MetaDataItem.class));
		return table.putItem(metaData);
	}

	public CompletableFuture<Boolean> deleteTable(final DynamoDbAsyncClient ddb, final String tableName) {
		final DeleteTableRequest request = DeleteTableRequest.builder()
				.tableName(tableName)
				.build();
		return tableExists(ddb, tableName)
				.thenCompose(isExist -> ddb.deleteTable(request))
				.handle((response, exception) -> {
					if (exception != null) {
						return false;
					}
					return true;
				});
	}
}
