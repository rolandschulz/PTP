package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.command.IProxyCommand;

public interface IProxyDebugCommand extends IProxyCommand {

	/*
	 * Command IDs
	 */
	public static final int STARTSESSION = 1;
	public static final int SETLINEBREAKPOINT = 2;
	public static final int SETFUNCBREAKPOINT = 3;
	public static final int DELETEBREAKPOINT = 4;
	public static final int ENABLEBREAKPOINT = 5;
	public static final int DISABLEBREAKPOINT = 6;
	public static final int CONDITIONBREAKPOINT = 7;
	public static final int BREAKPOINTAFTER = 8;
	public static final int SETWATCHPOINT = 9;
	public static final int GO = 10;
	public static final int STEP = 11;
	public static final int TERMINATE = 12;
	public static final int INTERRUPT = 13;
	public static final int LISTSTACKFRAMES = 14;
	public static final int SETCURRENTSTACKFRAME = 15;
	public static final int EVALUATEEXPRESSION = 16;
	public static final int GETTYPE = 17;
	public static final int LISTLOCALVARIABLES = 18;
	public static final int LISTARGUMENTS = 19;
	public static final int LISTGLOBALVARIABLES = 20;
	public static final int LISTINFOTHREADS = 21;
	public static final int SETTHREADSELECT = 22;
	public static final int STACKINFODEPTH = 23;
	public static final int DATAREADMEMORY = 24;
	public static final int DATAWRITEMEMORY = 25;
	public static final int LISTSIGNALS = 26;
	public static final int SIGNALINFO = 27;
	public static final int CLIHANDLE = 28;
	public static final int DATAEVALUATEEXPRESSION = 29;
	public static final int GETPARTIALAIF = 30;
	public static final int VARIABLEDELETE = 31;
}