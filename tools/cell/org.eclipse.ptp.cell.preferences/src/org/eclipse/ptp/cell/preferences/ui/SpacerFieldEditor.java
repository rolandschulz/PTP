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
package org.eclipse.ptp.cell.preferences.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page.
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class SpacerFieldEditor extends LabelFieldEditor {
	
	public SpacerFieldEditor(Composite parent) {
		super("", parent); //$NON-NLS-1$
	}
}
