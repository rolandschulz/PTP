package org.eclipse.ptp.proxy.util;

import java.util.BitSet;


public class DebugOptions {
	public final static int NUMBER_OF_OPTIONS = 3;
	
	public final static int CLIENT_MESSAGES = 0;
	public final static int SERVER_MESSAGES = 1;
	public final static int PROTOCOL_TRACING = 2;
	
	private BitSet options = new BitSet(NUMBER_OF_OPTIONS);

	public DebugOptions() {
	}

	/**
	 * @return true if the option is set
	 */
	public boolean getOption(int option) {
		return options.get(option);
	}

	/**
	 * @param option the option to set
	 */
	public void setOption(int option) {
		options.set(option);
	}
	
	/**
	 * @param option the option to unset
	 */
	public void unsetOption(int option) {
		options.clear(option);
	}
}
