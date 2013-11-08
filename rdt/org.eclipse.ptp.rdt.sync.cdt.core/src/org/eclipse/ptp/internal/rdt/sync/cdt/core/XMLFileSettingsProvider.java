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
import org.eclipse.core.runtime.IPath;

/**
 * A generic XML Settings provider. The user can specify an XML and XSLT file, thus providing generic support for build tools not
 * fully supported with their own provider.
 */
public class XMLFileSettingsProvider extends AbstractXMLSettingsProvider implements ILanguageSettingsEditableProvider {
	private IPath XMLFile = null;
	private IPath XSLTFile = null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider#clone()
	 */
	@Override
	public XMLFileSettingsProvider clone() throws CloneNotSupportedException {
		return (XMLFileSettingsProvider) super.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider#cloneShallow()
	 */
	@Override
	public XMLFileSettingsProvider cloneShallow() throws CloneNotSupportedException {
		return (XMLFileSettingsProvider) super.cloneShallow();
	}

	/**
	 * Set the XML file
	 * @param path to XML file or null to indicate no XML file
	 */
	public void setXMLFile(IPath path) {
		XMLFile = path;
	}

	/**
	 * Set the XSLT file
	 * @param path to XSLT file or null to indicate no XSLT file
	 */
	public void setXSLTFile(IPath path) {
		XSLTFile = path;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.sync.cdt.core.AbstractXMLSettingsProvider#getXMLFile()
	 */
	@Override
	public IPath getXMLFile() {
		return XMLFile;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.sync.cdt.core.AbstractXMLSettingsProvider#getXSLTFile()
	 */
	@Override
	public IPath getXSLTFile() {
		return XSLTFile;
	}
}