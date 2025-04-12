package tu.stratup.market.sale.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonReader {

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());

	public static <T> T readJsonFile(final String fileName, final Class<T> clazz) {
		try(InputStream inputStream = new ClassPathResource(fileName).getInputStream()) {
			return objectMapper.readValue(inputStream, clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> readJsonFileAsList(final String fileName, final Class<T> clazz) {
		try(InputStream inputStream = new ClassPathResource(fileName).getInputStream()) {
			return objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readJsonFileAsString(final String fileName) {
		try(InputStream inputStream = new ClassPathResource(fileName).getInputStream()) {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
