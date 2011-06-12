/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.jardesc;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Extracts jardesc elements from a file and hands off the result to an
 * <code>IElementHandler</code>.
 * </p>
 * 
 * <p>
 * This class can be run on the command line to generate an XML file
 * containing the files corresponding to a jardesc specification.  Usage:
 * </p> 
 * 
 * <p>
 * <code>
 * java org.eclipse.ptp.internal.rdt.core.jardesc.JarDescElementExtractor \<br/>
 *      <em>input.jardesc</em> <em>output.xml</em> <em>basePath</em>
 * </code>
 * </p>
 * 
 * <p>
 * ... where <em><code>basePath</code></em> is the workspace root.  As such,
 * this tool will only work if all the dependent projects are located directly
 * within the workspace directory.
 * </p>
 */
public class JarDescElementExtractor {
	/**
	 * Extracts Jardesc elements from the given Jardesc XML file.
	 */
	public void extract(String fileName, IElementHandler handler) throws IOException, SAXException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(fileName));
			extract(document, handler);
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
	}
	
	/**
	 * Extracts Jardesc elements from the given Jardesc XML
	 * <code>Document</code>.
	 */
	public void extract(Document document, IElementHandler handler) {
		NodeList nodes = document.getElementsByTagName("javaElement"); //$NON-NLS-1$
		for (int i = 0; i < nodes.getLength(); i++) {
			Element node = (Element) nodes.item(i);
			String elementData = node.getAttribute("handleIdentifier"); //$NON-NLS-1$
			String[] folderDetails = elementData.split("<"); //$NON-NLS-1$
			String packageName = null;
			
			// Check if we're dealing with the default package, or the whole source folder. 
			if (elementData.endsWith("<")) { //$NON-NLS-1$
				packageName = ""; //$NON-NLS-1$
			}
			
			String fileName = null;
			if (folderDetails.length == 2) {
				String[] fileDetails = folderDetails[1].split("\\{"); //$NON-NLS-1$
				packageName = fileDetails[0];
				if (fileDetails.length == 2) {
					fileName = fileDetails[1];
				}
			}

			String sourceFolder = folderDetails[0]; 

			if (packageName == null && fileName == null) {
				// Whole source folder
				handler.handleFolder(sourceFolder);
			} else if (fileName == null) {
				// Whole package
				handler.handlePackage(sourceFolder, packageName);
			} else {
				// Single file
				handler.handleFile(sourceFolder, packageName, fileName);
			}
		}
	}
	
	public static void main(String[] args) throws IOException, SAXException, TransformerException {
		String jarDescFileName = args[0];
		String outFileName = args[1];
		
		String basePath;
		if (args.length > 2) {
			basePath = args[2];
		} else {
			basePath = "."; //$NON-NLS-1$
		}
		
		JarDescElementExtractor extractor = new JarDescElementExtractor();
		DocumentBackedElementHandler handler = new SloppyElementHandler(basePath); 
		extractor.extract(jarDescFileName, handler);
		Document document = handler.getDocument();
		
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.transform(new DOMSource(document), new StreamResult(new File(outFileName)));
	}
}
