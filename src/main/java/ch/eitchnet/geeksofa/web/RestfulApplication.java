package ch.eitchnet.geeksofa.web;

import ch.eitchnet.geeksofa.rest.VideosResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Priorities;
import li.strolch.rest.RestfulStrolchComponent;
import li.strolch.rest.StrolchRestfulClasses;
import li.strolch.rest.StrolchRestfulExceptionMapper;
import li.strolch.rest.filters.AuthenticationRequestFilter;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.logging.Level;

import static ch.eitchnet.geeksofa.web.StartupListener.APP_NAME;

@ApplicationPath("rest")
public class RestfulApplication extends ResourceConfig {

	private static final Logger logger = LoggerFactory.getLogger(RestfulApplication.class);

	public RestfulApplication() {
		setApplicationName(APP_NAME);

		// add project resources by package name
		packages(VideosResource.class.getPackage().getName());

		// strolch services
		for (Class<?> clazz : StrolchRestfulClasses.getRestfulClasses()) {
			register(clazz);
		}

		// filters
		for (Class<?> clazz : StrolchRestfulClasses.getProviderClasses()) {
			if (clazz == AuthenticationRequestFilter.class)
				register(CustomAuthenticationRequestFilter.class, Priorities.AUTHENTICATION);
			else
				register(clazz);
		}

		// log exceptions and return them as plain text to the caller
		register(StrolchRestfulExceptionMapper.class);

		RestfulStrolchComponent restfulComponent = RestfulStrolchComponent.getInstance();
		if (restfulComponent.isRestLogging()) {
			register(new LoggingFeature(java.util.logging.Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
					Level.SEVERE, LoggingFeature.Verbosity.PAYLOAD_ANY, Integer.MAX_VALUE));

			property(ServerProperties.TRACING, "ALL");
			property(ServerProperties.TRACING_THRESHOLD, "TRACE");
		}

		logger.info(MessageFormat.format(
				"Initialized REST application {0} with {1} classes, {2} instances, {3} resources and {4} properties",
				getApplicationName(), getClasses().size(), getInstances().size(), getResources().size(),
				getProperties().size()));
	}
}
