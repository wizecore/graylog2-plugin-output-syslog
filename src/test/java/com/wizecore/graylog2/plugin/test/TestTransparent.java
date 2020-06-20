package com.wizecore.graylog2.plugin.test;

import com.eaio.uuid.UUID;
import com.wizecore.graylog2.plugin.TransparentSyslogSender;
import org.graylog2.inputs.converters.SyslogPriUtilities;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.message.processor.SyslogMessageProcessor;
import org.graylog2.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;


public class TestTransparent implements Runnable {
	int portStart = 45000;
	int portEnd = 45100;
	int port = 0;
	private ServerSocket listen;
	private String hostname;
	private StringBuilder receivedSymbols = new StringBuilder();
	
	@Before
	public void initTcpListener() throws IOException {
		port = 0;
		for (int i = portStart; i <= portEnd; i++) {
			try {
				InetAddress local = InetAddress.getLocalHost();
				hostname = local.getHostName();
				System.err.println("Trying to listen on tcp://" + hostname + ":" + i);
				listen = new ServerSocket(i, 10, local);
				Executors.newSingleThreadExecutor().execute(this);
				port = i;
				break;
			} catch (IOException e) {
				// Failed to create socket
			}
		}
		if (port == 0) {
			throw new IOException("Can`t bind to listen on one of ports " + portStart + "..." + portEnd);
		}
	}
	
	@After
	public void countSymbols() throws IOException {
		System.err.println("Received symbols total: " + receivedSymbols);
		listen.close();
	}
	
	@Override
	public void run() {
		try {
			Socket conn = listen.accept();             
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String s = null;
			while ((s = in.readLine()) != null) {
				System.out.println(s);
				receivedSymbols.append(s);
				receivedSymbols.append("\n");
			}
			conn.close();
		} catch (IOException e) {
			
		}
	}

	@Test
	public void testConvertGraylogPriority() {
		int p = 86;
		int f = SyslogPriUtilities.facilityFromPriority(p);
		int l = SyslogPriUtilities.levelFromPriority(p);
		System.out.println("Facility:" + f);
		System.out.println("Level:" + l);
	}

	@Test
	public void testMessageHeader() throws InterruptedException {
		TransparentSyslogSender s = new TransparentSyslogSender(new Configuration(new HashMap<String,Object>()));
		
		SyslogConfigIF config = new TCPNetSyslogConfig();
		config.setHost(hostname);
		config.setPort(port);
		config.setTruncateMessage(true);
		config.setMaxMessageLength(1024 * 16);
		HashMap<String,Object> fields = new HashMap<>();
		fields.put(Message.FIELD_MESSAGE, "localhost Hello, world!");
		fields.put(Message.FIELD_SOURCE, "localhost");
		fields.put(Message.FIELD_TIMESTAMP, DateTime.parse("2020-01-01T12:34:56.789"));
		fields.put(Message.FIELD_ID, (new UUID()).toString());
		fields.put("facility", "security/authorization");
		Message msg = new Message(fields);
		System.out.println("Original message: ");
		System.out.println(msg);
		SyslogIF syslog = Syslog.createInstance("tcp_" + System.currentTimeMillis(), config);
		syslog.setMessageProcessor(new SyslogMessageProcessor() {
			@Override
			public String createSyslogHeader(int facility, int level, String localName, boolean sendLocalName, Date datetime) {
				return "";
			}

			@Override
			public String createSyslogHeader(int facility, int level, String localName, boolean sendLocalTimestamp, boolean sendLocalName) {
				return "";
			}
		});
		s.send(syslog, Syslog.LEVEL_INFO, msg);
		Thread.sleep(1000);
	}
}
