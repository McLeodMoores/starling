/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * A cache of String -> reference data in Mongo.
 */
public class MongoDBReferenceDataCache {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBReferenceDataCache.class);
  /**
   * Mongo field name.
   */
  private static final String SECURITY_DES_KEY_NAME = "Security Description";
  /**
   * Mongo field name.
   */
  private static final String FIELD_DATA_KEY_NAME = "Field Data";

  /**
   * The Mongo connector.
   */
  private final MongoConnector _mongoConnector;
  /**
   * The Mongo collection.
   */
  private final DBCollection _mongoCollection;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   *
   * @param mongoConnector  the Mongo connector, not null
   * @param collectionName  the Mongo collection name, not null
   */
  public MongoDBReferenceDataCache(final MongoConnector mongoConnector, final String collectionName) {
    this(mongoConnector, collectionName, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   *
   * @param mongoConnector  the Mongo connector, not null
   * @param collectionName  the Mongo collection name, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBReferenceDataCache(final MongoConnector mongoConnector, final String collectionName, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(mongoConnector, "mongoConnector");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
    _mongoConnector = mongoConnector;
    _mongoCollection = _mongoConnector.getDBCollection(collectionName);
    _mongoCollection.ensureIndex(SECURITY_DES_KEY_NAME);
  }

  //-------------------------------------------------------------------------
  public Set<String> getAllCachedSecurities() {
    final BasicDBObject query = new BasicDBObject();
    query.put(SECURITY_DES_KEY_NAME, new BasicDBObject("$exists", 1));

    final BasicDBObject fields = new BasicDBObject();
    fields.put(SECURITY_DES_KEY_NAME, 1);
    final DBCursor cursor = _mongoCollection.find(query, fields);
    final Set<String> result = new HashSet<>();
    while (cursor.hasNext()) {
      final DBObject dbObject = cursor.next();
      final String securityDes = (String) dbObject.get(SECURITY_DES_KEY_NAME);
      result.add(securityDes);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  public void save(final ReferenceData securityResult) {
    final FudgeDeserializer deserializer = new FudgeDeserializer(_fudgeContext);

    final String securityDes = securityResult.getIdentifier();
    final FudgeMsg fieldData = securityResult.getFieldValues();

    if (securityDes != null && fieldData != null) {
      LOGGER.info("Persisting fields for \"{}\": {}", securityDes, securityResult.getFieldValues());
      final DBObject mongoDBObject = createMongoDBForResult(deserializer, securityResult);
      LOGGER.debug("dbObject={}", mongoDBObject);
      final BasicDBObject query = new BasicDBObject();
      query.put(SECURITY_DES_KEY_NAME, securityDes);
      _mongoCollection.update(query, mongoDBObject, true, false);
    }
  }

  public Set<String> getAllCachedIdentifiers() {
    final BasicDBObject query = new BasicDBObject();
    query.put(SECURITY_DES_KEY_NAME, new BasicDBObject("$exists", 1));

    final BasicDBObject fields = new BasicDBObject();
    fields.put(SECURITY_DES_KEY_NAME, 1);
    final DBCursor cursor = _mongoCollection.find(query, fields);
    final Set<String> result = new HashSet<>();
    while (cursor.hasNext()) {
      final DBObject dbObject = cursor.next();
      final String securityDes = (String) dbObject.get(SECURITY_DES_KEY_NAME);
      result.add(securityDes);
    }
    return result;
  }

  public Map<String, ReferenceData> load(final Set<String> securities) {
    final Map<String, ReferenceData> result = new TreeMap<>();
    final FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);

    final BasicDBObject query = new BasicDBObject();
    query.put(SECURITY_DES_KEY_NAME, new BasicDBObject("$in", securities));
    final DBCursor cursor = _mongoCollection.find(query);
    while (cursor.hasNext()) {
      final DBObject dbObject = cursor.next();
      LOGGER.debug("dbObject={}", dbObject);

      final String securityDes = (String) dbObject.get(SECURITY_DES_KEY_NAME);
      LOGGER.debug("Have security data for des {} in MongoDB", securityDes);
      final ReferenceData perSecResult = parseDBObject(serializer, securityDes, dbObject);
      if (result.put(securityDes, perSecResult) != null) {
        LOGGER.warn("{}/{} Querying on des {} gave more than one document",
            new Object[] {_mongoConnector.getName(), _mongoCollection.getName(), securityDes });
      }
    }
    return result;
  }

  private ReferenceData parseDBObject(final FudgeSerializer serializer, final String securityDes, final DBObject fromDB) {
    final ReferenceData result = new ReferenceData(securityDes);
    final DBObject fieldData = (DBObject) fromDB.get(FIELD_DATA_KEY_NAME);
    result.setFieldValues(serializer.objectToFudgeMsg(fieldData));
    return result;
  }

  private DBObject createMongoDBForResult(final FudgeDeserializer deserializer, final ReferenceData refDataResult) {
    final BasicDBObject result = new BasicDBObject();
    result.put(SECURITY_DES_KEY_NAME, refDataResult.getIdentifier());
    final DBObject fieldData = deserializer.fudgeMsgToObject(DBObject.class, refDataResult.getFieldValues());
    result.put(FIELD_DATA_KEY_NAME, fieldData);
    return result;
  }

}
