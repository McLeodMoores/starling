package com.opengamma.integration.tool.enginedebugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.integration.tool.enginedebugger.MarketDataSpecificationRowComponent.Action;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;

import net.miginfocom.swing.MigLayout;

/**
 *
 */
public class MarketDataDialog extends JDialog {
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataDialog.class);
  private final List<LiveDataMetaDataProvider> _liveDataMetaDataProvider;
  private final ConfigMaster _configMaster;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final JButton _cancelButton = new JButton("Cancel");
  private final JButton _okayButton = new JButton("OK");

  private final Map<MarketDataSpecificationRowComponent, Boolean> _validState = new HashMap<>();
  private final List<MarketDataSpecificationRowComponent> _components = new ArrayList<>();
  private volatile boolean _cancelled;

  public MarketDataDialog(final List<LiveDataMetaDataProvider> liveDataMetaDataProvider, final ConfigMaster configMaster, final MarketDataSnapshotMaster snapshotMaster) {
    super();
    setModalityType(ModalityType.APPLICATION_MODAL);
    _liveDataMetaDataProvider = liveDataMetaDataProvider;
    _configMaster = configMaster;
    _snapshotMaster = snapshotMaster;
    buildForm();
  }

  public void buildForm() {
    final MigLayout layout = new MigLayout();
    setLayout(layout);

    addRow(0);

    _okayButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        setVisible(false);
        dispose();
      }
    });
    _cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        _cancelled = true;
        setVisible(false);
        dispose();
      }
    });
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    final JPanel jPanel = new JPanel(new MigLayout("inset 15 0 -5 -5"));
    jPanel.add(_okayButton, "right");
    jPanel.add(_cancelButton, "right");
    add(jPanel, "span 2, right, wrap");
    pack();
  }

  private boolean validState() {
    boolean activate = true;
    for (final boolean good : _validState.values()) {
      activate = activate & good;
    }
    return activate && !_cancelled;
  }

  private void checkOkayCancel() {
    _okayButton.setEnabled(validState());
  }

  private void addRow(final int index) {
    // + 1 because we're about to add it.
    final MarketDataSpecificationRowComponent rowComponent = new MarketDataSpecificationRowComponent(_liveDataMetaDataProvider, _configMaster, _snapshotMaster);
    // listen for changes in component's validity and record them in map
    final ChangeListener changeListener = new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final MarketDataSpecificationComponent component = (MarketDataSpecificationComponent) e.getSource();
        _validState.put(rowComponent, component.getCurrentState() != null);
        checkOkayCancel();
      }
    };
    _validState.put(rowComponent, false);
    rowComponent.addChangeListener(changeListener);
    rowComponent.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final Action command = MarketDataSpecificationRowComponent.Action.valueOf(e.getActionCommand());
        final MarketDataSpecificationRowComponent rowComponent = (MarketDataSpecificationRowComponent) e.getSource();
        final int index = _components.indexOf(rowComponent);
        switch (command) {
          case MOVE_UP:
            remove(rowComponent);
            add(rowComponent, "span 2, wrap", index - 1);
            _components.remove(rowComponent);
            _components.add(index - 1, rowComponent);
            break;
          case MOVE_DOWN:
            remove(rowComponent);
            add(rowComponent, "span 2, wrap", index + 1);
            _components.remove(rowComponent);
            _components.add(index + 1, rowComponent);
            break;
          case ADD:
            addRow(index + 1);
            break;
          case REMOVE:
            remove(rowComponent);
            rowComponent.removeActionListener(this);
            rowComponent.removeChangeListener(changeListener);
            _validState.remove(rowComponent);
            _components.remove(rowComponent);
            break;
        }
        checkButtons();
        pack();
        for (int i = 0; i < _components.size(); i++) {
          final MarketDataSpecificationRowComponent marketDataSpecificationRowComponent = _components.get(i);
          if (marketDataSpecificationRowComponent == null) {
            LOGGER.error("{} was null", i);
          } else {
            LOGGER.error("{} was {}", i, marketDataSpecificationRowComponent.getCurrentState());
          }
        }
      }
    });
    _components.add(index, rowComponent);
    checkButtons();
    checkOkayCancel();
    add(rowComponent, "span 2, wrap", index);
  }

  private void checkButtons() {
    int i = 0;
    for (final MarketDataSpecificationRowComponent component : _components) {
      component.checkButtons(i++, _components.size());
    }
  }

  public List<MarketDataSpecification> showDialog() {
    setVisible(true);
    if (validState()) {
      final List<MarketDataSpecification> specs = new ArrayList<>();
      for (final MarketDataSpecificationRowComponent component : _components) {
        specs.add(component.getCurrentState());
      }
      return specs;
    }
    return null;
  }


}
