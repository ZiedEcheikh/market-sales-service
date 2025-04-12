package tu.stratup.market.sale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
@Profile("dev")
public class DynamoDbConfig {

	@Bean
	public DynamoDbAsyncClient getDynamoDbAsyncClient() {
		return DynamoDbAsyncClient.builder()
				.build();
	}

	@Bean
	public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() {
		return DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(getDynamoDbAsyncClient())
				.build();
	}
}
