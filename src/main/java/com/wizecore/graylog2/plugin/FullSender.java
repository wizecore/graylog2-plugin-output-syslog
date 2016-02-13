package com.wizecore.graylog2.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.message.structured.StructuredSyslogMessage;

/**
 * Sends full message to Syslog.
 * 
 * <165>1 2003-10-11T22:14:15.003Z mymachine.example.com
           evntslog - ID47 [exampleSDID@0 iut="3" eventSource=
           "Application" eventID="1011"] BOMAn application
           event log entry...

 */
public class FullSender implements MessageSender {
	private Logger log = Logger.getLogger(FullSender.class.getName());

	@Override
	public void send(SyslogIF syslog, int level, Message msg) {		
		Map<String, String> sdParams = new HashMap<String, String>();
		Map<String, Object> fields = msg.getFields();
		for (String key: fields.keySet()) {
			sdParams.put(key, fields.get(key).toString());
		}
		
		// http://www.iana.org/assignments/enterprise-numbers/enterprise-numbers
		// <name>@<enterpriseId>
		String sdId = "all@0";
		// log.info("Sending " + level + ", " + msg.getId() + ", " + msg.getSource() + ", " + sdId + "=" + sdParams + ", " + msg.getMessage());
		Map<String,Map<String,String>> sd = new HashMap<String, Map<String,String>>();
		sd.put(sdId, sdParams);
		
		String msgId = null;		
		if (msgId == null) {
			String source = msg.getSource();
    		if (source != null) {
    			msgId = source;
    		}
		}		
		if (msgId == null) {
			msgId = "-";
		}
		
		String sourceId = null;
		if (sourceId == null) {
			Object facility = msg.getField("facility");
    		if (facility != null) {
    			sourceId = facility.toString();
    		}
		}		
		if (sourceId == null) {
			sourceId = "-";
		}

		syslog.log(level, new StructuredSyslogMessage(msgId, sourceId, sd, msg.toString()));
	}
}
