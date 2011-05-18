/**********************************************************************
 * Copyright (c) 2010,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.upc.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.upc.UPCPlugin;
import org.eclipse.ptp.pldt.upc.internal.UPCIDs;

/**
 * Class used to initialize default preference values.
 * 
 * @author Beth Tibbitts
 * @since 4.0
 */
public class UPCPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = UPCPlugin.getDefault().getPreferenceStore();
		store.setDefault(UPCIDs.UPC_RECOGNIZE_APIS_BY_PREFIX_ALONE, true);

	}

}
