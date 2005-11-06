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
package org.eclipse.ptp.debug.external.target;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.cdi.Session;

/**
 * @author Clement chu
 *
 */
public abstract class AbstractTargetEvent implements ITargetEvent {
	protected Session session = null;
	protected BitList targets = null;
	protected Object result = null;
	protected int type;
	private final int TIME_OUT = 20000;
	
	public AbstractTargetEvent(Session session, BitList targets, int type) {
		this.session = session;
		this.targets = targets;
		this.type = type;
	}
	public void exec() {
		new Thread(new Runnable() {
			public void run() {
				try {
					session.getDebugger().addTargetEvent(AbstractTargetEvent.this);
					action();
				} catch (PCDIException e) {
					synchronized(AbstractTargetEvent.this) {
						AbstractTargetEvent.this.notifyAll();
					}
				}
			}
		}).start();
		synchronized (this) {
			try {
				wait(TIME_OUT);
			} catch (InterruptedException e) {}
		}
	}
	public Session getSession() {
		return session;
	}
	public BitList getTargets() {
		return targets;
	}
	public boolean contain(int task) {
		return targets.get(task);
	}
	public boolean contain(BitList tasks) {
		return targets.intersects(tasks);
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		synchronized (this) {
			this.result = result;
			notifyAll();
		}
	}
	public int getType() {
		return type;
	}
	public abstract void action() throws PCDIException;
}
