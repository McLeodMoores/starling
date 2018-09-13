/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.SingletonFactoryBean;

/**
 * An implementation of {@link EndPointDescriptionProvider} that produces values from a local or remote URI.
 */
public class UriEndPointDescriptionProviderFactoryBean extends SingletonFactoryBean<UriEndPointDescriptionProvider> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UriEndPointDescriptionProviderFactoryBean.class);

  private static final boolean ENABLE_IPV4 = System.getProperty("com.opengamma.transport.jaxrs.UriEndPointDescriptionProviderFactoryBean.disableIPv4") == null;
  private static final boolean ENABLE_IPV6 = System.getProperty("com.opengamma.transport.jaxrs.UriEndPointDescriptionProviderFactoryBean.enableIPv6") != null;

  private final List<String> _uris = new LinkedList<>();

  private String _local;
  private int _port = 80;
  private int _securePort = 443;
  private boolean _secure;

  //-------------------------------------------------------------------------
  /**
   * Sets an absolute URI.
   *
   * @param uri the absolute URI, e.g. {@code http://hostname.domain:port/foo/bar}
   */
  public void setAbsolute(final String uri) {
    _uris.add(uri);
  }

  /**
   * Sets a local path using the default host and port.
   *
   * @param local  the local path, e.g. {@code /foo/bar}
   */
  public void setLocal(final String local) {
    _local = local;
  }

  /**
   * Sets the default port.
   *
   * @param port  the default port
   */
  public void setPort(final int port) {
    _port = port;
  }

  /**
   * Gets the port.
   *
   * @return  the port
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets the secure port.
   *
   * @param securePort  the secure port
   */
  public void setSecurePort(final int securePort) {
    _securePort = securePort;
  }

  /**
   * Gets the secure port.
   *
   * @return  the secure port
   */
  public int getSecurePort() {
    return _securePort;
  }

  /**
   * Sets whether or not the port is secure.
   *
   * @param isSecure  true if the port should be secure
   */
  public void setSecure(final boolean isSecure) {
    _secure = isSecure;
  }

  /**
   * True if the port is secure.
   *
   * @return  true if the port is secure
   */
  public boolean isSecure() {
    return _secure;
  }

  //-------------------------------------------------------------------------
  @Override
  protected UriEndPointDescriptionProvider createObject() {
    if (_local != null) {
      if (_secure) {
        LOGGER.warn("Secure local connections not available - using unsecured connections");
      }
      final Collection<String> localAddresses = getLocalNetworkAddresses();
      for (final String address : localAddresses) {
        final String uri = "http://" + address + ":" + _port + _local;
        _uris.add(uri);
        LOGGER.debug("Publishing {}", uri);
      }
    }
    return new UriEndPointDescriptionProvider(_uris);
  }

  //-------------------------------------------------------------------------
  private Collection<String> getLocalNetworkAddresses() {
    final List<String> addresses = new LinkedList<>();
    try {
      final Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
      while (ni.hasMoreElements()) {
        loadInterfaceAddress(ni.nextElement(), addresses);
      }
    } catch (final IOException e) {
      LOGGER.warn("Error resolving local addresses; no local connections available", e);
      return Collections.emptySet();
    }
    return addresses;
  }

  private void loadInterfaceAddress(final NetworkInterface iface, final Collection<String> addresses) {
    final Enumeration<NetworkInterface> ni = iface.getSubInterfaces();
    while (ni.hasMoreElements()) {
      loadInterfaceAddress(ni.nextElement(), addresses);
    }
    final Enumeration<InetAddress> ai = iface.getInetAddresses();
    while (ai.hasMoreElements()) {
      final InetAddress a = ai.nextElement();
      if (a.isLoopbackAddress()) {
        continue;
      }
      if (a instanceof Inet4Address) {
        if (ENABLE_IPV4) {
          addresses.add(a.getHostAddress());
        }
      } else if (a instanceof Inet6Address) {
        if (ENABLE_IPV6) {
          addresses.add("[" + a.getHostAddress() + "]");
        }
      }
    }
  }

}
