package me.geso.toydi.v5;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import me.geso.toydi.v5.ProviderConfig;
import me.geso.toydi.v5.ToyDI;

public class InstanceProviderConfig<T> implements ProviderConfig<T> {
	private T instance;

	public InstanceProviderConfig(T instance) {
		this.instance = instance;
	}

	@Override
	public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		return instance;
	}
}
