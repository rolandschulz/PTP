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
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.io.BufferedReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteCommandJob;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2MachineAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2NodeAttributes;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;

/**
 * 
 * @author Greg Watson
 *
 */
public class MPICH2DiscoverJob extends AbstractRemoteCommandJob {
	MPICH2RuntimeSystem rts;

	public MPICH2DiscoverJob(MPICH2RuntimeSystem rts) {
		super(rts,
				NLS.bind(Messages.MPICH2DiscoverJob_name, rts.getRmConfiguration().getName()),
				rts.retrieveEffectiveToolRmConfiguration().getDiscoverCmd(),
				Messages.MPICH2DiscoverJob_interruptedErrorMessage,
				Messages.MPICH2DiscoverJob_processErrorMessage,
				Messages.MPICH2DiscoverJob_parsingErrorMessage);
		this.rts = rts;
	}

	@Override
	protected void parse(BufferedReader output) throws CoreException {
		/*
		 * MPI resource manager have only one machine and one queue.
		 * There they are implicitly "discovered".
		 */
		IResourceManager rm = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rts.getRmID());
		String machineID = rts.createMachine(rm.getName());
		rts.setMachineID(machineID);
		String queueID = rts.createQueue(Messages.MPICH2DiscoverJob_defaultQueueName);
		rts.setQueueID(queueID);

		IPMachine machine = rm.getMachineById(machineID);
		assert machine != null;

		/*
		 * Any exception from now on is caught in order to add the error message as an attribute to the machine.
		 * Then, the exception is re-thrown.
		 */
		try {
			/*
			 * Parse output of trace command that describes the system configuration.
			 */
			MPICH2TraceParser parser = new MPICH2TraceParser();
			MPICH2HostMap hostMap = parser.parse(output);
			if (hostMap == null) {
				machine.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
				machine.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(Messages.MPICH2DiscoverJob_Exception_HostFileParseError));
				throw new CoreException(new Status(IStatus.ERROR, MPICH2Plugin.getDefault().getBundle().getSymbolicName(), parser.getErrorMessage()));
			}

			/*
			 * Create model according to data from discover.
			 */
			int nodeCounter = 0;

			for (MPICH2HostMap.Host host : hostMap.getHosts()) {

				// Add node to model
				String nodeId = rts.createNode(machineID, host.getName(), nodeCounter++);

				// Add processor information to node.
				AttributeManager attrManager = new AttributeManager();
				if (host.getNumProcessors() != 0) {
					try {
						attrManager.addAttribute(MPICH2NodeAttributes.getNumberOfNodesAttributeDefinition().create(host.getNumProcessors()));
					} catch (IllegalValueException e) {
						// This situation is not possible since host.getNumProcessors() is always valid.
						assert false;
					}
				}
				rts.changeNode(nodeId, attrManager);
				rts.setNodeIDForName(host.getName(), nodeId);
			}
		} catch (CoreException e) {
			/*
			 * Show message of core exception and change machine status to error.
			 */
			if (e.getStatus().getSeverity() == IStatus.ERROR) {
				AttributeManager attrManager = new AttributeManager();
				attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
				attrManager.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(NLS.bind(Messages.MPICH2DiscoverJob_Exception_DiscoverCommandFailed, e.getMessage())));
				rts.changeMachine(machineID, attrManager);
			}
			throw e;
		} catch (Exception e) {
			/*
			 * Show message of all other exceptions and change machine status to error.
			 */
			AttributeManager attrManager = new AttributeManager();
			attrManager.addAttribute(MachineAttributes.getStateAttributeDefinition().create(MachineAttributes.State.ERROR));
			attrManager.addAttribute(MPICH2MachineAttributes.getStatusMessageAttributeDefinition().create(NLS.bind(Messages.MPICH2DiscoverJob_Exception_DiscoverCommandInternalError, e.getMessage())));
			rts.changeMachine(machineID, attrManager);
		}
	}
}
