/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.io.CharArrayReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.json.FudgeMsgJSONReader;

/**
 * Abstract base class for RESTful resources intended for websites.
 * <p>
 * Websites and web-services are related but different RESTful elements.
 * This is because a website needs to bend the RESTful rules in order to be usable.
 */
public abstract class AbstractWebResource {
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebResource.class);

  private static final int INDENTATION_SIZE = 4;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * Creates the resource, used by the root resource.
   */
  protected AbstractWebResource() {
  }

  //-------------------------------------------------------------------------
  /**
   * Builds the paging request.
   * <p>
   * This method is lenient, applying sensible default values.
   *
   * @param pgIdx  the paging first-item index, null if not input
   * @param pgNum  the paging page, null if not input
   * @param pgSze  the paging size, null if not input
   * @return the paging request, not null
   */
  protected PagingRequest buildPagingRequest(final Integer pgIdx, final Integer pgNum, final Integer pgSze) {
    final int size = pgSze != null ? pgSze : PagingRequest.DEFAULT_PAGING_SIZE;
    if (pgIdx != null) {
      return PagingRequest.ofIndex(pgIdx, size);
    } else if (pgNum != null) {
      return PagingRequest.ofPage(pgNum, size);
    } else {
      return PagingRequest.ofPage(1, size);
    }
  }

  /**
   * Builds the sort order.
   * <p>
   * This method is lenient, returning the default in case of error.
   *
   * @param <T>  the sort order type
   * @param order  the sort order, null or empty returns default
   * @param defaultOrder  the default order, not null
   * @return the sort order, not null
   */
  protected <T extends Enum<T>> T buildSortOrder(final String order, final T defaultOrder) {
    if (StringUtils.isEmpty(order)) {
      return defaultOrder;
    }
    String orderStr = order.toUpperCase(Locale.ENGLISH);
    if (orderStr.endsWith(" ASC")) {
      orderStr = StringUtils.replace(orderStr, " ASC", "_ASC");
    } else if (orderStr.endsWith(" DESC")) {
      orderStr = StringUtils.replace(orderStr, " DESC", "_DESC");
    } else if (!orderStr.endsWith("_ASC") && !orderStr.endsWith("_DESC")) {
      orderStr = orderStr + "_ASC";
    }
    try {
      final Class<T> cls = defaultOrder.getDeclaringClass();
      return Enum.valueOf(cls, orderStr);
    } catch (final IllegalArgumentException ex) {
      return defaultOrder;
    }
  }


  /**
   * Utility method to convert XML to configuration object.
   *
   * @param <T> the type to parse to
   * @param xml  the configuration xml, not null
   * @param type  the type to parse to, not null
   * @return the configuration object
   */
  @SuppressWarnings("unchecked")
  protected <T> T parseXML(final String xml, final Class<T> type) {
    if (xml.contains("<fudgeEnvelope")) {
      return (T) parseXML(xml);
    }
    return JodaBeanSerialization.deserializer().xmlReader().read(xml, type);
  }

  /**
   * Utility method to convert XML to configuration object
   * @param xml the configuration xml
   * @return the configuration object
   */
  protected Object parseXML(final String xml) {
    final CharArrayReader car = new CharArrayReader(xml.toCharArray());
    @SuppressWarnings("resource")
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeXMLStreamReader(getFudgeContext(), car));
    final FudgeMsg message = fmr.nextMessage();
    return getFudgeContext().fromFudgeMsg(message);
  }

  protected String createBeanXML(final Object obj) {
    if (obj instanceof Bean) {
      try {
        // NOTE jim 8-Jan-2014 -- changed last param from false to true so bean type is set.  Not necessary for UI, but enables easier parsing if cut and pasted elsewhere.
        return JodaBeanSerialization.serializer(true).xmlWriter().write((Bean) obj, true);
      } catch (final RuntimeException ex) {
        LOGGER.warn("Error serialising bean to XML with JodaBean serializer", ex);
        return createXML(obj);
      }
    }
    return createXML(obj);
  }

  protected String createXML(final Object obj) {
    // get xml and pretty print it
    final FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(obj);
    LOGGER.debug("{} converted to fudge {}", obj, msg);
    final StringWriter buf = new StringWriter(1024);
    @SuppressWarnings("resource")
    final
    FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeXMLStreamWriter(getFudgeContext(), buf));
    writer.writeMessageEnvelope(msg);
    LOGGER.debug("{} converted to xmk {}", obj, buf.toString());
    try {
      return prettyXML(buf.toString(), INDENTATION_SIZE);
    } catch (final Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected String prettyXML(final String input, final int indent) throws TransformerException {
    final Source xmlInput = new StreamSource(new StringReader(input));
    final StreamResult xmlOutput = new StreamResult(new StringWriter());
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    try {
      transformerFactory.setAttribute("indent-number", indent);
    } catch (final IllegalArgumentException e) {
      //ignore
    }
    final Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(xmlInput, xmlOutput);
    return xmlOutput.getWriter().toString();
  }

  /**
   * Converts JSON to configuration object
   *
   * @param json the config document in JSON
   * @return the configuration object
   */
  protected Object parseJSON(final String json) {
    LOGGER.debug("converting JSON to java: " + json);
    final FudgeMsgJSONReader fudgeJSONReader = new FudgeMsgJSONReader(getFudgeContext(), new StringReader(json));

    final FudgeMsg fudgeMsg = fudgeJSONReader.readMessage();
    LOGGER.debug("converted FudgeMsg: " + fudgeMsg);

    return new FudgeDeserializer(getFudgeContext()).fudgeMsgToObject(fudgeMsg);

  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
