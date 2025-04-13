package com.startup.market.sales.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
@Profile("test")
public class DynamoDbITConfigTesting {
	private final String dynamoDbEndPointUrl;

	private final String accessKey;

	private final String secretKey;

	public DynamoDbITConfigTesting(@Value("${aws.dynamodb.endpoint}") final String dynamoDbEndPointUrl,
			@Value("${aws.accessKey}") final String accessKey, @Value("${aws.secretKey}") final String secretKey) {
		this.dynamoDbEndPointUrl = dynamoDbEndPointUrl;
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	@Bean
	AwsBasicCredentials awsBasicCredentials() {
		return AwsBasicCredentials.create(accessKey, secretKey);
	}

	@Bean
	public DynamoDbAsyncClient getDynamoDbAsyncClient() {
		return DynamoDbAsyncClient.builder()
				.endpointOverride(URI.create(dynamoDbEndPointUrl))
				.credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials()))
				.region(Region.EU_WEST_1)
				.build();
	}


	@Bean
	public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() {
		return DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(getDynamoDbAsyncClient())
				.build();
	}
}
