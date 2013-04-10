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
package org.eclipse.ptp.rdt.sync.core.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Class for encapsulating logic to handle sync exceptions. The intent is to offer a bridge between the sync core and other
 * packages, particularly the ui, so that the caller can specify how to handle errors.
 */
public interface ISyncExceptionHandler {
	public void handle(final IProject project, final CoreException e);
}