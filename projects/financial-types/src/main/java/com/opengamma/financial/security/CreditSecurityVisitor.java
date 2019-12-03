/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;

/**
 * @param <T>
 *          The return type of the visitor
 */
public interface CreditSecurityVisitor<T> {

  @Deprecated
  T visitCDSSecurity(CDSSecurity security);

  @Deprecated
  T visitStandardVanillaCDSSecurity(StandardVanillaCDSSecurity security);

  @Deprecated
  T visitStandardFixedRecoveryCDSSecurity(StandardFixedRecoveryCDSSecurity security);

  @Deprecated
  T visitStandardRecoveryLockCDSSecurity(StandardRecoveryLockCDSSecurity security);

  @Deprecated
  T visitLegacyVanillaCDSSecurity(LegacyVanillaCDSSecurity security);

  @Deprecated
  T visitLegacyFixedRecoveryCDSSecurity(LegacyFixedRecoveryCDSSecurity security);

  @Deprecated
  T visitLegacyRecoveryLockCDSSecurity(LegacyRecoveryLockCDSSecurity security);

  T visitCreditDefaultSwapIndexDefinitionSecurity(CreditDefaultSwapIndexDefinitionSecurity security);

  T visitCreditDefaultSwapIndexSecurity(CreditDefaultSwapIndexSecurity security);

  T visitCreditDefaultSwapOptionSecurity(CreditDefaultSwapOptionSecurity security);
}
