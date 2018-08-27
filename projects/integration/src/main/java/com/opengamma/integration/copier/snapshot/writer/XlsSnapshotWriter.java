/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.copier.sheet.writer.XlsSheetWriter;
import com.opengamma.integration.copier.sheet.writer.XlsWriter;
import com.opengamma.integration.copier.snapshot.SnapshotColumns;
import com.opengamma.integration.copier.snapshot.SnapshotType;
import com.opengamma.integration.tool.marketdata.MarketDataSnapshotToolUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Export snapshot to XLS, separating snapshot elements into workbook sheets
 */
public class XlsSnapshotWriter implements SnapshotWriter {

  private final XlsWriter _xlsWriter;
  private final XlsSheetWriter _nameSheet;
  private final XlsSheetWriter _curveSheet;
  private final XlsSheetWriter _yieldCurveSheet;
  private final XlsSheetWriter _globalsSheet;
  private final XlsSheetWriter _surfaceSheet;

  private static final Logger LOGGER = LoggerFactory.getLogger(XlsSnapshotWriter.class);

  public XlsSnapshotWriter(final String filename) {

    if (filename == null) {
      throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
    }

    _xlsWriter = new XlsWriter(filename);
    _nameSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.NAME.get());
    _curveSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.CURVE.get());
    _yieldCurveSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.YIELD_CURVE.get());
    _globalsSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.GLOBAL_VALUES.get());
    _surfaceSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.VOL_SURFACE.get());

  }

  /**
   * @param snapshot UnstructuredMarketDataSnapshot
   * @return Map of ID Bundle (delimited with |) to Pair of market and override value
   */
  private Map<String, ObjectsPair<String, String>> buildUnstructuredMarketDataSnapshotMap(final UnstructuredMarketDataSnapshot snapshot) {
    final Map<String, ObjectsPair<String, String>> values = new LinkedHashMap<>();
    values.put(SnapshotColumns.ID_BUNDLE.get(),
               ObjectsPair.of(SnapshotColumns.MARKET_VALUE.get(), SnapshotColumns.OVERRIDE_VALUE.get()));

    for (final ExternalIdBundle eib : snapshot.getTargets()) {
      final Map<String, ValueSnapshot> valueSnapshots =  snapshot.getTargetValues(eib);
      if (valueSnapshots.size() > 1) {
        throw new OpenGammaRuntimeException("XML export only supports a single value snapshot for UnstructuredMarketDataSnapshot. " +
                                                eib.toString() + " contains " + valueSnapshots.size() + " ValueSnapshots. " +
                                                "Export to CSV in this instance.");
      }
      final ValueSnapshot valueSnapshot = valueSnapshots.entrySet().iterator().next().getValue();
      final String market = valueSnapshot.getMarketValue() == null ? "" : valueSnapshot.getMarketValue().toString();
      final String override = valueSnapshot.getOverrideValue() == null ? "" : valueSnapshot.getOverrideValue().toString();
      values.put(StringUtils.join(eib.getExternalIds(), '|'), ObjectsPair.of(market, override));

    }
    return values;
  }

  @Override
  public void writeVolatilitySurface(final Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface) {

    if (volatilitySurface == null || volatilitySurface.isEmpty()) {
      LOGGER.warn("Snapshot does not contain any Volatility Surfaces.");
      return;
    }

    for (final Map.Entry<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> entry : volatilitySurface.entrySet()) {
      final VolatilitySurfaceSnapshot surface = entry.getValue();
      final Map<String, String> details = new HashMap<>();
      details.put(SnapshotColumns.TYPE.get(), SnapshotType.VOL_SURFACE.get());
      details.put(SnapshotColumns.NAME.get(), entry.getKey().getName());
      details.put(SnapshotColumns.SURFACE_TARGET.get(), entry.getKey().getTarget().toString());
      details.put(SnapshotColumns.SURFACE_INSTRUMENT_TYPE.get(), entry.getKey().getInstrumentType());
      details.put(SnapshotColumns.SURFACE_QUOTE_TYPE.get(), entry.getKey().getQuoteType());
      details.put(SnapshotColumns.SURFACE_QUOTE_UNITS.get(), entry.getKey().getQuoteUnits());

      final Map<Pair<String, String>, String> marketValueMap = new LinkedHashMap<>();
      final Map<Pair<String, String>, String> overrideValueMap = new LinkedHashMap<>();

      final Set<String> xMap = new HashSet<>();
      final Set<String> yMap = new HashSet<>();

      for (final Map.Entry<Pair<Object, Object>, ValueSnapshot> value : surface.getValues().entrySet()) {
        final Pair<String, String> ordinals = MarketDataSnapshotToolUtils.ordinalsAsString(value.getKey());

        xMap.add(ordinals.getFirst());
        yMap.add(ordinals.getSecond());

        final ValueSnapshot valueSnapshot = value.getValue();
        final String market = valueSnapshot.getMarketValue() == null ? "" : valueSnapshot.getMarketValue().toString();
        final String override = valueSnapshot.getOverrideValue() == null ? "" : valueSnapshot.getOverrideValue().toString();

        marketValueMap.put(ordinals, market);
        overrideValueMap.put(ordinals, override);
      }

      _surfaceSheet.writeKeyValueBlock(details);
      _surfaceSheet.writeMatrix(xMap, yMap, SnapshotColumns.MARKET_VALUE.get(), marketValueMap, Cell.CELL_TYPE_NUMERIC);
      _surfaceSheet.writeMatrix(xMap, yMap, SnapshotColumns.OVERRIDE_VALUE.get(), overrideValueMap, Cell.CELL_TYPE_NUMERIC);

    }
  }

  @Override
  public void writeGlobalValues(final UnstructuredMarketDataSnapshot globalValues) {

    if (globalValues == null || globalValues.isEmpty()) {
      LOGGER.warn("Snapshot does not contain any Global Values.");
      return;
    }
    _globalsSheet.writeKeyPairBlock(buildUnstructuredMarketDataSnapshotMap(globalValues));
  }


  @Override
  public void writeYieldCurves(final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {

    if (yieldCurves == null || yieldCurves.isEmpty()) {
      LOGGER.warn("Snapshot does not contain any Yield Curve Snapshots.");
      return;
    }

    for (final Map.Entry<YieldCurveKey, YieldCurveSnapshot> entry : yieldCurves.entrySet()) {
      final YieldCurveSnapshot curve = entry.getValue();
      final Map<String, String> details = new LinkedHashMap<>();
      details.put(SnapshotColumns.NAME.get(), entry.getKey().getName());
      details.put(SnapshotColumns.YIELD_CURVE_CURRENCY.get(), entry.getKey().getCurrency().toString());
      details.put(SnapshotColumns.INSTANT.get(), curve.getValuationTime().toString());
      _yieldCurveSheet.writeKeyValueBlock(details);
      _yieldCurveSheet.writeKeyPairBlock(buildUnstructuredMarketDataSnapshotMap(curve.getValues()));
    }
  }

  @Override
  public void writeCurves(final Map<CurveKey, CurveSnapshot> curves) {

    if (curves == null || curves.isEmpty()) {
      LOGGER.warn("Snapshot does not contain any Curve Snapshots.");
      return;
    }

    for (final Map.Entry<CurveKey, CurveSnapshot> entry : curves.entrySet()) {
      final CurveSnapshot curve = entry.getValue();
      final Map<String, String> details = new LinkedHashMap<>();
      details.put(SnapshotColumns.NAME.get(), entry.getKey().getName());
      details.put(SnapshotColumns.INSTANT.get(), curve.getValuationTime().toString());
      _curveSheet.writeKeyValueBlock(details);
      _curveSheet.writeKeyPairBlock(buildUnstructuredMarketDataSnapshotMap(curve.getValues()));
    }
  }

  @Override
  public void writeName(final String name) {
    final Map<String, String> detail = new HashMap<>();
    detail.put(SnapshotType.NAME.get(), name);
    _nameSheet.writeKeyValueBlock(detail);
    _nameSheet.decrementCurrentRowIndex(); //this ensures basis name is directly after name, rather than a new block
  }

  @Override
  public void writeBasisViewName(final String basisName) {
    final Map<String, String> detail = new HashMap<>();
    detail.put(SnapshotType.BASIS_NAME.get(), basisName);
    _nameSheet.writeKeyValueBlock(detail);
  }

  @Override
  public void close() {
    _nameSheet.autoSizeAllColumns();
    _curveSheet.autoSizeAllColumns();
    _yieldCurveSheet.autoSizeAllColumns();
    _globalsSheet.autoSizeAllColumns();
    _surfaceSheet.autoSizeAllColumns();
    _xlsWriter.close();
  }

  @Override
  public void flush() {
    // not used here
  }
}
