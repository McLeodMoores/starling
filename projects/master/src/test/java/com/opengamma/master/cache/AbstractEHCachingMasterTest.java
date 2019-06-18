/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatcher;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;

/**
 * Common properties and methods for testing EHCaching masters. This abstract class declares document variables of generic type without initial values, and
 * provides a populate method which can be called by a test subclass to populate a mock master with the documents, but only after the the subclass initialises
 * the documents with objects of the correct type.
 *
 * @param <M>
 *          the type of the master
 * @param <D>
 *          the type of the document
 */
public abstract class AbstractEHCachingMasterTest<M extends AbstractChangeProvidingMaster<D>, D extends AbstractDocument> {

  protected static final String ID_SCHEME = "Test";

  protected static final Instant NOW = Instant.now();

  // Document A (100, 200, 300)
  protected static final ObjectId A_OID = ObjectId.of(ID_SCHEME, "A");
  protected static final UniqueId A100_UID = UniqueId.of(A_OID, "100");
  protected static final UniqueId A200_UID = UniqueId.of(A_OID, "200");
  protected static final UniqueId A300_UID = UniqueId.of(A_OID, "300");
  protected D _docA100V1999to2010Cto2011;
  protected D _docA200V2010to;
  protected D _docA300V1999to2010C2011to;

  // Document B (200, 400, 500)
  protected static final ObjectId B_OID = ObjectId.of(ID_SCHEME, "B");
  protected static final UniqueId B200_UID = UniqueId.of(B_OID, "200");
  protected static final UniqueId B400_UID = UniqueId.of(B_OID, "400");
  protected static final UniqueId B500_UID = UniqueId.of(B_OID, "500");
  protected D _docB200V2000to2009;
  protected D _docB400V2009to2011;
  protected D _docB500V2011to;

  // Document C (100, 300)
  protected static final ObjectId C_OID = ObjectId.of(ID_SCHEME, "C");
  protected static final UniqueId C100_UID = UniqueId.of(C_OID, "100");
  protected static final UniqueId C300_UID = UniqueId.of(C_OID, "300");
  protected D _docC100Vto2011;
  protected D _docC300V2011to;

  // Document to add
  protected static final ObjectId ADDED_OID = ObjectId.of(ID_SCHEME, "ADDED");
  protected static final UniqueId ADDED_UID = UniqueId.of(ADDED_OID, "1");
  protected D _docToAdd;
  protected D _docAdded;

  /**
   * Creates a fresh mock master and configures it to respond as though it contains the above documents.
   *
   * @param mockUnderlyingMaster
   *          the underlying master
   * @return the mock master
   */
  protected AbstractChangeProvidingMaster<D> populateMockMaster(final M mockUnderlyingMaster) {

    final ChangeManager changeManager = new BasicChangeManager();
    when(mockUnderlyingMaster.changeManager()).thenReturn(changeManager);

    // Set up VersionFrom, VersionTo, CorrectionFrom, CorrectionTo

    // Document A 100: v 1999 to 2010, c to 2011

    _docA100V1999to2010Cto2011.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(1999, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    _docA100V1999to2010Cto2011.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    _docA100V1999to2010Cto2011.setCorrectionToInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document A 200: v 2010 to
    _docA200V2010to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document A 300 (corrects A100): v 1999 to 2010, c 2011 to
    _docA300V1999to2010C2011to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(1999, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    _docA300V1999to2010C2011to.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2010, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    _docA300V1999to2010C2011to.setCorrectionFromInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document B 200: v 2000 to 2009
    _docB200V2000to2009.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2000, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    _docB200V2000to2009.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document B 400: v 2009 to 2011
    _docB400V2009to2011.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2009, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());
    _docB400V2009to2011.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document B 500: v 2011 to
    _docB500V2011to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document C 100: v to 2011
    _docC100Vto2011.setVersionToInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Document C 300: v 2011 to
    _docC300V2011to.setVersionFromInstant(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 12, 0, 0, 0), ZoneOffset.UTC).toInstant());

