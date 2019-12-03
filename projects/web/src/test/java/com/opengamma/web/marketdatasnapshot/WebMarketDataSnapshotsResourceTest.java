/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.MockHistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.SimpleFunctionConfigurationSource;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.live.InMemoryLKVLiveMarketDataProviderFactory;
import com.opengamma.engine.marketdata.live.LiveDataFactory;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.event.ViewProcessorEventListenerRegistry;
import com.opengamma.engine.view.impl.ViewProcessorInternal;
import com.opengamma.financial.analytics.volatility.cube.ConfigDBVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.test.TestLiveDataClient;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchSortOrder;
import com.opengamma.master.marketdatasnapshot.impl.InMemorySnapshotMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.MockUriInfo;
import com.opengamma.web.user.WebUser;

import freemarker.template.Configuration;

/**
 * Tests for {@link WebMarketDataSnapshotsResource}.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class WebMarketDataSnapshotsResourceTest {
  private WebMarketDataSnapshotsResource _snapshotResource;
  private static final ConfigMaster CONFIG_MASTER = new InMemoryConfigMaster();
  private static final LiveMarketDataProviderFactory PROVIDER_FACTORY;
  private static final ConfigSource CONFIG_SOURCE = new MasterConfigSource(CONFIG_MASTER);
  private static final ComputationTargetResolver RESOLVER = new MapComputationTargetResolver();
  private static final ViewProcessor PROCESSOR = new MockViewProcessor();
  private static final HistoricalTimeSeriesSource HTS_SOURCE = new MockHistoricalTimeSeriesSource();
  private static final VolatilityCubeDefinitionSource VOL_SOURCE = new ConfigDBVolatilityCubeDefinitionSource(CONFIG_SOURCE, VersionCorrection.LATEST);
  private static final String VIEW_DEFINITION_NAME = "view";
  static {
    final LiveDataClient liveDataClient = new TestLiveDataClient();
    final Collection<ExternalScheme> schemes = Collections.singleton(ExternalSchemes.OG_SYNTHETIC_TICKER);
    final Collection<String> names = Collections.singleton(MarketDataRequirementNames.CLOSE);
    final MarketDataAvailabilityFilter availabilityFilter = new DomainMarketDataAvailabilityFilter(schemes, names);
    final LiveDataFactory liveData = new LiveDataFactory(liveDataClient, availabilityFilter);
    PROVIDER_FACTORY = new InMemoryLKVLiveMarketDataProviderFactory(liveData,
        Collections.singletonMap("liveData", liveData));
    final ViewDefinition viewDefinition = new ViewDefinition(VIEW_DEFINITION_NAME, "");
    CONFIG_MASTER.add(new ConfigDocument(ConfigItem.of(viewDefinition)));
  }
  /**
   * Sets up an empty master and the web resource.
   */
  @BeforeMethod
  public void setUp() {
    final MarketDataSnapshotMaster snapshotMaster = new InMemorySnapshotMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    _snapshotResource = setUpResource(snapshotMaster, CONFIG_MASTER, PROVIDER_FACTORY, CONFIG_SOURCE, RESOLVER, PROCESSOR, HTS_SOURCE, VOL_SOURCE, uriInfo);
  }

  /**
   * Cleans up the web resource.
   */
  @AfterMethod
  public void cleanUp() {
    _snapshotResource = null;
  }

  /**
   * Tests the HTML GET response.
   */
  public void testHtmlGet() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String sort = "NAME ASC";
    final String name = "name";
    final List<String> ids = Arrays.asList(ExternalSchemes.OG_SYNTHETIC_TICKER + "~id1", ExternalSchemes.OG_SYNTHETIC_TICKER + "eid~id2");
    final UriInfo uriInfo = new MockUriInfo(true);
    final String response = _snapshotResource.getHTML(index, number, size, sort, name, ids, uriInfo);
    assertNotNull(response);
  }

  /**
   * Tests the HTML POST response. This always fails because the mock view
   * processor implementation cannot create a snapshot.
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testHtmlPostFails() {
    final String name = "name";
    final String valuationTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    final List<String> liveDataSources = Collections.singletonList("liveData");
    final List<String> tsResolverKeys = Collections.emptyList();
    final List<String> userSnapshotIds = Collections.emptyList();
    _snapshotResource.postHTML(name, VIEW_DEFINITION_NAME, valuationTime, tsResolverKeys, userSnapshotIds, liveDataSources, null);
  }

  /**
   * Tests the HTML POST response with bad valuation time.
   */
  public void testHtmlPostBadValuationTime() {
    final String name = "name";
    final List<String> liveDataSources = Collections.singletonList("liveData");
    final List<String> tsResolverKeys = Collections.emptyList();
    final List<String> userSnapshotIds = Collections.emptyList();
    final Response response = _snapshotResource.postHTML(name, VIEW_DEFINITION_NAME, "10:34 am", tsResolverKeys, userSnapshotIds, liveDataSources, null);
    assertEquals(response.getStatus(), 200);
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the HTML POST response with no view definition name.
   */
  public void testHtmlPostNullViewDefinitionName() {
    final String name = "name";
    final String valuationTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    final List<String> liveDataSources = Collections.singletonList("liveData");
    final List<String> tsResolverKeys = Collections.emptyList();
    final List<String> userSnapshotIds = Collections.emptyList();
    final Response response = _snapshotResource.postHTML(name, null, valuationTime, tsResolverKeys, userSnapshotIds, liveDataSources, null);
    assertEquals(response.getStatus(), 200);
    assertTrue(((String) response.getEntity()).contains("err"));
  }

  /**
   * Tests the data in the root bean.
   */
  public void testRootData() {
    final FlexiBean root = _snapshotResource.createRootData();
    assertEquals(root.propertyNames().size(), 15);
    final String baseUri = (String) root.get("baseUri");
    assertEquals(baseUri, "http://localhost:8080/");
    final WebUser user = (WebUser) root.get("userSecurity");
    assertEquals(user.getSubject().getPrincipal(), "permissive");
    final WebMarketDataSnapshotUris uris = (WebMarketDataSnapshotUris) root.get("uris");
    assertEquals(uris.snapshot().getPath(), "/datasnapshots/null");
    final MarketDataSnapshotSearchRequest searchRequest = (MarketDataSnapshotSearchRequest) root.get("searchRequest");
    assertNull(searchRequest.getName());
    assertEquals(searchRequest.getPagingRequest(), PagingRequest.ALL);
    assertNull(searchRequest.getSnapshotIds());
    assertEquals(searchRequest.getSortOrder(), MarketDataSnapshotSearchSortOrder.OBJECT_ID_ASC);
    assertNull(searchRequest.getType());
    assertNull(searchRequest.getUniqueIdScheme());
    assertEquals(searchRequest.getVersionCorrection(), VersionCorrection.LATEST);
  }

  /**
   * Tests the search result data.
   */
  public void testSearchResult() {
    final Integer index = 2;
    final Integer number = 1;
    final Integer size = 4;
    final String sort = "NAME ASC";
    final String name = "my snapshot name";
    final ManageableMarketDataSnapshot snapshot = new ManageableMarketDataSnapshot();
    snapshot.setName(name);
    snapshot.setBasisViewName(VIEW_DEFINITION_NAME);
    final MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(snapshot);
    final InMemorySnapshotMaster master = new InMemorySnapshotMaster();
    master.add(document);
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName(name);
    final UniqueId uid = master.search(request).getFirstDocument().getUniqueId();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    uriInfo.setQueryParameter("idscheme.0", "eid1");
    uriInfo.setQueryParameter("idvalue.0", "1");
    uriInfo.setQueryParameter("idscheme.1", "eid2");
    uriInfo.setQueryParameter("idvalue.1", "a");
    final WebMarketDataSnapshotsResource resource = setUpResource(master, CONFIG_MASTER, PROVIDER_FACTORY, CONFIG_SOURCE, RESOLVER, PROCESSOR, HTS_SOURCE,
        VOL_SOURCE, uriInfo);
    final String response = resource.getHTML(index, number, size, sort, name, Arrays.asList(uid.toString()), uriInfo);
    assertTrue(response.contains(name));
    assertTrue(response.contains(VIEW_DEFINITION_NAME));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindSnapshot() {
    final String name = "name";
    final ManageableMarketDataSnapshot snapshot = new ManageableMarketDataSnapshot();
    snapshot.setName(name);
    snapshot.setBasisViewName(VIEW_DEFINITION_NAME);
    final MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(snapshot);
    final InMemorySnapshotMaster master = new InMemorySnapshotMaster();
    master.add(document);
    final MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName(name);
    final UniqueId uid = master.search(request).getFirstDocument().getUniqueId();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebMarketDataSnapshotsResource resource = setUpResource(master, CONFIG_MASTER, PROVIDER_FACTORY, CONFIG_SOURCE, RESOLVER, PROCESSOR, HTS_SOURCE,
        VOL_SOURCE, uriInfo);
    final WebMarketDataSnapshotResource snapshotResource = resource.findSnapshot(uid.toString());
    // uid is set on add to master
    assertTrue(JodaBeanUtils.equalIgnoring((ManageableMarketDataSnapshot) snapshotResource.data().getSnapshot().getNamedSnapshot(), snapshot,
        ManageableMarketDataSnapshot.meta().uniqueId()));
  }

  /**
   * Tests the result of a search.
   */
  public void testFindUserHistoryOnly() {
    // final String userName = "userName";
    // final String email = "user@test.com";
    // final ManageableUser user = new ManageableUser(userName);
    // user.setEmailAddress(email);
    // user.setStatus(UserAccountStatus.LOCKED);
    // final InMemoryUserMaster master = new InMemoryUserMaster();
    // master.add(user);
    // master.removeByName(userName);
    // final MockUriInfo uriInfo = new MockUriInfo(true);
    // final WebMarketDataSnapshotsResource resource = setUpResource(master,
    // uriInfo);
    // final WebUserResource userResource = resource.findUser(userName);
    // // TODO user returned by history is a generic user
    // assertNotNull(userResource.data().getUser());
  }

  /**
   * Tests the result of a search.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testFindSnapshotNoResultFromMaster() {
    final InMemorySnapshotMaster master = new InMemorySnapshotMaster();
    final MockUriInfo uriInfo = new MockUriInfo(true);
    final WebMarketDataSnapshotsResource resource = setUpResource(master, CONFIG_MASTER, PROVIDER_FACTORY, CONFIG_SOURCE, RESOLVER, PROCESSOR, HTS_SOURCE,
        VOL_SOURCE, uriInfo);
    resource.findSnapshot(UniqueId.of(InMemorySnapshotMaster.DEFAULT_OID_SCHEME, "1").toString());
  }

  private static WebMarketDataSnapshotsResource setUpResource(final MarketDataSnapshotMaster snapshotMaster, final ConfigMaster configMaster,
      final LiveMarketDataProviderFactory liveData, final ConfigSource configSource, final ComputationTargetResolver resolver,
      final ViewProcessor viewProcessor, final HistoricalTimeSeriesSource htsSource, final VolatilityCubeDefinitionSource volSource,
      final MockUriInfo uriInfo) {
    final WebMarketDataSnapshotsResource usersResource = new WebMarketDataSnapshotsResource(snapshotMaster, configMaster, liveData, configSource, resolver,
        viewProcessor, htsSource, volSource);
    usersResource.setUriInfo(uriInfo);
    final MockServletContext servletContext = new MockServletContext("/web-engine", new FileSystemResourceLoader());
    final Configuration configuration = FreemarkerOutputter.createConfiguration();
    configuration.setServletContextForTemplateLoading(servletContext, "WEB-INF/pages");
    FreemarkerOutputter.init(servletContext, configuration);
    servletContext.setAttribute(FreemarkerOutputter.class.getName() + ".FreemarkerConfiguration", configuration);
    usersResource.setServletContext(servletContext);
    return usersResource;
  }

  private static class MockViewProcessor implements ViewProcessorInternal {
    private final CompiledFunctionService _compiledFunctionService;
    private final LinkedBlockingQueue<Boolean> _suspendState = new LinkedBlockingQueue<>();
    private boolean _running;
    private boolean _suspended;

    public MockViewProcessor() {
      final FunctionConfigurationSource functions = new SimpleFunctionConfigurationSource(new FunctionConfigurationBundle());
      _compiledFunctionService = new CompiledFunctionService(functions, new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
    }

    @Override
    public Future<Runnable> suspend(final ExecutorService executorService) {
      return executorService.submit(new Runnable() {
        @Override
        public void run() {
          synchronized (MockViewProcessor.this) {
            assertTrue(_running);
            assertFalse(_suspended);
            _suspended = true;
            _suspendState.add(Boolean.TRUE);
          }
        }
      }, (Runnable) new Runnable() {
        @Override
        public void run() {
          synchronized (MockViewProcessor.this) {
            assertTrue(_running);
            assertTrue(_suspended);
            _suspended = false;
            _suspendState.add(Boolean.FALSE);
          }
        }
      });
    }

    @Override
    public synchronized boolean isRunning() {
      return _running;
    }

    @Override
    public synchronized void start() {
      assertFalse(_running);
      _running = true;
    }

    @Override
    public synchronized void stop() {
      assertTrue(_running);
      _running = false;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public ConfigSource getConfigSource() {
      return CONFIG_SOURCE;
    }

    @Override
    public Collection<? extends ViewProcess> getViewProcesses() {
      return null;
    }

    @Override
    public ViewProcess getViewProcess(final UniqueId viewProcessId) {
      return null;
    }

    @Override
    public Collection<ViewClient> getViewClients() {
      return null;
    }

    @Override
    public ViewClient createViewClient(final UserPrincipal clientUser) {
      return null;
    }

    @Override
    public ViewClient getViewClient(final UniqueId clientId) {
      return null;
    }

    @Override
    public CompiledFunctionService getFunctionCompilationService() {
      return _compiledFunctionService;
    }

    @Override
    public ViewProcessorEventListenerRegistry getViewProcessorEventListenerRegistry() {
      return null;
    }

    @Override
    public EngineResourceManager<ViewCycle> getViewCycleManager() {
      return null;
    }

    @Override
    public InMemoryNamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
      return null;
    }

  }
}
