/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A portfolio copier that copies positions/securities from readers to the specified writer while resolving time-series.
 */
public class ResolvingPortfolioCopier implements PortfolioCopier {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolvingPortfolioCopier.class);

  private HistoricalTimeSeriesMaster _htsMaster;
  private HistoricalTimeSeriesProvider _htsProvider;
  private ReferenceDataProvider _bbgRefDataProvider;
  private String _dataProvider;
  private String[] _dataFields;

  public ResolvingPortfolioCopier(
      final HistoricalTimeSeriesMaster htsMaster,
      final HistoricalTimeSeriesProvider htsProvider,
      final ReferenceDataProvider bbgRefDataProvider,
      final String dataProvider,
      final String[] dataFields) {

    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(htsProvider, "htsProvider");
    ArgumentChecker.notNull(bbgRefDataProvider, "bbgRefDataProvider");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataFields, "dataFields");

    _htsMaster = htsMaster;
    _htsProvider = htsProvider;
    _bbgRefDataProvider = bbgRefDataProvider;
    _dataProvider = dataProvider;
    _dataFields = dataFields;
  }

  @Override
  public void copy(final PositionReader positionReader, final PositionWriter positionWriter) {
    copy(positionReader, positionWriter, null);
  }

  @Override
  public void copy(final PositionReader positionReader, final PositionWriter positionWriter, final PortfolioCopierVisitor visitor) {

    ArgumentChecker.notNull(positionWriter, "positionWriter");
    ArgumentChecker.notNull(positionReader, "positionReader");

    // Get bbg hts loader
    final BloombergIdentifierProvider bbgIdentifierProvider = new BloombergIdentifierProvider(_bbgRefDataProvider);
    final BloombergHistoricalTimeSeriesLoader bbgLoader = new BloombergHistoricalTimeSeriesLoader(_htsMaster, _htsProvider, bbgIdentifierProvider);

    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    // Read in next row, checking for EOF
    while ((next = positionReader.readNext()) != null) {

      // Is position and security data is available for the current row?
      if (next.getFirst() != null && next.getSecond() != null) {

        // Set current path
        final String[] path = positionReader.getCurrentPath();
        positionWriter.setPath(path);

        // Load all relevant HTSes
        for (final ManageableSecurity security : next.getSecond()) {
          resolveTimeSeries(bbgLoader, security, _dataFields, _dataProvider, visitor);
        }

        // Write position and security data
        final ObjectsPair<ManageablePosition, ManageableSecurity[]> written =
            positionWriter.writePosition(next.getFirst(), next.getSecond());

        if (visitor != null) {
          visitor.info(StringUtils.arrayToDelimitedString(path, "/"), written.getFirst(), written.getSecond());
        }
      } else {
        if (visitor != null) {
          if (next.getFirst() == null) {
            visitor.error("Could not load position");
          }
          if (next.getSecond() == null) {
            visitor.error("Could not load security(ies)");
          }
        }
      }

    }

    // Flush changes to portfolio master
    positionWriter.flush();
  }

  void resolveTimeSeries(final BloombergHistoricalTimeSeriesLoader bbgLoader, final ManageableSecurity security, final String[] dataFields, final String dataProvider, final PortfolioCopierVisitor visitor) {
    for (final String dataField : dataFields) {
      Set<ExternalId> ids = new HashSet<ExternalId>();
      ids = security.getExternalIdBundle().getExternalIds();
      Map<ExternalId, UniqueId> tsMap = null;
      for (final ExternalId id : ids) {
        tsMap = bbgLoader.loadTimeSeries(Collections.singleton(id), dataProvider, dataField, null, null);
        final String message = "historical time series " + id.toString() + ", fields " + dataField +
            " from " + dataProvider;
        if (tsMap.size() > 0) {
          s_logger.info("Loaded " + message + ": " + tsMap);
          if (visitor != null) {
            visitor.info("Loaded " + message);
          }
          break;
        }
      }
      if (tsMap == null || tsMap.size() == 0) {
        s_logger.warn("Could not load historical time series for security " + security);
        if (visitor != null) {
          visitor.error("Could not load historical time series for security " + security);
        }
      }
    }
  }

  public HistoricalTimeSeriesMaster getHtsMaster() {
    return _htsMaster;
  }

  public void setHtsMaster(final HistoricalTimeSeriesMaster htsMaster) {
    _htsMaster = htsMaster;
  }

  public HistoricalTimeSeriesProvider getHtsProvider() {
    return _htsProvider;
  }

  public void setHtsProvider(final HistoricalTimeSeriesProvider htsProvider) {
    _htsProvider = htsProvider;
  }

  public ReferenceDataProvider getBbgRefDataProvider() {
    return _bbgRefDataProvider;
  }

  public void setBbgRefDataProvider(final ReferenceDataProvider bbgRefDataProvider) {
    _bbgRefDataProvider = bbgRefDataProvider;
  }

  public String getDataProvider() {
    return _dataProvider;
  }

  public void setDataProvider(final String dataProvider) {
    _dataProvider = dataProvider;
  }

  public String[] getDataFields() {
    return _dataFields;
  }

  public void setDataFields(final String[] dataFields) {
    _dataFields = dataFields;
  }

}
