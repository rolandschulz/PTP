package org.eclipse.ptp.rtsystem.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;

public class SimulationMonitoringSystem implements IMonitoringSystem {
	protected List listeners = new ArrayList(2);
	protected HashMap nodeMap;

	protected HashMap nodeUserMap;

	protected HashMap nodeGroupMap;

	protected HashMap nodeModeMap;

	protected HashMap nodeStateMap;

	/* define the number of machines here */
	final static int numMachines = 4;


	/*
	 * define how many nodes each machine has - the array length must equal
	 * numMachines
	 */
	final static int[] numNodes = { 256, 256, 256, 512 };

	
	public SimulationMonitoringSystem() {
		
	}
	
	public void startup() {
		nodeMap = new HashMap();
		for (int i = 0; i < numMachines; i++) {
			String s = new String("machine" + i);
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
		String nmode = new String(((int) (Math.random() * 2)) == 0 ? "0111"
				: "0100");
		int gstart = ((int) (Math.random() * 50 + (nstart + nlen)));
		int glen = ((int) (Math.random() * 100 + 40));
		String gmode = new String(((int) (Math.random() * 2)) == 0 ? "0111"
				: "0100");
		int cstart = ((int) (Math.random() * 50 + (nstart + nlen + gstart + glen)));
		int clen = ((int) (Math.random() * 75 + 25));
		String cmode = new String(((int) (Math.random() * 2)) == 0 ? "0111"
				: "0100");
		for (int i = 0; i < numNodes[3]; i++) {
			String s = new String("machine3_node" + i);
			if (i >= nstart && i <= (nstart + nlen)) {
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
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
				nodeModeMap.put(s, new String("0111"));
			}

		}

		for (int i = 0; i < numNodes[0]; i++) {
			String s = new String("machine0_node" + i);
			nodeStateMap.put(s, new String("up"));
			nodeUserMap.put(s, new String(System.getProperty("user.name")));
			nodeGroupMap.put(s, new String("ptp"));
			nodeModeMap.put(s, new String("0100"));
		}

		for (int i = 0; i < numNodes[1]; i++) {
			String s = new String("machine1_node" + i);
			nodeStateMap.put(s, new String("up"));
			if (i < 32) {
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, new String("0100"));
			} else if (i < 64) {
				nodeUserMap.put(s, new String(""));
				nodeGroupMap.put(s, new String(""));
				nodeModeMap.put(s, new String("0111"));
			} else if (i < 128) {
				nodeUserMap.put(s, new String("wjones"));
				nodeGroupMap.put(s, new String("parl"));
				nodeModeMap.put(s, new String("0100"));
			} else {
				nodeUserMap.put(s, new String("jsmith"));
				nodeGroupMap.put(s, new String("awhere"));
				nodeModeMap.put(s, new String("0111"));
			}
		}

		/*
		 * setup machine[2]'s hardware - a machine w/ a bunch of nodes w/
		 * different state
		 */
		for (int i = 0; i < numNodes[3]; i++) {
			String s = new String("machine2_node" + i);
			int r = ((int) (Math.random() * 100));
			if (r < 3) {
				nodeStateMap.put(s, new String("error"));
			} else if (r < 6)
				nodeStateMap.put(s, new String("down"));
			else {
				nodeStateMap.put(s, new String("up"));
				nodeUserMap.put(s, new String(System.getProperty("user.name")));
				nodeGroupMap.put(s, new String("ptp"));
				nodeModeMap.put(s, new String("0100"));
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
		String[] ne = new String[set.size()];
		Iterator it = set.iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			ne[i++] = new String(key);
		}

		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public String[] getNodes(String machineName) {
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

	public String getNodeAttribute(String nodeName, String attrib) {
		String s = null;
		if (attrib.equals("state")) {
			s = (String) nodeStateMap.get(nodeName);
		} else if (attrib.equals("mode")) {
			s = (String) nodeModeMap.get(nodeName);
		} else if (attrib.equals("user")) {
			s = (String) nodeUserMap.get(nodeName);
		} else if (attrib.equals("group")) {
			s = (String) nodeGroupMap.get(nodeName);
		}
		return s;
	}
}
