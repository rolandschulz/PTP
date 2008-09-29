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
package org.eclipse.ptp.cell.debug.ui.preferences;

import org.eclipse.ptp.cell.preferences.ui.AbstractBasicPreferencePage;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class CellDebugPreferencePage
	extends AbstractBasicPreferencePage{

	public CellDebugPreferencePage() {
		super();
		setDescription(DebugPreferencesMessages.getString("CellDebugPreferencePage.0")); //$NON-NLS-1$
	}
	
}