package tu.stratup.market.sale.repositrory;

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
import tu.stratup.market.sale.constants.Status;
import tu.stratup.market.sale.items.MetaDataItem;
import tu.stratup.market.sale.items.SaleItem;
import tu.stratup.market.sale.repositrory.commun.DynamodbBuilderTesting;
import tu.stratup.market.sale.repositrory.commun.DynamodbServiceTesting;
import tu.stratup.market.sale.utils.JsonReader;

import java.net.URI;

@Testcontainers
class SaleRepositoryTest {

	@Container
	private static GenericContainer<?> dynamodb = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local"))
			.withExposedPorts(8000);

	private String salePathFile = "/data/sale/models/sale.json";
	private String saleTableName = "market-sale";
	private String metadataTableName = "metadata";

	private DynamodbServiceTesting dynamodbService;
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
		this.dynamodbService = new DynamodbServiceTesting();
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

		final DynamodbBuilderTesting dynamodbBuilder = new DynamodbBuilderTesting();

		this.saleRepository = new SaleRepository(saleTableName, metadataTableName, dynamoDbEnhancedAsyncClient);

		this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, saleTableName).get();
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

		if (!this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.saleTableName).get()) {
			this.dynamodbService.createTable(dynamoDbAsyncClient, this.saleTableName,
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
		if (this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.saleTableName).get()) {
			this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, saleTableName).get();
		}
		final String expectedExceptionMsg = "Error happened during retrieve sales by status";
		StepVerifier
				.create(saleRepository.retrieveByStatus(Status.DRAFT))
				.expectErrorMatches(throwable -> throwable instanceof RuntimeException
						&& expectedExceptionMsg.equals(throwable.getMessage()));
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
		if (this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.saleTableName).get()) {
			this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, saleTableName).get();
		}
		final String expectedExceptionMsg = "Error happened during retrieve sale";
		StepVerifier
				.create(saleRepository.retrieveByKey("id"))
				.expectErrorMatches(throwable -> throwable instanceof RuntimeException
						&& expectedExceptionMsg.equals(throwable.getMessage()));
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
		if (this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.saleTableName).get()) {
			this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, saleTableName).get();
		}
		final String expectedExceptionMsg = "Error happened during creating sale";
		StepVerifier
				.create(saleRepository.createSale(sale))
				.expectErrorMatches(throwable -> throwable instanceof RuntimeException
						&& expectedExceptionMsg.equals(throwable.getMessage()));
	}


	private AwsBasicCredentials awsBasicCredentials() {
		return AwsBasicCredentials.create("accessKey", "secretKey");
	}
}
