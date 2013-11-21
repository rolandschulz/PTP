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

import java.io.IOException;

import javax.xml.transform.Source;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
	 * Get the XML file
	 * @return XML file
	 */
	public IPath getXMLFile() {
		return XMLFile;
	}

	/**
	 * Get the XSLT file
	 * @return XSLT file
	 */
	public IPath getXSLTFile() {
		return XSLTFile;
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
	public Document getXML() throws SAXException, IOException {
		return XMLConversionUtil.XMLFileToDOM(XMLFile);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.internal.rdt.sync.cdt.core.AbstractXMLSettingsProvider#getXSLTFile()
	 */
	@Override
	public Source getXSLT() {
		if (XSLTFile == null) {
			return null;
		} else {
			return XMLConversionUtil.XSLTFileToSource(XSLTFile);
		}
	}
}