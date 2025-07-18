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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.repository.Node;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.PrintStream;

import static io.ballerina.lib.wso2.controlplane.Constants.ARTIFACT;
import static io.ballerina.lib.wso2.controlplane.Constants.BALLERINA_HOME;
import static io.ballerina.lib.wso2.controlplane.Constants.BAL_HOME;
import static io.ballerina.lib.wso2.controlplane.Constants.BAL_VERSION;
import static io.ballerina.lib.wso2.controlplane.Constants.NAME;
import static io.ballerina.lib.wso2.controlplane.Constants.NODE;
import static io.ballerina.lib.wso2.controlplane.Constants.OS_NAME;
import static io.ballerina.lib.wso2.controlplane.Constants.OS_VERSION;
import static io.ballerina.lib.wso2.controlplane.Constants.PLATFORM_VERSION;

/**
 * Native function implementations of the wso2 control plane module.
 *
 * @since 1.0.0
 */
public class Utils {

    public static Object getBallerinaNode(Environment env) {
        Module currentModule = env.getCurrentModule();
        PrintStream out = System.out;
        out.println(env.getCurrentModule());
        out.println(env.getRepository());
        out.println(env.getRepository().getArtifacts());
        Node node = env.getRepository().getNode();
        BMap<BString, Object> nodeEntries = ValueCreator.createMapValue();
        nodeEntries.put(StringUtils.fromString(PLATFORM_VERSION),
                StringUtils.fromString(getBallerinaVersionString((String) node.getDetail(BAL_VERSION))));
        nodeEntries.put(StringUtils.fromString(BALLERINA_HOME),
                StringUtils.fromString((String) node.getDetail(BAL_HOME)));
        nodeEntries.put(StringUtils.fromString(Constants.OS_NAME),
                StringUtils.fromString((String) node.getDetail(OS_NAME)));
        nodeEntries.put(StringUtils.fromString(OS_VERSION),
                StringUtils.fromString((String) node.getDetail(OS_VERSION)));
        return ValueCreator.createReadonlyRecordValue(currentModule, NODE, nodeEntries);
    }

    private static String getBallerinaVersionString(String detail) {
        String version = detail.split("-")[0];
        int minorVersion = Integer.parseInt(version.split("\\.")[1]);
        String updateVersionText = minorVersion > 0 ? " Update " + minorVersion : "";
        return "Ballerina " + version + " (Swan Lake Update " + updateVersionText + ")";
    }

    public static boolean isControlPlaneService(BObject serviceObj, Module currentModule) {
        Type originalType = serviceObj.getOriginalType();
        Module module = originalType.getPackage();
        return module != null && module.equals(currentModule);
    }

    public static BMap<BString, Object> getArtifact(String name, Module module) {
        BMap<BString, Object> artifact = ValueCreator.createMapValue();
        artifact.put(StringUtils.fromString(NAME), StringUtils.fromString(name));
        return ValueCreator.createReadonlyRecordValue(module, ARTIFACT, artifact);
    }

}
