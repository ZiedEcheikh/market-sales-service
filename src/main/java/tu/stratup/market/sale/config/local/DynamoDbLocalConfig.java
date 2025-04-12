package tu.stratup.market.sale.config.local;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
@Profile("local")
public class DynamoDbLocalConfig {
	private final String dynamoDbEndPointUrl;

	private final String region;

	public DynamoDbLocalConfig(@Value("${aws.dynamodb.endpoint}") final String dynamoDbEndPointUrl,
			@Value("${aws.region}") final String region) {
		this.dynamoDbEndPointUrl = dynamoDbEndPointUrl;
		this.region = region;
	}

	@Bean
	public DynamoDbAsyncClient getDynamoDbAsyncClient() {
		return DynamoDbAsyncClient.builder()
				.endpointOverride(URI.create(dynamoDbEndPointUrl))
				.region(Region.of(region))
				.build();
	}

	@Bean
	public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() {
		return DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(getDynamoDbAsyncClient())
				.build();
	}
}
