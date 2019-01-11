/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import static com.opengamma.master.position.Deal.DEAL_CLASSNAME;
import static com.opengamma.master.position.Deal.DEAL_PREFIX;
import static com.opengamma.master.position.Deal.DEAL_TYPE;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Trade;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides helper methods to store a {@link Deal}'s data in a map of strings and reload it into a
 * {@link Deal} instance.  Intended to be used for storing a {@link Deal} in a {@link Trade}'s
 * {@link Trade#getAttributes() attributes}.
 */
public final class DealAttributeEncoder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DealAttributeEncoder.class);

  /**
   * Restricted constructor
   */
  private DealAttributeEncoder() {
  }

  /**
   * Reads attributes represented as key/value pairs into a deal.
   *
   * The type of the deal is given by the value for the key
   * {@link Deal#DEAL_CLASSNAME}. If there is no value for this key, then null
   * is returned. If an instance of this class cannot be loaded from the class
   * loader, then an exception is thrown.
   *
   * Once a
   *
   * @param tradeAttributes
   *          the attributes map in which Deal information is stored, not null
   * @return a deal of the type in the attributes, or null
   */
  public static Deal read(final Map<String, String> tradeAttributes) {
    ArgumentChecker.notNull(tradeAttributes, "tradeAttributes");
    final String dealClass = tradeAttributes.get(DEAL_CLASSNAME);
    Deal deal = null;
    if (dealClass != null) {
      Class<?> cls;
      try {
        cls = DealAttributeEncoder.class.getClassLoader().loadClass(dealClass);
      } catch (final ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Unable to load deal class", ex);
      }
      final MetaBean metaBean = JodaBeanUtils.metaBean(cls);
      deal = (Deal) metaBean.builder().build();
      for (final Map.Entry<String, String> entry : tradeAttributes.entrySet()) {
        final String key = entry.getKey();
        if (key.startsWith(DEAL_PREFIX) && !key.equals(DEAL_CLASSNAME) && !key.equals(DEAL_TYPE)) {
          final String propertyName = StringUtils.substringAfter(key, DEAL_PREFIX);
          if (metaBean.metaPropertyExists(propertyName)) {
            final MetaProperty<?> mp = metaBean.metaProperty(propertyName);
            final String value = entry.getValue();
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Setting property {}({}) with value {}", new Object[]{mp, mp.propertyType(), value});
            }
            mp.setString(deal, value);
          }
        }
      }
    }
    return deal;
  }

  public static Map<String, String> write(final Deal deal) {
    final Map<String, String> attributes = new HashMap<>();
    attributes.put(DEAL_CLASSNAME, deal.getClass().getName());
    for (final MetaProperty<?> mp : deal.metaBean().metaPropertyIterable()) {
      final Object value = mp.get(deal);
      if (value != null) {
        @SuppressWarnings("unchecked")
        final
        StringConverter<Object> stringConverter = (StringConverter<Object>) JodaBeanUtils.stringConverter().findConverter(mp.propertyType());
        attributes.put(DEAL_PREFIX + mp.name(), stringConverter.convertToString(value));
      }
    }
    return attributes;
  }

}
