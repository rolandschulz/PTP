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

/**
 * SLURM Job attributes
 */
public class SLURMJobAttributes {
	private static final String JOB_NUM_NODES_ATTR_ID="jobNumNodes";//$NON-NLS-1$
	private static final String JOB_TIME_LIMIT_ATTR_ID="jobTimeLimit";//$NON-NLS-1$
	private static final String JOB_PARTITION_ATTR_ID="jobPartition";//$NON-NLS-1$
	private static final String JOB_TYPE_COMBO_ATTR_ID="jobType";//$NON-NLS-1$

	
	private final static IntegerAttributeDefinition jobNumNodesAttrDef = 
		new IntegerAttributeDefinition(JOB_NUM_NODES_ATTR_ID, "Nodes", //$NON-NLS-1$
				"Number of nodes required", true, 1);	//$NON-NLS-1$
	
	private final static IntegerAttributeDefinition jobTimelimitAttrDef = 
		new IntegerAttributeDefinition(JOB_TIME_LIMIT_ATTR_ID, "Limit", //$NON-NLS-1$
				"Job timelimit", true, 1);	//$NON-NLS-1$
	
	private final static StringAttributeDefinition jobPartitionAttrDef = 
		new StringAttributeDefinition(JOB_PARTITION_ATTR_ID, "Partition", //$NON-NLS-1$
				"Partition used to launch job", true, "");//$NON-NLS-1$
	
	private final static StringAttributeDefinition jobTypeComboAttrDef = 
		new StringAttributeDefinition(JOB_TYPE_COMBO_ATTR_ID, "Type", //$NON-NLS-1$
				"Number of processes to launch", true, "");//$NON-NLS-1$


	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				jobNumNodesAttrDef,
				jobTimelimitAttrDef,
				jobPartitionAttrDef,
				jobTypeComboAttrDef,
			};
	}

	public static IntegerAttributeDefinition getJobNumberOfNodesAttributeDefinition() {
		return jobNumNodesAttrDef;
	}

	public static IntegerAttributeDefinition getJobTimelimitAttributeDefinition() {
		return jobTimelimitAttrDef;
	}	

	public static StringAttributeDefinition getJobPartationAttributeDefinition() {
		return jobPartitionAttrDef;
	}	

	public static StringAttributeDefinition getJobTypeComboAttributeDefinition() {
		return jobTypeComboAttrDef;
	}
}
