/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;

import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;

import com.opengamma.util.ArgumentChecker;

/**
 * TODO docs clarifying why this exists on top of the standard Joda converters - property-specific behaviour TODO also clarify that you want one instance of
 * this class for converting in each direction.
 */
/* package */ class Converters {

  /**
   * Sentinal value signalling a value cannot be converted. null can't be used to signal conversion failure because null is a valid return value for the
   * conversion operation.
   */
  public static final Object CONVERSION_FAILED = new Object();

  /** Converters keyed by the property whose values they can convert. */
  private final Map<MetaProperty<?>, Converter<?, ?>> _converters;
  /** For converting strings to objects. */
  private final StringConvert _stringConvert;

  /* package */ Converters(final Map<MetaProperty<?>, Converter<?, ?>> converters, final StringConvert stringConvert) {
    // TODO defensive copying and validation of converters
    ArgumentChecker.notNull(converters, "converters");
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _converters = converters;
    _stringConvert = stringConvert;
  }

  /**
   *
   * @param value
   *          The value to convert, possibly null
   * @param property
   *          The property associated with the value (the converter might be converting from a value of the property type or to a value that will be used to set
   *          the property).
   * @param type
   *          The type to convert from or to
   * @return The converted value, possibly null, {@link #CONVERSION_FAILED} if the value couldn't be converted
   */
  @SuppressWarnings("unchecked")
  Object convert(final Object value, final MetaProperty<?> property, final Class<?> type) {
    final Converter<Object, Object> converter = (Converter<Object, Object>) _converters.get(property);
    if (converter != null) {
      return converter.convert(value);
    } else if (value == null) {
      return null;
    } else if (value instanceof String) {
      try {
        final StringConverter<Object> stringConverter = (StringConverter<Object>) _stringConvert.findConverter(type);
        return stringConverter.convertFromString(type, (String) value);
      } catch (final Exception e) {
        // carry on
      }
    } else {
      try {
        final StringConverter<Object> stringConverter = (StringConverter<Object>) _stringConvert.findConverter(type);
        return stringConverter.convertToString(value);
      } catch (final Exception e) {
        // carry on
      }
    }
    return CONVERSION_FAILED;
  }
}

/**
 * @param <F>
 *          The type to convert from
 * @param <T>
 *          The type to convert to
 */
/* package */ interface Converter<F, T> {

  /**
   * Converts an object from one type to another.
   *
   * @param f
   *          The unconverted object, possibly null
   * @return The converted object, possibly null
   */
  T convert(F f);
}
