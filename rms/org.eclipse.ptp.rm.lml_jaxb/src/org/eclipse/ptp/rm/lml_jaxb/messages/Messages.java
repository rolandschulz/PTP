/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml_jaxb.messages;

import org.eclipse.osgi.util.NLS;

/**
 * @author arossi
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.lml_jaxb.messages.messages"; //$NON-NLS-1$
	public static String LMLJAXBRMLaunchConfigurationFactory_wrongRMType;
	public static String LMLJAXBRMLaunchConfigurationFactory_doCreateError;

	public static String RemoveJobWarning;
	public static String CannotUndoOperation;

	public static String RefreshJobStatusError;
	public static String RefreshJobStatus;
	public static String ReadOutputFile;
	public static String ReadOutputFileError;
	public static String CancelJob;
	public static String CancelJobError;
	public static String CachedDefinitionFile;
	public static String RemoveFiles;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
