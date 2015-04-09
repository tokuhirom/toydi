package me.geso.toydi.v5;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Provider;

import org.junit.Test;

public class SingletonProviderConfigTest {
	@Test
	public void test() throws Exception {
		ToyDI di = new ToyDI(new BasicModule());
		Counter counter1 = di.getInstance(Counter.class);
		Counter counter2 = di.getInstance(Counter.class);
		counter1.incr();
		counter1.incr();
		counter1.incr();
		System.out.println(counter1.get());
		assertThat(counter1.get())
			.isEqualTo(3);
		assertThat(counter1)
				.isSameAs(counter2);
		assertThat(counter1.get())
			.isEqualTo(counter2.get());
	}

	public static class BasicModule implements Module {
		public void configure(ToyDI di) {
			di.register(Counter.class, new SingletonProviderConfig<>(CounterProvider.class));
		}
	}

	public static class Counter {
		private int i;

		public Counter() {
			i = 0;
		}

		public void incr() {
			++i;
		}

		public int get() {
			return i;
		}
	}

	public static class CounterProvider implements Provider<Counter> {
		public Counter get() {
			System.out.println("Create new Counter");
			return new Counter();
		}
	}
}
