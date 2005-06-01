/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.command;

/**
 * @author donny
 *
 */
public class DebugCommand {
	String operation = "";
	String[] args = null;
	
	public DebugCommand(String oper) {
		operation = oper;
	}
	
	public DebugCommand(String oper, String[] arguments) {
		operation = oper;
		args = arguments;
	}
	
	public String[] getArgs() {
		return args;
	}
	
	public void setArgs(String[] strs) {
		args = strs;
	}
	
	public void execute() {
	}
}
