package me.geso.toydi.v5;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

public class ProviderInjectionTest {
	@Test
	public void test() throws Exception {
		ToyDI di = new ToyDI();
		System.out.println("getInstance");
		Foo foo = di.getInstance(Foo.class);
		assertThat(foo)
				.isNotNull();
		System.out.println(foo.getMessage());
	}

	public static class Foo {
		@Inject
		private Provider<Bar> bar;

		public String getMessage() {
			System.out.println("Foo::getMessage");
			return bar.get().getMessage();
		}
	}

	public static class Bar {
		public Bar() {
			System.out.println("Bar::new");
		}

		public String getMessage() {
			System.out.println("Bar::getMessage");
			return "Get!";
		}
	}
}
