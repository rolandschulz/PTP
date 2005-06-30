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

/**
 * @author clement
 *
 */
public class DebugManager {
	private static DebugManager instance = null;
	protected GroupManager groupManager = null; 
	
	public DebugManager() {
		groupManager = new GroupManager();
		PProcessGroup group = new PProcessGroup(0);
		for (int i=0; i<350; i++) {
			PProcess p = new PProcess(i);
			p.setStatus(randomStatus());
			group.addProcess(p);
		}
		groupManager.addGroup(group);
		group = new PProcessGroup(1);
		for (int i=0; i<66; i++) {
			PProcess p = new PProcess(i);
			p.setStatus(randomStatus());
			group.addProcess(p);
		}
		groupManager.addGroup(group);
		group = new PProcessGroup(2);
		for (int i=0; i<15; i++) {
			PProcess p = new PProcess(i);
			p.setStatus(randomStatus());
			group.addProcess(p);
		}
		groupManager.addGroup(group);
	}
	
	private int randomStatus() {
		return (int)Math.round(Math.random() * 4);
	}
	
	public static DebugManager getInstance() {
		if (instance == null)
			instance = new DebugManager();
		return instance;
	}
	
	public GroupManager getGroupManager() {
		return groupManager;
	}
	
}
