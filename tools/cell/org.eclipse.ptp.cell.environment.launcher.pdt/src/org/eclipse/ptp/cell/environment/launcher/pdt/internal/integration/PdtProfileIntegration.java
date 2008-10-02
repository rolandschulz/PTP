/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.pdt.internal.integration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.cell.environment.launcher.pdt.Activator;
import org.eclipse.ptp.cell.environment.launcher.pdt.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.pdt.internal.IPdtLaunchAttributes;
import org.eclipse.ptp.cell.environment.launcher.pdt.internal.PdtLaunchBean;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.environment.launcher.core.NullLaunchIntegration;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


/**
 * @author Richard Maciel
 *
 */
public class PdtProfileIntegration extends NullLaunchIntegration {

	IRemoteExecutionManager executionManager;
	ILaunchConfiguration config;
	ILaunch launch;
	String mode;
	IProgressMonitor monitor;
	IPath projectPath;
	PdtLaunchBean launchBean;
	
	
	public PdtProfileIntegration(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor, PdtLaunchBean launchBean) {
		this.config = config;
		this.launch = launch;
		this.mode = mode;
		this.monitor = monitor;
		this.launchBean = launchBean; 
	}
	
	@Override
	public void setExecutionManager(IRemoteExecutionManager manager) {
		executionManager = manager;
	}
	
	@Override
	public String[] createLaunchScript(String applicationFullPath,
			String[] arguments) throws CoreException {
		// Don't have to modify the command-line.
		return super.createLaunchScript(applicationFullPath, arguments);
	}
	
