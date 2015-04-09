# 作って学ぶ Dependency Injection

DI がなぜ必要か?

DI とはなにか｡DI についての解説は数多あるが､現実的にはインスタンス生成に関するボイラープレートコードを省略するというのが重要な点となる｡

DI は仕組みとしては単純なのだが､LL をバックグラウンドに持つエンジニアの中には｢黒魔術……｣のように感じてしまうエンジニアも多いようだ｡
LL のメタプログラミングとたいして変わらないのだが､裏でゴニョゴニョする感じなので､挙動がつかみづらいと感じがち｡

そこで､本稿では､シンプルな DI ライブラリを実際に作成しながら､学んでいく｡

## 単純にクラスを生成する｡

本稿では､おもちゃの DI ライブラリ､ToyDI を作成していく｡このライブラリは Google Guice のサブセットのような機能を持つことになる予定だ｡

	public class ToyDI {
		public ToyDI() {
		}

		public <T> T getInstance(Class<T> classType) {
			try {
				return classType.newInstance();
			} catch (InstantiationException|IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

単純にクラスを生成するだけならば､このように､getInstance メソッドを実装すればよろしい｡単純な reflection である｡

このクラスを利用するコードは以下のようになる｡

	public class ToyDITest {
		@Test
		public void testNewInstance() {
			Foo foo = new ToyDI().getInstance(Foo.class);
			assertThat(foo)
					.isInstanceOf(Foo.class);
		}

		public static class Foo {
		}
	}
	
これだけでは､全く意味がないので次へ進もう｡	

## フィールドに値を埋める｡

フィールドに､依存しているクラスのインスタンスを自動的に埋めるようにしてみましょう｡

インジェクト対象のフィールドかどうかを判定できるように､@javax.inject.Inject というアノテーションをつけましょう｡

テストコードは以下のようになります｡Foo の bar というフィールドに､自動的にインスタンスを埋めるようになります｡

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

以下のように､フィールド一覧を取得して @Inject アノテーションが付いているものの一覧を取得して､必要なインスタンスを埋めればいいですね｡
コードとしては得に凝ったことはしておらず､簡単です｡

    public class ToyDI {
        public ToyDI() {
        }
    
        public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException {
                T instance = classType.newInstance();
                this.instantiateMembers(instance);
                return instance;
        }
    
        public void instantiateMembers(Object object) throws IllegalAccessException, InstantiationException {
            for (final Field field : object.getClass().getDeclaredFields()) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject != null) {
                    field.setAccessible(true);
                    final Class<?> type = field.getType();
                    Object value = this.getInstance(type);
                    field.set(object, value);
                }
            }
        }
    }

## DI 前提ではないクラスのインジェクト

例えば､JDBC の Connection を取得するなど､DI 前提で作られていないクラスもあります｡
そのようなクラスの instantiate を行う場合には､instantiate するファクトリを用意する必要があります｡

たとえば以下のようにファクトリを定義します｡DI 用のファクトリには javax.inject.Provider インターフェースを実装するようにします｡

	public static class ConnectionProvider implements Provider<Connection> {
		public Connection get() {
			return new Connection("jdbc:mysql:localhost", "root", "");
		}
	}

次に､どの Provider を利用するか､という設定を一個ずつ渡していると大変なので､Provider を管理する存在として Module というクラスを定義します｡

	public static class BasicModule implements ModuleV3 {
	    @Override
		public void configure(ToyDIV3 di) {
			di.registerProvider(Connection.class, ConnectionProvider.class);
		}
	}
	
最後に､ToyDI で Provider/Module を利用するように変更しましょう｡

    public class ToyDIV3 {
        private final Map<Class<?>, Class<? extends Provider<?>>> providers;

        public ToyDIV3(ModuleV3... modules) {
            providers = new HashMap<>();
            for (ModuleV3 module: modules) {
                module.configure(this);
            }
        }
    
        public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException {
            final Class<? extends Provider<?>> providerClass = providers.get(classType);
            if (providerClass != null) {
                final Provider<?> provider = this.getInstance(providerClass);
                Object instance = provider.get();
                return (T) instance;
            } else {
                T instance = classType.newInstance();
                this.instantiateMembers(instance);
                return instance;
            }
        }

        public <T> void registerProvider(Class<T> type, Class<? extends Provider<T>> provider) {
            providers.put(type, provider);
        }
    
        public void instantiateMembers(Object object) throws IllegalAccessException, InstantiationException {
            for (final Field field : object.getClass().getDeclaredFields()) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject != null) {
                    field.setAccessible(true);
                    final Class<?> type = field.getType();
                    Object value = this.getInstance(type);
                    field.set(object, value);
                }
            }
        }
    }

