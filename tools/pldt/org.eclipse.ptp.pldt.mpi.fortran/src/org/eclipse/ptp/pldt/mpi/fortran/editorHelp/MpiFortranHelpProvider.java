/**********************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.fortran.editorHelp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.photran.ui.IFortranAPIHelpProvider;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.fortran.MPIFortranPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Help book for Fortran MPI functions
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class MpiFortranHelpProvider implements IFortranAPIHelpProvider {
	private final Map<String, String> fNameToCname;

	/**
	 * builds the list of function summaries by parsing an XML file
	 */
	public MpiFortranHelpProvider() {
		Map<String, String> fNameToCname;
		URL fileURL = FileLocator.find(Platform.getBundle(MpiPlugin.getPluginId()), new Path("mpiref.xml"), null); //$NON-NLS-1$
		try {
			fNameToCname = parseDOM(fileURL.openStream());
		} catch (IOException e) {
			MPIFortranPlugin.log(e);
			fNameToCname = Collections.<String, String>emptyMap();
		}
		this.fNameToCname = fNameToCname;
	}

	public static Map<String, String> parseDOM(InputStream xmlIn) {
		final Map<String, String> fNameToCname = new HashMap<String, String>();
		if (xmlIn != null)
		{
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = builder.parse(xmlIn);
				NodeList functionList = document.getElementsByTagName("function");//$NON-NLS-1$
				for (int i = 0; i < functionList.getLength(); i++) {
					Node func = functionList.item(i);
					NodeList child = func.getChildNodes();
					String fname = null;
					String cname = null;
					// String desc = null;
					for (int j = 0; j < child.getLength(); j++) {
						Node sub = child.item(j);
						if (sub.getNodeName().equals("cname")) {
							cname = sub.getTextContent(); // java 5
						//} else if (sub.getNodeName().equals("description")) {//$NON-NLS-1$
							// desc = sub.getTextContent(); // java 5
						} else if (sub.getNodeName().equals("fname")) {//$NON-NLS-1$
							fname = sub.getTextContent(); // java 5
						}
					}
					if (fname != null && cname != null) {
						fNameToCname.put(fname.toUpperCase(), cname);
					}
				}
			} catch (Exception e) {
				MPIFortranPlugin.log(e);
			}
		}
		return fNameToCname;
	}

	public IHelpResource[] getHelpResources(ITextEditor fortranEditor, String name, String precedingText) {
		final String fname = name.toUpperCase();
		if (fNameToCname.containsKey(fname)) {
			return new IHelpResource[] { new IHelpResource() {
				public String getHref() {
					return String.format("/%s/html/%s.html", MpiPlugin.getPluginId(), fNameToCname.get(fname));
				}
				public String getLabel() {
					return fname;
				}
			} };
		}
		return null;
	}
}
