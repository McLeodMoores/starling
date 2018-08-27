/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool;

import java.io.FileWriter;

import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.paging.PagingRequest;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Tool to generate a template for doing field mapping tasks
 */
@Scriptable
public class SecurityFieldMappingTemplateGenerator extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFieldMappingTemplateGenerator.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new SecurityFieldMappingTemplateGenerator().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final CSVWriter csvWriter = new CSVWriter(new FileWriter(getCommandLine().getArgs()[0]));
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final SecurityMetaDataRequest metaRequest = new SecurityMetaDataRequest();
    final SecurityMetaDataResult metaData = securityMaster.metaData(metaRequest);
    for (final String securityType : metaData.getSecurityTypes()) {
      LOGGER.info("Processing security type " + securityType);
      final SecuritySearchRequest searchRequest = new SecuritySearchRequest();
      searchRequest.setName("*");
      searchRequest.setSecurityType(securityType);
      searchRequest.setPagingRequest(PagingRequest.ONE);
      final SecuritySearchResult search = securityMaster.search(searchRequest);
      LOGGER.info("Search returned " + search.getPaging().getTotalItems() + " securities");
      dumpSecurityStructure(csvWriter, securityType, search.getFirstSecurity());
    }
    csvWriter.close();
  }

  private void dumpSecurityStructure(final CSVWriter csvWriter, final String securityType, final ManageableSecurity firstSecurity) {
    if (firstSecurity == null) {
      LOGGER.error("null security passed to dumpSecurityStructure");
      return;
    }
    LOGGER.info("Processing security " + firstSecurity);
    csvWriter.writeNext(new String[] {securityType });
    csvWriter.writeNext(new String[] {firstSecurity.metaBean().beanName() });
    csvWriter.writeNext(new String[] {"Type", "Name", "Example"});
    final Iterable<MetaProperty<?>> metaPropertyIterable = firstSecurity.metaBean().metaPropertyIterable();
    for (final MetaProperty<?> metaProperty : metaPropertyIterable) {
      LOGGER.info("Field" + metaProperty.name());
      String strValue;
      try {
        strValue = metaProperty.getString(firstSecurity);
      } catch (final IllegalStateException ise) {
        strValue = metaProperty.get(firstSecurity).toString();
      }
      csvWriter.writeNext(new String[] {metaProperty.propertyType().getSimpleName(), metaProperty.name(), strValue });
    }
    csvWriter.writeNext(new String[] {});
  }

}
