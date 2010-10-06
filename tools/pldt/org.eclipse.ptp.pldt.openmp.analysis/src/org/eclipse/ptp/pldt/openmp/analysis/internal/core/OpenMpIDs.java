/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.internal.core;

/**
 * IDs e.g. for markers
 * 
 * @author tibbitts
 * 
 */
public class OpenMpIDs {
	/**
	 * These are the marker type used for showing concurrency
	 */

	public static final String ConcurrencyType = "org.eclipse.ptp.pldt.openmp.analysis.concurrency";
	/**
	 * These are the marker type used for showing non-concurrency
	 */
	public static final String NonConcurrencyType = "org.eclipse.ptp.pldt.openmp.analysis.nonconcurrency";
	
	public static final String OPENMP_RECOGNIZE_APIS_BY_PREFIX_ALONE = "openmpRecognizeAPIsByPrefixAlone";

}
