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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.smoa.core.attrib.messages"; //$NON-NLS-1$
	public static String SMOAJobAttributes_0;
	public static String SMOAJobAttributes_Application;
	public static String SMOAJobAttributes_CustomApplication;
	public static String SMOAJobAttributes_CustomMakeCommand;
	public static String SMOAJobAttributes_JobDescription;
	public static String SMOAJobAttributes_JobNativeSpec;
	public static String SMOAJobAttributes_MaxCpus;
	public static String SMOAJobAttributes_MinCpus;
	public static String SMOAJobAttributes_PreferredMachines;
	public static String SMOAJobAttributes_QueueName;
	public static String SMOAJobAttributes_RunDirectly;
	public static String SMOAJobAttributes_SmoaUUID;
	public static String SMOANodeAttributes_CpuArch;
	public static String SMOANodeAttributes_CpuCount;
	public static String SMOANodeAttributes_PhysicalMemory;
	public static String SMOARMAttributes_AcceptsNewActvities;
	public static String SMOARMAttributes_CommonName;
	public static String SMOARMAttributes_QueueingSystem;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
