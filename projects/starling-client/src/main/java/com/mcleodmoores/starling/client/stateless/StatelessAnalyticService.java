package com.mcleodmoores.starling.client.stateless;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.starling.client.config.ConfigManager;
import com.mcleodmoores.starling.client.portfolio.PortfolioKey;
import com.mcleodmoores.starling.client.portfolio.PortfolioManager;
import com.mcleodmoores.starling.client.results.AnalyticService;
import com.mcleodmoores.starling.client.results.AsynchronousJob;
import com.mcleodmoores.starling.client.results.ResultListener;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.SynchronousJob;
import com.mcleodmoores.starling.client.results.ViewKey;
import com.mcleodmoores.starling.client.utils.SessionPortfolioTransformer;
import com.opengamma.analytics.env.AnalyticsEnvironment;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Analytic service that creates a portfolio and views only for the duration of the calculation.
 */
public class StatelessAnalyticService {
  private final ConfigManager _configManager;
  private final PortfolioManager _portfolioManager;
  private final AnalyticService _analyticService;
  private static AtomicLong s_sessionCount = new AtomicLong(System.currentTimeMillis());

  /**
   * Create a stateless analytic service instance.  Note this object is only good for a single
   * calculation.
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param positionSource  the position source, not null
   * @param securityMaster  the security master, not null
   * @param securitySource  the security source, not null
   * @param configMaster  the configuration master, not null
   * @param configSource  the configuration source, not null
   * @param viewProcessor  the view processor, not null
   */
  public StatelessAnalyticService(
      final PortfolioMaster portfolioMaster,
      final PositionMaster positionMaster, final PositionSource positionSource,
      final SecurityMaster securityMaster, final SecuritySource securitySource,
      final ConfigMaster configMaster, final ConfigSource configSource,
      final ViewProcessor viewProcessor) {
    _portfolioManager = new PortfolioManager(portfolioMaster, positionMaster, positionSource, securityMaster, securitySource);
    _analyticService = new AnalyticService(ArgumentChecker.notNull(viewProcessor, "viewProcessor"), positionSource, configSource);
    _configManager = new ConfigManager(configMaster, configSource);
  }

  /**
   * Create a stateless analytic service instance.  Note this object is only good for a single calculation.
   * @param toolContext  a tool context, not null, must contain: portfolio, position, security masters; position
   *                     and config sources; a view processor
   */
  public StatelessAnalyticService(final ToolContext toolContext) {
    this(ArgumentChecker.notNull(toolContext, "toolContext").getPortfolioMaster(),
         toolContext.getPositionMaster(),
         toolContext.getPositionSource(),
         toolContext.getSecurityMaster(),
         toolContext.getSecuritySource(),
         toolContext.getConfigMaster(),
         toolContext.getConfigSource(),
         toolContext.getViewProcessor());
  }

  /**
   * Create a job that calls back to a listener provided when start(ResultListener) is called.  The caller is responsible for
   * either explicitly closing the job with close() on completion or running the whole thing inside a try-with-resources block
   * (assuming the listener completes before the block)
   * @param templateViewKey  the key for the view to use as a template, not null
   * @param portfolio  the portfolio to use in the temporary view
   * @param correlationIdScheme  the scheme used for correlation ids
   * @param valuationTime  the valuation time, not null
   * @param snapshotDate  the date from which data is to be retrieved, not null
   * @return an AsynchronousJob, on which the start method should be called with a ResultListener
   */
  public AsynchronousJob createAsynchronousJob(final ViewKey templateViewKey, final SimplePortfolio portfolio, final ExternalScheme correlationIdScheme,
      final Instant valuationTime, final LocalDate snapshotDate) {
    return new StatelessAsynchronousJob(templateViewKey, portfolio, correlationIdScheme, valuationTime, snapshotDate);
  }

  /**
   * Create a job that returns the results synchronously when the run method is called.  The caller is not responsible for
   * cleaning up resources.
   * @param templateViewKey  the key for the view to use as a template, not null
   * @param portfolio  the portfolio to use in the temporary view
   * @param correlationIdScheme  the scheme used for correlation ids
   * @param valuationTime  the valuation time, not null
   * @param snapshotDate  the date from which data is to be retrieved, not null
   * @return an SynchronousJob, on which the run method should be called
   */
  public SynchronousJob createSynchronousJob(final ViewKey templateViewKey, final SimplePortfolio portfolio, final ExternalScheme correlationIdScheme,
      final Instant valuationTime, final LocalDate snapshotDate) {
    return new StatelessSynchronousJob(templateViewKey, portfolio, correlationIdScheme, valuationTime, snapshotDate);
  }

  /**
   * Create a job that returns the results synchronously when the run method is called.  The caller is not responsible for
   * cleaning up resources.
   * @param templateViewKey  the key for the view to use as a template, not null
   * @param portfolio  the portfolio to use in the temporary view
   * @param correlationIdScheme  the scheme used for correlation ids
   * @param valuationTime  the valuation time, not null
   * @param snapshotDate  the date from which data is to be retrieved, not null
   * @return a SynchronousJob, on which the run method should be called
   */
  public SynchronousJob createSynchronousJob(final ViewKey templateViewKey, final SimplePortfolio portfolio, final ExternalScheme correlationIdScheme,
      final Instant valuationTime, final LocalDate snapshotDate, final DayCount analyticsDayCount) {
    return new StatelessSynchronousJob(templateViewKey, portfolio, correlationIdScheme, valuationTime, snapshotDate, analyticsDayCount);
  }

  public final class StatelessAsynchronousJob implements AsynchronousJob, AutoCloseable {
    private final AsynchronousJob _job;
    private final String _sessionPrefix;
    private final PortfolioKey _portfolioKey;
    private final ViewKey _tempView;

