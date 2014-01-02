package com.wizecore.graylog2.plugin;

import java.util.Collections;
import java.util.Set;

import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutput.Factory;

import com.google.inject.multibindings.MapBinder;

public class SyslogOutputModule extends PluginModule {

	@Override
	public Set<? extends PluginConfigBean> getConfigBeans() {
		return Collections.emptySet();
	}

	@Override
	protected void configure() {
		MapBinder<String, Factory<? extends MessageOutput>> outputMapBinder = outputsMapBinder();
		installOutput(outputMapBinder, SyslogOutput.class, SyslogOutput.Factory.class);
	}
}
