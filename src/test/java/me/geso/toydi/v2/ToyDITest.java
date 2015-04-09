package me.geso.toydi.v2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import lombok.Getter;
import javax.inject.Inject;

public class ToyDITest {
	@Test
	public void testV2() throws Exception {
		Foo foo = new ToyDI().getInstance(Foo.class);
		assertThat(foo)
			.isInstanceOf(Foo.class);
		assertThat(foo.getBar())
				.isInstanceOf(Bar.class);
	}

	@Getter
	public static class Foo {
		@Inject
		private Bar bar;
	}

	public static class Bar {
	}
}
