/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;


import java.util.Observer;

import org.eclipse.ptp.debug.external.actionpoint.DebugActionpoint;
import org.eclipse.ptp.debug.external.command.DebugCommand;
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.utils.Queue;
import org.eclipse.ptp.debug.external.variable.DebugVariable;



/**
 * @author donny
 *
 */
public interface IDebugger {
	public void initDebugger();
	public void destroyDebugger();
	
	
	/* General Debugger Interface */
	public DebugCommand[] history();
	public DebugCommand[] history(int numCmds);
	public DebugVariable[] set();
	public DebugVariable set(String name);
	public void set(String name, String value);
	public void unset(String name);
	public void unsetAll();

	/* Process/Thread Sets */
	public void focus(String name);
	public void defSet(String name, int[] procs);
	public void undefSet(String name);
	public void undefSetAll();
	public MProcess[] viewSet(String name);
	
	/* Debugger Initialization/Termination */
	public void load(String prg);
	public void load(String prg, int numProcs);
	public void run(String[] args);
	public void run();
	public void detach();
	public void quit();
	public void exit();
	
	/* Program Information */
	
	/* Data Display and Manipulation */
	
	/* Execution Control */
	public void step();
	public void stepSet(String set);
	public void step(int count);
	public void stepSet(String set, int count);
	public void stepOver();
	public void stepOverSet(String set);
	public void stepOver(int count);
	public void stepOverSet(String set,int count);
	public void stepFinish();
	public void stepFinishSet(String set);
	public void halt();
	public void haltSet(String set);
	public void cont();
	public void contSet(String set);
	
	/* Actionpoints */
	public void breakpoint(String loc);
	public void breakpointSet(String set, String loc);
	public void breakpoint(String loc, int count);
	public void breakpointSet(String set, String loc, int count);
	public void breakpoint(String loc, String cond);
	public void breakpointSet(String set, String loc, String cond);
	public void watchpoint(String var);
	public void watchpointSet(String set, String var);
	public DebugActionpoint[] actions();
	public DebugActionpoint[] actions(int[] ids);
	public DebugActionpoint[] actions(String type);
	public void delete(int[] ids);
	public void delete(String type);
	public void disable(int[] ids);
	public void disable(String type);
	public void enable(int[] ids);
	public void enable(String type);
	
	/* The methods below will not be found in the HPDF Spec */
	
	/* Events */
	public void addDebuggerObserver(Observer obs);
	public void deleteDebuggerObserver(Observer obs);
	public void notifyObservers(Object arg);
	public Queue getEventQueue();
	public boolean isExiting();
	public void fireEvents(DebugEvent[] events);
	public void fireEvent(DebugEvent event);
	
	/* Remote Debugging */
	public void remote(String host, int port);
	
	/* Methods that are required to interface with Eclipse Debug/CDI Model */
	public abstract Process getSessionProcess();
	public abstract Process[] getProcesses();

}

