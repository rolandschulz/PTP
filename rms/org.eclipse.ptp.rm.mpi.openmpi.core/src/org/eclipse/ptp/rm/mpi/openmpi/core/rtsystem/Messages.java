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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.messages"; //$NON-NLS-1$
	public static String OpenMPIDiscoverJob_defaultQueueName;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandFailed;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandFailedParseHostFile;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandFailedReadHostFile;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandHostFileEmpty;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandHostFileNotFound;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandHostFilePathNotAbsolute;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandInternalError;
	public static String OpenMPIDiscoverJob_Exception_DiscoverCommandMissingHostFilePath;
	public static String OpenMPIDiscoverJob_Exception_HostFileErrors;
	public static String OpenMPIDiscoverJob_Exception_HostFileParseError;
	public static String OpenMPIDiscoverJob_Exception_HostnameCommandFailed;
	public static String OpenMPIDiscoverJob_Exception_HostnameCommandFailedParse;
	public static String OpenMPIDiscoverJob_Exception_HostnameCommandFailedParseOutput;
	public static String OpenMPIDiscoverJob_Exception_HostnameCommandFailedWithCode;
	public static String OpenMPIDiscoverJob_Exception_IgnoredInvalidParameter;
	public static String OpenMPIDiscoverJob_Exception_InvalidMaxSlotsParameter;
	public static String OpenMPIDiscoverJob_Exception_InvalidSlotsParameter;
	public static String OpenMPIDiscoverJob_interruptedErrorMessage;
	public static String OpenMPIDiscoverJob_name;
	public static String OpenMPIDiscoverJob_parsingErrorMessage;
	public static String OpenMPIDiscoverJob_processErrorMessage;
	public static String OpenMPIProcessMapText12Parser_Exception_InvalidLine;
	public static String OpenMPIProcessMapText12Parser_Exception_MissingDisplayMapInformation;
	public static String OpenMPIProcessMapText12Parser_Exception_BrokenDisplayMapInformation;
	public static String OpenMPIProcessMapXml13Parser_Exception_AttributeNotInteger;
	public static String OpenMPIProcessMapXml13Parser_Exception_MissingAttribute;
	public static String OpenMPIProcessMapXml13Parser_Exception_UnknownAttribute;
	public static String OpenMPIProcessMapXml13Parser_Exception_UnknownElement;
	public static String OpenMPIRuntimeSystem_JobName;
	public static String OpenMPIRuntimeSystemJob_Exception_FailedParse;
	public static String OpenMPIRuntimeSystemJob_Exception_HostnamesDoNotMatch;
	public static String OpenMPIRuntimeSystemJob_ProcessName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
