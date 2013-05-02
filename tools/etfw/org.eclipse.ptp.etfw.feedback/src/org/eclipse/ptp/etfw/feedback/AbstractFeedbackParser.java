/**********************************************************************
 * Copyright (c) 2009,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.feedback;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.etfw.feedback.MarkerManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Abstract class that may contain utility methods for parsing feedback xml
 * files.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
 * this API will work or that it will remain the same. We do not recommending using this API without consulting with the
 * etfw.feedback team.
 * 
 * @author beth tibbitts
 * 
 */
abstract public class AbstractFeedbackParser implements IFeedbackParser {

	private MarkerManager mkrMgr;

	public void createMarkers(List<IFeedbackItem> items, String markerID) {
		// System.out.println("create markers");
		if (mkrMgr == null) {
			mkrMgr = new MarkerManager();
		}
		mkrMgr.createMarkers(items, markerID);

	}

	/**
	 * @since 5.0
	 */
	public IFile findSourceFile(String filename, IFile xmlSourceFile) {
		IFile f2 = (IFile) getResourceInProject(xmlSourceFile.getProject(), filename);
		return f2;
	}

	/**
	 * @since 5.0
	 */
	public IResource getResourceInProject(IProject proj, String filename) {
		IResource res = proj.findMember(filename);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.etfw.feedback.AbstractFeedbackParser#findSourceFile(java
	 * .lang.String) This is generic enough it could be pulled up to
	 * AbstractFeedbackParser - but that does not know value of xmlSourceFile
	 * 
	 * xmlSourceFile could actually be any file in the project. Just used here
	 * to find the project.
	 */

	/**
	 * Find a source file in some fashion - presumed filename without absolute
	 * path information. How do we know where to look? Ideas include same
	 * project as the xml file is found; same workspace; some path specified in
	 * preferences maybe?
	 * 
	 * @param filename
	 * @return fully qualified filename including path, or null if not found
	 */
	// public String findSourceFile(String filename) {
	// return null;
	// }
	/**
	 * find file based on project and filename
	 * 
	 * @param projName
	 * @param filename
	 * @return
	 * @since 5.0
	 */
	public IResource getResourceInProject(String projName, String filename) {
		ResourcesPlugin.getWorkspace();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject proj = root.getProject(projName);
		IResource res = proj.findMember(filename);
		res.exists();

		// IFile file=root.getFile(new Path(filename)); // works when filename
		// contains project name
		return res;
	}

	/**
	 * @param file
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @since 5.0
	 */
	public Document getXMLDocument(IFile file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		Document document = null;
		DocumentBuilder builder = domFactory.newDocumentBuilder();

		InputStream xmlIn = null;
		try {
			xmlIn = file.getContents();
			document = builder.parse(xmlIn);
		} catch (CoreException e1) {
			System.out.println("CoreException getting inputstream from: " + file); //$NON-NLS-1$
			e1.printStackTrace();
			document = null;
		}
		return document;
	}

}
