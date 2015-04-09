package me.geso.toydi.v3;

import org.junit.Test;
import lombok.Getter;
import javax.inject.Inject;
import javax.inject.Provider;
import static org.assertj.core.api.Assertions.*;

public class ToyDIV3Test {
	@Test
	public void test() throws Exception {
		ToyDI di = new ToyDI(new BasicModule());
		Foo foo = di.getInstance(Foo.class);
		Connection connection = foo.getConnection();
		assertThat(connection.getUrl())
				.isEqualTo("jdbc:mysql:localhost");
	}

	@Getter
	public static class Foo {
		@Inject
		private Connection connection;
	}

	@Getter
	public static class Connection {
		private String url;
		private String username;
		private String password;

		public Connection(String url, String username, String password) {
			this.url = url;
			this.username = username;
			this.password = password;
		}
	}

	public static class BasicModule implements Module {
		public void configure(ToyDI di) {
			di.registerProvider(Connection.class, ConnectionProvider.class);
		}
	}

	public static class ConnectionProvider implements Provider<Connection> {
		public Connection get() {
			return new Connection("jdbc:mysql:localhost", "root", "");
		}
	}
}
