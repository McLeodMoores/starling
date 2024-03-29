/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import org.apache.shiro.authc.credential.PasswordService;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.user.UserMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful user resources.
 */
public abstract class AbstractWebUserResource
    extends AbstractPerRequestWebResource<WebUserData> {

  /**
   * HTML ftl directory.
   */
  protected static final String HTML_DIR = "users/html/";

  /**
   * Creates the resource.
   *
   * @param userMaster
   *          the user master, not null
   * @param passwordService
   *          the password service, not null
   */
  protected AbstractWebUserResource(final UserMaster userMaster, final PasswordService passwordService) {
    super(new WebUserData());
    ArgumentChecker.notNull(userMaster, "userMaster");
    ArgumentChecker.notNull(passwordService, "passwordService");
    data().setUserMaster(userMaster);
    data().setPasswordService(passwordService);
  }

  /**
   * Creates the resource.
   *
   * @param parent
   *          the parent resource, not null
   */
  protected AbstractWebUserResource(final AbstractWebUserResource parent) {
    super(parent);
  }

  // -------------------------------------------------------------------------
  /**
   * Creates the output root data.
   *
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    out.put("uris", new WebUserUris(data()));
    return out;
  }

}
