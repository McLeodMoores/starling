/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.viewer.status.ViewStatusOption.ResultFormat;
import com.opengamma.util.ArgumentChecker;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * View status result producer.
 */
public class ViewStatusResultProducer {
  /**
   * Produce view result with the given aggregation parameters.
   *
   * @param aggregator
   *          the view status aggregator, not null
   * @param format
   *          the result format type, not null
   * @param aggregateType
   *          the list of aggregation type in the desired order, not null
   * @return the string representation of the result
   */
  public String statusResult(final ViewStatusResultAggregator aggregator, final ResultFormat format, final AggregateType aggregateType) {
    ArgumentChecker.notNull(aggregator, "aggregator");
    ArgumentChecker.notNull(format, "format");
    ArgumentChecker.notNull(aggregateType, "aggregateType");

    final ViewStatusModel viewStatusModel = aggregator.aggregate(aggregateType);
    return formatResultModel(format, viewStatusModel);
  }

  private String formatResultModel(final ResultFormat format, final ViewStatusModel viewStatusModel) {
    final StringWriter stringWriter = new StringWriter();
    final Configuration cfg = new Configuration();
    try {
      cfg.setClassForTemplateLoading(ViewStatusResultProducer.class, "");
      final String templateName = getTemplateName(format);
      final Template template = cfg.getTemplate(templateName);
      final Map<String, Object> input = new HashMap<>();
      input.put("viewStatus", viewStatusModel);
      template.process(input, stringWriter);
      stringWriter.flush();

    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("Error generating html format for View status report", e);
    }
    return stringWriter.toString();
  }

  private String getTemplateName(final ResultFormat format) {
    switch (format) {
      case HTML:
        return "view-status-html.ftl";
      case CSV:
        return "view-status-csv.ftl";
      case XML:
        return "view-status-xml.ftl";
      default:
        throw new OpenGammaRuntimeException("Unsupported format: " + format.name());
    }
  }

}
