/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core;

/**
 * A simple filtering interface intended for sync providers to pass to the underlying sync tool to indicate files that should not
 * by sync'ed. This may include Eclipse files, like .project, binary files, or files indicated by the user. 
 */
public interface SyncFileFilter {
	public boolean shouldIgnore(String fileName);
}
