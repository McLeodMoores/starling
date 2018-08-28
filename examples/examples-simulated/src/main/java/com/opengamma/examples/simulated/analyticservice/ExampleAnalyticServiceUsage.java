/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.analyticservice;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Random;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.scripts.Scriptable;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.money.Currency;

/**
 *
 */
@Scriptable
public class ExampleAnalyticServiceUsage extends AbstractTool<ToolContext> {

  private static final String QUEUE_OPTION = "queue";
  private static final String ACTIVE_MQ_OPTION = "activeMQ";
  private static final int WAIT_BTW_TRADES = 10000;
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleAnalyticServiceUsage.class);
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "TEST"));
  private static final String PROVIDER_ID_NAME  = "providerId";
  private static final String RANDOM_ID_SCHEME = "Rnd";
  private static final String PREFIX = "OGAnalytics";
  private static final String SEPARATOR = ".";

  private final Random _random = new Random();

  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new ExampleAnalyticServiceUsage().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {

    final CommandLine commandLine = getCommandLine();

    final String activeMQUrl = commandLine.getOptionValue(ACTIVE_MQ_OPTION);
    final String destinationName = commandLine.getOptionValue(QUEUE_OPTION);

    final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(activeMQUrl);
    activeMQConnectionFactory.setWatchTopicAdvisories(false);

    final PooledConnectionFactory jmsConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
    jmsConnectionFactory.start();

    final JmsConnectorFactoryBean jmsConnectorFactoryBean = new com.opengamma.util.jms.JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setName("StandardJms");
    jmsConnectorFactoryBean.setConnectionFactory(jmsConnectionFactory);
    jmsConnectorFactoryBean.setClientBrokerUri(URI.create(activeMQUrl));

    final JmsConnector jmsConnector = jmsConnectorFactoryBean.getObjectCreating();
    final ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(new FudgeMessageReceiver() {

      @Override
      public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
        final FudgeMsg message = msgEnvelope.getMessage();
        LOGGER.debug("received {}", message);
      }
    }, FUDGE_CONTEXT);
    final JmsByteArrayMessageDispatcher jmsDispatcher = new JmsByteArrayMessageDispatcher(fudgeReceiver);

    final Connection connection = jmsConnector.getConnectionFactory().createConnection();
    connection.start();

    pushTrade("ARG", connection, destinationName, jmsConnector, jmsDispatcher);
    Thread.sleep(WAIT_BTW_TRADES);
    pushTrade("MMM", connection, destinationName, jmsConnector, jmsDispatcher);
    Thread.sleep(WAIT_BTW_TRADES * 10);
    connection.stop();
    jmsConnectionFactory.stop();

  }

  private void pushTrade(final String securityId, final Connection connection, final String destinationName, final JmsConnector jmsConnector, final JmsByteArrayMessageDispatcher jmsDispatcher) {
    final String providerId = generateTrade(securityId, destinationName, jmsConnector);
    final String topicStr = PREFIX + SEPARATOR + providerId + SEPARATOR + "Default" + SEPARATOR + ValueRequirementNames.FAIR_VALUE;
    try {
      final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      final Topic topic = session.createTopic(topicStr);
      final MessageConsumer messageConsumer = session.createConsumer(topic);
      messageConsumer.setMessageListener(jmsDispatcher);
    } catch (final JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create subscription to JMS topics ", e);
    }
  }

  private String generateTrade(final String securityId, final String destinationName, final JmsConnector jmsConnector) {
    final SimpleTrade trade = new SimpleTrade();
    trade.setCounterparty(COUNTERPARTY);
    trade.setPremiumCurrency(Currency.USD);
    trade.setQuantity(BigDecimal.valueOf(_random.nextInt(10) + 10));
    trade.setTradeDate(LocalDate.now());
    final String providerId = GUIDGenerator.generate().toString();
    trade.addAttribute(PROVIDER_ID_NAME, RANDOM_ID_SCHEME + "~" + providerId);
    trade.setSecurityLink(new SimpleSecurityLink(ExternalSchemes.syntheticSecurityId(securityId)));
    LOGGER.debug("Generated {}", trade);

    final FudgeMsg msg = FUDGE_CONTEXT.toFudgeMsg(trade).getMessage();

    LOGGER.debug("sending {} to {}", msg, destinationName);

    final byte[] bytes = FUDGE_CONTEXT.toByteArray(msg);

    jmsConnector.getJmsTemplateQueue().send(destinationName, new MessageCreator() {
      @Override
      public Message createMessage(final Session session) throws JMSException {
        final BytesMessage bytesMessage = session.createBytesMessage();
        bytesMessage.writeBytes(bytes);
        return bytesMessage;
      }
    });
    return providerId;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(final boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createActiveMQOption());
    options.addOption(createDestinationOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createActiveMQOption() {
    return OptionBuilder.isRequired(true)
                        .hasArgs()
                        .withArgName("ActiveMQ URL")
                        .withDescription("the ActiveMQ broker URL")
                        .withLongOpt(ACTIVE_MQ_OPTION)
                        .create("a");
  }

  @SuppressWarnings("static-access")
  private Option createDestinationOption() {
    return OptionBuilder.isRequired(true)
                        .hasArgs()
                        .withArgName("queue name")
                        .withDescription("JMS queue name")
                        .withLongOpt(QUEUE_OPTION)
                        .create("q");
  }

}
