/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rdt.ui.messages.Messages;

/**
 * This class is responsible for populating the project type/template selection
 * page with a "Remote Makefile Project" entry.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 * 
 */
public class RemoteMakefileWizard extends AbstractCWizard {

	public static final String ID = "org.eclipse.ptp.rdt.ui.wizards.RemoteMakefileWizard"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("RemoteMakefileWizard.0"); //$NON-NLS-1$
	public static final String EMPTY_PROJECT = Messages.getString("RemoteMakefileWizard.1"); //$NON-NLS-1$

	public RemoteMakefileWizard() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.CNewWizard#createItems(boolean, org.eclipse.jface.wizard.IWizard)
	 */
	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		RemoteMakefileWizardHandler handler = new RemoteMakefileWizardHandler(parent, wizard);
		handler.addTc(null); // add default toolchain
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (int i=0; i<tcs.length; i++)
			if (isValid(tcs[i], supportedOnly, wizard)) 
				handler.addTc(tcs[i]);
		EntryDescriptor ed = new EntryDescriptor(ID, null, NAME, true, handler, null); 
		// A default project type for that category -- not using any template.
		EntryDescriptor entryDescriptor = new EntryDescriptor(ID + ".default", ID, //$NON-NLS-1$
				EMPTY_PROJECT, false, handler, null);
		entryDescriptor.setDefaultForCategory(true);
		return new EntryDescriptor[] {ed, entryDescriptor};
	}

}
