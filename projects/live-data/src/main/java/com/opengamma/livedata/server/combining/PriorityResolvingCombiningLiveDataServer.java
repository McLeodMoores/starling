/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.combining;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.resolver.EHCachingDistributionSpecificationResolver;
import com.opengamma.livedata.server.CombiningLiveDataServer;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.CacheManager;

/**
 * Combines live data servers by choosing the first server which can resolve the ID
 * If none can then the first server is returned (which will fail).
 */
public class PriorityResolvingCombiningLiveDataServer extends CombiningLiveDataServer {
  private final List<? extends StandardLiveDataServer> _servers;

  /**
   * Constructs an instance.
   *
   * @param servers Servers in preference order (Best first)
   * @param cacheManager  the cache manager, not null
   */
  public PriorityResolvingCombiningLiveDataServer(final List<? extends StandardLiveDataServer> servers, final CacheManager cacheManager) {
    super(servers, cacheManager);
    ArgumentChecker.notEmpty(servers, "servers");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _servers = servers;
  }

  @Override
  protected Map<StandardLiveDataServer, Collection<LiveDataSpecification>> groupByServer(
      final Collection<LiveDataSpecification> specs) {
    final Map<StandardLiveDataServer, Collection<LiveDataSpecification>> ret = new HashMap<>();

    Collection<LiveDataSpecification> unresolvedSpecs = specs;

    for (final StandardLiveDataServer server : _servers) {
      final Map<LiveDataSpecification, DistributionSpecification> resolved = server.getDistributionSpecificationResolver()
          .resolve(unresolvedSpecs);

      unresolvedSpecs = new HashSet<>();
      final Set<LiveDataSpecification> resolvedSpecs = new HashSet<>();

      for (final Entry<LiveDataSpecification, DistributionSpecification> entry : resolved.entrySet()) {
        if (entry.getValue() != null) {
          resolvedSpecs.add(entry.getKey());
        } else {
          unresolvedSpecs.add(entry.getKey());
        }
      }
      ret.put(server, resolvedSpecs);
      if (unresolvedSpecs.size() == 0) {
        return ret;
      }
    }

    final StandardLiveDataServer defaultServer = _servers.get(0);
    final Collection<LiveDataSpecification> defaultSet = ret.get(defaultServer);
    if (defaultSet == null) {
      ret.put(defaultServer, unresolvedSpecs);
    } else {
      defaultSet.addAll(unresolvedSpecs);
    }
    return ret;

  }

  /**
   * Delegates the distribution of market data to clients.
   */
  private class DelegatingDistributionSpecificationResolver extends AbstractResolver<LiveDataSpecification, DistributionSpecification>
  implements DistributionSpecificationResolver {
    //TODO: dedupe with group by ?

    @Override
    public Map<LiveDataSpecification, DistributionSpecification> resolve(final Collection<LiveDataSpecification> specs) {
      final Map<LiveDataSpecification,  DistributionSpecification> ret = new HashMap<>();

      Collection<LiveDataSpecification> unresolvedSpecs = specs;
      for (final StandardLiveDataServer server : _servers) {
        final Map<LiveDataSpecification, DistributionSpecification> resolved =
            server.getDistributionSpecificationResolver().resolve(unresolvedSpecs);
        unresolvedSpecs = new HashSet<>();
        for (final Entry<LiveDataSpecification, DistributionSpecification> entry : resolved.entrySet()) {
          if (entry.getValue() != null) {
            ret.put(entry.getKey(), entry.getValue());
          } else {
            unresolvedSpecs.add(entry.getKey());
          }
        }
        if (unresolvedSpecs.size() == 0) {
          return ret;
        }
      }

      for (final LiveDataSpecification liveDataSpecification : unresolvedSpecs) {
        ret.put(liveDataSpecification, null);
      }
      return ret;
    }
  }

  /**
   * @return the specification resolver
   */
  public DistributionSpecificationResolver getDefaultDistributionSpecificationResolver() {
    final DistributionSpecificationResolver distributionSpecResolver = new DelegatingDistributionSpecificationResolver();
    //TODO should I cache here
    return new EHCachingDistributionSpecificationResolver(distributionSpecResolver, getCacheManager(), "COMBINING");
  }
}
