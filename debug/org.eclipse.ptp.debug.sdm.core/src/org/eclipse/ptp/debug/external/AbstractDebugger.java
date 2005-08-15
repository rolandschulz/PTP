/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.model.MProcessSet;
import org.eclipse.ptp.debug.external.utils.Queue;

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

	protected ArrayList userDefinedProcessSetList = null;
	protected MProcessSet allSet = null;
	protected MProcessSet currentFocus = null;
	
	protected boolean isExitingFlag = false; /* Checked by the eventThread */

	protected abstract void startDebugger(IPJob job);
	
	public void initialize(IPJob job) {
		actionpointList = new ArrayList();
		eventQueue = new Queue();
		eventThread = new EventThread(this);
		eventThread.start();

		userDefinedProcessSetList = new ArrayList();
		allSet = new MProcessSet("all");
		currentFocus = allSet;

		// Initialize state variables
		startDebugger(job);
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
	
	public final MProcessSet defSet(String name, int[] procs) {
		int size = userDefinedProcessSetList.size();
		
		if (name.equals("all"))
			return allSet;
		
		/* to avoid duplicates */
		for (int i = 0; i < size; i++) {
			MProcessSet set = (MProcessSet) userDefinedProcessSetList.get(i);
			if (set.getName().equals(name)) {
				return set;
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
		return procSet;
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