package com.mcleodmoores.starling.client.marketdata;

import au.com.bytecode.opencsv.CSVWriter;
import com.mcleodmoores.starling.client.marketdata.DataField;
import com.mcleodmoores.starling.client.marketdata.DataProvider;
import com.mcleodmoores.starling.client.marketdata.DataSource;
import com.mcleodmoores.starling.client.marketdata.MarketDataKey;
import com.mcleodmoores.starling.client.marketdata.MarketDataSet;
import com.mcleodmoores.starling.client.marketdata.UnitNormalizer;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.ser.JodaBeanSer;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to write a MarketDataSet to a CSV file (or String).
 */
public class MarketDataSetFileWriter {
  private static final String DATE = "Date";
  private static final String FIELD = "Field";
  private static final String SOURCE = "Source";
  private static final String PROVIDER = "Provider";
  private static final String NORMALIZER = "Normalizer";
  private static final String VALUE = "Value";

  private final DateTimeFormatter _formatter;

  /**
   * Constructor.
   * @param formatter  dateTimeFormatter to use for formatting dates in a time series
   */
  public MarketDataSetFileWriter(final DateTimeFormatter formatter) {
    _formatter = ArgumentChecker.notNull(formatter, "formatter");
  }

  /**
   * Convert a market data set into a CSV file and return in a string.
   * @param dataSet  the data set, not null
   * @return a string containing a CSV representation
   */
  public String toCSV(final MarketDataSet dataSet) {
    StringWriter writer = new StringWriter();
    writeCSV(writer, dataSet);
    return writer.toString();
  }

