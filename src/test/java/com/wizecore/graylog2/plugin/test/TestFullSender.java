package com.wizecore.graylog2.plugin.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executors;

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


public class TestFullSender implements Runnable {
	
	
	int portStart = 45000;
	int portEnd = 45100;
	int port = 0;
	private ServerSocket listen;
	private String hostname;
	private int receivedSymbols;
	
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
		assertTrue(receivedSymbols >= 1024 * 16);
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
				receivedSymbols += s.length();
			}
			conn.close();
		} catch (IOException e) {
			
		}
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
