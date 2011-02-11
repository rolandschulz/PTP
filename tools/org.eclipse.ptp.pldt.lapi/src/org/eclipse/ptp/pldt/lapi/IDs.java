/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi;

import org.eclipse.ptp.pldt.lapi.messages.Messages;

/**
 * Constants to be used in this plug-in
 * 
 * @since 5.0
 */
public interface IDs {
	String API_NAME = "LAPI"; //$NON-NLS-1$

	/**
	 * Preference page name
	 * 
	 * @since 5.0
	 */
	String PREF_INCLUDES = Messages.Lapi_includes_pref_page_title;

	String MARKER_ID = "org.eclipse.ptp.pldt.lapi.lapiMarker"; //$NON-NLS-1$
	String MARKER_ERROR_ID = "org.eclipse.ptp.lap.lapiErrorMarker"; //$NON-NLS-1$

	/** Artifact view id */
	String VIEW_ID = "org.eclipse.ptp.pldt.lapi.views.LapiArtifactView"; //$NON-NLS-1$

	/**
	 * If we recognize LAPI APIs by prefix (LAPI_) alone, we don't need the
	 * include file location and the hassle that it causes for users to get it
	 * right.
	 * 
	 * @since 5.0
	 * 
	 */
	public static final String RECOGNIZE_APIS_BY_PREFIX_ALONE = "lapiRecognizeAPIsByPrefixAlone"; //$NON-NLS-1$
}
