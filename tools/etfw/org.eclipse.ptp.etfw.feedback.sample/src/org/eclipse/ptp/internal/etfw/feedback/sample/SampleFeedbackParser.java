/**********************************************************************
 * Copyright (c) 2009,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.sample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackParser;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Sample parser to return items for the ETFw Feedback view
 * 
 * @author beth
 * 
 */
public class SampleFeedbackParser extends AbstractFeedbackParser {
	private boolean traceOn = false;
	private List<IFeedbackItem> items = new ArrayList<IFeedbackItem>();

	public List<IFeedbackItem> getFeedbackItems(IFile ifile) {
		if (traceOn)
			System.out.println("Reading xml file: " + ifile.getLocation());

		items = new ArrayList<IFeedbackItem>();
		try {
			items = parse(ifile);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return items;
	}

	/**
	 * @deprecated use getFeedbackItems(IFile) instead
	 */
	public List<IFeedbackItem> getFeedbackItems(File file) {
		// this is probably twisted around, too much converting back and forth
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(file.getAbsolutePath());
		IFile ifile = workspace.getRoot().getFileForLocation(location);
		List<IFeedbackItem> items = getFeedbackItems(ifile);
		return items;
	}

	/**
	 * Marker ID for markers added by this feedback parser.
	 * For now they are all the same, using plugin id.
	 */
	public String getMarkerID() {
		return Activator.MARKER_ID;
	}

	public String getViewID() {
		return Activator.VIEW_ID;
	}

	/**
	 * Populate objects from the xml file given
	 * 
	 * @param xmlfile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public List<IFeedbackItem> parse(IFile ifile) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		List<IFeedbackItem> items = new ArrayList<IFeedbackItem>();

		// We will look for file in same dir as xml file; save its locn here
		IPath p = ifile.getFullPath();
		p = p.removeLastSegments(1);
		String filepath = p.toPortableString() + IPath.SEPARATOR;

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		Document document = super.getXMLDocument(ifile);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		/* get the items */
		XPathExpression expr = xpath.compile("//*[local-name()='MyItem']");
		Object result = expr.evaluate(document, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			NamedNodeMap attributes = nodes.item(i).getAttributes();
			if (attributes == null)
				continue;
			try {
				String name = attributes.getNamedItem("name").getNodeValue();
				String fname = attributes.getNamedItem("file").getNodeValue();
				// Remote: assure we save enough info to regurgitate even a remote file
				// Note: we never had an IResource/IFile for this to begin with.
				// must create enough info
				fname = filepath + fname; // fully qualify
				String function = attributes.getNamedItem("function").getNodeValue();
				String line = attributes.getNamedItem("lineNo").getNodeValue();
				int lineNo = Integer.parseInt(line);
				String id = attributes.getNamedItem("id").getNodeValue();
				Node parentNode = attributes.getNamedItem("parent");
				String parentID = "noParent";
				if (parentNode != null) {
					parentID = attributes.getNamedItem("parent").getNodeValue();
				}
				SampleFeedbackItem item = new SampleFeedbackItem(name, parentID, id, fname, lineNo, function);
				items.add(item);
			} catch (Exception e) {
				System.out.println("SampleFeedbackParser: Exception creating item " + i);
			}

		}// end for

		if (traceOn)
			System.out.println("SFP found items: " + items.size() + " elements");
		return items;
	}

	/**
	 * For testing only:
	 * try to create an IFile/IResource from the info we have
	 * 
	 * @param fname
	 */
	private void tryCreateFile(String fname, IFile xmlFile) {
		System.out.println("xmlFile: " + xmlFile);
		IProject proj = xmlFile.getProject();
		IResource foundRes = proj.findMember(fname);
		boolean exists = foundRes.exists();
		IPath path = foundRes.getFullPath();
		String s = path.toString();
		String s2 = path.toPortableString();
		String s3 = path.toOSString();
		IResource recreatedRes = getResourceInProject(proj, fname);
		exists = recreatedRes.exists();

	}

}
