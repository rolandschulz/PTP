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

package org.eclipse.ptp.rtsystem.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;

public class SimulationMonitoringSystem implements IMonitoringSystem {
	protected List listeners = new ArrayList(2);
	protected HashMap nodeMap;

	protected HashMap nodeUserMap;

	protected HashMap nodeGroupMap;

	protected HashMap nodeModeMap;

	protected HashMap nodeStateMap;

	protected int numMachines = 1;
	protected int[] numNodes;
	
	public SimulationMonitoringSystem() {
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		int prefNumMachines = preferences.getInt(PreferenceConstants.SIMULATION_NUM_MACHINES);
		System.out.println("User selected "+prefNumMachines+" simulated machines.");
		if(prefNumMachines < 1) {
			preferences.setValue(PreferenceConstants.SIMULATION_NUM_MACHINES, 1);
			preferences.setValue(PreferenceConstants.SIMULATION_MACHINE_NODE_PREFIX + "0", 256);
			prefNumMachines = 1;

			PTPCorePlugin.getDefault().savePluginPreferences();

			System.err.println("No existing / invalid number of machines to to simulate detected.  Default " + "number of machines set to 1.  Set using the PTP preferences -> simulation page.");
			numNodes = new int[1];
			numNodes[0] = 256;
		}
		else {
			numNodes = new int[prefNumMachines];
			for(int i=0; i<prefNumMachines; i++) {
				int nn = preferences.getInt(PreferenceConstants.SIMULATION_MACHINE_NODE_PREFIX + ""+(i)+"");
				System.out.println("SimPreferences: Machine "+(i)+" = "+nn+" nodes");
				numNodes[i] = nn;
			}
		}
		numMachines = prefNumMachines;
	}
	
	public void startup() {
		nodeMap = new HashMap();
		for (int i = 0; i < numMachines; i++) {
			String s = new String("machine" + (i));
			nodeMap.put(s, new Integer(numNodes[i]));
		}
		
		int totnodes = 0;
		for (int i = 0; i < numMachines; i++) {
			totnodes += numNodes[i];
		}
		nodeUserMap = new HashMap();
		nodeGroupMap = new HashMap();
		nodeModeMap = new HashMap();
		nodeStateMap = new HashMap();

		/* machine 3 has a bunch of nodes w/ different states */
		if(numMachines >= 4) {
			for (int i = 0; i < numNodes[3]; i++) {
				String s = new String("machine3_node" + i);
				int r = ((int) (Math.random() * 100));
				if (r < 10)
					nodeStateMap.put(s, new String("error"));
				else if (r < 30)
					nodeStateMap.put(s, new String("down"));
				else {
					nodeStateMap.put(s, new String("up"));
				}
			}
			int nstart = ((int) (Math.random() * 50));
			int nlen = ((int) (Math.random() * 40 + 10));
			String nmode = new String(((int) (Math.random() * 2)) == 0 ? "73"
					: "64");
			int gstart = ((int) (Math.random() * 50 + (nstart + nlen)));
			int glen = ((int) (Math.random() * 100 + 40));
			String gmode = new String(((int) (Math.random() * 2)) == 0 ? "73"
					: "64");
			int cstart = ((int) (Math.random() * 50 + (nstart + nlen + gstart + glen)));
			int clen = ((int) (Math.random() * 75 + 25));
			String cmode = new String(((int) (Math.random() * 2)) == 0 ? "73"
					: "64");
			for (int i = 0; i < numNodes[3]; i++) {
				String s = new String("machine3_node" + i);
				if (i >= nstart && i <= (nstart + nlen)) {
					nodeUserMap.put(s, new String(System
							.getProperty("user.name")));
					nodeGroupMap.put(s, new String("ptp"));
					nodeModeMap.put(s, nmode);
				} else if (i >= gstart && i <= (gstart + glen)) {
					nodeUserMap.put(s, new String("gwatson"));
					nodeGroupMap.put(s, new String("ptp"));
					nodeModeMap.put(s, gmode);
				} else if (i >= cstart && i <= (cstart + clen)) {
					nodeUserMap.put(s, new String("crasmussen"));
					nodeGroupMap.put(s, new String("ptp"));
					nodeModeMap.put(s, cmode);
				} else {
					nodeUserMap.put(s, new String(""));
					nodeGroupMap.put(s, new String(""));
					nodeModeMap.put(s, new String("73"));
				}
			}
		}

		if(numMachines >= 1) {
			for (int i = 0; i < numNodes[0]; i++) {
				String s = new String("machine0_node" + i);
				nodeStateMap.put(s, new String("up"));
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, new String("64"));
			}
		}

