package com.wizecore.graylog2.plugin;


import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.graylog2.syslog4j.impl.net.udp.UDPNetSyslogConfig;

import com.google.inject.assistedinject.Assisted;

/**
 * Implementation of plugin to Graylog 1.0 to send stream via Syslog
 * 
 * @author Huksley <huksley@sdot.ru>
 */
public class SyslogOutput implements MessageOutput {
    
	public final static int PORT_MIN = 9000;
	public final static int PORT_MAX = 9099;
	
	private Logger log = Logger.getLogger(SyslogOutput.class.getName());
    private String host;
    private int port;
    private String protocol;
    private SyslogIF syslog;
    private String format;
    private MessageSender sender;
    
	public static MessageSender createSender(String fmt) {
		try {
    		if (fmt == null || fmt.equalsIgnoreCase("plain")) {
    			return new PlainSender();
    		} else 
    		if (fmt == null || fmt.equalsIgnoreCase("structured")) {
    			return new StructuredSender();
    		} else 
    		if (fmt == null || fmt.equalsIgnoreCase("full")) {
    			return new FullSender();
    		} else 
    		if (fmt == null || fmt.equalsIgnoreCase("cef")) {
    			return new CEFSender();
    		} else 
    		if (fmt == null || fmt.toLowerCase().startsWith("custom:")) {
    			String clazz = fmt.substring(fmt.indexOf(":") + 1);
    			return (MessageSender) Class.forName(clazz).newInstance();
    		} else {
    			throw new IllegalArgumentException("Unknown format: " + fmt);
    		}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to accept format: " + fmt, e);
		}
	}
    
    @Inject 
    public SyslogOutput(@Assisted Stream stream, @Assisted Configuration conf) {
    	host = conf.getString("host");
    	port = Integer.parseInt(conf.getString("port"));
    	protocol = conf.getString("protocol");
    	format = conf.getString("format");
    	if (format == null || format.equals("")) {
    		format = "plain";
    	}
    	
    	log.info("Creating syslog output " + protocol + "://" + host + ":" + port + ", format " + format);
    	SyslogConfigIF config = null;
    	if (protocol.toLowerCase().equals("udp")) {
    		config = new UDPNetSyslogConfig();
    	} else
    	if (protocol.toLowerCase().equals("tcp")) {
    		config = new TCPNetSyslogConfig();
    	}
    	config.setHost(host);
    	config.setPort(port);
    	config.setMaxMessageLength(4096);
    	config.setTruncateMessage(true);
    	
    	String hash = protocol + "_" + host + "_" + port + "_" + format;
    	syslog = Syslog.createInstance(hash, config);
		
		sender = createSender(format);
		if (sender instanceof StructuredSender) {
			// Always send via structured data
			syslog.getConfig().setUseStructuredData(true);
		} else
		if (sender instanceof PlainSender || sender instanceof CEFSender) {
			// Will write this fields manually
	    	syslog.getConfig().setSendLocalName(false);
			syslog.getConfig().setSendLocalTimestamp(false);
		}
    }
    
    @Override
    public boolean isRunning() {
    	return syslog != null;
    }
    
    @Override
    public void stop() {
        if (syslog != null) {
            syslog = null;
        }
    }
    
    @Override
    public void write(List<Message> msgs) throws Exception {
    	for (Message msg: msgs) {
    		write(msg);
    	}
    }
    
    @Override
    public void write(Message msg) throws Exception {
    	int level = -1;
    	
    	if (level < 0) {
        	Object mlev = msg.getField("level");
        	if (mlev != null && mlev instanceof Number) {
        		level = ((Number) mlev).intValue();
        	}
    	}
    	
    	if (level < 0) {
    		Object mlev = msg.getField("_level");
        	if (mlev != null && mlev instanceof Number) {
        		level = ((Number) mlev).intValue();
        	}
    	}
    	
    	if (level < 0) {
    		Object mlev = msg.getField("original_level");
        	if (mlev != null && mlev instanceof String) {
        		if (mlev.toString().equalsIgnoreCase("INFO")) {
        			level = SyslogIF.LEVEL_INFO;
        		} else
        		if (mlev.toString().equalsIgnoreCase("SEVERE")) {
        			level = SyslogIF.LEVEL_ERROR;
        		} else
    			if (mlev.toString().equalsIgnoreCase("WARNING")) {
    				level = SyslogIF.LEVEL_WARN;
        		};
        	}
    	}

    	if (level < 0) {
    		level = SyslogIF.LEVEL_INFO; 
    	}
    	
    	if (sender != null) {
    		sender.send(syslog, level, msg);
    	} else {
    		syslog.log(level, msg.getMessage());
    	}
    }
            
	public interface Factory extends MessageOutput.Factory<SyslogOutput> {
		@Override
		SyslogOutput create(Stream stream, Configuration configuration);

		@Override
		Config getConfig();

		@Override
		Descriptor getDescriptor();
	}
    
    public static class Descriptor extends MessageOutput.Descriptor { 
    	public Descriptor() { 
    		super("Syslog Output", false, "", "Forwards stream to Syslog."); 
    	} 
    }

	public static class Config extends MessageOutput.Config {
		@Override
		public ConfigurationRequest getRequestedConfiguration() {
			final ConfigurationRequest configurationRequest = new ConfigurationRequest();
			configurationRequest.addField(new TextField("protocol", "Protocol to use", "udp", "Choose protocol. Enter either udp or tcp.", ConfigurationField.Optional.NOT_OPTIONAL));
			configurationRequest.addField(new TextField("host", "Syslog host", "localhost", "Host to send syslog messages to.", ConfigurationField.Optional.NOT_OPTIONAL));
			configurationRequest.addField(new TextField("port", "Syslog port", "514", "Syslog port. Default is 514.", ConfigurationField.Optional.NOT_OPTIONAL));
			configurationRequest.addField(new TextField("format", "Message format", "plain", "Message format: plain,structured,cef,full.", ConfigurationField.Optional.NOT_OPTIONAL));
			return configurationRequest;
		}
	}

}
