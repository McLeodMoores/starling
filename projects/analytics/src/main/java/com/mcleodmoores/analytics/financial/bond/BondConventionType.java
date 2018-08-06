/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 * A set of bond yield convention types that dispatch from the bond security to an implementation
 * of a method specific to the convention type.
 */
public enum BondConventionType implements YieldConventionType, DataYieldConventionType {

  /**
   * The convention used by market participants in the U.S.A. to value treasuries. This is based on
   * an accrual basis of ACT/ACT ISMA and assumes that yields are compounded semi-annually in all
   * periods except the last, where a money market yield is used.
   */
  US_STREET {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitUsStreet(bond, data);
    }

    @Override
    public <RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<RESULT_TYPE> visitor, final BondFixedSecurity bond) {
      return visitor.visitUsStreet(bond);
    }
  },

  UK_BUMP_DMO {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitUkDmo(bond, data);
    }

    @Override
    public <RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<RESULT_TYPE> visitor, final BondFixedSecurity bond) {
      return visitor.visitUkDmo(bond);
    }
  },

  FRANCE_COMPOUND {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitFranceCompound(bond, data);
    }

    @Override
    public <RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<RESULT_TYPE> visitor, final BondFixedSecurity bond) {
      return visitor.visitFranceCompound(bond);
    }
  },

  ITALIAN_TREASURY {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final DataYieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitItalyTreasury(bond, data);
    }

    @Override
    public <RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<RESULT_TYPE> visitor, final BondFixedSecurity bond) {
      return visitor.visitItalyTreasury(bond);
    }
  };

}
