package com.wizecore.graylog2.plugin.ssl;

import java.io.IOException;
import java.io.FileInputStream;

import javax.net.SocketFactory;

import org.graylog2.syslog4j.impl.AbstractSyslog;
import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogWriter;
import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfigIF;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class CustomSSLSyslogWriter extends SSLTCPNetSyslogWriter {
    private Logger log = Logger.getLogger(CustomSSLSyslogWriter.class.getName());
    private SSLContext sslContext;
    protected SSLTCPNetSyslogConfigIF sslNetSyslogConfig;

    public void initialize(AbstractSyslog abstractSyslog) {
        super.initialize(abstractSyslog);
        sslNetSyslogConfig = (SSLTCPNetSyslogConfigIF) this.tcpNetSyslogConfig;
        sslNetSyslogConfig.setUseOctetCounting(true);
        sslNetSyslogConfig.setPersistentConnection(true);
    }

    private SSLContext createSSLContext() throws IOException, GeneralSecurityException {
        String keyStore = sslNetSyslogConfig.getKeyStore();
        String keyStorePassword = sslNetSyslogConfig.getKeyStorePassword();

        KeyStore ksKeys = KeyStore.getInstance(KeyStore.getDefaultType());
        log.info("Reading " + keyStore);
        FileInputStream fis = new FileInputStream(keyStore);
        try {
            ksKeys.load(fis, keyStorePassword.toCharArray());
        } finally {
            fis.close();
        }

        String trustStore = sslNetSyslogConfig.getTrustStore();
        String trustStorePassword = sslNetSyslogConfig.getTrustStorePassword();
        char[] tsPass = null;
        if (trustStorePassword != null && !trustStorePassword.isEmpty()) {
            tsPass = trustStorePassword.toCharArray();
        }

        log.info("Reading " + trustStore);
        KeyStore ksTrust = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis2 = new FileInputStream(trustStore);
        try {
            ksTrust.load(fis2, tsPass);
        } finally {
            fis2.close();
        }

        // KeyManagers decide which key material to use
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        kmf.init(ksKeys, keyStorePassword.toCharArray());

        // TrustManagers decide whether to allow connections
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ksTrust);
        

        // Get an instance of SSLContext for TLS protocols
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx;
    }

    protected SocketFactory obtainSocketFactory() {
        if (sslContext == null) {
            try {
                sslContext = createSSLContext();
            } catch (GeneralSecurityException | IOException e) {
                log.log(Level.WARNING, "Failed to create SSLContext: " + e.getMessage(), e);
                throw new RuntimeException("Failed to create SSLContext: " + e.getMessage(), e);
            }
        }
        return sslContext.getSocketFactory();
    }

}
