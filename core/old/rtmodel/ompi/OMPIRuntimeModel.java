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

package org.eclipse.ptp.rtmodel.ompi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.rtmodel.JobRunConfiguration;
import org.eclipse.ptp.rtmodel.IRuntimeListener;
import org.eclipse.ptp.rtmodel.IRuntimeModel;
import org.eclipse.ptp.rtmodel.NamedEntity;
import org.eclipse.ptp.rtmodel.RuntimeEvent;

public class OMPIRuntimeModel implements IRuntimeModel {
	protected List listeners = new ArrayList(2);

	public OMPIRuntimeModel() {
		System.out.println("JAVA OMPI: constructor called");
		OMPIInit();
		startProgressMaker();
	}
	
	public native void OMPIInit();
	public native void OMPIFinalize();
	public native void OMPIProgress();
	public native void OMPIRun();
	
	static {
        System.loadLibrary("ptp_ompi_jni");
    }
    
	public void startProgressMaker() {
		Thread progressThread = new Thread("PTP RTE OMPI Progress Thread") {
			public void run() {
				OMPIProgress();
			}
		};
		progressThread.start();
	}
	
	/* returns the new job name that it started - unique */
	public String run(JobRunConfiguration jobRunConfig) {
		System.out.println("JAVA OMPI: run() with args:\n"+jobRunConfig.toString());

		/*
		Thread runThread = new Thread("PTP RTE Run Thread") {
			public void run() {
				System.out.println("PTP RTE Run Thread - run()");
				 
				 */
				OMPIRun();
				/*
				System.out.println("************ DONE RUNNING");
			}
		};
		runThread.start();
		*/
		
		/* replace this job# here with a real job# coming out of the RTE */
		String s = new String("job0");
		return s;
	}

	public void abortJob(String jobID) {
		System.out.println("JAVA OMPI: abortJob() with args: " + jobID);
	}

	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}

	protected synchronized void fireEvent(String ID, RuntimeEvent event) {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IRuntimeListener listener = (IRuntimeListener) i.next();
			switch (event.getEventNumber()) {
			case RuntimeEvent.EVENT_NODE_STATUS_CHANGE:
				listener.runtimeNodeStatusChange(ID);
				break;
			case RuntimeEvent.EVENT_PROCESS_OUTPUT:
				listener.runtimeProcessOutput(ID, event.getText());
				break;
			case RuntimeEvent.EVENT_JOB_EXITED:
				listener.runtimeJobExited(ID);
				break;
			case RuntimeEvent.EVENT_JOB_STATE_CHANGED:
				listener.runtimeJobStateChanged(ID);
				break;
			case RuntimeEvent.EVENT_NEW_JOB:
				listener.runtimeNewJob(ID);
				break;
			}
		}
	}

	public void shutdown() {
		System.out.println("JAVA OMPI: shutdown() called");
		OMPIFinalize();
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
