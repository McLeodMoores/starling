/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Provides remote access to a {@link FunctionBlacklist}.
 */
public class RemoteFunctionBlacklist extends AbstractFunctionBlacklist {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteFunctionBlacklist.class);

  private final class Listener extends BaseFunctionBlacklistRuleListener implements FudgeMessageReceiver {

    @Override
    protected Pair<Integer, ? extends Collection<FunctionBlacklistRule>> getUnderlyingRules(final int modificationCount) {
      final FudgeMsg msg = getProvider().refresh(getName(), modificationCount);
      if (msg.isEmpty()) {
        return null;
      }
      final FudgeDeserializer fdc = new FudgeDeserializer(getProvider().getFudgeContext());
      return Pairs.of(msg.getInt(DataFunctionBlacklistFields.MODIFICATION_COUNT_FIELD), getRules(fdc, msg.getMessage(DataFunctionBlacklistFields.RULES_FIELD)));
    }

    @Override
    protected synchronized void replaceRules(final Collection<FunctionBlacklistRule> rules) {
      final List<FunctionBlacklistRule> newRules = new ArrayList<>(rules.size());
      final Set<FunctionBlacklistRule> oldRules = new HashSet<>(_rules);
      for (final FunctionBlacklistRule rule : rules) {
        if (_rules.contains(rule)) {
          oldRules.remove(rule);
          continue;
        }
        newRules.add(rule);
      }
      if (!newRules.isEmpty()) {
        addRules(newRules);
      }
      if (!oldRules.isEmpty()) {
        removeRules(oldRules);
      }
    }

    @Override
    protected synchronized void addRule(final FunctionBlacklistRule rule) {
      _rules.add(rule);
      notifyAddRule(rule);
    }

    @Override
    protected synchronized void addRules(final Collection<FunctionBlacklistRule> rules) {
      _rules.addAll(rules);
      notifyAddRules(rules);
    }

    @Override
    protected synchronized void removeRule(final FunctionBlacklistRule rule) {
      _rules.remove(rule);
      notifyRemoveRule(rule);
    }

    @Override
    protected synchronized void removeRules(final Collection<FunctionBlacklistRule> rules) {
      _rules.removeAll(rules);
      notifyRemoveRules(rules);
    }

    @Override
    public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
      final FudgeMsg msg = msgEnvelope.getMessage();
      final int modificationCount = msg.getInt(DataFunctionBlacklistFields.MODIFICATION_COUNT_FIELD);
      FudgeField field = msg.getByName(DataFunctionBlacklistFields.RULES_ADDED_FIELD);
      final FudgeDeserializer fdc = new FudgeDeserializer(fudgeContext);
      if (field != null) {
        final List<FudgeField> rulesMsg = msg.getFieldValue(FudgeMsg.class, field).getAllFields();
        if (rulesMsg.size() == 1) {
          ruleAdded(modificationCount, fdc.fieldValueToObject(FunctionBlacklistRule.class, rulesMsg.get(0)), getProvider().getBackgroundTasks());
        } else {
          final List<FunctionBlacklistRule> rules = new ArrayList<>(rulesMsg.size());
          for (final FudgeField ruleField : rulesMsg) {
            rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, ruleField));
          }
          rulesAdded(modificationCount, rules, getProvider().getBackgroundTasks());
        }
      }
      field = msg.getByName(DataFunctionBlacklistFields.RULES_REMOVED_FIELD);
      if (field != null) {
        final List<FudgeField> rulesMsg = msg.getFieldValue(FudgeMsg.class, field).getAllFields();
        if (rulesMsg.size() == 1) {
          ruleRemoved(modificationCount, fdc.fieldValueToObject(FunctionBlacklistRule.class, rulesMsg.get(0)), getProvider().getBackgroundTasks());
        } else {
          final List<FunctionBlacklistRule> rules = new ArrayList<>(rulesMsg.size());
          for (final FudgeField ruleField : rulesMsg) {
            rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, ruleField));
          }
          rulesRemoved(modificationCount, rules, getProvider().getBackgroundTasks());
        }
      }
    }

  }

  private final RemoteFunctionBlacklistProvider _provider;
  private final Set<FunctionBlacklistRule> _rules = new HashSet<>();
  private final Listener _listener = new Listener();
  private final Connection _connection;

  private static Collection<FunctionBlacklistRule> getRules(final FudgeDeserializer fdc, final FudgeMsg rulesField) {
    if (rulesField != null) {
      final List<FunctionBlacklistRule> rules = new ArrayList<>(rulesField.getNumFields());
      for (final FudgeField rule : rulesField) {
        rules.add(fdc.fieldValueToObject(FunctionBlacklistRule.class, rule));
      }
      return rules;
    }
    return Collections.emptyList();
  }

  public RemoteFunctionBlacklist(final FudgeDeserializer fdc, final FudgeMsg info, final RemoteFunctionBlacklistProvider provider) {
    super(info.getString(DataFunctionBlacklistFields.NAME_FIELD), provider.getBackgroundTasks());
    _provider = provider;
    _listener.init(info.getInt(DataFunctionBlacklistFields.MODIFICATION_COUNT_FIELD), getRules(fdc, info.getMessage(DataFunctionBlacklistFields.RULES_FIELD)));
    _connection = startJmsConnection(info.getString(DataFunctionBlacklistFields.JMS_TOPIC_FIELD), _listener);
    _listener.refresh();
  }

  protected Connection startJmsConnection(final String topicName, final FudgeMessageReceiver listener) {
    try {
      final Connection connection = getProvider().getJmsConnector().getConnectionFactory().createConnection();
      connection.start();
      final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      final Topic topic = session.createTopic(topicName);
      final MessageConsumer messageConsumer = session.createConsumer(topic);
      messageConsumer.setMessageListener(new JmsByteArrayMessageDispatcher(new ByteArrayFudgeMessageReceiver(listener, getProvider().getFudgeContext())));
      return connection;
    } catch (final JMSException e) {
      throw new OpenGammaRuntimeException("Failed to create JMS connection on " + topicName, e);
    }
  }

  @Override
  protected void finalize() {
    if (_connection != null) {
      try {
        _connection.close();
      } catch (final JMSException e) {
        LOGGER.warn("Failed to close JMS connection", e);
      }
    }
  }

  protected RemoteFunctionBlacklistProvider getProvider() {
    return _provider;
  }

  @Override
  public Set<FunctionBlacklistRule> getRules() {
    synchronized (_listener) {
      return new HashSet<>(_rules);
    }
  }

}
