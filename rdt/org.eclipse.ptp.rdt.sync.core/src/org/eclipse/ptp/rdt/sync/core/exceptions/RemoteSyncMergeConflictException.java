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
package org.eclipse.ptp.rdt.sync.core.exceptions;

import org.eclipse.core.runtime.Status;

/**
 * A RemoteSyncException especially for merge conflicts. Since it is a subclass, clients only need to catch this exception if
 * they want to handle merge conflicts in a different way.
 * 
 * Later, this exception could add API with information about the merge conflict.
 * 
 * @since 3.0
 */
public class RemoteSyncMergeConflictException extends RemoteSyncException {
	private static final long serialVersionUID = 1L;

	public RemoteSyncMergeConflictException(Status status) {
		super(status);
	}

	public RemoteSyncMergeConflictException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RemoteSyncMergeConflictException(String arg0) {
		super(arg0);
	}

	public RemoteSyncMergeConflictException(Throwable arg0) {
		super(arg0);
	}
}
