/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

/**
 * Builder to convert DirectBean to and from Fudge.
 *
 * @param <T> the bean type
 */
public final class DirectBeanFudgeBuilder<T extends Bean> implements FudgeBuilder<T> {

  /**
   * The meta bean for this instance.
   */
  private final MetaBean _metaBean;

  /**
   * Creates a builder from a class, using reflection to find the meta-bean.
   * @param <R> the bean type
   * @param cls  the class to get the builder for, not null
   * @return the bean builder, not null
   */
  public static <R extends Bean> DirectBeanFudgeBuilder<R> of(final Class<R> cls) {
    final MetaBean meta = JodaBeanUtils.metaBean(cls);
    return new DirectBeanFudgeBuilder<>(meta);
  }

  /**
   * Constructor.
   * @param metaBean  the meta-bean, not null
   */
  public DirectBeanFudgeBuilder(final MetaBean metaBean) {
    _metaBean = metaBean;
  }

  //-------------------------------------------------------------------------
  // TODO: FudgeFieldName and Ordinal annotations

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final T bean) {
    try {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (final MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
        if (prop.style().isReadable()) {
          final Object obj = prop.get(bean);
          if (obj instanceof List<?>) {
            final MutableFudgeMsg subMsg = buildMessageCollection(serializer, prop, bean.getClass(), (List<?>) obj);
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else if (obj instanceof Set<?>) {
            final MutableFudgeMsg subMsg = buildMessageCollection(serializer, prop, bean.getClass(), new ArrayList<>((Set<?>) obj));
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else if (obj instanceof Map<?, ?>) {
            final MutableFudgeMsg subMsg = buildMessageMap(serializer, bean.getClass(), prop, (Map<?, ?>) obj);
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else if (obj instanceof Multimap<?, ?>) {
            final MutableFudgeMsg subMsg = buildMessageMultimap(serializer, bean.getClass(), prop, (Multimap<?, ?>) obj);
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else {
            serializer.addToMessageWithClassHeaders(msg, prop.name(), null, obj, prop.propertyType()); // ignores null
          }
        }
      }
      return msg;
    } catch (final RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + _metaBean.beanName(), ex);
    }
  }

  private static MutableFudgeMsg buildMessageCollection(final FudgeSerializer serializer, final MetaProperty<?> prop, final Class<?> beanType,
      final List<?> list) {
    final Class<?> contentType = JodaBeanUtils.collectionType(prop, beanType);
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final Object entry : list) {
      if (entry == null) {
        msg.add(null, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else if (contentType == null) {
        serializer.addToMessage(msg, null, null, entry);
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, null, entry, contentType);
      }
    }
    return msg;
  }

  private static MutableFudgeMsg buildMessageMap(final FudgeSerializer serializer, final Class<?> beanType, final MetaProperty<?> prop,
                                                 final Map<?, ?> map) {
    return buildMessageMapFromEntries(map.entrySet(), serializer, beanType, prop);
  }

  private static MutableFudgeMsg buildMessageMultimap(final FudgeSerializer serializer, final Class<?> beanType, final MetaProperty<?> prop,
                                                      final Multimap<?, ?> multimap) {
    return buildMessageMapFromEntries(multimap.entries(), serializer, beanType, prop);
  }

  private static MutableFudgeMsg buildMessageMapFromEntries(final Collection<? extends Map.Entry<?, ?>> entries,
                                                            final FudgeSerializer serializer,
                                                            final Class<?> beanType,
                                                            final MetaProperty<?> prop) {
    final Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanType);
    final Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanType);
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final Map.Entry<?, ?> entry : entries) {
      if (entry.getKey() == null) {
        msg.add(null, 1, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else if (keyType == null) {
        serializer.addToMessage(msg, null, 1, entry.getKey());
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, 1, entry.getKey(), keyType);
      }
      if (entry.getValue() == null) {
        msg.add(null, 2, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else if (valueType == null) {
        serializer.addToMessage(msg, null, 2, entry.getValue());
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, 2, entry.getValue(), valueType);
      }
    }
    return msg;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    try {
      final BeanBuilder<T> builder = (BeanBuilder<T>) _metaBean.builder();
      for (final MetaProperty<?> mp : _metaBean.metaPropertyIterable()) {
        if (mp.style().isBuildable()) {
          final FudgeField field = msg.getByName(mp.name());
          if (field != null) {
            Object value = null;
            if (List.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectList(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value);
              }
            } else if (SortedSet.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectSet(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, new TreeSet<>());
              }
            } else if (Set.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectSet(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, new LinkedHashSet<>());
              }
            } else if (Map.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMap(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value);
              }
            } else if (ListMultimap.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMultimap(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, ArrayListMultimap.create());
              }
            } else if (SortedSetMultimap.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMultimap(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, TreeMultimap.create());
              }
            } else if (Multimap.class.isAssignableFrom(mp.propertyType())) {
              // In the absence of other information we'll create a hash multimap
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMultimap(deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, HashMultimap.create());
              }
            }
            if (value == null) {
              try {
                value = deserializer.fieldValueToObject(mp.propertyType(), field);
              } catch (final IllegalArgumentException ex) {
                if (!(field.getValue() instanceof String)) {
                  throw ex;
                }
                value = JodaBeanUtils.stringConverter().convertFromString(mp.propertyType(), (String) field.getValue());
              }
            }
            if (value != null || !mp.propertyType().isPrimitive()) {
              builder.set(mp.name(), value);
            }
          }
        }
      }
      return builder.build();
    } catch (final RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to deserialize: " + _metaBean.beanName(), ex);
    }
  }

  private static List<Object> buildObjectList(final FudgeDeserializer deserializer, final MetaProperty<?> prop,
      final Class<?> type, final FudgeMsg msg) {
    final Class<?> contentType = JodaBeanUtils.collectionType(prop, type);
    final List<Object> list = new ArrayList<>();  // should be List<contentType>
    for (final FudgeField field : msg) {
      if (field.getOrdinal() != null && field.getOrdinal() != 1) {
        throw new IllegalArgumentException("Sub-message doesn't contain a list (bad field " + field + ")");
      }
      final boolean abstractOrInterface = contentType == null || contentType.isInterface() || Modifier.isAbstract(contentType.getModifiers());
      final Object obj = abstractOrInterface ? deserializer.fieldValueToObject(field) : deserializer.fieldValueToObject(contentType, field);
      list.add(obj instanceof IndicatorType ? null : obj);
    }
    return list;
  }

  private static Set<Object> buildObjectSet(final FudgeDeserializer deserializer, final MetaProperty<?> prop, final Class<?> type, final FudgeMsg msg,
      final Set<Object> set) {
    final Class<?> contentType = JodaBeanUtils.collectionType(prop, type);
    for (final FudgeField field : msg) {
      if (field.getOrdinal() != null && field.getOrdinal() != 1) {
        throw new IllegalArgumentException("Sub-message doesn't contain a set (bad field " + field + ")");
      }
      final boolean abstractOrInterface = contentType == null || contentType.isInterface() || Modifier.isAbstract(contentType.getModifiers());
      final Object obj = abstractOrInterface ? deserializer.fieldValueToObject(field) : deserializer.fieldValueToObject(contentType, field);
      set.add(obj instanceof IndicatorType ? null : obj);
    }
    return set;
  }

  private static Map<Object, Object> buildObjectMap(final FudgeDeserializer deserializer, final MetaProperty<?> prop,
      final Class<?> type, final FudgeMsg msg) {
    final Class<?> keyType = JodaBeanUtils.mapKeyType(prop, type);
    final boolean keyAbstractOrInterface = keyType == null || keyType.isInterface() || Modifier.isAbstract(keyType.getModifiers());
    final Class<?> valueType = JodaBeanUtils.mapValueType(prop, type);
    final boolean valueAbstractOrInterface = valueType == null || valueType.isInterface() || Modifier.isAbstract(valueType.getModifiers());
    final Map<Object, Object> map = Maps.newHashMap();  // should be Map<keyType,contentType>
    final Queue<Object> keys = new LinkedList<>();
    final Queue<Object> values = new LinkedList<>();
    for (final FudgeField field : msg) {
      if (field.getOrdinal() == 1) {
        Object fieldValue = keyAbstractOrInterface ? deserializer.fieldValueToObject(field) : deserializer.fieldValueToObject(keyType, field);
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (values.isEmpty()) {
          // no values ready, so store the key till next time
          keys.add(fieldValue);
        } else {
          // store key along with next value
          map.put(fieldValue, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        Object fieldValue = valueAbstractOrInterface ? deserializer.fieldValueToObject(field) : deserializer.fieldValueToObject(valueType, field);
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (keys.isEmpty()) {
          // no keys ready, so store the value till next time
          values.add(fieldValue);
        } else {
          // store value along with next key
          map.put(keys.remove(), fieldValue);
        }
      } else {
        throw new IllegalArgumentException("Sub-message doesn't contain a map (bad field " + field + ")");
      }
    }
    return map;
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static Multimap<Object, Object> buildObjectMultimap(final FudgeDeserializer deserializer,
                                                              final MetaProperty<?> prop,
                                                              final Class<?> type,
                                                              final FudgeMsg msg,
                                                              final Multimap multimap) {

    final Class<?> keyType = JodaBeanUtils.mapKeyType(prop, type);
    final boolean keyAbstractOrInterface = keyType == null || keyType.isInterface() || Modifier.isAbstract(keyType.getModifiers());
    final Class<?> valueType = JodaBeanUtils.mapValueType(prop, type);
    final boolean valueAbstractOrInterface = valueType == null || valueType.isInterface() || Modifier.isAbstract(valueType.getModifiers());
    final Queue<Object> keys = new LinkedList<>();
    final Queue<Object> values = new LinkedList<>();
    for (final FudgeField field : msg) {
      if (field.getOrdinal() == 1) {
        Object fieldValue = keyAbstractOrInterface ? deserializer.fieldValueToObject(field) : deserializer.fieldValueToObject(keyType, field);
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (values.isEmpty()) {
          // no values ready, so store the key till next time
          keys.add(fieldValue);
        } else {
          // store key along with next value
          multimap.put(fieldValue, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        Object fieldValue = valueAbstractOrInterface ? deserializer.fieldValueToObject(field) : deserializer.fieldValueToObject(valueType, field);
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (keys.isEmpty()) {
          // no keys ready, so store the value till next time
          values.add(fieldValue);
        } else {
          // store value along with next key
          multimap.put(keys.remove(), fieldValue);
        }
      } else {
        throw new IllegalArgumentException("Sub-message doesn't contain a map (bad field " + field + ")");
      }
    }
    return multimap;
  }

}
