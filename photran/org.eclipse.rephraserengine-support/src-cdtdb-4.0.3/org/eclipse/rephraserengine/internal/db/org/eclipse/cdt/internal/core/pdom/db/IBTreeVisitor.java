/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * The visitor visits all records where compare returns 0. 
 */
public interface IBTreeVisitor {

	/**
	 * Compare the record against an internally held key. The comparison must be
	 * compatible with the one used for the btree.
	 * Used for visiting.
	 * 
	 * @param record
	 * @return -1 if record < key, 0 if record == key, 1 if record > key
	 * @throws IOException
	 */
	public abstract int compare(int record) throws CoreException;

	/**
	 * Visit a given record and return whether to continue or not.

	 * @return <code>true</code> to continue the visit, <code>false</code> to abort it.
	 * @throws IOException
	 */
	public abstract boolean visit(int record) throws CoreException;
	
}
