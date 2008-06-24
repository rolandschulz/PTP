/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.mpi.openmpi.core.Activator;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiMachineAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiNodeAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.Parameters;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMpiResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiHostMap.Host;

public class OpenMpiRuntimeSystem extends AbstractToolRuntimeSystem {

	private Parameters params = new Parameters();

	/** The machine where open mpi is running on. */
	private String machineID;
	/** The queue that dispatches jobs to mpi. */
	private String queueID;
	/** List of hosts discovered for the machine. */
	private OpenMpiHostMap hostMap;
	/** Mapping of discovered hosts and their ID for IPHost elements. */
	private Map<String,String> hostToElementMap = new HashMap<String, String>();

	public OpenMpiRuntimeSystem(Integer openmpi_rmid,
			OpenMpiResourceManagerConfiguration config,
			AttributeDefinitionManager attrDefMgr) {
		super(openmpi_rmid, config, attrDefMgr);
	}

	public String getMachineID() {
		return machineID;
	}

	public String getQueueID() {
		return queueID;
	}

	public Parameters getParameters() {
		return params;
	}
	
	public String getNodeIDforName(String hostname) {
		return hostToElementMap.get(hostname);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doShutdown(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doShutdown(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartEvents()
	 */
	@Override
	protected void doStartEvents() throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStopEvents()
	 */
	@Override
	protected void doStopEvents() throws CoreException {
		// Nothing to do
	}

	@Override
	protected Job createDiscoverJob() {
		if (! rmConfiguration.hasDiscoverCmd()) {
			return null;
		}
		Job job = new AbstractRemoteCommandJob(
				this,
				NLS.bind("Discover on {0}", rmConfiguration.getName()),
				rmConfiguration.getDiscoverCmd(),
				"Interrupted while running discover command.",
				"Failed to create remote process for discover command.",
				"Failed to parse output of discover command.") {


					@Override
					protected void parse(BufferedReader output) throws CoreException {
						/*
						 * MPI resource manager have only one machine and one queue.
						 * There they are implicitly "discovered".
						 */
						IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(getRmID());
						machineID = createMachine(rm.getName());
						queueID = createQueue("default");
						try {
							/*
							 * Parse output of command.
							 */
							// TODO validate lines and write to log if invalid lines were found.
							try {
								String line;
								while ((line = output.readLine()) != null) {
									int nameStart = line.indexOf(":param:");
									if (nameStart >= 0) {
										nameStart += 7;
										int pos = line.indexOf(":", nameStart);
										if (pos >= 0) {
											/*
											 * If parameter is already in list, then update, otherwise add.
											 */
											String name = line.substring(nameStart, pos);
											Parameters.Parameter param = params.getParameter(name);
											if (param == null) {
												param = params.addParameter(name);
											}
											int pos2;
											if ((pos2 = line.indexOf(":value:", pos)) >= 0) {
												param.setValue(line.substring(pos2 + 7));
											} else if ((pos2 = line.indexOf(":status:", pos)) >= 0) {
												if (line.substring(pos2 + 8).equals("read-only")) {
													param.setReadOnly(true);
												}
											} else if ((pos2 = line.indexOf(":help:", pos)) >= 0) {
												param.setHelp(line.substring(pos2 + 6));
											}
										}
									}
								}
							} catch (IOException e) {
								throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Parsing of parameters failed.", e));
							}

							/*
							 * Read file that describes machine geography.
							 * If no nodes are given, then we assume MPI default when host are not configured: there is only one node on the machine.
							 */
							int numNodes = 0;

							Parameters.Parameter param = params.getParameter("rds_hostfile_path");
							if (param != null) {
								String filename = param.getValue();

								if (filename != null) {
									IProgressMonitor monitor = new NullProgressMonitor();
									Assert.isNotNull(remoteServices);
									Assert.isNotNull(connection);

									IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
									IFileStore hostfile;
									try {
										hostfile = fileMgr.getResource(new Path(filename), monitor);
									} catch (IOException e) {
										throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
									}

									/*
									 * Read hostfile
									 */
									try {
										BufferedReader reader = new BufferedReader(new InputStreamReader(hostfile.openInputStream(EFS.NONE, monitor)));
										hostMap = OpenMpiHostMapParser.parse(reader);
									} catch (IOException e) {
										throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
									}
								}
							} else {
								throw new CoreException(new Status(IStatus.WARNING, Activator.getDefault().getBundle().getSymbolicName(), "Discover command did not inform file with host configuration."));
							}

							/*
							 * If no host information was found in the stream, add default.
							 */
							if (hostMap.count() == 0) {
								hostMap.addDefaultHost();
							}

							/*
							 * Create model according to data from discover.
							 */
							int rankCounter = 0;
							boolean hasSomeError = false;
							for (OpenMpiHostMap.Host host : hostMap.getHosts()) {
								String nodeId = createNode(machineID, host.getName(), rankCounter++);
								hostToElementMap.put(host.getName(), nodeId);
								IPNode node = PTPCorePlugin.getDefault().getUniverse().getResourceManager(getRmID()).getMachineById(machineID).getNodeById(nodeId);
								if (host.getNumProcessors() != 0) {
									try {
										node.addAttribute(OpenMpiNodeAttributes.getNumberOfNodesAttributeDefinition().create(host.getNumProcessors()));
									} catch (IllegalValueException e) {
										// This situation is not possible since host.getNumProcessors() is always valid.
										Assert.isTrue(false);
									}
								}
								if (host.getMaxNumProcessors() != 0) {
									try {
										node.addAttribute(OpenMpiNodeAttributes.getMaximalNumberOfNodesAttributeDefinition().create(host.getMaxNumProcessors()));
									} catch (IllegalValueException e) {
										// This situation is not possible since host.getMaxNumProcessors() is always valid.
										Assert.isTrue(false);
									}
								}
								if (host.getErrors() != 0) {
									if ((host.getErrors() & Host.ERR_MAX_NUM_SLOTS) != 0) {
										node.addAttribute(OpenMpiNodeAttributes.getStatusMessageDefinition().create("Invalid 'max-slots' parameter was ignored for this host."));
									} else if ((host.getErrors() & Host.ERR_NUM_SLOTS) != 0) {
										node.addAttribute(OpenMpiNodeAttributes.getStatusMessageDefinition().create("Invalid 'slots/cpus/count' parameter was ignored for this host."));
									} else if ((host.getErrors() & Host.ERR_UNKNOWN_ATTR) != 0) {
										node.addAttribute(OpenMpiNodeAttributes.getStatusMessageDefinition().create("Invalid parameter was ignored for this host."));
									}
									node.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));
									hasSomeError = true;
								}
							}
							if (hostMap.hasErrors) {
								IPMachine machine = PTPCorePlugin.getDefault().getUniverse().getResourceManager(getRmID()).getMachineById(machineID);
								machine.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
								machine.addAttribute(OpenMpiMachineAttributes.getStatusMessageDefinition().create("Parse error(s) in hostfile."));
							}
							if (hostMap.hasParseErrors() || hasSomeError) {
								throw new CoreException(new Status(IStatus.WARNING, Activator.getDefault().getBundle().getSymbolicName(), "There are errors in the hostfile."));
							}
						} catch (CoreException e) {
							if (e.getStatus().getSeverity() == IStatus.ERROR) {
								IPMachine machine = PTPCorePlugin.getDefault().getUniverse().getResourceManager(getRmID()).getMachineById(machineID);
								machine.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
								machine.addAttribute(OpenMpiMachineAttributes.getStatusMessageDefinition().create(NLS.bind("Error while running discove command. {0}", e.getMessage())));
							}
							throw e;
						}
					}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(false);
		return job;
	}

	/**
	 * Creates a job that periodically monitors the remote machine. The default implementation runs the periodic monitor
	 * command if defined in the RM capability.
	 * @return
	 */
	@Override
	protected Job createPeriodicMonitorJob() {
//		if (! rmConfiguration.hasPeriodicMonitorCmd()) {
//			return null;
//		}
//		Job job = new CommandJob(NLS.bind("Periodic monitor on {0}", rmConfiguration.getName()),
//				rmConfiguration.getPeriodicMonitorCmd(),
//				"Interrupted while running periodic monitor command.",
//				"Failed to create remote process for periodic monitor command.",
//				"Failed to parse output of periodic monitor command.",
//				rmConfiguration.getPeriodicMonitorTime()) {
//
//					@Override
//					protected void parse(BufferedReader output) throws CoreException {
//						doParsePeriodicMonitorCommand(output);
//					}
//		};
//		job.setPriority(Job.SHORT);
//		job.setSystem(true);
//		job.setUser(false);
//		return job;
		return null;
	}

	/**
	 * Creates a job that keeps monitoring the remote machine. The default implementation runs the continuous monitor
	 * command if defined in the RM capability.
	 * @return
	 */
	@Override
	protected Job createContinuousMonitorJob() {
//		if (! rmConfiguration.hasContinuousMonitorCmd()) {
//			return null;
//		}
//		Job job = new CommandJob(NLS.bind("Continuous monitor on {0}", rmConfiguration.getName()),
//				rmConfiguration.getContinuousMonitorCmd(),
//				"Interrupted while running continuous monitor command.",
//				"Failed to create remote process for continuous monitor command.",
//				"Failed to parse output of continuous monitor command.") {
//
//					@Override
//					protected void parse(BufferedReader output) throws CoreException {
//						doParseContinuousMonitorCommand(output);
//					}
//		};
//		job.setPriority(Job.LONG);
//		job.setSystem(true);
//		job.setUser(false);
//		return job;
		return null;
	}

	@Override
	public Job createRuntimeSystemJob(String jobID, AttributeManager attrMgr) {
		return new OpenMpiRuntimSystemJob(jobID, "Open Mpi Job", this, attrMgr);
	}
}
