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


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class TargetPreferencePage
	extends AbstractBasicPreferencePage{

	public TargetPreferencePage() {
		super();
		setDescription("Remote Tools preferences");
	}
	
}
