/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.util.PublicSPI;

/**
 * Result providing the history of an exchange.
 * <p>
 * The returned documents may be a mixture of versions and corrections.
 * The document instant fields are used to identify which are which.
 * See {@link ExchangeHistoryRequest} for more details.
 */
@PublicSPI
@BeanDefinition
public class ExchangeHistoryResult extends AbstractHistoryResult<ExchangeDocument> {

  /**
   * Creates an instance.
   */
  public ExchangeHistoryResult() {
  }

  /**
   * Creates an instance.
   * 
   * @param coll  the collection of documents to add, not null
   */
  public ExchangeHistoryResult(Collection<ExchangeDocument> coll) {
    super(coll);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the returned exchanges from within the documents.
   * 
   * @return the exchanges, not null
   */
  public List<ManageableExchange> getExchanges() {
    List<ManageableExchange> result = new ArrayList<ManageableExchange>();
    if (getDocuments() != null) {
      for (ExchangeDocument doc : getDocuments()) {
        result.add(doc.getObject());
      }
    }
    return result;
  }

  /**
   * Gets the first exchange, or null if no documents.
   * 
   * @return the first exchange, null if none
   */
  public ManageableExchange getFirstExchange() {
    return getDocuments().size() > 0 ? getDocuments().get(0).getObject() : null;
  }

  /**
   * Gets the single result expected from a query.
   * <p>
   * This throws an exception if more than 1 result is actually available.
   * Thus, this method implies an assumption about uniqueness of the queried exchange.
   * 
   * @return the matching exchange, not null
   * @throws IllegalStateException if no exchange was found
   */
  public ManageableExchange getSingleExchange() {
    if (getDocuments().size() != 1) {
      throw new OpenGammaRuntimeException("Expecting zero or single resulting match, and was " + getDocuments().size());
    } else {
      return getDocuments().get(0).getObject();
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExchangeHistoryResult}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static ExchangeHistoryResult.Meta meta() {
    return ExchangeHistoryResult.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ExchangeHistoryResult.Meta.INSTANCE);
  }

  @Override
  public ExchangeHistoryResult.Meta metaBean() {
    return ExchangeHistoryResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExchangeHistoryResult}.
   */
  public static class Meta extends AbstractHistoryResult.Meta<ExchangeDocument> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends ExchangeHistoryResult> builder() {
      return new DirectBeanBuilder<ExchangeHistoryResult>(new ExchangeHistoryResult());
    }

    @Override
    public Class<? extends ExchangeHistoryResult> beanType() {
      return ExchangeHistoryResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
