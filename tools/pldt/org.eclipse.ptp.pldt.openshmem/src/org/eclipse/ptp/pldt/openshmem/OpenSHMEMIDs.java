/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem;

import org.eclipse.ptp.pldt.openshmem.messages.Messages;



/**
 * openshmem Plugin constants
 * 
 */
public interface OpenSHMEMIDs {
	/** Preference page name for openshmem */
	String OpenSHMEM_INCLUDES = Messages.OpenSHMEMIDs_openshmem_includes_pref_page_title;

	String MARKER_ID = "org.eclipse.ptp.pldt.openshmem.openshmemMarker"; //$NON-NLS-1$
	String MARKER_ERROR_ID = "org.eclipse.ptp.openshmem.openshmemErrorMarker"; //$NON-NLS-1$

	/** Artifact view id */
	String OpenSHMEM_VIEW_ID = "org.eclipse.ptp.pldt.openshmem.views.openshmemArtifactView"; //$NON-NLS-1$
	
	/**
	 * If we recognize OpenSHMEM APIs by prefix (openshmem_) alone, we don't need the
	 * include file location and the hassle that it causes for users to get it
	 * right.
	 * 
	 */
	public static final String OpenSHMEM_RECOGNIZE_APIS_BY_PREFIX_ALONE = "openshmemRecognizeAPIsByPrefixAlone"; //$NON-NLS-1$
}
