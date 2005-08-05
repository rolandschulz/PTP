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
package org.eclipse.ptp.debug.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.ui.MachineManager;

/**
 * @author clement chu
 *
 */
public class DebugManager {
	private static final int total = 5000;
	private static DebugManager instance = null;
	private List listeners = new ArrayList();
	private Map processMap = new HashMap();
	
	public DebugManager() {
		Runnable startSim = new Runnable() {
			public void run() {
				try {
					Thread.sleep(getRandom(5000 + 1));
					startSimulation();
				} catch (Exception e) {
				}
			}
		};
		new Thread(startSim).start();
	}
	
	public void startSimulation() {
		createProcess();
		fireListener(IPProcess.RUNNING);
		Runnable sim = new Runnable() {
			private int counter = 0;
			public void run() {
				while (counter <= DebugManager.total) {
					try {
						Thread.sleep(getRandom(1000) + 1);
					} catch (Exception e) {
						System.out.println("Sleep err: " + e.getMessage());
					}
					int randomPnum = getRandom(processMap.size());
					PProcess p = getProcess(String.valueOf(randomPnum));
					if (p != null && !p.getStatus().equals(IPProcess.EXITED)) {
						String status = getRandomStatus();
						if (status.equals(IPProcess.EXITED))
							counter++;
						p.setStatus(status);
						fireListener(status);
					}
				}
			}
		};
		new Thread(sim).start();
	}

	public void fireListener(String status) {
		for (Iterator i=listeners.iterator(); i.hasNext();) {
			IDebugParallelModelListener listener = (IDebugParallelModelListener)i.next();
			if (status.equals(IPProcess.RUNNING))
				listener.run();
			else if (status.equals(IPProcess.STARTING))
				listener.start();
			if (status.equals(IPProcess.STOPPED))
				listener.stop();
			else if (status.equals(IPProcess.EXITED_SIGNALLED))
				listener.suspend();
			else if (status.equals(IPProcess.EXITED))
				listener.exit();
			else 
				listener.error();
		}
	}
	
	public void addListener(IDebugParallelModelListener listener) {
		listeners.add(listener);
	}
	public void removeListener(IDebugParallelModelListener listener) {
		listeners.remove(listener);
	}
	
	public void createProcess() {
		for (int i=0; i<total; i++) {
			PProcess p = new PProcess(String.valueOf(i));
			p.setStatus(IPProcess.STARTING);
			processMap.put(p.getID(), p);
		}
	}
	
	public PProcess getProcess(String id) {
		return (PProcess)processMap.get(id);
	}
	
	public PProcess[] getProcesses() {
		return (PProcess[])processMap.values().toArray(new PProcess[processMap.size()]);
	}
	
	public int getRandom(int max) {
		return (int)Math.round(Math.random() * max);
	}
	
	private String getRandomStatus() {
		int random = getRandom(5);
		//NOTE: NO RUNNING
		switch(random) {
			case MachineManager.PROC_EXITED:
				return IPProcess.EXITED;
			case MachineManager.PROC_EXITED_SIGNAL:
				return IPProcess.EXITED_SIGNALLED;
			//case MachineManager.PROC_RUNNING:
				//return IPProcess.RUNNING;
			case MachineManager.PROC_STOPPED:
				return IPProcess.STOPPED;
			case MachineManager.PROC_STARTING:
				return IPProcess.STARTING;
			default:
				return IPProcess.ERROR;
		}
	}
	
	public static DebugManager getInstance() {
		if (instance == null)
			instance = new DebugManager();
		return instance;
	}	
}
