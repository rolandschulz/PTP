/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ptp.remotetools.environment.control.ITargetControlEventListener;
import org.eclipse.ptp.remotetools.environment.control.ITargetControlEventProvider;


/**
 * Default implementation of the ITargetControlEventProvider interface, based
 * on a simple pooling algorithm.
 * 
 * @author Ricardo M. Matinata
 * @since 1.2
 *
 */
public class PoolingTargetControlEventProvider  implements
		ITargetControlEventProvider {

	public Thread monitorThread = null;
	private int monitorInterval = 4000; // milliseconds
	private Map controls = new HashMap();
	
	class MonitorThread extends Thread {
		
		public MonitorThread() {
			super("Target Control polling");
		}
		public void run() {
			while (monitorThread == this)
			{
				synchronized (PoolingTargetControlEventProvider.this) {
					Iterator ctrls = controls.keySet().iterator();
					
					while(ctrls.hasNext()) {
		
							ITargetControl control = (ITargetControl) ctrls.next();
							int entryStatus = control.query();
							TargetInfo info = (TargetInfo) controls.get(control);
							
							if (entryStatus != info.getStatus() ) {
								info.setStatus(entryStatus);
								controls.put(control,info);
								fireEvent(entryStatus, control);
								/*
								if (entryStatus == ICellStatus.STARTED) {
									model.notifyStarted(element);
								} else if (entryStatus == ICellStatus.STOPPED) {
									model.notifyStopped(element);
								} else if (entryStatus == ICellStatus.RESUMED) {
									model.notifyResumed(element);
								} else if (entryStatus == ICellStatus.PAUSED) {
									model.notifyPaused(element);
								}
								*/
							}
						
					}
				}
				
				try {
					sleep (monitorInterval);
				} catch (InterruptedException e) {}
			}
		}
		
	}
	
	public void fireEvent(int status, ITargetControl control) {
		
		if (control != null && controls.containsKey (control))
		{
			TargetInfo info = (TargetInfo) controls.get(control);
			ITargetControlEventListener listener = info.getListener();
			listener.handleStateChangeEvent(status, control);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetControlEventProviderw#registerControlAndListener()
	 */
	public void registerControlAndListener(ITargetControl control, ITargetControlEventListener listener) {
		
		if (control != null && !controls.containsKey (control))
		{
			synchronized(this) {
				controls.put (control, new TargetInfo(listener,ITargetStatus.STOPPED));
			}
			
			if (monitorThread == null) {
				(monitorThread = new MonitorThread()).start();
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetControlEventProviderw#unregisterControlAndListener()
	 */
	public void unregisterControlAndListener(ITargetControl control, ITargetControlEventListener listener) {
		
		if (control != null && controls.containsKey (control))
		{
			synchronized(this) {
				controls.remove (control);
			}
			
			if (controls.size() == 0)
				monitorThread = null;
		}
		
	}
	
	class TargetInfo {
		
		private int status;
		private ITargetControlEventListener listener;
		
		public TargetInfo(ITargetControlEventListener listener, int status) {
			this.listener = listener;
			this.status = status;
		}

		public ITargetControlEventListener getListener() {
			return listener;
		}

		public void setListener(ITargetControlEventListener listener) {
			this.listener = listener;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
		
	}
		

}
