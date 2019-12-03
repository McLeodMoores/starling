/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 *
 */
public class ReportGenerator {

  // TODO does this to be extensible?
  enum Format {
    TEXT("/regression", "plain-text-report.ftl");

    private final String _templateLocation;
    private final String _templateName;

    Format(final String templateLocation, final String templateName) {
      _templateLocation = templateLocation;
      _templateName = templateName;
    }

    public String getTemplateLocation() {
      return _templateLocation;
    }

    private String getTemplateName() {
      return _templateName;
    }
  }

  public static String generateReport(final RegressionTestResults results) {
    final StringWriter writer = new StringWriter();
    generateReport(results, Format.TEXT, writer);
    return writer.toString();
  }

  public static void generateReport(final RegressionTestResults results, final Format format, final Writer writer) {
    final Configuration cfg = new Configuration();
    try {
      cfg.setClassForTemplateLoading(ReportGenerator.class, format.getTemplateLocation());
      final Template template = cfg.getTemplate(format.getTemplateName());
      final Map<String, Object> input = ImmutableMap.<String, Object>of("results", results);
      template.process(input, writer);
      writer.flush();
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("Error generating report", e);
    }
  }

  public static void main(final String[] args) throws IOException {
    RegressionTestResults results;
    try (BufferedReader reader = new BufferedReader(new FileReader("/Users/chris/tmp/regression/results.xml"))) {
      final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
      final FudgeXMLStreamReader streamReader = new FudgeXMLStreamReader(OpenGammaFudgeContext.getInstance(), reader);
      final FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(streamReader);
      final FudgeMsg msg = fudgeMsgReader.nextMessage();
      results = deserializer.fudgeMsgToObject(RegressionTestResults.class, msg);
    }
    System.out.println(generateReport(results));
    System.exit(0);
  }
}
