package com.opengamma.integration.tool.convention;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Class to load conventions from an input stream.
 */
public class ConventionLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConventionLoader.class);
  private final ConventionMaster _conventionMaster;
  private final boolean _actuallyStore;
  private final boolean _verbose;
  private boolean _attemptToPortPortfolioIds;

  public ConventionLoader(final ConventionMaster conventionMaster, final boolean actuallyStore, final boolean verbose) {
    _conventionMaster = conventionMaster;
    _actuallyStore = actuallyStore;
    _verbose = verbose;
  }

  public void loadConvention(final InputStream inputStream) {
    try (final FudgeXMLStreamReader xmlStreamReader = new FudgeXMLStreamReader(OpenGammaFudgeContext.getInstance(), new InputStreamReader(new BufferedInputStream(inputStream)))) {
      final FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(xmlStreamReader);
      final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
      final FudgeMsg conventionsMessage = fudgeMsgReader.nextMessage();
      if (conventionsMessage == null) {
        LOGGER.error("Error reading first message from XML stream");
        return;
      }
      final Object object = deserializer.fudgeMsgToObject(FlexiBean.class, conventionsMessage);
      if (!(object instanceof FlexiBean)) {
        LOGGER.error("XML Stream deserialised to object of type " + object.getClass() + ": " + object.toString());
        return;
      }
      final FlexiBean wrapper = (FlexiBean) object;
      if (!wrapper.contains("conventions")) {
        LOGGER.error("File stream does not contain conventions element");
        return;
      }
      @SuppressWarnings("unchecked")
      final
      List<ConventionEntry> conventions = (List<ConventionEntry>) wrapper.get("conventions");
      loadConventions(conventions, Collections.<UniqueId, String>emptyMap());
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  private void loadConventions(final List<ConventionEntry> convention, final Map<UniqueId, String> idNameMap) {
    for (final ConventionEntry entry : convention) {
      final ConventionType type = ConventionType.of(entry.getType());
      final Object object = entry.getObject();
      if (!(object instanceof ManageableConvention)) {
        LOGGER.error("Object {} was not a ManageableConvention; ignoring", object);
        continue;
      }
      final ManageableConvention item = (ManageableConvention) object;
      if (!item.getConventionType().getName().equals(entry.getType())) {
        LOGGER.error("Type of convention {} did not match that in ConventionEntry {}; ignoring", item, type);
        continue;
      }
      if (!item.getName().equals(entry.getName())) {
        LOGGER.error("Name of convention {} did not match that in ConventionEntry {}; ignoring", item, entry.getName());
        continue;
      }
      if (_actuallyStore) {
        _conventionMaster.add(new ConventionDocument(item));
        if (_verbose) {
          LOGGER.info("Stored " + entry.getName() + " of type " + entry.getType());
        }
      } else {
        if (_verbose) {
          LOGGER.info("Simulated store " + entry.getName() + " of type " + entry.getType());
        }
      }
    }
  }

}
