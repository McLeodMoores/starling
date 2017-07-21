package com.mcleodmoores.examples.simulated.loader.securities;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.test.TestGroup;

/**
 *
 * Tests {@link SecuritiesGenerator}.
 */
@Test(groups = TestGroup.UNIT)
public class SecuritiesGeneratorTest {

  /**
   * Tests for failure in the case that the security generator is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator() {
    new SecuritiesGenerator(null, 10);
  }

  /**
   * Tests failure when too many securities are requested.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void testTooManySecurities() {
    final int maxSecurities = 10;
    final SecuritiesGenerator generator = new SecuritiesGenerator(new MySecurityGenerator(maxSecurities), maxSecurities + 10);
    generator.createManageableSecurities();
  }

  /**
   * Tests the list of generated securities.
   */
  @Test
  public void testGeneratedSecuritiesList() {
    final int maxSecurities = 10;
    SecuritiesGenerator generator = new SecuritiesGenerator(new MySecurityGenerator(maxSecurities), 0);
    List<ManageableSecurity> securities = generator.createManageableSecurities();
    assertTrue(securities.isEmpty());
    generator = new SecuritiesGenerator(new MySecurityGenerator(maxSecurities), maxSecurities);
    securities = generator.createManageableSecurities();
    assertEquals(10, securities.size());
    int i = 0;
    for (final ManageableSecurity security : securities) {
      assertTrue(security instanceof RawSecurity);
      final RawSecurity rawSecurity = (RawSecurity) security;
      assertTrue(rawSecurity.getAttributes().isEmpty());
      assertTrue(rawSecurity.getExternalIdBundle().isEmpty());
      assertEquals(0, rawSecurity.getRawData().length);
      assertNull(rawSecurity.getUniqueId());
      assertEquals("SECURITY" + i++, rawSecurity.getName());
    }
  }

  /**
   * Testing class that creates a list of {@link RawSecurity} that only hold a name.
   */
  private static class MySecurityGenerator extends SecurityGenerator<RawSecurity> {
    /** The maximum number of securities that can be created */
    private final int _maxSecurities;
    /** The count of securities that have been created */
    private int _count;

    /**
     * @param maxSecurities The maximum number of securities
     */
    public MySecurityGenerator(final int maxSecurities) {
      _maxSecurities = maxSecurities;
    }

    @Override
    public RawSecurity createSecurity() {
      if (_count > _maxSecurities) {
        throw new IllegalStateException("Trying to create too many securities");
      }
      final RawSecurity security = new RawSecurity();
      security.setName("SECURITY" + _count++);
      return security;
    }

  }
}
