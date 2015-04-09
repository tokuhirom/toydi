package me.geso.toydi.v5;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Provider;

public class SingletonProviderConfig<T> implements ProviderConfig<T> {
	private Class<? extends Provider<T>> providerClass;
	private T instance;

	public SingletonProviderConfig(Class<? extends Provider<T>> providerClass) {
		this.providerClass = providerClass;
	}

	@Override
	public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		if (instance != null) {
			return instance;
		}
		Provider<T> provider = di.getInstance(providerClass);
		instance = provider.get();
		if (instance == null) {
			throw new NullPointerException("Provider returns null: " + provider);
		}
		return instance;
	}
}

