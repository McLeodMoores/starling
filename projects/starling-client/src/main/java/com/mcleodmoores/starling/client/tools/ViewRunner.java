package com.mcleodmoores.starling.client.tools;

import java.io.File;
import java.util.Locale;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.mcleodmoores.starling.client.results.AnalyticService;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.SynchronousJob;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.mcleodmoores.starling.client.utils.PreviousBusinessDayTemporalAdjuster;
import com.mcleodmoores.starling.client.utils.ResultModelUtils;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Created by jim on 18/05/15.
 */
@Scriptable
public class ViewRunner extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewRunner.class);

  private static final String DATE_OPTION = "d";
  private static final String DATE_LONG = "date";
  private static final String DATE_DESCRIPTION = "The date (in current locale short form or Today/Yesterday/YesterdayBusiness) of the scalar values, default to today";
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

  private static final String VIEW_NAME_OPTION = "v";
  private static final String VIEW_NAME_LONG = "view";
  private static final String VIEW_NAME_DESCRIPTION = "The view";
  private static final String VIEW_NAME_ARG_NAME =  "The view name";

  private static final String OUTPUT_FILE_OPTION = "f";
  private static final String OUTPUT_FILE_LONG = "file";
  private static final String OUTPUT_FILE_DESCRIPTION = "The output CSV file";
  private static final String OUTPUT_FILE_ARG_NAME = "filename";

  private static final String OVERWRITE_OPTION = "o";
  private static final String OVERWRITE_LONG = "overwrite";
  private static final String OVERWRITE_DESCRIPTION = "Overwrite existing file when outputting";


  private DateTimeFormatter _dateFormatter;
  private LocalDate _date;
  private String _portfolio;

  @Override
  protected void doRun() throws Exception {
    initDateFormatter();
    initDate();
    final String viewName = getCommandLine().getOptionValue(VIEW_NAME_OPTION);
    runView(viewName, _date, Instant.now());
  }

  void runView(final String viewName, final LocalDate snapshotDate, final Instant valuationTime) {
    final OperationTimer timer = new OperationTimer(LOGGER, "Running view {} with market data on {}", viewName, snapshotDate);
    //CacheManager cacheManager = CacheManager.create("classpath:default-ehcache.xml");
    final ViewProcessor viewProcessor = getToolContext().getViewProcessor();
    final PositionSource positionSource = getToolContext().getPositionSource();
    final ConfigSource configSource = getToolContext().getConfigSource();
    final AnalyticService service = new AnalyticService(viewProcessor, positionSource, configSource);
    final SynchronousJob job = service.createSynchronousJob(ViewKey.of(viewName), valuationTime, snapshotDate);
    final ResultModel resultModel = job.run();
    timer.finished();
    ResultModelUtils.displayResult(resultModel);
    if (getCommandLine().hasOption(OUTPUT_FILE_OPTION)) {
      final File file = new File(getCommandLine().getOptionValue(OUTPUT_FILE_OPTION));
      if (file.exists()) {
        if (getCommandLine().hasOption(OVERWRITE_OPTION)) {
          ResultModelUtils.saveResult(resultModel, file);
          LOGGER.info("File saved as {}, check parent dir if file not in CWD", file.getName());
        } else {
          LOGGER.error("Not writing output CSV file because it already exists, use --overwrite option to force");
        }
      } else {
        ResultModelUtils.saveResult(resultModel, file);
        LOGGER.info("File saved as {}, check parent dir if file not in CWD", file.getName());
      }
    }
  }

  private void initDateFormatter() {
    if (getCommandLine().hasOption(DATEPATTERN_OPTION)) {
      final String datePattern = getCommandLine().getOptionValue(DATEPATTERN_OPTION);
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
      final String dateStr = getCommandLine().getOptionValue(DATE_OPTION);
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
            _date = (LocalDate) _dateFormatter.parseBest(dateStr, LocalDate.FROM);
          } catch (final DateTimeParseException dtpe) {
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
    final Options options = super.createOptions(requiresConfigResource);

    final Option fileOption = new Option(OUTPUT_FILE_OPTION, OUTPUT_FILE_LONG, true, OUTPUT_FILE_DESCRIPTION);
    fileOption.setArgName(OUTPUT_FILE_ARG_NAME);
    fileOption.setArgs(1);
    fileOption.setRequired(false);
    options.addOption(fileOption);

    final Option overwriteOption = new Option(OVERWRITE_OPTION, OVERWRITE_LONG, false, OVERWRITE_DESCRIPTION);
    overwriteOption.setRequired(false);
    options.addOption(overwriteOption);

    final Option localeOption = new Option(DATEPATTERN_OPTION, DATEPATTERN_LONG, true, DATEPATTERN_DESCRIPTION);
    localeOption.setArgName(DATEPATTERN_ARG_NAME);
    localeOption.setArgs(1);
    localeOption.setRequired(false);
    options.addOption(localeOption);

    final Option dateOption = new Option(DATE_OPTION, DATE_LONG, true, DATE_DESCRIPTION);
    dateOption.setArgName(DATE_ARG_NAME);
    dateOption.setArgs(1);
    dateOption.setRequired(false);
    options.addOption(dateOption);

    final Option viewNameOption = new Option(VIEW_NAME_OPTION, VIEW_NAME_LONG, true, VIEW_NAME_DESCRIPTION);
    viewNameOption.setArgName(VIEW_NAME_ARG_NAME);
    viewNameOption.setArgs(1);
    viewNameOption.setRequired(false);
    options.addOption(viewNameOption);

    final Option testOption = new Option(TEST_OPTION, TEST_LONG, false, TEST_DESCRIPTION);
    testOption.setRequired(false);
    options.addOption(testOption);

    return options;
  }


  public static void main(final String[] args) {
    final ViewRunner loader = new ViewRunner();
    loader.invokeAndTerminate(args);
  }
}
