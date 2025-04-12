package tu.stratup.market.sale.items;

import com.fasterxml.jackson.annotation.JsonInclude;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.Locale;

@DynamoDbBean
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleItem {

	private String identify;
	private String title;
	private String description;
	private String status;
	private String createAt;
	private String startDate;
	private String endDate;

	@DynamoDbPartitionKey
	@DynamoDbAttribute("id")
	public String getIdentify() {
		return identify;
	}

	public void setIdentify(final String identify) {
		this.identify = identify;
	}

	@DynamoDbAttribute("title")
	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	@DynamoDbAttribute("description")
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@DynamoDbAttribute("status")
	@DynamoDbSecondaryPartitionKey(indexNames = {"status_index"})
	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status.toUpperCase(Locale.ROOT);
	}

	@DynamoDbAttribute("create_at")
	public String getCreateAt() {
		return createAt;
	}

	public void setCreateAt(final String createAt) {
		this.createAt = createAt;
	}

	@DynamoDbAttribute("start_date")
	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	@DynamoDbAttribute("end_date")
	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}
}