利用時には以下のようにしましょう｡Module を渡すようになったのが大きな違いです｡

		ToyDIV3 di = new ToyDIV3(new BasicModule());
		Foo foo = di.getInstance(Foo.class);
		Connection connection = foo.getConnection();
		assertThat(connection.getUrl())
				.isEqualTo("jdbc:mysql:localhost");

## Provider も DI ライブラリがインスタンス作成するよ

`Provider<Connection>` のインスタンスも DI コンテナから取得されます｡つまり､Provider にも @Inject アノテーションが適用可能です｡
これにより､例えば設定を読み込むプロバイダが返す設定情報を元に､DB コネクションを作る､なんてことも実装可能です｡

	@Test
	public void test() throws Exception {
		ToyDIV3 di = new ToyDIV3(new BasicModule());
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

	public static class BasicModule implements ModuleV3 {
		public void configure(ToyDIV3 di) {
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

## 一度作ったインスタンスは使いまわしてほしい｡

例えば設定ファイルの読み込みの場合には､一度生成したインスタンスを使いまわしてほしいですね｡
一度生成したインスタンスを保持するようにコードを変更してみましょう｡

singleton 指定で登録する方法を DI に定義してみましょう｡

Provider からインスタンスを取得する部分の処理を 1 つのインターフェースと2つのクラスに分離しましょう｡

Provider からのインスタンス取得の処理を汎化するためにインターフェースを定義します｡
initialize() メソッドは､モジュールの初期化がすべて完了したタイミングで呼ばれます｡ getInstance メソッドは､インスタンスの取得が行われるタイミングで呼ばれます｡

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

次に､通常のインスタンス取得処理を記述します｡今までのインスタンス取得処理とほとんど同じです｡

    public class NormalProviderConfig<T> implements ProviderConfig<T> {
        @Getter
        private Class<? extends Provider<T>> providerClass;
    
        public NormalProviderConfig(Class<? extends Provider<T>> providerClass) {
            this.providerClass = providerClass;
        }
    
        @Override
        public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException {
            Provider<T> provider = di.getInstance(providerClass);
            return provider.get();
        }
    }

最後にシングルトンの場合のインスタンス取得処理を記述します｡これは､一度インスタンスを作成したら､そのまま使い続けるようになっています｡

    public class SingletonProviderConfig<T> implements ProviderConfig<T> {
        private Class<? extends Provider<T>> providerClass;
        private T instance;
    
        public SingletonProviderConfig(Class<? extends Provider<T>> providerClass) {
            this.providerClass = providerClass;
        }
    
        @Override
        public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException {
            if (instance != null) {
                return instance;
            }
            Provider<T> provider = di.getInstance(providerClass);
            instance = provider.get();
            if (instance == null) {
                throw new NullPointerException("Provider returns null: " + provider);
            }
            return instance;
        }
    }

そして､これらを利用するように DI の instance 取得処理を以下のように変更しましょう｡

	public <T> T getInstance(Class<T> classType) throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		final ProviderConfig<T> providerConfig = (ProviderConfig<T>)providers.get(classType);
		if (providerConfig != null) {
			return providerConfig.getInstance(this);
		} else {
			try {
				final Constructor<T> constructor = classType.getConstructor();
				T instance = constructor.newInstance();
				this.instantiateMembers(instance);
				return instance;
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("There is no default constructor or provider: " + classType.getName());
			}
		}
	}

	public <T> void register(Class<T> type, ProviderConfig<T> providerConfig) {
		providers.put(type, providerConfig);
	}
	
## 出来合いのインスタンスを渡したいんだけど｡｡

すでに出来上がっているインスタンスを､単に DI で Inject したいというケースもあるでしょう｡

InstanceProviderConfig を定義します｡単に､インスタンスを返すだけなので簡単ですね｡

    public class InstanceProviderConfig<T> implements ProviderConfig<T> {
        private T instance;
    
        public InstanceProviderConfig(T instance) {
            this.instance = instance;
        }
    
        @Override
        public void initialize(final ToyDI di)
                throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        }
    
        @Override
        public T getInstance(ToyDI di) throws InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException {
            return instance;
        }
    }

最後に利用例です｡Instance を直接モジュールで設定します｡
    
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
	
    	
## 設定は起動時に読み込んでほしい

設定ファイルの読み込みを､必要になった時点で行うようにすると､設定ファイルのミスが失敗している時に問題解決が遅れることになる｡
というわけで､eager に singleton のインスタンスを作って欲しいというケースがあります｡

そんなトキには､インスタンスを渡すようにすればいいじゃん､と思うところですが､そうしてしまうと､インスタンスの生成に DI を利用することができませんね｡
なので､すべてのセットアップが終わった段階で､一気に処理するように実装する必要があります｡

EagerSingletonProviderConfig というクラスを設定します｡initialize 時にインスタンスを生成します｡

    public class EagerSingletonProviderConfig<T> implements ProviderConfig<T> {
        private Class<? extends Provider<T>> providerClass;
        private T instance;
    
        public EagerSingletonProviderConfig(final Class<? extends Provider<T>> providerClass) {
            this.providerClass = providerClass;
        }
    
        @Override
        public void initialize(final ToyDI di)
                throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            Provider<T> provider = di.getInstance(providerClass);
            instance = provider.get();
        }
    
        @Override
        public T getInstance(final ToyDI di)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            return instance;
        }
    }
	
