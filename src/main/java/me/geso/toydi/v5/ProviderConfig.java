package me.geso.toydi.v5;

import java.lang.reflect.InvocationTargetException;

import me.geso.toydi.v5.ToyDI;

public interface ProviderConfig<T> {
	/**
	 * ToyDI calls this after initializing modules.
	 */
	default public void initialize(ToyDI di)
			throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
	}

	public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException;
}
