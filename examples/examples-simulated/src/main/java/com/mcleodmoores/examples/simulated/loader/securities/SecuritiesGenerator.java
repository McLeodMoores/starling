package com.mcleodmoores.examples.simulated.loader.securities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

public class SecuritiesGenerator {
  /** The security generator */
  private final SecurityGenerator<? extends ManageableSecurity> _securityGenerator;
  /** The number of securities to generate */
  private final int _numberOfSecurities;

  /**
   * @param securityGenerator The security generator, not null
   * @param numberOfSecurities The number of securities to generate
   */
  public SecuritiesGenerator(final SecurityGenerator<? extends ManageableSecurity> securityGenerator, final int numberOfSecurities) {
    ArgumentChecker.notNull(securityGenerator, "securityGenerator");
    _securityGenerator = securityGenerator;
    _numberOfSecurities = numberOfSecurities;
  }

  /**
   * Generates a list of securities. This method attempts to generate the number of {{@link #_numberOfSecurities}
   * without checking that this is possible. It is the responsibility of the underlying security generator to
   * perform this check.
   * @return A list of manageable securities
   */
  public List<ManageableSecurity> createManageableSecurities() {
    final List<ManageableSecurity> securities = new ArrayList<>();
    for (int i = 0; i < _numberOfSecurities; i++) {
      securities.add(_securityGenerator.createSecurity());
    }
    return securities;
  }

  /**
   * Gets the security generator.
   * @return The security generator
   */
  protected SecurityGenerator<? extends ManageableSecurity> getSecurityGenerator() {
    return _securityGenerator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _numberOfSecurities;
    result = prime * result + _securityGenerator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SecuritiesGenerator)) {
      return false;
    }
    final SecuritiesGenerator other = (SecuritiesGenerator) obj;
    if (_numberOfSecurities != other._numberOfSecurities) {
      return false;
    }
    if (!ObjectUtils.equals(_securityGenerator, other._securityGenerator)) {
      return false;
    }
    return true;
  }

}
