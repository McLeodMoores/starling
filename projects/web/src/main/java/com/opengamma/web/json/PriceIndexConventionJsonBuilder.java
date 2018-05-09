/**
 *
 */
package com.opengamma.web.json;

import java.util.Map;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert a {@link PriceIndexConvention} to JSON and back again.
 */
public final class PriceIndexConventionJsonBuilder extends ConventionJsonBuilder<PriceIndexConvention> {
  /**
   * Static instance.
   */
  public static final PriceIndexConventionJsonBuilder INSTANCE = new PriceIndexConventionJsonBuilder();

  @Override
  PriceIndexConvention fromJson(final String json, final Map<String, String> attributes) {
    final PriceIndexConvention convention = fromJSON(PriceIndexConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  PriceIndexConvention getCopy(final PriceIndexConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new PriceIndexConvention("", ExternalIdBundle.EMPTY, Currency.USD, ExternalSchemes.financialRegionId("US"),
        ExternalId.of("SCHEME", "VALUE")));
  }
  private PriceIndexConventionJsonBuilder() {
  }
}
