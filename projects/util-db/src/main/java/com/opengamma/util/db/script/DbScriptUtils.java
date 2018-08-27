/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Utilities around master database schema versions.
 */
public final class DbScriptUtils {

  private static final String METADATA_FILE = "ogdb-metadata.properties";
  private static final String METADATA_RESOURCE_PATH = "db/" + METADATA_FILE;

  private static final Logger LOGGER = LoggerFactory.getLogger(DbScriptUtils.class);

  private static final Map<String, DbSchemaGroupMetadata> DB_SCHEMA_GROUP_META_DATA;

  static {
    final Map<String, DbSchemaGroupMetadata> schemaGroupMetadata = Maps.newTreeMap(new Comparator<String>() {

      @Override
      public int compare(final String schemaName1, final String schemaName2) {
        if (schemaName1.contains("-") && schemaName2.contains("-")) {
          return schemaName1.compareTo(schemaName2);
        }
        if (schemaName1.contains("-")) {
          return 1;
        }
        if (schemaName2.contains("-")) {
          return -1;
        }
        return schemaName1.compareTo(schemaName2);
      }
    });
    final ClassLoader classLoader = DbScriptUtils.class.getClassLoader();
    try {
      final Enumeration<URL> metadataResourceUrls = classLoader.getResources(METADATA_RESOURCE_PATH);
      while (metadataResourceUrls.hasMoreElements()) {
        final URL metadataResourceUrl = metadataResourceUrls.nextElement();
        final String metadataResourceUrlString = metadataResourceUrl.toExternalForm();
        final String baseResourceUrlString = metadataResourceUrlString.substring(0, metadataResourceUrlString.length() - METADATA_FILE.length() - 1);
        try {
          final InputStream in = metadataResourceUrl.openStream();
          try {
            final Properties properties = new Properties();
            properties.load(in);
            for (final Map.Entry<Object, Object> metadata : properties.entrySet()) {
              final String schemaGroupName = (String) metadata.getKey();
              if (schemaGroupMetadata.containsKey(schemaGroupName)) {
                continue;
              }
              final int currentVersion = Integer.parseInt((String) metadata.getValue());
              schemaGroupMetadata.put(schemaGroupName, new DbSchemaGroupMetadata(schemaGroupName, baseResourceUrlString, currentVersion));
            }
          } catch (final Exception e) {
            LOGGER.error("Error reading database metadata resource at " + metadataResourceUrl, e);
          } finally {
            in.close();
          }
        } catch (final IOException e) {
          LOGGER.error("Error opening database metadata resource at " + metadataResourceUrl, e);
        }
      }
    } catch (final IOException e) {
      LOGGER.error("Error looking for database metadata resources", e);
    }

    DB_SCHEMA_GROUP_META_DATA = ImmutableMap.copyOf(schemaGroupMetadata);
  }

  private DbScriptUtils() {
  }

  public static Integer getCurrentVersion(final String schemaGroupName) {
    final DbSchemaGroupMetadata metadata = getDbSchemaGroupMetadata(schemaGroupName);
    if (metadata == null) {
      return null;
    }
    return metadata.getCurrentVersion();
  }

  public static Set<String> getAllSchemaNames() {
    return DB_SCHEMA_GROUP_META_DATA.keySet();
  }

  public static List<DbSchemaGroupMetadata> getAllSchemaGroupMetadata() {
    final List<DbSchemaGroupMetadata> allSchemaGroupMetadata = Lists.newArrayListWithCapacity(DB_SCHEMA_GROUP_META_DATA.size());
    for (final Entry<String, DbSchemaGroupMetadata> entry : DB_SCHEMA_GROUP_META_DATA.entrySet()) {
      allSchemaGroupMetadata.add(entry.getValue());
    }
    return allSchemaGroupMetadata;
  }

  public static DbSchemaGroupMetadata getDbSchemaGroupMetadata(final String schemaGroupName) {
    return getDbSchemaGroupMetadata().get(schemaGroupName);
  }

  private static Map<String, DbSchemaGroupMetadata> getDbSchemaGroupMetadata() {
    return DB_SCHEMA_GROUP_META_DATA;
  }

}
