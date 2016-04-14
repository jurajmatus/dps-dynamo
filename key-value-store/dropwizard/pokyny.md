Základná architektúra a pokyny pre vývoj:

# Interná databáza

BerkeleyDB
* prístup cez abstrahovanú vrstvu DatabaseAdapter
 * získanie inštancie cez @Inject (viď existujúci kód)

# Práca s JSOn
* Knižnica [Jackson](https://github.com/FasterXML/jackson)
* Používať POJO triedy s Jackson anotáciami -> framework ich automaticky (de)serializuje, stačí použiť ako parameter / return type

# Globálne inštancie
* cez injekcie frameworku
* treba zaregistrovať inštanciu v KeyValueStoreService
```java
environment.jersey().register(new AbstractBinder() {
	@Override
	protected void configure() {
		bind(configuration).to(TopConfiguration.class);
		bind(activeMQBundle).to(ActiveMQBundle.class);
		bind(metrics).to(MetricRegistry.class);
		bind(db).to(DatabaseAdapter.class);
		bind(mq).to(MQ.class);
		bind(states).to(RequestStates.class);
		
		// Add new instances here
		bind(someInstance).to(ClassName.class);
		
		bind(ActiveMQSenderFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
		bind(ActiveMQSenderFactoryProvider.InjectionResolver.class)
			.to(new TypeLiteral<InjectionResolver<MQSender>>() {}).in(Singleton.class);
	}
});
```
* získanie v resource-i cez injekciu
```java
class {
...
  @Inject
  private ClassName someInstance;
}
```

# Granularita a asynchronicita vykonávania
* vykonávanie v malých, neblokujúcich kusoch
* medzivýsledok sa zapíše do message queue, tá si to prečíta a pokračuje vo vykonávaní

* Zápis do queue
```java
class {
...
@MQSender(topic = "xxx")
  private ActiveMQSender xxxWorker;
  
  method {
     xxxWorker.send(msgObject);
  }
}
```

# Komunikácia medzi uzlami

cez message queue: ActiveMQ

* poslanie správy
```java
class {
...
  @MQSender
  private MQ mq;
  
  method {
     mq.send("IP.ADDRESS", "topic-name", msgObject);
  }
}
```
* počúvanie: viď SampleWorker
* registrácia v KeyValueStoreService
```java
activeMQBundle.registerReceiver("sample", injectManager.register(new SampleWorker()),
										String.class, true);
```
 * injectManager.register zabezpečí injekciu potrebných inštancií do workera
 
 # Paralelizmus
 
 * používať ScheduledExecutorService poskytnutý frameworkom
 ```java
class {
...
  @Inject
	private ScheduledExecutorService execService;
  
  method {
     execService...
  }
}
```
