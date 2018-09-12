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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loads a CSV formatted UN/LOCODE file based on the regions in the holiday database.
 * <p>
 * This populates a region master.
 */
class UnLocodeRegionFileReader {

  /**
   * Path to the default regions file.
   */
  private static final String REGIONS_RESOURCE = "/com/opengamma/region/UNLOCODE.csv";
  /**
   * Path to the list of locode regions to load.
   */
  private static final String LOAD_RESOURCE = "/com/opengamma/master/region/impl/UnLocode.txt";

  /**
   * The region master to populate.
   */
  private final RegionMaster _regionMaster;

  /**
   * Populates a region master.
   *
   * @param regionMaster  the region master to populate, not null
   * @return the master, not null
   */
  static RegionMaster populate(final RegionMaster regionMaster) {
    final InputStream stream = regionMaster.getClass().getResourceAsStream(REGIONS_RESOURCE);
    final UnLocodeRegionFileReader reader = new UnLocodeRegionFileReader(regionMaster);
    reader.parse(stream);
    return regionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance with a master to populate.
   *
   * @param regionMaster  the region master, not null
   */
  UnLocodeRegionFileReader(final RegionMaster regionMaster) {
    ArgumentChecker.notNull(regionMaster, "regionMaster");
    _regionMaster = regionMaster;
  }

  //-------------------------------------------------------------------------
  private void parse(final InputStream in) {
    final InputStreamReader reader = new InputStreamReader(new BufferedInputStream(in), Charsets.UTF_8);
    try {
      parse(reader);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  private void parse(final InputStreamReader reader) {
    final Set<String> required = parseRequired();
    final Set<ManageableRegion> regions = parseLocodes(reader, required);
    coppClark(regions);
    store(regions);
  }

  private Set<String> parseRequired() {
    final InputStream stream = getClass().getResourceAsStream(LOAD_RESOURCE);
    if (stream == null) {
      throw new OpenGammaRuntimeException("Unable to find UnLocode.txt defining the UN/LOCODEs");
    }
    try {
      final Set<String> lines = new HashSet<>(IOUtils.readLines(stream, "UTF-8"));
      final Set<String> required = new HashSet<>();
      for (String line : lines) {
        line = StringUtils.trimToNull(line);
        if (line != null) {
          required.add(line);
        }
      }
      return required;
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Unable to read UnLocode.txt defining the UN/LOCODEs");
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private Set<ManageableRegion> parseLocodes(final Reader in, final Set<String> required) {
    final Set<ManageableRegion> regions = new HashSet<>(1024, 0.75f);
    String name = null;
    try {
      @SuppressWarnings("resource")
      final
      CSVReader reader = new CSVReader(in);
      final int typeIdx = 0;
      final int countryIsoIdx = 1;
      final int unlocodePartIdx = 2;
      final int nameColumnIdx = 4;
      final int fullNameColumnIdx = 3;

      String[] row = null;
      while ((row = reader.readNext()) != null) {
        if (row.length < 9) {
          continue;
        }
        name = StringUtils.trimToNull(row[nameColumnIdx]);
        final String type = StringUtils.trimToNull(row[typeIdx]);
        String fullName = StringUtils.trimToNull(row[fullNameColumnIdx]);
        fullName = MoreObjects.firstNonNull(fullName, name);
        final String countryISO = StringUtils.trimToNull(row[countryIsoIdx]);
        final String unlocodePart = StringUtils.trimToNull(row[unlocodePartIdx]);
        final String unlocode = countryISO + unlocodePart;
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(fullName) || StringUtils.isEmpty(countryISO)
            || StringUtils.isEmpty(unlocodePart) || unlocode.length() != 5
            || countryISO.equals("XZ") || "=".equals(type) || !required.remove(unlocode)) {
          continue;
        }

        final ManageableRegion region = createRegion(name, fullName, countryISO);
        region.addExternalId(ExternalSchemes.unLocode20102RegionId(unlocode));
        regions.add(region);
      }
    } catch (final Exception ex) {
      final String detail = name != null ? " while processing " + name : "";
      throw new OpenGammaRuntimeException("Unable to read UN/LOCODEs" + detail, ex);
    }
    if (required.size() > 0) {
      throw new OpenGammaRuntimeException("Requested UN/LOCODEs could not be found: " + required);
    }
    return regions;
  }

  private ManageableRegion createRegion(final String name, final String fullName, final String countryISO) {
    final ManageableRegion region = new ManageableRegion();
    region.setClassification(RegionClassification.MUNICIPALITY);
    region.setName(name);
    region.setFullName(fullName);
    addParent(region, countryISO);
    return region;
  }

  private void addParent(final ManageableRegion region, final String countryISO) {
    final RegionSearchRequest request = new RegionSearchRequest();
    request.addCountry(Country.of(countryISO));
    final ManageableRegion parent = _regionMaster.search(request).getFirstRegion();
    if (parent == null) {
      throw new OpenGammaRuntimeException("Cannot find parent '" + countryISO + "'  for '" + region.getName() + "'");
    }
    region.getParentRegionIds().add(parent.getUniqueId());
  }

  private void coppClark(final Set<ManageableRegion> regions) {
    for (final ManageableRegion region : regions) {
      final String unLocode = region.getExternalIdBundle().getValue(ExternalSchemes.UN_LOCODE_2010_2);
      final String coppClarkLocode = COPP_CLARK_ALTERATIONS.get(unLocode);
      if (coppClarkLocode != null) {
        region.addExternalId(ExternalSchemes.coppClarkRegionId(coppClarkLocode));
        if (!coppClarkLocode.substring(0, 2).equals(unLocode.substring(0, 2))) {
          addParent(region, coppClarkLocode.substring(0, 2));
        }
      } else {
        region.addExternalId(ExternalSchemes.coppClarkRegionId(unLocode));
      }
    }
    for (final Entry<String, String> entry : COPP_CLARK_ADDITIONS.entrySet()) {
      final ManageableRegion region = createRegion(entry.getValue(), entry.getValue(), entry.getKey().substring(0, 2));
      region.addExternalId(ExternalSchemes.coppClarkRegionId(entry.getKey()));
      regions.add(region);
    }
  }

  private void store(final Set<ManageableRegion> regions) {
    for (final ManageableRegion region : regions) {
      final RegionDocument doc = new RegionDocument();
      doc.setRegion(region);
      final RegionSearchRequest request = new RegionSearchRequest();
      request.addExternalIds(region.getExternalIdBundle());
      final RegionSearchResult result = _regionMaster.search(request);
      if (result.getDocuments().size() == 0) {
        _regionMaster.add(doc);
      } else {
        final RegionDocument existing = result.getFirstDocument();
        if (!existing.getRegion().getName().equals(doc.getRegion().getName())
            || !existing.getRegion().getFullName().equals(doc.getRegion().getFullName())) {
          existing.getRegion().setName(doc.getRegion().getName());
          existing.getRegion().setFullName(doc.getRegion().getFullName());
          _regionMaster.update(existing);
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  private static final Map<String, String> COPP_CLARK_ALTERATIONS = new HashMap<>();
  static {
    COPP_CLARK_ALTERATIONS.put("CNCAN", "CNXSA");  // Guangzhou (China)
    COPP_CLARK_ALTERATIONS.put("GPMSB", "MFMGT");  // Marigot (Guadaloupe/St.Martin-MF)
    COPP_CLARK_ALTERATIONS.put("GPGUS", "BLSTB");  // Gustavia (Guadaloupe/St.Barts-BL)
    COPP_CLARK_ALTERATIONS.put("FIMHQ", "AXMHQ");  // Mariehamn (Finaland/Aland-AX)
    COPP_CLARK_ALTERATIONS.put("FMPNI", "FMFSM");  // Pohnpei (Micronesia)
    COPP_CLARK_ALTERATIONS.put("MSMNI", "MSMSR");  // Montserrat
  };
  private static final Map<String, String> COPP_CLARK_ADDITIONS = new HashMap<>();
  static {
    COPP_CLARK_ADDITIONS.put("PSPSE", "West Bank");
    COPP_CLARK_ADDITIONS.put("LKMAT", "Matara");
    COPP_CLARK_ADDITIONS.put("ILJRU", "Jerusalem");
  };

}
