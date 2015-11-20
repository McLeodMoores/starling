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
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * RESTful URIs for time-series.
 */
public class DataHistoricalTimeSeriesSourceUris {
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("hts/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }
  
  public static URI uriExternalIdBundleGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsMeta/externalIdBundle/{htsId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  public static URI uriGet(URI baseUri, UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("hts/{htsId}");
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
    } catch (UnsupportedEncodingException e) {  // CSIGNORE
      throw new OpenGammaRuntimeException("Caught", e);
    }
    return array;
  }

  public static URI uriSearchSingle(
      URI baseUri, ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/single");
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

  public static URI uriSearchSingle(
      URI baseUri, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/single");
    bld.queryParam("id", identifiers(identifierBundle));
    bld.queryParam("idValidityDate", (identifierValidityDate != null ? identifierValidityDate : "ALL"));
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
      URI baseUri, ExternalIdBundle identifierBundle, String dataField, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/resolve");
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
      URI baseUri, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataField, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/resolve");
    bld.queryParam("id", identifiers(identifierBundle));
    bld.queryParam("idValidityDate", (identifierValidityDate != null ? identifierValidityDate : "ALL"));
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

  public static URI uriSearchBulk(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("htsSearches/bulk");
    return bld.build();
  }

  public static FudgeMsg uriSearchBulkData(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    FudgeSerializer serializationContext = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    MutableFudgeMsg msg = serializationContext.newMessage();
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
