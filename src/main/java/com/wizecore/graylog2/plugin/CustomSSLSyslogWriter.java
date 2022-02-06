package com.wizecore.graylog2.plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogRuntimeException;
import org.graylog2.syslog4j.impl.AbstractSyslog;
import org.graylog2.syslog4j.impl.AbstractSyslogWriter;
import org.graylog2.syslog4j.util.SyslogUtility;
import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogWriter;
import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfig;
import org.graylog2.syslog4j.impl.net.tcp.ssl.SSLTCPNetSyslogConfigIF;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.logging.Logger;
import java.util.Arrays;

public class CustomSSLSyslogWriter extends SSLTCPNetSyslogWriter {
        private Logger log = Logger.getLogger(CustomSSLSyslogWriter.class.getName());
        private SSLContext sslContext;

        public void initialize(AbstractSyslog abstractSyslog) {
                super.initialize(abstractSyslog);

                // initialize SSL/TLS settings
                SSLTCPNetSyslogConfigIF cfg = (SSLTCPNetSyslogConfigIF) this.tcpNetSyslogConfig;
                sslContext = createSSLContext(cfg);

        }

        private void reportError(Exception ex, String msg) {
                String n = ex.getClass().getName();
                log.warning(n +" caught " + msg +": " + ex.getMessage());
        }

        private SSLContext createSSLContext(SSLTCPNetSyslogConfigIF cfg) {

                // from: https://docs.oracle.com/en/java/javase/11/security/java-secure-socket-extension-jsse-reference-guide.html
                // First initialize the key and trust material
                String keyStore = cfg.getKeyStore();
                String keyStorePassword = cfg.getKeyStorePassword();
                KeyStore ksKeys, ksTrust;
                try {
                        // doesn't work in java8:
                        // ksKeys = KeyStore.getInstance(new File(keyStore), keyStorePassword.toCharArray());
                        ksKeys = KeyStore.getInstance(KeyStore.getDefaultType());
                        FileInputStream fis = new FileInputStream(keyStore);
                        ksKeys.load(fis, keyStorePassword.toCharArray());
                } catch(KeyStoreException|IOException|NoSuchAlgorithmException|CertificateException ex) {
                        reportError(ex, "creating keystore from file: " + keyStore);
                        return null;
                }

                String trustStore = cfg.getTrustStore();
                String trustStorePassword = cfg.getTrustStorePassword();
                char[] tsPass = null;
                if(!(trustStorePassword == null || trustStorePassword.isEmpty())) {
                        tsPass = trustStorePassword.toCharArray();
                }
                try {
                        // doesn't work in java8:
                        // ksTrust = KeyStore.getInstance(new File(trustStore), tsPass);
                        ksTrust = KeyStore.getInstance(KeyStore.getDefaultType());
                        FileInputStream fis = new FileInputStream(trustStore);
                        ksTrust.load(fis, tsPass);
                } catch(KeyStoreException|IOException|NoSuchAlgorithmException|CertificateException ex) {
                        reportError(ex, "trustStore from file: " + trustStore);
                        return null;
                }

                // KeyManagers decide which key material to use
                KeyManagerFactory kmf;
                try {
                        kmf = KeyManagerFactory.getInstance("PKIX");
                        kmf.init(ksKeys, keyStorePassword.toCharArray());
                } catch(NoSuchAlgorithmException|KeyStoreException|UnrecoverableKeyException ex) {
                        reportError(ex, "creating/initializing keymanagerfactory");
                        return null;
                }

                // TrustManagers decide whether to allow connections
                TrustManagerFactory tmf;
                try {
                        tmf = TrustManagerFactory.getInstance("PKIX");
                        tmf.init(ksTrust);
                } catch(NoSuchAlgorithmException|KeyStoreException ex) {
                        reportError(ex, "creating/initializing trustmanagerfactory");
                        return null;
                }

                // Get an instance of SSLContext for TLS protocols
                SSLContext ctx;
                try {
                        ctx = SSLContext.getInstance("TLS");
                        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                } catch(NoSuchAlgorithmException|KeyManagementException ex) {
                        reportError(ex, "creating/initializiing SSLcontext");
                        return null;
                }
                return ctx;

        }

        protected SocketFactory obtainSocketFactory() {
                if(sslContext == null) {
                        sslContext = createSSLContext((SSLTCPNetSyslogConfigIF) this.tcpNetSyslogConfig);
                        if(sslContext == null) {
                                log.warning("Cannot create SSLContext.");
                                return null;
                        }
                }
                return sslContext.getSocketFactory();
        }

}
