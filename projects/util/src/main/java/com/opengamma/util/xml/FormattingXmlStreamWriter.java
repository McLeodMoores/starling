/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.xml;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.XmlStreamWriter;

import com.google.common.base.Throwables;

import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;

/**
 * Provides utils for when working with XML.
 */
public final class FormattingXmlStreamWriter implements XMLStreamWriter {

  private final StreamWriterToReceiver _delegate;
  private final Flushable _flushable;


  public FormattingXmlStreamWriter(final StreamWriterToReceiver delegate, final Flushable flushable) {
    _delegate = delegate;
    _flushable = flushable;
  }

  /**
   *
   */
  public static class FormattingXMLStreamWriterBuilder {

    private final Serializer _serializer;
    private final Flushable _flushable;

    private boolean _indent;
    private Integer _lineLength;

    FormattingXMLStreamWriterBuilder(final Serializer serializer, final Flushable flushable) {
      _serializer = serializer;
      _flushable = flushable;
    }

    public FormattingXMLStreamWriterBuilder indent(final boolean indent) {
      this._indent = indent;
      return this;
    }

    public FormattingXMLStreamWriterBuilder lineLength(final int lineLength) {
      this._lineLength = lineLength;
      return this;
    }

    public FormattingXmlStreamWriter build() {
      if (_indent) {
        _serializer.setOutputProperty(Property.INDENT, "yes");
      }
      if (_lineLength != null) {
        _serializer.setOutputProperty(Property.SAXON_LINE_LENGTH, _lineLength.toString());
      }
      StreamWriterToReceiver xmlStreamWriter;
      try {
        xmlStreamWriter = _serializer.getXMLStreamWriter();
      } catch (final SaxonApiException ex) {
        throw Throwables.propagate(ex);
      }
      return new FormattingXmlStreamWriter(xmlStreamWriter, _flushable);

    }

  }

  /**
   * Create a new {@link XmlStreamWriter} builder with a {@link Writer}.
   * @param writer the writer to use
   * @return a builder
   */
  public static FormattingXMLStreamWriterBuilder builder(final Writer writer) {
    final Serializer serializer = new Processor(false).newSerializer(writer);
    return new FormattingXMLStreamWriterBuilder(serializer, writer);
  }

  /**
   * Create a new {@link XmlStreamWriter} builder with an {@link OutputStream}.
   * @param os the output stream to use
   * @return a builder
   */
  public static FormattingXMLStreamWriterBuilder formattingStreamWriterBuilder(final OutputStream os) {
    final Serializer serializer = new Processor(false).newSerializer(os);
    return new FormattingXMLStreamWriterBuilder(serializer, os);
  }


  @Override
  public void close() throws XMLStreamException {
    _delegate.close();
  }

  @Override
  public void flush() throws XMLStreamException {
    //saxon XMLStreamWriter doesn't call through to the
    //underlying writer/output stream in its flush
    //implementation, so do it here.
    try {
      _flushable.flush();
    } catch (final IOException ex) {
      throw new XMLStreamException(ex);
    }
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return _delegate.getNamespaceContext();
  }

  @Override
  public String getPrefix(final String uri) throws XMLStreamException {
    return _delegate.getPrefix(uri);
  }

  @Override
  public Object getProperty(final String name) throws IllegalArgumentException {
    return _delegate.getProperty(name);
  }

  @Override
  public void setDefaultNamespace(final String uri) throws XMLStreamException {
    _delegate.setDefaultNamespace(uri);
  }

  @Override
  public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
    _delegate.setNamespaceContext(context);
  }

  @Override
  public void setPrefix(final String prefix, final String uri) throws XMLStreamException {
    _delegate.setPrefix(prefix, uri);
  }

  @Override
  public void writeAttribute(final String localName, final String value) throws XMLStreamException {
    _delegate.writeAttribute(localName, value);
  }

  @Override
  public void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException {
    _delegate.writeAttribute(namespaceURI, localName, value);
  }

  @Override
  public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) throws XMLStreamException {
    _delegate.writeAttribute(prefix, namespaceURI, localName, value);
  }

  @Override
  public void writeCData(final String data) throws XMLStreamException {
    _delegate.writeCData(data);
  }

  @Override
  public void writeCharacters(final String text) throws XMLStreamException {
    _delegate.writeCharacters(text);
  }

  @Override
  public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
    _delegate.writeCharacters(text, start, len);
  }

  @Override
  public void writeComment(final String data) throws XMLStreamException {
    _delegate.writeComment(data);
  }

  @Override
  public void writeDTD(final String dtd) throws XMLStreamException {
    _delegate.writeDTD(dtd);
  }

  @Override
  public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
    _delegate.writeDefaultNamespace(namespaceURI);
  }

  @Override
  public void writeEmptyElement(final String localName) throws XMLStreamException {
    _delegate.writeEmptyElement(localName);
  }

  @Override
  public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
    _delegate.writeEmptyElement(namespaceURI, localName);
  }

  @Override
  public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
    _delegate.writeEmptyElement(prefix, localName, namespaceURI);
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    _delegate.writeEndDocument();
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    _delegate.writeEndElement();
  }

  @Override
  public void writeEntityRef(final String name) throws XMLStreamException {
    _delegate.writeEntityRef(name);
  }

  @Override
  public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
    _delegate.writeNamespace(prefix, namespaceURI);
  }

  @Override
  public void writeProcessingInstruction(final String target) throws XMLStreamException {
    _delegate.writeProcessingInstruction(target);
  }

  @Override
  public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
    _delegate.writeProcessingInstruction(target, data);
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    _delegate.writeStartDocument();
  }

  @Override
  public void writeStartDocument(final String version) throws XMLStreamException {
    _delegate.writeStartDocument(version);
  }

  @Override
  public void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
    _delegate.writeStartDocument(encoding, version);
  }

  @Override
  public void writeStartElement(final String localName) throws XMLStreamException {
    _delegate.writeStartElement(localName);
  }

  @Override
  public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
    _delegate.writeStartElement(namespaceURI, localName);
  }

  @Override
  public void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
    _delegate.writeStartElement(prefix, localName, namespaceURI);
  }


}
