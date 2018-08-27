/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PlatformConfigUtils;

/**
 * A utility to refresh some portion of a mongodb cache by requerying fields from the underlying
 * [BBG-88]
 */
public class MongoDBReferenceDataCacheRefresher {

  private static final String HELP_OPTION = "help";

  private final MongoDBReferenceDataCache _cache;
  private final MongoDBValueCachingReferenceDataProvider _cachedProvider;

  public MongoDBReferenceDataCacheRefresher(final MongoDBValueCachingReferenceDataProvider cachedProvider) {
    super();
    _cachedProvider = cachedProvider;
    _cache = cachedProvider.getCache();
  }

  public void refreshCaches() {
    final Set<String> securities = _cache.getAllCachedSecurities();
    refreshCaches(securities);
  }

  /**
   *
   * @param numberOfSecurities Approximately how many securities to refresh each time
   * @param id some id number, should increment for each call.  Will result in all securities being refreshed after #securities in cache/numberOfSecurities ids.
   */
  public void refreshCaches(final int numberOfSecurities, final long id) {
    ArgumentChecker.isTrue(numberOfSecurities > 0 , "Positive number of securities must be specified");

    final Set<String> securities = _cache.getAllCachedSecurities();
    final int hashBasis = Math.max(securities.size() / numberOfSecurities, 1);

    final Set<String> chosen = new HashSet<>(numberOfSecurities);
    for (final String candidate : securities) {
      if (Math.abs(candidate.hashCode() % hashBasis) == id % hashBasis) {
        chosen.add(candidate);
      }
    }
    refreshCaches(chosen);
  }

  /**
   * NOTE: only refreshes securities where this field was _succesfully_ looked up
   * @param field The field which a security must have for it to be updated
   */
  public void refreshCachesHaving(final String field) {
    final Set<String> securities = _cache.getAllCachedSecurities();
    final Map<String, ReferenceData> loadCachedResults = _cache.load(securities);

    final Set<String> chosen = new HashSet<>();
    for (final String candidate : securities) {
      final ReferenceData cachedResult = loadCachedResults.get(candidate);
      if (cachedResult.getFieldValues().getByName(field) != null) {
        chosen.add(candidate);
      }
    }
    refreshCaches(chosen);
  }

  public void refreshCaches(final Set<String> securities) {
    _cachedProvider.refresh(securities);
  }

  /**
   * Runs the tool.
   *
   * @param args  empty arguments
   * @throws Exception
   */
  public static void main(final String[] args) throws Exception { // CSIGNORE
    PlatformConfigUtils.configureSystemProperties();
    System.out.println("Starting connections");
    final String configLocation = "com/opengamma/bbg/bbg-reference-data-context.xml";

    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
    try {
      context.start();
      final MongoDBValueCachingReferenceDataProvider mongoProvider = context.getBean("bloombergReferenceDataProvider", MongoDBValueCachingReferenceDataProvider.class);
      final MongoDBReferenceDataCacheRefresher refresher = new MongoDBReferenceDataCacheRefresher(mongoProvider);

      final Options options = createOptions();
      final CommandLineParser parser = new PosixParser();
      CommandLine line = null;
      try {
        line = parser.parse(options, args);
      } catch (final ParseException e) {
        usage(options);
        return;
      }
      if (line.hasOption(HELP_OPTION)) {
        usage(options);
        return;
      }

      //TODO other options, e.g. explicitly specify security
      final int numberOfSecurities = Integer.parseInt(line.getArgs()[0]);
      final int id = Integer.parseInt(line.getArgs()[1]);
      System.out.println("Refreshing " + numberOfSecurities + " securities, id " + id);
      refresher.refreshCaches(numberOfSecurities, id);
      System.out.println("Done refreshing");
    } catch (final Exception ex) {
      context.close();
      throw ex;
    }
  }

  private static void usage(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + MongoDBReferenceDataCache.class.getName() + " numberOfSecurities id", options);
  }

  private static Options createOptions() {
    final Options options = new Options();
    options.addOption(new Option("h", HELP_OPTION, false, "Print this message"));
    return options;
  }

}
