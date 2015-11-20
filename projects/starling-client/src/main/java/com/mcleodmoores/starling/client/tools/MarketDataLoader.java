package com.mcleodmoores.starling.client.tools;

import au.com.bytecode.opencsv.CSVWriter;
import com.mcleodmoores.starling.client.marketdata.MarketDataFileParser;
import com.mcleodmoores.starling.client.marketdata.MarketDataKey;
import com.mcleodmoores.starling.client.marketdata.MarketDataManager;
import com.mcleodmoores.starling.client.marketdata.MarketDataMetaData;
import com.mcleodmoores.starling.client.marketdata.MarketDataSet;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.mcleodmoores.starling.client.utils.PreviousBusinessDayTemporalAdjuster;
import com.mcleodmoores.starling.client.utils.TablePrinter;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.scripts.Scriptable;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command line tool to load market data from a CSV file.
 */
@Scriptable
public class MarketDataLoader extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataLoader.class);

  private static final String INPUT_FILE_OPTION = "f";
  private static final String INPUT_FILE_LONG = "file";
  private static final String INPUT_FILE_DESCRIPTION = "The input CSV file";
  private static final String INPUT_FILE_ARG_NAME = "filename";

  private static final String DATE_OPTION = "d";
  private static final String DATE_LONG = "date";
  private static final String DATE_DESCRIPTION = "The date (in current locale short form or Today/Yesterday/YesterdayBusiness) "
                                               + "of the scalar values, default to today";
  private static final String DATE_ARG_NAME = "date";
  private static final String TODAY = "TODAY";
  private static final String YESTERDAY = "YESTERDAY";
  private static final String YESTERDAY_BUSINESS = "YESTERDAYBUSINESS";

  private static final String TEST_OPTION = "t";
  private static final String TEST_LONG = "test";
  private static final String TEST_DESCRIPTION = "Don't persist the data, just parse it";

  private static final String DATEPATTERN_OPTION = "dp";
  private static final String DATEPATTERN_LONG = "date-pattern";
  private static final String DATEPATTERN_DESCRIPTION = "Provide a date parsing pattern";
  private static final String DATEPATTERN_ARG_NAME =  "The pattern, e.g. dd/MM/yyyy";

  private static final String QUERY_OPTION = "q";
  private static final String QUERY_LONG = "query";
  private static final String QUERY_DESCRIPTION = "Output template file";
  private static final String QUERY_ARG_NAME = "view name";

  private static final String OVERWRITE_OPTION = "o";
  private static final String OVERWRITE_LONG = "overwrite";
  private static final String OVERWRITE_DESCRIPTION = "Overwrite existing file when in query mode";

  private static final Pattern PATTERN = Pattern.compile("^ExternalId\\[(.*?)\\]$", Pattern.CASE_INSENSITIVE);

  private DateTimeFormatter _dateFormatter;
  private LocalDate _date;

  @Override
  protected void doRun() throws Exception {
    initDateFormatter();
    initDate();
    final String fileName = getCommandLine().getOptionValue(INPUT_FILE_OPTION);
    if (getCommandLine().hasOption(QUERY_OPTION)) {
      File file = new File(fileName);
      if (file.exists()) {
        if (getCommandLine().hasOption(OVERWRITE_OPTION)) {
          writeFile(fileName, getCommandLine().getOptionValue(QUERY_OPTION));
        } else {
          LOGGER.error("Command would cause existing file to be overwritten, use -o option to force.");
        }
      } else {
        writeFile(fileName, getCommandLine().getOptionValue(QUERY_OPTION));
      }
    } else {
      File file = new File(fileName);
      if (!file.exists()) {
        LOGGER.error("File {} does not exist", fileName);
        System.exit(1);
      }
      if (!file.canRead()) {
        LOGGER.error("File {} cannot be read", fileName);
        System.exit(1);
      }
      if (file.isDirectory()) {
        LOGGER.error("File {} is a directory", fileName);
        System.exit(1);
      }
      MarketDataFileParser parser = new MarketDataFileParser(_dateFormatter, _date);
      MarketDataSet dataSet = parser.readFile(new FileReader(file));
      save(dataSet);
    }
  }

  /**
   * Write a CSV file with the required market data together with meta-data.
   * @param fileName  the path of the file to save
   * @param viewName  the name of the view
   */
  protected void writeFile(final String fileName, final String viewName) {
    MarketDataManager manager = new MarketDataManager(getToolContext());
    Map<MarketDataKey, MarketDataMetaData> requiredData = manager.getRequiredData(ViewKey.of(viewName), Instant.now());
    try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(fileName)))) {
      Map<Integer, String> header = createHeader(requiredData);
      writeHeader(writer, header);
      for (Map.Entry<MarketDataKey, MarketDataMetaData> entry : requiredData.entrySet()) {
        String[] row = new String[header.size()];
        MarketDataKey key = entry.getKey();
        MarketDataMetaData metaData = entry.getValue();
        int i = 0;
        while (i < header.size()) {
          String columnName = header.get(i);
          Matcher matcher = PATTERN.matcher(columnName);
          if (matcher.matches()) {
            String scheme = matcher.group(1);
            for (ExternalId id : key.getExternalIdBundle().getExternalIds(ExternalScheme.of(scheme))) {
              row[i++] = id.getValue();
            }
            while (header.get(i).equals(columnName)) {
              i++; // skip over any columns this key doesn't need to fill in.
            }
          } else {
            switch (columnName) {
              case "Field":
                row[i++] = key.getField().getName();
                break;
              case "Source":
                row[i++] = key.getSource().getName();
                break;
              case "Provider":
                row[i++] = key.getProvider().getName();
                break;
              case "Normalizer":
                row[i++] = key.getNormalizer().getName();
                break;
              case "Value":
                row[i++] = "";
                break;
              case "TypeInfo":
                row[i++] = metaData.toString();
                break;
              default:
                break;

            }
          }

        }
        writer.writeNext(row);
      }
      writer.close();
    } catch (IOException ioe) {
      LOGGER.error("Problem writing file {}", fileName);
    }
  }

  /**
   * write a header tow to a CSVWriter.
   * @param writer  the CSVWriter
   * @param header  a map of index to column name
   */
  protected void writeHeader(final CSVWriter writer, final Map<Integer, String> header) {
    String[] headerStr = new String[header.size()];
    int i = 0;
    for (String columnName :header.values()) {
      headerStr[i++] = columnName;
    }
    writer.writeNext(headerStr);
  }

  /**
   * Create a header map (index->columnName).
   * @param requiredData  map of the data required for a view
   * @return a header map
   */
  protected Map<Integer, String> createHeader(final Map<MarketDataKey, MarketDataMetaData> requiredData) {
    // Unforunately this is more complex because it's possible to have several ids with the same scheme in a bundle.
    Map<ExternalScheme, Integer> schemes = new LinkedHashMap<>(); // count of number of each scheme.
    for (MarketDataKey key : requiredData.keySet()) {
      Map<ExternalScheme, Integer> localSchemes = new LinkedHashMap<>(); // just within this bundle
      for (ExternalId id : key.getExternalIdBundle().getExternalIds()) {
        ExternalScheme scheme = id.getScheme();
        if (localSchemes.containsKey(scheme)) {
          localSchemes.put(scheme, localSchemes.get(scheme) + 1);
        } else {
          localSchemes.put(scheme, 1);
        }
      }
      for (Map.Entry<ExternalScheme, Integer> entry : localSchemes.entrySet()) { // merge it in.
        ExternalScheme scheme = entry.getKey();
        int count = entry.getValue();
        if (schemes.containsKey(scheme)) {
          if (schemes.get(scheme) < count) {
            schemes.put(scheme, count);
          }
        } else {
          schemes.put(scheme, count);
        }
      }
    }
    int i = 0;
    Map<Integer, String> columnMappings = new LinkedHashMap<>();
    for (Map.Entry<ExternalScheme, Integer> entry : schemes.entrySet()) {
      ExternalScheme key = entry.getKey();
      Integer value = entry.getValue();
      for (int j = 0; j < value; j++) {
        columnMappings.put(i++, "ExternalId[" + key + "]");
      }
    }
    columnMappings.put(i++, "Field");
    columnMappings.put(i++, "Source");
    columnMappings.put(i++, "Provider");
    columnMappings.put(i++, "Normalizer");
    columnMappings.put(i++, "Value");
    columnMappings.put(i, "TypeInfo");
    return columnMappings;
  }

  /**
   * Save or update a market data set.
   * @param dataSet  the market data set
   */
  protected void save(final MarketDataSet dataSet) {
    if (!getCommandLine().hasOption(TEST_OPTION)) {
      MarketDataManager manager = new MarketDataManager(getToolContext());
      manager.saveOrUpdate(dataSet, _date);
      LOGGER.info("Data saved.");
    } else {
      System.out.println(TablePrinter.toPrettyPrintedString(dataSet));
      LOGGER.info("Test mode, data parsed but not saved");
    }
  }



  private void initDateFormatter() {
    if (getCommandLine().hasOption(DATEPATTERN_OPTION)) {
      String datePattern = getCommandLine().getOptionValue(DATEPATTERN_OPTION);
      _dateFormatter = DateTimeFormatter.ofPattern(datePattern);
    } else {
      if (Locale.getDefault().getCountry().equals("US")) {
        _dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      } else {
        _dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      }
    }
  }

  private void initDate() {
    if (getCommandLine().hasOption(DATE_OPTION)) {
      String dateStr = getCommandLine().getOptionValue(DATE_OPTION);
      switch (dateStr.toUpperCase()) {
        case TODAY:
          _date = LocalDate.now();
          break;
        case YESTERDAY:
          _date = LocalDate.now().minusDays(1);
          break;
        case YESTERDAY_BUSINESS:
          _date = LocalDate.now().with(new PreviousBusinessDayTemporalAdjuster());
          break;
        default:
          try {
            _date = (LocalDate) _dateFormatter.parse(dateStr, LocalDate.FROM);
          } catch (DateTimeParseException dtpe) {
            LOGGER.error("Could not parse date {}, expected format is {}, try setting the locale via the --locale flag", dateStr, _dateFormatter.toString());
            System.exit(1);
          }
          break;
      }
    } else {
      _date = LocalDate.now();
    }
  }





  @Override
  protected Options createOptions(final boolean requiresConfigResource) {
    Options options = super.createOptions(requiresConfigResource);

    Option fileOption = new Option(INPUT_FILE_OPTION, INPUT_FILE_LONG, true, INPUT_FILE_DESCRIPTION);
    fileOption.setArgName(INPUT_FILE_ARG_NAME);
    fileOption.setArgs(1);
    fileOption.setRequired(true);
    options.addOption(fileOption);

    Option localeOption = new Option(DATEPATTERN_OPTION, DATEPATTERN_LONG, true, DATEPATTERN_DESCRIPTION);
    localeOption.setArgName(DATEPATTERN_ARG_NAME);
    localeOption.setArgs(1);
    localeOption.setRequired(false);
    options.addOption(localeOption);

    Option dateOption = new Option(DATE_OPTION, DATE_LONG, true, DATE_DESCRIPTION);
    dateOption.setArgName(DATE_ARG_NAME);
    dateOption.setArgs(1);
    dateOption.setRequired(false);
    options.addOption(dateOption);

    Option testOption = new Option(TEST_OPTION, TEST_LONG, false, TEST_DESCRIPTION);
    testOption.setRequired(false);
    options.addOption(testOption);

    Option queryOption = new Option(QUERY_OPTION, QUERY_LONG, false, QUERY_DESCRIPTION);
    queryOption.setArgName(QUERY_ARG_NAME);
    queryOption.setArgs(1);
    queryOption.setRequired(false);
    options.addOption(queryOption);

    Option overwriteOption = new Option(OVERWRITE_OPTION, OVERWRITE_LONG, false, OVERWRITE_DESCRIPTION);
    overwriteOption.setRequired(false);
    options.addOption(overwriteOption);
    return options;
  }

  /**
   * MarketDataLoader CLI entry point.
   * @param args  command line arguments
   */
  public static void main(final String[] args) {
    MarketDataLoader loader = new MarketDataLoader();
    loader.invokeAndTerminate(args);
  }
}
