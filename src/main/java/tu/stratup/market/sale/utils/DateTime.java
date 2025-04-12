package tu.stratup.market.sale.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTime {
	public static String localDateToISO(final LocalDateTime localDate) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		return localDate.atOffset(ZoneOffset.UTC).format(formatter);
	}
}
