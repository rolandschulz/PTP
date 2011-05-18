/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.internal;

import org.eclipse.ptp.pldt.openmp.core.messages.Messages;

/**
 * @author Beth Tibbitts
 * 
 */
public class OpenMPIDs {

	public static final String OpenMP_INCLUDES = Messages.OpenMPIDs_OpenMP_includes;
	// FIXME the following is used as a key?
	public static final String OpenMP_BUILD_CMD = "gcc -fopenmp"; //$NON-NLS-1$
	public static final String OPENMP_RECOGNIZE_APIS_BY_PREFIX_ALONE = "openmpRecognizeAPIsByPrefixAlone";//$NON-NLS-1$

	// Constants
	// preference page name for OpenMP
	public static final String OPEN_MP_INCLUDES = Messages.OpenMPPlugin_OpenMP_includes;

	public static final String MARKER_ID = "org.eclipse.ptp.pldt.openmp.core.openMPMarker"; //$NON-NLS-1$

	// artifact view id
	public static final String VIEW_ID = "org.eclipse.ptp.pldt.openmp.core.views.OpenMPArtifactView"; //$NON-NLS-1$

}
