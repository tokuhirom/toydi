package me.geso.toydi.v5;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Provider;

import lombok.Getter;
import me.geso.toydi.v5.ProviderConfig;
import me.geso.toydi.v5.ToyDI;

public class NormalProviderConfig<T> implements ProviderConfig<T> {
	@Getter
	private Class<? extends Provider<T>> providerClass;

	public NormalProviderConfig(Class<? extends Provider<T>> providerClass) {
		this.providerClass = providerClass;
	}

	@Override
	public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		Provider<T> provider = di.getInstance(providerClass);
		return provider.get();
	}
}
