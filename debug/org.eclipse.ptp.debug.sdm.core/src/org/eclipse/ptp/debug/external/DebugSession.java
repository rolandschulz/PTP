/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;

import java.util.Observer;

import org.eclipse.ptp.debug.external.actionpoint.DebugActionpoint;
import org.eclipse.ptp.debug.external.command.DebugCommand;
import org.eclipse.ptp.debug.external.gdbserver.RemoteDebugManager;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.simulator.DebugSimulator;
import org.eclipse.ptp.debug.external.variable.DebugVariable;

/**
 * @author donny
 *
 */
public class DebugSession {
	IDebugger debugger;
	DebugConfig debugConfig;

	RemoteDebugManager remoteManager;
	
	public DebugSession() {
		debugConfig = new DebugConfig();
		
		/* Default for Donny */
		/*
		debugConfig.setMpirunPath("/home/donny/local/mpich/bin/mpirun");
		debugConfig.setMpirunMasterHost("chloe");
		debugConfig.setMpirunMasterPort("33333");
		debugConfig.setUserHome("/home/donny");
		debugConfig.setUserName("donny");
		debugConfig.setDebuggerPath(new String[] {"/usr/bin/gdb"});
		debugger = new GDBDebugger(debugConfig);
		*/
		
		debugger = new DebugSimulator(debugConfig);
		debugger.initDebugger(this);
	}
	
	public void quit() {
		debugger.quit();
	}
	
	public void exit() {
		debugger.exit();
	}
	
	public void load(String prg) {
		debugger.load(prg);
	}

	public void load(String prg, int numProcs) {
		debugger.load(prg, numProcs);
	}

	public void run() {
		debugger.run();
	}
	
	public void run(String[] args) {
		debugger.run(args);
	}

	public DebugCommand[] history() {
		return debugger.history();
	}

	public DebugCommand[] history(int numCmds) {
		return debugger.history(numCmds);
	}
	
	public DebugVariable set(String var) {
		return debugger.set(var);
	}

	public DebugVariable[] set() {
		return debugger.set();
	}

	public void set(String name, String val) {
		debugger.set(name, val);
	}

	public void unset(String var) {
		debugger.unset(var);
	}

	public void unsetAll() {
		debugger.unsetAll();
	}

	public void detach() {
		debugger.detach();
	}
	
	public void breakpoint(String loc) {
		debugger.breakpoint(loc);
	}
	
	public void breakpointSet(String set, String loc) {
		debugger.breakpointSet(set, loc);
	}
	
	public void breakpoint(String loc, int count) {
		debugger.breakpoint(loc, count);
	}

	public void breakpointSet(String set, String loc, int count) {
		debugger.breakpointSet(set, loc, count);
	}

	public void breakpoint(String loc, String cond) {
		debugger.breakpoint(loc, cond);
	}

	public void breakpointSet(String set, String loc, String cond) {
		debugger.breakpointSet(set, loc, cond);
	}
	
	public void watchpoint(String var) {
		debugger.watchpoint(var);
	}
	
	public void watchpointSet(String set, String var) {
		debugger.watchpointSet(set, var);
	}
	
	public DebugActionpoint[] actions() {
		return debugger.actions();
	}
	
	public DebugActionpoint[] actions(int[] ids) {
		return debugger.actions(ids);
	}

	public DebugActionpoint[] actions(String type) {
		return debugger.actions(type);
	}
	
	public void delete(int[] ids) {
		debugger.delete(ids);
	}

	public void delete(String type) {
		debugger.delete(type);
	}
	
	public void disable(int[] ids) {
		debugger.disable(ids);
	}

	public void disable(String type) {
		debugger.disable(type);
	}
	
	public void enable(int[] ids) {
		debugger.enable(ids);
	}

	public void enable(String type) {
		debugger.enable(type);
	}
	
	public void cont() {
		debugger.cont();
	}

	public void contSet(String set) {
		debugger.contSet(set);
	}
	
	public void step() {
		debugger.step();
	}

	public void stepSet(String set) {
		debugger.stepSet(set);
	}

	public void step(int count) {
		debugger.step(count);
	}

	public void stepSet(String set, int count) {
		debugger.stepSet(set, count);
	}

	public void stepOver() {
		debugger.stepOver();
	}

	public void stepOverSet(String set) {
		debugger.stepOverSet(set);
	}

	public void stepOver(int count) {
		debugger.stepOver(count);
	}
	
	public void stepOverSet(String set, int count) {
		debugger.stepOverSet(set, count);
	}
	
	public void stepFinish() {
		debugger.stepFinish();
	}

	public void stepFinishSet(String set) {
		debugger.stepFinishSet(set);
	}

	public void halt() {
		debugger.halt();
	}

	public void haltSet(String set) {
		debugger.haltSet(set);
	}

	public void defSet(String name, int[] procs) {
		debugger.defSet(name, procs);
	}
	
	public void undefSet(String name) {
		debugger.undefSet(name);
	}
	
	public void undefSetAll() {
		debugger.undefSetAll();
	}
	
	public MProcess[] viewSet(String name) {
		return debugger.viewSet(name);
	}
	
	public void focus(String name) {
		debugger.focus(name);
	}
	
	/* Remote Debugging Methods */
	
	public void remote(String host, int port) {
		debugger.remote(host, port);
	}
	
	public void remoteManagerServe() {
		remoteManager = new RemoteDebugManager();
		remoteManager.serve();
	}
	
	public void remoteManagerDestroy() {
		remoteManager.destroy();
	}

	public boolean remoteManagerIsInferiorConnected() {
		return remoteManager.isInferiorConnected();
	}
	
	public boolean remoteManagerIsSuperiorConnected() {
		return remoteManager.isSuperiorConnected();
	}

	/* Miscellaneous Methods */
	
	public DebugConfig getDebugConfig() {
		return debugConfig;
	}
	
	public IDebugger getDebugger() {
		return debugger;
	}
	
	public void addDebuggerObserver(Observer obs) {
		debugger.addDebuggerObserver(obs);
	}

	public void deleteDebuggerObserver(Observer obs) {
		debugger.deleteDebuggerObserver(obs);
	}
}
