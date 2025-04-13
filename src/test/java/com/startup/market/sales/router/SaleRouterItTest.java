package com.startup.market.sales.router;

import com.startup.market.sales.config.SecurityConfigTesting;
import com.startup.market.sales.constants.Status;
import com.startup.market.sales.handler.dto.SaleCreateRequest;
import com.startup.market.sales.handler.dto.SaleDto;
import com.startup.market.sales.items.MetaDataItem;
import com.startup.market.sales.items.SaleItem;
import com.startup.market.sales.repositrory.SaleRepository;
import com.startup.market.sales.utils.JsonReader;
import com.startup.market.sales.utils.dynamodb.DynamodbServices;
import com.startup.market.sales.utils.dynamodb.SalesTablesBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@SpringBootTest(classes = SecurityConfigTesting.class)
@AutoConfigureWebTestClient
@Testcontainers
@WithMockUser(authorities = "SCOPE_message.read")
@Profile("test")
class SaleRouterItTest {

	@Container
	private static GenericContainer<?> dynamodb = new GenericContainer<>(DockerImageName.parse("amazon/dynamodb-local"))
			.withExposedPorts(8000);
	private String salesURI = "/sales";
	private String salesStatusURI = "/sales/status/";
	private String salesTableName = "market-sales";
	private String metadataTableName = "metadata";
	private String metadataSaleId = "Sale";

	@Autowired
	private WebTestClient webTestClient;
	@Autowired
	private AwsBasicCredentials awsBasicCredentials;

	private DynamoDbAsyncClient dynamoDbAsyncClient;
	private SaleRepository saleRepository;
	private DynamodbServices dynamodbService;

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
				.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
				.region(Region.EU_WEST_1)
				.build();
		final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(dynamoDbAsyncClient).build();

		final SalesTablesBuilder dynamodbBuilder = new SalesTablesBuilder();

