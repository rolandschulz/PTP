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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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

	/**
	 * Clears and reloads settings from XML file. It does nothing if XML file is null. If XSLT is non-null, it is used to
	 * transform XML before loading settings.
	 *
	 * @throws IOException
	 * 					on problems reading XML file
	 * @throws SAXException
	 * 					on problems parsing XML
	 * @throws TransformerException
	 * 					on problems with XSLT transformation
	 */
	public void reset() throws IOException, SAXException, TransformerException {
		super.clear();
		IPath XMLFile = getXMLFile();
		if (XMLFile == null) {
			return;
		}
		IPath XSLTFile = getXSLTFile();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// Should never happen since no configuring was done
			throw new RuntimeException(e);
		}

		Document doc;
		if (XSLTFile == null) {
			doc = builder.parse(XMLFile.toFile());
		} else {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Source XSLTSource = new StreamSource(XSLTFile.toFile());
			Transformer transformer = tFactory.newTransformer(XSLTSource);

			Source XMLSource = new StreamSource(XMLFile.toFile());
			StringWriter transformedXML = new StringWriter();
			transformer.transform(XMLSource, new StreamResult(transformedXML));
			doc = builder.parse(transformedXML.toString());
		}

		super.loadEntries(doc.getDocumentElement());
	}
}