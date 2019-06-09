/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Parses a bundle XML file into a bundle manager.
 */
public class BundleParser {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleParser.class);

  /** The bundle element tag name. */
  private static final String BUNDLE_ELEMENT = "bundle";
  /** The fragment element tag name. */
  private static final String FRAGMENT_ELEMENT = "fragment";
  /** The ID attribute name. */
  private static final String ID_ATTR = "id";

  /**
   * The URI provider for fragment references.
   */
  private final UriProvider _fragmentUriProvider;
  /**
   * The base path.
   */
  private final String _basePath;
  /**
   * The bundle manager to populate.
   */
  private final BundleManager _bundleManager = new BundleManager();
  /**
   * The cache of elements.
   */
  private final Map<String, Element> _elementsByIdMap = new HashMap<>();

  /**
   * Creates a parser.
   *
   * @param fragmentUriProvider
   *          the URI provider for fragments, not null
   * @param basePath
   *          the base path, not null
   */
  public BundleParser(final UriProvider fragmentUriProvider, final String basePath) {
    ArgumentChecker.notNull(fragmentUriProvider, "fragmentUriProvider");
    ArgumentChecker.notNull(basePath, "basePath");
    _fragmentUriProvider = fragmentUriProvider;
    _basePath = basePath.startsWith("/") ? basePath : "/" + basePath;
  }

  // -------------------------------------------------------------------------
  /**
   * Parses the XML file, returning the bundle manager.
   *
   * @param xmlStream
   *          the XML input stream, not null
   * @return the parsed bundle manager, not null
   */
  public BundleManager parse(final InputStream xmlStream) {
    ArgumentChecker.notNull(xmlStream, "xml inputstream");
    final DocumentBuilder builder = getDocumentBuilder();
    if (builder != null) {
      try {
        final Document document = builder.parse(xmlStream);
        processXMLDocument(document);
      } catch (final SAXException ex) {
        throw new OpenGammaRuntimeException("unable to parse xml file", ex);
      } catch (final IOException ex) {
        throw new OpenGammaRuntimeException("unable to read xml file", ex);
      }
    }
    return _bundleManager;
  }

  private void processXMLDocument(final Document document) {
    buildAllElements(document);
    for (final Element element : _elementsByIdMap.values()) {
      addToManager(element);
    }
  }

  private void addToManager(final Element element) {
    final String idAttr = element.getAttribute(ID_ATTR);
    final Bundle bundle = new Bundle(idAttr);
    final NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        final Element childElement = (Element) node;
        if (childElement.getNodeName().equals(BUNDLE_ELEMENT)) {
          processRefBundle(bundle, childElement);
        }
        if (childElement.getNodeName().equals(FRAGMENT_ELEMENT)) {
          processFragment(bundle, childElement);
        }
      }
    }
    _bundleManager.addBundle(bundle);
  }

  private void processFragment(final Bundle bundle, final Element element) {
    final String fragment = element.getTextContent();
    if (isValidFragment(fragment)) {
      bundle.addChildNode(createBundleFragment(fragment));
    }
  }

  private static boolean isValidFragment(final String fragment) {
    if (StringUtils.isNotBlank(fragment)) {
      return true;
    }
    throw new OpenGammaRuntimeException("invalid fragment value while parsing bundle xml file");
  }

  private BundleNode createBundleFragment(final String fragment) {
    final URI fragmentUri = getFragmentUriProvider().getUri(fragment);
    final String fragmentPath = getBasePath() + fragment;
    return new Fragment(fragmentUri, fragmentPath);
  }

  private void processRefBundle(final Bundle bundle, final Element element) {
    final String idRef = element.getAttribute("idref");
    if (isValidIdRef(idRef)) {
      Bundle refBundle = _bundleManager.getBundle(idRef);
      if (refBundle == null) {
        final Element refElement = _elementsByIdMap.get(idRef);
        // this can cause infinite loop if we have circular reference
        addToManager(refElement);
        refBundle = _bundleManager.getBundle(idRef);
      }
      bundle.addChildNode(refBundle);
    }
  }

  private boolean isValidIdRef(final String idRef) {
    if (StringUtils.isNotBlank(idRef) && idRefExists(idRef)) {
      return true;
    }
    throw new OpenGammaRuntimeException(" invalid idref [" + idRef + "]");
  }

  private boolean idRefExists(final String idRef) {
    return _elementsByIdMap.get(idRef) != null;
  }

  private void buildAllElements(final Document document) {
    final Element rootElement = document.getDocumentElement();
    if (isValidRootElement(rootElement)) {
      rootElement.normalize();
      final NodeList childNodes = rootElement.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        final Node node = childNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          if (isValidBundleElement(element)) {
            final String idAttr = element.getAttribute(ID_ATTR);
            if (_elementsByIdMap.get(idAttr) == null) {
              _elementsByIdMap.put(idAttr, element);
            } else {
              throw new OpenGammaRuntimeException("parsing bundle XML : duplicate id attribute in " + node.getNodeName());
            }
          }
        }
      }
    }
  }

  private static boolean isValidRootElement(final Element rootElement) {
    if (rootElement.getNodeName().equals("uiResourceConfig")) {
      return true;
    }
    throw new OpenGammaRuntimeException("parsing bundle XML : invalid root element " + rootElement.getNodeName());
  }

  private static boolean isValidBundleElement(final Element element) {
    return isBundleElement(element) && hasChildren(element) && hasValidId(element);
  }

  private static boolean hasValidId(final Element element) {
    if (element.hasAttribute(ID_ATTR) && StringUtils.isNotBlank(element.getAttribute(ID_ATTR))) {
      return true;
    }
    throw new OpenGammaRuntimeException("parsing bundle XML : bundle element needs id attribute");
  }

  private static boolean hasChildren(final Element element) {
    if (element.hasChildNodes()) {
      return true;
    }
    throw new OpenGammaRuntimeException("parsing bundle XML : missing children elements in bundle");
  }

  private static boolean isBundleElement(final Element element) {
    if (element.getNodeName().equals(BUNDLE_ELEMENT)) {
      return true;
    }
    throw new OpenGammaRuntimeException("parsing bundle XML : element not a bundle");
  }

  private static DocumentBuilder getDocumentBuilder() {
    DocumentBuilder builder = null;
    final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      LOGGER.warn("Unable to create a DOM parser", e);
    }
    return builder;
  }

  private UriProvider getFragmentUriProvider() {
    return _fragmentUriProvider;
  }

  private String getBasePath() {
    return _basePath;
  }

}
