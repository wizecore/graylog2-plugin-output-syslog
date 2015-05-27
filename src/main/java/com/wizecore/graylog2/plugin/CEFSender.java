package com.wizecore.graylog2.plugin;

import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.SyslogIF;

/**
 * Using CEF format
 */

/*
 * http://blog.rootshell.be/2011/05/11/ossec-speaks-arcsight/
 * 
 * 
 * CEF:Version|Device Vendor|Device Product|Device Version|Signature ID|\
Name|Severity|Extension

CEF:0|ArcSight|Logger|5.0.0.5355.2|sensor:115|Logger Internal Event|1|\
cat=/Monitor/Sensor/Fan5 cs2=Current Value cnt=1 dvc=10.0.0.1 cs3=Ok \
cs1=null type=0 cs1Label=unit rt=1305034099211 cs3Label=Status cn1Label=value \
cs2Label=timeframe
 */
public class CEFSender implements MessageSender {

	@Override
	public void send(SyslogIF syslog, int level, Message msg) {
		throw new UnsupportedOperationException("CEF is not yet complete!");
	}
}
