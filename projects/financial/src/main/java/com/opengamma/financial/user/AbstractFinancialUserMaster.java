/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;

/**
 * Base class for wrapping masters to trap calls to record user based information,
 * allowing clean up and hooks for access control logics if needed.
 *
 * @param <D>  the type of the document
 */
public abstract class AbstractFinancialUserMaster<D extends AbstractDocument> implements AbstractChangeProvidingMaster<D> {

  /**
   * The user name.
   */
  private final String _userName;
  /**
   * The client name.
   */
  private final String _clientName;
  /**
   * The tracker.
   */
  private final FinancialUserDataTracker _tracker;
  /**
   * The data type.
   */
  private final FinancialUserDataType _type;


  private void setupChangeListener() {
    changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(final ChangeEvent event) {
        if (event.getType().equals(ChangeType.REMOVED)) {
          _tracker.deleted(_userName, _clientName, _type, event.getObjectId());
        } else if (event.getType().equals(ChangeType.ADDED)) {
          _tracker.created(_userName, _clientName, _type, event.getObjectId());
        }
      }
    });
  }

  /**
   * Creates an instance.
   *
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @param tracker  the tracker, not null
   * @param type  the data type, not null
   */
  public AbstractFinancialUserMaster(final String userName, final String clientName, final FinancialUserDataTracker tracker, final FinancialUserDataType type) {
    _userName = userName;
    _clientName = clientName;
    _tracker = tracker;
    _type = type;
  }

  protected void init() {
    setupChangeListener();
  }

  /**
   * Creates an instance.
   *
   * @param client  the client, not null
   * @param type  the data type, not null
   */
  public AbstractFinancialUserMaster(final FinancialClient client, final FinancialUserDataType type) {
    _userName = client.getUserName();
    _clientName = client.getClientName();
    _tracker = client.getUserDataTracker();
    _type = type;
  }

  //-------------------------------------------------------------------------
  protected void created(final ObjectId oid) {
    _tracker.created(_userName, _clientName, _type, oid);
  }

  protected void deleted(final ObjectId oid) {
    _tracker.deleted(_userName, _clientName, _type, oid);
  }

}
