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
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.model;

import java.util.ArrayList;



/**
 * @author donny
 *
 */
public class MProcessSet {
	private ArrayList processList = null;
	private String groupName;
	
	public MProcessSet(String name) {
		groupName = name;
		processList = new ArrayList();
	}
	
	public void addProcess(MProcess proc) {
		if (!processList.contains(proc))
			processList.add(proc);
	}
	
	public void delProcess(MProcess proc) {
		if (processList.contains(proc))
			processList.remove(proc);
	}
	
	public int getSize() {
		return processList.size();
	}
	
	public String getName() {
		return groupName;
	}
	
	public MProcess getProcess(int index) {
		return (MProcess) processList.get(index);
	}
	
	public MProcess[] getProcessList() {
		return (MProcess[]) processList.toArray();
	}
	
	public void clear() {
		processList.clear();
	}
}
