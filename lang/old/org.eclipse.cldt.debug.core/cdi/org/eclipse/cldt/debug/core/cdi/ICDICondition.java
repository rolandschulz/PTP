/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cldt.debug.core.cdi;

/**
 * 
 * Represents a break condition.
 * 
 * @since Jul 9, 2002
 */
public interface ICDICondition {
	/**
	 * Returns the condition expression.
	 * 
	 * @return the condition expression
	 */
	String getExpression();
	
	/**
	 * Returns the ignore count of this condition.
	 * 
	 * @return the ignore count of this condition
	 */
	int getIgnoreCount();

	/**
	 * Returns the thread Ids for this condition.
	 * 
	 * @return  the thread Ids for this condition.
	 */
	String[] getThreadIds();

	boolean equals(ICDICondition cond);
}
