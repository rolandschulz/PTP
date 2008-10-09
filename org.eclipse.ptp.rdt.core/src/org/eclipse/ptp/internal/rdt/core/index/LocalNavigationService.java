/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CHContentProvider
 * Version: 1.17
 */
package org.eclipse.ptp.internal.rdt.core.index;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.model.LocalCProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public class LocalNavigationService extends AbstractNavigationService {
	@Override
	public ICElement findElement(Scope scope, ICElement input) throws CoreException, InterruptedException {
		IIndex index= CCorePlugin.getIndexManager().getIndex(input.getCProject());
		index.acquireReadLock();
		try {
			if (!IndexQueries.isIndexed(index, input)) {
				return null;
			} 
			else {
				return IndexQueries.attemptConvertionToHandle(index, input, null, new LocalCProjectFactory());
			}
		}
		finally {
			index.releaseReadLock();
		}
	}
}
