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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.ptp.debug.core.sourcelookup.MappingSourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author clement
 *
 */
public class MappingSourceContainerType extends AbstractSourceContainerTypeDelegate {
	private final static String ELEMENT_MAPPING = "mapping";
	private final static String ELEMENT_MAP_ENTRY = "mapEntry";
	private final static String ATTR_NAME = "name";
	private final static String ATTR_MEMENTO = "memento";

	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if (ELEMENT_MAPPING.equals(element.getNodeName())) {
				String name = element.getAttribute(ATTR_NAME);
				if (name == null) 
					name = "";
				List<MapEntrySourceContainer> entries = new ArrayList<MapEntrySourceContainer>();
				Node childNode = element.getFirstChild();
				while(childNode != null) {
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element child = (Element)childNode;
						if (ELEMENT_MAP_ENTRY.equals(child.getNodeName())) {
							String childMemento = child.getAttribute(ATTR_MEMENTO);
							if (childMemento == null || childMemento.length() == 0) {
								abort(InternalSourceLookupMessages.getString("MappingSourceContainerType.0"), null);
							}
							ISourceContainerType type = DebugPlugin.getDefault().getLaunchManager().getSourceContainerType(MapEntrySourceContainer.TYPE_ID);
							MapEntrySourceContainer entry = (MapEntrySourceContainer)type.createSourceContainer(childMemento);
							entries.add(entry);
						}
					}
					childNode = childNode.getNextSibling();
				}
				MappingSourceContainer container = new MappingSourceContainer(name);
				Iterator<MapEntrySourceContainer> it = entries.iterator();
				while(it.hasNext()) {
					container.addMapEntry((MapEntrySourceContainer)it.next());
				}
				return container;
			}
			abort(InternalSourceLookupMessages.getString("MappingSourceContainerType.1"), null);
		}
		abort(InternalSourceLookupMessages.getString("MappingSourceContainerType.2"), null);
		return null;		
	}
	public String getMemento(ISourceContainer container) throws CoreException {
		Document document = newDocument();
		Element element = document.createElement(ELEMENT_MAPPING);
		element.setAttribute(ATTR_NAME, container.getName());
		ISourceContainer[] entries = ((MappingSourceContainer)container).getSourceContainers();
		for (int i = 0; i < entries.length; ++i) {
			Element child = document.createElement(ELEMENT_MAP_ENTRY);
			child.setAttribute(ATTR_MEMENTO, entries[i].getType().getMemento(entries[i]));
			element.appendChild(child);
		}
		document.appendChild(element);
		return serializeDocument(document);
	}
}
