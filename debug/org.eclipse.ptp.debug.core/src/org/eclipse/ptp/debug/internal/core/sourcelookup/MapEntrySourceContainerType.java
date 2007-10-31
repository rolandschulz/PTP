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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author clement
 *
 */
public class MapEntrySourceContainerType extends AbstractSourceContainerTypeDelegate {
	private final static String ELEMENT_NAME = "mapEntry";
	private final static String BACKEND_PATH = "backendPath";
	private final static String LOCAL_PATH = "localPath";

	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			if (ELEMENT_NAME.equals(element.getNodeName())) {
				String path = element.getAttribute(BACKEND_PATH);
				IPath backend = new Path(path);
				if (!backend.isValidPath(path)) {
					abort(InternalSourceLookupMessages.getString("MapEntrySourceContainerType.0"), null);
				}
				path = element.getAttribute(LOCAL_PATH);
				IPath local = new Path(path);
				if (!local.isValidPath(path)) {
					abort(InternalSourceLookupMessages.getString("MapEntrySourceContainerType.1"), null);
				}
				return new MapEntrySourceContainer(backend, local);
			}
			abort(InternalSourceLookupMessages.getString("MapEntrySourceContainerType.2"), null);
		}
		abort(InternalSourceLookupMessages.getString("MapEntrySourceContainerType.3"), null);
		return null;
	}
	public String getMemento(ISourceContainer container) throws CoreException {
		MapEntrySourceContainer entry = (MapEntrySourceContainer)container;
		Document document = newDocument();
		Element element = document.createElement(ELEMENT_NAME);
		element.setAttribute(BACKEND_PATH, entry.getBackendPath().toOSString());
		element.setAttribute(LOCAL_PATH, entry.getLocalPath().toOSString());
		document.appendChild(element);
		return serializeDocument(document);
	}
}
