package com.wizecore.graylog2.plugin.ssl;

import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfig;

public class CustomSSLSyslogConfig extends SSLTCPNetSyslogConfig {
    public Class<CustomSSLSyslogWriter> getSyslogWriterClass() {
        return CustomSSLSyslogWriter.class;
    }
}
