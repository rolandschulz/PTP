/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.ptp.debug.core.sourcelookup.PDirectorySourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SourceUtils {
	private static final String NAME_COMMON_SOURCE_LOCATIONS = "commonSourceLocations";
	private static final String NAME_SOURCE_LOCATION = "sourceLocation";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_MEMENTO = "memento";

	public static String getCommonSourceLocationsMemento(IPSourceLocation[] locations) {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = document.createElement(NAME_COMMON_SOURCE_LOCATIONS);
			document.appendChild(element);
			saveSourceLocations(document, element, locations);
			return CDebugUtils.serializeDocument(document);
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		} catch (TransformerException e) {
			ex = e;
		}
		PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error saving common source settings.", ex));
		return null;
	}
	private static void saveSourceLocations(Document doc, Element node, IPSourceLocation[] locations) {
		for (int i = 0; i < locations.length; i++) {
			Element child = doc.createElement(NAME_SOURCE_LOCATION);
			child.setAttribute(ATTR_CLASS, locations[i].getClass().getName());
			try {
				child.setAttribute(ATTR_MEMENTO, locations[i].getMemento());
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
				continue;
			}
			node.appendChild(child);
		}
	}
	public static IPSourceLocation[] getCommonSourceLocationsFromMemento(String memento) {
		IPSourceLocation[] result = new IPSourceLocation[0];
		if (!isEmpty(memento)) {
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				StringReader reader = new StringReader(memento);
				InputSource source = new InputSource(reader);
				Element root = parser.parse(source).getDocumentElement();
				if (root.getNodeName().equalsIgnoreCase(NAME_COMMON_SOURCE_LOCATIONS))
					result = initializeSourceLocations(root);
			} catch (ParserConfigurationException e) {
				PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e));
			} catch (SAXException e) {
				PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e));
			} catch (IOException e) {
				PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, "Error initializing common source settings.", e));
			}
		}
		return result;
	}
	public static IPSourceLocation[] initializeSourceLocations(Element root) {
		List sourceLocations = new LinkedList();
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(NAME_SOURCE_LOCATION)) {
					String className = entry.getAttribute(ATTR_CLASS);
					String data = entry.getAttribute(ATTR_MEMENTO);
					if (className == null || className.trim().length() == 0) {
						PTPDebugCorePlugin.log("Unable to restore common source locations - invalid format.");
						continue;
					}
					Class clazz = null;
					try {
						clazz = PTPDebugCorePlugin.getDefault().getBundle().loadClass(className);
					} catch (ClassNotFoundException e) {
						PTPDebugCorePlugin.log(MessageFormat.format("Unable to restore source location - class not found {0}", new String[] { className }));
						continue;
					}
					IPSourceLocation location = null;
					try {
						location = (IPSourceLocation) clazz.newInstance();
					} catch (IllegalAccessException e) {
						PTPDebugCorePlugin.log("Unable to restore source location: " + e.getMessage());
						continue;
					} catch (InstantiationException e) {
						PTPDebugCorePlugin.log("Unable to restore source location: " + e.getMessage());
						continue;
					}
					try {
						location.initializeFrom(data);
						sourceLocations.add(location);
					} catch (CoreException e) {
						PTPDebugCorePlugin.log("Unable to restore source location: " + e.getMessage());
					}
				}
			}
		}
		return (IPSourceLocation[]) sourceLocations.toArray(new IPSourceLocation[sourceLocations.size()]);
	}
	private static boolean isEmpty(String string) {
		return (string == null || string.trim().length() == 0);
	}
	static public ISourceContainer[] convertSourceLocations(IPSourceLocation[] locations) {
		ArrayList containers = new ArrayList(locations.length);
		int mappingCount = 0;
		for (int i = 0; i < locations.length; ++i) {
			if (locations[i] instanceof IProjectSourceLocation) {
				containers.add(new ProjectSourceContainer(((IProjectSourceLocation) locations[i]).getProject(), false));
			} else if (locations[i] instanceof IDirectorySourceLocation) {
				IDirectorySourceLocation d = (IDirectorySourceLocation) locations[i];
				IPath a = d.getAssociation();
				if (a != null) {
					MappingSourceContainer mapping = new MappingSourceContainer(InternalSourceLookupMessages.getString("SourceUtils.0") + (++mappingCount));
					mapping.addMapEntries(new MapEntrySourceContainer[] { new MapEntrySourceContainer(a, d.getDirectory()) });
					containers.add(mapping);
				}
				containers.add(new PDirectorySourceContainer(d.getDirectory(), d.searchSubfolders()));
			}
		}
		return (ISourceContainer[]) containers.toArray(new ISourceContainer[containers.size()]);
	}
}