  /**
   * Write a market data set to a CSV file.
   * @param file  the file to write to, not null
   * @param dataSet  the data set, not null
   */
  public void writeCSV(final File file, final MarketDataSet dataSet) {
    ArgumentChecker.notNull(file, "file");
    if (file.isDirectory()) {
      throw new RuntimeException("File does not exist");
    }
    try {
      writeCSV(new FileWriter(file), dataSet);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Write a market data set to a Writer.
   * @param writer  the Writer to write to, not null
   * @param dataSet  the data set, not null
   */
  public void writeCSV(final Writer writer, final MarketDataSet dataSet) {
    ArgumentChecker.notNull(writer, "writer");
    ArgumentChecker.notNull(dataSet, "dataSet");
    try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(writer))) {
      DataSetAnalysis analysis = DataSetAnalysis.of(dataSet);
      writeHeader(csvWriter, analysis);
      for (Map.Entry<MarketDataKey, Object> entry : dataSet.entrySet()) {
        Object val = entry.getValue();
        if (val == null) {
          writeEntry(csvWriter, analysis, entry.getKey(), (Double) null);
        } else if (val instanceof Double) {
          writeEntry(csvWriter, analysis, entry.getKey(), (Double) val);
        } else if (val instanceof LocalDateDoubleTimeSeries) {
          writeEntry(csvWriter, analysis, entry.getKey(), (LocalDateDoubleTimeSeries) val);
        } else {
          throw new RuntimeException("Unrecognised value type of " + entry.getValue().getClass()
              + " for key " + entry.getKey() + " in MarketDataSet");
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private void writeHeader(final CSVWriter csvWriter, final DataSetAnalysis analysis) {
    List<String> fields = new ArrayList<>();
    for (ExternalScheme scheme : analysis.getExternalSchemes()) {
      fields.add("ExternalId[" + scheme.getName() + "]");
    }
    if (analysis.isFieldRequired()) {
      fields.add(FIELD);
    }
    if (analysis.isSourceRequired()) {
      fields.add(SOURCE);
    }
    if (analysis.isProviderRequired()) {
      fields.add(PROVIDER);
    }
    if (analysis.isNormalizerRequired()) {
      fields.add(NORMALIZER);
    }
    if (analysis.isDateRequred()) {
      fields.add(DATE);
    }
    fields.add(VALUE);
    String[] header = fields.toArray(new String[fields.size()]);
    csvWriter.writeNext(header);
  }

  private List<String> keyFields(final DataSetAnalysis analysis, final MarketDataKey key) {
    List<String> fields = new ArrayList<>();
    for (ExternalScheme scheme : analysis.getExternalSchemes()) {
      String idValue = key.getExternalIdBundle().getValue(scheme);
      if (idValue != null) {
        fields.add(idValue);
      } else {
        fields.add("");
      }
    }
    if (analysis.isFieldRequired()) {
      fields.add(key.getField().getName());
    }
    if (analysis.isSourceRequired()) {
      fields.add(key.getSource().getName());
    }
    if (analysis.isProviderRequired()) {
      fields.add(key.getProvider().getName());
    }
    if (analysis.isNormalizerRequired()) {
      fields.add(key.getNormalizer().getName());
    }
    return fields;
  }

  private void writeEntry(final CSVWriter csvWriter, final DataSetAnalysis analysis, final MarketDataKey key, final Double value) {
    List<String> fields = keyFields(analysis, key);
    if (analysis.isDateRequred()) {
      fields.add(""); // blank for scalar
    }
    if (value == null) {
      fields.add(""); // this lets us write data sets with missing values indicated by null values.
    } else {
      fields.add(Double.toString(value));
    }
    csvWriter.writeNext(fields.toArray(new String[fields.size()]));
  }

  private void writeEntry(final CSVWriter csvWriter, final DataSetAnalysis analysis, final MarketDataKey key, final LocalDateDoubleTimeSeries value) {
    final List<String> fields = keyFields(analysis, key);
    final String[] row = fields.toArray(new String[fields.size() + 2]);
    final int dateIndex = fields.size();
    final int valueIndex = fields.size() + 1;
    for (Map.Entry<LocalDate, Double> entry : value) {
      row[dateIndex] = _formatter.format(entry.getKey());
      row[valueIndex] = Double.toString(entry.getValue());
      csvWriter.writeNext(row);
    }
    csvWriter.writeNext(fields.toArray(new String[fields.size()]));
  }

  /**
   * This class builds up a bunch of statistics on whether various fields
   * require non-default values, and also what schemes are used in all the
   * ExternalIdBundles in a MarketDataSet.
   */
  private static class DataSetAnalysis {
    private boolean _fieldRequired; // = false
    private boolean _sourceRequired; // = false
    private boolean _providerRequired; // = false
    private boolean _normalizerRequired; // = false
    private boolean _dateRequried; // = false;

    private Set<ExternalScheme> _schemes = new LinkedHashSet<>();

    private void analyze(final MarketDataKey key, final Object value) {
      if (!key.getField().equals(DataField.PRICE)) {
        _fieldRequired = true;
      }
      if (!key.getSource().equals(DataSource.DEFAULT)) {
        _sourceRequired = true;
      }
      if (!key.getProvider().equals(DataProvider.DEFAULT)) {
        _providerRequired = true;
      }
      if (!key.getNormalizer().equals(UnitNormalizer.INSTANCE)) {
        _normalizerRequired = true;
      }
      if (value instanceof LocalDateDoubleTimeSeries) {
        _dateRequried = true;
      }
      for (ExternalId externalId : key.getExternalIdBundle().getExternalIds()) {
        _schemes.add(externalId.getScheme());
      }
    }

    public static DataSetAnalysis of(final MarketDataSet dataSet) {
      DataSetAnalysis analysis = new DataSetAnalysis();
      for (MarketDataKey key : dataSet.keySet()) {
        analysis.analyze(key, dataSet.get(key));
      }
      return analysis;
    }

    public boolean isFieldRequired() {
      return _fieldRequired;
    }

    public boolean isSourceRequired() {
      return _sourceRequired;
    }

    public boolean isProviderRequired() {
      return _providerRequired;
    }

    public boolean isNormalizerRequired() {
      return _normalizerRequired;
    }

    public boolean isDateRequred() {
      return _dateRequried;
    }

    public Set<ExternalScheme> getExternalSchemes() {
      return _schemes;
    }
  }

}
