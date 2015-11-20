package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.poi.ss.formula.functions.T;

import com.opengamma.engine.resource.EngineResourceReference;

/**
 * Data engine resource manager constants shared between Resource and Remotes
 */
public class DataEngineResourceManagerUris {
  /**
   * The time after which unused references may be automatically released.
   */
  public static final long REFERENCE_LEASE_MILLIS = 5000;
  
  public static URI uriReference(URI baseUri, long referenceId) {
    return UriBuilder.fromUri(baseUri).segment(Long.toString(referenceId)).build();
  }
}
