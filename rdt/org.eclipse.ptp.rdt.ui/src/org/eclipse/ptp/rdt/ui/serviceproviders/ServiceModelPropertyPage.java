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

package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.rdt.ui.wizards.ServiceModelWidget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

public class ServiceModelPropertyPage extends PropertyPage{
	
	ServiceModelWidget fModelWidget;
	
	public ServiceModelPropertyPage() {
		fModelWidget = new ServiceModelWidget();
	}

	@Override
	protected Control createContents(Composite parent) {
		return fModelWidget.createContents(parent);
	}
}
