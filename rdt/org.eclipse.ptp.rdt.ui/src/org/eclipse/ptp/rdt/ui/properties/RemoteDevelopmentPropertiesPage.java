/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.properties;

import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Implementation of the Remote Development property page.  Currently empty.
 * 
 * @author crecoskie
 *
 */
public class RemoteDevelopmentPropertiesPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public RemoteDevelopmentPropertiesPage() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("RemoteDevelopmentPropertiesPage_0")); //$NON-NLS-1$
		return label;
	}

}
