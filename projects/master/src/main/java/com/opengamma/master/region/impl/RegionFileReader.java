/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Charsets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loads a CSV formatted region file
 * <p>
 * This populates a region master.
 */
public class RegionFileReader {

  /**
   * Path to the default regions file
   */
  private static final String REGIONS_RESOURCE = "/com/opengamma/region/regions.csv";
  /**
   * The name column header.
   */
  private static final String NAME_COLUMN = "Name";
  /**
   * The full name column header.
   */
  private static final String FORMAL_NAME_COLUMN = "Formal Name";
  /**
   * The type column header.
   */
  private static final String CLASSIFICATION_COLUMN = "Type";
  /**
   * The sovereignty column header.
   */
  private static final String SOVEREIGNITY_COLUMN = "Sovereignty";
  /**
   * The country code column header.
   */
  private static final String ISO_COUNTRY_2_COLUMN = "ISO 3166-1 2 Letter Code";
  /**
   * The currency code column header.
   */
  private static final String ISO_CURRENCY_3_COLUMN = "ISO 4217 Currency Code";
  /**
   * The sub regions column header
   */
  private static final String SUB_REGIONS_COLUMN = "Sub Regions";

  /**
   * The region master to populate.
   */
  private final RegionMaster _regionMaster;

  /**
   * Creates a populated in-memory master and source.
   * <p>
   * The values can be extracted using the accessor methods.
   *
   * @return the region reader, not null
   */
  public static RegionFileReader createPopulated() {
    return createPopulated0(new InMemoryRegionMaster());
  }

  /**
   * Populates a region master.
   *
   * @param regionMaster  the region master to populate, not null
   * @return the master, not null
   */
  public static RegionMaster createPopulated(final RegionMaster regionMaster) {
    return createPopulated0(regionMaster).getRegionMaster();
  }

  /**
   * Creates a populated file reader.
   * <p>
   * The values can be extracted using the accessor methods.
   *
   * @param regionMaster  the region master to populate, not null
   * @return the region reader, not null
   */
  private static RegionFileReader createPopulated0(final RegionMaster regionMaster) {
    final RegionFileReader fileReader = new RegionFileReader(regionMaster);
    final InputStream stream = regionMaster.getClass().getResourceAsStream(REGIONS_RESOURCE);
    try {
      fileReader.parse(stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
    UnLocodeRegionFileReader.populate(regionMaster);
    return fileReader;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance with a master to populate.
   *
   * @param regionMaster  the region master, not null
   */
  public RegionFileReader(final RegionMaster regionMaster) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regionMaster = regionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the region master.
   * @return the region master, not null
   */
  public RegionMaster getRegionMaster() {
    return _regionMaster;
  }

  /**
   * Gets a {@code MasterRegionSource} for the master.
   * @return the region source, not null
   */
  public MasterRegionSource getRegionSource() {
    return new MasterRegionSource(getRegionMaster());
  }

  //-------------------------------------------------------------------------
  /**
   * Parses the specified file to populate the master.
   *
   * @param in  the input stream to read, not null
   */
  public void parse(final InputStream in) {
    final InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in), Charsets.UTF_8);
    try {
      parse(reader);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * Parses the specified file to populate the master.
   *
   * @param in  the input reader to read, not closed, not null
   */
  public void parse(final Reader in) {
    String name = null;
    try {
      final Map<String, ManageableRegion> regions = new HashMap<>();
      final Map<UniqueId, Set<String>> subRegions = new HashMap<>();

      // open CSV file
      @SuppressWarnings("resource")
      final
      CSVReader reader = new CSVReader(in);
      final List<String> columns = Arrays.asList(reader.readNext());

      // identify columns
      final int nameColumnIdx = columns.indexOf(NAME_COLUMN);
      final int formalNameColumnIdx = columns.indexOf(FORMAL_NAME_COLUMN);
      final int classificationColumnIdx = columns.indexOf(CLASSIFICATION_COLUMN);
      final int sovereignityColumnIdx = columns.indexOf(SOVEREIGNITY_COLUMN);
      final int countryColumnIdx = columns.indexOf(ISO_COUNTRY_2_COLUMN);
      final int currencyColumnIdx = columns.indexOf(ISO_CURRENCY_3_COLUMN);
      final int subRegionsColumnIdx = columns.indexOf(SUB_REGIONS_COLUMN);

      // parse
      String[] row = null;
      while ((row = reader.readNext()) != null) {
        name = row[nameColumnIdx].trim();  // the primary key
        String fullName = StringUtils.trimToNull(row[formalNameColumnIdx]);
        if (fullName == null) {
          fullName = name;
        }
        final RegionClassification classification = RegionClassification.valueOf(row[classificationColumnIdx].trim());
        final String sovereignity = StringUtils.trimToNull(row[sovereignityColumnIdx]);
        final String countryISO = StringUtils.trimToNull(row[countryColumnIdx]);
        final String currencyISO = StringUtils.trimToNull(row[currencyColumnIdx]);
        Set<String> rowSubRegions = new HashSet<>(Arrays.asList(row[subRegionsColumnIdx].split(";")));
        rowSubRegions = trim(rowSubRegions);

        final ManageableRegion region = new ManageableRegion();
        region.setClassification(classification);
        region.setName(name);
        region.setFullName(fullName);
        if (countryISO != null) {
          region.setCountry(Country.of(countryISO));
          region.addExternalId(ExternalSchemes.financialRegionId(countryISO));  // TODO: looks odd
        }
        if (currencyISO != null) {
          region.setCurrency(Currency.of(currencyISO));
        }
        if (sovereignity != null) {
          final ManageableRegion parent = regions.get(sovereignity);
          if (parent == null) {
            throw new OpenGammaRuntimeException("Cannot find parent '" + sovereignity + "'  for '" + name + "'");
          }
          region.getParentRegionIds().add(parent.getUniqueId());
        }
        for (final Entry<UniqueId, Set<String>> entry : subRegions.entrySet()) {
          if (entry.getValue().remove(name)) {
            region.getParentRegionIds().add(entry.getKey());
          }
        }

        // store
        final RegionDocument doc = getRegionMaster().add(new RegionDocument(region));
        if (rowSubRegions.size() > 0) {
          subRegions.put(doc.getUniqueId(), rowSubRegions);
        }
        regions.put(name, region);
      }
      for (final Set<String> set : subRegions.values()) {
        if (set.size() > 0) {
          throw new OpenGammaRuntimeException("Cannot find children: " + set);
        }
      }

    } catch (final Exception ex) {
      final String detail = name != null ? " while processing " + name : "";
      throw new OpenGammaRuntimeException("Cannot open region data file (or file I/O problem)" + detail, ex);
    }
  }

  /**
   * Trim the contents of a set.
   * @param subRegions  the set to trim, not null
   * @return the trimmed set, not null
   */
  private static Set<String> trim(final Set<String> subRegions) {
    final Set<String> result = new HashSet<>();
    for (final String subRegion : subRegions) {
      final String trimmed = subRegion.trim();
      if (!trimmed.isEmpty()) {
        result.add(trimmed);
      }
    }
    return result;
  }

}
