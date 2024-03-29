/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.infrastructure;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractAliasedComponentFactory;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.HibernateMappingFiles;

/**
 * Component factory for a database connector.
 * <p>
 * This class is designed to allow protected methods to be overridden.
 */
@BeanDefinition
public class DbConnectorComponentFactory extends AbstractAliasedComponentFactory {

  /**
   * The data source.
   */
  @PropertyDefinition(validate = "notNull")
  private DataSource _dataSource;
  /**
   * The name of the connector, defaults to the classifier.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The database dialect helper class.
   */
  @PropertyDefinition(validate = "notNull")
  private String _dialect;
  /**
   * The name of the Hibernate mapping files implementation.
   */
  @PropertyDefinition
  private String _hibernateMappingFiles;
  /**
   * Indicates whether Hibernate should output its SQL.
   */
  @PropertyDefinition
  private boolean _hibernateShowSql;
  /**
   * Indicates whether a Hibernate session should be bound to a thread.
   */
  @PropertyDefinition
  private boolean _allowHibernateThreadBoundSession;
  /**
   * The transaction isolation level.
   */
  @PropertyDefinition
  private String _transactionIsolationLevel;
  /**
   * The transaction propagation behavior.
   */
  @PropertyDefinition
  private String _transactionPropagationBehavior;
  /**
   * The transaction timeout in seconds.
   */
  @PropertyDefinition
  private int _transactionTimeout;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    if (getName() == null) {
      setName(getClassifier());
    }
    initDbConnector(repo);
  }

  /**
   * Creates and registers the database connector.
   *
   * @param repo  the component repository, not null
   * @return the connector, not null
   */
  protected DbConnector initDbConnector(final ComponentRepository repo) {
    final DbConnector dbConnector = createDbConnector(repo);
    registerComponentAndAliases(repo, DbConnector.class, dbConnector);
    return dbConnector;
  }

  /**
   * Creates the database connector, without registering it.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the connector, not null
   */
  protected DbConnector createDbConnector(final ComponentRepository repo) {
    final DbDialect dialect = createDialect(repo);
    final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
    final SessionFactory hibernateSessionFactory = createHibernateSessionFactory(repo, dialect);
    final HibernateTemplate hibernateTemplate = createHibernateTemplate(repo, hibernateSessionFactory);
    final TransactionTemplate transactionTemplate = createTransactionTemplate(repo, hibernateSessionFactory);
    return new DbConnector(getName(), dialect, getDataSource(), jdbcTemplate, hibernateTemplate, transactionTemplate);
  }

  /**
   * Creates the database dialect.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the dialect, not null
   */
  protected DbDialect createDialect(final ComponentRepository repo) {
    try {
      return (DbDialect) getClass().getClassLoader().loadClass(getDialect()).newInstance();
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Unable to create database dialect from class: " + getDialect(), ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the Hibernate session factory.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @param dialect  the dialect instance, not null
   * @return the session factory, may be null
   */
  protected SessionFactory createHibernateSessionFactory(final ComponentRepository repo, final DbDialect dialect) {
    if (getHibernateMappingFiles() == null) {
      return null; // Hibernate not required
    }
    final LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
    factory.setMappingResources(getHibernateMappingResources(repo));
    factory.setDataSource(getDataSource());
    final Properties props = new Properties();
    props.setProperty("hibernate.dialect", dialect.getHibernateDialect().getClass().getName());
    props.setProperty("hibernate.show_sql", String.valueOf(isHibernateShowSql()));
    props.setProperty("hibernate.connection.release_mode", "on_close");
    if (isAllowHibernateThreadBoundSession()) {
      props.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
      props.setProperty(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY,
          "org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl");
    }
    factory.setHibernateProperties(props);
    //factory.setLobHandler(dialect.getLobHandler());
    try {
      factory.afterPropertiesSet();
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
    return factory.getObject();
  }

  /**
   * Creates the complete list of Hibernate configuration files.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the set of Hibernate files, not null
   */
  protected String[] getHibernateMappingResources(final ComponentRepository repo) {
    final String hibernateMappingFilesClassName = getHibernateMappingFiles();
    Class<?> hibernateMappingFilesClass;
    try {
      hibernateMappingFilesClass = getClass().getClassLoader().loadClass(hibernateMappingFilesClassName);
    } catch (final ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not find Hibernate mapping files implementation: " + hibernateMappingFilesClassName, ex);
    }
    HibernateMappingFiles hibernateMappingFiles;
    try {
      hibernateMappingFiles = (HibernateMappingFiles) hibernateMappingFilesClass.newInstance();
    } catch (final InstantiationException ex) {
      throw new OpenGammaRuntimeException("Could not instantiate Hibernate mapping files implementation: " + hibernateMappingFilesClassName, ex);
    } catch (final IllegalAccessException ex) {
      throw new OpenGammaRuntimeException("Could not access Hibernate mapping files implementation: " + hibernateMappingFilesClassName, ex);
    }
    final Set<String> config = new HashSet<>();
    for (final Class<?> cls : hibernateMappingFiles.getHibernateMappingFiles()) {
      final String hbm = cls.getName().replace('.', '/') + ".hbm.xml";
      config.add(hbm);
    }
    return config.toArray(new String[config.size()]);
  }

  /**
   * Creates the Hibernate template, using the session factory.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @param sessionFactory  the Hibernate session factory, may be null
   * @return the Hibernate template, not null
   */
  protected HibernateTemplate createHibernateTemplate(final ComponentRepository repo, final SessionFactory sessionFactory) {
    if (sessionFactory == null) {
      return null;
    }
    return new HibernateTemplate(sessionFactory);
  }

  /**
   * Creates the transaction template.
   * <p>
   * This is Hibernate aware if Hibernate is available.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @param hibernateSessionFactory  the session factory, not null
   * @return the template, not null
   */
  protected TransactionTemplate createTransactionTemplate(final ComponentRepository repo, final SessionFactory hibernateSessionFactory) {
    final DefaultTransactionDefinition transactionDef = new DefaultTransactionDefinition();
    transactionDef.setName(getName());
    if (getTransactionIsolationLevel() != null) {
      transactionDef.setIsolationLevelName(getTransactionIsolationLevel());
    }
    if (getTransactionPropagationBehavior() != null) {
      transactionDef.setPropagationBehaviorName(getTransactionPropagationBehavior());
    }
    if (getTransactionTimeout() != 0) {
      transactionDef.setTimeout(getTransactionTimeout());
    }
    return new TransactionTemplate(createTransactionManager(repo, hibernateSessionFactory), transactionDef);
  }

  /**
   * Creates the transaction manager.
   * <p>
   * This is Hibernate aware if Hibernate is available.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @param hibernateSessionFactory  the Hibernate session factory, may be null
   * @return the transaction manager, not null
   */
  protected PlatformTransactionManager createTransactionManager(final ComponentRepository repo, final SessionFactory hibernateSessionFactory) {
    AbstractPlatformTransactionManager newTransMgr;
    if (hibernateSessionFactory == null) {
      newTransMgr = new DataSourceTransactionManager(getDataSource());
    } else {
      newTransMgr = new HibernateTransactionManager(hibernateSessionFactory);
    }
    newTransMgr.setNestedTransactionAllowed(true);
    return newTransMgr;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbConnectorComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbConnectorComponentFactory.Meta meta() {
    return DbConnectorComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbConnectorComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbConnectorComponentFactory.Meta metaBean() {
    return DbConnectorComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data source.
   * @return the value of the property, not null
   */
  public DataSource getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the data source.
   * @param dataSource  the new value of the property, not null
   */
  public void setDataSource(DataSource dataSource) {
    JodaBeanUtils.notNull(dataSource, "dataSource");
    this._dataSource = dataSource;
  }

  /**
   * Gets the the {@code dataSource} property.
   * @return the property, not null
   */
  public final Property<DataSource> dataSource() {
    return metaBean().dataSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the connector, defaults to the classifier.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the connector, defaults to the classifier.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database dialect helper class.
   * @return the value of the property, not null
   */
  public String getDialect() {
    return _dialect;
  }

  /**
   * Sets the database dialect helper class.
   * @param dialect  the new value of the property, not null
   */
  public void setDialect(String dialect) {
    JodaBeanUtils.notNull(dialect, "dialect");
    this._dialect = dialect;
  }

  /**
   * Gets the the {@code dialect} property.
   * @return the property, not null
   */
  public final Property<String> dialect() {
    return metaBean().dialect().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the Hibernate mapping files implementation.
   * @return the value of the property
   */
  public String getHibernateMappingFiles() {
    return _hibernateMappingFiles;
  }

  /**
   * Sets the name of the Hibernate mapping files implementation.
   * @param hibernateMappingFiles  the new value of the property
   */
  public void setHibernateMappingFiles(String hibernateMappingFiles) {
    this._hibernateMappingFiles = hibernateMappingFiles;
  }

  /**
   * Gets the the {@code hibernateMappingFiles} property.
   * @return the property, not null
   */
  public final Property<String> hibernateMappingFiles() {
    return metaBean().hibernateMappingFiles().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets indicates whether Hibernate should output its SQL.
   * @return the value of the property
   */
  public boolean isHibernateShowSql() {
    return _hibernateShowSql;
  }

  /**
   * Sets indicates whether Hibernate should output its SQL.
   * @param hibernateShowSql  the new value of the property
   */
  public void setHibernateShowSql(boolean hibernateShowSql) {
    this._hibernateShowSql = hibernateShowSql;
  }

  /**
   * Gets the the {@code hibernateShowSql} property.
   * @return the property, not null
   */
  public final Property<Boolean> hibernateShowSql() {
    return metaBean().hibernateShowSql().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets indicates whether a Hibernate session should be bound to a thread.
   * @return the value of the property
   */
  public boolean isAllowHibernateThreadBoundSession() {
    return _allowHibernateThreadBoundSession;
  }

  /**
   * Sets indicates whether a Hibernate session should be bound to a thread.
   * @param allowHibernateThreadBoundSession  the new value of the property
   */
  public void setAllowHibernateThreadBoundSession(boolean allowHibernateThreadBoundSession) {
    this._allowHibernateThreadBoundSession = allowHibernateThreadBoundSession;
  }

  /**
   * Gets the the {@code allowHibernateThreadBoundSession} property.
   * @return the property, not null
   */
  public final Property<Boolean> allowHibernateThreadBoundSession() {
    return metaBean().allowHibernateThreadBoundSession().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the transaction isolation level.
   * @return the value of the property
   */
  public String getTransactionIsolationLevel() {
    return _transactionIsolationLevel;
  }

  /**
   * Sets the transaction isolation level.
   * @param transactionIsolationLevel  the new value of the property
   */
  public void setTransactionIsolationLevel(String transactionIsolationLevel) {
    this._transactionIsolationLevel = transactionIsolationLevel;
  }

  /**
   * Gets the the {@code transactionIsolationLevel} property.
   * @return the property, not null
   */
  public final Property<String> transactionIsolationLevel() {
    return metaBean().transactionIsolationLevel().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the transaction propagation behavior.
   * @return the value of the property
   */
  public String getTransactionPropagationBehavior() {
    return _transactionPropagationBehavior;
  }

  /**
   * Sets the transaction propagation behavior.
   * @param transactionPropagationBehavior  the new value of the property
   */
  public void setTransactionPropagationBehavior(String transactionPropagationBehavior) {
    this._transactionPropagationBehavior = transactionPropagationBehavior;
  }

  /**
   * Gets the the {@code transactionPropagationBehavior} property.
   * @return the property, not null
   */
  public final Property<String> transactionPropagationBehavior() {
    return metaBean().transactionPropagationBehavior().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the transaction timeout in seconds.
   * @return the value of the property
   */
  public int getTransactionTimeout() {
    return _transactionTimeout;
  }

  /**
   * Sets the transaction timeout in seconds.
   * @param transactionTimeout  the new value of the property
   */
  public void setTransactionTimeout(int transactionTimeout) {
    this._transactionTimeout = transactionTimeout;
  }

  /**
   * Gets the the {@code transactionTimeout} property.
   * @return the property, not null
   */
  public final Property<Integer> transactionTimeout() {
    return metaBean().transactionTimeout().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DbConnectorComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbConnectorComponentFactory other = (DbConnectorComponentFactory) obj;
      return JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getDialect(), other.getDialect()) &&
          JodaBeanUtils.equal(getHibernateMappingFiles(), other.getHibernateMappingFiles()) &&
          (isHibernateShowSql() == other.isHibernateShowSql()) &&
          (isAllowHibernateThreadBoundSession() == other.isAllowHibernateThreadBoundSession()) &&
          JodaBeanUtils.equal(getTransactionIsolationLevel(), other.getTransactionIsolationLevel()) &&
          JodaBeanUtils.equal(getTransactionPropagationBehavior(), other.getTransactionPropagationBehavior()) &&
          (getTransactionTimeout() == other.getTransactionTimeout()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDialect());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHibernateMappingFiles());
    hash = hash * 31 + JodaBeanUtils.hashCode(isHibernateShowSql());
    hash = hash * 31 + JodaBeanUtils.hashCode(isAllowHibernateThreadBoundSession());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTransactionIsolationLevel());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTransactionPropagationBehavior());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTransactionTimeout());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("DbConnectorComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("dataSource").append('=').append(JodaBeanUtils.toString(getDataSource())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("dialect").append('=').append(JodaBeanUtils.toString(getDialect())).append(',').append(' ');
    buf.append("hibernateMappingFiles").append('=').append(JodaBeanUtils.toString(getHibernateMappingFiles())).append(',').append(' ');
    buf.append("hibernateShowSql").append('=').append(JodaBeanUtils.toString(isHibernateShowSql())).append(',').append(' ');
    buf.append("allowHibernateThreadBoundSession").append('=').append(JodaBeanUtils.toString(isAllowHibernateThreadBoundSession())).append(',').append(' ');
    buf.append("transactionIsolationLevel").append('=').append(JodaBeanUtils.toString(getTransactionIsolationLevel())).append(',').append(' ');
    buf.append("transactionPropagationBehavior").append('=').append(JodaBeanUtils.toString(getTransactionPropagationBehavior())).append(',').append(' ');
    buf.append("transactionTimeout").append('=').append(JodaBeanUtils.toString(getTransactionTimeout())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbConnectorComponentFactory}.
   */
  public static class Meta extends AbstractAliasedComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<DataSource> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", DbConnectorComponentFactory.class, DataSource.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", DbConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code dialect} property.
     */
    private final MetaProperty<String> _dialect = DirectMetaProperty.ofReadWrite(
        this, "dialect", DbConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code hibernateMappingFiles} property.
     */
    private final MetaProperty<String> _hibernateMappingFiles = DirectMetaProperty.ofReadWrite(
        this, "hibernateMappingFiles", DbConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code hibernateShowSql} property.
     */
    private final MetaProperty<Boolean> _hibernateShowSql = DirectMetaProperty.ofReadWrite(
        this, "hibernateShowSql", DbConnectorComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code allowHibernateThreadBoundSession} property.
     */
    private final MetaProperty<Boolean> _allowHibernateThreadBoundSession = DirectMetaProperty.ofReadWrite(
        this, "allowHibernateThreadBoundSession", DbConnectorComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code transactionIsolationLevel} property.
     */
    private final MetaProperty<String> _transactionIsolationLevel = DirectMetaProperty.ofReadWrite(
        this, "transactionIsolationLevel", DbConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code transactionPropagationBehavior} property.
     */
    private final MetaProperty<String> _transactionPropagationBehavior = DirectMetaProperty.ofReadWrite(
        this, "transactionPropagationBehavior", DbConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code transactionTimeout} property.
     */
    private final MetaProperty<Integer> _transactionTimeout = DirectMetaProperty.ofReadWrite(
        this, "transactionTimeout", DbConnectorComponentFactory.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "dataSource",
        "name",
        "dialect",
        "hibernateMappingFiles",
        "hibernateShowSql",
        "allowHibernateThreadBoundSession",
        "transactionIsolationLevel",
        "transactionPropagationBehavior",
        "transactionTimeout");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1272470629:  // dataSource
          return _dataSource;
        case 3373707:  // name
          return _name;
        case 1655014950:  // dialect
          return _dialect;
        case 1110639547:  // hibernateMappingFiles
          return _hibernateMappingFiles;
        case 257395935:  // hibernateShowSql
          return _hibernateShowSql;
        case 1850252619:  // allowHibernateThreadBoundSession
          return _allowHibernateThreadBoundSession;
        case 1321533396:  // transactionIsolationLevel
          return _transactionIsolationLevel;
        case 230249600:  // transactionPropagationBehavior
          return _transactionPropagationBehavior;
        case -1923367773:  // transactionTimeout
          return _transactionTimeout;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbConnectorComponentFactory> builder() {
      return new DirectBeanBuilder<DbConnectorComponentFactory>(new DbConnectorComponentFactory());
    }

    @Override
    public Class<? extends DbConnectorComponentFactory> beanType() {
      return DbConnectorComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dataSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DataSource> dataSource() {
      return _dataSource;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code dialect} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dialect() {
      return _dialect;
    }

    /**
     * The meta-property for the {@code hibernateMappingFiles} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> hibernateMappingFiles() {
      return _hibernateMappingFiles;
    }

    /**
     * The meta-property for the {@code hibernateShowSql} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> hibernateShowSql() {
      return _hibernateShowSql;
    }

    /**
     * The meta-property for the {@code allowHibernateThreadBoundSession} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> allowHibernateThreadBoundSession() {
      return _allowHibernateThreadBoundSession;
    }

    /**
     * The meta-property for the {@code transactionIsolationLevel} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> transactionIsolationLevel() {
      return _transactionIsolationLevel;
    }

    /**
     * The meta-property for the {@code transactionPropagationBehavior} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> transactionPropagationBehavior() {
      return _transactionPropagationBehavior;
    }

    /**
     * The meta-property for the {@code transactionTimeout} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> transactionTimeout() {
      return _transactionTimeout;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1272470629:  // dataSource
          return ((DbConnectorComponentFactory) bean).getDataSource();
        case 3373707:  // name
          return ((DbConnectorComponentFactory) bean).getName();
        case 1655014950:  // dialect
          return ((DbConnectorComponentFactory) bean).getDialect();
        case 1110639547:  // hibernateMappingFiles
          return ((DbConnectorComponentFactory) bean).getHibernateMappingFiles();
        case 257395935:  // hibernateShowSql
          return ((DbConnectorComponentFactory) bean).isHibernateShowSql();
        case 1850252619:  // allowHibernateThreadBoundSession
          return ((DbConnectorComponentFactory) bean).isAllowHibernateThreadBoundSession();
        case 1321533396:  // transactionIsolationLevel
          return ((DbConnectorComponentFactory) bean).getTransactionIsolationLevel();
        case 230249600:  // transactionPropagationBehavior
          return ((DbConnectorComponentFactory) bean).getTransactionPropagationBehavior();
        case -1923367773:  // transactionTimeout
          return ((DbConnectorComponentFactory) bean).getTransactionTimeout();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1272470629:  // dataSource
          ((DbConnectorComponentFactory) bean).setDataSource((DataSource) newValue);
          return;
        case 3373707:  // name
          ((DbConnectorComponentFactory) bean).setName((String) newValue);
          return;
        case 1655014950:  // dialect
          ((DbConnectorComponentFactory) bean).setDialect((String) newValue);
          return;
        case 1110639547:  // hibernateMappingFiles
          ((DbConnectorComponentFactory) bean).setHibernateMappingFiles((String) newValue);
          return;
        case 257395935:  // hibernateShowSql
          ((DbConnectorComponentFactory) bean).setHibernateShowSql((Boolean) newValue);
          return;
        case 1850252619:  // allowHibernateThreadBoundSession
          ((DbConnectorComponentFactory) bean).setAllowHibernateThreadBoundSession((Boolean) newValue);
          return;
        case 1321533396:  // transactionIsolationLevel
          ((DbConnectorComponentFactory) bean).setTransactionIsolationLevel((String) newValue);
          return;
        case 230249600:  // transactionPropagationBehavior
          ((DbConnectorComponentFactory) bean).setTransactionPropagationBehavior((String) newValue);
          return;
        case -1923367773:  // transactionTimeout
          ((DbConnectorComponentFactory) bean).setTransactionTimeout((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DbConnectorComponentFactory) bean)._dataSource, "dataSource");
      JodaBeanUtils.notNull(((DbConnectorComponentFactory) bean)._dialect, "dialect");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
