/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

import net.sf.ehcache.util.NamedThreadFactory;

/**
 * Utilities that assist with obtaining a tool context.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ToolContextUtils {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ToolContextUtils.class);

  /**
   * The default classifier chain for selecting components from a server
   */
  private static final List<String> DEFAULT_CLASSIFIER_CHAIN =
      Arrays.asList("main", "combined", "shared", "central", "default");

  /**
   * Restricted constructor.
   */
  private ToolContextUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Uses a {@code ComponentManager} or a {@code ComponentServer} to start and load a {@code ToolContext}.
   * <p>
   * The context should be closed after use.
   *
   * @param <T>  the tool context type
   * @param configResourceLocation  the location of the context resource file, not null
   * @param toolContextClazz  the type of tool context to return, not null
   * @return the context, not null
   */
  public static <T extends ToolContext> T getToolContext(final String configResourceLocation, final Class<T> toolContextClazz) {
    return getToolContext(configResourceLocation, toolContextClazz, DEFAULT_CLASSIFIER_CHAIN);
  }

  //-------------------------------------------------------------------------
  /**
   * Uses a {@code ComponentManager} or a {@code ComponentServer} to start and load a {@code ToolContext}.
   * <p>
   * The context should be closed after use.
   *
   * @param <T>  the tool context type
   * @param configResourceLocation  the location of the context resource file, not null
   * @param toolContextClazz  the type of tool context to return, not null
   * @param classifierChain  the classifier chain to use when determining which components to select
   * @return the context, not null
   */
  public static <T extends ToolContext> T getToolContext(String configResourceLocation, final Class<T> toolContextClazz, final List<String> classifierChain) {
    configResourceLocation = configResourceLocation.trim();

    if (configResourceLocation.startsWith("http://")) {
      return createToolContextByHttp(configResourceLocation, toolContextClazz, classifierChain);

    } else {  // use local file
      final ComponentManager manager = new ComponentManager("toolcontext");
      manager.start(configResourceLocation);
      final ComponentRepository repo = manager.getRepository();
      return toolContextClazz.cast(repo.getInstance(ToolContext.class, "tool"));
    }
  }

  private static <T extends ToolContext> T createToolContextByHttp(String configResourceLocation, final Class<T> toolContextClazz, final List<String> classifierChain) {
    configResourceLocation = StringUtils.stripEnd(configResourceLocation, "/");
    if (configResourceLocation.endsWith("/jax") == false) {
      configResourceLocation += "/jax";
    }

    // Get the remote component server using the supplied URI
    final RemoteComponentServer remoteComponentServer = new RemoteComponentServer(URI.create(configResourceLocation));
    final ComponentServer componentServer = remoteComponentServer.getComponentServer();

    // Attempt to build a tool context of the specified type
    T toolContext;
    try {
      toolContext = toolContextClazz.newInstance();
    } catch (final Throwable t) {
      return null;
    }

    // Populate the tool context from the remote component server
    for (final MetaProperty<?> metaProperty : toolContext.metaBean().metaPropertyIterable()) {
      if (metaProperty.propertyType().equals(ComponentServer.class)) {
        metaProperty.set(toolContext, componentServer);
      } else if (!metaProperty.name().equals("contextManager")) {
        try {
          final ComponentInfo componentInfo = getComponentInfo(componentServer, classifierChain, metaProperty.propertyType());
          if (componentInfo == null) {
            LOGGER.debug("Unable to populate tool context '" + metaProperty.name() +
                "', no appropriate component found on the server");
            continue;
          }
          if (ViewProcessor.class.equals(componentInfo.getType())) {
            final JmsConnector jmsConnector = createJmsConnector(componentInfo);
            final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("rvp"));
            final ViewProcessor vp = new RemoteViewProcessor(componentInfo.getUri(), jmsConnector, scheduler);
            toolContext.setViewProcessor(vp);
            toolContext.setContextManager(new Closeable() {
              @Override
              public void close() throws IOException {
                scheduler.shutdownNow();
                jmsConnector.close();
              }
            });
          } else {
            final String clazzName = componentInfo.getAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA);
            if (clazzName == null) {
              LOGGER.warn("Unable to populate tool context '" + metaProperty.name() +
                  "', no remote access class found");
              continue;
            }
            final Class<?> clazz = Class.forName(clazzName);
            metaProperty.set(toolContext, clazz.getConstructor(URI.class).newInstance(componentInfo.getUri()));
            LOGGER.info("Populated tool context '" + metaProperty.name() + "' with " + metaProperty.get(toolContext));
          }
        } catch (final Throwable ex) {
          LOGGER.warn("Unable to populate tool context '" + metaProperty.name() + "': " + ex.getMessage());
        }
      }
    }
    return toolContext;
  }

  private static JmsConnector createJmsConnector(final ComponentInfo info) {
    final JmsConnectorFactoryBean jmsConnectorFactoryBean = new JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setName("ToolContext JMS Connector");
    final String jmsBroker = info.getAttribute(ComponentInfoAttributes.JMS_BROKER_URI);
    final URI jmsBrokerUri = URI.create(jmsBroker);
    jmsConnectorFactoryBean.setClientBrokerUri(jmsBrokerUri);
    jmsConnectorFactoryBean.setConnectionFactory(new ActiveMQConnectionFactory(jmsBrokerUri));
    return jmsConnectorFactoryBean.getObjectCreating();
  }

  private static ComponentInfo getComponentInfo(final ComponentServer componentServer, final List<String> preferenceList, final Class<?> type) {
    final Map<String, ComponentInfo> infos = componentServer.getComponentInfoMap(type);
    if (preferenceList != null) {
      for (final String preference : preferenceList) {
        final ComponentInfo componentInfo = infos.get(preference);
        if (componentInfo != null) {
          return componentInfo;
        }
      }
    }
    infos.remove("test");
    if (infos.size() == 0) {
      return null;
    }
    if (infos.size() > 1) {
      LOGGER.warn("Multiple remote components match: " + type.getSimpleName() + "::" + infos.keySet());
      return null;
    }
    return infos.values().iterator().next();
  }

}
