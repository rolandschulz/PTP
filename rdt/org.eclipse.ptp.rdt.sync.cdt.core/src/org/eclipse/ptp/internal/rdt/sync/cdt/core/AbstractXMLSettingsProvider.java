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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Abstract class for language settings providers that get settings through XML.
 * This class handles reading, parsing, and storing XML data to the appropriate CDT data structures. Concrete classes only need
 * to provide the source of the XML and optional XSLT data, and specify when the data should be refreshed.
 * 
 * Note: Currently this class only supports XML from files. A future improvement would be to support XML data from other sources.
 */
public abstract class AbstractXMLSettingsProvider extends LanguageSettingsSerializableProvider {
	/**
	 * @return path to XML file that contains language settings or null for no settings. This XML file will be converted by the
	 * XSLT file returned by {@link #getXSLTFile}
	 */
	public abstract IPath getXMLFile();

	/**
	 * @return path to XSLT file to convert XML file returned by {@link #getXMLFile}, or null if no conversion is needed (XML file
	 * is already in the proper format).
	 */
	public abstract IPath getXSLTFile();

	public void reset() throws ParserConfigurationException, SAXException, IOException {
		super.clear();
		IPath XMLFile = getXMLFile();
		if (XMLFile == null) {
			return;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(getXMLFile().toFile());
		super.loadEntries(doc.getDocumentElement());
	}
}