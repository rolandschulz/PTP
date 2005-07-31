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

/**
 * @author clement chu
 *
 */
public class DebugManager {
	private static final int total = 300;
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
		fireListener(IDebugParallelModelListener.STATUS_RUNNING);
		Runnable sim = new Runnable() {
			private int counter = 0;
			public void run() {
				while (counter <= DebugManager.total) {
					try {
						Thread.sleep(getRandom(100) + 1);
					} catch (Exception e) {
						System.out.println("Sleep err: " + e.getMessage());
					}
					int randomPnum = getRandom(processMap.size());
					IPProcess p = getProcess(String.valueOf(randomPnum));
					if (p != null && !p.getStatus().equals(IDebugParallelModelListener.STATUS_EXITED)) {
						String status = getRandomStatus();
						if (status.equals(IDebugParallelModelListener.STATUS_EXITED))
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
			if (status.equals(IDebugParallelModelListener.STATUS_RUNNING))
				listener.run();
			else if (status.equals(IDebugParallelModelListener.STATUS_STARTING))
				listener.start();
			if (status.equals(IDebugParallelModelListener.STATUS_STOPPED))
				listener.stop();
			else if (status.equals(IDebugParallelModelListener.STATUS_SUSPENDED))
				listener.suspend();
			else if (status.equals(IDebugParallelModelListener.STATUS_EXITED))
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
			p.setStatus(IDebugParallelModelListener.STATUS_STARTING);
			processMap.put(p.getID(), p);
		}
	}
	
	public IPProcess getProcess(String id) {
		return (IPProcess)processMap.get(id);
	}
	
	public PProcess[] getProcesses() {
		return (PProcess[])processMap.values().toArray(new PProcess[processMap.size()]);
	}
	
	public int getRandom(int max) {
		return (int)Math.round(Math.random() * max);
	}
	
	private String getRandomStatus() {
		int random = getRandom(5);
		//NOTE: NO STARTING
		switch(random) {
			//case 1:
			//	return IDebugParallelModelListener.STATUS_RUNNING;
			case 3:
				return IDebugParallelModelListener.STATUS_STOPPED;
			case 4:
				return IDebugParallelModelListener.STATUS_EXITED;
			case 5:
				return IDebugParallelModelListener.STATUS_SUSPENDED;
			default:
				return IDebugParallelModelListener.STATUS_ERROR;
		}
	}
	
	public static DebugManager getInstance() {
		if (instance == null)
			instance = new DebugManager();
		return instance;
	}	
}
