package com.wizecore.graylog2.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;

import org.joda.time.DateTime;

/**
 * Formats fields into message text 
 * 

        <34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8
         ^priority
         	^ version
         	  ^ date 
         	  						   ^ host
         	  						   						 ^ APP-NAME
         	  						   						 	^ structured data?
         	  						   						 	  ^ MSGID 
         	  						   						 	  	    
 */
public class SnareWindowsSender implements MessageSender {
	private Logger log = Logger.getLogger(SnareWindowsSender.class.getName());

	public static final String SYSLOG_DATEFORMAT = "MMM dd HH:mm:ss";
  public static final String MSEVENT_DATEFORMAT = "EEE MMM dd HH:mm:ss yyyy";
	public static final String SEPARATOR = "\t";
	/**
	 * From syslog4j
	 * 
	 * @param dt
	 * @return
	 */
	public static void appendSyslogTimestamp(Date dt, StringBuilder buffer) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(SYSLOG_DATEFORMAT,Locale.ENGLISH);		
		String datePrefix = dateFormat.format(dt);	
		
		int pos = buffer.length() + 4;		
		buffer.append(datePrefix);
	
		//  RFC 3164 requires leading space for days 1-9
		if (buffer.charAt(pos) == '0') {
			buffer.setCharAt(pos,' ');
		}
	}

	public static void appendMSEventTimestamp(Date dt, StringBuilder buffer) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MSEVENT_DATEFORMAT,Locale.ENGLISH);		
		String datePrefix = dateFormat.format(dt);	
		
		int pos = buffer.length() + 4;		
		buffer.append(datePrefix);
	
		//  RFC 3164 requires leading space for days 1-9
		if (buffer.charAt(pos) == '0') {
			buffer.setCharAt(pos,' ');
		}
	}
	
	@Override
	public void send(SyslogIF syslog, int level, Message msg) {
		StringBuilder out = new StringBuilder();
		
		// add syslog header
		appendHeader(msg, out);

		out.append("MSWinEventLog").append(SEPARATOR);
		appendCriticality(msg, out);
		appendField(msg, out, "Channel");
		appendField(msg, out, "RecordNumber"); // we do not have snare counter
		
		// If available, add timestamp from original windows event in the snare header
		Date dt = null;
		Object ts = msg.getTimestamp();
		if (ts != null && ts instanceof DateTime) {
			dt = new Date(((DateTime)ts).getMillis());
		}
		
		if (dt == null) {
			dt = new Date();
		}
		appendMSEventTimestamp(dt, out);
		out.append(SEPARATOR);

		appendField(msg, out, "EventID");

		appendField(msg, out, "SourceName");
		appendWinUser(msg, out);
		appendField(msg, out, "AccountType");

		appendField(msg, out, "EventType");

		appendField(msg, out, "source");
		appendField(msg, out, "Category");

		// manca il data
		out.append(SEPARATOR);

		// ExtendedData
		appendField(msg, out, "message");

		Object fld = msg.getField("RecordNumber");
		if (fld == null){
		fld = new String("N/A");
		}
		out.append(fld.toString());

	  //out.append(msg.getMessage());
		String str = out.toString();
		// log.info("Sending plain message: " + level + ", " + str);
		syslog.log(level, str);
	}

	public static void appendHeader(Message msg, StringBuilder out) {
		
		// set priority
		appendPriority(msg, out);

		// if available, set date as original windows event
		Date dt = null;
		Object ts = msg.getTimestamp();
		if (ts != null && ts instanceof DateTime) {
			dt = new Date(((DateTime)ts).getMillis());
		}
		
		// else, create a new date
		if (dt == null) {
			dt = new Date();
		}
		appendSyslogTimestamp(dt, out);
		out.append(" ");

		// set source
		Object fld = msg.getSource();
		if (fld == null){
			fld = new String("N/A");
		}
		out.append(fld.toString());
		out.append(" ");
	}

  public static void appendField(Message msg, StringBuilder out, String field){
    Object fld = msg.getField(field.toString());
    if (fld == null){
      fld = new String("N/A");
    }
    String f = fld.toString().replaceAll("\t", " ");
    out.append(f).append(SEPARATOR);
  }

  public static void appendWinUser(Message msg, StringBuilder out){
    Object domain = msg.getField("Domain");
    if(domain != null){
      out.append(domain.toString()).append("\\");
    }
    appendField(msg, out, "AccountName");
  }

  public static void appendCriticality(Message msg, StringBuilder out){
    Object severityValue = msg.getField("SeverityValue");
    String criticality = "0";
    if(severityValue!=null){
      int i_severityValue = Integer.parseInt(severityValue.toString());
      criticality = String.valueOf(i_severityValue-1);
    }
    out.append(criticality.toString()).append(SEPARATOR);
  }

  public static void appendPriority(Message msg, StringBuilder out){
    out.append("<").append("14").append(">");
  }
}
