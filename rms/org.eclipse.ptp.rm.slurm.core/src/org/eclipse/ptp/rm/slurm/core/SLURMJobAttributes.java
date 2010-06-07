/*******************************************************************************
 * Copyright (c) 2009 School of Computer Science,
 * National University of Defense Technology, P.R.China.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Jie Jiang, National University of Defense Technology
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.slurm.core.messages.Messages;

/**
 * SLURM Job attributes
 */
public class SLURMJobAttributes {
	private static final String JOB_NUM_PROCS_ATTR_ID = "jobNumProcs";//$NON-NLS-1$
	private static final String JOB_NUM_NODES_ATTR_ID = "jobNumNodes";//$NON-NLS-1$
	private static final String JOB_TIME_LIMIT_ATTR_ID = "jobTimeLimit";//$NON-NLS-1$
	private static final String JOB_PARTITION_ATTR_ID = "jobPartition";//$NON-NLS-1$
	private static final String JOB_REQ_NODELIST_ATTR_ID = "jobReqList";//$NON-NLS-1$
	private static final String JOB_EXC_NODELIST_ATTR_ID = "jobExcList";//$NON-NLS-1$

	private final static IntegerAttributeDefinition jobNumProcsAttrDef = new IntegerAttributeDefinition(JOB_NUM_PROCS_ATTR_ID,
			Messages.SLURMJobAttributes_0, 
			Messages.SLURMJobAttributes_1, true, 1); 

	private final static IntegerAttributeDefinition jobNumNodesAttrDef = new IntegerAttributeDefinition(JOB_NUM_NODES_ATTR_ID,
			Messages.SLURMJobAttributes_2, 
			Messages.SLURMJobAttributes_3, true, 1); 

	private final static IntegerAttributeDefinition jobTimelimitAttrDef = new IntegerAttributeDefinition(JOB_TIME_LIMIT_ATTR_ID,
			Messages.SLURMJobAttributes_4, 
			Messages.SLURMJobAttributes_5, true, 5); 

	private final static StringAttributeDefinition jobPartitionAttrDef = new StringAttributeDefinition(JOB_PARTITION_ATTR_ID,
			Messages.SLURMJobAttributes_6, 
			Messages.SLURMJobAttributes_7, true, ""); //$NON-NLS-2$

	private final static StringAttributeDefinition jobReqNodeListAttrDef = new StringAttributeDefinition(JOB_REQ_NODELIST_ATTR_ID,
			Messages.SLURMJobAttributes_8, 
			Messages.SLURMJobAttributes_9, true, ""); //$NON-NLS-2$

	private final static StringAttributeDefinition jobExcNodeListAttrDef = new StringAttributeDefinition(JOB_EXC_NODELIST_ATTR_ID,
			Messages.SLURMJobAttributes_10, 
			Messages.SLURMJobAttributes_11, true, ""); //$NON-NLS-2$

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { jobNumProcsAttrDef, jobNumNodesAttrDef, jobTimelimitAttrDef, jobPartitionAttrDef,
				jobReqNodeListAttrDef, jobExcNodeListAttrDef, };
	}

	/**
	 * @since 4.0
	 */
	public static IntegerAttributeDefinition getJobNumberOfProcsAttributeDefinition() {
		return jobNumProcsAttrDef;
	}

	public static IntegerAttributeDefinition getJobNumberOfNodesAttributeDefinition() {
		return jobNumNodesAttrDef;
	}

	public static IntegerAttributeDefinition getJobTimelimitAttributeDefinition() {
		return jobTimelimitAttrDef;
	}

	/**
	 * @since 4.0
	 */
	public static StringAttributeDefinition getJobPartitionAttributeDefinition() {
		return jobPartitionAttrDef;
	}

	/**
	 * @since 4.0
	 */
	public static StringAttributeDefinition getJobReqNodeListAttributeDefinition() {
		return jobReqNodeListAttrDef;
	}

	/**
	 * @since 4.0
	 */
	public static StringAttributeDefinition getJobExcNodeListAttributeDefinition() {
		return jobExcNodeListAttrDef;
	}
}
