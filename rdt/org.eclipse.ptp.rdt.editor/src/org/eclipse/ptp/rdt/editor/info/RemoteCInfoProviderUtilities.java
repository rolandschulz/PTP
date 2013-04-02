/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.editor.info;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.editor.Activator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class RemoteCInfoProviderUtilities {

	private static IDocumentProvider _provider;

	/**
	 * Retrieves the list of editor information providers used to configure the editor.
	 * @return the list of editor info providers
	 */
	public static List<IRemoteCEditorInfoProvider> getEditorInfoProviders() {
		List<IRemoteCEditorInfoProvider> editorProviders = new ArrayList<IRemoteCEditorInfoProvider>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "editorInfoProvider"); //$NON-NLS-1$
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; ++i) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; ++j) {
					if (elements[j].getName().equals("editorInfoProvider")) { //$NON-NLS-1$
						try {
							IRemoteCEditorInfoProvider value = (IRemoteCEditorInfoProvider) elements[j].createExecutableExtension("class"); //$NON-NLS-1$
							editorProviders.add(value);
						} catch (CoreException c) {
							Activator.log(c);
						}
					}
				}
			}
		}
		return editorProviders;
	}

	public static IRemoteCEditorInfoProvider getApplicableEditorInfoProvider(List<IRemoteCEditorInfoProvider> infoProviders, IEditorInput input) {
		IRemoteCEditorInfoProvider winner = null;
		int highestVal = 0;
		for (IRemoteCEditorInfoProvider provider: infoProviders) {
			int applicable = Math.min(100, provider.isApplicableEditorInput(input));
			if(applicable <= 0)
				continue;
			if(applicable > highestVal)
				winner = provider;
		}
		
		return winner;
	}

}
