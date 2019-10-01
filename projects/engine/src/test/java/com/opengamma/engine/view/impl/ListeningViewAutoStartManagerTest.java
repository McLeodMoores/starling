/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewAutoStartManager;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ListeningViewAutoStartManagerTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSourceMustBeProvided() {
    new ListeningViewAutoStartManager(null);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testCannotGetViewsUntilStarted() {

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(createConfigSource(1));
    manager.getAutoStartViews();
  }

  @Test
  public void testSingleConfigItemLoadsOnStart() {

    final ConfigSource configSource = createConfigSource(1);

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(configSource);
    manager.initialize();

    assertThat(manager.getAutoStartViews().size(), is(1));
  }

  @Test
  public void testMultipleConfigItemsGetLoadedOnStart() {

    final ConfigSource configSource = createConfigSource(10);

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(configSource);
    manager.initialize();

    assertThat(manager.getAutoStartViews().size(), is(10));
  }

  @Test
  public void testDuplicateViewDefinitionsAreIgnored() {

    final ConfigSource configSource = createConfigSource(createConfigItems("id1", "id1"));

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(configSource);
    manager.initialize();

    checkViewIds(manager.getAutoStartViews(), "id1");
  }

  @Test
  public void testAfterItemsAreRemovedFromDatabaseTheyAreRemovedFromAutoStartList() {

    final TestConfigSource configSource = createConfigSource(createConfigItems("id1", "id2"));

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(configSource);
    manager.initialize();

    checkViewIds(manager.getAutoStartViews(), "id1", "id2");

    configSource.removeItemWithTrigger(createConfigItem("id1"));
    checkViewIds(manager.getAutoStartViews(), "id2");
  }

  @Test
  public void testAfterItemsAreAddedToDatabaseTheyAreAddedToAutoStartList() {

    final TestConfigSource configSource = createConfigSource(createConfigItems("id1", "id2"));

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(configSource);
    manager.initialize();

    checkViewIds(manager.getAutoStartViews(), "id1", "id2");

    configSource.addItemWithTrigger(createConfigItem("id3"));
    checkViewIds(manager.getAutoStartViews(), "id1", "id2", "id3");
  }

  @Test
  public void testModifiedItemsAreAddedToAutoStartList() {

    final TestConfigSource configSource = createConfigSource(createConfigItems("id1", "id2"));

    final ViewAutoStartManager manager = new ListeningViewAutoStartManager(configSource);
    manager.initialize();

    checkViewIds(manager.getAutoStartViews(), "id1", "id2");

    configSource.modifyItemWithTrigger(createConfigItem("id2", "differentId"));
    checkViewIds(manager.getAutoStartViews(), "id1", "differentId");
  }

  private void checkViewIds(final Map<String, AutoStartViewDefinition> views, final String... ids) {

    final Set<UniqueId> expected = new HashSet<>();
    for (final String id : ids) {
      expected.add(UniqueId.of("VD", id));
    }

    final Set<UniqueId> checked = new HashSet<>();
    assertThat(views.size(), is(ids.length));
    for (final AutoStartViewDefinition view : views.values()) {
      final UniqueId uniqueId = view.getViewDefinitionId();
      assertThat(expected.contains(uniqueId), is(true));
      checked.add(uniqueId);
    }

    assertThat(checked, is(expected));
  }

  private TestConfigSource createConfigSource(final int qty) {
    return createConfigSource(createConfigItems(qty));
  }

  private TestConfigSource createConfigSource(final List<ConfigItem<AutoStartViewDefinition>> configItems) {
    return new TestConfigSource(configItems);
  }

  private List<ConfigItem<AutoStartViewDefinition>> createConfigItems(final int qty) {

    final String[] ids = new String[qty];
    for (int i = 0; i < qty; i++) {
      ids[i] = "id-" + i;
    }
    return createConfigItems(ids);
  }

  private List<ConfigItem<AutoStartViewDefinition>> createConfigItems(final String... ids) {

    final List<ConfigItem<AutoStartViewDefinition>> result = new ArrayList<>();
    for (final String id : ids) {
      result.add(createConfigItem(id));
    }
    return result;
  }

  private ConfigItem<AutoStartViewDefinition> createConfigItem(final String id) {
    return createConfigItem(id, id);
  }

  private ConfigItem<AutoStartViewDefinition> createConfigItem(final String id, final String viewId) {
    final ConfigItem<AutoStartViewDefinition> configItem = ConfigItem.of(createAutoStartViewDefinition(viewId));
    configItem.setUniqueId(UniqueId.of("CI", id));
    configItem.setName("Item-" + id);
    return configItem;
  }

  private AutoStartViewDefinition createAutoStartViewDefinition(final String id) {
    return new AutoStartViewDefinition(UniqueId.of("VD", id), ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(),
        EnumSet.of(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE)));
  }

  private static class TestConfigSource implements ConfigSource {

    private final BasicChangeManager _changeManager = new BasicChangeManager();
    private final Map<ObjectId, ConfigItem<AutoStartViewDefinition>> _views = new HashMap<>();

    TestConfigSource(final List<ConfigItem<AutoStartViewDefinition>> configItems) {
      for (final ConfigItem<AutoStartViewDefinition> item : configItems) {
        addItem(item);
      }
    }

    @Override
    public ConfigItem<?> get(final UniqueId uniqueId) {
      return null;
    }

    @Override
    public ConfigItem<?> get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      return _views.get(objectId);
    }

    @Override
    public Map<UniqueId, ConfigItem<?>> get(final Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, ConfigItem<?>> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Collection<ConfigItem<R>> get(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {

      final Collection<ConfigItem<R>> result = new HashSet<>();
      if (clazz == AutoStartViewDefinition.class) {
        for (final ConfigItem<AutoStartViewDefinition> item : _views.values()) {

          result.add((ConfigItem<R>) item);
        }
      }
      return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Collection<ConfigItem<R>> getAll(final Class<R> clazz, final VersionCorrection versionCorrection) {

      final Collection<ConfigItem<R>> result = new HashSet<>();
      if (clazz == AutoStartViewDefinition.class) {
        for (final ConfigItem<AutoStartViewDefinition> item : _views.values()) {
          result.add((ConfigItem<R>) item);
        }
      }
      return result;
    }

    @Override
    public <R> R getConfig(final Class<R> clazz, final UniqueId uniqueId) {
      return null;
    }

    @Override
    public <R> R getConfig(final Class<R> clazz, final ObjectId objectId, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public <R> R getSingle(final Class<R> clazz, final String configName, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public <R> R getLatestByName(final Class<R> clazz, final String name) {
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return _changeManager;
    }

    private void addItem(final ConfigItem<AutoStartViewDefinition> item) {
      _views.put(item.getObjectId(), item);
    }

    public void addItemWithTrigger(final ConfigItem<AutoStartViewDefinition> item) {
      addItem(item);
      _changeManager.entityChanged(ChangeType.ADDED, item.getObjectId(), Instant.now(), null, Instant.now());
    }

    public void removeItemWithTrigger(final ConfigItem<AutoStartViewDefinition> item) {
      final ObjectId objectId = item.getObjectId();
      _views.remove(objectId);
      _changeManager.entityChanged(ChangeType.REMOVED, objectId, Instant.now(), null, Instant.now());
    }

    public void modifyItemWithTrigger(final ConfigItem<AutoStartViewDefinition> item) {
      addItem(item);
      _changeManager.entityChanged(ChangeType.CHANGED, item.getObjectId(), Instant.now(), null, Instant.now());
    }
  }
}
