package com.wizecore.graylog2.plugin;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.streams.Stream;

import com.google.inject.assistedinject.Assisted;

public class SyslogOutput implements MessageOutput {
    
	public final static int PORT_MIN = 9000;
	public final static int PORT_MAX = 9099;
	
	private Logger log = Logger.getLogger(SyslogOutput.class.getName());
    private DatagramSocket socket = null;
    private String host = "localhost";
    private int port = 1514;
    private InetAddress destination;
    
    @Inject 
    public SyslogOutput(@Assisted Stream stream, @Assisted Configuration conf) {
    	host = conf.getString("host");
    	port = conf.getInt("port");
    }
    
    @Override
    public boolean isRunning() {
    	return true;
    }
    
    protected DatagramSocket initiateSocket() throws UnknownHostException, SocketException {
        int port = PORT_MIN;
        DatagramSocket resultingSocket = null;
        boolean binded = false;
        while (!binded) {
            try {
                resultingSocket = new DatagramSocket(port);
                binded = true;
            } catch (SocketException e) {
                port++;
                if (port > PORT_MAX) {
                    throw e;
                }
            }
        }
        
        return resultingSocket;
    }
    
    @Override
    public void stop() {
        if (socket != null && socket.isConnected()) {
            socket.close();
			socket = null;
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
    	writeSocket(msg);
    }
    
    /**
     * Randomly choose destination
     * 
     * @throws UnknownHostException
     */
	protected void findDestination() throws UnknownHostException {
		List<InetAddress> all = new ArrayList<InetAddress>();
    	if (host.indexOf(",") > 0) {
    		String[] l = host.split("\\,");
    		for (String h: l) {
    			all.addAll(Arrays.asList(InetAddress.getAllByName(h.trim())));
    		}
    	} else {
    		all.addAll(Arrays.asList(InetAddress.getAllByName(host.trim())));
    	}
    	if (all.size() == 1) {
    		destination = all.get(0);
    	} else {
    		// Choose one random
    		destination = all.get(new Random(System.currentTimeMillis()).nextInt(all.size()));
    	}
	}
    
    protected void writeSocket(Message msg) throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = initiateSocket();
        }
        
        if (destination == null) {
        	findDestination();
        }
        
        log.info("Writing " + msg + " to " + host + ":" + port);
        byte[] bytes = msg.getMessage().getBytes();        
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, destination, port);
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
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
    		super("Syslog Output", false, "", "Forwards stream to Syslog UDP."); 
    	} 
    }

	public static class Config extends MessageOutput.Config {
		@Override
		public ConfigurationRequest getRequestedConfiguration() {
			final ConfigurationRequest configurationRequest = new ConfigurationRequest();
			configurationRequest.addField(new TextField("host", "Syslog host", "localhost", "Host to send syslog messages to.", ConfigurationField.Optional.NOT_OPTIONAL));
			configurationRequest.addField(new TextField("port", "Syslog port", "514", "UDP port. Default is 514.", ConfigurationField.Optional.NOT_OPTIONAL));
			configurationRequest.addField(new TextField("fields", "Message fields", "timestamp,message", "A comma separated list of field values in messages that should be transmitted.", ConfigurationField.Optional.NOT_OPTIONAL));
			return configurationRequest;
		}
	}

}