/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.alf.ui.core;

/**
 * ALFConstants defines static variables that can be used by other classes to compare values
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFConstants {
	
	// definition of partition methods
	public static int ALF_PARTITION_HOST = 0;
	public static int ALF_PARTITION_ACCELERATOR = 1;
	
	/* constants to be used in indexing the distribution model combos */
	public static final int DIST_MODEL_STAR = 0;
	public static final int DIST_MODEL_BLOCK = 1;
	public static final int DIST_MODEL_CYCLIC = 2;
	
	/* constants to be used in indexing the number of dimensions combo */
	public static final int ONE_DIMENSIONAL = 0;
	public static final int TWO_DIMENSIONAL = 1;
	public static final int THREE_DIMENSIONAL = 2;
	
	/* constants to be used for determining the type of buffer */
	public static final int ALF_BUFFER_INPUT = 0; 
	public static final int ALF_BUFFER_OUTPUT = 1;
	
	/* constants to be used for determining the selected configuration */
	public static final int CONFIG_GNU_32 = 0;
	public static final int CONFIG_GNU_64 = 1;
	
	/* constants to be used for determining the element unit */
	public static final int ALF_DATA_BYTE = 0;
	public static final int ALF_DATA_INT16 = 1;
	public static final int ALF_DATA_INT32 = 2;
	public static final int ALF_DATA_INT64 = 3;
	public static final int ALF_DATA_FLOAT = 4;
	public static final int ALF_DATA_DOUBLE = 5;
	public static final int ALF_DATA_ADDR32 = 6;
	public static final int ALF_DATA_ADDR64 = 7;
	public static final int ALF_DATA_ELEMENT_TYPE = 8;
}
