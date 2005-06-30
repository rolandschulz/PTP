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

package org.eclipse.ptp.rtsystem.ompi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.rtsystem.NamedEntity;
import org.eclipse.ptp.rtsystem.RuntimeEvent;

public class OMPIControlSystem implements IControlSystem {
	protected List listeners = new ArrayList(2);

	public OMPIControlSystem() {
		System.out.println("JAVA OMPI: constructor called");
	}
	
	public native String OMPIGetError();
	public native int OMPIInit(String orted_bin_path);
	public native void OMPIFinalize();
	public native void OMPIProgress();
	public native void OMPIRun();
	
	static {
        System.loadLibrary("ptp_ompi_jni");
    }
	
	public void startup() {
		int rc = OMPIInit("foo/bar");
		System.out.println("OMPI Init() return code = "+rc);
		if(rc != 0) {
			String error_msg = OMPIGetError();
			CoreUtils.showErrorDialog("OMPI Runtime Initialization Error", error_msg, null);
			return;
		}
		startProgressMaker();
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
}
