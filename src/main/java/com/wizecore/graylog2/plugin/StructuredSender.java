package com.wizecore.graylog2.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.message.structured.StructuredSyslogMessage;

public class StructuredSender implements MessageSender {
	private Logger log = Logger.getLogger(StructuredSender.class.getName());

	@Override
	public void send(SyslogIF syslog, int level, Message msg) {		
		Map<String, Map<String, String>> data = new HashMap<String, Map<String,String>>();
		Map<String, Object> fields = msg.getFields();
		for (String key: fields.keySet()) {
			Map<String,String> inner = new HashMap<String, String>();
			inner.put(key, fields.get(key).toString());
			data.put(key, inner);
		}
		
		log.info("Sending " + level + ", " + msg.getId() + ", " + msg.getSource() + ", " + data + ", " + msg.getMessage());
		syslog.log(level, new StructuredSyslogMessage(msg.getId(), msg.getSource(), data, msg.getMessage()));
	}
}
