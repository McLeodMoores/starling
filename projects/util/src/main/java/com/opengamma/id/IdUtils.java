/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with identifiers.
 * <p>
 * This class is a thread-safe static utility class.
 */
public final class IdUtils {

  /**
   * Restricted constructor.
   */
  private IdUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the unique identifier of an object if it implements {@code MutableUniqueIdentifiable}.
   * <p>
   * This provides uniform access to objects that support having their unique identifier
   * updated after construction.
   * <p>
   * For example, code in the database layer will need to update the unique identifier
   * when the object is stored.
   *
   * @param object  the object to set into
   * @param uniqueId  the unique identifier to set, may be null
   */
  public static void setInto(final Object object, final UniqueId uniqueId) {
    if (object instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) object).setUniqueId(uniqueId);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a list of {@code UniqueId} or {@code ObjectId} to a list of strings.
   *
   * @param ids  the ids to convert, null returns empty list
   * @return the string list, not null
   */
  public static List<String> toStringList(final Iterable<? extends ObjectIdentifiable> ids) {
    final List<String> strs = new ArrayList<>();
    if (ids != null) {
      for (final ObjectIdentifiable obj : ids) {
        if (obj instanceof UniqueId) {
          strs.add(obj.toString());
        } else {
          strs.add(obj.getObjectId().toString());
        }
      }
    }
    return strs;
  }

  /**
   * Converts a list of strings to a list of {@code UniqueId}.
   *
   * @param uniqueIdStrs  the identifiers to convert, null returns empty list
   * @return the list of unique identifiers, not null
   */
  public static List<UniqueId> parseUniqueIds(final Iterable<String> uniqueIdStrs) {
    final List<UniqueId> uniqueIds = new ArrayList<>();
    if (uniqueIdStrs != null) {
      for (final String uniqueIdStr : uniqueIdStrs) {
        uniqueIds.add(UniqueId.parse(uniqueIdStr));
      }
    }
    return uniqueIds;
  }

  /**
   * Converts a list of strings to a list of {@code ObjectId}.
   *
   * @param objectIdStrs  the identifiers to convert, null returns empty list
   * @return the list of unique identifiers, not null
   */
  public static List<ObjectId> parseObjectIds(final Iterable<String> objectIdStrs) {
    final List<ObjectId> objectIds = new ArrayList<>();
    if (objectIdStrs != null) {
      for (final String objectIdStr : objectIdStrs) {
        objectIds.add(ObjectId.parse(objectIdStr));
      }
    }
    return objectIds;
  }

}
