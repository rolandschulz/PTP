/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.wizard;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public abstract class AbstractEnvironmentDialogPage extends WizardPage {

	public AbstractEnvironmentDialogPage(String pageName) {
		super(pageName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	abstract public void createControl(Composite parent);

	final public boolean canFinish() {
		String name = getName();
		if (name != null) {
			if (!name.equals("")) {  //$NON-NLS-1$
				return isValid();
			}

		}
		return false;
	}

	/**
	 * Should return the configuration's map derived from populating this own
	 * page. null is not allowed.
	 * 
	 * @return the configuration attributes map
	 */
	abstract public Map getAttributes();

	/**
	 * Provides this configuration instance key name.
	 * 
	 * @returns the configuration's name
	 */
	abstract public String getName();

	/**
	 * Returns weather this current page information represents a valid state of
	 * configuration.
	 * 
	 * @return true if valid, false otherwise.
	 */
	abstract public boolean isValid();

}
