/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.ptp.debug.external.actionpoint.ABreakpoint;
import org.eclipse.ptp.debug.external.actionpoint.AWatchpoint;
import org.eclipse.ptp.debug.external.actionpoint.DebugActionpoint;
import org.eclipse.ptp.debug.external.command.CActions;
import org.eclipse.ptp.debug.external.command.CBreak;
import org.eclipse.ptp.debug.external.command.CCont;
import org.eclipse.ptp.debug.external.command.CDefSet;
import org.eclipse.ptp.debug.external.command.CDelete;
import org.eclipse.ptp.debug.external.command.CDetach;
import org.eclipse.ptp.debug.external.command.CDisable;
import org.eclipse.ptp.debug.external.command.CEnable;
import org.eclipse.ptp.debug.external.command.CExit;
import org.eclipse.ptp.debug.external.command.CFocus;
import org.eclipse.ptp.debug.external.command.CHalt;
import org.eclipse.ptp.debug.external.command.CHistory;
import org.eclipse.ptp.debug.external.command.CLoad;
import org.eclipse.ptp.debug.external.command.CQuit;
import org.eclipse.ptp.debug.external.command.CRemote;
import org.eclipse.ptp.debug.external.command.CRun;
import org.eclipse.ptp.debug.external.command.CSet;
import org.eclipse.ptp.debug.external.command.CStep;
import org.eclipse.ptp.debug.external.command.CStepFinish;
import org.eclipse.ptp.debug.external.command.CStepOver;
import org.eclipse.ptp.debug.external.command.CUndefSet;
import org.eclipse.ptp.debug.external.command.CUndefSetAll;
import org.eclipse.ptp.debug.external.command.CUnset;
import org.eclipse.ptp.debug.external.command.CUnsetAll;
import org.eclipse.ptp.debug.external.command.CViewSet;
import org.eclipse.ptp.debug.external.command.CWatch;
import org.eclipse.ptp.debug.external.command.DebugCommand;
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.model.MProcessSet;
import org.eclipse.ptp.debug.external.utils.Queue;
import org.eclipse.ptp.debug.external.variable.DebugVariable;
import org.eclipse.ptp.debug.external.variable.VMaxHistory;
import org.eclipse.ptp.debug.external.variable.VMode;
import org.eclipse.ptp.debug.external.variable.VStartModel;
import org.eclipse.ptp.debug.external.variable.VStopModel;




/**
 * @author donny
 *
 */
public abstract class AbstractDebugger extends Observable implements IDebugger {
	/* We use an array to store the actionpoints, the
	 * index of the array (plus 1) corresponds to the id of the actionpoint
	 * Note: actionpoint id starts at 1, array index starts at 0
	 */
	protected ArrayList actionpointList = null;
	protected ArrayList commandHistory = null;
	protected Queue eventQueue = null;
	protected EventThread eventThread = null;
	protected HashMap stateVariables = null;
	
	protected ArrayList userDefinedProcessSetList = null;
	
	protected MProcessSet allSet = null;
	protected MProcessSet currentFocus = null;
	
	protected String debuggedProgram = null;
	
	protected DebugConfig debugConfig = null;
	
	public boolean isExitingFlag = false; /* Checked by the eventThread */
	
	public AbstractDebugger(DebugConfig dConf) {
		debugConfig = dConf;
	}
	
	public void initDebugger() {
		commandHistory = new ArrayList();
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();
		stateVariables = new HashMap();
		userDefinedProcessSetList = new ArrayList();
		actionpointList = new ArrayList();
	
		allSet = new MProcessSet("all");
		currentFocus = allSet;
		
		// Initialize state variables
		stateVariables.put("MAX_HISTORY", new VMaxHistory());
		stateVariables.put("MODE", new VMode());
		stateVariables.put("START_MODEL", new VStartModel());
		stateVariables.put("STOP_MODEL", new VStopModel());
	}
	
	public void addDebuggerObserver(Observer obs) {
		this.addObserver(obs);
	}
	
	public abstract void destroyDebugger();
	
	public boolean isExiting() {
		return isExitingFlag;
	}
	
