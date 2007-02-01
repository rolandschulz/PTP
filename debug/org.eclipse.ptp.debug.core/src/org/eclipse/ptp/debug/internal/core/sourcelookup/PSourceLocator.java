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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocator;
import org.eclipse.ptp.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.SourceLookupFactory;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Clement chu
 * 
 */
public class PSourceLocator implements IPSourceLocator, IPersistableSourceLocator, IResourceChangeListener {
	private static final String SOURCE_LOCATOR_NAME = "pSourceLocator";
	private static final String DISABLED_GENERIC_PROJECT_NAME = "disabledGenericProject";
	private static final String ADDITIONAL_SOURCE_LOCATION_NAME = "additionalSourceLocation";
	private static final String SOURCE_LOCATION_NAME = "cSourceLocation";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_MEMENTO = "memento";
	private static final String ATTR_PROJECT_NAME = "projectName";
	private static final String ATTR_DUPLICATE_FILES = "duplicateFiles";
	private IProject fProject = null;
	private IPSourceLocation[] fSourceLocations;
	private List fReferencedProjects = new ArrayList(10);
	private boolean fDuplicateFiles = false;

	public PSourceLocator(IProject project) {
		setProject(project);
		setReferencedProjects();
		setSourceLocations(getDefaultSourceLocations());
	}
	public Object getSourceElement(IStackFrame stackFrame) {
		return getInput(stackFrame);
	}
	public int getLineNumber(IStackFrame frame) {
		return (frame instanceof IPStackFrame) ? ((IPStackFrame) frame).getFrameLineNumber() : 0;
	}
	protected Object getInput(IStackFrame f) {
		if (f instanceof IPStackFrame) {
			IPStackFrame frame = (IPStackFrame) f;
			LinkedList list = new LinkedList();
			if (frame != null) {
				Object result = null;
				String fileName = frame.getFile();
				if (fileName != null && fileName.length() > 0) {
					IPSourceLocation[] locations = getSourceLocations();
					for (int i = 0; i < locations.length; ++i) {
						try {
							result = locations[i].findSourceElement(fileName);
						} catch (CoreException e) {
							// do nothing
						}
						if (result != null) {
							if (result instanceof List)
								list.addAll((List) result);
							else
								list.add(result);
							if (!searchForDuplicateFiles())
								break;
						}
					}
				}
			}
			return (list.size() > 0) ? ((list.size() == 1) ? list.getFirst() : list) : null;
		}
		return null;
	}
	public boolean contains(IResource resource) {
		IPSourceLocation[] locations = getSourceLocations();
		for (int i = 0; i < locations.length; ++i) {
			if (resource instanceof IProject) {
				if (locations[i] instanceof PProjectSourceLocation && ((PProjectSourceLocation) locations[i]).getProject().equals(resource)) {
					return true;
				}
			}
			if (resource instanceof IFile) {
				try {
					Object result = locations[i].findSourceElement(resource.getLocation().toOSString());
					if (result instanceof IFile && ((IFile) result).equals(resource))
						return true;
					if (result instanceof List && ((List) result).contains(resource))
						return true;
				} catch (CoreException e) {
				}
			}
		}
		return false;
	}
	public IPSourceLocation[] getSourceLocations() {
		return fSourceLocations;
	}
	public void setSourceLocations(IPSourceLocation[] locations) {
		fSourceLocations = locations;
	}
	public static IPSourceLocation[] getDefaultSourceLocations(IProject project) {
		ArrayList list = new ArrayList();
		if (project != null && project.exists()) {
			list.add(SourceLookupFactory.createProjectSourceLocation(project));
			addReferencedSourceLocations(list, project);
		}
		return (IPSourceLocation[]) list.toArray(new IPSourceLocation[list.size()]);
	}
	private static void addReferencedSourceLocations(List list, IProject project) {
		if (project != null) {
			try {
				IProject[] projects = project.getReferencedProjects();
				for (int i = 0; i < projects.length; i++) {
					if (projects[i].exists() && !containsProject(list, projects[i])) {
						list.add(SourceLookupFactory.createProjectSourceLocation(projects[i]));
						addReferencedSourceLocations(list, projects[i]);
					}
				}
			} catch (CoreException e) {
				// do nothing
			}
		}
	}
	private static boolean containsProject(List list, IProject project) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			PProjectSourceLocation location = (PProjectSourceLocation) it.next();
			if (project.equals(location.getProject()))
				return true;
		}
		return false;
	}
	public Object findSourceElement(String fileName) {
		Object result = null;
		if (fileName != null && fileName.length() > 0) {
			IPSourceLocation[] locations = getSourceLocations();
			for (int i = 0; i < locations.length; ++i) {
				try {
					result = locations[i].findSourceElement(fileName);
				} catch (CoreException e) {
					// do nothing
				}
				if (result != null)
					break;
			}
		}
		return result;
	}
	public String getMemento() throws CoreException {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element node = document.createElement(SOURCE_LOCATOR_NAME);
			document.appendChild(node);
			IPSourceLocation[] locations = getSourceLocations();
			saveDisabledGenericSourceLocations(locations, document, node);
			saveAdditionalSourceLocations(locations, document, node);
			node.setAttribute(ATTR_DUPLICATE_FILES, new Boolean(searchForDuplicateFiles()).toString());
			return CDebugUtils.serializeDocument(document);
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		} catch (TransformerException e) {
			ex = e;
		}
		abort(InternalSourceLookupMessages.getString("PSourceLocator.0"), ex);
		// execution will not reach here
		return null;
	}
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		setSourceLocations(getDefaultSourceLocations());
	}
	public void initializeFromMemento(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
			if (!root.getNodeName().equalsIgnoreCase(SOURCE_LOCATOR_NAME)) {
				abort(InternalSourceLookupMessages.getString("PSourceLocator.1"), null);
			}
			List sourceLocations = new ArrayList();
			// Add locations based on referenced projects
			IProject project = getProject();
			if (project != null && project.exists() && project.isOpen())
				sourceLocations.addAll(Arrays.asList(getDefaultSourceLocations()));
			removeDisabledLocations(root, sourceLocations);
			addAdditionalLocations(root, sourceLocations);
			// To support old launch configuration
			addOldLocations(root, sourceLocations);
			setSourceLocations((IPSourceLocation[]) sourceLocations.toArray(new IPSourceLocation[sourceLocations.size()]));
			setSearchForDuplicateFiles(Boolean.valueOf(root.getAttribute(ATTR_DUPLICATE_FILES)).booleanValue());
			return;
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		abort(InternalSourceLookupMessages.getString("PSourceLocator.2"), ex);
	}
	private void removeDisabledLocations(Element root, List sourceLocations) {
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		HashSet disabledProjects = new HashSet(length);
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(DISABLED_GENERIC_PROJECT_NAME)) {
					String projectName = entry.getAttribute(ATTR_PROJECT_NAME);
					if (isEmpty(projectName)) {
						PTPDebugCorePlugin.log("Unable to restore C/C++ source locator - invalid format.");
					}
					disabledProjects.add(projectName.trim());
				}
			}
		}
		Iterator it = sourceLocations.iterator();
		while (it.hasNext()) {
			IPSourceLocation location = (IPSourceLocation) it.next();
			if (location instanceof IProjectSourceLocation && disabledProjects.contains(((IProjectSourceLocation) location).getProject().getName()))
				it.remove();
		}
	}
	private void addAdditionalLocations(Element root, List sourceLocations) throws CoreException {
		Bundle bundle = PTPDebugCorePlugin.getDefault().getBundle();
		MultiStatus status = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, InternalSourceLookupMessages.getString("PSourceLocator.3"), null);
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(ADDITIONAL_SOURCE_LOCATION_NAME)) {
					String className = entry.getAttribute(ATTR_CLASS);
					String data = entry.getAttribute(ATTR_MEMENTO);
					if (isEmpty(className)) {
						PTPDebugCorePlugin.log("Unable to restore C/C++ source locator - invalid format.");
						continue;
					}
					Class clazz = null;
					try {
						clazz = bundle.loadClass(className);
					} catch (ClassNotFoundException e) {
						PTPDebugCorePlugin.log(MessageFormat.format("Unable to restore source location - class not found {0}", new String[] { className }));
						continue;
					}
					IPSourceLocation location = null;
					try {
						location = (IPSourceLocation) clazz.newInstance();
					} catch (IllegalAccessException e) {
						PTPDebugCorePlugin.log("Unable to restore source location.");
						continue;
					} catch (InstantiationException e) {
						PTPDebugCorePlugin.log("Unable to restore source location.");
						continue;
					}
					try {
						location.initializeFrom(data);
						sourceLocations.add(location);
					} catch (CoreException e) {
						status.addAll(e.getStatus());
					}
				}
			}
		}
		if (status.getSeverity() > IStatus.OK)
			throw new CoreException(status);
	}
	private void addOldLocations(Element root, List sourceLocations) throws CoreException {
		Bundle bundle = PTPDebugCorePlugin.getDefault().getBundle();
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(SOURCE_LOCATION_NAME)) {
					String className = entry.getAttribute(ATTR_CLASS);
					String data = entry.getAttribute(ATTR_MEMENTO);
					if (isEmpty(className)) {
						PTPDebugCorePlugin.log("Unable to restore C/C++ source locator - invalid format.");
						continue;
					}
					Class clazz = null;
					try {
						clazz = bundle.loadClass(className);
					} catch (ClassNotFoundException e) {
						PTPDebugCorePlugin.log(MessageFormat.format("Unable to restore source location - class not found {0}", new String[] { className }));
						continue;
					}
					IPSourceLocation location = null;
					try {
						location = (IPSourceLocation) clazz.newInstance();
					} catch (IllegalAccessException e) {
						PTPDebugCorePlugin.log("Unable to restore source location.");
						continue;
					} catch (InstantiationException e) {
						PTPDebugCorePlugin.log("Unable to restore source location.");
						continue;
					}
					location.initializeFrom(data);
					if (!sourceLocations.contains(location)) {
						if (location instanceof PProjectSourceLocation)
							((PProjectSourceLocation) location).setGenerated(isReferencedProject(((PProjectSourceLocation) location).getProject()));
						sourceLocations.add(location);
					}
				}
			}
		}
	}
	private void abort(String message, Throwable e) throws CoreException {
		IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, e);
		throw new CoreException(s);
	}
	private boolean isEmpty(String string) {
		return string == null || string.trim().length() == 0;
	}
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace && event.getDelta() != null) {
			IResourceDelta[] deltas = event.getDelta().getAffectedChildren();
			if (deltas != null) {
				ArrayList list = new ArrayList(deltas.length);
				for (int i = 0; i < deltas.length; ++i)
					if (deltas[i].getResource() instanceof IProject)
						list.add(deltas[i].getResource());
				resetSourceLocations(list);
			}
		}
	}
	private void saveDisabledGenericSourceLocations(IPSourceLocation[] locations, Document doc, Element node) {
		IProject project = getProject();
		if (project != null && project.exists() && project.isOpen()) {
			List list = CDebugUtils.getReferencedProjects(project);
			HashSet names = new HashSet(list.size() + 1);
			names.add(project.getName());
			Iterator it = list.iterator();
			while (it.hasNext()) {
				names.add(((IProject) it.next()).getName());
			}
			for (int i = 0; i < locations.length; ++i)
				if (locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation) locations[i]).isGeneric())
					names.remove(((IProjectSourceLocation) locations[i]).getProject().getName());
			it = names.iterator();
			while (it.hasNext()) {
				Element child = doc.createElement(DISABLED_GENERIC_PROJECT_NAME);
				child.setAttribute(ATTR_PROJECT_NAME, (String) it.next());
				node.appendChild(child);
			}
		}
	}
	private void saveAdditionalSourceLocations(IPSourceLocation[] locations, Document doc, Element node) {
		for (int i = 0; i < locations.length; i++) {
			if (locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation) locations[i]).isGeneric())
				continue;
			Element child = doc.createElement(ADDITIONAL_SOURCE_LOCATION_NAME);
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
	public IProject getProject() {
		return fProject;
	}
	protected void setProject(IProject project) {
		fProject = project;
	}
	private boolean isReferencedProject(IProject ref) {
		if (getProject() != null) {
			try {
				return Arrays.asList(getProject().getReferencedProjects()).contains(ref);
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
		return false;
	}
	private void setReferencedProjects() {
		fReferencedProjects.clear();
		fReferencedProjects = CDebugUtils.getReferencedProjects(getProject());
	}
	protected IPSourceLocation[] getDefaultSourceLocations() {
		Iterator it = fReferencedProjects.iterator();
		ArrayList list = new ArrayList(fReferencedProjects.size());
		if (getProject() != null && getProject().exists() && getProject().isOpen())
			list.add(SourceLookupFactory.createProjectSourceLocation(getProject()));
		while (it.hasNext()) {
			IProject project = (IProject) it.next();
			if (project != null && project.exists() && project.isOpen())
				list.add(SourceLookupFactory.createProjectSourceLocation(project));
		}
		return (IPSourceLocation[]) list.toArray(new IPSourceLocation[list.size()]);
	}
	private void resetSourceLocations(List affectedProjects) {
		if (affectedProjects.size() != 0 && getProject() != null) {
			if (!getProject().exists() || !getProject().isOpen()) {
				removeGenericSourceLocations();
			} else {
				updateGenericSourceLocations(affectedProjects);
			}
		}
	}
	private void removeGenericSourceLocations() {
		fReferencedProjects.clear();
		IPSourceLocation[] locations = getSourceLocations();
		ArrayList newLocations = new ArrayList(locations.length);
		for (int i = 0; i < locations.length; ++i)
			if (!(locations[i] instanceof IProjectSourceLocation) || !((IProjectSourceLocation) locations[i]).isGeneric())
				newLocations.add(locations[i]);
		setSourceLocations((IPSourceLocation[]) newLocations.toArray(new IPSourceLocation[newLocations.size()]));
	}
	private void updateGenericSourceLocations(List affectedProjects) {
		List newRefs = CDebugUtils.getReferencedProjects(getProject());
		IPSourceLocation[] locations = getSourceLocations();
		ArrayList newLocations = new ArrayList(locations.length);
		for (int i = 0; i < locations.length; ++i) {
			if (!(locations[i] instanceof IProjectSourceLocation) || !((IProjectSourceLocation) locations[i]).isGeneric()) {
				newLocations.add(locations[i]);
			} else {
				IProject project = ((IProjectSourceLocation) locations[i]).getProject();
				if (project.exists() && project.isOpen()) {
					if (newRefs.contains(project) || project.equals(getProject())) {
						newLocations.add(locations[i]);
						newRefs.remove(project);
					}
				}
			}
		}
		Iterator it = newRefs.iterator();
		while (it.hasNext()) {
			IProject project = (IProject) it.next();
			if (!fReferencedProjects.contains(project))
				newLocations.add(SourceLookupFactory.createProjectSourceLocation(project));
		}
		fReferencedProjects = newRefs;
		setSourceLocations((IPSourceLocation[]) newLocations.toArray(new IPSourceLocation[newLocations.size()]));
	}
	public boolean searchForDuplicateFiles() {
		return fDuplicateFiles;
	}
	public void setSearchForDuplicateFiles(boolean search) {
		fDuplicateFiles = search;
		IPSourceLocation[] locations = getSourceLocations();
		for (int i = 0; i < locations.length; ++i)
			locations[i].setSearchForDuplicateFiles(search);
	}
}