		if (numMachines >= 2) {
			for (int i = 0; i < numNodes[1]; i++) {
				String s = new String("machine1_node" + i);
				nodeStateMap.put(s, new String("up"));
				if (i < 32) {
					nodeUserMap.put(s, new String(System
							.getProperty("user.name")));
					nodeGroupMap.put(s, new String("ptp"));
					nodeModeMap.put(s, new String("64"));
				} else if (i < 64) {
					nodeUserMap.put(s, new String(""));
					nodeGroupMap.put(s, new String(""));
					nodeModeMap.put(s, new String("73"));
				} else if (i < 128) {
					nodeUserMap.put(s, new String("wjones"));
					nodeGroupMap.put(s, new String("parl"));
					nodeModeMap.put(s, new String("64"));
				} else {
					nodeUserMap.put(s, new String("jsmith"));
					nodeGroupMap.put(s, new String("awhere"));
					nodeModeMap.put(s, new String("73"));
				}
			}
		}

		if (numMachines >= 3) {
			/*
			 * setup machine[2]'s hardware - a machine w/ a bunch of nodes w/
			 * different state
			 */
			for (int i = 0; i < numNodes[2]; i++) {
				String s = new String("machine2_node" + i);
				int r = ((int) (Math.random() * 100));
				if (r < 3) {
					nodeStateMap.put(s, new String("error"));
				} else if (r < 6)
					nodeStateMap.put(s, new String("down"));
				else {
					nodeStateMap.put(s, new String("up"));
					nodeUserMap.put(s, new String(System
							.getProperty("user.name")));
					nodeGroupMap.put(s, new String("ptp"));
					nodeModeMap.put(s, new String("64"));
				}
			}
		}
		
		for(int i=4; i< numMachines; i++) {
			for(int j=0; j<numNodes[i]; j++) {
				String s = new String("machine"+(i)+"_node"+j);
				nodeStateMap.put(s, new String("up"));
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, new String("64"));
			}
		}
	}
	public void shutdown() {
		listeners.clear();
		listeners = null;
	}
	
	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}
	
	public String[] getMachines() {
		int i = 0;
		Set set = nodeMap.keySet();
		Object[] arset = set.toArray();
		Arrays.sort(arset);
		
		String[] ne = new String[arset.length];
		//Iterator it = set.iterator();

		for(i=0; i<arset.length; i++) {
			ne[i] = new String((String)arset[i]);
		}
		
		/*
		while (it.hasNext()) {
			String key = (String) it.next();
			ne[i++] = new String(key);
		}
		*/

		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public String[] getNodes(IPMachine machine) {
		String machineName = machine.getElementName();
		/* find this machineName in the map - if it's there */
		if (!nodeMap.containsKey(machineName))
			return null;

		int n = ((Integer) (nodeMap.get(machineName))).intValue();

		String[] ne = new String[n];

		for (int i = 0; i < ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new String(machineName + "_node" + i);
		}

		return ne;
	}

	public String getNodeMachineName(String nodeName) {
		int idx;
		idx = nodeName.indexOf("_");
		if (idx >= 0) {
			return nodeName.substring(0, idx);
		}
		return "";
	}

	public String[] getNodeAttributes(IPNode node, String[] attribs) {
		String nodeName = node.getElementName();
		String[] retstr = new String[attribs.length];
		
		for(int i=0; i<attribs.length; i++) {
			String attrib = attribs[i];
			String s = null;
			
			//System.out.println("attrib = "+attrib);
			
			if (attrib.equals(AttributeConstants.ATTRIB_NODE_NAME)) {
				s = nodeName;
			} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_STATE)) {
				s = (String) nodeStateMap.get(nodeName);
			} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_MODE)) {
				s = (String) nodeModeMap.get(nodeName);
			} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_USER)) {
				s = (String) nodeUserMap.get(nodeName);
			} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_GROUP)) {
				s = (String) nodeGroupMap.get(nodeName);
			}
			
			retstr[i] = new String(s);
			//System.out.println("ret["+i+"] = '"+retstr[i]+"'");
		}
		
		return retstr;
	}
	
	public String[] getAllNodesAttributes(IPMachine machine, String[] attribs) {
		IPNode[] nodes = machine.getSortedNodes();
		
		String[] allvals = new String[attribs.length * nodes.length];
		
		for(int i=0; i<nodes.length; i++) {
			String[] nvals = getNodeAttributes(nodes[i], attribs);
			
			for(int j=0; j<nvals.length; j++) {
				allvals[(i * nvals.length) + j] = new String(nvals[j]);
			}
		}

		
		return allvals;
	}
}
