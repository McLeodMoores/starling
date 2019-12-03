/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collections;

import org.joda.beans.JodaBeanUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataDuplicationException;
import com.opengamma.DataNotFoundException;
import com.opengamma.DataVersionException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.master.user.UserSearchSortOrder;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryUserMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryUserMasterTest {

  private static final String TEST_USER_1 = "testuser1";
  private static final String TEST_USER_2 = "testuser2";
  private static final String EMAIL_ADDRESS = "info@opengamma.com";
  private static final ExternalId BLOOMBERG_SID = ExternalId.of("BloombergSid", "837283");
  private static final ExternalId OTHER_USER_ID1 = ExternalId.of("OtherUserId", "sk03e47s");
  private static final ExternalId OTHER_USER_ID2 = ExternalId.of("OtherUserId", "352378");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(BLOOMBERG_SID, OTHER_USER_ID1);

  private InMemoryUserMaster _master;
  private ManageableUser _addedUser1;
  private ManageableUser _addedUser2;

  /**
   * Populates the master before each method.
   */
  @BeforeMethod
  public void setUp() {
    _master = new InMemoryUserMaster();
    _addedUser1 = new ManageableUser(TEST_USER_1);
    _addedUser1.setAlternateIds(BUNDLE_FULL);
    _addedUser1.setEmailAddress(EMAIL_ADDRESS);
    final UniqueId addedId1 = _master.add(_addedUser1);
    _addedUser1.setUniqueId(addedId1);
    _addedUser2 = new ManageableUser(TEST_USER_2);
    _addedUser2.setAlternateIds(OTHER_USER_ID2.toBundle());
    final UniqueId addedId2 = _master.add(_addedUser2);
    _addedUser2.setUniqueId(addedId2);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests retrieval by name.
   */
  public void testGetByNameMatch1() {
    final ManageableUser result = _master.getByName(TEST_USER_1);
    assertEquals(UniqueId.of("MemUsr", "1", "1"), result.getUniqueId());
    assertEquals(_addedUser1, result);
  }

  /**
   * Tests retrieval by name.
   */
  public void testGetByNameMatch2() {
    final ManageableUser result = _master.getByName(TEST_USER_2);
    assertEquals(UniqueId.of("MemUsr", "2", "1"), result.getUniqueId());
    assertEquals(_addedUser2, result);
  }

  /**
   * Tests that the master ignores the case of the name.
   */
  public void testGetByNameMatchCaseInsensitive() {
    final ManageableUser result = _master.getByName("TestUser1");
    assertEquals(UniqueId.of("MemUsr", "1", "1"), result.getUniqueId());
    assertEquals(_addedUser1, result);
  }

  /**
   * Tests that an exception is thrown if there is no user that matches.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetByNameNoMatch() {
    _master.getByName("notfound");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests retrieval by object id.
   */
  public void testGetByIdMatch() {
    final ManageableUser result = _master.getById(_addedUser1.getObjectId());
    assertEquals(UniqueId.of("MemUsr", "1", "1"), result.getUniqueId());
    assertEquals(_addedUser1, result);
  }

  /**
   * Tests that an exception is thrown if there is no user that matches.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testGetByIdNoMatch() {
    _master.getById(ObjectId.of("A", "B"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the addition of a user.
   */
  @Test
  public void testAdd() {
    final ManageableUser user = new ManageableUser("newuser");
    final UniqueId uniqueId = _master.add(user);
    user.setUniqueId(uniqueId);
    assertEquals(user, _master.getByName("newuser"));
    assertEquals(_addedUser1, _master.getByName(TEST_USER_1));
    assertEquals(_addedUser2, _master.getByName(TEST_USER_2));
  }

  /**
   * Tests that a duplicate user cannot be added.
   */
  @Test(expectedExceptions = DataDuplicationException.class)
  public void testAddAlreadyExists() {
    _master.add(new ManageableUser(TEST_USER_1));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a user entry can be updated with a new field.
   */
  @Test
  public void testUpdate() {
    final ManageableUser updatedUser = _addedUser1.clone();
    updatedUser.setPasswordHash("foo");
    final UniqueId uniqueId = _master.update(updatedUser);
    updatedUser.setUniqueId(uniqueId);
    assertEquals(UniqueId.of("MemUsr", "1", "2"), uniqueId);
    assertEquals(updatedUser, _master.getByName(TEST_USER_1));
    assertEquals(_addedUser2, _master.getByName(TEST_USER_2));
  }

  /**
   * Tests that the name of a user can be updated.
   */
  @Test
  public void testUpdateRename() {
    final ManageableUser updatedUser = _addedUser1.clone();
    updatedUser.setUserName("newuser");
    final UniqueId uniqueId = _master.update(updatedUser);
    updatedUser.setUniqueId(uniqueId);
    assertEquals(UniqueId.of("MemUsr", "1", "2"), uniqueId);
    assertEquals(updatedUser, _master.getByName("newuser"));
    assertEquals(updatedUser, _master.getByName(TEST_USER_1));
    assertEquals(_addedUser2, _master.getByName(TEST_USER_2));
  }

  /**
   * Tests that the unique id version must match the original when updating.
   */
  @Test(expectedExceptions = DataVersionException.class)
  public void testUpdateBadVersion() {
    final ManageableUser updatedUser = _addedUser1.clone();
    updatedUser.setUniqueId(UniqueId.of("MemUsr", "1", "9"));
    _master.update(updatedUser);
  }

  /**
   * Tests that a user cannot be updated with the same name as another.
   */
  @Test(expectedExceptions = DataDuplicationException.class)
  public void testUpdateRenameAlreadyExists() {
    final ManageableUser updatedUser = _addedUser1.clone();
    updatedUser.setUserName(TEST_USER_2);
    _master.update(updatedUser);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests removing a user by name.
   */
  @Test
  public void testRemoveByName() {
    _master.removeByName(TEST_USER_1);
    assertEquals(_addedUser2, _master.getByName(TEST_USER_2));
    try {
      _master.getByName(TEST_USER_1);
      fail();
    } catch (final DataNotFoundException ex) {
      // expected
    }
    try {
      _master.getById(_addedUser1.getObjectId());
      fail();
    } catch (final DataNotFoundException ex) {
      // expected
    }
  }

  /**
   * Test that an exception is thrown when trying to remove a user that does not exist.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveByNameNotFound() {
    _master.removeByName("notfound");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests removing a user by id.
   */
  @Test
  public void testRemoveById() {
    _master.removeById(_addedUser1.getObjectId());
    assertEquals(_addedUser2, _master.getByName(TEST_USER_2));
    try {
      _master.getById(_addedUser1.getObjectId());
      fail();
    } catch (final DataNotFoundException ex) {
      // expected
    }
    try {
      _master.getByName(TEST_USER_1);
      fail();
    } catch (final DataNotFoundException ex) {
      // expected
    }
    _master.removeById(_addedUser1.getObjectId());  // idempotent
  }

  /**
   * Test that an exception is thrown when trying to remove a user that does not exist.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testRemoveByIdNotFound() {
    _master.removeById(ObjectId.of("NOT", "FOUND"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an exception is thrown when trying to save a user with a unique id that has not previously
   * been added to the master.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testSaveNotPresent() {
    final String name = "TestUser3";
    final ManageableUser user = new ManageableUser(name);
    user.setUniqueId(UniqueId.of("MemUsr", "100", "1"));
    _master.save(user);
  }

  /**
   * Tests that saving will update an object that has a unique id.
   */
  @Test
  public void testSaveUpdates() {
    final ManageableUser user = _master.getByName(TEST_USER_1);
    _master.save(user);
    final ManageableUser saved = _master.getByName(TEST_USER_1);
    assertNotNull(user.getUniqueId());
    assertNotNull(saved.getUniqueId());
    assertNotEquals(user.getUniqueId(), saved.getUniqueId());
    assertTrue(JodaBeanUtils.equalIgnoring(saved, user, ManageableUser.meta().metaProperty("uniqueId")));
  }

  /**
   * Tests that saving an object without an id adds it to the master.
   */
  @Test
  public void testSaveNewUser() {
    final String name = "TestUser3";
    final ManageableUser user = new ManageableUser(name);
    assertNull(user.getUniqueId());
    _master.save(user);
    final ManageableUser saved = _master.getByName(name);
    assertNull(user.getUniqueId());
    assertNotNull(saved.getUniqueId());
    assertTrue(JodaBeanUtils.equalIgnoring(saved, user, ManageableUser.meta().metaProperty("uniqueId")));
  }

  //-------------------------------------------------------------------------
  /**
   * Searches the master for a user by name.
   */
  public void testSearchByName() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setUserName(TEST_USER_1);
    final UserSearchResult result = _master.search(request);
    assertEquals(result.getUsers().size(), 1);
    assertEquals(result.getUsers().get(0), _addedUser1);
  }

  /**
   * Searches the master for a user by id.
   */
  public void testSearchById() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setObjectIds(Collections.singleton(_addedUser1.getObjectId()));
    final UserSearchResult result = _master.search(request);
    assertEquals(result.getUsers().size(), 1);
    assertEquals(result.getUsers().get(0), _addedUser1);
  }

  /**
   * Searches for users by name.
   */
  public void testSearchMultipleByName() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setUserName("testUse*");
    UserSearchResult result = _master.search(request);
    assertEquals(result.getUsers().size(), 2);
    assertEquals(result.getUsers().get(0), _addedUser1);
    assertEquals(result.getUsers().get(1), _addedUser2);
    request.setSortOrder(UserSearchSortOrder.NAME_DESC);
    result = _master.search(request);
    assertEquals(result.getUsers().size(), 2);
    assertEquals(result.getUsers().get(0), _addedUser2);
    assertEquals(result.getUsers().get(1), _addedUser1);
  }

  /**
   * Searches the master for a user by id.
   */
  public void testSearchMultipleById() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setObjectIds(Arrays.asList(_addedUser1.getObjectId(), _addedUser1.getObjectId(), _addedUser2.getObjectId()));
    final UserSearchResult result = _master.search(request);
    assertEquals(result.getUsers().size(), 2);
    assertEquals(result.getUsers().get(0), _addedUser1);
    assertEquals(result.getUsers().get(1), _addedUser2);
  }

  /**
   * Tests that an empty result is returned.
   */
  public void testSearchNoMatchByName() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setUserName("testuser3");
    final UserSearchResult result = _master.search(request);
    assertTrue(result.getUsers().isEmpty());
  }

  /**
   * Tests that an empty result is returned.
   */
  public void testSearchNoMatchById() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setObjectIds(Collections.singleton(ObjectId.of("MemUsr", "300")));
    final UserSearchResult result = _master.search(request);
    assertTrue(result.getUsers().isEmpty());
  }

  /**
   * Tests that an empty result is returned.
   */
  public void testSearchNoMatchByPermissions() {
    final UserSearchRequest request = new UserSearchRequest();
    request.setAssociatedPermission("all");
    final UserSearchResult result = _master.search(request);
    assertTrue(result.getUsers().isEmpty());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that an exception is thrown if the user has not been stored.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testEventHistoryByIdUnknownUser() {
    final String name = "TestUser3";
    final ManageableUser user = new ManageableUser(name);
    user.setUniqueId(UniqueId.of("MemUsr", "100", "1"));
    _master.eventHistory(user.getObjectId(), null);
  }

  /**
   * Tests that an exception is thrown if the user has not been stored.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testEventHistoryByNameUnknownUser() {
    final String name = "TestUser3";
    final ManageableUser user = new ManageableUser(name);
    user.setUniqueId(UniqueId.of("MemUsr", "100", "1"));
    _master.eventHistory(null, name);
  }

  /**
   * Tests that an empty history is always returned.
   */
  @Test
  public void testEventHistory() {
    assertTrue(_master.eventHistory(null, null).isEmpty());
    assertTrue(_master.eventHistory(_addedUser1.getObjectId(), null).isEmpty());
    assertTrue(_master.eventHistory(null, _addedUser1.getUserName()).isEmpty());
    assertTrue(_master.eventHistory(_addedUser1.getObjectId(), _addedUser1.getUserName()).isEmpty());
    assertTrue(_master.eventHistory(null, null).isEmpty());
    assertTrue(_master.eventHistory(_addedUser2.getObjectId(), null).isEmpty());
    assertTrue(_master.eventHistory(null, _addedUser2.getUserName()).isEmpty());
    assertTrue(_master.eventHistory(_addedUser2.getObjectId(), _addedUser2.getUserName()).isEmpty());
  }

  /**
   * Tests that an empty history is always returned.
   */
  @Test
  public void testEventHistoryByUserRequest() {
    assertTrue(_master.eventHistory(new UserEventHistoryRequest(_addedUser1.getObjectId())).getEvents().isEmpty());
    assertTrue(_master.eventHistory(new UserEventHistoryRequest(_addedUser1.getUserName())).getEvents().isEmpty());
  }

  /**
   * Tests toString().
   */
  @Test
  public void testToString() {
    assertEquals("InMemoryUserMaster[size=2]", _master.toString());
  }
}
