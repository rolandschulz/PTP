/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rtsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ErrorAttributes;
import org.eclipse.ptp.core.elements.attributes.FilterAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.attributes.MessageAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration;
import org.eclipse.ptp.rm.smoa.core.SMOACoreActivator;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOAMachineAttributes;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOANodeAttributes;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOAQueueAttributes;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOARMAttributes;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManager;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAConnection;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;
import org.eclipse.ptp.rtsystem.AbstractRuntimeSystem;
import org.eclipse.ptp.rtsystem.events.IRuntimeEventFactory;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRMChangeEvent;
import org.eclipse.ptp.rtsystem.events.RuntimeEventFactory;
import org.eclipse.ptp.utils.core.ArgumentParser;
import org.eclipse.ptp.utils.core.RangeSet;

import com.smoa.comp.sdk.SMOAFactory;
import com.smoa.comp.stubs.bes.factory.BasicResourceAttributesDocumentType;
import com.smoa.comp.stubs.bes.factory.FactoryResourceAttributesDocumentType;
import com.smoa.comp.stubs.factory.ApplicationsType.Application;

/**
 * Co-works with {@link SMOAResourceManager} in order to manage ResourceManager
 * and jobs. Is created by {@link SMOAResourceManager}.
 * 
 * Methods from this class are called by Eclipse in order to start and terminate
 * a job
 */
public class SMOARuntimeSystem extends AbstractRuntimeSystem {

	private final SMOAConfiguration configuration;
	private int nextJobNumber = 0;

	/** Creates events for the ResourceManager */
	private final IRuntimeEventFactory eventFactory = new RuntimeEventFactory();

	/** Caches errors from {@link SMOAResourceManager} by submitting jobs */
	private final Map<String, Exception> jobSbmissionErrors = new HashMap<String, Exception>();

	/** Resource Manager ID (needed for RM change events) */
	private final String rmId;

	/** Attribute definitions for the RTS. */
	protected AttributeDefinitionManager attrMgr = new AttributeDefinitionManager();

	public SMOARuntimeSystem(SMOAConfiguration configuration, String rmId) {
		super();
		this.configuration = configuration;
		this.rmId = rmId;
	}

	public void addJobSubmissionError(String subId, Exception e) {
		jobSbmissionErrors.put(subId, e);
	}

	public void addUnknownNode(IPMachine machine, String name) {
		final AttributeManager am = new SMOANodeAttributes();

		am.addAttribute(ElementAttributes.getNameAttributeDefinition().create(name));
		am.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(name));

		final ElementAttributeManager eam = new ElementAttributeManager();
		eam.setAttributeManager(new RangeSet(machine.getNodes().length), am);

		final IRuntimeNewNodeEvent irnne = eventFactory.newRuntimeNewNodeEvent(machine.getID(), eam);

