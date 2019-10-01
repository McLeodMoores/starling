/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.user.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.DummyWebResource;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.user.UserAccount;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Tests for {@link RemoteUserSource}.
 */
@Test(groups = TestGroup.UNIT)
public class RemoteUserSourceTest {
  private URI _baseUri;

  /**
   * Creates the base URI.
   *
   * @throws URISyntaxException
   *           if there is a problem with the path
   */
  @BeforeMethod
  public void createBaseUri() throws URISyntaxException {
    _baseUri = new URI("path/to");
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri11() {
    new RemoteUserSource(null);
  }

  /**
   * Tests that the base URI cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseUri2() {
    new RemoteUserSource(null, new BasicChangeManager());
  }

  /**
   * Tests that the change manager cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeManager() {
    new RemoteUserSource(_baseUri, null);
  }

  /**
   * Tests the default change manager.
   */
  public void testDefaultChangeManager() {
    assertTrue(new RemoteUserSource(_baseUri).changeManager() instanceof BasicChangeManager);
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAccountNullName() {
    new RemoteUserSource(_baseUri).getAccount((String) null);
  }

  /**
   * Tests getting by name.
   */
  public void testGetAccountByName() {
    final String name = "user name";
    final UserAccount account = new SimpleUserAccount(name);
    final DummyUserWebResource resource = new DummyUserWebResource();
    resource.addData(name, account);
    final RemoteUserSource userSource = new DummyRemoteSource<>(_baseUri, resource);
    assertEquals(userSource.getAccount(name), account);
  }

  private static class DummyRemoteSource<TYPE> extends RemoteUserSource {
    private final DummyWebResource<TYPE> _resource;

    DummyRemoteSource(final URI baseUri, final DummyWebResource<TYPE> resource) {
      super(baseUri);
      _resource = resource;
    }

    DummyRemoteSource(final URI baseUri, final ChangeManager changeManager, final DummyWebResource<TYPE> resource) {
      super(baseUri, changeManager);
      _resource = resource;
    }

    @Override
    public UniformInterface accessRemote(final URI uri) {
      _resource.addURI(getBaseUri(), uri);
      return _resource;
    }
  }

  private static class DummyUserWebResource extends DummyWebResource<UserAccount> {
    private final Map<String, UserAccount> _accountsByName = new HashMap<>();

    public void addData(final String name, final UserAccount account) {
      _accountsByName.put(name, account);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final Class<T> c) throws UniformInterfaceException, ClientHandlerException {
      final String path = getUri().getPath();
      final String[] split = path.split("/");
      final UserAccount result = _accountsByName.get(split[split.length - 1]);
      if (result != null) {
        return (T) result;
      }
      throw new DataNotFoundException("");
    }
  }
}
