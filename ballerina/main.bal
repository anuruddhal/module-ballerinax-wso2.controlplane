import ballerina/lang.runtime;
import ballerina/log;
import ballerina/task;


function init() returns error? {
    log:printInfo("Starting ICP Agent...");

    // Load configuration
    IcpConfig config = check loadConfig();
    log:printInfo("Loaded ICP configuration: " + config.toJsonString());

    // Initialize ICP client
    IcpClient icpClient = check new (config);
    log:printInfo("ICP Client initialized with server URL: " + config.icp.serverUrl);

    // Get initial integration status
    IntegrationStatus integrations = check getCurrentIntegrations();
    log:printInfo("Current integrations: " + integrations.toJsonString());

    // Register with ICP server
    check icpClient->registerRuntime(integrations);
    log:printInfo("Runtime registered with ICP server");

    // Start periodic heartbeat
    HeartbeatJob heartbeatJob = new (icpClient, config.icp.heartbeatInterval);
    task:JobId|task:Error result = task:scheduleJobRecurByFrequency(heartbeatJob, config.icp.heartbeatInterval);
    if result is task:Error {
        log:printError("Failed to start heartbeat job", result);
        return error("Heartbeat scheduling failed");
    }

    log:printInfo("ICP Agent started successfully with job ID: " + result.toString());

    // Keep the main function running to allow periodic tasks to execute
    while true {
        // Sleep for a while to prevent busy waiting
        runtime:sleep(1000);
    }
}

// Heartbeat job
public class HeartbeatJob {
    *task:Job;
    private final IcpClient icpClient;
    private final decimal interval;

    public function init(IcpClient icpClient, decimal interval) {
        self.icpClient = icpClient;
        self.interval = interval;
    }

    # Executes the heartbeat job.
    public function execute() {
        // Get current integration status
        IntegrationStatus|error integrationStatus = getCurrentIntegrations();
        if integrationStatus is error {
            log:printError("Failed to get current integrations to send heartbeat", integrationStatus);
            return;
        }
        error? result = self.icpClient->sendHeartbeat(integrationStatus);
        if result is error {
            log:printError("Failed to send heartbeat", result);
        } else {
            log:printInfo("Heartbeat sent successfully");
        }
    }
}
