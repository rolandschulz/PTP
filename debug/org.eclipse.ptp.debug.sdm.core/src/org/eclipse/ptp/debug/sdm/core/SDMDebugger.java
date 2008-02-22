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
package org.eclipse.ptp.debug.sdm.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.Session;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;
import org.eclipse.ptp.debug.sdm.core.pdi.PDIDebugger;
import org.eclipse.ptp.launch.PTPLaunchPlugin;

/**
 * @author clement
 *
 */
public class SDMDebugger implements IPDebugger {
	private IPDIDebugger pdiDebugger = null;
	private IPDIModelFactory modelFactory = null;
	private IPDIManagerFactory managerFactory = null;
	private IPDIEventFactory eventFactory = null;
	private IPDIRequestFactory requestFactory = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#createDebugSession(long, org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.core.runtime.IPath)
	 */
	public IPDISession createDebugSession(long timeout, IPLaunch launch, IPath corefile) throws CoreException {
		if (modelFactory == null) {
			modelFactory = new SDMModelFactory();
		}
		if (managerFactory == null) {
			managerFactory = new SDMManagerFactory();
		}
		if (eventFactory == null) {
			eventFactory = new SDMEventFactory();
		}
		if (requestFactory == null) {
			requestFactory = new SDMRequestFactory();
		}
		return createSession(timeout, launch, corefile);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#initialize(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void initialize(AttributeManager attrMgr) throws CoreException {
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());
		
		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}

		List<String> dbgArgs = dbgArgsAttr.getValue();

		try {
			getDebugger().initialize(dbgArgs);
		} catch (PDIException e) {
			throw newCoreException(e);
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#getLaunchAttributes(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void getLaunchAttributes(ILaunchConfiguration configuration, AttributeManager attrMgr) throws CoreException {
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());
		
		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}
		
		List<String> dbgArgs = dbgArgsAttr.getValue();

		Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();
		
		String localAddress = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, "localhost"); //$NON-NLS-1$
		
		dbgArgs.add("--host=" + localAddress); //$NON-NLS-1$
		dbgArgs.add("--debugger=" + store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)); //$NON-NLS-1$
		
		String dbgPath = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH);
		if (dbgPath.length() > 0) {
			dbgArgs.add("--debugger_path=" + dbgPath); //$NON-NLS-1$
		}
		
		String dbgExtraArgs = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS);
		if (dbgExtraArgs.length() > 0) {
			dbgArgs.addAll(Arrays.asList(dbgExtraArgs.split(" "))); //$NON-NLS-1$
		}
	
		// remote setting
		String dbgExePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);;
		if (dbgExePath == null) {
			dbgExePath = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_FILE);
		}
		PTPLaunchPlugin.getDefault().verifyResource(dbgExePath, configuration);
		
		IPath path = new Path(dbgExePath);
		attrMgr.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
		attrMgr.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(path.removeLastSegments(1).toString()));

		String dbgWD = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
		if (dbgWD != null) {
			StringAttribute wdAttr = (StringAttribute) attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition());
			if (wdAttr != null) {
				wdAttr.setValueAsString(dbgWD);
			} else {
				attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(dbgWD));
			}
			attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(dbgWD + "/Debug")); //$NON-NLS-1$
		}
		attrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));
	}
	
	/**
	 * Get the PDI debugger implementation. Creates the class if necessary.
	 * 
	 * @return IPDIDebugger
	 */
	private IPDIDebugger getDebugger() {
		if (pdiDebugger == null) {
			pdiDebugger = new PDIDebugger();
		}
		return pdiDebugger;
	}
	
	/**
	 * Work out the expected number of processes in the job. If it hasn't been
	 * specified, assume one.
	 * 
	 * @param job job that was launched
	 * @return number of processes
	 */
	private int getJobSize(IPJob job) {
		IntegerAttribute numProcAttr = job.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		if (numProcAttr != null) {
			return numProcAttr.getValue();
		}
		return 1;
	}
	
	/**
	 * Create a CoreException that can be thrown
	 * 
	 * @param exception
	 * @return CoreException
	 */
	private CoreException newCoreException(Throwable exception) {
		MultiStatus status = new MultiStatus(SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, "Cannot start debugging", exception);
		status.add(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
	}

	/**
	 * Create a PDI session
	 * 
	 * @param timeout
	 * @param launch
	 * @param corefile
	 * @param monitor
	 * @return Session
	 * @throws CoreException
	 */
	protected Session createSession(long timeout, IPLaunch launch, IPath corefile) throws CoreException {
		IPJob job = launch.getPJob();
		int job_size = getJobSize(job);
		try {
			return new Session(managerFactory, requestFactory, eventFactory, modelFactory,
					launch.getLaunchConfiguration(), timeout, getDebugger(), job.getID(), job_size);
		}
		catch (PDIException e) {
			throw newCoreException(e);
		}
	}
}
