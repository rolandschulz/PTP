/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.debug.core.sourcelookup;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Container type for ResourceMappingSourceContainerType
 * 
 * @since 4.0
 */
public class ResourceMappingSourceContainerType extends AbstractSourceContainerTypeDelegate {
	private final static String ELEMENT_NAME = "mapEntry"; //$NON-NLS-1$
	private final static String MAPPING_PATH = "mappingPath"; //$NON-NLS-1$
	private final static String PROJECT_NAME = "project"; //$NON-NLS-1$
	private final static String PROJECT_PATH = "projectPath"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate#
	 * createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		Node node = parseDocument(memento);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			if (ELEMENT_NAME.equals(element.getNodeName())) {
				String path = element.getAttribute(MAPPING_PATH);
				IPath remote = new Path(path);
				if (!remote.isValidPath(path)) {
					abort(Messages.ResourceMappingSourceContainerType_0, null);
				}
				String projectName = element.getAttribute(PROJECT_NAME);
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project != null) {
					IPath local = new Path(element.getAttribute(PROJECT_PATH));
					IContainer container = project;
					if (!local.isEmpty()) {
						container = project.getFolder(local);
						if (!container.exists()) {
							abort(Messages.ResourceMappingSourceContainerType_2, null);
						}
					}
					return new ResourceMappingSourceContainer(remote, container);
				}
				abort(NLS.bind(Messages.ResourceMappingSourceContainerType_1, projectName), null);
			}
			abort(Messages.ResourceMappingSourceContainerType_3, null);
		}
		abort(Messages.ResourceMappingSourceContainerType_4, null);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate#getMemento
	 * (org.eclipse.debug.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento(ISourceContainer container) throws CoreException {
		ResourceMappingSourceContainer entry = (ResourceMappingSourceContainer) container;
		Document document = newDocument();
		Element element = document.createElement(ELEMENT_NAME);
		element.setAttribute(MAPPING_PATH, entry.getPath().toOSString());
		element.setAttribute(PROJECT_NAME, entry.getContainer().getProject().getName());
		IPath localPath = entry.getContainer().getProjectRelativePath();
		element.setAttribute(PROJECT_PATH, localPath.toOSString());
		document.appendChild(element);
		return serializeDocument(document);
	}
}
