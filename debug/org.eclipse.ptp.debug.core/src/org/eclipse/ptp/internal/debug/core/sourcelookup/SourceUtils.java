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
package org.eclipse.ptp.internal.debug.core.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.debug.core.PDebugUtils;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SourceUtils {
	private static final String NAME_COMMON_SOURCE_LOCATIONS = "commonSourceLocations"; //$NON-NLS-1$
	private static final String NAME_SOURCE_LOCATION = "sourceLocation"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	/**
	 * @param locations
	 * @return
	 */
	public static ISourceContainer[] convertSourceLocations(IPSourceLocation[] locations) {
		ArrayList<ISourceContainer> containers = new ArrayList<ISourceContainer>(locations.length);
		for (int i = 0; i < locations.length; ++i) {
			if (locations[i] instanceof IProjectSourceLocation) {
				containers.add(new ProjectSourceContainer(((IProjectSourceLocation) locations[i]).getProject(), false));
			}
		}
		return containers.toArray(new ISourceContainer[containers.size()]);
	}

	/**
	 * @param memento
	 * @return
	 */
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
				PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0,
						Messages.SourceUtils_0, e));
			} catch (SAXException e) {
				PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0,
						Messages.SourceUtils_1, e));
			} catch (IOException e) {
				PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0,
						Messages.SourceUtils_2, e));
			}
		}
		return result;
	}

	/**
	 * @param locations
	 * @return
	 */
	public static String getCommonSourceLocationsMemento(IPSourceLocation[] locations) {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element element = document.createElement(NAME_COMMON_SOURCE_LOCATIONS);
			document.appendChild(element);
			saveSourceLocations(document, element, locations);
			return PDebugUtils.serializeDocument(document);
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		} catch (TransformerException e) {
			ex = e;
		}
		PTPDebugCorePlugin.log(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), 0, Messages.SourceUtils_3, ex));
		return null;
	}

	/**
	 * @param root
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static IPSourceLocation[] initializeSourceLocations(Element root) {
		List<IPSourceLocation> sourceLocations = new LinkedList<IPSourceLocation>();
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
						PTPDebugCorePlugin.log(Messages.SourceUtils_4);
						continue;
					}
					Class clazz = null;
					try {
						clazz = PTPDebugCorePlugin.getDefault().getBundle().loadClass(className);
					} catch (ClassNotFoundException e) {
						PTPDebugCorePlugin.log(NLS.bind(Messages.SourceUtils_5, new Object[] { className }));
						continue;
					}
					IPSourceLocation location = null;
					try {
						location = (IPSourceLocation) clazz.newInstance();
					} catch (IllegalAccessException e) {
						PTPDebugCorePlugin.log(Messages.SourceUtils_6 + e.getMessage());
						continue;
					} catch (InstantiationException e) {
						PTPDebugCorePlugin.log(Messages.SourceUtils_6 + e.getMessage());
						continue;
					}
					try {
						location.initializeFrom(data);
						sourceLocations.add(location);
					} catch (CoreException e) {
						PTPDebugCorePlugin.log(Messages.SourceUtils_6 + e.getMessage());
					}
				}
			}
		}
		return sourceLocations.toArray(new IPSourceLocation[sourceLocations.size()]);
	}

	/**
	 * @param string
	 * @return
	 */
	private static boolean isEmpty(String string) {
		return (string == null || string.trim().length() == 0);
	}

	/**
	 * @param doc
	 * @param node
	 * @param locations
	 */
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
}
