package com.wizecore.graylog2.plugin;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus.Capability;
import org.graylog2.plugin.Version;

public class SyslogOutputMetaData implements PluginMetaData {

	@Override
	public String getAuthor() {
		return "Wizecore. Based on work by Intelie.";
	}

	@Override
	public String getDescription() {
		return "Enables sending messages to syslog via TCP, UDP and TCP over SSL.";
	}

	@Override
	public String getName() {
		return "SyslogOutputPlugin";
	}

	@Override
	public Set<Capability> getRequiredCapabilities() {
		return Collections.emptySet();
	}

	@Override
	public Version getRequiredVersion() {
		return Version.from(6, 0, 0);
	}

	@Override
	public URI getURL() {
		return URI.create("https://github.com/wizecore/graylog2-output-syslog");
	}

	@Override
	public String getUniqueId() {
		return SyslogOutput.class.getName();
	}

	@Override
	public Version getVersion() {
		return Version.from(6, 0, 4);
	}
}