		fireRuntimeNewNodeEvent(irnne);
	}

	public void filterEvents(IPElement element, boolean filterChildren, AttributeManager filterAttributes) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IControlSystem#getAttributeDefinitionManager()
	 */
	public AttributeDefinitionManager getAttributeDefinitionManager() {
		return attrMgr;
	}

	public void shutdown() throws CoreException {

	}

	public void startEvents() throws CoreException {
	}

	/**
	 * Opens the connection and retrieves basic information about RM, machine
	 * and nodes. Creates the default queue
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {

		initialize();

		final SMOARemoteServices remoteServices = (SMOARemoteServices) PTPRemoteCorePlugin.getDefault().getRemoteServices(
				configuration.getRemoteServicesId());

		final SMOAConnection connection = remoteServices.getConnectionManager().getConnection(configuration.getConnectionName());

		if (!connection.isOpen()) {
			try {
				connection.open(monitor);
			} catch (final RemoteConnectionException e1) {
				throw new CoreException(new Status(IStatus.ERROR, SMOACoreActivator.PLUGIN_ID, e1.getLocalizedMessage(), e1));
			}
		}

		final SMOAFactory besFactory = connection.getFactory();

		FactoryResourceAttributesDocumentType cluster;

		try {
			cluster = besFactory.getFactoryAttributesDocument();
		} catch (final Throwable t) {
			throw new CoreException(new Status(IStatus.ERROR, SMOACoreActivator.PLUGIN_ID, t.getLocalizedMessage(), t));
		}

		final List<Application> apps = SMOAFactory.getApplications(cluster).getApplication();

		configuration.setAvailableAppList(apps);

		// Machine && nodes information
		{
			final ElementAttributeManager machines = new ElementAttributeManager();
			final AttributeManager machine = new SMOAMachineAttributes();

			machine.addAttribute(ElementAttributes.getNameAttributeDefinition().create(cluster.getCommonName()));

			try {
				machine.addAttribute(MachineAttributes.getNumNodesAttributeDefinition().create(
						cluster.getContainedResource().size()));
			} catch (final IllegalValueException e1) {
				// One of the "should never happen"
				throw new CoreException(new Status(IStatus.ERROR, SMOACoreActivator.PLUGIN_ID, e1.getLocalizedMessage(), e1));
			}

			machines.setAttributeManager(new RangeSet(0), machine);

			final IRuntimeNewMachineEvent irnme = eventFactory.newRuntimeNewMachineEvent(null, machines);
			fireRuntimeNewMachineEvent(irnme);

			int i = 0;

			for (final Object _ : cluster.getContainedResource()) {
				final BasicResourceAttributesDocumentType host = (BasicResourceAttributesDocumentType) _;

				try {
					final AttributeManager am = new SMOANodeAttributes();

					am.addAttribute(ElementAttributes.getNameAttributeDefinition().create(host.getResourceName()));
					am.addAttribute(ProcessAttributes.getNodeIdAttributeDefinition().create(host.getResourceName()));
					am.addAttribute(SMOANodeAttributes.getCpuArchDef().create(
							host.getCPUArchitecture().getCPUArchitectureName().toString()));
					am.addAttribute(SMOANodeAttributes.getCpuCountDef().create(host.getCPUCount().intValue()));
					am.addAttribute(SMOANodeAttributes.getMemorySizeDef().create((host.getPhysicalMemory() / (1024 * 1024)) + "MB")); //$NON-NLS-1$

					am.addAttribute(NodeAttributes.getStateAttributeDefinition().create(NodeAttributes.State.UP));

					final ElementAttributeManager eam = new ElementAttributeManager();
					eam.setAttributeManager(new RangeSet(i++), am);

					final IRuntimeNewNodeEvent irnne = eventFactory.newRuntimeNewNodeEvent("0", eam); //$NON-NLS-1$

					fireRuntimeNewNodeEvent(irnne);

				} catch (final IllegalValueException e) {
					throw new CoreException(new Status(IStatus.ERROR, SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
				}
			}

		}

		// Queue information

		{
			final RangeSet rs = new RangeSet(0);
			final AttributeManager am = new SMOAQueueAttributes();
			try {
				am.getAttribute("name").setValueAsString("Default queue"); //$NON-NLS-1$ //$NON-NLS-2$
				am.getAttribute("id").setValueAsString("0"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (final IllegalValueException e) {
				throw new RuntimeException(e);
			}

			final ElementAttributeManager eam = new ElementAttributeManager();
			eam.setAttributeManager(rs, am);

			final IRuntimeNewQueueEvent irnqe = eventFactory.newRuntimeNewQueueEvent(null, eam);
			fireRuntimeNewQueueEvent(irnqe);
		}

		// RM informations

		{

			final ElementAttributeManager eam = new ElementAttributeManager();
			final RangeSet rs = new RangeSet(rmId);
			final AttributeManager am = new SMOARMAttributes();

			am.addAttribute(SMOARMAttributes.getQueuingSystemDef().create(cluster.getLocalResourceManagerType()));
			am.addAttribute(SMOARMAttributes.getAcceptsActivitiesDef().create(cluster.isIsAcceptingNewActivities()));
			am.addAttribute(SMOARMAttributes.getCommonNameDef().create(cluster.getCommonName()));

			eam.setAttributeManager(rs, am);
			final IRuntimeRMChangeEvent irrmce = eventFactory.newRuntimeRMChangeEvent(eam);
			fireRuntimeRMChangeEvent(irrmce);
		}

	}

	public void stopEvents() throws CoreException {
	}

	/**
	 * Called by Eclipse when the job is to be submitted.
	 * 
	 * @param subId
	 *            - eclipse submission ID. {@link SMOAResourceManager} needs
	 *            this to inform Eclipse that the job has been submitted.
	 */
	public void submitJob(String subId, ILaunchConfiguration configuration, String mode) throws CoreException {

		AttributeManager attrMgr = new AttributeManager(getAttributes(configuration, mode).toArray(new IAttribute<?, ?, ?>[0]));

		attrMgr.addAttribute(JobAttributes.getSubIdAttributeDefinition().create(subId));

		final RangeSet rs = new RangeSet(nextJobNumber);
		nextJobNumber++;
		final ElementAttributeManager eam = new ElementAttributeManager();
		eam.setAttributeManager(rs, attrMgr);

		final IRuntimeNewJobEvent irnje = eventFactory.newRuntimeNewJobEvent("0", eam); //$NON-NLS-1$
		fireRuntimeNewJobEvent(irnje);

		final Exception e = jobSbmissionErrors.remove(subId);
		if (e != null) {
			throw new CoreException(new Status(IStatus.ERROR, SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IControlSystem#terminateJob(java.lang.String)
	 */
	public void terminateJob(String jobId) throws CoreException {
	}

	/**
	 * Change attributes of a job
	 * 
	 * @param jobID
	 * @param changedAttrMgr
	 */
	public void changeJob(String jobID, AttributeManager changedAttrMgr) {
		AttributeManager attrMgr = new AttributeManager();
		attrMgr.addAttributes(changedAttrMgr.getAttributes());
		ElementAttributeManager elementAttrs = new ElementAttributeManager();
		elementAttrs.setAttributeManager(new RangeSet(jobID), attrMgr);
		IRuntimeJobChangeEvent event = eventFactory.newRuntimeJobChangeEvent(elementAttrs);
		fireRuntimeJobChangeEvent(event);
	}

	/**
	 * Convert launch configuration attributes to PTP attributes
	 */
	private List<IAttribute<?, ?, ?>> getAttributes(ILaunchConfiguration configuration, String mode) throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();

		/*
		 * Collect attributes from Application tab
		 */
		String exePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);
		if (exePath != null) {
			IPath programPath = new Path(exePath);
			attrs.add(JobAttributes.getExecutableNameAttributeDefinition().create(programPath.lastSegment()));

			String path = programPath.removeLastSegments(1).toString();
			if (path != null) {
				attrs.add(JobAttributes.getExecutablePathAttributeDefinition().create(path));
			}
		}

		/*
		 * Collect attributes from Arguments tab
		 */
		String wd = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
		if (wd != null) {
			attrs.add(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
		}

		String[] args = getProgramArguments(configuration, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS);
		if (args != null) {
			attrs.add(JobAttributes.getProgramArgumentsAttributeDefinition().create(args));
		}

		/*
		 * Collect attributes from Environment tab
		 */
		String[] envArr = getEnvironment(configuration);
		if (envArr != null) {
			attrs.add(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
		}

		/*
		 * Collect attributes from Debugger tab if this is a debug launch
		 */
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			boolean stopInMainFlag = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
			attrs.add(JobAttributes.getDebuggerStopInMainFlagAttributeDefinition().create(Boolean.valueOf(stopInMainFlag)));

			attrs.add(JobAttributes.getDebugFlagAttributeDefinition().create(Boolean.TRUE));

			args = getProgramArguments(configuration, IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ARGS);
			if (args != null) {
				attrs.add(JobAttributes.getDebuggerArgumentsAttributeDefinition().create(args));
			}

			String dbgExePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					(String) null);
			if (dbgExePath != null) {
				IPath path = new Path(dbgExePath);
				attrs.add(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
				attrs.add(JobAttributes.getDebuggerExecutablePathAttributeDefinition()
						.create(path.removeLastSegments(1).toString()));
			}
		}

		/*
		 * PTP launched this job
		 */
		attrs.add(JobAttributes.getLaunchedByPTPFlagAttributeDefinition().create(Boolean.valueOf(true)));

		return attrs;
	}

	/**
	 * Get environment to append
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
		Map<?, ?> defaultEnv = null;
		Map<?, ?> configEnv = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, defaultEnv);
		if (configEnv == null) {
			return null;
		}

		List<String> strings = new ArrayList<String>(configEnv.size());
		Iterator<?> iter = configEnv.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			strings.add(key + "=" + value); //$NON-NLS-1$

		}
		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Convert application arguments to an array of strings.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return array of strings containing the program arguments
	 * @throws CoreException
	 */
	private String[] getProgramArguments(ILaunchConfiguration configuration, String attrName) throws CoreException {
		String temp = configuration.getAttribute(attrName, (String) null);
		if (temp != null && temp.length() > 0) {
			ArgumentParser ap = new ArgumentParser(temp);
			List<String> args = ap.getTokenList();
			if (args != null) {
				return args.toArray(new String[args.size()]);
			}
		}
		return new String[0];
	}

	/**
	 * Initialize the attribute manager. This is called each time the runtime is
	 * started.
	 */
	private void initialize() {
		attrMgr.clear();
		attrMgr.setAttributeDefinitions(ElementAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(ErrorAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(FilterAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(JobAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(MachineAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(MessageAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(NodeAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(ProcessAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(QueueAttributes.getDefaultAttributeDefinitions());
		attrMgr.setAttributeDefinitions(ResourceManagerAttributes.getDefaultAttributeDefinitions());
	}
}
