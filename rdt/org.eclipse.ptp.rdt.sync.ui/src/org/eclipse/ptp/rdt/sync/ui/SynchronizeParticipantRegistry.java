/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.internal.rdt.sync.ui.SynchronizeParticipantDescriptor;

public class SynchronizeParticipantRegistry {
	private static List<ISynchronizeParticipantDescriptor> fAllDescriptors;

	public static ISynchronizeParticipantDescriptor[] getDescriptors() {
		loadDescriptors();
		return fAllDescriptors.toArray(new ISynchronizeParticipantDescriptor[fAllDescriptors.size()]);
	}

	private static void loadDescriptors() {
		if (fAllDescriptors == null) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(RDTSyncUIPlugin.PLUGIN_ID,
					RDTSyncUIPlugin.SYNCHRONIZE_EXTENSION);
			if (point != null) {
				fAllDescriptors = new ArrayList<ISynchronizeParticipantDescriptor>();
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement configElement : extension.getConfigurationElements()) {
						fAllDescriptors.add(new SynchronizeParticipantDescriptor(configElement));
					}
				}
			}
		}
	}
}
