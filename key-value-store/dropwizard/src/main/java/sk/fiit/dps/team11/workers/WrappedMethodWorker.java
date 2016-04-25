package sk.fiit.dps.team11.workers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQBaseExceptionHandler;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;
import com.kjetland.dropwizard.activemq.ActiveMQReceiver;

public class WrappedMethodWorker<T> implements ActiveMQReceiver<T>, ActiveMQBaseExceptionHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WrappedMethodWorker.class);
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface MQListener {
		public String queue();
	}
	
	public static List<WrappedMethodWorker<?>> scan(Object multiWorker) {
		List<WrappedMethodWorker<?>> workers = new ArrayList<>();
		for (Method method : multiWorker.getClass().getMethods()) {
			MQListener info = method.getAnnotation(MQListener.class);
			if (info != null) {
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 1) {
					workers.add(new WrappedMethodWorker<>(info, multiWorker, method, parameterTypes[0]));
				}
			}
		}
		return workers;
	}
	
	private final MQListener info;
	
	private final Object multiWorker;
	
	private final Method method;
	
	private final Class<T> cls;
	
	private WrappedMethodWorker(MQListener info, Object multiWorker, Method method, Class<T> cls) {
		this.info = info;
		this.multiWorker = multiWorker;
		this.method = method;
		this.cls = cls;
	}

	public void register(ActiveMQBundle bundle) {
		bundle.registerReceiver(info.queue(), this, cls, true);
	}
	
	@Override
	public boolean onException(Message jmsMessage, String message, Exception exception) {
		
		LOGGER.error("Error reading from queue: {}", info.queue(), exception);
		
		return false;
	}

	@Override
	public void receive(T requestId) {
		try {
			method.invoke(multiWorker, requestId);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error("Worker invocation error ({}.{})", multiWorker.getClass().getSimpleName(), method.getName());
		}
	}

}
