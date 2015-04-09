package me.geso.toydi.v2;

import java.lang.reflect.Field;

import javax.inject.Inject;

public class ToyDI {
	public ToyDI() {
	}

	public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException {
			T instance = classType.newInstance();
			this.instantiateMembers(instance);
			return instance;
	}

	public void instantiateMembers(Object object) throws IllegalAccessException, InstantiationException {
		for (final Field field : object.getClass().getDeclaredFields()) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				field.setAccessible(true);
				final Class<?> type = field.getType();
				Object value = this.getInstance(type);
				field.set(object, value);
			}
		}
	}
}
