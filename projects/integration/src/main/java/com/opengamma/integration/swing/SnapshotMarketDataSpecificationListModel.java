/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingWorker;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.tuple.Pair;

/**
 * List/ComboBox model for historical market data specifications.
 */
public class SnapshotMarketDataSpecificationListModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private static final long serialVersionUID = 1L;
  private List<String> _names = Collections.emptyList();
  private List<ObjectId> _objectIds = Collections.emptyList();
  private Object _selected;

  public SnapshotMarketDataSpecificationListModel(final MarketDataSnapshotMaster snapshotMaster) {
    final SwingWorker<List<Pair<String, ObjectId>>, Object> worker = new SwingWorker<List<Pair<String, ObjectId>>, Object>() {

      @Override
      protected List<Pair<String, ObjectId>> doInBackground() throws Exception {
        final List<Pair<String, ObjectId>> resolverNames = new ArrayList<>();
        final MarketDataSnapshotSearchRequest searchRequest = new MarketDataSnapshotSearchRequest();
        searchRequest.setIncludeData(false);
        final MarketDataSnapshotSearchResult searchResults = snapshotMaster.search(searchRequest);
        for (final MarketDataSnapshotDocument item : searchResults.getDocuments()) {
          resolverNames.add(Pair.of(item.getName(), item.getObjectId()));
        }
        return resolverNames;
      }

      @Override
      protected void done() {
        try {
          final List<Pair<String, ObjectId>> list = get();
          final List<String> names = new ArrayList<>();
          final List<ObjectId> objectIds = new ArrayList<>();
          // unpack - a bit icky, but I'd prefer to atomically swap out the list in case of multiple threads reading _names;
          for (final Pair<String, ObjectId> pair : list) {
            names.add(pair.getFirst());
            objectIds.add(pair.getSecond());
          }
          synchronized (this) {
            _names = names;
            _objectIds = objectIds;
          }
          fireIntervalAdded(SnapshotMarketDataSpecificationListModel.this, 0, _names.size() - 1);
        } catch (final InterruptedException ex) {
          throw new OpenGammaRuntimeException("InterruptedException retreiving available market data specifications", ex);
        } catch (final ExecutionException ex) {
          throw new OpenGammaRuntimeException("ExecutionException retreiving available market data specifications", ex);
        }
      }
    };
    worker.execute();
  }

  @Override
  public synchronized int getSize() {
    return _names.size();
  }

  @Override
  public synchronized String getElementAt(final int index) {
    return _names.get(index);
  }

  public synchronized ObjectId getObjectIdAt(final int index) {
    return _objectIds.get(index);
  }

  @Override
  public void setSelectedItem(final Object anItem) {
    _selected = anItem;
  }

  @Override
  public Object getSelectedItem() {
    return _selected;
  }

}
