package com.opengamma.component.rest;

import java.net.URI;
import java.util.List;
import java.util.Set;

import com.opengamma.component.ComponentInfo;

public interface RestComponents {

  //-------------------------------------------------------------------------
  /**
   * Adds a managed component to the known set.
   * <p>
   * The instance is a JAX_RS class annotated with {@code Path} on the methods.
   * Any {@code Path} at the class level is ignored.
   * See {@link DataComponentServerResource}.
   * 
   * @param info  the managed component info, not null
   * @param instance  the JAX-RS singleton instance, not null
   */
  void publish(ComponentInfo info, Object instance);

  /**
   * Adds a JAX-RS helper instance to the known set.
   * <p>
   * This is used for JAX-RS consumers, producers and filters and unmanaged singleton resources.
   * These classes are not managed by {@code DataComponentsResource}.
   * 
   * @param instance  the JAX-RS singleton instance, not null
   */
  void publishHelper(Object instance);

  /**
   * Adds a JAX-RS root resource to the known set.
   * <p>
   * This is used for JAX-RS unmanaged resources.
   * The class is not managed by {@code DataComponentsResource}.
   * 
   * @param singletonInstance  the unmanaged singleton instance, not null
   */
  void publishResource(Object singletonInstance);

  /**
   * Adds a JAX-RS root resource to the known set.
   * <p>
   * This is used for JAX-RS unmanaged resources.
   * These classes are not managed by {@code DataComponentsResource}.
   * 
   * @param factory  the factory for creating the resource per request, not null
   */
  void publishResource(RestResourceFactory factory);

  /**
   * Re-publishes the component.
   * <p>
   * This is used when a component is read in from a remote location and is then re-published.
   * 
   * @param info  the component information, not null
   */
  void republish(ComponentInfo info);

  //-------------------------------------------------------------------------
  /**
   * Gets the complete set of singletons, handling managed components.
   * <p>
   * This method wraps the managed components in an instance of {@link DataComponentServerResource}.
   * 
   * @return the complete set of singletons, not null
   */
  Set<Object> buildJaxRsSingletons();

  /**
   * Gets the complete set of JaxRs classes.
   * 
   * @return the complete set of classes, not null
   */
  Set<Class<?>> buildJaxRsClasses();

  //-----------------------------------------------------------------------
  /**
   * Gets the base URI.
   * This is normally the base URI of all the JAX-RS resources.
   * @return the value of the property, not null
   */
  URI getBaseUri();

  //-----------------------------------------------------------------------
  /**
   * Gets the managed components.
   * These will be controlled by {@link DataComponentServerResource}.
   * @return the value of the property, not null
   */
  List<RestComponent> getLocalComponents();

  /**
   * Sets the managed components.
   * These will be controlled by {@link DataComponentServerResource}.
   * @param localComponents  the new value of the property, not null
   */
  void setLocalComponents(List<RestComponent> localComponents);

  //-----------------------------------------------------------------------
  /**
   * Gets the remote components.
   * These have been imported from another server and are being re-exposed.
   * @return the value of the property, not null
   */
  List<ComponentInfo> getRemoteComponents();

  /**
   * Sets the remote components.
   * These have been imported from another server and are being re-exposed.
   * @param remoteComponents  the new value of the property, not null
   */
  void setRemoteComponents(List<ComponentInfo> remoteComponents);

  //-----------------------------------------------------------------------
  /**
   * Gets the set of root resources.
   * These are not managed by {@link DataComponentServerResource}.
   * @return the value of the property, not null
   */
  Set<Object> getRootResourceSingletons();

  /**
   * Sets the set of root resources.
   * These are not managed by {@link DataComponentServerResource}.
   * @param rootResourceSingletons  the new value of the property, not null
   */
  void setRootResourceSingletons(Set<Object> rootResourceSingletons);

  //-----------------------------------------------------------------------
  /**
   * Gets the set of root resource factories.
   * These are not managed by {@link DataComponentServerResource}.
   * @return the value of the property, not null
   */
  Set<RestResourceFactory> getRootResourceFactories();

  /**
   * Sets the set of root resource factories.
   * These are not managed by {@link DataComponentServerResource}.
   * @param rootResourceFactories  the new value of the property, not null
   */
  void setRootResourceFactories(Set<RestResourceFactory> rootResourceFactories);

  //-----------------------------------------------------------------------
  /**
   * Gets the set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   * @return the value of the property, not null
   */
  Set<Object> getHelpers();

  /**
   * Sets the set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   * @param helpers  the new value of the property, not null
   */
  void setHelpers(Set<Object> helpers);

}
