/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;

public class SyncXMLFileSettingsProvider extends LanguageSettingsSerializableProvider implements ILanguageSettingsEditableProvider {
	private String XMLFile = ""; //$NON-NLS-1$

	@Override
	public SyncXMLFileSettingsProvider clone() throws CloneNotSupportedException {
		return (SyncXMLFileSettingsProvider) super.clone();
	}
	@Override
	public SyncXMLFileSettingsProvider cloneShallow() throws CloneNotSupportedException {
		return (SyncXMLFileSettingsProvider) super.cloneShallow();
	}

	public String getXMLFile() {
		return XMLFile;
	}
}