package sk.fiit.dps.team11.providers;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.Parameter;

abstract public class AnnotationReadingValueFactoryProvider<T extends Annotation>
	extends SimpleValueFactoryProvider {
	
	private final Class<T> annotationClass;

	protected AnnotationReadingValueFactoryProvider(ServiceLocator locator, Class<T> annotationClass) {
		super(locator);
		this.annotationClass = annotationClass;
	}

	protected abstract Factory<?> createValueFactory(T annotation);
	
	@Override
	protected Factory<?> createValueFactory(Parameter parameter) {
		return Optional.ofNullable(parameter.getAnnotation(annotationClass))
				.map(this::createValueFactory).orElse(null);
	}

}
