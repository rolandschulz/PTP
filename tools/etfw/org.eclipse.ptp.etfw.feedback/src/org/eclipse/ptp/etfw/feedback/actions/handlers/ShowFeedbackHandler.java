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
package org.eclipse.ptp.etfw.feedback.actions.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.etfw.feedback.Activator;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackParser;
import org.eclipse.ptp.etfw.feedback.util.ViewActivator;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. We do not recommending using this API without consulting with
 * the etfw.feedback team.
 */
public class ShowFeedbackHandler extends AbstractHandler {
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_CLASSNAME = "class"; //$NON-NLS-1$
	private IStructuredSelection selection=null;
	private static final boolean traceOn=false;
	/**
	 * The constructor.
	 */
	public ShowFeedbackHandler() {
	}

	/**
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 * @return the result of the execution. Reserved for future use, must be
	 *         <code>null</code>.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel=getSelection(event);
		IFeedbackParser parser=null;
		// iterate over multiple selections.  Note we do NOT descend e.g. into folders.
		for (Iterator<IResource> it = sel.iterator(); it.hasNext();) {
			IResource obj = it.next();  
			if (obj instanceof IFile) {
				IFile file = (IFile) obj;
				if(traceOn)System.out.println("selected: "+file); //$NON-NLS-1$
				String name = file.getName();
				IPath path = file.getLocation();
				if(traceOn)System.out.println("Selected: " + name); //$NON-NLS-1$
				if (name.endsWith(".xml")) { //$NON-NLS-1$
					File jfile = path.toFile();
					parser = findFeedbackParser(jfile );
					List<IFeedbackItem> items=parser.getFeedbackItems(jfile);
					parser.createMarkers(items,parser.getMarkerID());
				}
			}
		}
		// show view only after all files have been processed.
		if (parser != null) {
			ViewActivator.activateView(parser.getViewID());
		}
		return null;
	}
	
	/**
	 * Find the eclipse extension that is registered
	 * to handle reports of the type indicated by the root node element
	 * of the given xml file.  Return its IFeedbackParser.
	 * @param jfile
	 */
	private IFeedbackParser findFeedbackParser(File file) {
		 
			InputStream xmlIn;
			try {
				xmlIn = new FileInputStream(file);
			} catch (FileNotFoundException e1) {
				System.out.println("File not found: "+file.getAbsolutePath()); //$NON-NLS-1$
				return null;
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document = null;
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				if (xmlIn != null)
					document = builder.parse(xmlIn);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("TransformDocXMLParser IOException: "+e.getMessage()); //$NON-NLS-1$
			}
			Node node = document.getFirstChild();
			String rootNode=node.getNodeName();
			
			// first node might be xml version info or stylesheet info
			if(rootNode.equals("xml") || (rootNode.equals("xml-stylesheet"))) { //$NON-NLS-1$ //$NON-NLS-2$
				Node next = node.getNextSibling();
				node=next;
				rootNode=node.getNodeName();
			}
			// 1. find the extensions
			// 2. find one that matches nodeName in its id
			// 3. instantiate the class of parser or whatever that knows
			//       how to read this type
			final String pid=Activator.PLUGIN_ID;
			final String extid=Activator.FEEDBACK_EXTENSION_ID;
			IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(pid, extid).getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extn=extensions[i];
				String extLabel=extn.getLabel();
				if(traceOn)System.out.println("Found extension for "+extLabel+ "  id="+extn.getUniqueIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
				IConfigurationElement[] configElements =
					extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					IConfigurationElement ice= configElements[j];
					// from this thing we should be able to make the specific
					// parts that we need.
					// specifically: something that can parse this file and return
					// things that implement the IFeedbackItem interface.
					if(traceOn)System.out.println(ice.getAttributeNames());
					String nodeName=ice.getAttribute("nodeName"); //$NON-NLS-1$

					if(rootNode.equals(nodeName)) {
						// we found a match!
						if(traceOn)System.out.println("match! "+rootNode); //$NON-NLS-1$				
						String className = ice.getAttribute(ATTR_CLASSNAME);
						String name=ice.getAttribute(ATTR_NAME);
						if(traceOn)System.out.println("class="+className+"   name="+name); //$NON-NLS-1$ //$NON-NLS-2$
						Object obj=null;
						try {
							obj=ice.createExecutableExtension(ATTR_CLASSNAME);
							if(obj instanceof IFeedbackParser) {
								IFeedbackParser fp= (IFeedbackParser)obj;
								return fp;
							}
						} catch (CoreException e) {
							System.out.println("Failed to create class "+className); //$NON-NLS-1$
							e.printStackTrace();
						}
					}
					
				}
			}
			return null;
			 
		 
		
	}

	/**
	 * Get the current selection from the handler event. If it's a structured selection
	 * (e.g. resource in the project explorer) then return it.
	 * If it's e.g. a text selection in the editor, we don't care about that
	 * 
	 * Note that we  cache the last structured selection (like the previous "action" version
	 * of this class) since we don't get selection changed events.
	 * However, AnalysisDropDownHandler does get these events, and its value
	 * will be used if HanderUtil doesn't have any information yet.
	 * 
	 * @param event
	 * @return the current selection if it's a structured selection e.g. in the navigator
	 */
	public IStructuredSelection getSelection(ExecutionEvent event) {
		ISelection curSel = HandlerUtil.getCurrentSelection(event);
		if (curSel instanceof IStructuredSelection) {
			selection = (IStructuredSelection) curSel;
		}else {
			System.out.println("no selection."); //$NON-NLS-1$
		}

		return selection;
	
	}
}
