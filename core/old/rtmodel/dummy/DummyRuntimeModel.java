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

package org.eclipse.ptp.rtmodel.dummy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ptp.rtmodel.IRuntimeModel;
import org.eclipse.ptp.rtmodel.NamedEntity;

public class DummyRuntimeModel implements IRuntimeModel {
	/* define the number of machines here */
	final static int numMachines = 4;
	/* define how many nodes each machine has - the array length must equal
	 * numMachines
	 */
	final static int[] numNodes = { 16, 32, 64, 128 };
	protected HashMap nodeMap;
	
	/* define the number of jobs here */
	final static int numJobs = 3;
	/* define how many processes are in each job - the array length must
	 * equal numJobs
	 */
	final static int[] numProcesses = { 10, 20, 30, 40 };
	protected HashMap processMap;
	
	public DummyRuntimeModel() {
		nodeMap = new HashMap();
		for(int i=0; i<numMachines; i++) {
			String s = new String("machine"+i);
			nodeMap.put(s, new Integer(numNodes[i]));
		}
		processMap = new HashMap();
		for(int i=0; i<numJobs; i++) {
			String s = new String("job"+i);
			processMap.put(s, new Integer(numProcesses[i]));
		}
	}
	
	public NamedEntity[] getMachines() {
		int i = 0;
		NamedEntity[] ne = new NamedEntity[numMachines];
		Set set = nodeMap.keySet();
		Iterator it = set.iterator();
		
		while(it.hasNext()) {
			String key = (String)it.next();
			ne[i++] = new NamedEntity(key);
		}
		
		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public NamedEntity[] getNodes(String machineName) {
		/* find this machineName in the map - if it's there */
		if(!nodeMap.containsKey(machineName)) return null;
		
		int n = ((Integer)(nodeMap.get(machineName))).intValue();
		
		NamedEntity[] ne = new NamedEntity[n];
		
		for(int i=0; i<ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new NamedEntity(machineName+"_node"+i);
		}
		
		return ne;
	}
	
	public NamedEntity[] getJobs() {
		int i = 0;
		NamedEntity[] ne = new NamedEntity[numMachines];
		Set set = processMap.keySet();
		Iterator it = set.iterator();
		
		while(it.hasNext()) {
			String key = (String)it.next();
			ne[i++] = new NamedEntity(key);
		}
		
		return ne;
	}
	
	/* get the processes pertaining to a certain job */
	public NamedEntity[] getProcesses(String jobName) {
		/* find this machineName in the map - if it's there */
		if(!processMap.containsKey(jobName)) return null;
		
		int n = ((Integer)(processMap.get(jobName))).intValue();
		
		NamedEntity[] ne = new NamedEntity[n];
		
		for(int i=0; i<ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new NamedEntity(jobName+"_process"+i);
		}
		
		return ne;
	}

}
