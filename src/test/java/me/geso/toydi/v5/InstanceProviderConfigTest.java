package me.geso.toydi.v5;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Provider;

import org.junit.Test;

public class InstanceProviderConfigTest {
	@Test
	public void test() throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		assertEquals(0, Foo.getInstanceCount());
		Foo foo = new Foo();
		assertEquals(1, Foo.getInstanceCount());
		ToyDI di = new ToyDI(new BasicModule(foo));
		assertEquals(1, Foo.getInstanceCount());
		di.getInstance(Foo.class);
		assertEquals(1, Foo.getInstanceCount());
	}

	public static class BasicModule implements Module {
		private final Foo foo;

		public BasicModule(Foo foo) {
			this.foo = foo;
		}

		@Override
		public void configure(ToyDI di) throws InstantiationException, IllegalAccessException,
				NoSuchMethodException, InvocationTargetException {
			di.register(Foo.class, new InstanceProviderConfig<>(foo));
		}
	}

	public static class FooProvider implements Provider<Foo> {
		@Override
		public Foo get() {
			return new Foo();
		}
	}

	public static class Foo {
		private static int instanceCount = 0;

		public Foo() {
			instanceCount++;
		}

		public static int getInstanceCount() {
			return instanceCount;
		}
	}

}
