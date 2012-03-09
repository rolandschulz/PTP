/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.core.model.IWorkingCopy;

/**
 * Provides Semantic Highlighting added and removed positions from the remote host.
 */
public interface IRemoteSemanticHighlightingService {
	/**
	 * Returns a list of added and removed positions needed by the Presenter.
	 *
	 * @param working copy of the file
	 * @return A comma separated list of highlighting positions where Element x = the offset,
	 * x+1 = the length, x+2 is the Highlightings index.
	 */
	String computeSemanticHighlightingPositions(IWorkingCopy workingCopy);
}
