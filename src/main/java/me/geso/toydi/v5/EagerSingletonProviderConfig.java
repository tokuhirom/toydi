package me.geso.toydi.v5;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Provider;

public class EagerSingletonProviderConfig<T> implements ProviderConfig<T> {
	private Class<? extends Provider<T>> providerClass;
	private T instance;

	public EagerSingletonProviderConfig(final Class<? extends Provider<T>> providerClass) {
		this.providerClass = providerClass;
	}

	@Override
	public void initialize(final ToyDI di)
			throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		Provider<T> provider = di.getInstance(providerClass);
		instance = provider.get();
	}

	@Override
	public T getInstance(final ToyDI di)
			throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		return instance;
	}
}
