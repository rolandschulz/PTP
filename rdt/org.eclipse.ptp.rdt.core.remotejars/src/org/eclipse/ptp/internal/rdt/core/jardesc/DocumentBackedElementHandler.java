/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.jardesc;

import java.io.File;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Expands jardesc elements into corresponding files and stores the
 * result in a <code>Document</code>.
 */
public abstract class DocumentBackedElementHandler implements IElementHandler {

	private Element fFiles;
	private Element fProperties;
	private Element fGrammarTemplate;
	private Document fDocument;
	private String fBasePath;

	public DocumentBackedElementHandler(String basePath) throws SAXException {
		fBasePath = basePath;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
		
		fDocument = builder.newDocument();
		Element root = fDocument.createElement("elements"); //$NON-NLS-1$
		fDocument.appendChild(root);
		
		fFiles = fDocument.createElement("files"); //$NON-NLS-1$
		fProperties = fDocument.createElement("properties"); //$NON-NLS-1$
		fGrammarTemplate = fDocument.createElement("g"); //$NON-NLS-1$
		
		root.appendChild(fFiles);
		root.appendChild(fProperties);
		root.appendChild(fGrammarTemplate);
	}
	
	/**
	 * Returns a set of unqualified class names that correspond to the top-level
	 * classes contained within the given file.
	 * 
	 * @param basePath the physical directory containing the file.
	 * @param fileName the Java source file name.
	 * @return a set of unqualified class names.
	 */
	protected abstract Set<String> collectTopLevelClasses(String basePath, String fileName);

	public void handleFile(String sourceFolder, String packageName, String fileName) {
		String project = extractProject(sourceFolder);
		String path = packageName.replaceAll("\\.", "/");  //$NON-NLS-1$//$NON-NLS-2$
		addFile(project, path, fileName, sourceFolder);
	}

	private void addFile(String project, String path, String fileName, String sourceFolder) {
		String folder = sourceFolder.substring(1);
		
		String basePath = fBasePath + "/" + folder + "/" + path;  //$NON-NLS-1$//$NON-NLS-2$
		for (String name : collectTopLevelClasses(basePath, fileName)) {
			Element element;
			if (fileName.endsWith(".g"))
				element= fDocument.createElement("grammar_file"); //$NON-NLS-1$
			else if (fileName.endsWith(".properties"))
				element = fDocument.createElement("properties_file"); //$NON-NLS-1$
			else
				element = fDocument.createElement("file"); //$NON-NLS-1$
			element.setAttribute("folder", project); //$NON-NLS-1$
			
			// Strip off ".java" from the end.
			StringBuilder className = new StringBuilder();
			if (path.length() > 0) {
				className.append(path);
				className.append("/"); //$NON-NLS-1$
			}
			className.append(name.replaceAll("\\.java$", "")); //$NON-NLS-1$ //$NON-NLS-2$
			
			element.setTextContent(className.toString());
			
			if (fileName.endsWith(".g"))
				fGrammarTemplate.appendChild(element);
			else if (fileName.endsWith(".properties"))
				fProperties.appendChild(element);
			else
				fFiles.appendChild(element);			
		}
	}

	private String extractProject(String sourceFolder) {
		String[] projectDetails = sourceFolder.split("/"); //$NON-NLS-1$
		return projectDetails[0].replace("=", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void handleFolder(String sourceFolder) {
		String[] projectDetails = sourceFolder.split("/"); //$NON-NLS-1$
		String project = projectDetails[0].replace("=", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String sourceRoot = projectDetails[1];
		
		expandFolders(project + "/" + sourceRoot, project, sourceFolder, true); //$NON-NLS-1$
	}

	private void expandFolders(String path, String project, String sourceFolder, boolean expandSubFolders) {
		File root = new File(fBasePath + "/" + path); //$NON-NLS-1$
		expandFolders(root, project, "", sourceFolder, expandSubFolders); //$NON-NLS-1$
	}
	
	private void expandFolders(File root, String project, String path, String sourceFolder, boolean expandSubFolders) {
		for (File file : root.listFiles()) {
			if (file.isDirectory() && expandSubFolders) {
				String basePath = path.length() == 0 ? file.getName() : path + "/" + file.getName(); //$NON-NLS-1$
				expandFolders(file, project, basePath, sourceFolder, expandSubFolders);
			} else if (file.getName().endsWith(".java")) { //$NON-NLS-1$
				addFile(project, path, file.getName(), sourceFolder);
			} else if (file.getName().endsWith(".properties")) { //$NON-NLS-1$
				addFile(project, path, file.getName(), sourceFolder);
			} else if (file.getName().endsWith(".g")) { //$NON-NLS-1$
				addFile(project, path, file.getName(), sourceFolder);
			}
		}
	}

	public void handlePackage(String sourceFolder, String packageName) {
		String[] projectDetails = sourceFolder.split("/"); //$NON-NLS-1$
		String project = projectDetails[0].replace("=", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String sourceRoot = projectDetails[1];
		
		String packagePath = packageName.replaceAll("\\.", "/");  //$NON-NLS-1$//$NON-NLS-2$
		String path = project + "/" + sourceRoot + "/" + packagePath;  //$NON-NLS-1$//$NON-NLS-2$
		File root = new File(fBasePath + "/" + path); //$NON-NLS-1$
		expandFolders(root, project, packagePath, sourceFolder, false);
	}

	public Document getDocument() {
		return fDocument;
	}

}
