/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Main page of the RDT wizard, which filters out local project types.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * @author crecoskie
 *
 */
public class RDTMainWizardPage extends CDTMainWizardPage {

	public RDTMainWizardPage(String pageName) {
		super(pageName);
		
		// default to view all toolchains
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOSUPP, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.IWizardItemsListListener#filterItems(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List filterItems(List items) {
		/// iterate through the list, removing entry descriptors we don't care about
		Iterator iterator = items.iterator();
		
		List<EntryDescriptor> filteredList = new LinkedList<EntryDescriptor>();
		
		while(iterator.hasNext()) {
			EntryDescriptor ed = (EntryDescriptor) iterator.next();
			if(ed.getId().startsWith("org.eclipse.ptp.rdt")) {  // both the category and the template start with this //$NON-NLS-1$
				filteredList.add(ed);
			}
		}
		
		return filteredList;
	}
	
	public void createControl(Composite parent){
		super.createControl(parent);
		Shell shell = getContainer().getShell(); //if not created on the shell, will not display properly
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, RDTHelpContextIds.CREATING_A_REMOTE_PROJECT);
	}

}
