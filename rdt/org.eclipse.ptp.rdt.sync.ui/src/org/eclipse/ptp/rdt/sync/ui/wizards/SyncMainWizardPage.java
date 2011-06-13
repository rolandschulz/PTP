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
	private static String MAKEFILE_PROJECT_TYPE = "org.eclipse.cdt.build.makefile.projectType"; //$NON-NLS-1$

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
		 * Limit visible project types to makefile projects for now. Needs to be
		 * reviewed when managed projects are supported
		 */
		Iterator iterator = items.iterator();

		List<EntryDescriptor> filteredList = new LinkedList<EntryDescriptor>();

		while (iterator.hasNext()) {
			EntryDescriptor ed = (EntryDescriptor) iterator.next();
			String pid = ed.getParentId();
			if (ed.getId().startsWith(MAKEFILE_PROJECT_TYPE) || (pid != null && pid.startsWith(MAKEFILE_PROJECT_TYPE))) {
				filteredList.add(ed);
			}
		}

		return filteredList;
	}
}
