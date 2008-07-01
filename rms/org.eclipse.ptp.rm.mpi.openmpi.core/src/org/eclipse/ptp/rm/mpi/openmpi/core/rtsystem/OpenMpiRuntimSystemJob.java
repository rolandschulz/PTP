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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.DefaultToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.ILineStreamListener;
import org.eclipse.ptp.rm.core.utils.TextStreamObserver;
import org.eclipse.ptp.rm.mpi.openmpi.core.Activator;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiJobAttributes;

public class OpenMpiRuntimSystemJob extends DefaultToolRuntimeSystemJob {
	private TextStreamObserver stderrObserver;
	private TextStreamObserver stdoutObserver;
	private BufferedReader inReader;
	private BufferedReader errReader;
	/** Information parsed from launch command. */
	OpenMpiProcessMap map;
	/** Mapping of processes created by this job. */
	private Map<String,IPProcess> processMap = new HashMap<String, IPProcess>();
	/** Process with rank 0 (zero) that prints all output. */
	private String rankZeroProcessID;

	public OpenMpiRuntimSystemJob(String jobID, String name, AbstractToolRuntimeSystem rtSystem, AttributeManager attrMgr) {
		super(jobID, name, rtSystem, attrMgr);
	}


	@Override
	protected void doExecutionStarted() throws CoreException {
		inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		try {
			map = OpenMpiProcessMapParser.parse(errReader);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to parse Open Mpi run command output.", e));
		}
//		System.out.println(map);
		OpenMpiRuntimeSystem rtSystem = (OpenMpiRuntimeSystem) getRtSystem();
		IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(rtSystem.getQueueID()).getJobById(getJobID());
		try {
			ipJob.addAttribute(OpenMpiJobAttributes.getMpiJobId().create(map.map_for_job));
			ipJob.addAttribute(OpenMpiJobAttributes.getVpidStart().create(map.starting_vpid));
			ipJob.addAttribute(OpenMpiJobAttributes.getVpidRange().create(map.vpid_range));
			if (map.mapping_mode == org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiProcessMap.MappingMode.bynode) {
				ipJob.addAttribute(OpenMpiJobAttributes.getMappingModeDefinition().create(org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiJobAttributes.MappingMode.BY_NODE));
			} else if (map.mapping_mode == org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiProcessMap.MappingMode.byslot) {
				ipJob.addAttribute(OpenMpiJobAttributes.getMappingModeDefinition().create(org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiJobAttributes.MappingMode.BY_SLOT));
			} else {
				ipJob.addAttribute(OpenMpiJobAttributes.getMappingModeDefinition().create(org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiJobAttributes.MappingMode.UNKNOWN));
			}
			ipJob.addAttribute(OpenMpiJobAttributes.getNumMappedNodesDefinition().create(map.mappedNodes.size()));
		} catch (IllegalValueException e) {
			// No invalid values can be generated
			Assert.isTrue(false);
		}
		if (map.mapping_mode == OpenMpiProcessMap.MappingMode.bynode) {
			ipJob.addAttribute(OpenMpiJobAttributes.getMappingModeDefinition().create(OpenMpiJobAttributes.MappingMode.BY_NODE));
		} else if (map.mapping_mode == OpenMpiProcessMap.MappingMode.byslot) {
			ipJob.addAttribute(OpenMpiJobAttributes.getMappingModeDefinition().create(OpenMpiJobAttributes.MappingMode.BY_SLOT));
		}
		int procCount = ipJob.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition()).getValue();

