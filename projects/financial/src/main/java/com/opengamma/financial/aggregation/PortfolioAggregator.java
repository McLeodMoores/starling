/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;

/**
 * An aggregator of portfolios.
 */
public final class PortfolioAggregator {

  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioAggregator.class);

  private static final UniqueIdSupplier SYNTHETIC_IDENTIFIERS = new UniqueIdSupplier("PortfolioAggregator");

  private final List<AggregationFunction<?>> _aggregationFunctions;

  public PortfolioAggregator(final AggregationFunction<?>... aggregationFunctions) {
    _aggregationFunctions = Arrays.asList(aggregationFunctions);
  }

  public PortfolioAggregator(final Collection<AggregationFunction<?>> aggregationFunctions) {
    _aggregationFunctions = new ArrayList<>(aggregationFunctions);
  }

  private static UniqueId createSyntheticIdentifier() {
    return SYNTHETIC_IDENTIFIERS.get();
  }

  public Portfolio aggregate(final Portfolio inputPortfolio) {
    final UniqueId portfolioId = inputPortfolio.getUniqueId();
    UniqueId aggId;
    if (portfolioId != null) {
      final String aggPortfolioId = buildPortfolioName(portfolioId.getValue());
      aggId = UniqueId.of(portfolioId.getScheme(), aggPortfolioId);
    } else {
      aggId = createSyntheticIdentifier();
    }
    final String aggPortfolioName = buildPortfolioName(inputPortfolio.getName());
    final List<Position> flattenedPortfolio = flatten(inputPortfolio);
    final SimplePortfolioNode root = new SimplePortfolioNode(createSyntheticIdentifier(), buildPortfolioName("Portfolio"));
    final SimplePortfolio aggPortfolio = new SimplePortfolio(aggId, aggPortfolioName, root);
    aggregate(root, flattenedPortfolio, new ArrayDeque<>(_aggregationFunctions));
    aggPortfolio.setAttributes(inputPortfolio.getAttributes());
    return aggPortfolio;
  }

  public static List<Position> flatten(final Portfolio inputPortfolio) {
    final List<Position> positions = Lists.newArrayList();
    flatten(inputPortfolio.getRootNode(), positions);
    return positions;
  }

  private static void flatten(final PortfolioNode portfolioNode, final List<Position> flattenedPortfolio) {
    flattenedPortfolio.addAll(portfolioNode.getPositions());
    for (final PortfolioNode subNode : portfolioNode.getChildNodes()) {
      flatten(subNode, flattenedPortfolio);
    }
  }

  private void aggregate(final SimplePortfolioNode inputNode, final List<Position> flattenedPortfolio, final Queue<AggregationFunction<?>> functionList) {
    final AggregationFunction<?> nextFunction = functionList.remove();
    LOGGER.debug("Aggregating {} positions by {}", flattenedPortfolio, nextFunction);
    @SuppressWarnings("unchecked")
    final
    Map<String, List<Position>> buckets = new TreeMap<>((Comparator<? super String>) nextFunction);
    for (final Object entry : nextFunction.getRequiredEntries()) {
      buckets.put(entry.toString(), new ArrayList<Position>());
    }
    // drop into buckets - could drop straight into tree but this is easier because we can use faster lookups as we're going.
    for (final Position position : flattenedPortfolio) {
      final Object obj = nextFunction.classifyPosition(position);
      if (obj != null) {
        final String name = obj.toString();
        if (buckets.containsKey(name)) {
          buckets.get(name).add(position);
        } else {
          final ArrayList<Position> list = new ArrayList<>();
          list.add(position);
          buckets.put(name, list);
        }
      }
    }
    for (final String bucketName : buckets.keySet()) {
      final SimplePortfolioNode newNode = new SimplePortfolioNode();
      newNode.setUniqueId(createSyntheticIdentifier());
      newNode.setParentNodeId(inputNode.getUniqueId());
      newNode.setName(bucketName);
      inputNode.addChildNode(newNode);
      final List<Position> bucket = buckets.get(bucketName);
      Collections.sort(bucket, nextFunction.getPositionComparator());
      if (functionList.isEmpty() || bucket.isEmpty()) { //IGN-138 - don't build huge empty portfolios
        for (final Position position : bucket) {
          newNode.addPosition(position);
        }
      } else {
        aggregate(newNode, bucket, new ArrayDeque<>(functionList)); // make a copy for each bucket.
      }
    }
  }

  private String buildPortfolioName(final String existingName) {
    final StringBuilder aggregatedPortfolioName = new StringBuilder();
    aggregatedPortfolioName.append(existingName);
    aggregatedPortfolioName.append(" aggregated by ");
    for (final AggregationFunction<?> aggFunction : _aggregationFunctions) {
      aggregatedPortfolioName.append(aggFunction.getName());
      aggregatedPortfolioName.append(", ");
    }
    if (_aggregationFunctions.size() > 0) {
      aggregatedPortfolioName.delete(aggregatedPortfolioName.length() - 2, aggregatedPortfolioName.length()); // remember it's end index _exclusive_
    }
    return aggregatedPortfolioName.toString();
  }

  public static void aggregate(final String portfolioName, final String aggregationName,
                               final PortfolioMaster portfolioMaster, final PositionMaster positionMaster,
                               final PositionSource positionSource, final SecuritySource secSource,
                               final AggregationFunction<?>[] aggregationFunctions, final boolean split) {
    final PortfolioSearchRequest searchReq = new PortfolioSearchRequest();
    searchReq.setName(portfolioName);
    LOGGER.info("Searching for portfolio " + portfolioName + "...");
    final PortfolioSearchResult searchResult = portfolioMaster.search(searchReq);
    LOGGER.info("Done. Got " + searchResult.getDocuments().size() + " results");
    final ManageablePortfolio manageablePortfolio = searchResult.getFirstPortfolio();
    if (manageablePortfolio == null) {
      LOGGER.error("Portfolio " + portfolioName + " was not found");
      System.exit(1);
    }
    LOGGER.info("Reloading portfolio from position source...");
    final Portfolio portfolio = positionSource.getPortfolio(manageablePortfolio.getUniqueId(), VersionCorrection.LATEST);
    if (portfolio == null) {
      LOGGER.error("Portfolio " + portfolioName + " was not found from PositionSource");
      System.exit(1);
    }
    LOGGER.info("Done.");
    final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);
    LOGGER.info("Resolving portfolio positions and securities...");
    final Portfolio resolvedPortfolio = PortfolioCompiler.resolvePortfolio(portfolio, newFixedThreadPool, secSource);
    if (resolvedPortfolio == null) {
      LOGGER.error("Portfolio " + portfolioName + " was not correctly resolved by PortfolioCompiler");
      System.exit(1);
    }
    LOGGER.info("Resolution Complete.");
    final PortfolioAggregator aggregator = new PortfolioAggregator(aggregationFunctions);
    LOGGER.info("Beginning aggregation");
    final Portfolio aggregatedPortfolio = aggregator.aggregate(resolvedPortfolio);
    LOGGER.info("Aggregation complete, about to persist...");
    if (aggregatedPortfolio == null) {
      LOGGER.error("Portfolio " + portfolioName + " was not correctly aggregated by the Portfolio Aggregator");
      System.exit(1);
    }
    final SavePortfolio savePortfolio = new SavePortfolio(newFixedThreadPool, portfolioMaster, positionMaster);
    if (split) {
      for (final PortfolioNode portfolioNode : aggregatedPortfolio.getRootNode().getChildNodes()) {
        final String splitPortfolioName = portfolioName + " (" + aggregationName + " " + portfolioNode.getName() + ")";
        final SimplePortfolioNode root = new SimplePortfolioNode("root");
        root.addChildNode(portfolioNode);
        final Portfolio splitPortfolio = new SimplePortfolio(splitPortfolioName, root);
        splitPortfolio.setAttributes(aggregatedPortfolio.getAttributes());
        LOGGER.info("Saving split portfolio " + portfolioName + "...");
        savePortfolio.savePortfolio(splitPortfolio, true);
      }

    } else {
      savePortfolio.savePortfolio(aggregatedPortfolio, true); // update matching named portfolio.
    }
    LOGGER.info("Saved.");

    // Shut down thread pool before returning
    newFixedThreadPool.shutdown();
  }
}
