package com.startup.market.sales.constants;

public enum Status {
	DRAFT("Draft"), READY("Ready");

	private final String value;

	Status(final String value) {
		this.value = value;
	}

	public static Status findByValue(final String value) {
		Status result = null;
		for (final Status status : values()) {
			if (status.value.equalsIgnoreCase(value)) {
				result = status;
				break;
			}
		}
		return result;
	}
}
