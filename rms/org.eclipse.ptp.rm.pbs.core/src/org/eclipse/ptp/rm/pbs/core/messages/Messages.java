/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *  IBM Corporation - initial API and implementation
 *	Albert L. Rossi (NCSA) - Updated attributes (bug 310189)
 * 				 			Updated attributes 04/30/2010
 * 						    Updated attributes 05/11/2010
 *    Benjamin Lindner (ben@benlabs.net) - Attribute Definitions and Mapping (bug 316671)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * NLS definitions for attributes.
 * 
 * @author arossi
 */
public class Messages extends NLS {
	public static String PBSProxyRuntimeClient_0;
	public static String PBSProxyRuntimeClient_1;
	public static String PBSProxyRuntimeClient_2;
	public static String PBSProxyRuntimeClient_3;
	public static String PBSProxyRuntimeClient_4;
	public static String PBSProxyRuntimeClient_5;
	public static String PBSProxyRuntimeClient_6;
	public static String PBSProxyRuntimeClient_7;
	public static String PBSAttributeNotFound;
	public static String PBSBatchScriptTemplate_parseError;
	public static String PBSBatchScriptTemplateManager_zerostringError;
	public static String PBSBatchScriptTemplateManager_removeError;
	public static String PBSBatchScriptTemplateManager_illegalArgument;
	public static String PBSBatchScriptTemplateManager_storeError;
	public static String PBSResourceManagerConfiguration_PBSResourceManager;

	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.pbs.core.messages.messages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
