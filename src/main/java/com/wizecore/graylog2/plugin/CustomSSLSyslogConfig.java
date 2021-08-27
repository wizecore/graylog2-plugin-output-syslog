package com.wizecore.graylog2.plugin;


import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfig;

public class CustomSSLSyslogConfig extends SSLTCPNetSyslogConfig {
        public Class getSyslogWriterClass() {
                return CustomSSLSyslogWriter.class;
        }

}
