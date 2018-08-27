package com.opengamma.web.analytics.push;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tests live updating of analytics from a real engine.  Requires an engine running on {@code localhost}.
 */
public class EngineAnalyticsTest {

  public static void main(final String[] args) throws IOException, JSONException {
    final WebPushTestUtils webPushTestUtils = new WebPushTestUtils();
    final String clientId = webPushTestUtils.handshake();
    final String viewDefJson = "{" +
        "\"viewDefinitionName\": \"Single Swap Test View\", " +
        //"\"snapshotId\": \"Tst~123\", " + // use live data
        "\"portfolioViewport\": {" +
        "\"rowIds\": [0, 1, 2, 3], " +
        "\"lastTimestamps\": [null, null, null, null], " +
        "\"dependencyGraphCells\": [[0, 0], [1, 1]]" +
        "}" +
        "}";
    final String viewportUrl = webPushTestUtils.createViewport(clientId, viewDefJson);
    // need to request data to activate the subscription
    final String firstResults = webPushTestUtils.readFromPath(viewportUrl + "/data", clientId);
    System.out.println("first results: " + firstResults);
    //noinspection InfiniteLoopStatement
    while (true) {
      final String urlJson = webPushTestUtils.readFromPath("/updates/" + clientId);
      System.out.println("updates: " + urlJson);
      if (!StringUtils.isEmpty(urlJson)) {
        final JSONObject urlsObject = new JSONObject(urlJson);
        final JSONArray updates = urlsObject.getJSONArray("updates");
        for (int i = 0; i < updates.length(); i++) {
          final String url = updates.getString(i);
          final String results = webPushTestUtils.readFromPath(url, clientId);
          System.out.println("url: " + url + ", results: " + results);
        }
      }
    }
  }
}
