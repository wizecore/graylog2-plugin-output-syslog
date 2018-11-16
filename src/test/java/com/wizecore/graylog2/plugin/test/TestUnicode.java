package com.wizecore.graylog2.plugin.test;

import com.wizecore.graylog2.plugin.FullSender;
import org.graylog2.plugin.Message;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class TestUnicode extends SimpleSocketTcpServer {

    @Test
    public void testMessageUTF8() throws InterruptedException {
        FullSender s = new FullSender();

        SyslogConfigIF config = new TCPNetSyslogConfig();
        config.setHost(hostname);
        config.setPort(port);
        config.setTruncateMessage(true);
        config.setMaxMessageLength(1024 * 16);
        Message msg = new Message("32832832", "localhost", new DateTime());
        System.out.println("Original message: ");
        System.out.println(msg);
        SyslogIF syslog = Syslog.createInstance("tcp_" + System.currentTimeMillis(), config);
        s.send(syslog, Syslog.LEVEL_INFO, msg);
        Thread.sleep(1000);
    }

    @Test
    public void testStringBom() throws UnsupportedEncodingException {
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        String boms = new String(bom, "UTF-8");
        System.out.println("BOM: {" + boms + "}, bom length: {" + boms.length() + "}, bom bytes: {" + boms.getBytes("UTF-8").length + "}");
    }
}
