package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.command.IProxyCommand;

public interface IProxyDebugCommand extends IProxyCommand {

	/*
	 * Command IDs
	 */
	public static final int DEBUG_CMD_STARTSESSION = 1;
	public static final int DEBUG_CMD_SETLINEBREAKPOINT = 2;
	public static final int DEBUG_CMD_SETFUNCBREAKPOINT = 3;
	public static final int DEBUG_CMD_DELETEBREAKPOINT = 4;
	public static final int DEBUG_CMD_ENABLEBREAKPOINT = 5;
	public static final int DEBUG_CMD_DISABLEBREAKPOINT = 6;
	public static final int DEBUG_CMD_CONDITIONBREAKPOINT = 7;
	public static final int DEBUG_CMD_BREAKPOINTAFTER = 8;
	public static final int DEBUG_CMD_SETWATCHPOINT = 9;
	public static final int DEBUG_CMD_GO = 10;
	public static final int DEBUG_CMD_STEP = 11;
	public static final int DEBUG_CMD_TERMINATE = 12;
	public static final int DEBUG_CMD_INTERRUPT = 13;
	public static final int DEBUG_CMD_LISTSTACKFRAMES = 14;
	public static final int DEBUG_CMD_SETCURRENTSTACKFRAME = 15;
	public static final int DEBUG_CMD_EVALUATEEXPRESSION = 16;
	public static final int DEBUG_CMD_GETTYPE = 17;
	public static final int DEBUG_CMD_LISTLOCALVARIABLES = 18;
	public static final int DEBUG_CMD_LISTARGUMENTS = 19;
	public static final int DEBUG_CMD_LISTGLOBALVARIABLES = 20;
	public static final int DEBUG_CMD_LISTINFOTHREADS = 21;
	public static final int DEBUG_CMD_SETTHREADSELECT = 22;
	public static final int DEBUG_CMD_STACKINFODEPTH = 23;
	public static final int DEBUG_CMD_DATAREADMEMORY = 24;
	public static final int DEBUG_CMD_DATAWRITEMEMORY = 25;
	public static final int DEBUG_CMD_LISTSIGNALS = 26;
	public static final int DEBUG_CMD_SIGNALINFO = 27;
	public static final int DEBUG_CMD_CLIHANDLE = 28;
	public static final int DEBUG_CMD_DATAEVALUATEEXPRESSION = 29;
	public static final int DEBUG_CMD_GETPARTIALAIF = 30;
	public static final int DEBUG_CMD_VARIABLEDELETE = 31;
}