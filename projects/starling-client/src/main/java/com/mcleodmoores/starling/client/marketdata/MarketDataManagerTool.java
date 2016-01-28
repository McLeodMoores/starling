/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.marketdata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.concurrent.SynchronousQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class MarketDataManagerTool {
  private JFrame _frame;
  private final SynchronousQueue<Void> _endQueue = new SynchronousQueue<>();
  private JSplitPane _splitPane;

  public static void main(final String[] args) {
    try {
      new MarketDataManagerTool().doRun()/*.invokeAndTerminate(args)*/;
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

//  @Override
  protected void doRun() throws Exception {
    initialize();
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        try {
          _frame.pack();
          _frame.setVisible(true);
          _frame.addWindowStateListener(new WindowStateListener() {

            @Override
            public void windowStateChanged(final WindowEvent e) {
              if (e.getNewState() == WindowEvent.WINDOW_CLOSED) {
                _endQueue.add(null);
              }
            }
          });
          _splitPane.setDividerLocation(0.3d);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }

    });
    _endQueue.take();
  }

  /**
   * @wbp.parser.entryPoint
   */
  private void initialize() {
    _frame = new JFrame();
    _frame.setTitle("Market Data Manager");
    _frame.setPreferredSize(new Dimension(1000, 700));
    _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final JPanel mainPanel = new JPanel();
    _frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.setLayout(new BorderLayout());

    final JPanel viewSelectionPanel = new JPanel();
    final JPanel parametersPanel = new JPanel();
    final BoxLayout boxLayout = new BoxLayout(parametersPanel, BoxLayout.LINE_AXIS);
    parametersPanel.setLayout(boxLayout);
    parametersPanel.add(viewSelectionPanel, Box.createHorizontalGlue());

    final JPanel panel = new JPanel();
    mainPanel.add(panel, BorderLayout.CENTER);
    panel.setLayout(new BorderLayout(0, 0));

    _splitPane = new JSplitPane();
    panel.add(_splitPane);
  }
}
