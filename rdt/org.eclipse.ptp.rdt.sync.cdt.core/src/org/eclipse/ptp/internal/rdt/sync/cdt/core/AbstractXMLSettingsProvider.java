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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
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
 */
public abstract class AbstractXMLSettingsProvider extends LanguageSettingsSerializableProvider {
	/**
	 * @return XML file that contains language settings or null for no settings. This XML will be converted by the XSLT returned
	 * by {@link #getXSLTFile} See {@link org.eclipse.ptp.rdt.sync.cdt.core.XMLConversionUtil} for helper methods to read files
	 * into Document objects.
	 * 
	 * This function is called by "reset" so is allowed to throw the same exceptions, since the UI layer will probably call "reset".
	 * @throws IOException
	 * 					on problems retrieving XML from file system
	 * @throws SAXException
	 * 					on problems parsing XML 
	 */
	public abstract Document getXML() throws IOException, SAXException;

	/**
	 * @return XSLT to convert XML returned by {@link #getXMLFile}, or null if no conversion is needed (XML is already in the
	 * proper format). See {@link org.eclipse.ptp.rdt.sync.cdt.core.XMLConversionUtil} for helper methods to read files into
	 * Document objects.
	 */
	public abstract Source getXSLT();

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
		Document XMLInput = getXML();
		if (XMLInput == null) {
			return;
		}
		Source XSLTInput = getXSLT();
		
		if (XSLTInput == null) {
			super.loadEntries(XMLInput.getDocumentElement());
		} else {
			// Transform XML with XSLT into a stream
			Transformer transformer = TransformerFactory.newInstance().newTransformer(XSLTInput);
			DOMSource tmpDOMSource = new DOMSource(XMLInput);
			StringWriter XMLTransformed = new StringWriter();
			transformer.transform(tmpDOMSource, new StreamResult(XMLTransformed));

			// Convert stream back into a document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// Should never happen since no configuring was done
				throw new RuntimeException(e);
			}
			// TODO: Is default character set okay?
			Document finalDoc = builder.parse(new ByteArrayInputStream(XMLTransformed.toString().getBytes()));

			super.loadEntries(finalDoc.getDocumentElement());
		}
	}
}