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

package org.eclipse.ptp.rm.smoa.core.attrib;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;

/**
 * Keeps attributes specific for SMOA Jobs.
 * 
 * Also keeps environmental variables
 */
public class SMOAJobAttributes extends AttributeManager {

	/** Variable name requesting to run 'make' before starting task */
	public static final String ENV_IF_MAKE = "SMOA_COMP_PTP_RUN_MAKE"; //$NON-NLS-1$
	/** Variable name requesting to run 'make' before starting task */
	public static final String ENV_MAKE_COMMAND = "SMOA_COMP_PTP_MAKE_COMMAND"; //$NON-NLS-1$
	/** Environment variable containing file name for stdout */
	public static final String ENV_STDOUT = "SMOA_COMP_PTP_STDOUT"; //$NON-NLS-1$
	/** Environment variable containing file name for stderr */
	public static final String ENV_STDERR = "SMOA_COMP_PTP_STDERR"; //$NON-NLS-1$
	/** Environment variable containing for all processes their node names */
	public static final String ENV_MACHINEFILE = "SMOA_COMP_PTP_MACHINEFILE"; //$NON-NLS-1$

	/** Text displayed in GUI launch configuration for no wrapper script */
	public static final String NO_WRAPPER_SCRIPT = Messages.SMOAJobAttributes_RunDirectly;

	public final static StringAttributeDefinition getAppNameDef() {
		return new StringAttributeDefinition("applicationName", Messages.SMOAJobAttributes_Application, //$NON-NLS-1$
				Messages.SMOAJobAttributes_Application, true, Messages.SMOAJobAttributes_CustomApplication);
	}

	public final static StringAttributeDefinition getCustomMakeCommandDef() {
		return new StringAttributeDefinition("customMakeCommand", //$NON-NLS-1$
				Messages.SMOAJobAttributes_CustomMakeCommand, Messages.SMOAJobAttributes_CustomMakeCommand, true, null);
	}

	public final static StringAttributeDefinition getDescDef() {
		return new StringAttributeDefinition("job_desc", Messages.SMOAJobAttributes_JobDescription, //$NON-NLS-1$
				Messages.SMOAJobAttributes_JobDescription, true, ""); //$NON-NLS-1$
	}

	public final static StringMapAttributeDefinition getEnvDef() {
		return new StringMapAttributeDefinition("org.eclipse.debug.core.environmentVariables", //$NON-NLS-1$
				"Enviornment vars", "Enviornment vars", false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public final static BooleanAttributeDefinition getIfCustomMakeDef() {
		return new BooleanAttributeDefinition("ifCustomMakeCommand", //$NON-NLS-1$
				"ifCustomMakeCommand", "ifCustomMakeCommand", false, false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns set of possible SMOA Activity attributes.
	 * 
	 * Used in LaunchConfiguration for filtering attributes.
	 */
	public static Map<String, IAttributeDefinition<?, ?, ?>> getLaunchAttributes() {
		final Map<String, IAttributeDefinition<?, ?, ?>> map = new TreeMap<String, IAttributeDefinition<?, ?, ?>>();

		putFromDef(map, ElementAttributes.getNameAttributeDefinition());

		putFromDef(map, JobAttributes.getWorkingDirectoryAttributeDefinition());
		putFromDef(map, JobAttributes.getQueueIdAttributeDefinition());

		putFromDef(map, JobAttributes.getDebugFlagAttributeDefinition());
		putFromDef(map, JobAttributes.getDebuggerArgumentsAttributeDefinition());
		putFromDef(map,
				JobAttributes.getDebuggerExecutableNameAttributeDefinition());
		putFromDef(map,
				JobAttributes.getDebuggerExecutablePathAttributeDefinition());
		putFromDef(map, JobAttributes.getDebuggerIdAttributeDefinition());
		putFromDef(map,
				JobAttributes.getDebuggerStopInMainFlagAttributeDefinition());

		putFromDef(map, getMinCpuDef());
		putFromDef(map, getMaxCpuDef());
		putFromDef(map, getDescDef());
		putFromDef(map, getNativeSpecDef());
		putFromDef(map, getPrefferedDef());
		putFromDef(map, getEnvDef());
		putFromDef(map, getAppNameDef());
		putFromDef(map, getMakeDef());
		putFromDef(map, getIfCustomMakeDef());
		putFromDef(map, getCustomMakeCommandDef());
		putFromDef(map, getQueueNameDef());

		return map;
	}

	public final static BooleanAttributeDefinition getMakeDef() {
		return new BooleanAttributeDefinition("runMake", Messages.SMOAJobAttributes_0, "runMake", //$NON-NLS-1$ //$NON-NLS-2$
				false, false);
	}

	public final static IntegerAttributeDefinition getMaxCpuDef() {
		return new IntegerAttributeDefinition("cpu_max", Messages.SMOAJobAttributes_MaxCpus, //$NON-NLS-1$
				Messages.SMOAJobAttributes_MaxCpus, true, 1);
	}

	public final static IntegerAttributeDefinition getMinCpuDef() {
		return new IntegerAttributeDefinition("cpu_min", Messages.SMOAJobAttributes_MinCpus, //$NON-NLS-1$
				Messages.SMOAJobAttributes_MinCpus, true, 1);
	}

	public final static StringAttributeDefinition getNativeSpecDef() {
		return new StringAttributeDefinition("job_native_spec", //$NON-NLS-1$
				Messages.SMOAJobAttributes_JobNativeSpec, Messages.SMOAJobAttributes_JobNativeSpec, true, ""); //$NON-NLS-1$
	}

	public final static ArrayAttributeDefinition<String> getPrefferedDef() {
		return new ArrayAttributeDefinition<String>("preffered_machines", //$NON-NLS-1$
				Messages.SMOAJobAttributes_PreferredMachines, Messages.SMOAJobAttributes_PreferredMachines, true, new String[0]);
	}

	public final static StringAttributeDefinition getQueueNameDef() {
		return new StringAttributeDefinition("queue_name", Messages.SMOAJobAttributes_QueueName, //$NON-NLS-1$
				Messages.SMOAJobAttributes_QueueName, true, null);
	}

	public final static StringAttributeDefinition getSmoaUuidDef() {
		return new StringAttributeDefinition("smoa_uid", Messages.SMOAJobAttributes_SmoaUUID, //$NON-NLS-1$
				Messages.SMOAJobAttributes_SmoaUUID, true, null);
	}

	private static void putFromDef(
			Map<String, IAttributeDefinition<?, ?, ?>> map,
			IAttributeDefinition<?, ?, ?> def) {
		map.put(def.getId(), def);
	}

	public SMOAJobAttributes() {
		addAttribute(JobAttributes.getJobIdAttributeDefinition().create());
		addAttribute(JobAttributes.getStateAttributeDefinition().create());
		addAttribute(JobAttributes.getUserIdAttributeDefinition().create());
		addAttribute(ElementAttributes.getNameAttributeDefinition().create());
	}
}