    public StatelessAsynchronousJob(final ViewKey templateViewKey, final SimplePortfolio portfolio, final ExternalScheme correlationIdScheme,
                         final Instant valuationTime, final LocalDate snapshotDate) {
      _sessionPrefix = Long.toString(s_sessionCount.incrementAndGet()) + "-";
      final SimplePortfolio sessionCopy = SessionPortfolioTransformer.buildSessionCopy(portfolio, correlationIdScheme, _sessionPrefix);
      _portfolioKey = _portfolioManager.savePortfolio(sessionCopy);
      _tempView = _configManager.createTemplateView(templateViewKey, _portfolioKey, _sessionPrefix);
      _job = _analyticService.createAsynchronousJob(_tempView, valuationTime, snapshotDate);
    }

    /**
     * Start the job notifying the listener when the result is available.  The caller is responsible for closing the
     * job by calling close() explicitly or using in a try-with-resources block.
     * @param resultListener  the result listener, to be called when the calculation is complete, not null
     */
    @Override
    public void start(final ResultListener resultListener) {
      _job.start(new SessionResultListener(resultListener, _sessionPrefix));
    }

    /**
     * Indicate the service has completed its calculation and resources can be freed.
     * @throws Exception  any exception encountered when releasing resources.
     */
    @Override
    public void close() throws Exception {
      _job.close();
      _portfolioManager.delete(_portfolioKey,
              EnumSet.of(PortfolioManager.DeleteScope.SECURITY, PortfolioManager.DeleteScope.POSITION, PortfolioManager.DeleteScope.PORTFOLIO));
      _configManager.deleteView(_tempView);
    }
  }

  public final class StatelessSynchronousJob implements SynchronousJob {
    private final Logger LOGGER = LoggerFactory.getLogger(StatelessSynchronousJob.class);

    private final SynchronousJob _job;
    private final String _sessionPrefix;
    private final PortfolioKey _portfolioKey;
    private final ViewKey _tempView;

    public StatelessSynchronousJob(final ViewKey templateViewKey, final SimplePortfolio portfolio, final ExternalScheme correlationIdScheme,
        final Instant valuationTime, final LocalDate snapshotDate) {
      _sessionPrefix = Long.toString(s_sessionCount.incrementAndGet()) + "-";
      final SimplePortfolio sessionCopy = SessionPortfolioTransformer.buildSessionCopy(portfolio, correlationIdScheme, _sessionPrefix);

      _portfolioKey = _portfolioManager.savePortfolio(sessionCopy);
      _tempView = _configManager.createTemplateView(templateViewKey, _portfolioKey, _sessionPrefix);
      _job = _analyticService.createSynchronousJob(_tempView, valuationTime, snapshotDate);
    }

    public StatelessSynchronousJob(final ViewKey templateViewKey, final SimplePortfolio portfolio, final ExternalScheme correlationIdScheme,
        final Instant valuationTime, final LocalDate snapshotDate, final DayCount environmentDayCount) {
      AnalyticsEnvironment.setInstance(AnalyticsEnvironment.builder()
          .modelDayCount(environmentDayCount)
          .fixedAnnuityDefinitionBuilder(new FixedAnnuityDefinitionBuilder())
          .floatingAnnuityDefinitionBuilder(new FloatingAnnuityDefinitionBuilder())
          .build());
      _sessionPrefix = Long.toString(s_sessionCount.incrementAndGet()) + "-";
      final SimplePortfolio sessionCopy = SessionPortfolioTransformer.buildSessionCopy(portfolio, correlationIdScheme, _sessionPrefix);

      _portfolioKey = _portfolioManager.savePortfolio(sessionCopy);
      _tempView = _configManager.createTemplateView(templateViewKey, _portfolioKey, _sessionPrefix);
      _job = _analyticService.createSynchronousJob(_tempView, valuationTime, snapshotDate);
    }

    @Override
    public ResultModel run() {
      final ResultModel resultModel = _job.run();
      final ResultModel wrappedModel = new StatelessResultModel(resultModel, _sessionPrefix);
      return wrappedModel;
    }

    /**
     * Indicate the service has completed its calculation and resources can be freed.
     * @throws Exception  any exception encountered when releasing resources.
     */
    @Override
    public void close() throws Exception {
      _job.close();
      _portfolioManager.delete(_portfolioKey,
          EnumSet.of(PortfolioManager.DeleteScope.SECURITY, PortfolioManager.DeleteScope.POSITION, PortfolioManager.DeleteScope.PORTFOLIO));
      _configManager.deleteView(_tempView);
      AnalyticsEnvironment.setInstance(AnalyticsEnvironment.builder()
          .modelDayCount(DayCounts.ACT_ACT_ISDA)
          .fixedAnnuityDefinitionBuilder(new FixedAnnuityDefinitionBuilder())
          .floatingAnnuityDefinitionBuilder(new FloatingAnnuityDefinitionBuilder())
          .build());
    }
  }

  /**
   * Listener for result that wraps the result in a wrapper that translates from the temporary ids back to
   * the original ids.
   */
  private class SessionResultListener implements ResultListener {
    private final Logger LOGGER = LoggerFactory.getLogger(SessionResultListener.class);
    private final ResultListener _underlying;
    private final String _sessionPrefix;

    public SessionResultListener(final ResultListener underlying, final String sessionPrefix) {
      _underlying = underlying;
      _sessionPrefix = sessionPrefix;
    }

    @Override
    public void calculationComplete(final ResultModel resultModel, final AsynchronousJob job) {
      final StatelessResultModel statelessResultModel = new StatelessResultModel(resultModel, _sessionPrefix);
      _underlying.calculationComplete(statelessResultModel, job);
    }
  }
}
