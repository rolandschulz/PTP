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

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rdt.ui.messages.Messages;

/**
 * @author crecoskie
 * 
 * This class is responsible for populating the project type/template selection page with a "Remote Makefile Project" entry.
 *
 */
public class RemoteMakefileWizard extends AbstractCWizard {

	private static final String ID = "org.eclipse.ptp.rdt.ui.wizards.RemoteMakefileWizard"; //$NON-NLS-1$
	private static final String NAME = Messages.getString("RemoteMakefileWizard.0"); //$NON-NLS-1$


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
		return new EntryDescriptor[] {ed};
	}

}
