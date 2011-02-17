/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.slurm.core;

/**
 * @since 5.0
 */
public class SLURMLaunchConfiguration {
	private static final String ATTR_PREFIX = SLURMCorePlugin.getUniqueIdentifier() + ".launchAttributes"; //$NON-NLS-1$

	public static final String ATTR_NUMPROCS = ATTR_PREFIX + ".numProcs"; //$NON-NLS-1$
	public static final String ATTR_NUMNODES = ATTR_PREFIX + ".numNodes"; //$NON-NLS-1$
	public static final String ATTR_TIMELIMIT = ATTR_PREFIX + ".timeLimit"; //$NON-NLS-1$
	public static final String ATTR_JOBPARTITION = ATTR_PREFIX + ".jobPartition"; //$NON-NLS-1$
	public static final String ATTR_JOBREQNODELIST = ATTR_PREFIX + ".jobReqNodeList"; //$NON-NLS-1$
	public static final String ATTR_JOBEXCNODELIST = ATTR_PREFIX + ".jobExcNodeList"; //$NON-NLS-1$

}
