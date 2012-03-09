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
import org.eclipse.ptp.internal.rdt.core.navigation.FoldingRegionsResult;

/**
 * Provides code folding regions from the remote host.
 */
public interface IRemoteCCodeFoldingService {
	/**
	 * Add what this does
	 *
	 * @param 
	 * @return 
	 */
	FoldingRegionsResult computeCodeFoldingRegions(IWorkingCopy workingCopy, int docLength, boolean fPreprocessorBranchFoldingEnabled, boolean fStatementsFoldingEnabled);
}

