/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.tool.convention;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ConventionSearchSortOrder;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A class that saves all conventions that are present in the master into an XML file.
 */
public class ConventionSaver {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConventionSaver.class);
  /** The convention master */
  private final ConventionMaster _conventionMaster;
  /** The names of the conventions */
  private final List<String> _names;
  /** The types of the conventions */
  private final List<String> _types;
  /** True if verbose output is required */
  private final boolean _verbose;
  /** The search order */
  private final ConventionSearchSortOrder _order;

  public ConventionSaver(final ConventionMaster conventionMaster, final List<String> names, final List<String> types, final boolean verbose) {
    this(conventionMaster, names, types, verbose, ConventionSearchSortOrder.NAME_ASC);
  }

  public ConventionSaver(final ConventionMaster conventionMaster, final List<String> names, final List<String> types, final boolean verbose, final ConventionSearchSortOrder order) {
    _conventionMaster = conventionMaster;
    _names = names;
    _types = types;
    _verbose = verbose;
    _order = order;
  }

  public void saveConventions(final PrintStream outputStream) {
    final List<ConventionEntry> allConventions = getAllConventions();
    if (_verbose) {
      LOGGER.info("Matched " + allConventions.size() + " conventions");
    }
    try (FudgeXMLStreamWriter xmlStreamWriter = new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(), new OutputStreamWriter(outputStream))) {
      final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
      final FlexiBean wrapper = new FlexiBean();
      wrapper.set("conventions", allConventions);
      final MutableFudgeMsg msg = serializer.objectToFudgeMsg(wrapper);
      try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(xmlStreamWriter)) {
        fudgeMsgWriter.writeMessage(msg);
        fudgeMsgWriter.close();
      } catch (final Exception e) {
        LOGGER.error(e.getMessage());
      }
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  private List<ConventionEntry> getAllConventions() {
    final List<ConventionEntry> conventionsToSave = new ArrayList<>();
    if (_types.size() > 0) {
      for (final String type : _types) {
        final ConventionType conventionType = ConventionType.of(type);
        if (_names.size() > 0) {
          for (final String name : _names) {
            conventionsToSave.addAll(getConventions(conventionType, name));
          }
        } else {
          conventionsToSave.addAll(getConventions(conventionType));
        }
      }
    } else {
      if (_names.size() > 0) {
        for (final String name : _names) {
          conventionsToSave.addAll(getConventions(name));
        }
      } else {
        conventionsToSave.addAll(getConventions());
      }
    }
    return conventionsToSave;
  }

  private List<ConventionEntry> getConventions(final ConventionType type, final String name) {
    final ConventionSearchRequest searchReq = createSearchRequest();
    searchReq.setName(name);
    searchReq.setConventionType(type);
    final ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    if (_verbose) {
      LOGGER.info("Found {} conventions of type {} called {}", new Object[] {searchResult.getDocuments().size(), type, name});
    }
    return docsToConventionEntries(searchResult);
  }

  private List<ConventionEntry> getConventions(final ConventionType type) {
    final ConventionSearchRequest searchReq = createSearchRequest();
    searchReq.setConventionType(type);
    final ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    if (_verbose) {
      LOGGER.info("Found {} conventions of type {}", new Object[] {searchResult.getDocuments().size(), type});
    }
    return docsToConventionEntries(searchResult);
  }

  private List<ConventionEntry> getConventions(final String name) {
    final ConventionSearchRequest searchReq = createSearchRequest();
    searchReq.setName(name);
    final ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    if (_verbose) {
      LOGGER.info("Found {} conventions called {}", new Object[] {searchResult.getDocuments().size(), name});
    }
    return docsToConventionEntries(searchResult);
  }

  private List<ConventionEntry> getConventions() {
    final ConventionSearchRequest searchReq = createSearchRequest();
    final ConventionSearchResult searchResult = _conventionMaster.search(searchReq);
    if (_verbose) {
      LOGGER.info("Found {} conventions", searchResult.getDocuments().size());
    }
    return docsToConventionEntries(searchResult);
  }

  /**
   * @return a search request with defaults set
   */
  private ConventionSearchRequest createSearchRequest() {
    final ConventionSearchRequest searchRequest = new ConventionSearchRequest();
    searchRequest.setSortOrder(_order);
    return searchRequest;
  }

  private List<ConventionEntry> docsToConventionEntries(final ConventionSearchResult searchResult) {
    final List<ConventionEntry> results = new ArrayList<>();
    for (final Convention convention : searchResult.getConventions()) {
      final ConventionEntry conventionEntry = new ConventionEntry();
      conventionEntry.setName(convention.getName());
      conventionEntry.setType(convention.getClass().getCanonicalName());
      conventionEntry.setObject(convention);
      results.add(conventionEntry);
    }
    return results;
  }

}