	public void exit() {
		isExitingFlag = true;
		destroyDebugger();
		// Allow (10 secs) for the EventThread  to finish processing the queue.
		for (int i = 0; !eventQueue.isEmpty() && i < 5; i++) {
			try {
				java.lang.Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		// Kill the event Thread.
		try {
			if (eventThread.isAlive()) {
				eventThread.interrupt();
				eventThread.join(); // Should use a timeout ?
			}
		} catch (InterruptedException e) {
		}		
		commandHistory.add(new CExit());
	}
	
	public void quit() {
		isExitingFlag = true;
		destroyDebugger();
		// Allow (10 secs) for the EventThread  to finish processing the queue.
		for (int i = 0; !eventQueue.isEmpty() && i < 5; i++) {
			try {
				java.lang.Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		// Kill the event Thread.
		try {
			if (eventThread.isAlive()) {
				eventThread.interrupt();
				eventThread.join(); // Should use a timeout ?
			}
		} catch (InterruptedException e) {
		}		
		commandHistory.add(new CQuit());
	}

	/* Serial program */
	public abstract void load(String prg);

	/* Parallel program */
	public void load(String prg, int numProcs) {
		debuggedProgram = prg;
		if (numProcs == 1)
			commandHistory.add(new CLoad(prg));
		else
			commandHistory.add(new CLoad(prg, numProcs));
	}

	public void run(String[] args) {
		if (args == null)
			commandHistory.add(new CRun());
		else
			commandHistory.add(new CRun(args));
	}
	
	public abstract void run();
		
	public DebugCommand[] history() {
		DebugCommand[] retValue;
		int hSize = commandHistory.size();
		String maxHistory = ((VMaxHistory) stateVariables.get("MAX_HISTORY")).getValue();
		int maxHistoryInt = Integer.parseInt(maxHistory);
		
		if (hSize > maxHistoryInt)
			retValue = (DebugCommand[]) commandHistory.subList(hSize - maxHistoryInt, hSize).toArray();
		else
			retValue = (DebugCommand[]) commandHistory.toArray();
		
		commandHistory.add(new CHistory());
		return retValue;
	}
	
	public DebugCommand[] history(int numCmds) {
		DebugCommand[] retValue;
		int hSize = commandHistory.size();
		String maxHistory = ((VMaxHistory) stateVariables.get("MAX_HISTORY")).getValue();
		int maxHistoryInt = Integer.parseInt(maxHistory);
		
		if (numCmds > maxHistoryInt)
			numCmds = maxHistoryInt;
		
		if (hSize > numCmds)
			retValue = (DebugCommand[]) commandHistory.subList(hSize - numCmds, hSize).toArray();
		else
			retValue = (DebugCommand[]) commandHistory.toArray();
		
		commandHistory.add(new CHistory(numCmds));
		return retValue;
	}

	public void unsetAll() {
		Iterator keys = stateVariables.keySet().iterator();
		while (keys.hasNext()) {
			Object currentKey = keys.next();
			DebugVariable dVar = (DebugVariable) stateVariables.get(currentKey);
			dVar.setValue(dVar.getDefaultValue());
		}
		commandHistory.add(new CUnsetAll());
	}

	public DebugVariable[] set() {
		ArrayList dVars = new ArrayList();
		Iterator keys = stateVariables.keySet().iterator();
		while (keys.hasNext()) {
			Object currentKey = keys.next();
			DebugVariable dVar = (DebugVariable) stateVariables.get(currentKey);
			dVars.add(dVar);
		}
		commandHistory.add(new CSet());
		return (DebugVariable[]) dVars.toArray();
	}
	
	public void unset(String varName) {
		DebugVariable dVar = (DebugVariable) stateVariables.get(varName);
		dVar.setValue(dVar.getDefaultValue());
		commandHistory.add(new CUnset(varName));
	}
	
	public DebugVariable set(String varName) {
		commandHistory.add(new CSet(varName));
		return (DebugVariable) stateVariables.get(varName);
	}
	
	public void set(String varName, String varValue) {
		DebugVariable dVar = (DebugVariable) stateVariables.get(varName);
		dVar.setValue(varValue);
		commandHistory.add(new CSet(varName, varValue));
	}
	
	public void detach() {
		commandHistory.add(new CDetach());
	}
	
	public void breakpoint(String loc) {
		commandHistory.add(new CBreak(loc));
	}
	
	public void breakpointSet(String set, String loc) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		breakpoint(loc);
		currentFocus = savedFocus;
	}
	
	public void breakpoint(String loc, int count) {
		commandHistory.add(new CBreak(loc, count));
	}
	
	public void breakpointSet(String set, String loc, int count) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		breakpoint(loc, count);
		currentFocus = savedFocus;
	}
	
	public void breakpoint(String loc, String cond) {
		commandHistory.add(new CBreak(loc, cond));
	}
	
	public void breakpointSet(String set, String loc, String cond) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		breakpoint(loc, cond);
		currentFocus = savedFocus;
	}
	
	public void watchpoint(String var) {
		commandHistory.add(new CWatch(var));
	}

	public void watchpointSet(String set, String var) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		watchpoint(var);
		currentFocus = savedFocus;
	}

	public DebugActionpoint[] actions() {
		ArrayList daList = new ArrayList();
		int size = actionpointList.size();
		for (int i = 0; i < size; i++) {
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(i);
			if (!da.isDeleted())
				daList.add(da);
		}
		commandHistory.add(new CActions());
		return (DebugActionpoint[]) daList.toArray();
	}

	public DebugActionpoint[] actions(int[] ids) {
		/* Internally the array for actionpoints starts at 0 but
		 * actionpoint id starts at 1
		 */
		ArrayList daList = new ArrayList();
		int size = actionpointList.size();
		int argSize = ids.length;
		
		for (int i = 0; i < argSize; i++) {
			int id = ids[i] - 1; // See the note above
			if (id >= size)
				continue;
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(id);
			if (!da.isDeleted())
				daList.add(da);
		}
		commandHistory.add(new CActions(ids));
		return (DebugActionpoint[]) daList.toArray();
	}
	
