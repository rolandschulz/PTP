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

/**
 * SLURM node status
 */

public class SLURMMPINodeAttributes {
	public enum Status {
		IDLE, ALLOCATED, DOWN, UNKNOWN,
		/**
		 * @since 4.0
		 */
		MIXED,
		/**
		 * @since 4.0
		 */
		FUTURE,
		/**
		 * @since 4.0
		 */
		ERROR
	}
}