		this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, salesTableName).get();
		this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, metadataTableName).get();

		this.saleRepository = new SaleRepository(salesTableName, metadataTableName, metadataSaleId, dynamoDbEnhancedAsyncClient);

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
		webTestClient = webTestClient.mutate()
				.responseTimeout(Duration.ofMillis(30_000))
				.build();
	}

	@Test
	void shouldGetSalesByStatus() {
		final String status = "DRAFT";
		final SaleItem sale = JsonReader.readJsonFile("/data/sales/models/sale.json", SaleItem.class);
		final SaleItem saleCreated = saleRepository.createSale(sale).block();
		final WebTestClient.ResponseSpec response = webTestClient.get().uri(salesStatusURI + status)
				.accept(MediaType.APPLICATION_JSON)
				.exchange();
		final List<SaleDto> responseBody = response.expectStatus().isOk()
				.expectBodyList(SaleDto.class)
				.returnResult()
				.getResponseBody();
		assertAll(() -> assertEquals(1, responseBody.size()),
				() -> assertEquals(saleCreated.getIdentify(), responseBody.get(0).getIdentify())
		);
	}

	@Test
	void shouldGetSalesByStatusNotFound() {
		final String status = "READY";
		final WebTestClient.ResponseSpec response = webTestClient.get().uri(salesStatusURI + status)
				.accept(MediaType.APPLICATION_JSON)
				.exchange();
		final WebTestClient.BodyContentSpec bodyContentSpec = response.expectStatus()
				.is2xxSuccessful()
				.expectBody();
		final String expectedResponse = JsonReader.readJsonFileAsString("/data/sales/response/noDataFound.json");
		assertAll(
				() -> assertNotNull(bodyContentSpec),
				() -> bodyContentSpec.json(expectedResponse));
	}

	@Test
	void shouldGetSalesByStatusStatusNotValidClientError() {
		final String status = "DELIVERED";
		final WebTestClient.ResponseSpec response = webTestClient.get().uri(salesStatusURI + status)
				.accept(MediaType.APPLICATION_JSON)
				.exchange();
		final WebTestClient.BodyContentSpec bodyContentSpec = response.expectStatus()
				.is4xxClientError()
				.expectBody();
		final String expectedResponse = JsonReader.readJsonFileAsString("/data/sales/response/statusNotValid_clientError.json");
		assertAll(
				() -> assertNotNull(bodyContentSpec),
				() -> bodyContentSpec.json(expectedResponse));
	}

	@Test
	@SneakyThrows
	void shouldGetSalesByStatusServerError() {
		final String status = "draft";
		if (this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.salesTableName).get()) {
			this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, salesTableName).get();
		}
		final WebTestClient.ResponseSpec response = webTestClient.get().uri(salesStatusURI + status)
				.accept(MediaType.APPLICATION_JSON)
				.exchange();

		final String expectedResponse = JsonReader.readJsonFileAsString("/data/sales/response/internal_serverError.json");
		response.expectStatus().is5xxServerError()
				.expectBody()
				.json(expectedResponse);
	}

	@Test
	void shouldCreateNewSale() {
		final SaleCreateRequest saleCreateRequest = JsonReader.readJsonFile("/data/sales/request/correct_request.json", SaleCreateRequest.class);

		final WebTestClient.ResponseSpec response = webTestClient.mutateWith(csrf()).post().uri("/sales")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(saleCreateRequest), SaleCreateRequest.class)
				.exchange();

		final SaleDto responseBody = response.expectStatus().isOk()
				.expectBody(SaleDto.class)
				.returnResult()
				.getResponseBody();
		assertAll(
				() -> assertNotNull(responseBody.getIdentify(), "Sale identifier should not be null"),
				() -> assertEquals(Status.DRAFT.name(), responseBody.getStatus(),
						"New sale should have DRAFT status"));
	}

	@Test
	void shouldCreateNewSaleRangeDatesNotValid() {
		final SaleCreateRequest saleCreateRequest = JsonReader.readJsonFile("/data/sales/request/range_dates_not_valid.json", SaleCreateRequest.class);
		final WebTestClient.ResponseSpec response = webTestClient.mutateWith(csrf()).post().uri(salesURI)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(saleCreateRequest), SaleCreateRequest.class)
				.exchange();
		final WebTestClient.BodyContentSpec bodyContentSpec = response.expectStatus()
				.is4xxClientError()
				.expectBody();
		final String expectedResponse = JsonReader.readJsonFileAsString("/data/sales/response/rangeDatesNotValid_clientError.json");
		assertAll(
				() -> assertNotNull(bodyContentSpec),
				() -> bodyContentSpec.json(expectedResponse));
	}

	@Test
	void shouldCreateNewSaleMissingFields() {
		final SaleCreateRequest saleCreateRequest = JsonReader.readJsonFile("/data/sales/request/missing_fields.json", SaleCreateRequest.class);
		final WebTestClient.ResponseSpec response = webTestClient.mutateWith(csrf()).post().uri(salesURI)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(saleCreateRequest), SaleCreateRequest.class)
				.exchange();
		final WebTestClient.BodyContentSpec bodyContentSpec = response.expectStatus()
				.is4xxClientError()
				.expectBody();
		final String expectedResponse = JsonReader.readJsonFileAsString("/data/sales/response/missingFields_clientError.json");
		assertAll(
				() -> assertNotNull(bodyContentSpec),
				() -> bodyContentSpec.json(expectedResponse));
	}

	@Test
	@SneakyThrows
	void shouldCreateNewSaleServerError() {
		final SaleCreateRequest saleCreateRequest = JsonReader.readJsonFile("/data/sales/request/correct_request.json", SaleCreateRequest.class);

		if (this.dynamodbService.verifyTableExists(dynamoDbAsyncClient, this.salesTableName).get()) {
			this.dynamodbService.deleteTable(this.dynamoDbAsyncClient, salesTableName).get();
		}
		final WebTestClient.ResponseSpec response = webTestClient.mutateWith(csrf()).post().uri(salesURI)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(saleCreateRequest), SaleCreateRequest.class)
				.exchange();
		final String expectedResponse = JsonReader.readJsonFileAsString("/data/sales/response/internal_serverError.json");
		response.expectStatus()
				.is5xxServerError()
				.expectBody()
				.json(expectedResponse);
	}
}
