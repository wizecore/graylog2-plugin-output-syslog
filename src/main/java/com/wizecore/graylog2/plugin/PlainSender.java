package com.wizecore.graylog2.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;

/**
 * Formats fields into message text 
 */
public class PlainSender implements MessageSender {
	private Logger log = Logger.getLogger(PlainSender.class.getName());

	public static final String SYSLOG_DATEFORMAT = "MMM dd HH:mm:ss";
	
	/**
	 * From syslog4j
	 * 
	 * @param dt
	 * @return
	 */
	public static String formatTimestamp(Date dt) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(SYSLOG_DATEFORMAT,Locale.ENGLISH);		
		String datePrefix = dateFormat.format(dt);	
		
		StringBuilder buffer = new StringBuilder();
		int pos = buffer.length() + 4;		
		buffer.append(datePrefix);
	
		//  RFC 3164 requires leading space for days 1-9
		if (buffer.charAt(pos) == '0') {
			buffer.setCharAt(pos,' ');
		}	
		return buffer.toString();
	}
	
	@Override
	public void send(SyslogIF syslog, int level, Message msg) {
		Date dt = null;
		Object ts = msg.getField("timestamp");
		if (ts != null && ts instanceof Number) {
			dt = new Date(((Number) ts).longValue());
		}
		
		if (dt == null) {
			dt = new Date();
		}
		
		StringBuilder out = new StringBuilder();
		
		// Write time
		out.append(formatTimestamp(dt));
		out.append(" ");
		
		// Write source (host)
		String source = msg.getSource();
		if (source != null) {
			out.append(source).append(" ");
		}
		
		// Write service
		Object facility = msg.getField("facility");
		if (facility != null) {
			out.append("[").append(facility.toString()).append("]");
		}
		
		Object username = msg.getField("username");
		if (username != null) {
			out.append("[").append(username.toString()).append("]");
		}
		
		if (out.length() > 0) {
			out.append(' ');
		}
		
		out.append(msg.getMessage());
		String str = out.toString();
		log.info("Sending plain message: " + level + ", " + str);
		syslog.log(level, str);
	}
}
