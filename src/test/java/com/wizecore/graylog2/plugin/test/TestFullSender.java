package com.wizecore.graylog2.plugin.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wizecore.graylog2.plugin.FullSender;


public class TestFullSender extends SimpleSocketTcpServer {

    @Before
    public void initServer() throws IOException {
        startServer();
    }

    @After
    public void countSymbols() throws IOException {
        System.err.println("Received symbols total: " + receivedBytes);
        assertTrue(receivedBytes >= 1024 * 16);
        listen.close();
    }

	@Test
	public void testMessageTruncation() throws InterruptedException {
		FullSender s = new FullSender();
		
		SyslogConfigIF config = new TCPNetSyslogConfig();
		config.setHost(hostname);
		config.setPort(port);
		config.setTruncateMessage(true);
		config.setMaxMessageLength(1024 * 16);
		char[] buf = new char[16384];
		double decims = Math.ceil(buf.length / 10.0);
		char[] dec = "1234567890".toCharArray();
		for (int i = 0; i < decims; i++) {
			System.arraycopy(dec, 0, buf, i * 10, Math.min(buf.length - i * 10, 10));
		}
		Message msg = new Message(new String(buf), "localhost", new DateTime());
		System.out.println("Original message: ");
		System.out.println(msg);
		SyslogIF syslog = Syslog.createInstance("tcp_" + System.currentTimeMillis(), config);
		s.send(syslog, Syslog.LEVEL_INFO, msg);
		Thread.sleep(1000);
	}
}
