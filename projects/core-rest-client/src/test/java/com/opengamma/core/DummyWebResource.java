/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Implementation of a {@link UniformInterface} that can be used in testing of
 * remote sources. It allows responses to be stored internally, so that a REST
 * client is not needed.
 *
 * All methods throw {@link NotImplementedException}.
 *
 * @param <TYPE>
 *          The type of the items to be returned
 */
public abstract class DummyWebResource<TYPE> implements UniformInterface {
  private final Map<UniqueId, List<TYPE>> _dataByUniqueId = new HashMap<>();
  private final Map<Pair<ExternalIdBundle, String>, List<TYPE>> _dataByExternalIdBundle = new HashMap<>();
  private URI _baseUri;
  private URI _uri;

  /**
   * Adds the base and full URI to be requested.
   *
   * @param baseUri
   *          the base URI, not null
   * @param uri
   *          the URI, not null
   */
  public void addURI(final URI baseUri, final URI uri) {
    _baseUri = ArgumentChecker.notNull(baseUri, "baseUri");
    _uri = ArgumentChecker.notNull(uri, "uri");
  }

  /**
   * Gets the base URI.
   *
   * @return the base URI
   */
  public URI getBaseUri() {
    return _baseUri;
  }

  /**
   * Gets the URI.
   *
   * @return the URI
   */
  public URI getUri() {
    return _uri;
  }

  /**
   * Adds data for a unique id.
   *
   * @param uid
   *          the unique id
   * @param data
   *          the data
   */
  public void addData(final UniqueId uid, final TYPE data) {
    List<TYPE> items = _dataByUniqueId.get(uid);
    if (items == null) {
      items = new ArrayList<>();
    }
    items.add(data);
    _dataByUniqueId.put(uid, items);
  }

  /**
   * Adds data for an object id and version.
   *
   * @param oid
   *          the object id
   * @param vc
   *          the version
   * @param data
   *          the data
   */
  public void addData(final ObjectId oid, final VersionCorrection vc, final TYPE data) {
    addData(UniqueId.of(oid, vc.toString()), data);
  }

  /**
   * Adds data for an external id.
   *
   * @param eid
   *          the external id
   * @param data
   *          the data
   */
  public void addData(final ExternalId eid, final TYPE data) {
    addData(eid.toBundle(), VersionCorrection.LATEST, data);
  }

  /**
   * Adds data for an external id and version.
   * 
   * @param eid
   *          the external id
   * @param vc
   *          the version
   * @param data
   *          the data
   */
  public void addData(final ExternalId eid, final VersionCorrection vc, final TYPE data) {
    addData(eid.toBundle(), vc, data);
  }

  /**
   * Adds data for an external id bundle.
   * 
   * @param eids
   *          the external ids
   * @param data
   *          the data
   */
  public void addData(final ExternalIdBundle eids, final TYPE data) {
    addData(eids, VersionCorrection.LATEST, data);
  }

  /**
   * Adds data for an external id bundle and version.
   * 
   * @param eids
   *          the external ids
   * @param vc
   *          the version
   * @param data
   *          the data
   */
  public void addData(final ExternalIdBundle eids, final VersionCorrection vc, final TYPE data) {
    final Pair<ExternalIdBundle, String> key = Pairs.of(eids, vc.toString());
    List<TYPE> items = _dataByExternalIdBundle.get(key);
    if (items == null) {
      items = new ArrayList<>();
    }
    items.add(data);
    _dataByExternalIdBundle.put(key, items);
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public Map<UniqueId, List<TYPE>> getDataByUniqueId() {
    return _dataByUniqueId;
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public Map<Pair<ExternalIdBundle, String>, List<TYPE>> getDataByExternalIdBundle() {
    return _dataByExternalIdBundle;
  }

  @Override
  public ClientResponse head() throws ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T options(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T options(final GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T get(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T get(final GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void put() throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void put(final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T put(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T put(final GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T put(final Class<T> c, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T put(final GenericType<T> gt, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void post() throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void post(final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T post(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T post(final GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T post(final Class<T> c, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T post(final GenericType<T> gt, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void delete() throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void delete(final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T delete(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T delete(final GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T delete(final Class<T> c, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T delete(final GenericType<T> gt, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void method(final String method) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public void method(final String method, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T method(final String method, final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T method(final String method, final GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T method(final String method, final Class<T> c, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T method(final String method, final GenericType<T> gt, final Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
    throw new NotImplementedException();
  }

}