この機能を利用するためのコードは以下のようになるでしょう｡
	
	public static class BasicModule implements Module {
		@Override
		public void configure(ToyDI di) throws InstantiationException, IllegalAccessException,
				NoSuchMethodException, InvocationTargetException {
			di.register(Foo.class, new EagerSingletonProviderConfig<>(FooProvider.class));
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
	
## 閑話休題｡ところで､@Inject や Provider ってどこから来てるの?

javax.inject.Inject や javax.inject.Provider は JSR-330 で定義されております｡
JSR ってのは､Java 界の RFC みたいなやつです｡

@Inject 等のアノテーションは Java の DI のほぼすべてで利用可能です｡
とは言え､実装に差異があるので､単純にリプレース可能かというと微妙かもしれないですね｡

## インスタンスの生成を必要になるまで遅らせたい｡

このクラスではアレのインスタンスが必要なんだけど､一部のメソッドでしか使わないんですよね､とか
一部のパスでしか使わないんですよね､みたいなことってありますよね｡

そんな時は､そのフィールドに対するインジェクションのタイミングを遅らせたほうが良いですね｡
以下のように､Provider をフィールドに設定し､それを Inject 対象にすることで､インスタンスの取得を遅らせることができます｡

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
	
DI 側は以下のように､インジェクト対象が Provider であった場合には､遅延評価を行う lambda をインジェクトするようにします｡	
	
	private void instantiateMember(final Object object, final Field field)
			throws IllegalAccessException, InstantiationException, NoSuchMethodException,
			InvocationTargetException {
		final Class<?> type = field.getType();
		if (type.isAssignableFrom(Provider.class)) {
			final Type genericType = field.getGenericType();
			if (genericType instanceof ParameterizedType) {
				final Type type1 = ((ParameterizedType)genericType).getActualTypeArguments()[0];
				if (type1 instanceof Class) {
					Provider<?> value = this.getProvider((Class<?>)type1);
					field.set(object, value);
				} else {
					throw new IllegalStateException();
				}
			} else {
				throw new IllegalStateException();
			}
		} else {
			Object value = this.getInstance(type);
			field.set(object, value);
		}
	}

	private Provider<?> getProvider(Class<?> type) {
		return () -> {
			try {
				return this.getInstance(type);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		};
	}	
	
## 落穂ひろい
	
残っている処理としては以下のものがあるでしょう｡
	
	* @Named
	* createChildInjector
	
