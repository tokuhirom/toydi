package me.geso.toydi.v1;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import me.geso.toydi.v2.ToyDI;

public class ToyDITest {
	@Test
	public void testNewInstance() throws Exception {
		Foo foo = new ToyDI().getInstance(Foo.class);
		assertThat(foo)
			.isInstanceOf(Foo.class);
	}

	public static class Foo {
	}
}
