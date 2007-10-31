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
package org.eclipse.ptp.debug.internal.core.model;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IEnableDisableTarget;
import org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.internal.core.PRegisterManager;
import org.eclipse.ptp.debug.internal.core.PSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author clement
 *
 */
public class PRegisterGroup extends PDebugElement implements IPPersistableRegisterGroup, IEnableDisableTarget {
	private static final String ELEMENT_REGISTER_GROUP = "registerGroup";
	private static final String ATTR_REGISTER_GROUP_NAME = "name";
	private static final String ATTR_REGISTER_GROUP_ENABLED = "enabled";
	private static final String ELEMENT_REGISTER = "register";
	private static final String ATTR_REGISTER_NAME = "name";
	private static final String ATTR_REGISTER_ORIGINAL_GROUP_NAME = "originalGroupName";

	private String fName;
	private IPRegisterDescriptor[] fRegisterDescriptors;
	private IRegister[] fRegisters;
	private boolean fIsEnabled = true;
	private boolean fDisposed = false;

	public PRegisterGroup(PSession session, BitList tasks) {
		this(session, tasks, null, null);
	}
	public PRegisterGroup(PSession session, BitList tasks, String name, IPRegisterDescriptor[] descriptors) {
		super(session, tasks);
		fName = name;
		fRegisterDescriptors = descriptors;
	}
	public String getName() throws DebugException {
		return fName;
	}
	public IRegister[] getRegisters() throws DebugException {
		if (fDisposed)
			return new IRegister[0];
		if (fRegisters == null) {
			synchronized(this) {
				if (fRegisters == null) {
					fRegisters = new IRegister[fRegisterDescriptors.length];
					for(int i = 0; i < fRegisters.length; ++i) {
						fRegisters[i] = new PRegister(this, fRegisterDescriptors[i]);
					}
				}
			}
		}
		return fRegisters;
	}

	public boolean hasRegisters() throws DebugException {
		return (fRegisterDescriptors.length > 0);
	}
	public void dispose() {
		fDisposed = true;
		invalidate();
	}
	public void targetSuspended() {
		if (fRegisters == null) {
			return;
		}
		for (int i = 0; i < fRegisters.length; ++i) {
			if (fRegisters[i] != null && ((PRegister)fRegisters[i]).hasErrors()) {
				((PRegister)fRegisters[i]).resetStatus();
			}
		}
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IEnableDisableTarget.class))
			return this;
		return super.getAdapter(adapter);
	}
	public boolean canEnableDisable() {
		return true;
	}
	public boolean isEnabled() {
		return fIsEnabled;
	}
	public void setEnabled(boolean enabled) throws DebugException {
		if (fRegisters != null) {
			synchronized(fRegisters) {
				if (fRegisters != null) {
					for (int i = 0; i < fRegisters.length; ++i) {
						if (fRegisters[i] instanceof PRegister) {
							((PRegister)fRegisters[i]).setEnabled(enabled);
						}
					}
				}
			}
		}
		fIsEnabled = enabled;
		fireChangeEvent(DebugEvent.CONTENT);
	}
	public String getMemento() throws CoreException {
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement(ELEMENT_REGISTER_GROUP);
		element.setAttribute(ATTR_REGISTER_GROUP_NAME, getName());
		element.setAttribute(ATTR_REGISTER_GROUP_ENABLED, Boolean.valueOf(isEnabled()).toString());
		for (int i = 0; i < fRegisterDescriptors.length; ++i) {
			Element child = document.createElement(ELEMENT_REGISTER);
			child.setAttribute(ATTR_REGISTER_NAME, fRegisterDescriptors[i].getName());
			child.setAttribute(ATTR_REGISTER_ORIGINAL_GROUP_NAME, fRegisterDescriptors[i].getGroupName());
			element.appendChild(child);
		}
		document.appendChild(element);
		return DebugPlugin.serializeDocument(document);
	}
	public void initializeFromMemento(String memento) throws CoreException {
		Node node = DebugPlugin.parseDocument(memento);
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			abort(CoreModelMessages.getString("PRegisterGroup.0"), null);
		}
		Element element = (Element)node;
		if (!ELEMENT_REGISTER_GROUP.equals(element.getNodeName())) {
			abort(CoreModelMessages.getString("PRegisterGroup.1"), null);
		}
		String groupName = element.getAttribute(ATTR_REGISTER_GROUP_NAME);
		if (groupName == null || groupName.length() == 0) {
			abort(CoreModelMessages.getString("PRegisterGroup.2"), null);
		}
		String e = element.getAttribute(ATTR_REGISTER_GROUP_ENABLED);
		boolean enabled = Boolean.valueOf(e).booleanValue();
		PRegisterManager rm = getRegisterManager();
		ArrayList<IPRegisterDescriptor> list = new ArrayList<IPRegisterDescriptor>();
		Node childNode = element.getFirstChild();
		while(childNode != null) {
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element)childNode;
				if (ELEMENT_REGISTER.equals(child.getNodeName())) {
					String name = child.getAttribute(ATTR_REGISTER_NAME);
					String originalGroupName = child.getAttribute(ATTR_REGISTER_ORIGINAL_GROUP_NAME);
					if (name == null || name.length() == 0 || originalGroupName == null || originalGroupName.length() == 0) {
						abort(CoreModelMessages.getString("PRegisterGroup.3"), null);
					}
					else {
						IPRegisterDescriptor d = rm.findDescriptor(getTasks(), originalGroupName, name);
						if (d != null)
							list.add(d);
						else
							PTPDebugCorePlugin.log(CoreModelMessages.getString("PRegisterGroup.4"));
					}
				}
			}
			childNode = childNode.getNextSibling();
		}
		setName(groupName);
		fRegisterDescriptors = (IPRegisterDescriptor[])list.toArray(new IPRegisterDescriptor[list.size()]);
		setEnabled(enabled);
	}
	private void abort(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, exception);
		throw new CoreException(status);
	}
	private void setName(String name) {
		fName = name;
	}
	public void setRegisterDescriptors(IPRegisterDescriptor[] registerDescriptors) {
		invalidate();
		fRegisterDescriptors = registerDescriptors;
	}
	public IPRegisterDescriptor[] getRegisterDescriptors() {
		return fRegisterDescriptors;
	}
	private PRegisterManager getRegisterManager() {
		return (PRegisterManager)getSession().getAdapter(PRegisterManager.class);
	}
	private void invalidate() {
		if (fRegisters == null) {
			return;
		}
		for (int i = 0; i < fRegisters.length; ++i) {
			if (fRegisters[i] != null) {
				((PRegister)fRegisters[i]).dispose();
			}
		}
		fRegisters = null;
	}
	public void resetRegisterValues() {
		if (fRegisters == null) {
			return;
		}
		for (int i = 0; i < fRegisters.length; ++i) {
			if (fRegisters[i] != null) {
				((PRegister)fRegisters[i]).invalidateValue();
			}
		}
	}
}
