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

/**
 * LAPI Plugin constants
 */
public interface LapiIDs
{
    /** Preference page name for LAPI */
    String LAPI_INCLUDES    = Messages.LapiIDs_lapi_includes_pref_page_title;
    
    String MARKER_ID       = "org.eclipse.ptp.pldt.lapi.lapiMarker"; //$NON-NLS-1$
    String MARKER_ERROR_ID = "org.eclipse.ptp.lap.lapiErrorMarker"; //$NON-NLS-1$
    
    /** Artifact view id */
    String LAPI_VIEW_ID = "org.eclipse.ptp.pldt.lapi.views.LapiArtifactView"; //$NON-NLS-1$
}
