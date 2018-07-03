/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.bond;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;

/**
 *
 */
public enum BondConventionTypes implements YieldConventionType {

  US_STREET {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitUsStreet(bond, data);
    }

  },

  UK_BUMP_DMO {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitUkDmo(bond, data);
    }
  },

  FRANCE_COMPOUND {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitFranceCompound(bond, data);
    }
  },

  GERMAN_BOND {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitUsStreet(bond, data);
    }

  },

  ITALIAN_TREASURY {

    @Override
    public <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(final YieldConventionTypeVisitor<DATA_TYPE, RESULT_TYPE> visitor, final BondFixedSecurity bond,
        final DATA_TYPE data) {
      return visitor.visitItalyTreasury(bond, data);
    }

  };

}