		String procIDs[] = new String[procCount];
		for (OpenMpiProcessMap.MappedNode node : map.mappedNodes) {
			String nodename = node.nodename;
			String nodeID = rtSystem.getNodeIDforName(nodename);
			
			Assert.isNotNull(nodeID);
			for (OpenMpiProcessMap.MappedProc proc : node.procs) {
				String name = proc.getName();
				int index = proc.getRank();
				procIDs[index] = rtSystem.createProcess(getJobID(), name, index);
				IPProcess ipProc = ipJob.getProcessById(procIDs[index]);
				processMap.put(procIDs[index], ipProc);
				if (index == 0) {
					/*
					 * Exactly one process must have rank 0.
					 */
					Assert.isTrue(rankZeroProcessID == null);
					rankZeroProcessID = procIDs[index];
				}
				try {
					ipProc.addAttribute(ProcessAttributes.getPIDAttributeDefinition().create(proc.getPID()));
					ipProc.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));
				} catch (IllegalValueException e) {
					// No invalid values can be generated
					Assert.isTrue(false);
				}
			}
			/*
			 * Exactly one process must have rank 0.
			 */
			Assert.isNotNull(rankZeroProcessID);
		}

	}

	@Override
	protected void doWaitExecution() throws CoreException {
		OpenMpiRuntimeSystem rtSystem = (OpenMpiRuntimeSystem) getRtSystem();
		final IPProcess ipProc = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(rtSystem.getQueueID()).getJobById(getJobID()).getProcessById(rankZeroProcessID);

		stdoutObserver = new TextStreamObserver(
				inReader,
				new ILineStreamListener() {
					public void newLine(String line) {
//						System.out.println(line);
						ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
					}

					public void streamClosed() {
						System.out.println("stdout closed");
					}

					public void streamError(Exception e) {
						System.out.println("sdtout "+e.getLocalizedMessage());
					}
				}
		);
		stdoutObserver.start();

		stderrObserver = new TextStreamObserver(
				errReader,
				new ILineStreamListener() {
					public void newLine(String line) {
//						System.err.println(line);
						ipProc.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(line));
					}

					public void streamClosed() {
						System.err.println("stderr closed");
					}

					public void streamError(Exception e) {
						System.err.println("stderr "+e.getLocalizedMessage());
					}
				}
		);
		stderrObserver.start();

		try {
			stderrObserver.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			stdoutObserver.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void doTerminateJob() {
		if (stderrObserver != null) {
			stderrObserver.kill();
			stderrObserver = null;
		}
		if (stdoutObserver != null) {
			stdoutObserver.kill();
			stdoutObserver = null;
		}
	}

	@Override
	protected void doExecutionFinished() throws CoreException {
		for (IPProcess proc : processMap.values()) {
			proc.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.EXITED));
		}
	}

	@Override
	protected void doExecutionCleanUp() {
	}

	@Override
	protected IAttribute<?, ?, ?>[] getExtraSubstitutionVariables() throws CoreException {
		List<IAttribute<?, ?, ?>> newAttributes = new ArrayList<IAttribute<?,?,?>>();
		ArrayAttribute<String> environmentAttribute = getAttrMgr().getAttribute(JobAttributes.getEnvironmentAttributeDefinition());

		if (environmentAttribute != null) {
			List<String> environment = environmentAttribute.getValue();
			int p = 0;
			String keys[] = new String[environment.size()];
			for (String var : environment) {
				int i = var.indexOf('=');
				String key = var.substring(0, i);
				keys[p++] = key;
			}
			newAttributes.add(OpenMpiJobAttributes.getEnvironmentKeysDefinition().create(keys));
		}
		return newAttributes.toArray(new IAttribute<?, ?, ?>[newAttributes.size()]);
	}

	protected IAttributeDefinition<?, ?, ?>[] getDefaultSubstitutionAttributes() {
		IAttributeDefinition<?, ?, ?>[] attributesFromSuper = super.getDefaultSubstitutionAttributes();
		IAttributeDefinition<?, ?, ?>[] moreAttributes = new IAttributeDefinition[] {
				OpenMpiJobAttributes.getEnvironmentKeysDefinition(),
			};
		IAttributeDefinition<?, ?, ?>[]  allAttributes = new IAttributeDefinition[attributesFromSuper.length+moreAttributes.length];
	   System.arraycopy(attributesFromSuper, 0, allAttributes, 0, attributesFromSuper.length);
	   System.arraycopy(moreAttributes, 0, allAttributes, attributesFromSuper.length, moreAttributes.length);
	   return allAttributes;
	}
}
