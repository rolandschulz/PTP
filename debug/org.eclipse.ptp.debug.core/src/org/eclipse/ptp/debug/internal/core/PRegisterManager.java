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
package org.eclipse.ptp.debug.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PRegisterDescriptor;
import org.eclipse.ptp.debug.internal.core.model.PRegisterGroup;
import org.eclipse.ptp.debug.internal.core.model.PStackFrame;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author clement
 *
 */
public class PRegisterManager implements IAdaptable {
	private class PRegisterSet {
		List<IRegisterGroup> fRegisterGroups;
		IPRegisterDescriptor[] fRegisterDescriptors;
		boolean fUseDefaultRegisterGroups = true;
		PStackFrame fCurrentFrame;
		PDebugTarget debugTarget = null;
		BitList rTasks;
		
		PRegisterSet(BitList rTasks, PDebugTarget debugTarget) {
			this.rTasks = rTasks;
			this.debugTarget = debugTarget;
		}
		PDebugTarget getDebugTarget() {
			if (debugTarget == null)
				debugTarget = session.findDebugTarget(rTasks);
			return debugTarget;
		}
		void dispose() {
			synchronized (fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while( it.hasNext() ) {
					((PRegisterGroup)it.next()).dispose();
				}
				fRegisterGroups.clear();
			}
		}
		void initialize() {
			IPDIRegisterGroup[] groups = new IPDIRegisterGroup[0];
			try {
				groups = getDebugTarget().getPDITarget().getRegisterGroups();
			}
			catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
			List<IPRegisterDescriptor> list = new ArrayList<IPRegisterDescriptor>();
			for( int i = 0; i < groups.length; ++i ) {
				try {
					IPDIRegisterDescriptor[] pdiDescriptors = groups[i].getRegisterDescriptors();
					for (int j = 0; j < pdiDescriptors.length; ++j) {
						list.add(new PRegisterDescriptor(groups[i], pdiDescriptors[j]));
					}
				}
				catch (PDIException e) {
					PTPDebugCorePlugin.log(e);
				}
			}
			fRegisterDescriptors = (IPRegisterDescriptor[])list.toArray(new IPRegisterDescriptor[0]);
			createRegisterGroups();
		}		
		IPRegisterDescriptor[] getAllRegisterDescriptors() throws DebugException {
			return fRegisterDescriptors;
		}
		IRegisterGroup[] getRegisterGroups(PStackFrame frame) throws DebugException {
			IRegisterGroup[] groups = (IRegisterGroup[])fRegisterGroups.toArray(new IRegisterGroup[0]);
			if (getCurrentFrame() != frame) {
				for (int i = 0; i < groups.length; ++i) {
					((PRegisterGroup)groups[i]).resetRegisterValues();
				}
				setCurrentFrame(frame);
			}
			return groups;
		}
		void addRegisterGroup(String name, IPRegisterDescriptor[] descriptors) {
			fRegisterGroups.add(new PRegisterGroup(session, rTasks, name, descriptors));
			setUseDefaultRegisterGroups(false);
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}
		void removeAllRegisterGroups() {
			synchronized(fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while(it.hasNext()) {
					((PRegisterGroup)it.next()).dispose();
				}
				fRegisterGroups.clear();
			}
			setUseDefaultRegisterGroups(false);
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}
		void removeRegisterGroups(IRegisterGroup[] groups) {
			for (int i = 0; i < groups.length; ++i) {
				((PRegisterGroup)groups[i]).dispose();
			}
			fRegisterGroups.removeAll(Arrays.asList(groups));
			setUseDefaultRegisterGroups(false);
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}
		void restoreDefaults() {
			synchronized(fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while(it.hasNext()) {
					((PRegisterGroup)it.next()).dispose();
				}
				fRegisterGroups.clear();
				initializeDefaults();
			}
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}
		boolean useDefaultRegisterGroups() {
			return fUseDefaultRegisterGroups;
		}
		void setUseDefaultRegisterGroups(boolean useDefaultRegisterGroups) {
			fUseDefaultRegisterGroups = useDefaultRegisterGroups;
		}
		PStackFrame getCurrentFrame() {
			return fCurrentFrame;
		}
		void setCurrentFrame(PStackFrame currentFrame) {
			fCurrentFrame = currentFrame;
		}
		private void createRegisterGroups() {
			fRegisterGroups = Collections.synchronizedList(new ArrayList<IRegisterGroup>(20));
			ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			try {
				String memento = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS, "");
				if (memento != null && memento.length() > 0) {
					initializeFromMemento(memento);
					return;
				}
			}
			catch(CoreException e) {
			}
			initializeDefaults();
		}
		private void initializeFromMemento(String memento) throws CoreException {
			Node node = DebugPlugin.parseDocument(memento);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				abort(InternalDebugCoreMessages.getString("PRegisterManager.0"), null);
			}
			Element element = (Element)node;
			if (!ELEMENT_REGISTER_GROUP_LIST.equals(element.getNodeName())) {
				abort(InternalDebugCoreMessages.getString("PRegisterManager.1"), null);
			}
			Node childNode = element.getFirstChild();
			while(childNode != null) {
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element)childNode;
					if (ELEMENT_REGISTER_GROUP.equals(child.getNodeName())) {
						String groupMemento = child.getAttribute(ATTR_REGISTER_GROUP_MEMENTO);
						PRegisterGroup group = new PRegisterGroup(session, rTasks);
						try {
							group.initializeFromMemento(groupMemento);
							doAddRegisterGroup(group);
						}
						catch(CoreException e) {
							// skip this group
						}
					}
				}
				childNode = childNode.getNextSibling();
			}
			setUseDefaultRegisterGroups(false);
		}
		void initializeDefaults() {
			setUseDefaultRegisterGroups(true);
			String current = null;
			int startIndex = 0;
			for (int i = 0; i < fRegisterDescriptors.length; ++i) {
				PRegisterDescriptor d = (PRegisterDescriptor)fRegisterDescriptors[i];
				if ( current != null && d.getGroupName().compareTo(current) != 0) {
					IPRegisterDescriptor[] descriptors = new IPRegisterDescriptor[i - startIndex];
					System.arraycopy(fRegisterDescriptors, startIndex, descriptors, 0, descriptors.length);
					fRegisterGroups.add(new PRegisterGroup(session, rTasks, current, descriptors));
					startIndex = i;
				}
				current = d.getGroupName();
			}
			if (startIndex < fRegisterDescriptors.length) {
				IPRegisterDescriptor[] descriptors = new IPRegisterDescriptor[fRegisterDescriptors.length - startIndex];
				System.arraycopy(fRegisterDescriptors, startIndex, descriptors, 0, descriptors.length);
				fRegisterGroups.add(new PRegisterGroup(session, rTasks, current, descriptors));
			}
		}
		synchronized void doAddRegisterGroup(IRegisterGroup group) {
			fRegisterGroups.add(group);
		}
		String getMemento() throws CoreException {
			if (useDefaultRegisterGroups() || fRegisterGroups == null)
				return "";
			Document document = DebugPlugin.newDocument();
			Element element = document.createElement(ELEMENT_REGISTER_GROUP_LIST);
			Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
			while(it.hasNext()) {
				PRegisterGroup group = (PRegisterGroup)it.next();
				Element child = document.createElement(ELEMENT_REGISTER_GROUP);
				child.setAttribute(ATTR_REGISTER_GROUP_MEMENTO, group.getMemento());
				element.appendChild(child);			
			}
			document.appendChild(element);
			return DebugPlugin.serializeDocument(document);
		}
		IPRegisterDescriptor findDescriptor(String groupName, String name) {
			for (int i = 0; i < fRegisterDescriptors.length; ++i) {
				IPRegisterDescriptor d = fRegisterDescriptors[i];
				if (groupName.equals(d.getGroupName()) && name.equals(d.getName()))
					return d;
			}
			return null;
		}
		void modifyRegisterGroup(IPPersistableRegisterGroup group, IPRegisterDescriptor[] descriptors) {
			group.setRegisterDescriptors(descriptors);					
			((PRegisterGroup)group).fireChangeEvent(DebugEvent.CONTENT);
		}
		void targetSuspended() {
			Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
			while(it.hasNext()) {
				((PRegisterGroup)it.next()).targetSuspended();
			}
		}
		void abort(String message, Throwable exception) throws CoreException {
			IStatus status = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, exception);
			throw new CoreException(status);
		}
	}
	private static final String ELEMENT_REGISTER_GROUP_LIST = "registerGroups";
	private static final String ELEMENT_REGISTER_GROUP = "group";
	private static final String ATTR_REGISTER_GROUP_MEMENTO = "memento";

	protected Map<BitList, PRegisterSet> fRegisterSetMap;
	private PSession session;

	public PRegisterManager(PSession session) {
		this.session = session;
	}
	public void initialize(IProgressMonitor monitor) {
		fRegisterSetMap = new Hashtable<BitList, PRegisterSet>();
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPSession.class))
			return getSession();
		if (adapter.equals(PRegisterManager.class))
			return this;
		return null;
	}
	public void dispose(IProgressMonitor monitor) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				synchronized(fRegisterSetMap) {
					Iterator<PRegisterSet> it = fRegisterSetMap.values().iterator();
					while(it.hasNext()) {
						((PRegisterSet)it.next()).dispose();
					}
					fRegisterSetMap.clear();
				}
			}
		});
	}
	public PRegisterSet getRegisterSet(BitList qTasks) {
		synchronized (fRegisterSetMap) {
			PRegisterSet set = (PRegisterSet)fRegisterSetMap.get(qTasks);
			if (set == null) {
				set = new PRegisterSet(qTasks, null);
				fRegisterSetMap.put(qTasks, set);
			}
			return set;
		}
	}
	public IPRegisterDescriptor[] getAllRegisterDescriptors(BitList qTasks) throws DebugException {
		return getRegisterSet(qTasks).getAllRegisterDescriptors();
	}
	public IRegisterGroup[] getRegisterGroups(BitList qTasks, PStackFrame frame) throws DebugException {
		return getRegisterSet(qTasks).getRegisterGroups(frame);
	}
	public void initialize(BitList qTasks, PDebugTarget debugTarget) {
		synchronized (fRegisterSetMap) {
			PRegisterSet set = new PRegisterSet(qTasks, debugTarget);
			fRegisterSetMap.put(qTasks, set);
			set.initialize();
		}
	}
	public void addRegisterGroup(final BitList qTasks, final String name, final IPRegisterDescriptor[] descriptors) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).addRegisterGroup(name, descriptors);
			}
		});
	}
	public void removeAllRegisterGroups(final BitList qTasks) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).removeAllRegisterGroups();
			}
		});
	}
	public void removeRegisterGroups(final BitList qTasks, final IRegisterGroup[] groups) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).removeRegisterGroups(groups);
			}
		});
	}
	public void restoreDefaults(final BitList qTasks) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).restoreDefaults();
			}
		});
	}
	public void targetSuspended(BitList qTasks) {
		getRegisterSet(qTasks).targetSuspended();
	}
	protected IPSession getSession() {
		return session;
	}
	public void save(BitList qTasks) {
		try {
			String memto = getRegisterSet(qTasks).getMemento();
			ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS, memto);
			wc.doSave();
		}
		catch(CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}
	public IPRegisterDescriptor findDescriptor(BitList qTasks, String groupName, String name) {
		return getRegisterSet(qTasks).findDescriptor(groupName, name);
	}
	public void modifyRegisterGroup(final BitList qTasks, final IPPersistableRegisterGroup group, final IPRegisterDescriptor[] descriptors) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).modifyRegisterGroup(group, descriptors);
			}
		});
	}
	public PStackFrame getCurrentFrame(BitList qTasks) {
		return getRegisterSet(qTasks).getCurrentFrame();
	}

}
