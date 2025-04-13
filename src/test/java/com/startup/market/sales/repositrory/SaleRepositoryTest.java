package com.startup.market.sales.repositrory;

import com.startup.market.sales.constants.Status;
import com.startup.market.sales.items.MetaDataItem;
import com.startup.market.sales.items.SaleItem;
import com.startup.market.sales.utils.JsonReader;
import com.startup.market.sales.utils.dynamodb.DynamodbServices;
import com.startup.market.sales.utils.dynamodb.SalesTablesBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;
import java.util.concurrent.ExecutionException;

@Testcontainers
class SaleRepositoryTest {

	@Container
	private static GenericContainer<?> dynamodb = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local"))
			.withExposedPorts(8000);

	private String salePathFile = "/data/sales/models/sale.json";
	private String salesTableName = "market-sales";
	private String metadataTableName = "metadata";
	private String metadataSaleId = "Sale";

	private DynamodbServices dynamodbService;
	private SaleRepository saleRepository;
	private DynamoDbAsyncClient dynamoDbAsyncClient;

	@DynamicPropertySource
	static void dynamicProperties(final DynamicPropertyRegistry registry) {
		registry.add("aws.dynamodb.endpoint", () ->
				"http://localhost:" + dynamodb.getFirstMappedPort() + "/");
	}

	@BeforeAll
	public static void initialize() {
		dynamodb.start();
	}

	@AfterAll
	public static void shutdown() {
		dynamodb.stop();
	}

	@BeforeEach
	@SneakyThrows
	public void setUp() {
		this.dynamodbService = new DynamodbServices();
		final String address = dynamodb.getHost();
		final Integer port = dynamodb.getFirstMappedPort();
		final String customDynamodbEndpoint = "http://" + address + ":" + port;

		this.dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
				.endpointOverride(URI.create(customDynamodbEndpoint))
				.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials()))
				.region(Region.EU_WEST_1)
				.build();
		final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(dynamoDbAsyncClient)
				.build();

		final SalesTablesBuilder dynamodbBuilder = new SalesTablesBuilder();

		this.saleRepository = new SaleRepository(salesTableName, metadataTableName, metadataSaleId, dynamoDbEnhancedAsyncClient);

		this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, salesTableName).get();
		this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, metadataTableName).get();
		if (!this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, metadataTableName).get()) {
			this.dynamodbService.createTable(dynamoDbAsyncClient, metadataTableName,
					dynamodbBuilder.metadataTableAttributes(),
					dynamodbBuilder.metadataTableKeys(), null).get();

			final MetaDataItem metaData = new MetaDataItem();
			metaData.setIncrementId(0L);
			metaData.setIdentify("Sale");

			this.dynamodbService.saveMetaData(dynamoDbEnhancedAsyncClient, metadataTableName, metaData).get();
		}

		if (!this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.salesTableName).get()) {
			this.dynamodbService.createTable(dynamoDbAsyncClient, this.salesTableName,
					dynamodbBuilder.saleTableAttributes(),
					dynamodbBuilder.tableSaleKeys(),
					dynamodbBuilder.tableSaleIndexes()).get();
		}

	}

	@Test
	void shouldRetrieveSalesByStatus() {
		final SaleItem sale = JsonReader.readJsonFile(salePathFile, SaleItem.class);
		final Flux<Page<SaleItem>> pageSaleFuture = saleRepository.createSale(sale)
				.flatMapMany(saleSaved ->
						saleRepository.retrieveByStatus(Status.findByValue(sale.getStatus())));
		StepVerifier
				.create(pageSaleFuture)
				.expectNextMatches(salePage -> salePage.items()
						.stream()
						.anyMatch(saleByStatus -> sale.getIdentify().equals(saleByStatus.getIdentify())))
				.expectComplete()
				.verify();
	}

	@Test
	void shouldRetrieveSalesByStatusNotMatch() {
		StepVerifier
				.create(saleRepository.retrieveByStatus(Status.READY))
				.expectNextMatches(salePage -> salePage.items().isEmpty())
				.expectComplete()
				.verify();
	}

	@Test
	@SneakyThrows
	void shouldRetrieveSalesByStatusServerError() {
		deleteTableIfExists();
		final String expectedExceptionMsg = "Error happened during retrieve sales by status";
		StepVerifier
				.create(saleRepository.retrieveByStatus(Status.DRAFT))
				.expectErrorMatches(throwable -> throwable instanceof RuntimeException
						&& expectedExceptionMsg.equals(throwable.getMessage()))
				.verify();
	}

	@Test
	void shouldRetrieveSaleByKey() {
		final SaleItem sale = JsonReader.readJsonFile(salePathFile, SaleItem.class);
		final Mono<SaleItem> saleByKeyFuture = saleRepository.createSale(sale)
				.flatMap(saleSaved -> saleRepository.retrieveByKey(saleSaved.getIdentify()));
		StepVerifier
				.create(saleByKeyFuture)
				.expectNextMatches(saleByKey ->
						saleByKey.getIdentify().equals(sale.getIdentify()))
				.expectComplete()
				.verify();
	}

	@Test
	void shouldRetrieveSaleByKeyNotMatch() {
		StepVerifier
				.create(saleRepository.retrieveByKey("id_not_exist"))
				.expectNextCount(0)
				.verifyComplete();
	}

	@Test
	@SneakyThrows
	void shouldRetrieveSaleKeyServerError() {
		deleteTableIfExists();
		final String expectedExceptionMsg = "Error happened during retrieve sale";
		StepVerifier
				.create(saleRepository.retrieveByKey("id"))
				.expectErrorMatches(throwable -> throwable instanceof RuntimeException
						&& expectedExceptionMsg.equals(throwable.getMessage()))
				.verify();
	}


	@Test
	void shouldSaveNewSale() {
		final SaleItem sale = JsonReader.readJsonFile(salePathFile, SaleItem.class);
		final Mono<SaleItem> saleSavedByKeyFuture = saleRepository.createSale(sale)
				.flatMap(saleSaved -> saleRepository.retrieveByKey(saleSaved.getIdentify()));
		StepVerifier
				.create(saleSavedByKeyFuture)
				.expectNextMatches(saleByKey ->
						saleByKey.getIdentify().equals(sale.getIdentify()))
				.expectComplete()
				.verify();
	}

	@Test
	@SneakyThrows
	void shouldSaveNewSaleServerError() {
		final SaleItem sale = JsonReader.readJsonFile(salePathFile, SaleItem.class);
		deleteTableIfExists();
		final String expectedExceptionMsg = "Error happened during creating sale";
		StepVerifier
				.create(saleRepository.createSale(sale))
				.expectErrorMatches(throwable -> throwable instanceof RuntimeException
						&& expectedExceptionMsg.equals(throwable.getMessage()))
				.verify();
	}

	private void deleteTableIfExists() throws ExecutionException, InterruptedException {
		if (this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.salesTableName).get()) {
			this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, salesTableName).get();
		}
	}

	private AwsBasicCredentials awsBasicCredentials() {
		return AwsBasicCredentials.create("accessKey", "secretKey");
	}
}
