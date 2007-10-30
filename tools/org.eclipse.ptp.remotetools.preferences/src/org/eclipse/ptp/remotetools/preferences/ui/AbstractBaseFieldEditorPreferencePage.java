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
package org.eclipse.ptp.remotetools.preferences.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public abstract class AbstractBaseFieldEditorPreferencePage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public AbstractBaseFieldEditorPreferencePage(int style) {
		super(style);
	}

	public AbstractBaseFieldEditorPreferencePage(String title, int style) {
		super(title, style);

	}

	public AbstractBaseFieldEditorPreferencePage(String title,
			ImageDescriptor image, int style) {
		super(title, image, style);

	}

	protected void createFieldEditors() {


	}

	public void init(IWorkbench workbench) {

	}
	
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		
	}

}
