package org.eclipse.ptp.rtsystem.ompi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;

public class OMPIMonitoringSystem implements IMonitoringSystem {

	protected List listeners = new ArrayList(2);

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

	public String[] getJobs() {
		System.out.println("JAVA OMPI: getJobs() called");

		String[] ne = new String[1];
		ne[0] = new String("job0");
		return ne;
	}

	/* get the processes pertaining to a certain job */
	public String[] getProcesses(String jobName) {
		System.out.println("JAVA OMPI: getProcesses(" + jobName + ") called");

		/* need to check is jobName is a valid job name */

		String[] ne = new String[1];
		ne[0] = new String("job0_process0");
		return ne;
	}

	public String getProcessNodeName(String procName) {
		System.out.println("JAVA OMPI: getProcessNodeName(" + procName
				+ ") called");

		/* check if procName is a valid process name */

		return "machine0_node0";
	}

	public String getProcessStatus(String procName) {
		System.out.println("JAVA OMPI: getProcessStatus(" + procName
				+ ") called");

		/* check is procName is a valid process name */

		return "-1";
	}

	public String getProcessExitCode(String procName) {
		System.out.println("JAVA OMPI: getProcessExitCode(" + procName
				+ ") called");

		/* check if procName is a valid process name */

		return "-1";
	}

	public String getProcessSignal(String procName) {
		System.out.println("JAVA OMPI: getProcessSignal(" + procName
				+ ") called");

		/* check is procName is a valid process name */

		return "-1";
	}
	
	public int getProcessPID(String procName) {
		return ((int)(Math.random() * 10000)) + 1000;
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

		if (attrib.equals("state")) {
			s = "down";
		} else if (attrib.equals("mode")) {
			s = "0100";
		} else if (attrib.equals("user")) {
			s = "root";
		} else if (attrib.equals("group")) {
			s = "root";
		}
		return s;
	}
}
