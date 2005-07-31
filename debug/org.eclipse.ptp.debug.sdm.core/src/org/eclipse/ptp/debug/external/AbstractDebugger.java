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
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.model.MProcessSet;
import org.eclipse.ptp.debug.external.utils.Queue;
import org.eclipse.ptp.debug.external.variable.DebugVariable;
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
	protected Queue eventQueue = null;
	protected EventThread eventThread = null;
	protected HashMap stateVariables = null;

	protected ArrayList userDefinedProcessSetList = null;
	protected MProcessSet allSet = null;
	protected MProcessSet currentFocus = null;
	
	protected DebugSession debugSession = null;
	
	protected boolean isExitingFlag = false; /* Checked by the eventThread */

	protected abstract void startDebugger();
	
	public void initialize(DebugSession dS) {
		actionpointList = new ArrayList();
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();
		stateVariables = new HashMap();

		userDefinedProcessSetList = new ArrayList();
		allSet = new MProcessSet("all");
		currentFocus = allSet;

		debugSession = dS;
		
		// Initialize state variables
		stateVariables.put("MODE", new VMode());
		stateVariables.put("START_MODEL", new VStartModel());
		stateVariables.put("STOP_MODEL", new VStopModel());
		startDebugger();
	}
	
	protected abstract void stopDebugger();
	
	public final void exit() {
		isExitingFlag = true;
		stopDebugger();
		
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
	}
	
	public final void unsetAll() {
		Iterator keys = stateVariables.keySet().iterator();
		while (keys.hasNext()) {
			Object currentKey = keys.next();
			DebugVariable dVar = (DebugVariable) stateVariables.get(currentKey);
			dVar.setValue(dVar.getDefaultValue());
		}
	}

	public final DebugVariable[] set() {
		ArrayList dVars = new ArrayList();
		Iterator keys = stateVariables.keySet().iterator();
		while (keys.hasNext()) {
			Object currentKey = keys.next();
			DebugVariable dVar = (DebugVariable) stateVariables.get(currentKey);
			dVars.add(dVar);
		}
		return (DebugVariable[]) dVars.toArray();
	}
	
	public final void unset(String varName) {
		DebugVariable dVar = (DebugVariable) stateVariables.get(varName);
		dVar.setValue(dVar.getDefaultValue());
	}
	
	public final DebugVariable set(String varName) {
		return (DebugVariable) stateVariables.get(varName);
	}
	
	public final void set(String varName, String varValue) {
		DebugVariable dVar = (DebugVariable) stateVariables.get(varName);
		dVar.setValue(varValue);
	}
	
	public final void breakpointSet(String set, String loc) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		breakpoint(loc);
		currentFocus = savedFocus;
	}
	
	public final void breakpointSet(String set, String loc, int count) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		breakpoint(loc, count);
		currentFocus = savedFocus;
	}
	
	public final void breakpointSet(String set, String loc, String cond) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		breakpoint(loc, cond);
		currentFocus = savedFocus;
	}
	
	public final void watchpointSet(String set, String var) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		watchpoint(var);
		currentFocus = savedFocus;
	}

	public final DebugActionpoint[] actions() {
		ArrayList daList = new ArrayList();
		int size = actionpointList.size();
		for (int i = 0; i < size; i++) {
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(i);
			if (!da.isDeleted())
				daList.add(da);
		}
		return (DebugActionpoint[]) daList.toArray();
	}

	public final DebugActionpoint[] actions(int[] ids) {
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
		return (DebugActionpoint[]) daList.toArray();
	}
	
	public final DebugActionpoint[] actions(String type) {
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
		return (DebugActionpoint[]) daList.toArray();
	}

	public final void goSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		go();
		currentFocus = savedFocus;
	}

	public final void haltSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		halt();
		currentFocus = savedFocus;
	}

	public final void stepFinishSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		stepFinish();
		currentFocus = savedFocus;
	}

	public final void stepSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		step();
		currentFocus = savedFocus;
	}

	public final void stepSet(String set, int count) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		step(count);
		currentFocus = savedFocus;
	}

	public final void stepOverSet(String set) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		stepOver();
		currentFocus = savedFocus;
	}

	public final void stepOverSet(String set, int count) {
		MProcessSet savedFocus = currentFocus;
		focus(set);
		stepOver(count);
		currentFocus = savedFocus;
	}
	
	public final void defSet(String name, int[] procs) {
		int size = userDefinedProcessSetList.size();
		
		/* to avoid duplicates */
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			if (set.getName().equals(name)) {
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
	}
	
	public final void undefSet(String name) {
		int size = userDefinedProcessSetList.size();
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			if (set.getName().equals(name)) {
				set.clear();
				userDefinedProcessSetList.remove(set);
				break;
			}
		}
	}
	
	public final void undefSetAll() {
		int size = userDefinedProcessSetList.size();
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			set.clear();
			userDefinedProcessSetList.remove(set);
		}
	}
	
	public final MProcess[] viewSet(String name) {
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
			
		return retValue;
	}
	
	public final void focus(String name) {
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
			
	}

	public final void addDebuggerObserver(Observer obs) {
		this.addObserver(obs);
	}

	public final void deleteDebuggerObserver(Observer obs) {
		this.deleteObserver(obs);
	}
	
	public final void fireEvents(DebugEvent[] events) {
		if (events != null && events.length > 0) {
			for (int i = 0; i < events.length; i++) {
				fireEvent(events[i]);
			}
		}
	}

	public final void fireEvent(DebugEvent event) {
		if (event != null) {
			eventQueue.addItem(event);
		}
	}
	
	public final void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}

	public final Queue getEventQueue() {
		return eventQueue;
	}
	
	public final boolean isExiting() {
		return isExitingFlag;
	}

}