package com.mcleodmoores.starling.client.marketdata;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Created by jim on 08/06/15.
 */
public class MarketDataKeyTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    MarketDataKey.of(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNulls() {
    MarketDataKey.of(null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    MarketDataKey.of(ExternalIdBundle.EMPTY, null);
  }

  @Test
  public void testOf() throws Exception {
    final MarketDataKey key = MarketDataKey.of(ExternalIdBundle.EMPTY);
    Assert.assertNotNull(key);
    Assert.assertNotNull(key.getExternalIdBundle());
    Assert.assertEquals(ExternalIdBundle.EMPTY, key.getExternalIdBundle());
    Assert.assertNotNull(key.getField());
    Assert.assertEquals(DataField.PRICE, key.getField());
    Assert.assertNotNull(key.getNormalizer());
    Assert.assertEquals(UnitNormalizer.INSTANCE.getName(), key.getNormalizer());
    Assert.assertNotNull(key.getProvider());
    Assert.assertEquals(DataProvider.DEFAULT, key.getProvider());
    Assert.assertNotNull(key.getSource());
    Assert.assertEquals(DataSource.DEFAULT, key.getSource());
  }

  @Test
  public void testOf1() throws Exception {
    final MarketDataKey key = MarketDataKey.of(ExternalIdBundle.EMPTY, DataField.of("LAST_PRICE"));
    Assert.assertNotNull(key);
    Assert.assertNotNull(key.getExternalIdBundle());
    Assert.assertEquals(ExternalIdBundle.EMPTY, key.getExternalIdBundle());
    Assert.assertNotNull(key.getField());
    Assert.assertEquals(DataField.of("LAST_PRICE"), key.getField());
    Assert.assertNotNull(key.getNormalizer());
    Assert.assertEquals(UnitNormalizer.INSTANCE.getName(), key.getNormalizer());
    Assert.assertNotNull(key.getProvider());
    Assert.assertEquals(DataProvider.DEFAULT, key.getProvider());
    Assert.assertNotNull(key.getSource());
    Assert.assertEquals(DataSource.DEFAULT, key.getSource());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderNull() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.build();
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderExternalIdNull() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(null);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderFieldNull() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(ExternalIdBundle.EMPTY);
    builder.field(null);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderProviderNull() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(ExternalIdBundle.EMPTY);
    builder.provider(null);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderSourceNull() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(ExternalIdBundle.EMPTY);
    builder.source(null);
    builder.build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBuilderNormalizerNull() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(ExternalIdBundle.EMPTY);
    builder.normalizer(null);
    builder.build();
  }

  public void testBuilderDefaults() {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(ExternalIdBundle.EMPTY);
    final MarketDataKey key = builder.build();
    Assert.assertEquals(key.getExternalIdBundle(), ExternalIdBundle.EMPTY);
    Assert.assertEquals(key.getField(), DataField.PRICE);
    Assert.assertEquals(key.getProvider(), DataProvider.DEFAULT);
    Assert.assertEquals(key.getSource(), DataSource.DEFAULT);
    Assert.assertEquals(key.getNormalizer(), UnitNormalizer.INSTANCE);
  }

  @Test
  public void testToBuilder() throws Exception {
    final MarketDataKey.Builder builder = MarketDataKey.builder();
    builder.externalIdBundle(ExternalIdBundle.EMPTY);
    MarketDataKey key = builder.build();
    key = key.toBuilder()
        .provider(DataProvider.of("PROVIDER"))
        .source(DataSource.of("SOURCE"))
        .field(DataField.of("FIELD"))
        .externalIdBundle(ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID"))).build();
    Assert.assertEquals(key.getExternalIdBundle(), ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID")));
    Assert.assertEquals(key.getField(), DataField.of("FIELD"));
    Assert.assertEquals(key.getProvider(), DataProvider.of("PROVIDER"));
    Assert.assertEquals(key.getSource(), DataSource.of("SOURCE"));
    Assert.assertEquals(key.getNormalizer(), UnitNormalizer.INSTANCE.getName());
  }

  @Test
  public void testEquals() throws Exception {
    final MarketDataKey key1 = MarketDataKey.builder().externalIdBundle(ExternalIdBundle.EMPTY).build();
    Assert.assertEquals(key1, key1);

    final MarketDataKey key1a = MarketDataKey.builder().externalIdBundle(ExternalIdBundle.EMPTY).build();
    Assert.assertEquals(key1a, key1);
    Assert.assertEquals(key1, key1a);

    final MarketDataKey key2 = MarketDataKey.builder()
        .provider(DataProvider.of("PROVIDER"))
        .source(DataSource.of("SOURCE"))
        .field(DataField.of("FIELD"))
        .externalIdBundle(ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID"))).build();
    Assert.assertEquals(key2, key2);

    final MarketDataKey key2a = MarketDataKey.builder()
        .provider(DataProvider.of("PROVIDER"))
        .source(DataSource.of("SOURCE"))
        .field(DataField.of("FIELD"))
        .externalIdBundle(ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID"))).build();
    Assert.assertEquals(key2a, key2);
    Assert.assertEquals(key2, key2a);

    Assert.assertNotEquals(key1, key2);
    Assert.assertNotEquals(key2, key2.toBuilder().externalIdBundle(ExternalIdBundle.EMPTY).build());
    Assert.assertNotEquals(key2, key2.toBuilder().field(DataField.PRICE).build());
    Assert.assertNotEquals(key2, key2.toBuilder().source(DataSource.DEFAULT).build());
    Assert.assertNotEquals(key2, key2.toBuilder().provider(DataProvider.DEFAULT).build());
    Assert.assertNotEquals(key2, key2.toBuilder().normalizer(Div100Normalizer.INSTANCE.getName()).build());
    Assert.assertNotEquals(key2, null);
    Assert.assertNotEquals(key2, new Object());
  }

  @Test
  public void testHashCode() throws Exception {
    final MarketDataKey key1 = MarketDataKey.builder().externalIdBundle(ExternalIdBundle.EMPTY).build();
    Assert.assertEquals(key1.hashCode(), key1.hashCode());

    final MarketDataKey key1a = MarketDataKey.builder().externalIdBundle(ExternalIdBundle.EMPTY).build();
    Assert.assertEquals(key1a.hashCode(), key1.hashCode());

    final MarketDataKey key2 = MarketDataKey.builder()
        .provider(DataProvider.of("PROVIDER"))
        .source(DataSource.of("SOURCE"))
        .field(DataField.of("FIELD"))
        .externalIdBundle(ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID"))).build();
    Assert.assertEquals(key2.hashCode(), key2.hashCode());

    final MarketDataKey key2a = MarketDataKey.builder()
        .provider(DataProvider.of("PROVIDER"))
        .source(DataSource.of("SOURCE"))
        .field(DataField.of("FIELD"))
        .externalIdBundle(ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID"))).build();
    Assert.assertEquals(key2.hashCode(), key2a.hashCode());
  }

  @Test
  public void testToString() throws Exception {
    final MarketDataKey key = MarketDataKey.builder()
        .provider(DataProvider.of("PROVIDER"))
        .source(DataSource.of("SOURCE"))
        .field(DataField.of("FIELD"))
        .externalIdBundle(ExternalIdBundle.of(ExternalId.of("EXTERNAL", "ID"))).build();
    Assert.assertEquals(key.toString(), "MarketDataKey{externalIdBundle=Bundle[EXTERNAL~ID], field=FIELD, source=SOURCE, provider=PROVIDER, normalizer=UnitNormalizer}");
    final MarketDataKey key1 = MarketDataKey.builder().externalIdBundle(ExternalIdBundle.EMPTY).build();
    Assert.assertEquals(key1.toString(), "MarketDataKey{externalIdBundle=Bundle[], field=Market_Value, source=DEFAULT, provider=DEFAULT, normalizer=UnitNormalizer}");
  }
}