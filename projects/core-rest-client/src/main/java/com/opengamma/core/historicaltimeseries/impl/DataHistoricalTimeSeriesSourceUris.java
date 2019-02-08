/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * RESTful URIs for time-series.
 */
public class DataHistoricalTimeSeriesSourceUris {

  /**
   * Builds a URI of the form <code>{path}/hts/{id}</code>. If the unique id is
   * versioned, a query of the form <code>version={versionString}</code> is
   * added to the URI.
   *
   * @param baseUri
   *          the base URI, not null
   * @param uniqueId
   *          the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("hts/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI of the form <code>{path}/htsMeta/externalIdBundle/{id}</code>.
   * If the unique id is versioned, a query of the for
   * <code>version={versionString}</code> is added to the URI.
   *
   * @param baseUri
   *          the base URI, not null
   * @param uniqueId
   *          the unique id, not null
   * @return the URI, not null
   */
  public static URI uriExternalIdBundleGet(final URI baseUri, final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsMeta/externalIdBundle/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI of the form <code>{path}/hts/{id}</code>. The following fields
   * are added to the query:
   * <ul>
   * <li><code>version={versionString}</code> if the unique id is versioned</li>
   * <li><code>start={start}&includeStart={includeStart}</code> if the start
   * date is not null</li>
   * <li><code>end={end}&includeEnd={includeEnd}</code> if the end data is not
   * null</li>
   * <li><code>maxPoints={maxPoints}</code> if the max points is not null</li>
   * </ul>
   *
   * @param baseUri
   *          the base URI, not null
   * @param uniqueId
   *          the identifier, not null
   * @param start
   *          the time series start date
   * @param includeStart
   *          true to include the start date
   * @param end
   *          the time series end date
   * @param includeEnd
   *          true to include the end date
   * @param maxPoints
   *          the maximum number of points to return
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final UniqueId uniqueId, final LocalDate start, final boolean includeStart, final LocalDate end,
      final boolean includeEnd, final Integer maxPoints) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("hts/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Workaround for {@link UriBuilder#queryParam} that will not escape strings that contain valid escaped sequences. For example, "%3FFoo" will be left as-is since "%3F" is a valid escape whereas
   * "%3GFoo" will be escaped to "%253GFoo". If the string contains a "%" then we will escape it in advance and the builder will leave it alone. Otherwise we'll let the builder deal with the string.
   *
   * @param bundle the identifiers to convert
   * @return the array of, possibly encoded, identifier strings
   */
  private static Object[] identifiers(final ExternalIdBundle bundle) {
    final List<String> identifiers = bundle.toStringList();
    final String[] array = new String[identifiers.size()];
    identifiers.toArray(array);
    try {
      for (int i = 0; i < array.length; i++) {
        if (array[i].indexOf('%') >= 0) {
          array[i] = URLEncoder.encode(array[i], "UTF-8").replace('+', ' ');
        }
      }
    } catch (final UnsupportedEncodingException e) {  // CSIGNORE
      throw new OpenGammaRuntimeException("Caught", e);
    }
    return array;
  }

  /**
   * Builds a URI of the form <code>{path}/htsSearches/single</code>. The
   * following fields are added to the query:
   * <ul>
   * <li><code>id={identifierBundle}</code></li>
   * <li><code>dataSource={dataSource}</code></li> if the data source is not
   * null</li>
   * <li><code>dataProvider={dataProvider}</code></li> if the data provider is
   * not null</li>
   * <li><code>dataField={dataField}</code></li> if the data field is not
   * null</li>
   * <li><code>start={start}&includeStart={includeStart}</code> if the start
   * date is not null</li>
   * <li><code>end={end}&includeEnd={includeEnd}</code> if the end data is not
   * null</li>
   * <li><code>maxPoints={maxPoints}</code> if the max points is not null</li>
   * </ul>
   *
   * @param baseUri
   *          the base URI, not null
   * @param identifierBundle
   *          the identifiers, not null
   * @param dataSource
   *          the data source
   * @param dataProvider
   *          the data provider
   * @param dataField
   *          the data field
   * @param start
   *          the time series start date
   * @param includeStart
   *          true to include the start date
   * @param end
   *          the time series end date
   * @param includeEnd
   *          true to include the end date
   * @param maxPoints
   *          the maximum number of points to return
   * @return the URI, not null
   */
  public static URI uriSearchSingle(
      final URI baseUri, final ExternalIdBundle identifierBundle, final String dataSource, final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final Integer maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/single");
    bld.queryParam("id", identifiers(identifierBundle));
    if (dataSource != null) {
      bld.queryParam("dataSource", dataSource);
    }
    if (dataProvider != null) {
      bld.queryParam("dataProvider", dataProvider);
    }
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  /**
   * Builds a URI of the form <code>{path}/htsSearches/single</code>. The
   * following fields are added to the query:
   * <ul>
   * <li><code>id={identifierBundle}</code></li>
   * <li><code>idValidityDate={identifierValidityDate}</code> if the validity
   * date is not null, otherwise <code>idValidityDate=ALL</code></li>
   * <li><code>dataSource={dataSource}</code></li> if the data source is not
   * null</li>
   * <li><code>dataProvider={dataProvider}</code></li> if the data provider is
   * not null</li>
   * <li><code>dataField={dataField}</code></li> if the data field is not
   * null</li>
   * <li><code>start={start}&includeStart={includeStart}</code> if the start
   * date is not null</li>
   * <li><code>end={end}&includeEnd={includeEnd}</code> if the end data is not
   * null</li>
   * <li><code>maxPoints={maxPoints}</code> if the max points is not null</li>
   * </ul>
   *
   * @param baseUri
   *          the base URI, not null
   * @param identifierBundle
   *          the identifiers, not null
   * @param identifierValidityDate
   *          the validity date of the identifiers
   * @param dataSource
   *          the data source
   * @param dataProvider
   *          the data provider
   * @param dataField
   *          the data field
   * @param start
   *          the time series start date
   * @param includeStart
   *          true to include the start date
   * @param end
   *          the time series end date
   * @param includeEnd
   *          true to include the end date
   * @param maxPoints
   *          the maximum number of points to return
   * @return the URI, not null
   */
  public static URI uriSearchSingle(
      final URI baseUri, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final Integer maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/single");
    bld.queryParam("id", identifiers(identifierBundle));
    bld.queryParam("idValidityDate", identifierValidityDate != null ? identifierValidityDate : "ALL");
    if (dataSource != null) {
      bld.queryParam("dataSource", dataSource);
    }
    if (dataProvider != null) {
      bld.queryParam("dataProvider", dataProvider);
    }
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchResolve(
      final URI baseUri, final ExternalIdBundle identifierBundle, final String dataField, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final Integer maxPoints) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/resolve");
    bld.queryParam("id", identifiers(identifierBundle));
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (resolutionKey != null) {
      bld.queryParam("resolutionKey", resolutionKey);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchResolve(
      final URI baseUri, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataField, final String resolutionKey,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final Integer maxPoints) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/resolve");
    bld.queryParam("id", identifiers(identifierBundle));
    bld.queryParam("idValidityDate", identifierValidityDate != null ? identifierValidityDate : "ALL");
    if (dataField != null) {
      bld.queryParam("dataField", dataField);
    }
    if (resolutionKey != null) {
      bld.queryParam("resolutionKey", resolutionKey);
    }
    if (start != null) {
      bld.queryParam("start", start);
      bld.queryParam("includeStart", includeStart);
    }
    if (end != null) {
      bld.queryParam("end", end);
      bld.queryParam("includeEnd", includeEnd);
    }
    if (maxPoints != null) {
      bld.queryParam("maxPoints", maxPoints);
    }
    return bld.build();
  }

  public static URI uriSearchBulk(final URI baseUri) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/bulk");
    return bld.build();
  }

  public static FudgeMsg uriSearchBulkData(
      final Set<ExternalIdBundle> identifierSet, final String dataSource, final String dataProvider, final String dataField,
      final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd) {
    final FudgeSerializer serializationContext = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializationContext.newMessage();
    serializationContext.addToMessage(msg, "id", null, identifierSet);
    serializationContext.addToMessage(msg, "dataSource", null, dataSource);
    serializationContext.addToMessage(msg, "dataProvider", null, dataProvider);
    serializationContext.addToMessage(msg, "dataField", null, dataField);
    serializationContext.addToMessage(msg, "start", null, start);
    serializationContext.addToMessage(msg, "includeStart", null, includeStart);
    serializationContext.addToMessage(msg, "end", null, end);
    serializationContext.addToMessage(msg, "includeEnd", null, includeEnd);
    return msg;
  }

}
