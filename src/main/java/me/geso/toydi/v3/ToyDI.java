package me.geso.toydi.v3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

public class ToyDI {
	private final Map<Class<?>, Class<? extends Provider<?>>> providers;

	public ToyDI(Module... modules) {
		providers = new HashMap<>();
		for (Module module : modules) {
			module.configure(this);
		}
	}

	public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		final Class<? extends Provider<?>> providerClass = providers.get(classType);
		if (providerClass != null) {
			final Provider<?> provider = this.getInstance(providerClass);
			Object instance = provider.get();
			return (T)instance;
		} else {
			try {
				final Constructor<T> constructor = classType.getConstructor();
				T instance = constructor.newInstance();
				this.instantiateMembers(instance);
				return instance;
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("There is no default constructor or provider: " + classType.getName());
			}
		}
	}

	public <T> void registerProvider(Class<T> type, Class<? extends Provider<T>> provider) {
		providers.put(type, provider);
	}

	public void instantiateMembers(Object object) throws IllegalAccessException, InstantiationException,
			NoSuchMethodException, InvocationTargetException {
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
