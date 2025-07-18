import ballerina/http;
import ballerina/log;
import ballerina/uuid;

public client class IcpClient {
    private final http:Client httpClient;
    private final IcpConfig config;

    public function init(IcpConfig config) returns http:ClientError? {
        self.config = config;
        self.httpClient = check new (config.icp.serverUrl);
    }

    // Register runtime with ICP server
    isolated remote function registerRuntime(IntegrationStatus integrations) returns error? {
        RuntimeRegistration payload = {
            runtimeId: uuid:createRandomUuid(),
            runtimeType: self.config.runtime.runtimeType,
            version: integrations.node.ballerinaHome,
            environment: self.config.runtime.environment,
            hostname: "localhost",
            integrations: {
                count: integrations.artifacts.length(),
                list: integrations.artifacts
            },
            metricsEnabled: self.config.observability.metricsEnabled
        };

        http:Request request = new;
        request.setHeader("Authorization", self.config.icp.authToken);
        request.setPayload(payload);
        log:printInfo("Registering runtime with ICP server: " + payload.toString());
        // http:Response response = check self.httpClient->post("/register", request);
        // if response.statusCode != http:STATUS_CREATED {
        //     log:printError("Failed to register runtime with ICP server");
        //     return error("Registration failed ");
        // }
    }

    // Send heartbeat to ICP server
    isolated remote function sendHeartbeat(IntegrationStatus integrationStatus) returns error? {
        Heartbeat payload = {
            runtimeId: self.config.runtime.id,
            integrations: integrationStatus,
            opensearchUrl: self.config.observability.opensearchUrl,
            metricsEnabled: self.config.observability.metricsEnabled
        };

        http:Request request = new;
        request.setHeader("Authorization", self.config.icp.authToken);
        request.setPayload(payload);
        log:printInfo("Sending heartbeat to ICP server: " + payload.toJsonString());

        // http:Response response = check self.httpClient->post("/heartbeat", request);
        // if response.statusCode != http:STATUS_OK {
        // log:printWarn("Heartbeat failed: " + response.statusCode.toString());
        // return error("Heartbeat failed");
        // }
    }

}
