/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Decorates a reference data provider, adding caching.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHValueCachingReferenceDataProvider extends AbstractValueCachingReferenceDataProvider {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EHValueCachingReferenceDataProvider.class);
  /**
   * Cache key for reference data.
   */
  /*package*/ static final String REFERENCE_DATA_CACHE = "referenceData";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The reference data cache.
   */
  private final Cache _cache;

  /**
   * Creates an instance.
   *
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   *
   * @param underlying  the underlying reference data provider, not null
   * @param cacheManager  the cache manager, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public EHValueCachingReferenceDataProvider(final ReferenceDataProvider underlying, final CacheManager cacheManager, final FudgeContext fudgeContext) {
    super(underlying, fudgeContext);
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    _cacheManager.addCacheIfAbsent(REFERENCE_DATA_CACHE);
    _cache = _cacheManager.getCache(REFERENCE_DATA_CACHE);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cache manager.
   *
   * @return the cache manager, not null
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Map<String, ReferenceData> loadFieldValues(final Set<String> identifiers) {
    final Map<String, ReferenceData> result = Maps.newTreeMap();
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    for (final String identifier : identifiers) {
      final ReferenceData cachedResult = loadStateFromCache(serializer, identifier);
      if (cachedResult != null) {
        result.put(identifier, cachedResult);
      }
    }
    return result;
  }

  @Override
  protected void saveFieldValues(final ReferenceData result) {
    final String identifier = result.getIdentifier();
    final FudgeMsg fieldData = result.getFieldValues();

    if (identifier != null && fieldData != null) {
      LOGGER.info("Persisting fields for \"{}\": {}", identifier, result.getFieldValues());
      final Object cachedObject = createCachedObject(result);
      LOGGER.debug("cachedObject={}", cachedObject);
      final Element element = new Element(identifier, cachedObject);
      _cache.put(element);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the state from the cache.
   *
   * @param serializer  the Fudge serializer, not null
   * @param identifier  the identifier, not null
   * @return the result, null if not found
   */
  protected ReferenceData loadStateFromCache(final FudgeSerializer serializer, final String identifier) {
    final Element element = _cache.get(identifier);
    if (element != null) {
      LOGGER.debug("Have security data for des {} in cache", identifier);
      final Object fromCache = element.getObjectValue();
      LOGGER.debug("cachedObject={}", fromCache);
      return parseCachedObject(fromCache);
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Data holder for storing the results.
   */
  private static final class CachedReferenceData implements Serializable {

    private static final long serialVersionUID = 3L;

    private transient String _security;

    private transient FudgeContext _fudgeContext;

    private transient volatile FudgeMsg _fieldDataMsg;

    private transient volatile byte[] _fieldData;

    private byte[] getFieldData() {
      byte[] fieldData = _fieldData;
      if (fieldData == null) {
        synchronized (this) {
          fieldData = _fieldData;
          if (fieldData == null) {
            fieldData = _fudgeContext.toByteArray(_fieldDataMsg);
            _fieldData = fieldData;
            _fieldDataMsg = null;
          }
        }
      }
      return fieldData;
    }

    private void setFieldData(final byte[] fieldData) {
      _fieldData = fieldData;
    }

    public FudgeMsg getFieldDataMsg(final FudgeContext fudgeContext) {
      FudgeMsg fieldDataMsg = _fieldDataMsg;
      if (fieldDataMsg == null) {
        synchronized (this) {
          fieldDataMsg = _fieldDataMsg;
          if (fieldDataMsg == null) {
            _fudgeContext = fudgeContext;
            fieldDataMsg = fudgeContext.deserialize(_fieldData).getMessage();
            _fieldDataMsg = fieldDataMsg;
            _fieldData = null;
          }
        }
      }
      return _fieldDataMsg;
    }

    public void setFieldDataMsg(final FudgeMsg fieldDataMsg, final FudgeContext fudgeContext) {
      _fieldDataMsg = fieldDataMsg;
      _fudgeContext = fudgeContext;
    }

    public String getSecurity() {
      return _security;
    }

    public void setSecurity(final String security) {
      _security = security;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
      out.writeUTF(getSecurity());
      final byte[] fieldData = getFieldData();
      out.writeInt(fieldData.length);
      out.write(fieldData);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
      setSecurity(in.readUTF());
      final int dataLength = in.readInt();
      final byte[] data = new byte[dataLength];
      in.readFully(data);
      setFieldData(data);
    }

  }

  //-------------------------------------------------------------------------
  /**
   * Parse the cached object.
   *
   * @param fromCache  the data from the cache, not null
   * @return the result, not null
   */
  protected ReferenceData parseCachedObject(final Object fromCache) {
    final CachedReferenceData rd = (CachedReferenceData) fromCache;
    return new ReferenceData(rd.getSecurity(), rd.getFieldDataMsg(getFudgeContext()));
  }

  /**
   * Creates the cached object.
   *
   * @param refDataResult  the reference data result.
   * @return the cache object, not null
   */
  protected Object createCachedObject(final ReferenceData refDataResult) {
    final CachedReferenceData result = new CachedReferenceData();
    result.setSecurity(refDataResult.getIdentifier());
    result.setFieldDataMsg(getFudgeContext().newMessage(refDataResult.getFieldValues()), getFudgeContext());
    return result;
  }

}
