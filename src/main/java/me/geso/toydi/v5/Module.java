package me.geso.toydi.v5;

import java.lang.reflect.InvocationTargetException;

public interface Module {
	public void configure(ToyDI toyDI) throws InstantiationException, IllegalAccessException,
	NoSuchMethodException, InvocationTargetException;
}

