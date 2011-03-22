package org.eclipse.ptp.rm.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;

/**
 * @since 2.0
 */
public class VolatileRemoteResourceManagerConfiguration implements IRemoteResourceManagerConfiguration {

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final String CM = ","; //$NON-NLS-1$

	private final String name;
	private List<String> options;
	private String localAddr;
	private int opts;
	private String proxyPath;
	private String id;
	private boolean useDefault;
	private String connectionName;

	public VolatileRemoteResourceManagerConfiguration() {
		name = EMPTY_STRING;
		options = null;
		localAddr = EMPTY_STRING;
		opts = 0;
		proxyPath = null;
		useDefault = false;
		connectionName = EMPTY_STRING;
	}

	public void addInvocationOptions(String optionString) {
		if (options == null) {
			options = new ArrayList<String>();
		}
		String[] split = optionString.split(CM);
		for (String s : split) {
			options.add(s);
		}
	}

	public String getConnectionName() {
		return connectionName;
	}

	public List<String> getInvocationOptions() {
		return options;
	}

	public String getInvocationOptionsStr() {
		StringBuffer b = new StringBuffer();
		int len = options.size();
		if (!options.isEmpty()) {
			b.append(options.get(0));
		}
		for (int i = 1; i < len; i++) {
			b.append(CM).append(options.get(i));
		}
		return b.toString();
	}

	public String getLocalAddress() {
		return localAddr;
	}

	public String getName() {
		return name;
	}

	public int getOptions() {
		return 0;
	}

	public String getProxyServerPath() {
		return proxyPath;
	}

	public String getRemoteServicesId() {
		return id;
	}

	public boolean getUseDefault() {
		return useDefault;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public void setInvocationOptions(String optionString) {
		if (options != null) {
			options.clear();
		}
		addInvocationOptions(optionString);
	}

	public void setLocalAddress(String localAddr) {
		this.localAddr = localAddr;
	}

	public void setOptions(int options) {
		opts = options;
	}

	public void setProxyServerPath(String proxyServerPath) {
		this.proxyPath = proxyServerPath;
	}

	public void setRemoteServicesId(String id) {
		this.id = id;
	}

	public void setUseDefault(boolean flag) {
		this.useDefault = flag;
	}

	public boolean testOption(int option) {
		return (opts | option) == option;
	}
}
