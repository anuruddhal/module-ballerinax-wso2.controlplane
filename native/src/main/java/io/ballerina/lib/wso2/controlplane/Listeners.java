/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.lib.wso2.controlplane;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.repository.Artifact;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.lib.wso2.controlplane.Artifacts.LISTENER_NAMES_MAP;
import static io.ballerina.lib.wso2.controlplane.Constants.HOST;
import static io.ballerina.lib.wso2.controlplane.Constants.HTTP;
import static io.ballerina.lib.wso2.controlplane.Constants.HTTPS;
import static io.ballerina.lib.wso2.controlplane.Constants.HTTP_VERSION;
import static io.ballerina.lib.wso2.controlplane.Constants.INFERRED_CONFIG;
import static io.ballerina.lib.wso2.controlplane.Constants.LISTENERS;
import static io.ballerina.lib.wso2.controlplane.Constants.LISTENER_DETAIL;
import static io.ballerina.lib.wso2.controlplane.Constants.NAME;
import static io.ballerina.lib.wso2.controlplane.Constants.PACKAGE;
import static io.ballerina.lib.wso2.controlplane.Constants.PORT;
import static io.ballerina.lib.wso2.controlplane.Constants.PROTOCOL;
import static io.ballerina.lib.wso2.controlplane.Constants.REQUEST_LIMIT;
import static io.ballerina.lib.wso2.controlplane.Constants.REQUEST_LIMITS;
import static io.ballerina.lib.wso2.controlplane.Constants.SECURE_SOCKET;
import static io.ballerina.lib.wso2.controlplane.Constants.SERVICE;
import static io.ballerina.lib.wso2.controlplane.Constants.TIMEOUT;
import static io.ballerina.lib.wso2.controlplane.Utils.getArtifact;

/**
 * Native function implementations of the wso2 control plane module.
 *
 * @since 1.0.0
 */
public class Listeners {
    public List<BListInitialValueEntry> getListenerList(Module currentModule) {
        List<BListInitialValueEntry> artifactEntries = new ArrayList<>();
        Set<BObject> listeners = getNonDuplicatedListeners(Artifacts.artifacts, currentModule);
        for (BObject listener : listeners) {
            artifactEntries.add(ValueCreator.createListInitialValueEntry(
                    getArtifact(LISTENER_NAMES_MAP.get(listener), currentModule)));
        }
        return artifactEntries;
    }

    private Set<BObject> getNonDuplicatedListeners(List<Artifact> artifacts, Module currentModule) {
        Set<BObject> listeners = new HashSet<>();
        for (Artifact artifact : artifacts) {
            if (Utils.isControlPlaneService((BObject) artifact.getDetail(SERVICE), currentModule)) {
                continue;
            }
            listeners.addAll((List<BObject>) artifact.getDetail(LISTENERS));
        }
        return listeners;
    }

    public BMap<BString, Object> getDetailedListener(BObject listener, Module currentModule) {
        Type originalType = listener.getOriginalType();
        BMap<BString, Object> listenerRecord = ValueCreator.createMapValue();
        listenerRecord.put(StringUtils.fromString(NAME), StringUtils.fromString(LISTENER_NAMES_MAP.get(listener)));
        listenerRecord.put(StringUtils.fromString(PROTOCOL), getListenerProtocol(listener));
        listenerRecord.put(StringUtils.fromString(PORT), listener.get(StringUtils.fromString(PORT)));
        listenerRecord.put(StringUtils.fromString(PACKAGE),
                StringUtils.fromString(originalType.getPackage().toString()));
        BMap<BString, Object> config = (BMap<BString, Object>)
                listener.get(StringUtils.fromString(INFERRED_CONFIG));
        listenerRecord.put(StringUtils.fromString(HTTP_VERSION),
                StringUtils.fromString(config.get(StringUtils.fromString(HTTP_VERSION)).toString()));
        listenerRecord.put(StringUtils.fromString(HOST),
                StringUtils.fromString(config.get(StringUtils.fromString(HOST)).toString()));
        listenerRecord.put(StringUtils.fromString(TIMEOUT),
                config.get(StringUtils.fromString(TIMEOUT)));
        listenerRecord.put(StringUtils.fromString(REQUEST_LIMITS), getRequestLimit(config, currentModule));
        return ValueCreator.createReadonlyRecordValue(currentModule, LISTENER_DETAIL, listenerRecord);
    }

    private static BMap<BString, Object> getRequestLimit(BMap<BString, Object> config, Module module) {
        return ValueCreator.createReadonlyRecordValue(module, REQUEST_LIMIT,
                (BMap<BString, Object>) config.getMapValue(StringUtils.fromString(REQUEST_LIMITS)));
    }

    private static BString getListenerProtocol(BObject listener) {
        BMap<BString, Object> config = (BMap<BString, Object>)
                listener.get(StringUtils.fromString(INFERRED_CONFIG));
        Object secureSocket = config.get(StringUtils.fromString(SECURE_SOCKET));
        return StringUtils.fromString(secureSocket == null ? HTTP : HTTPS);
    }

}
