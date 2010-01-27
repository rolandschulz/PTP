/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIMachineAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPINodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.OmpiInfo;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIHostMap.Host;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIDiscoverJob extends AbstractRemoteCommandJob {
	OpenMPIRuntimeSystem rts;

	public OpenMPIDiscoverJob(OpenMPIRuntimeSystem rts, IProgressMonitor monitor) {
		super(rts,
				NLS.bind(Messages.OpenMPIDiscoverJob_name, rts.getRmConfiguration().getName()),
				rts.retrieveEffectiveToolRmConfiguration().getDiscoverCmd(),
				Messages.OpenMPIDiscoverJob_interruptedErrorMessage,
				Messages.OpenMPIDiscoverJob_processErrorMessage,
				Messages.OpenMPIDiscoverJob_parsingErrorMessage,
				monitor);
		this.rts = rts;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob#parse(java.io.BufferedReader)
	 */
	@Override
	protected IStatus parse(BufferedReader output) {
		/*
		 * Local copy of attributes from the RuntimeSystem
		 */
		IRemoteConnection connection = rts.getConnection();
		assert connection != null;
		IRemoteServices remoteServices = rts.getRemoteServices();
		assert remoteServices != null;
		IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
		OmpiInfo info = rts.getOmpiInfo();
		IOpenMPIResourceManagerConfiguration rmConfiguration = (IOpenMPIResourceManagerConfiguration) rts.getRmConfiguration();
		assert fileMgr != null;

		/*
		 * MPI resource manager have only one machine and one queue.
		 * There they are implicitly "discovered".
		 */
		IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rts.getRmID());
		String machineID = rts.createMachine(connection.getName());
		rts.setMachineID(machineID);
		String queueID = rts.createQueue(Messages.OpenMPIDiscoverJob_defaultQueueName);
		rts.setQueueID(queueID);

		IPMachine machine = rm.getMachineById(machineID);
		assert machine != null;

		/*
		 * Any exception from now on is caught in order to add the error message as an attribute to the machine.
		 * Then, the exception is re-thrown.
		 */
		try {
			/*
			 * STEP 1:
			 * Parse output of command.
			 * TODO: validate lines and write to log if invalid lines were found.
			 */
			parseOmpiInfo(output, info);
			
			/*
			 * Check and/or set the OMPI version in the configuration. NOTE that this may change any
			 * subsequent commands that are queried!
			 */
			String version = info.get("ompi:version:full"); //$NON-NLS-1$
			if (version != null) {
				if (!rmConfiguration.setDetectedVersion(version)) {
					return new Status(IStatus.ERROR, OpenMPIPlugin.getUniqueIdentifier(), NLS.bind(Messages.OpenMPIDiscoverJob_Exception_InvalidVersion, version));
				}
			} else {
				return new Status(IStatus.ERROR, OpenMPIPlugin.getUniqueIdentifier(), Messages.OpenMPIDiscoverJob_Exception_UnableToDetermineVersion);
			}
			
			/*
			 * STEP 2:
			 * Read file that describes machine geography.
			 * If no nodes are given, then we assume MPI default when host are not configured: there is only one node on the machine.
			 * This part is a bit tricky.
			 * OpenMPI 1.2 has a RDS (resource discovery system) that knows the default hostfile as rds_hostfile_path parameter.
			 * But the RDS was dropped by version 1.3.
			 * Then the orte_default_hostfile parameter might be used instead, as long as it was defined in the system wide MCA parameters.
			 */
			OpenMPIHostMap hostMap = readHostFile(connection, remoteServices, fileMgr, info, rmConfiguration);

			/*
			 * Create model according to data from discover.
			 */
			int rankCounter = 0;
			boolean hasSomeError = false;
			assert hostMap != null;

			for (OpenMPIHostMap.Host host : hostMap.getHosts()) {

				// Add node to model
				String nodeId = rts.createNode(machineID, host.getName(), rankCounter++);
				rts.setNodeIDForName(host.getName(), nodeId);

				// Add processor information to node.
				AttributeManager attrManager = new AttributeManager();
				if (host.getNumProcessors() != 0) {
					try {
						attrManager.addAttribute(OpenMPINodeAttributes.getNumberOfNodesAttributeDefinition().create(Integer.valueOf(host.getNumProcessors())));
					} catch (IllegalValueException e) {
						// This situation is not possible since host.getNumProcessors() is always valid.
						assert false;
					}
				}
				if (host.getMaxNumProcessors() != 0) {
					try {
						attrManager.addAttribute(OpenMPINodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(Integer.valueOf(host.getMaxNumProcessors())));
					} catch (IllegalValueException e) {
						// This situation is not possible since host.getMaxNumProcessors() is always valid.
						assert false;
					}
				}
				if (host.getErrors() != 0) {
					if ((host.getErrors() & Host.ERR_MAX_NUM_SLOTS) != 0) {
						attrManager.addAttribute(OpenMPINodeAttributes.getStatusMessageAttributeDefinition().create(Messages.OpenMPIDiscoverJob_Exception_InvalidMaxSlotsParameter));
					} else if ((host.getErrors() & Host.ERR_NUM_SLOTS) != 0) {
						attrManager.addAttribute(OpenMPINodeAttributes.getStatusMessageAttributeDefinition().create(Messages.OpenMPIDiscoverJob_Exception_InvalidSlotsParameter));
					} else if ((host.getErrors() & Host.ERR_UNKNOWN_ATTR) != 0) {
						attrManager.addAttribute(OpenMPINodeAttributes.getStatusMessageAttributeDefinition().create(Messages.OpenMPIDiscoverJob_Exception_IgnoredInvalidParameter));
					}
					attrManager.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));
					hasSomeError = true;
				}
				rts.changeNode(nodeId, attrManager);
			}
			if (hostMap.hasErrors) {
				machine.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
				machine.addAttribute(OpenMPIMachineAttributes.getStatusMessageAttributeDefinition().create(Messages.OpenMPIDiscoverJob_Exception_HostFileParseError));
			}
			if (hostMap.hasParseErrors() || hasSomeError) {
				return new Status(IStatus.WARNING, OpenMPIPlugin.getDefault().getBundle().getSymbolicName(), Messages.OpenMPIDiscoverJob_Exception_HostFileErrors);
			}
		} catch (Exception e) {
			/*
			 * Show message of all other exceptions and change machine status to error.
			 */
			AttributeManager attrManager = new AttributeManager();
			attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
			attrManager.addAttribute(OpenMPIMachineAttributes.getStatusMessageAttributeDefinition().create(NLS.bind(Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandInternalError, e.getMessage())));
			rts.changeMachine(machineID, attrManager);
			rm.addAttribute(ResourceManagerAttributes.getStateAttributeDefinition().create(ResourceManagerAttributes.State.ERROR));
			return new Status(IStatus.ERROR, OpenMPIPlugin.getUniqueIdentifier(), NLS.bind(Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandInternalError, e.getMessage()), e);
		}
		
		return Status.OK_STATUS;
	}

	private OpenMPIHostMap readHostFile(IRemoteConnection connection,
			IRemoteServices remoteServices, IRemoteFileManager fileMgr,
			OmpiInfo info,
			IOpenMPIResourceManagerConfiguration rmConfiguration)
	throws CoreException, IOException {

		/*
		 * OpenMpi 1.2 uses rds_hostfile_path. Open 1.3 uses orte_default_hostfile.
		 * For 1.2, path must not be empty. For 1.3 it may be empty and default host is assumed.
		 */
		OpenMPIHostMap hostMap = null;
		String hostFile = null;
		IPath hostFilePath = null;

		Parameters.Parameter rds_param = info.getParameter("rds_hostfile_path"); //$NON-NLS-1$
		Parameters.Parameter orte_param = info.getParameter("orte_default_hostfile"); //$NON-NLS-1$
		String prefix = info.get("path:prefix"); //$NON-NLS-1$

		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "rds_hostfile_path: {0}", (rds_param==null?"null":rds_param.getValue())); //$NON-NLS-1$  //$NON-NLS-2$
		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "orte_default_hostfile: {0}", (orte_param==null?"null":orte_param.getValue())); //$NON-NLS-1$  //$NON-NLS-2$
		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "prefix: {0}", (prefix==null?"null":prefix)); //$NON-NLS-1$  //$NON-NLS-2$

		if (rds_param != null) {
			hostFile = rds_param.getValue();
			if (hostFile.trim().length() != 0) {
				hostFilePath = new Path(hostFile);
			}
		}
		
		if (hostFilePath == null && orte_param != null) {
			hostFile = orte_param.getValue();
			if (hostFile.trim().length() != 0) {
				hostFilePath = new Path(hostFile);
			}
		}
		
		if (hostFilePath == null) {
			if (rmConfiguration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
				DebugUtil.error(DebugUtil.RTS_DISCOVER_TRACING, "Missing mandatory hostfile for Open MPI 1.2."); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandMissingHostFilePath));
			}
			
			hostMap = new OpenMPIHostMap();
			String hostname = getRemoteHostname(connection, remoteServices);
			hostMap.addDefaultHost(hostname);
			DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "Missing optional hostfile. Assuming {0} as default host.", hostname); //$NON-NLS-1$
			return hostMap;
		}
		
		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "Found hostfile for Open MPI"); //$NON-NLS-1$
		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "hostFilePath: {0}", hostFilePath); //$NON-NLS-1$

		if (!hostFilePath.isAbsolute()) {
			if (rmConfiguration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
				throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, NLS.bind(Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandHostFilePathNotAbsolute, hostFilePath)));
			}
			
			hostMap = new OpenMPIHostMap();
			String hostname = getRemoteHostname(connection, remoteServices);
			hostMap.addDefaultHost(hostname);
			DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "Bad hostfile specification. Assuming {0} as default host.", hostname); //$NON-NLS-1$
			return hostMap;
		}
		
		// Try to read.
		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "Opening hostfile."); //$NON-NLS-1$
		IProgressMonitor monitor = new NullProgressMonitor();
		IFileStore hostfile = fileMgr.getResource(hostFilePath.toString());

		InputStream is = null;
		try {
			is = hostfile.openInputStream(EFS.NONE, monitor);
		} catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, NLS.bind(Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandFailedReadHostFile, hostfile), e));
		}

		DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "Parsing hostfile."); //$NON-NLS-1$
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			hostMap = OpenMPIHostMapParser.parse(reader);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, NLS.bind(Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandFailedParseHostFile, hostfile), e));
		}
		
		/*
		 * If no host information was found in the hostfile, add default.
		 * Only for Open MPI 1.2. On 1.3, there is no default host file assumed.
		 */
		if (hostMap.count() == 0) {
			if (rmConfiguration.getDetectedVersion().equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
				// This was not correct for remote hosts. Worked only for local hosts.
				//					try {
				//						InetAddress localhost = InetAddress.getLocalHost();
				//						hostMap.addDefaultHost(localhost.getHostName());
				//					} catch (UnknownHostException e) {
				//						throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Cannot retrive network information for local machine. Check network configuration."));
				//					}
				String hostname = getRemoteHostname(connection, remoteServices);
				hostMap.addDefaultHost(hostname);
				DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "Hostfile is empty. Added default host {0} for Open MPI 1.2.", hostname); //$NON-NLS-1$
			} else {
				DebugUtil.error(DebugUtil.RTS_DISCOVER_TRACING, "Empty hostfile is not allowed."); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, NLS.bind(Messages.OpenMPIDiscoverJob_Exception_DiscoverCommandHostFileEmpty, hostfile)));
			}
		}
		return hostMap;
	}

	private String getRemoteHostname(IRemoteConnection connection,
			IRemoteServices remoteServices) throws CoreException, IOException {
		IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, "hostname"); //$NON-NLS-1$
		IRemoteProcess process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, Messages.OpenMPIDiscoverJob_Exception_HostnameCommandFailed, e));
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// Ignore
		}
		if (process.exitValue() != 0)
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, NLS.bind(Messages.OpenMPIDiscoverJob_Exception_HostnameCommandFailedWithCode, Integer.valueOf(process.exitValue()))));
		String hostname = br.readLine();
		if (hostname == null)
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.PLUGIN_ID, Messages.OpenMPIDiscoverJob_Exception_HostnameCommandFailedParse));
		return hostname;
	}

	private void parseOmpiInfo(BufferedReader output, OmpiInfo info)
	throws CoreException {

		try {
			String line;
			while ((line = output.readLine()) != null) {
				if (line.indexOf("mca:") == 0) { //$NON-NLS-1$
					int nameStart = line.indexOf(":param:"); //$NON-NLS-1$
					if (nameStart >= 0) {
						nameStart += 7;
						int pos = line.indexOf(":", nameStart); //$NON-NLS-1$
						if (pos >= 0) {
							/*
							 * If parameter is already in list, then update, otherwise add.
							 */
							String name = line.substring(nameStart, pos);
							Parameters.Parameter param = info.getParameter(name);
							if (param == null) {
								param = info.addParameter(name);
							}
							int pos2;
							if ((pos2 = line.indexOf(":value:", pos)) >= 0) { //$NON-NLS-1$
								param.setValue(line.substring(pos2 + 7));
							} else if ((pos2 = line.indexOf(":status:", pos)) >= 0) { //$NON-NLS-1$
								if (line.substring(pos2 + 8).equals("read-only")) { //$NON-NLS-1$
									param.setReadOnly(true);
								}
							} else if ((pos2 = line.indexOf(":help:", pos)) >= 0) { //$NON-NLS-1$
								param.setHelp(line.substring(pos2 + 6));
							}
						}
					}
				} else {
					int valStart = line.lastIndexOf(":"); // will fail if value contains a colon! //$NON-NLS-1$
					if (valStart >= 0) {
						info.add(line.substring(0, valStart), line.substring(valStart+1));
					}
				}
			}
			if (DebugUtil.RTS_DISCOVER_TRACING) {
				System.out.println("Open MPI parameters:"); //$NON-NLS-1$
				for (Parameters.Parameter param : info.getParameters()) {
					System.out.println(MessageFormat.format("  {0}={1}", param.getName(), param.getValue())); //$NON-NLS-1$
				}
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIPlugin.getDefault().getBundle().getSymbolicName(), Messages.OpenMPIDiscoverJob_Exception_HostnameCommandFailedParseOutput, e));
		}
	}
}
