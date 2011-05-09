/*******************************************************************************
 * Copyright (c) 2006,2010 IBM Corp. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.ui.pv.views;

/**
 * IDs for OpenMP problem markers
 */
public class ProblemMarkerAttrIds
{

	/** identifies the OpenMP Error marker */
	public static final String MARKER_ERROR_ID = "org.eclipse.ptp.pldt.openmp.ui.pv.openMPProblemMarker"; //$NON-NLS-1$

	/** Marker id for storage of additional information about the artifact */
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	/** Marker ID for storage of the filename in which the artifact is found */
	public static final String RESOURCE = "resource"; //$NON-NLS-1$
	/** Marker ID for folder in which artifact is found */
	public static final String INFOLDER = "infoldername"; //$NON-NLS-1$
	/** Marker ID for location (line number) in which the artifact is found */
	public static final String LOCATION = "location"; //$NON-NLS-1$
	/** Marker ID that holds the problem object */
	public static final String PROBLEMOBJECT = "problem"; //$NON-NLS-1$

}
