package com.startup.market.sales.utils;

import java.util.List;
import java.util.function.Predicate;

public class ListUtils {
	public static <T> boolean existsInList(final List<T> list, final Predicate<T> condition) {
		return list.stream().anyMatch(condition);
	}
}
