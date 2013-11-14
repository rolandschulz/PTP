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
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class for common XML conversion operations
 */
public class XMLConversionUtil {
	/**
	 * Convert XML file to DOM
	 * @param XMLFile
	 *
	 * @return DOM
	 * @throws SAXException
	 *               on problems parsing XML
	 * @throws IOException
	 *               on problems reading file
	 */
	public static Document XMLFileToDOM(IPath XMLFile) throws SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// Should never happen since no configuring was done
			throw new RuntimeException(e); 
		}
		return builder.parse(XMLFile.toFile());
	}

	/**
	 * Convert XSLT file to a Source
	 * Note that no actual file access or parsing occurs, so no exceptions are thrown.
	 *
	 * @param XSLTFile
	 * @return Source
	 */
	public static Source XSLTFileToSource(IPath XSLTFile) {
		return new StreamSource(XSLTFile.toFile());
	}
}
