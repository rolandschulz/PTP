/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractCOptionPage extends DialogPage implements ICOptionPage {

	private boolean bIsValid = true;
	private ICOptionContainer fContainer;
	

	protected AbstractCOptionPage() {
		super();
	}

	protected AbstractCOptionPage(String title) {
		super(title);
	}

	protected AbstractCOptionPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void setContainer(ICOptionContainer container) {
		fContainer = container;
	}
	
	protected ICOptionContainer getContainer() {
		return fContainer;
	}

	protected void setValid(boolean isValid) {
		bIsValid = isValid;
	}
	
	public boolean isValid() {
		return bIsValid;
	}

	public abstract void performApply(IProgressMonitor monitor) throws CoreException;

	public abstract void performDefaults();
	
	public abstract void createControl(Composite parent);

}
