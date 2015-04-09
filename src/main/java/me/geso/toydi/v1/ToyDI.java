package me.geso.toydi.v1;

public class ToyDI {
	public ToyDI() {
	}

	public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException {
		return classType.newInstance();
	}
}
