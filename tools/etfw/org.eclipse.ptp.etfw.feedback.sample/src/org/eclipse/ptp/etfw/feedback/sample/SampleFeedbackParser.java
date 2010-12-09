/**********************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.etfw.feedback.sample;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackParser;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Sample parser to return items for the ETFw Feedback view
 * @author beth
 *
 */
public class SampleFeedbackParser extends AbstractFeedbackParser {
	private boolean traceOn=true;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackParser#getFeedbackItems()
	 */
	public IFeedbackItem[] getFeedbackItems() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<IFeedbackItem> getFeedbackItems(IFile ifile) {
		if(traceOn)System.out.println("Reading xml file: "+ifile.getLocation());
		/// FIXME remote???
		IPath path = ifile.getLocation();
		URI uri=ifile.getLocationURI();
		String xmlfile = null;
		if(path!=null) {
			xmlfile=path.toOSString();
		}
		List<IFeedbackItem> items=new ArrayList<IFeedbackItem>();
		try {
			items=parse(ifile);
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

	public void setFile(IFile file) {
		System.out.println("not used!");
		
	}

	/**
	 * @deprecated use getFeedbackItems(IFile) instead
	 */
	public List<IFeedbackItem> getFeedbackItems(File file) {		
		// this is probably twisted around, too much converting back and forth
		IWorkspace workspace =ResourcesPlugin.getWorkspace();
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
	 * @param xmlfile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public List<IFeedbackItem> parse(String xmlfile)
	  throws ParserConfigurationException, SAXException, 
	  IOException, XPathExpressionException {
	      //=== getXMLDocument
		  DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		  domFactory.setNamespaceAware(true); // never forget this!
	      Document document = null;
	      DocumentBuilder builder = domFactory.newDocumentBuilder();
	
	      File file = new File(xmlfile);
	      if(!file.exists()) {
	    	  System.out.println("Cannot find file: "+xmlfile);
	    	  return null;
	      }
	      if(traceOn)System.out.println("Parse XML file: "+file);

	      List<IFeedbackItem> items = new ArrayList<IFeedbackItem>();

	      // look for file in same dir as xml file
	      IPath p = new Path(xmlfile);
	      p=p.removeLastSegments(1);
	      String filepath=p.toPortableString()+IPath.SEPARATOR;//+"mhd.F";  

	      document = builder.parse(file);
		      
	      XPathFactory factory = XPathFactory.newInstance();
	      XPath xpath = factory.newXPath();
	      
	      /* get the hotspots */
	      XPathExpression expr 
	       = xpath.compile("//*[local-name()='MyItem']");
	
	      Object result = expr.evaluate(document, XPathConstants.NODESET);
	      NodeList nodes = (NodeList) result;
	      for (int i = 0; i < nodes.getLength(); i++) {
	          NamedNodeMap attributes = nodes.item(i).getAttributes();
	          if (attributes == null) continue;
				try {
					String name = attributes.getNamedItem("name").getNodeValue();

					String fname = attributes.getNamedItem("file").getNodeValue();
					fname=filepath+fname; // fully qualify (if needed?)
					String function = attributes.getNamedItem("function").getNodeValue();
					String line = attributes.getNamedItem("lineNo").getNodeValue();
					int lineNo = Integer.parseInt(line);
					String id = attributes.getNamedItem("id").getNodeValue();
					Node parentNode=attributes.getNamedItem("parent");
					String parentID="noParent";
					if(parentNode!=null) {
					  parentID=attributes.getNamedItem("parent").getNodeValue();
					}
					SampleFeedbackItem item = new SampleFeedbackItem(name, parentID, id, fname, lineNo, function);
					items.add(item);
				} catch (Exception e) {
					System.out.println("SampleFeedbackParser: Exception creating item " + i);
				}
	
	      }//end for


	      
	      return items;
	}

	/**
	 * Populate objects from the xml file given
	 * @param xmlfile
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public List<IFeedbackItem> parse(IFile ifile)
	  throws ParserConfigurationException, SAXException, 
	  IOException, XPathExpressionException {
	      
		List<IFeedbackItem> items = new ArrayList<IFeedbackItem>();
		
		  DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		  domFactory.setNamespaceAware(true); // never forget this!
	      Document document = null;
	      DocumentBuilder builder = domFactory.newDocumentBuilder();
	
	      
	      //===
	      // look for file in same dir as xml file
	      // work ok for remote files 
	      IPath p = ifile.getFullPath();
	      p=p.removeLastSegments(1);
	      String filepath=p.toPortableString()+IPath.SEPARATOR;//+"mhd.F"; 
	      //===
	      InputStream xmlIn=null;
		try {
			xmlIn = ifile.getContents();
		} catch (CoreException e1) {
			System.out.println("CoreException getting inputstream from: "+ifile); //$NON-NLS-1$
			e1.printStackTrace();
			return null;
		}	
	      document = builder.parse(xmlIn);	      
		      
	      XPathFactory factory = XPathFactory.newInstance();
	      XPath xpath = factory.newXPath();
	      
	      /* get the items */
	      XPathExpression expr 
	       = xpath.compile("//*[local-name()='MyItem']");
	
	      Object result = expr.evaluate(document, XPathConstants.NODESET);
	      NodeList nodes = (NodeList) result;
	      for (int i = 0; i < nodes.getLength(); i++) {
	          NamedNodeMap attributes = nodes.item(i).getAttributes();
	          if (attributes == null) continue;
				try {
					String name = attributes.getNamedItem("name").getNodeValue();
	
					String fname = attributes.getNamedItem("file").getNodeValue();
					// Remote: assure we save enough info to regurgitate even a remote file
					// Note: we never had an IResource/IFile for this to begin with. must create enough info
					tryCreateFile(fname, ifile);
					fname=filepath+fname; // fully qualify (if needed?)
					String function = attributes.getNamedItem("function").getNodeValue();
					String line = attributes.getNamedItem("lineNo").getNodeValue();
					int lineNo = Integer.parseInt(line);
					String id = attributes.getNamedItem("id").getNodeValue();
					Node parentNode=attributes.getNamedItem("parent");
					String parentID="noParent";
					if(parentNode!=null) {
					  parentID=attributes.getNamedItem("parent").getNodeValue();
					}
					SampleFeedbackItem item = new SampleFeedbackItem(name, parentID, id, fname, lineNo, function);
					items.add(item);
				} catch (Exception e) {
					System.out.println("SampleFeedbackParser: Exception creating item " + i);
				}
	
	      }//end for
	
	
	      System.out.println("SFP found items: "+items.size()+" elements");
	      return items;
	}

	/**
	 * try to create an IFile/IResource from the info we have
	 * @param fname
	 */
	private void tryCreateFile(String fname, IFile xmlFile) {
		System.out.println("xmlFile: "+xmlFile);
		//IPath fullpath=xmlFile.getFullPath();
		//String pstr=fullpath.toPortableString();
		IProject proj=xmlFile.getProject();
		IResource foundRes=proj.findMember(fname);
		boolean exists=foundRes.exists();
		IPath path = foundRes.getFullPath();
		String s = path.toString();
		String s2=path.toPortableString();
		String s3=path.toOSString();
		
		//IResource res=getResource(fname);
		
		
		IResource recreatedRes=getResourceInProject(proj,fname);
		 exists=recreatedRes.exists();
		 
		//System.out.println("end");
		//boolean exists=res.exists();
		System.out.println("end");
		
	}


}
