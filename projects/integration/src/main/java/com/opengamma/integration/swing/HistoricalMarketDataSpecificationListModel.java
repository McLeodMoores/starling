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
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;

/**
 * List/ComboBox model for historical market data specifications
 */
public class HistoricalMarketDataSpecificationListModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private static final long serialVersionUID = 1L;
  private List<String> _names = Collections.emptyList();
  private Object _selected;

  public HistoricalMarketDataSpecificationListModel(final ConfigMaster configMaster) {
    final SwingWorker<List<String>, Object> worker = new SwingWorker<List<String>, Object>() {

      @Override
      protected List<String> doInBackground() throws Exception {
        final List<String> resolverNames = new ArrayList<>();
        final ConfigSearchRequest<HistoricalTimeSeriesRating> configSearchRequest = new ConfigSearchRequest<>();
        configSearchRequest.setType(HistoricalTimeSeriesRating.class);
        final ConfigSearchResult<HistoricalTimeSeriesRating> searchResults = configMaster.search(configSearchRequest);
        for (final ConfigItem<HistoricalTimeSeriesRating> item : searchResults.getValues()) {
          resolverNames.add(item.getName());
        }
        return resolverNames;
      }

      @Override
      protected void done() {
        try {
          _names = get();
          fireIntervalAdded(HistoricalMarketDataSpecificationListModel.this, 0, _names.size() - 1);
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
  public int getSize() {
    return _names.size();
  }

  @Override
  public String getElementAt(final int index) {
    return _names.get(index);
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