	/**
	 * Copy xml file to the remote directory.
	 * 
	 */
	public void prepareLaunch() throws CoreException, CancelException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_PROFILE_INTEGRATION);
		
		boolean copyXmlFile =  config.getAttribute(
				IPdtLaunchAttributes.ATTR_COPY_XML_FILE, IPdtLaunchAttributes.DEFAULT_COPY_XML_FILE);
		String remoteXmldirPath = config.getAttribute(
				IPdtLaunchAttributes.ATTR_REMOTE_XML_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_XML_DIR);
		String localXmlfilePath = config.getAttribute(
				IPdtLaunchAttributes.ATTR_LOCAL_XML_FILE, IPdtLaunchAttributes.DEFAULT_LOCAL_XML_FILE); //TODO: set the complete filename of the xml file
		String remoteXmlFile = config.getAttribute(
				IPdtLaunchAttributes.ATTR_REMOTE_XML_FILE, IPdtLaunchAttributes.DEFAULT_REMOTE_XML_FILE);
		
		Debug.POLICY.trace(Debug.DEBUG_PROFILE_INTEGRATION_VARIABLES, "Launch attributes {0}", config.getAttributes()); //$NON-NLS-1$
		
		if(copyXmlFile) {
			try {
				// List all files in the directory where PDT generated the trace files
				IRemoteFileTools remFileTools = executionManager.getRemoteFileTools();
				remFileTools.assureDirectory(remoteXmldirPath);
				
				IRemoteCopyTools remCopyTools = executionManager.getRemoteCopyTools();
				remCopyTools.uploadFileToDir(new File(localXmlfilePath), remoteXmldirPath);
				
			} catch (RemoteConnectionException e) {
				Debug.POLICY.error(Debug.DEBUG_PROFILE_INTEGRATION, e);
				Debug.POLICY.logError(e);
				throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause())); 
			} catch (RemoteOperationException e) {
				Debug.POLICY.error(Debug.DEBUG_PROFILE_INTEGRATION, e);
				Debug.POLICY.logError(e);
				throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause()));
			}
		}
	}
	
	/**
	 * Copy generated profile data from the remote host to the
	 * pdt-trace directory
	 * 
	 */
	public void finalizeApplication() throws CoreException, CancelException {
		
		
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_PROFILE_INTEGRATION);
		
		String pdtOutputPath = config.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_TRACE_DIR);
		String pdtOutputPrefix = config.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX, IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX);
		Debug.POLICY.trace(Debug.DEBUG_PROFILE_INTEGRATION_VARIABLES, "Output path {0}", pdtOutputPath); //$NON-NLS-1$
		
		try {
			// List all files in the directory where PDT generated the trace files
			IRemoteFileTools remFileTools = executionManager.getRemoteFileTools();
			IRemoteDirectory pdtOutputDir = remFileTools.getDirectory(pdtOutputPath);
			Debug.POLICY.trace(Debug.DEBUG_PROFILE_INTEGRATION_VARIABLES, "Output dir {0}", pdtOutputDir.getPath()); //$NON-NLS-1$
			
			IRemoteItem [] dirItems = remFileTools.listItems(pdtOutputDir.getPath());
			
			// Filter the files. Only get the files with .trace/?/? extensions
			// whose prefix is equals to the prefix set by the user
			List<IRemoteFile> filteredItems = new LinkedList<IRemoteFile>();
			Debug.POLICY.trace(Debug.DEBUG_PROFILE_INTEGRATION_VARIABLES, "Non-filtered items {0}", filteredItems.toArray() ); //$NON-NLS-1$
			for(int i=0; i < dirItems.length; i++) {
				//if(dirItems[i]) // Verifies if it is a file
				IRemoteFile rFile = null;
				try {
					rFile = remFileTools.getFile(dirItems[i].getPath());
					
					// Look for the prefix of the filename
					IPath path = new Path(dirItems[i].getPath());
					if(path.lastSegment().matches("^" + pdtOutputPrefix + ".*")) {//$NON-NLS-1$ //$NON-NLS-2$
						filteredItems.add(rFile);
					}
				} catch(RemoteOperationException e) {
					if(!e.getMessage().contains("Not a file")) { //$NON-NLS-1$
						// Real problem. Throw it again
						Debug.POLICY.error(Debug.DEBUG_PROFILE_INTEGRATION, e);
						Debug.POLICY.logError(e);
						throw e;
					}
				}
				
				
			}
			Debug.POLICY.trace(Debug.DEBUG_PROFILE_INTEGRATION_VARIABLES, "Filtered items {0}", filteredItems.toArray() ); //$NON-NLS-1$
			
			// Copy the files to the pdt-trace directory inside the project 
			//executionManager.getRemoteCopyTools();
			IRemoteCopyTools copyTools = executionManager.getRemoteCopyTools();
			String localTraceDir = config.getAttribute(IPdtLaunchAttributes.ATTR_LOCAL_TRACE_DIR, IPdtLaunchAttributes.DEFAULT_LOCAL_TRACE_DIR);
			
			Debug.POLICY.trace(Debug.DEBUG_PROFILE_INTEGRATION_VARIABLES, "Local trace dir {0}", localTraceDir); //$NON-NLS-1$
			
			// If it didn't generated a trace file, throw an exception
			if(filteredItems.isEmpty()) {
				Debug.POLICY.error(Debug.DEBUG_PROFILE_INTEGRATION, "No tracefile generated"); //$NON-NLS-1$
				Debug.POLICY.logError(Messages.PdtProfileIntegration_FinalizeApplication_NoTraceFileGenerated);
				throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK, Messages.PdtProfileIntegration_FinalizeApplication_NoTraceFileGenerated, null));		
//				throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), Messages.PdtProfileIntegration_FinalizeApplication_NoTraceFileGenerated));		
			}
			
			for (IRemoteFile remoteFile : filteredItems) {
				copyTools.downloadFileToDir(remoteFile.getPath(), localTraceDir);
			}
			
			
		} catch (RemoteConnectionException e) {
			Debug.POLICY.error(Debug.DEBUG_PROFILE_INTEGRATION, e);
			Debug.POLICY.logError(e);
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause())); 
		} catch (RemoteOperationException e) {
			Debug.POLICY.error(Debug.DEBUG_PROFILE_INTEGRATION, e);
			Debug.POLICY.logError(e);
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e.getCause()));
		}
		
		Debug.POLICY.exit(Debug.DEBUG_PROFILE_INTEGRATION);
	}
	
	
}
