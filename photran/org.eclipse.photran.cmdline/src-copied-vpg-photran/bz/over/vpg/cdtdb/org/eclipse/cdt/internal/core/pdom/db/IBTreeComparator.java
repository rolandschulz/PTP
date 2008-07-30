/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package bz.over.vpg.cdtdb.org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public interface IBTreeComparator {

	/**
	 * Compare two records. Used for insert.
	 * 
	 * @param record1
	 * @param record2
	 * @return
	 * @throws IOException
	 */
	public abstract int compare(int record1, int record2) throws CoreException;
	
}
