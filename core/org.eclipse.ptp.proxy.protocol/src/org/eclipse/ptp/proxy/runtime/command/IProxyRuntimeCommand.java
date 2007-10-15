package org.eclipse.ptp.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.IProxyCommand;


public interface IProxyRuntimeCommand extends IProxyCommand {
	/*
	 * Command IDs
	 */
	public static final int INIT = 1;
	public static final int MODEL_DEF = 2;
	public static final int START_EVENTS = 3;
	public static final int STOP_EVENTS = 4;
	public static final int SUBMIT_JOB = 5;
	public static final int TERMINATE_JOB = 6;
		
}