    // Configure mock master to respond to versioned unique ID gets
    when(mockUnderlyingMaster.get(_docA100V1999to2010Cto2011.getUniqueId())).thenReturn(_docA100V1999to2010Cto2011);
    when(mockUnderlyingMaster.get(_docA200V2010to.getUniqueId())).thenReturn(_docA200V2010to);
    when(mockUnderlyingMaster.get(_docA300V1999to2010C2011to.getUniqueId())).thenReturn(_docA300V1999to2010C2011to);
    when(mockUnderlyingMaster.get(_docB200V2000to2009.getUniqueId())).thenReturn(_docB200V2000to2009);
    when(mockUnderlyingMaster.get(_docB400V2009to2011.getUniqueId())).thenReturn(_docB400V2009to2011);
    when(mockUnderlyingMaster.get(_docB500V2011to.getUniqueId())).thenReturn(_docB500V2011to);
    when(mockUnderlyingMaster.get(_docC100Vto2011.getUniqueId())).thenReturn(_docC100Vto2011);
    when(mockUnderlyingMaster.get(_docC300V2011to.getUniqueId())).thenReturn(_docC300V2011to);

    // Configure mock master to respond to unversioned unique ID gets (should return latest version)
    when(mockUnderlyingMaster.get(_docA100V1999to2010Cto2011.getUniqueId().toLatest())).thenReturn(_docA200V2010to);
    when(mockUnderlyingMaster.get(_docB200V2000to2009.getUniqueId().toLatest())).thenReturn(_docB500V2011to);
    when(mockUnderlyingMaster.get(_docC100Vto2011.getUniqueId().toLatest())).thenReturn(_docC300V2011to);

    // Configure mock master to respond to object ID/Version-Correction gets
    when(mockUnderlyingMaster.get(eq(A_OID), argThat(new IsValidFor(_docA100V1999to2010Cto2011)))).thenReturn(_docA100V1999to2010Cto2011);
    when(mockUnderlyingMaster.get(eq(A_OID), argThat(new IsValidFor(_docA200V2010to)))).thenReturn(_docA200V2010to);
    when(mockUnderlyingMaster.get(eq(A_OID), argThat(new IsValidFor(_docA300V1999to2010C2011to)))).thenReturn(_docA300V1999to2010C2011to);
    when(mockUnderlyingMaster.get(eq(B_OID), argThat(new IsValidFor(_docB200V2000to2009)))).thenReturn(_docB200V2000to2009);
    when(mockUnderlyingMaster.get(eq(B_OID), argThat(new IsValidFor(_docB400V2009to2011)))).thenReturn(_docB400V2009to2011);
    when(mockUnderlyingMaster.get(eq(B_OID), argThat(new IsValidFor(_docB500V2011to)))).thenReturn(_docB500V2011to);
    when(mockUnderlyingMaster.get(eq(C_OID), argThat(new IsValidFor(_docC100Vto2011)))).thenReturn(_docC100Vto2011);
    when(mockUnderlyingMaster.get(eq(C_OID), argThat(new IsValidFor(_docC300V2011to)))).thenReturn(_docC300V2011to);

    // Configure mock master to respond to add
    when(mockUnderlyingMaster.add(_docToAdd)).thenReturn(_docAdded);
    when(mockUnderlyingMaster.get(ADDED_UID)).thenReturn(_docAdded);
    when(mockUnderlyingMaster.get(eq(ADDED_OID), argThat(new IsValidFor(_docAdded)))).thenReturn(_docAdded);

    return mockUnderlyingMaster;
  }

  // -------------------------------------------------------------------------

  /**
   * Mockito argument matcher that checks whether a VersionCorrection is within a document's v/c range.
   */
  class IsValidFor extends ArgumentMatcher<VersionCorrection> {
    private final Instant _fromVersion, _fromCorrection;
    private final Instant _toVersion, _toCorrection;

    /**
     * @param document
     *          the document
     */
    IsValidFor(final AbstractDocument document) {
      _fromVersion = document.getVersionFromInstant();
      _toVersion = document.getVersionToInstant();
      _fromCorrection = document.getCorrectionFromInstant();
      _toCorrection = document.getCorrectionToInstant();
    }

    @Override
    public boolean matches(final Object o) {
      final VersionCorrection vc = (VersionCorrection) o;
      return (_fromVersion == null || vc.getVersionAsOf() == null || vc.getVersionAsOf().isAfter(_fromVersion))
          && (_toVersion == null || vc.getVersionAsOf() != null && vc.getVersionAsOf().isBefore(_toVersion))
          && (_fromCorrection == null || vc.getCorrectedTo() == null || vc.getCorrectedTo().isAfter(_fromCorrection))
          && (_toCorrection == null || vc.getCorrectedTo() != null && vc.getCorrectedTo().isBefore(_toCorrection));
    }
  }

}
