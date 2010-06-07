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
 * SLURM Node attributes
 */
public class SLURMNodeAttributes {
	private static final String SOCKNUMBER_ATTR_ID = "sockNumber"; //$NON-NLS-1$
	private static final String CORENUMBER_ATTR_ID = "coreNumber";//$NON-NLS-1$
	private static final String THREADNUMBER_ATTR_ID = "threadNumber";//$NON-NLS-1$
	private static final String CPUARCH_ATTR_ID = "cpuArch";//$NON-NLS-1$
	private static final String OS_ATTR_ID = "OS";//$NON-NLS-1$
	
	
	private final static IntegerAttributeDefinition sockAttrDef = 
		new IntegerAttributeDefinition(SOCKNUMBER_ATTR_ID, Messages.SLURMNodeAttributes_2, 
				Messages.SLURMNodeAttributes_3, true, 0);
	
	private final static IntegerAttributeDefinition coreAttrDef = 
		new IntegerAttributeDefinition(CORENUMBER_ATTR_ID, Messages.SLURMNodeAttributes_4, 
				Messages.SLURMNodeAttributes_5, true, 0);
		
	private final static IntegerAttributeDefinition threadAttrDef = 
		new IntegerAttributeDefinition(THREADNUMBER_ATTR_ID, Messages.SLURMNodeAttributes_6, 
				Messages.SLURMNodeAttributes_7, true, 0);

	private final static StringAttributeDefinition cpuAttrDef = 
		new StringAttributeDefinition(CPUARCH_ATTR_ID, Messages.SLURMNodeAttributes_8, 
				Messages.SLURMNodeAttributes_9, true, ""); //$NON-NLS-2$
	
	private final static StringAttributeDefinition osAttrDef = 
		new StringAttributeDefinition(OS_ATTR_ID, Messages.SLURMNodeAttributes_0, 
				Messages.SLURMNodeAttributes_1, true, ""); //$NON-NLS-2$
	
	public static IntegerAttributeDefinition getSockNumberAttributeDefinition() {
		return sockAttrDef;
	}
		
	public static IntegerAttributeDefinition getCoreNumberAttributeDefinition() {
		return coreAttrDef;
	}
	
	public static IntegerAttributeDefinition getThreadNumberAttributeDefinition() {
		return threadAttrDef;
	}

	public static StringAttributeDefinition getCpuArchAttributeDefinition() {
		return cpuAttrDef;
	}
	
	public static StringAttributeDefinition getOSAttributeDefinition() {
		return osAttrDef;
	}
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{sockAttrDef,coreAttrDef,threadAttrDef,cpuAttrDef,osAttrDef};
	}
}
