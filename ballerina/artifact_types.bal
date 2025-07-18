import ballerina/uuid;
public type Heartbeat record {|
    string runtimeId;
    IntegrationStatus integrations;
    string opensearchUrl?;
    boolean metricsEnabled?;
|};

public enum RuntimeType {
    MI,
    BI
}

public enum RuntimeStatus {
    RUNNING,
    FAILED,
    DISABLED
}

public type RuntimeRegistration record {
    string runtimeId;
    RuntimeType runtimeType = BI;
    string version?;
    string environment; // "K8S"|"VM"
    string hostname;
    string region?;
    string zone?;
    Artifacts integrations;
    boolean metricsEnabled;
};

public type IcpServer record {|
    string serverUrl;
    string authToken;
    decimal heartbeatInterval;
|};

public type Observability record {|
    string opensearchUrl;
    string logIndex;
    boolean metricsEnabled;
|};

public type Runtime record {|
    string id = uuid:createRandomUuid();
    RuntimeType runtimeType = "BI";
    string environment = "K8S";
|};

public type IcpConfig record {|
    IcpServer icp;
    Observability observability;
    Runtime runtime;
|};

public type IntegrationMetadata record {|
    string name;
    string version;
    boolean logsEnabled;
    boolean metricsEnabled;
|};
