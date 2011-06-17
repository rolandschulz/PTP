/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;

public class SyncMainWizardPage extends CDTMainWizardPage {
	private static String RDT_PROJECT_TYPE = "org.eclipse.ptp.rdt"; //$NON-NLS-1$

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 */
	public SyncMainWizardPage(String pageName) {
		super(pageName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.CDTMainWizardPage#filterItems(java.util.List)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List filterItems(List items) {
		/*
		 * Remove RDT project types as these will not work with synchronized
		 * projects
		 */
		Iterator iterator = items.iterator();

		List<EntryDescriptor> filteredList = new LinkedList<EntryDescriptor>();

		while (iterator.hasNext()) {
			EntryDescriptor ed = (EntryDescriptor) iterator.next();
			if (!ed.getId().startsWith(RDT_PROJECT_TYPE)) {
				filteredList.add(ed);
			}
		}

		return filteredList;
	}
}
