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

package com.thoughtworks.go.plugin.access.pluggabletask;

import com.thoughtworks.go.plugin.access.PluginRequestHelper;
import com.thoughtworks.go.plugin.access.common.AbstractExtension;
import com.thoughtworks.go.plugin.access.common.settings.PluginSettingsJsonMessageHandler1_0;
import com.thoughtworks.go.plugin.access.common.settings.JsonMessageHandlerForRequestProcessor;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.infra.Action;
import com.thoughtworks.go.plugin.infra.ActionWithReturn;
import com.thoughtworks.go.plugin.infra.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class TaskExtension extends AbstractExtension {
    private final static List<String> supportedVersions = Arrays.asList(JsonBasedTaskExtensionHandler_V1.VERSION);

    public final static String TASK_EXTENSION = "task";
    public final static String CONFIGURATION_REQUEST = "configuration";
    public final static String VALIDATION_REQUEST = "validate";
    public final static String EXECUTION_REQUEST = "execute";
    public final static String TASK_VIEW_REQUEST = "view";

    final HashMap<String, JsonBasedTaskExtensionHandler> messageHandlerMap = new HashMap<>();

    @Autowired
    public TaskExtension(PluginManager pluginManager) {
        super(pluginManager, new PluginRequestHelper(pluginManager, supportedVersions, TaskExtension.TASK_EXTENSION), TaskExtension.TASK_EXTENSION);
        registerHandler(JsonBasedTaskExtensionHandler_V1.VERSION, new PluginSettingsJsonMessageHandler1_0());
        messageHandlerMap.put(JsonBasedTaskExtensionHandler_V1.VERSION, new JsonBasedTaskExtensionHandler_V1());
    }

    public ExecutionResult execute(String pluginId, ActionWithReturn<Task, ExecutionResult> actionWithReturn) {
        JsonBasedPluggableTask task = new JsonBasedPluggableTask(pluginId, pluginRequestHelper, messageHandlerMap);
        return actionWithReturn.execute(task, pluginManager.getPluginDescriptorFor(pluginId));
    }

    public void doOnTask(String pluginId, Action<Task> action) {
        JsonBasedPluggableTask task = new JsonBasedPluggableTask(pluginId, pluginRequestHelper, messageHandlerMap);
        action.execute(task, pluginManager.getPluginDescriptorFor(pluginId));
    }

    public ValidationResult validate(String pluginId, TaskConfig taskConfig) {
        JsonBasedPluggableTask task = new JsonBasedPluggableTask(pluginId, pluginRequestHelper, messageHandlerMap);
        return task.validate(taskConfig);
    }

    @Override
    protected JsonMessageHandlerForRequestProcessor jsonMessageHandlerForRequestProcessor(String pluginVersion) {
        throw new UnsupportedOperationException("Fetch PluginSettings is not supported by Task Endpoint.");
    }

    @Override
    protected List<String> goSupportedVersions() {
        return supportedVersions;
    }
}
