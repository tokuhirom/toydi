package me.geso.toydi.v3;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Test;

import lombok.Getter;
import lombok.Value;

public class ToyDIV3Test2 {
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

	@Value
	public static class ConnectionConfig {
		private String url;
		private String username;
		private String password;
	}

	public static class BasicModule implements Module {
		public void configure(ToyDI di) {
			di.registerProvider(Connection.class, ConnectionProvider.class);
			di.registerProvider(ConnectionConfig.class, ConnectionConfigProvider.class);
		}
	}

	public static class ConnectionProvider implements Provider<Connection> {
		@Inject
		private ConnectionConfig config;

		public Connection get() {
			return new Connection(config.getUrl(), config.getUsername(), config.getPassword());
		}
	}

	public static class ConnectionConfigProvider implements Provider<ConnectionConfig> {
		public ConnectionConfig get() {
			return new ConnectionConfig("jdbc:mysql:localhost", "root", "");
		}
	}

}
