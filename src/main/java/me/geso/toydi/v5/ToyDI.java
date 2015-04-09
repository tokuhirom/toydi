package me.geso.toydi.v5;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ToyDI {
	private final Map<Class<?>, ProviderConfig<?>> providers;

	public ToyDI(Module... modules) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		providers = new HashMap<>();

		for (Module module : modules) {
			module.configure(this);
		}

		for (final ProviderConfig<?> providerConfig : providers.values()) {
			providerConfig.initialize(this);
		}
	}

	public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		final ProviderConfig<T> providerConfig = (ProviderConfig<T>)providers.get(classType);
		if (providerConfig != null) {
			return providerConfig.getInstance(this);
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

	public <T> void register(Class<T> type, ProviderConfig<T> providerConfig) {
		providers.put(type, providerConfig);
	}

	public void instantiateMembers(Object object) throws IllegalAccessException, InstantiationException,
			NoSuchMethodException, InvocationTargetException {
		for (final Field field : object.getClass().getDeclaredFields()) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				field.setAccessible(true);
				instantiateMember(object, field);
			}
		}
	}

	private void instantiateMember(final Object object, final Field field)
			throws IllegalAccessException, InstantiationException, NoSuchMethodException,
			InvocationTargetException {
		final Class<?> type = field.getType();
		if (type.isAssignableFrom(Provider.class)) {
			final Type genericType = field.getGenericType();
			if (genericType instanceof ParameterizedType) {
				final Type type1 = ((ParameterizedType)genericType).getActualTypeArguments()[0];
				if (type1 instanceof Class) {
					Provider<?> value = this.getProvider((Class<?>)type1);
					field.set(object, value);
				} else {
					throw new IllegalStateException();
				}
			} else {
				throw new IllegalStateException();
			}
		} else {
			Object value = this.getInstance(type);
			field.set(object, value);
		}
	}

	private Provider<?> getProvider(Class<?> type) {
		return () -> {
			try {
				return this.getInstance(type);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		};
	}

}
