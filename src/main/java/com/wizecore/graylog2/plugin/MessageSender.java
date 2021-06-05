package com.wizecore.graylog2.plugin;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;

/**
 * Optimized sender
 */
public interface MessageSender {
	void send(SyslogIF syslog, int level, Message msg);
}