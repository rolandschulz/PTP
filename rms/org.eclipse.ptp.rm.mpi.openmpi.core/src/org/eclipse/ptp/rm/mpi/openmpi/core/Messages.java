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
package org.eclipse.ptp.rm.mpi.openmpi.core;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.openmpi.core.messages"; //$NON-NLS-1$
	public static String OpenMPI12Defaults_Exception_FailedReadFile;
	public static String OpenMPI12Defaults_FailedParseInteger;
	public static String OpenMPI12Defaults_MissingValue;
	public static String OpenMPI13Defaults_Exception_FailedReadFile;
	public static String OpenMPI13Defaults_FailedParseInteger;
	public static String OpenMPI13Defaults_MissingValue;
	public static String OpenMPIApplicationAttributes_effectiveOpenMPIEnvAttrDef_description;
	public static String OpenMPIApplicationAttributes_effectiveOpenMPIEnvAttrDef_title;
	public static String OpenMPIApplicationAttributes_effectiveOpenMPIProgArgsAttrDef_description;
	public static String OpenMPIApplicationAttributes_effectiveOpenMPIProgArgsAttrDef_title;
	public static String OpenMPIApplicationAttributes_effectiveOpenMPIWorkingDirAttrDef_description;
	public static String OpenMPIApplicationAttributes_effectiveOpenMPIWorkingDirAttrDef_title;
	public static String OpenMPIJobAttributes_hostnameAttrDef_description;
	public static String OpenMPIJobAttributes_hostnameAttrDef_title;
	public static String OpenMPIJobAttributes_mappingModeAttrDef_description;
	public static String OpenMPIJobAttributes_mappingModeAttrDef_title;
	public static String OpenMPIJobAttributes_mpiJobIdAttrDef_description;
	public static String OpenMPIJobAttributes_mpiJobIdAttrDef_title;
	public static String OpenMPIJobAttributes_vpidRangeAttrDef_description;
	public static String OpenMPIJobAttributes_vpidRangeAttrDef_title;
	public static String OpenMPIJobAttributes_vpidStartAttrDef_description;
	public static String OpenMPIJobAttributes_vpidStartAttrDef_title;
	public static String OpenMPILaunchAttributes_environmentArgsAttrDef_description;
	public static String OpenMPILaunchAttributes_environmentArgsAttrDef_title;
	public static String OpenMPILaunchAttributes_environmentKeyAttrDef_description;
	public static String OpenMPILaunchAttributes_environmentKeyAttrDef_title;
	public static String OpenMPILaunchAttributes_launchArgsAttrDef_description;
	public static String OpenMPILaunchAttributes_launchArgsAttrDef_title;
	public static String OpenMPIMachineAttributes_statusMessageAttrDef_description;
	public static String OpenMPIMachineAttributes_statusMessageAttrDef_title;
	public static String OpenMPINodeAttributes_maxNumNodesAttrDef_description;
	public static String OpenMPINodeAttributes_maxNumNodesAttrDef_title;
	public static String OpenMPINodeAttributes_numNodesAttrDef_description;
	public static String OpenMPINodeAttributes_numNodesAttrDef_title;
	public static String OpenMPINodeAttributes_oversubscribedAttrDef_description;
	public static String OpenMPINodeAttributes_oversubscribedAttrDef_title;
	public static String OpenMPINodeAttributes_statusMessageAttrDef_description;
	public static String OpenMPINodeAttributes_statusMessageAttrDef_title;
	public static String OpenMPIPlugin_Exception_InternalError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
