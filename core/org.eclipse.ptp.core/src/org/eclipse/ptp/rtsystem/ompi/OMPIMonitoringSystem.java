package org.eclipse.ptp.rtsystem.ompi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;

public class OMPIMonitoringSystem implements IMonitoringSystem {

	protected List listeners = new ArrayList(2);
	
	private OMPIJNIBroker jniBroker = null;

	public OMPIMonitoringSystem(OMPIJNIBroker jniBroker) {
		this.jniBroker = jniBroker;
	}

	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}

	public void startup() {
		System.out.println("OMPIMonitoringSystem startup()");
	}

	public void shutdown() {
		System.out.println("OMPIMonitoringSystem shutdown()");
		listeners.clear();
		listeners = null;
	}
	
	public String[] getMachines() {
		System.out.println("JAVA OMPI: getMachines() called");

		String[] ne = new String[1];
		ne[0] = new String("machine0");

		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public String[] getNodes(String machineName) {
		System.out.println("JAVA OMPI: getNodes(" + machineName + ") called");

		/* need to check if machineName is a valid machine name */

		/* default to just returning 10 nodes on this machine */
		int n = 10;
		String[] ne = new String[n];

		for (int i = 0; i < ne.length; i++) {
			/* prepend this node name with the machine name */
			ne[i] = new String(machineName + "_node" + i);
		}

		return ne;
	}

	public String getNodeMachineName(String nodeName) {
		System.out.println("JAVA OMPI: getNodeMachineName(" + nodeName
				+ ") called");

		/* check nodeName . . . */

		return "machine0";
	}

	public String getNodeAttribute(String nodeName, String attrib) {
		System.out.println("JAVA OMPI: getNodeAttribute(" + nodeName + ", "
				+ attrib + ") called");
		String s = null;

		if (attrib.equals(AttributeConstants.ATTRIB_NODE_STATE)) {
			s = "down";
		} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_MODE)) {
			s = "0100";
		} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_USER)) {
			s = "root";
		} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_GROUP)) {
			s = "root";
		}
		return s;
	}
}
