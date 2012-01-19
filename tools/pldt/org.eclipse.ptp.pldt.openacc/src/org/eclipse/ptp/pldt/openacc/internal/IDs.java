/**********************************************************************
 * Copyright (c) 2007, 2010, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal;

/**
 * Constants used by multiple classes in this plug-in.
 * 
 * @author unknown (IBM)
 * @author Jeff Overbey (Illinois)
 */
public interface IDs {

	/** ID for OpenACC artifact markers */
	public static final String MARKER_ID = "org.eclipse.ptp.pldt.openacc.openaccMarker"; //$NON-NLS-1$

	/** Preference store key for the OpenACC Includes preference */
	public static final String PREF_INCLUDES = "openaccIncludes"; //$NON-NLS-1$

	/** Preference store key for the OpenACC &quot;Recognize APIs by prefix alone&quot; preference */
	public static final String PREF_RECOGNIZE_APIS_BY_PREFIX_ALONE = "openaccRecognizeAPIsByPrefixAlone"; //$NON-NLS-1$
}