	public DebugActionpoint[] actions(String type) {
		ArrayList daList = new ArrayList();
		int size = actionpointList.size();
		for (int i = 0; i < size; i++) {
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(i);
			if (type.equals("DISABLED") && da.isDisabled())
				daList.add(da);
			else if (type.equals("ENABLED") && da.isEnabled())
				daList.add(da);
			else if (type.equals("WATCH") && (da instanceof AWatchpoint))
				if (!da.isDeleted())
					daList.add(da);
			else if (type.equals("BREAK") && (da instanceof ABreakpoint))
				if (!da.isDeleted())
					daList.add(da);
		}
		commandHistory.add(new CActions(type));
		return (DebugActionpoint[]) daList.toArray();
	}
	
	public void delete(int[] ids) {
		commandHistory.add(new CDelete(ids));
	}
	
	public void delete(String type) {
		commandHistory.add(new CDelete(type));
	}
	
	public void disable(int[] ids) {
		commandHistory.add(new CDisable(ids));
	}
	
	public void disable(String type) {
		commandHistory.add(new CDisable(type));
	}
	
	public void enable(int[] ids) {
		commandHistory.add(new CEnable(ids));
	}
	
	public void enable(String type) {
		commandHistory.add(new CEnable(type));
	}
	
	public void cont() {
		commandHistory.add(new CCont());
	}

	public void contSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		cont();
		currentFocus = savedFocus;
	}

	public void halt() {
		commandHistory.add(new CHalt());
	}

	public void haltSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		halt();
		currentFocus = savedFocus;
	}

	public void stepFinish() {
		commandHistory.add(new CStepFinish());
	}

	public void stepFinishSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		stepFinish();
		currentFocus = savedFocus;
	}

	public void step() {
		commandHistory.add(new CStep());
	}

	public void stepSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		step();
		currentFocus = savedFocus;
	}

	public void step(int count) {
		commandHistory.add(new CStep(count));
	}

	public void stepSet(String set, int count) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		step(count);
		currentFocus = savedFocus;
	}

	public void stepOver() {
		commandHistory.add(new CStepOver());
	}

	public void stepOverSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		stepOver();
		currentFocus = savedFocus;
	}

	public void stepOver(int count) {
		commandHistory.add(new CStepOver(count));
	}
	
	public void stepOverSet(String set, int count) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		stepOver(count);
		currentFocus = savedFocus;
	}
	
	public void remote(String host, int port) {
		commandHistory.add(new CRemote(host, port));
	}
	
	public void defSet(String name, int[] procs) {
		int size = userDefinedProcessSetList.size();
		
		/* to avoid duplicates */
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			if (set.getName().equals(name)) {
				commandHistory.add(new CDefSet(name, procs));
				return;
			}
		}
		
		MProcessSet procSet = new MProcessSet(name);
		size = allSet.getSize();
		int procsLength = procs.length;
		for (int i = 0; i < procsLength; i++) {
			if (procs[i] >= size)
				continue;
			procSet.addProcess(allSet.getProcess(procs[i]));
		}
		userDefinedProcessSetList.add(procSet);
		commandHistory.add(new CDefSet(name, procs));
	}
	
	public void undefSet(String name) {
		int size = userDefinedProcessSetList.size();
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			if (set.getName().equals(name)) {
				set.clear();
				userDefinedProcessSetList.remove(set);
				break;
			}
		}
		commandHistory.add(new CUndefSet(name));
	}
	
	public void undefSetAll() {
		int size = userDefinedProcessSetList.size();
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			set.clear();
			userDefinedProcessSetList.remove(set);
		}
		commandHistory.add(new CUndefSetAll());
	}
	
	public MProcess[] viewSet(String name) {
		MProcess[] retValue = null;
		if (name.equals("all"))
			retValue = allSet.getProcessList();
		else {
			int size = userDefinedProcessSetList.size();
			for (int i = 0; i < size; i++) {
				MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
				if (set.getName().equals(name)) {
					retValue = set.getProcessList();
					break;
				}
			}
		}
			
		commandHistory.add(new CViewSet(name));
		return retValue;
	}
	
	public void focus(String name) {
		if (name.equals("all"))
			currentFocus = allSet;
		else {
			int size = userDefinedProcessSetList.size();
			for (int i = 0; i < size; i++) {
				MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
				if (set.getName().equals(name)) {
					currentFocus = set;
					break;
				}
			}
		}
			
		commandHistory.add(new CFocus(name));
	}
	
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	public Queue getEventQueue() {
		return eventQueue;
	}
	
	public void fireEvents(DebugEvent[] events) {
		if (events != null && events.length > 0) {
			for (int i = 0; i < events.length; i++) {
				fireEvent(events[i]);
			}
		}
	}

	public void fireEvent(DebugEvent event) {
		if (event != null) {
			eventQueue.addItem(event);
		}
	}
}