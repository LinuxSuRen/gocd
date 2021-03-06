/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.plugin.access.configrepo;

import com.thoughtworks.go.plugin.access.common.AbstractExtension;
import com.thoughtworks.go.plugin.access.configrepo.contract.CRParseResult;
import com.thoughtworks.go.plugin.access.notification.NotificationExtension;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.infra.PluginManager;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.HashMap;

import static com.thoughtworks.go.util.ArrayUtil.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConfigRepoExtensionTest {
    public static final String PLUGIN_ID = "plugin-id";

    @Mock
    private PluginManager pluginManager;
    @Mock
    private JsonMessageHandler1_0 jsonMessageHandler;
    private ConfigRepoExtension extension;
    private String requestBody = "expected-request";
    private String responseBody = "expected-response";

    private ArgumentCaptor<GoPluginApiRequest> requestArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        extension = new ConfigRepoExtension(pluginManager);
        extension.getMessageHandlerMap().put("1.0", jsonMessageHandler);

        requestArgumentCaptor = ArgumentCaptor.forClass(GoPluginApiRequest.class);

        when(pluginManager.resolveExtensionVersion(PLUGIN_ID, asList("1.0"))).thenReturn("1.0");
        when(pluginManager.isPluginOfType(ConfigRepoExtension.EXTENSION_NAME, PLUGIN_ID)).thenReturn(true);
        when(pluginManager.submitTo(eq(PLUGIN_ID), requestArgumentCaptor.capture())).thenReturn(DefaultGoPluginApiResponse.success(responseBody));
    }

    @Test
    public void shouldExtendAbstractExtension() throws Exception {
        assertTrue(extension instanceof AbstractExtension);
    }

    @Test
    public void shouldTalkToPluginToGetParsedDirectory() throws Exception {
        CRParseResult deserializedResponse = new CRParseResult();
        when(jsonMessageHandler.responseMessageForParseDirectory(responseBody)).thenReturn(deserializedResponse);

        CRParseResult response = extension.parseDirectory(PLUGIN_ID, "dir", null);

        assertRequest(requestArgumentCaptor.getValue(), ConfigRepoExtension.EXTENSION_NAME, "1.0", ConfigRepoExtension.REQUEST_PARSE_DIRECTORY, null);
        verify(jsonMessageHandler).responseMessageForParseDirectory(responseBody);
        assertSame(response, deserializedResponse);
    }

    @Test
    public void shouldSerializePluginSettingsToJSON() throws Exception {
        String pluginId = "plugin_id";
        HashMap<String, String> pluginSettings = new HashMap<>();
        pluginSettings.put("key1", "val1");
        pluginSettings.put("key2", "val2");

        ConfigRepoExtension configRepoExtension = new ConfigRepoExtension(pluginManager);

        when(pluginManager.resolveExtensionVersion(pluginId, configRepoExtension.goSupportedVersions())).thenReturn("1.0");
        String pluginSettingsJSON = configRepoExtension.pluginSettingsJSON(pluginId, pluginSettings);

        assertThat(pluginSettingsJSON, CoreMatchers.is("{\"key1\":\"val1\",\"key2\":\"val2\"}"));
    }

    private void assertRequest(GoPluginApiRequest goPluginApiRequest, String extensionName, String version, String requestName, String requestBody) {
        assertThat(goPluginApiRequest.extension(), is(extensionName));
        assertThat(goPluginApiRequest.extensionVersion(), is(version));
        assertThat(goPluginApiRequest.requestName(), is(requestName));
        assertThat(goPluginApiRequest.requestBody(), is(requestBody));
    }

}