/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.io.IOUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeDataInputStreamReader;
import org.fudgemsg.wire.FudgeDataOutputStreamWriter;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * This class allows you to test serializers from other platforms by using any {@link AbstractFudgeBuilderTestCase}s you have.
 */
public class BuilderTestProxyFactory {

  interface BuilderTestProxy {
    FudgeMsg proxy(final Class<?> clazz, FudgeMsg orig);
  }

  public BuilderTestProxy getProxy() {
    final String execPath = System.getProperty("com.opengamma.util.test.BuilderTestProxyFactory.ExecBuilderTestProxy.execPath");
    if (execPath != null) {
      return new ExecBuilderTestProxy(execPath);
    }
    return new NullBuilderTestProxy();
  }

  private static class NullBuilderTestProxy implements BuilderTestProxy {
    @Override
    public FudgeMsg proxy(final Class<?> clazz, final FudgeMsg orig) {
      return orig;
    }
  }

  private static class ExecBuilderTestProxy implements BuilderTestProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecBuilderTestProxy.class);

    private final String _execPath;

    public ExecBuilderTestProxy(final String execPath) {
      _execPath = execPath;
    }

    @Override
    public FudgeMsg proxy(final Class<?> clazz, final FudgeMsg orig) {
      final FudgeContext context = OpenGammaFudgeContext.getInstance();

      final LinkedList<String> command = new LinkedList<>();
      command.add(_execPath);
      command.add(clazz.getName());

      final ProcessBuilder processBuilder = new ProcessBuilder(command);
      try {
        final Process proc = processBuilder.start();
        try {
          try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(
              new FudgeDataOutputStreamWriter(context, proc.getOutputStream()))) {
            fudgeMsgWriter.writeMessage(orig);
            fudgeMsgWriter.flush();

            try (FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(
                new FudgeDataInputStreamReader(context, proc.getInputStream()))) {
              final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
              final Future<FudgeMsg> retMsgFuture = scheduledThreadPoolExecutor.submit(new Callable<FudgeMsg>() {
                @Override
                public FudgeMsg call() throws Exception {
                  return fudgeMsgReader.nextMessage();
                }
              });

              final Future<List<String>> errFuture = scheduledThreadPoolExecutor.submit(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                  final InputStream errorStream = proc.getErrorStream();
                  try {
                    return IOUtils.readLines(errorStream);
                  } finally {
                    errorStream.close();
                  }
                }
              });

              for (final String err : errFuture.get()) {
                LOGGER.warn(err);
              }
              final int ret = proc.waitFor();
              if (ret != 0) {
                throw new IOException("Exit code not expected: " + ret);
              }
              return retMsgFuture.get();
            }
          }
        } finally {
          proc.destroy();
        }
      } catch (final Exception ex) {
        throw new AssertionError(ex);
      }
    }
  }

}
