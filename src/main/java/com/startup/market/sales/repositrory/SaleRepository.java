package com.startup.market.sales.repositrory;

import com.startup.market.sales.constants.Status;
import com.startup.market.sales.items.MetaDataItem;
import com.startup.market.sales.items.SaleItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues.numberValue;

@Repository
public class SaleRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(SaleRepository.class);
	private final DynamoDbEnhancedAsyncClient enhancedAsyncClient;
	private final DynamoDbAsyncTable<SaleItem> saleDynamoDbAsyncTable;
	private final DynamoDbAsyncTable<MetaDataItem> metaDataDynamoDbAsyncTable;

	public SaleRepository(@Value("${aws.dynamodb.market.sale.table.name}") final String marketSaleTableName,
			@Value("${aws.dynamodb.metadata.table.name}") final String metadataTableName,
			final DynamoDbEnhancedAsyncClient asyncClient) {
		this.enhancedAsyncClient = asyncClient;
		this.saleDynamoDbAsyncTable = enhancedAsyncClient.table(marketSaleTableName, TableSchema.fromBean(SaleItem.class));
		this.metaDataDynamoDbAsyncTable = enhancedAsyncClient.table(metadataTableName, TableSchema.fromBean(MetaDataItem.class));
	}

	public Mono<SaleItem> retrieveByKey(final String identify) {
		//@formatter:off
		final CompletableFuture<SaleItem> futureSaleByKey = saleDynamoDbAsyncTable.getItem(Key.builder().partitionValue(identify).build())
				.handle((response, exception) -> {
					if (exception != null) {
						LOGGER.error(exception.getMessage());
						throw new RuntimeException("Error happened during retrieve sale");
					}
					return response;
				});
		return Mono.fromFuture(futureSaleByKey)
				.onErrorMap(exception -> new RuntimeException(exception.getMessage()));
		//@formatter:on
	}

	public Flux<Page<SaleItem>> retrieveByStatus(final Status status) {
		//@formatter:off
		final QueryConditional queryConditional = QueryConditional
				.keyEqualTo(Key.builder().partitionValue(status.name()).build());
		final DynamoDbAsyncIndex<SaleItem> saleStatusIndex = saleDynamoDbAsyncTable.index("status_index");
		final SdkPublisher<Page<SaleItem>> pagedResult = saleStatusIndex
				.query(q -> q.queryConditional(queryConditional))
				.doAfterOnError(throwable -> LOGGER.error(throwable.getMessage()));
		return Flux.from(pagedResult)
				.onErrorMap(exception -> new RuntimeException("Error happened during retrieve sales by status"));
		//@formatter:on
	}

	public Mono<SaleItem> createSale(final SaleItem saleToSave) {
		//@formatter:off
		final CompletableFuture<SaleItem> saleCreateFuture = getMetadataByKey("Sale")
				.thenComposeAsync(saleMetadata -> {
					saleToSave.setIdentify(String.valueOf(saleMetadata.getIncrementId()));
					return transactionCreateSale(saleToSave, saleMetadata);
				});

		return Mono.fromFuture(saleCreateFuture)
				.onErrorMap(
						exception -> new RuntimeException(exception.getMessage()));
		//@formatter:on
	}

	private CompletableFuture<MetaDataItem> getMetadataByKey(final String key) {
		//@formatter:off
		return metaDataDynamoDbAsyncTable.getItem(Key.builder().partitionValue(key).build())
				.handle((response, exception) -> {
					if (exception != null) {
						LOGGER.error(exception.getMessage());
						throw new RuntimeException("Error happened during retrieve sale metadata");
					}
					if (response == null) {
						LOGGER.error("No metadata found for sale");
						throw new RuntimeException("No metadata found for sale");
					}
					return response;
				});
		//@formatter:on
	}

	private CompletableFuture<SaleItem> transactionCreateSale(final SaleItem saleToSave, final MetaDataItem saleMetadata) {
		try {
			//@formatter:off
			return this.enhancedAsyncClient
					.transactWriteItems(b -> b
							.addUpdateItem(metaDataDynamoDbAsyncTable,
									TransactUpdateItemEnhancedRequest.builder(MetaDataItem.class)
									.item(saleMetadata)
									.conditionExpression(Expression.builder().expression("increment_id = :current_id")
											.expressionValues(Map.of(":current_id",
													numberValue(saleMetadata.getIncrementId())))
											.build()).build())
							.addPutItem(saleDynamoDbAsyncTable, TransactPutItemEnhancedRequest
									.builder(SaleItem.class).item(saleToSave)
									.build()))
					.handle((response, exception) -> {
						if (exception != null) {
							LOGGER.error(exception.getMessage());
							throw new RuntimeException("Error happened during creating sale");
						}
						return saleToSave;
					});

		} catch (TransactionCanceledException exception) {
			exception.cancellationReasons()
					.stream()
					.forEach(cancellationReason -> LOGGER.info(cancellationReason.toString()));
			throw exception;
		}
		//@formatter:on
	}

}
