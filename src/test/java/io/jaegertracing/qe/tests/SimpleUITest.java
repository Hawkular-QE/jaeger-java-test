/**
 * Copyright 2017-2018 The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jaegertracing.qe.tests;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleUITest {
    private static Map<String, String> evs = System.getenv();
    private static final String JAEGER_QUERY_HOST = evs.getOrDefault("JAEGER_QUERY_HOST", "jaeger-query");
    private static final Integer JAEGER_QUERY_SERVICE_PORT = new Integer(evs.getOrDefault("JAEGER_QUERY_SERVICE_PORT", "80"));
    private static final Logger logger = LoggerFactory.getLogger(SimpleUITest.class);

    /**
     * A very simple test to see if the Jaeger UI responds
     *
     * @throws IOException if it cannot open the page
     */
    @Test
    public void verifyUIRespondsTest() throws IOException {
        // Turn off HTMLUnit logging as it complains about javascript issues that are not relevant to this test
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        try (final WebClient webClient = new WebClient()) {
            WebClientOptions webClientOptions = webClient.getOptions();
            webClientOptions.setThrowExceptionOnScriptError(false);

            final String uiUrl = "http://" + JAEGER_QUERY_HOST + ":" + JAEGER_QUERY_SERVICE_PORT + "/search";
            logger.info("Connecting to Jaeger UI at :   " + uiUrl);
            final HtmlPage page = webClient.getPage(uiUrl);
            final String pageAsXml = page.asXml();
            final String pageAsText = page.asText();

            assertEquals("Jaeger UI", page.getTitleText());
            assertEquals("Jaeger UI", pageAsText);
            assertTrue(pageAsXml.contains("jaeger-ui-root"));
        }
    }
}
