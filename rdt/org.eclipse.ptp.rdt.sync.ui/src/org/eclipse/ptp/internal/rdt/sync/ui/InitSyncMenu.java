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
package org.eclipse.ptp.internal.rdt.sync.ui;

import org.eclipse.jface.action.ContributionItem;

/**
 * Force sync menu to be initialized when menu created
 */
public class InitSyncMenu extends ContributionItem {

	public InitSyncMenu() {
		// nothing to do
	}

	public InitSyncMenu(String id) {
		super(id);
	}
}
