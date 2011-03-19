/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract class to subclass contributed WizardPages to create new Resource
 * Managers
 * 
 * @author rsqrd
 * 
 */
public abstract class RMConfigurationWizardPage extends WizardPage {

	private final IRMConfigurationWizard wizard;
	private IResourceManagerComponentConfiguration fConfiguration;

	/**
	 * @param wizard
	 * @param pageName
	 */
	public RMConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(pageName);
		this.wizard = wizard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public abstract void createControl(Composite parent);

	/**
	 * @return
	 */
	protected IRMConfigurationWizard getConfigurationWizard() {
		return wizard;
	}

	/**
	 * @since 5.0
	 */
	protected IResourceManagerComponentConfiguration getConfiguration() {
		return fConfiguration;
	}

	/**
	 * @since 5.0
	 */
	protected void setConfiguration(IResourceManagerComponentConfiguration config) {
		fConfiguration = config;
	}
}
