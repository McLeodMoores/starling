/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.types.SecondaryFieldTypeBase;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.fudgemsg.wire.FudgeSize;
import org.fudgemsg.wire.json.FudgeJSONSettings;
import org.fudgemsg.wire.types.FudgeWireType;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.collect.Lists;

/**
 * A Fudge writer that produces JSON.
 * <p>
 * This writer writes a Fudge message as JSON.
 * This can be used for JSON output, or can be used to assist in developing/debugging
 * a streaming serializer without having to inspect the binary output.
 * <p>
 * Please refer to <a href="http://wiki.fudgemsg.org/display/FDG/JSON+Fudge+Messages">JSON Fudge Messages</a>
 * for details on the representation.
 */
public class FudgeMsgJSONWriter implements Flushable, Closeable {

  private static final String BLANK_FIELD_NAME = "";
  /**
   * The taxonomy identifier to use for any messages that are passed without envelopes.
   */
  private static final short DEFAULT_TAXONOMY_ID = 0;
  /**
   * The schema version to add to the envelope header for any messages that are passed without envelopes.
   */
  private static final int DEFAULT_MESSAGE_VERSION = 0;
  /**
   * The processing directive flags to add to the envelope header for any messages that are passed without envelopes.
   */
  private static final int DEFAULT_MESSAGE_PROCESSING_DIRECTIVES = 0;

  /**
   * The fudge context
   */
  private final FudgeContext _fudgeContext;

  /**
   * The JSON settings.
   */
  private final FudgeJSONSettings _settings;
  /**
   * The underlying writer.
   */
  private final Writer _underlyingWriter;
  /**
   * The JSON writer.
   */
  private JSONWriter _writer;

  /**
   * Creates a new instance for writing a Fudge stream to a JSON writer.
   *
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   */
  public FudgeMsgJSONWriter(final FudgeContext fudgeContext, final Writer writer) {
    this(fudgeContext, writer, new FudgeJSONSettings());
  }

