/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Tag used to generate the HTML output for Javascript.
 */
public class ScriptTag {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTag.class);

  /**
   * The request data.
   */
  private final WebBundlesData _data;

  /**
   * Creates an instance.
   *
   * @param data  the request data, not null
   */
  public ScriptTag(final WebBundlesData data) {
    ArgumentChecker.notNull(data, "data");
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Outputs the HTML for the bundle.
   *
   * @param bundleId  the bundle ID, not null
   * @param inline  whether to inline the script
   * @return the HTML for the bundle, may be null
   */
  public String print(final String bundleId, final boolean inline) {
    ArgumentChecker.notNull(bundleId, "bundleId");
    final Bundle bundle = _data.getBundleManager().getBundle(bundleId);
    if (bundle == null) {
      LOGGER.warn("{} not available ", bundleId);
      return "";
    }
    final DeployMode mode = _data.getMode();
    switch (mode) {
      case DEV:
        return inline ? printDevInline(bundle) : printDevLinked(bundle);
      case PROD:
        return inline ? printProdInline(bundle) : printProdLinked(bundle);
      default:
        LOGGER.warn("Unknown deployment mode type: " + mode);
        return null;
    }
  }

  private String printProdInline(final Bundle bundle) {
    final StringBuilder buf = new StringBuilder();
    buf.append("<script src=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
    buf.append(_data.getCompressor().compressBundle(bundle));
    buf.append("//--><!]]>\n</script>");
    return buf.toString();
  }

  private String printProdLinked(final Bundle bundle) {
    final StringBuilder buf = new StringBuilder();
    buf.append("<script src=\"");
    final WebBundlesUris uris = new WebBundlesUris(_data);
    buf.append(uris.bundle(DeployMode.PROD, bundle.getId()));
    buf.append("?" + BuildData.getBuildStamp());
    buf.append("\"></script>");
    return buf.toString();
  }

  private static String printDevInline(final Bundle bundle) {
    final StringBuilder buf = new StringBuilder();
    buf.append("<script src=\"text/javascript\"><!--//--><![CDATA[//><!--\n");
    buf.append(BundleUtils.readBundleSource(bundle));
    buf.append("//--><!]]>\n</script>");
    return buf.toString();
  }

  private String printDevLinked(Bundle bundle) {
    bundle = _data.getDevBundleManager().getBundle(bundle.getId());  // reload from dev manager
    final StringBuilder buf = new StringBuilder();
    final List<Fragment> allFragment = bundle.getAllFragments();
    for (final Fragment fragment : allFragment) {
      buf.append("<script src=\"");
      buf.append(fragment.getPath());
      buf.append("\"></script>\n");
    }
    return buf.toString();
  }

}
