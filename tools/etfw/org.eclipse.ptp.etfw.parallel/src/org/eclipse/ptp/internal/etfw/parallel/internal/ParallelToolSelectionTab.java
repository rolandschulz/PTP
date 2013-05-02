/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.parallel.internal;

import org.eclipse.ptp.internal.etfw.ui.ExternalToolSelectionTab;

public class ParallelToolSelectionTab extends ExternalToolSelectionTab {
	private static final String TAB_ID = "org.eclipse.ptp.etfw.parallelToolSelectionTab"; //$NON-NLS-1$

	public ParallelToolSelectionTab() {
		super(true);
	}

	@Override
	public String getId() {
		return TAB_ID;
	}
}