  /**
   * Creates a new stream writer for writing Fudge messages in JSON format to a given
   * {@link Writer}.
   *
   * @param fudgeContext  the Fudge context, not null
   * @param writer  the underlying writer, not null
   * @param settings  the JSON settings, not null
   */
  public FudgeMsgJSONWriter(final FudgeContext fudgeContext, final Writer writer, final FudgeJSONSettings settings) {
    if (fudgeContext == null) {
      throw new NullPointerException("FudgeContext must not be null");
    }
    if (writer == null) {
      throw new NullPointerException("Writer must not be null");
    }
    if (settings == null) {
      throw new NullPointerException("FudgeJSONSettings must not be null");
    }
    _settings = settings;
    _underlyingWriter = writer;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the taxonomy.
   * @return the taxonomy
   */
  private FudgeTaxonomy getTaxonomy(final int taxonomyId) {
    if (taxonomyId != DEFAULT_TAXONOMY_ID) {
      return getFudgeContext().getTaxonomyResolver().resolveTaxonomy((short) taxonomyId);
    }
    return null;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JSON settings.
   *
   * @return the JSON settings, not null
   */
  public FudgeJSONSettings getSettings() {
    return _settings;
  }

  /**
   * Gets the JSON writer being used, allocating one if necessary.
   *
   * @return the writer, not null
   */
  private JSONWriter getWriter() {
    if (_writer == null) {
      _writer = new JSONWriter(getUnderlying());
    }
    return _writer;
  }

  /**
   * Discards the JSON writer.
   * The implementation only allows a single use so we must drop the instance
   * after each message envelope completes.
   */
  private void clearWriter() {
    _writer = null;
  }

  /**
   * Gets the underlying {@link Writer} that is wrapped by {@link JSONWriter} instances for messages.
   *
   * @return the writer, not null
   */
  public Writer getUnderlying() {
    return _underlyingWriter;
  }


  //-------------------------------------------------------------------------
  /**
   * Writes a message with the given taxonomy, schema version and processing directive flags.
   *
   * @param message  message to write
   * @param taxonomyId  identifier of the taxonomy to use. If the taxonomy is recognized by the {@link FudgeContext} it will be used to reduce field names to ordinals where possible.
   * @param version  schema version
   * @param processingDirectives  processing directive flags
   */
  public void writeMessage(final FudgeMsg message, final int taxonomyId, final int version, final int processingDirectives) {
    writeMessageEnvelope(new FudgeMsgEnvelope(message, version, processingDirectives), taxonomyId);
  }

  /**
   * Writes a message with the given taxonomy. Default schema version and processing directive flags are used.
   *
   * @param message  message to write
   * @param taxonomyId  the identifier of the taxonomy to use, if the taxonomy is recognized
   *   by the {@link FudgeContext} it will be used to reduce field names to ordinals where possible.
   */
  public void writeMessage(final FudgeMsg message, final int taxonomyId) {
    writeMessage(message, taxonomyId, DEFAULT_MESSAGE_VERSION, DEFAULT_MESSAGE_PROCESSING_DIRECTIVES);
  }

  /**
   * Writes a message. Default taxonomy, schema version and processing directive flags are used.
   *
   * @param message  message to write
   * @throws NullPointerException if the default taxonomy has not been specified
   */
  public void writeMessage(final FudgeMsg message) {
    writeMessage(message, DEFAULT_TAXONOMY_ID);
  }

  /**
   * Writes a message envelope with the given taxonomy.
   *
   * @param envelope message envelope to write
   * @param taxonomyId  the identifier of the taxonomy to use, if the taxonomy is recognized
   *   by the {@link FudgeContext} it will be used to reduce field names to ordinals where possible.
   */
  public void writeMessageEnvelope(final FudgeMsgEnvelope envelope, final int taxonomyId) {
    if (envelope == null) {
      return;
    }
    final int messageSize = FudgeSize.calculateMessageEnvelopeSize(getTaxonomy(taxonomyId), envelope);
    writeEnvelopeHeader(envelope.getProcessingDirectives(), envelope.getVersion(), messageSize, taxonomyId);
    writeData(envelope.getMessage(), taxonomyId);
    writeMeta(envelope.getMessage(), taxonomyId);
    envelopeComplete();
  }

  private void writeMeta(final FudgeMsg message, final int taxonomyId) {
    try {
      getWriter().key("meta");
      getWriter().object();
    } catch (final JSONException e) {
      wrapException("start of data", e);
    }
    writeFields(message, taxonomyId, true);
    try {
      getWriter().endObject();
    } catch (final JSONException e) {
      wrapException("end of data", e);
    }
  }

  private void writeData(final FudgeMsg message, final int taxonomyId) {
    writeDataStart();
    writeFields(message, taxonomyId, false);
    writeDataEnd();
  }

  private void writeDataEnd() {
    try {
      getWriter().endObject();
    } catch (final JSONException e) {
      wrapException("end of data", e);
    }
  }

  private void writeDataStart() {
    try {
      getWriter().key("data");
      getWriter().object();
    } catch (final JSONException e) {
      wrapException("start of data", e);
    }
  }

  private void writeFields(final Iterable<FudgeField> fudgeMsg, final int taxonomyId, final boolean meta) {
    final Map<String, List<FudgeField>> fieldName2Fields = new LinkedHashMap<>();
    for (final FudgeField fudgeField : fudgeMsg) {
      if (fudgeField.getName() != null) {
        final List<FudgeField> fields = getFieldList(fieldName2Fields, fudgeField.getName());
        fields.add(fudgeField);
      } else if (fudgeField.getOrdinal() != null) {
        final List<FudgeField> fields = getFieldList(fieldName2Fields, fudgeField.getOrdinal().toString());
        fields.add(fudgeField);
      } else {
        final List<FudgeField> fields = getFieldList(fieldName2Fields, BLANK_FIELD_NAME);
        fields.add(fudgeField);
      }
    }

    for (final Entry<String, List<FudgeField>> entry : fieldName2Fields.entrySet()) {
      final List<FudgeField> fields = entry.getValue();
      if (!fields.isEmpty()) {
        if (fields.size() == 1) {
          final FudgeField fudgeField = fields.get(0);
          writeField(fudgeField.getName(), fudgeField.getOrdinal(), fudgeField.getType(), fudgeField.getValue(), taxonomyId, meta);
        } else {
          writeRepeatedFields(fields, taxonomyId, meta);
        }
      }
    }
  }

  private void writeRepeatedFields(final List<FudgeField> fields, final int taxonomyId, final boolean meta) {
    final FudgeField firstField = fields.iterator().next();
    try {
      final String key = fudgeFieldStart(firstField.getOrdinal(), firstField.getName(), taxonomyId);
      if (key != null) {
        getWriter().array();
        for (final FudgeField fudgeField : fields) {
          if (fudgeField.getType().getTypeId() == FudgeWireType.SUB_MESSAGE_TYPE_ID) {
            fudgeSubMessageStart();
            writeFields((FudgeMsg) fudgeField.getValue(), taxonomyId, meta);
            fudgeSubMessageEnd();
          } else {
            fudgeFieldValue(fudgeField.getType(), fudgeField.getValue(), meta);
          }
        }
        getWriter().endArray();
      }
    } catch (final JSONException e) {
      wrapException("writing repeated fields", e);
    }
  }

  private void writeField(final String name, final Integer ordinal, final FudgeFieldType type, final Object fieldValue, final int taxonomyId, final boolean meta) {
    if (fudgeFieldStart(ordinal, name, taxonomyId) != null) {
      if (type.getTypeId() == FudgeWireType.SUB_MESSAGE_TYPE_ID) {
        fudgeSubMessageStart();
        final FudgeMsg subMsg = (FudgeMsg) fieldValue;
        writeFields(subMsg, taxonomyId, meta);
        fudgeSubMessageEnd();
      } else {
        fudgeFieldValue(type, fieldValue, meta);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void fudgeFieldValue(final FudgeFieldType type, Object fieldValue, final boolean meta) {
    try {
      if (meta) {
        final String typeIdToString = getSettings().fudgeTypeIdToString(type.getTypeId());
        getWriter().value(typeIdToString);
      } else {
        if (type instanceof SecondaryFieldTypeBase<?, ?, ?>) {
          fieldValue = ((SecondaryFieldTypeBase<Object, Object, Object>) type).secondaryToPrimary(fieldValue);
        }
        switch (type.getTypeId()) {
          case FudgeWireType.INDICATOR_TYPE_ID:
            getWriter().value(null);
            break;
          case FudgeWireType.BYTE_ARRAY_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_4_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_8_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_16_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_20_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_32_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_64_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_128_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_256_TYPE_ID:
          case FudgeWireType.BYTE_ARRAY_512_TYPE_ID:
            writeArray((byte[]) fieldValue);
            break;
          case FudgeWireType.SHORT_ARRAY_TYPE_ID:
            writeArray((short[]) fieldValue);
            break;
          case FudgeWireType.INT_ARRAY_TYPE_ID:
            writeArray((int[]) fieldValue);
            break;
          case FudgeWireType.LONG_ARRAY_TYPE_ID:
            writeArray((long[]) fieldValue);
            break;
          case FudgeWireType.FLOAT_ARRAY_TYPE_ID:
            writeArray((float[]) fieldValue);
            break;
          case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
            writeArray((double[]) fieldValue);
            break;
          default:
            getWriter().value(fieldValue);
            break;
        }
      }
    } catch (final JSONException e) {
      wrapException("field value", e);
    }
  }

  private void writeArray(final double[] data) throws JSONException {
    getWriter().array();
    for (final double element : data) {
      getWriter().value(element);
    }
    getWriter().endArray();
  }

  private void writeArray(final float[] data) throws JSONException {
    getWriter().array();
    for (final float element : data) {
      getWriter().value(element);
    }
    getWriter().endArray();
  }

  private void writeArray(final long[] data) throws JSONException {
    getWriter().array();
    for (final long element : data) {
      getWriter().value(element);
    }
    getWriter().endArray();
  }

  private void writeArray(final int[] data) throws JSONException {
    getWriter().array();
    for (final int element : data) {
      getWriter().value(element);
    }
    getWriter().endArray();

  }

  private void writeArray(final short[] data) throws JSONException {
    getWriter().array();
    for (final short element : data) {
      getWriter().value(element);
    }
    getWriter().endArray();
  }

  private void writeArray(final byte[] data) throws JSONException {
    getWriter().array();
    for (final byte element : data) {
      getWriter().value(element);
    }
    getWriter().endArray();
  }

  /**
   * Ends the JSON sub-object.
   */
  private void fudgeSubMessageEnd() {
    try {
      getWriter().endObject();
    } catch (final JSONException e) {
      wrapException("end of submessage", e);
    }
  }

  /**
   * Starts a sub-object within the JSON object.
   */
  private void fudgeSubMessageStart() {
    try {
      getWriter().object();
    } catch (final JSONException e) {
      wrapException("start of submessage", e);
    }
  }

  private String fudgeFieldStart(final Integer ordinal, final String name, final int taxonomyId) {
    String fieldName = null;
    try {
      if (getSettings().getPreserveFieldNames()) {
        if (name != null) {
          getWriter().key(name);
          fieldName = name;
        } else if (ordinal != null) {
          fieldName = getFieldNameByOrdinal(ordinal, taxonomyId);
          getWriter().key(fieldName);
        } else {
          getWriter().key(BLANK_FIELD_NAME);
          fieldName = BLANK_FIELD_NAME;
        }
      } else {
        if (ordinal != null) {
          getWriter().key(ordinal.toString());
          fieldName = ordinal.toString();
        } else if (name != null) {
          getWriter().key(name);
          fieldName = name;
        } else {
          getWriter().key(BLANK_FIELD_NAME);
          fieldName = BLANK_FIELD_NAME;
        }
      }
    } catch (final JSONException e) {
      wrapException("start of field", e);
    }
    return fieldName;
  }

  private String getFieldNameByOrdinal(final Integer ordinal, final int taxonomyId) {
    String result = ordinal.toString();
    final FudgeTaxonomy taxonomy = getTaxonomy(taxonomyId);
    if (taxonomy != null) {
      final String fieldName = taxonomy.getFieldName(ordinal);
      if (fieldName != null) {
        result = fieldName;
      }
    }
    return result;
  }

  private List<FudgeField> getFieldList(final Map<String, List<FudgeField>> fieldName2Fields, final String fieldName) {
    List<FudgeField> fields = fieldName2Fields.get(fieldName);
    if (fields == null) {
      fields = Lists.newArrayList();
      fieldName2Fields.put(fieldName, fields);
    }
    return fields;
  }

  private void envelopeComplete() {
    fudgeEnvelopeEnd();
  }

  /**
   * Ends the JSON object.
   */
  private void fudgeEnvelopeEnd() {
    try {
      getWriter().endObject();
      clearWriter();
    } catch (final JSONException e) {
      wrapException("end of message", e);
    }
  }

  private void writeEnvelopeHeader(final int processingDirectives, final int version, final int messageSize, final int taxonomyId) {
    fudgeEnvelopeStart(processingDirectives, version, taxonomyId);
  }

  /**
   * Writes a message envelope using the default taxonomy.
   *
   * @param envelope message envelope to write
   * @throws NullPointerException if the default taxonomy has not been specified
   */
  public void writeMessageEnvelope(final FudgeMsgEnvelope envelope) {
    writeMessageEnvelope(envelope, DEFAULT_TAXONOMY_ID);
  }

  //-------------------------------------------------------------------------
  /**
   * Wraps a JSON exception (which may in turn wrap {@link IOException} into
   * either a {@link FudgeRuntimeException} or {@link FudgeRuntimeIOException}.
   *
   * @param message message describing the current operation
   * @param e the originating exception
   */
  private static void wrapException(final String message, final JSONException e) {
    final String newMessage = "Error writing " + message + " to JSON";
    if (e.getCause() instanceof IOException) {
      throw new FudgeRuntimeIOException(newMessage, (IOException) e.getCause());
    }
    throw new FudgeRuntimeException(newMessage, e);
  }

  /**
   * Flushes the underlying {@link Writer}.
   */
  @Override
  public void flush() {
    if (getUnderlying() != null) {
      try {
        getUnderlying().flush();
      } catch (final IOException e) {
        throw new FudgeRuntimeIOException(e);
      }
    }
  }

  /**
   * Flushes and closes the underlying {@link Writer}.
   */
  @Override
  public void close() {
    flush();
    if (getUnderlying() != null) {
      try {
        getUnderlying().close();
      } catch (final IOException ex) {
        throw new FudgeRuntimeIOException(ex);
      }
    }
  }

  /**
   * Begins a JSON object with the processing directives, schema and taxonomy.
   * @param taxonomyId
   */
  private void fudgeEnvelopeStart(final int processingDirectives, final int schemaVersion, final int taxonomyId) {
    try {
      getWriter().object();
      if (processingDirectives != 0 && getSettings().getProcessingDirectivesField() != null) {
        getWriter().key(getSettings().getProcessingDirectivesField()).value(processingDirectives);
      }
      if (schemaVersion != 0 && getSettings().getSchemaVersionField() != null) {
        getWriter().key(getSettings().getSchemaVersionField()).value(schemaVersion);
      }
      if (taxonomyId != DEFAULT_TAXONOMY_ID && getSettings().getTaxonomyField() != null) {
        getWriter().key(getSettings().getTaxonomyField()).value(taxonomyId);
      }
    } catch (final JSONException e) {
      wrapException("start of message", e);
    }
  }

}
