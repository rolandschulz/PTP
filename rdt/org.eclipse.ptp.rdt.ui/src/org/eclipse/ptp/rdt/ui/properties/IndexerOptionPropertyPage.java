/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.properties;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.internal.rdt.ui.preferences.IndexerBlock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class IndexerOptionPropertyPage extends PropertyPage implements ICOptionContainer {

	private IndexerBlock optionPage;

	public IndexerOptionPropertyPage(){
		super();
		optionPage = new IndexerBlock();
		optionPage.setContainer(this);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		optionPage.createControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_INDEXER_PROPERTIES);	
		
		return composite;
	}

	@Override
	protected void performDefaults() {
		optionPage.performDefaults();
	}
		
	@Override
	public boolean performOk() {
		try {
			optionPage.performApply(new NullProgressMonitor());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return true;
	}
	
	public IProject getProject(){
		IProject project= null;
		IAdaptable elem = getElement();
		if (elem instanceof IProject) {
			project= (IProject) elem;
		} else if (elem != null) {
			project= (IProject) elem.getAdapter(IProject.class);
		}
		return project;
	}

	public Preferences getPreferences() {
		throw new UnsupportedOperationException();
	}

	public void updateContainer() {
	}
	
}
