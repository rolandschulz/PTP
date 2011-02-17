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
package org.eclipse.ptp.rm.mpi.mpich2.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.mpich2.core.messages.messages"; //$NON-NLS-1$
	public static String MPICH2ApplicationAttributes_effectiveMPICH2EnvAttrDef_description;
	public static String MPICH2ApplicationAttributes_effectiveMPICH2EnvAttrDef_title;
	public static String MPICH2ApplicationAttributes_effectiveMPICH2ProgArgsAttrDef_description;
	public static String MPICH2ApplicationAttributes_effectiveMPICH2ProgArgsAttrDef_title;
	public static String MPICH2ApplicationAttributes_effectiveMPICH2WorkingDirAttrDef_description;
	public static String MPICH2ApplicationAttributes_effectiveMPICH2WorkingDirAttrDef_title;
	public static String MPICH2JobAttributes_hostnameAttrDef_description;
	public static String MPICH2JobAttributes_hostnameAttrDef_title;
	public static String MPICH2JobAttributes_mappingModeAttrDef_description;
	public static String MPICH2JobAttributes_mappingModeAttrDef_title;
	public static String MPICH2JobAttributes_mpiJobIdAttrDef_description;
	public static String MPICH2JobAttributes_mpiJobIdAttrDef_title;
	public static String MPICH2JobAttributes_vpidRangeAttrDef_description;
	public static String MPICH2JobAttributes_vpidRangeAttrDef_title;
	public static String MPICH2JobAttributes_vpidStartAttrDef_description;
	public static String MPICH2JobAttributes_vpidStartAttrDef_title;
	public static String MPICH2LaunchAttributes_environmentArgsAttrDef_description;
	public static String MPICH2LaunchAttributes_environmentArgsAttrDef_title;
	public static String MPICH2LaunchAttributes_environmentKeyAttrDef_description;
	public static String MPICH2LaunchAttributes_environmentKeyAttrDef_title;
	public static String MPICH2LaunchAttributes_launchArgsAttrDef_description;
	public static String MPICH2LaunchAttributes_launchArgsAttrDef_title;
	public static String MPICH2MachineAttributes_statusMessageAttrDef_description;
	public static String MPICH2MachineAttributes_statusMessageAttrDef_title;
	public static String MPICH2NodeAttributes_maxNumNodesAttrDef_description;
	public static String MPICH2NodeAttributes_maxNumNodesAttrDef_title;
	public static String MPICH2NodeAttributes_numNodesAttrDef_description;
	public static String MPICH2NodeAttributes_numNodesAttrDef_title;
	public static String MPICH2NodeAttributes_oversubscribedAttrDef_description;
	public static String MPICH2NodeAttributes_oversubscribedAttrDef_title;
	public static String MPICH2NodeAttributes_statusMessageAttrDef_description;
	public static String MPICH2NodeAttributes_statusMessageAttrDef_title;
	public static String MPICH2PeriodicJob_0;
	public static String MPICH2PeriodicJob_1;
	public static String MPICH2Plugin_Exception_InternalError;

	public static String MPICH2ResourceManagerConfiguration_defaultDescription;
	public static String MPICH2ResourceManagerConfiguration_defaultName;

	public static String MPICH2DiscoverJob_defaultQueueName;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandFailed;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandFailedParseHostFile;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandFailedReadHostFile;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandHostFileEmpty;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandHostFileNotFound;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandHostFilePathNotAbsolute;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandInternalError;
	public static String MPICH2DiscoverJob_Exception_DiscoverCommandMissingHostFilePath;
	public static String MPICH2DiscoverJob_Exception_HostFileErrors;
	public static String MPICH2DiscoverJob_Exception_HostFileParseError;
	public static String MPICH2DiscoverJob_Exception_HostnameCommandFailed;
	public static String MPICH2DiscoverJob_Exception_HostnameCommandFailedParse;
	public static String MPICH2DiscoverJob_Exception_HostnameCommandFailedParseOutput;
	public static String MPICH2DiscoverJob_Exception_HostnameCommandFailedWithCode;
	public static String MPICH2DiscoverJob_Exception_IgnoredInvalidParameter;
	public static String MPICH2DiscoverJob_Exception_InvalidMaxSlotsParameter;
	public static String MPICH2DiscoverJob_Exception_InvalidSlotsParameter;
	public static String MPICH2DiscoverJob_interruptedErrorMessage;
	public static String MPICH2DiscoverJob_name;
	public static String MPICH2DiscoverJob_parsingErrorMessage;
	public static String MPICH2DiscoverJob_processErrorMessage;

	public static String MPICH2MonitorJob_interruptedErrorMessage;
	public static String MPICH2MonitorJob_name;
	public static String MPICH2MonitorJob_parsingErrorMessage;
	public static String MPICH2MonitorJob_processErrorMessage;
	public static String MPICH2MonitorJob_Exception_CommandFailed;
	public static String MPICH2MonitorJob_Exception_InternalError;

	public static String MPICH2ProcessMapText12Parser_Exception_InvalidLine;
	public static String MPICH2ProcessMapText12Parser_Exception_MissingDisplayMapInformation;
	public static String MPICH2ProcessMapText12Parser_Exception_BrokenDisplayMapInformation;

	public static String MPICH2ProcessMapXml13Parser_Exception_AttributeNotInteger;
	public static String MPICH2ProcessMapXml13Parser_Exception_MissingAttribute;
	public static String MPICH2ProcessMapXml13Parser_Exception_UnknownAttribute;
	public static String MPICH2ProcessMapXml13Parser_Exception_UnknownElement;

	public static String MPICH2RuntimeSystem_InvalidConfiguration;
	public static String MPICH2RuntimeSystem_JobName;
	public static String MPICH2RuntimeSystem_NoDefaultQueue;
	public static String MPICH2RuntimeSystemJob_Exception_FailedParse;
	public static String MPICH2RuntimeSystemJob_Exception_HostnamesDoNotMatch;
	public static String MPICH2RuntimeSystemJob_ProcessName;
	public static String MPICH2RuntimeSystemJob_Exception_ExecutionFailedWithExitValue;
	public static String MPICH2RuntimeSystemJob_Exception_ExecutionFailedWithSignal;

	public static String MPICH2LaunchConfigurationDefaults_Exception_FailedReadFile;
	public static String MPICH2LaunchConfigurationDefaults_FailedParseInteger;
	public static String MPICH2LaunchConfigurationDefaults_MissingValue;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
