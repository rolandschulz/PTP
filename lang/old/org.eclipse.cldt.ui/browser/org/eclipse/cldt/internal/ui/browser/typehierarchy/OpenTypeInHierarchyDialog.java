/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.browser.typehierarchy;

import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to select a type from a list of types. The selected type will be
 * opened in the editor.
 */
public class OpenTypeInHierarchyDialog extends TypeSelectionDialog {

	private static final String DIALOG_SETTINGS= OpenTypeInHierarchyDialog.class.getName();
	private final int[] VISIBLE_TYPES = { ICElement.C_CLASS, ICElement.C_STRUCT };

	/**
	 * Constructs an instance of <code>OpenTypeDialog</code>.
	 * @param parent  the parent shell.
	 */
	public OpenTypeInHierarchyDialog(Shell parent) {
		super(parent);
		setTitle(TypeHierarchyMessages.getString("OpenTypeInHierarchyDialog.title")); //$NON-NLS-1$
		setMessage(TypeHierarchyMessages.getString("OpenTypeInHierarchyDialog.message")); //$NON-NLS-1$
		setVisibleTypes(VISIBLE_TYPES);
		setDialogSettings(DIALOG_SETTINGS);
	}
}