package com.hackhub.application.mapper;

import java.lang.reflect.Field;

final class MapperFieldAccess {

	private MapperFieldAccess() {
	}

	@SuppressWarnings("unchecked")
	static <T> T read(Object source, String fieldName, Class<T> type) {
		if (source == null) {
			return null;
		}

		Class<?> current = source.getClass();
		while (current != null) {
			try {
				Field field = current.getDeclaredField(fieldName);
				field.setAccessible(true);
				Object value = field.get(source);
				return (T) value;
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Unable to read field '" + fieldName + "'", ex);
			}
		}

		throw new IllegalStateException(
			"Field '" + fieldName + "' was not found on " + source.getClass().getName()
		);
	}
}
