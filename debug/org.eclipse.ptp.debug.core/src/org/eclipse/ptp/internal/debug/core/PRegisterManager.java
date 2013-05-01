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
package org.eclipse.ptp.internal.debug.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.eclipse.ptp.debug.core.IPRegisterManager;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.model.PRegisterDescriptor;
import org.eclipse.ptp.internal.debug.core.model.PRegisterGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author clement
 * 
 */
public class PRegisterManager implements IAdaptable, IPRegisterManager {
	private class PRegisterSet {
		private final List<IRegisterGroup> fRegisterGroups = Collections.synchronizedList(new ArrayList<IRegisterGroup>(20));
		private IPRegisterDescriptor[] fRegisterDescriptors;
		private boolean fUseDefaultRegisterGroups = true;
		private IPStackFrame fCurrentFrame;
		private IPDebugTarget debugTarget = null;
		private final TaskSet rTasks;

		public PRegisterSet(TaskSet rTasks, IPDebugTarget debugTarget) {
			this.rTasks = rTasks;
			this.debugTarget = debugTarget;
		}

		/**
		 * @param message
		 * @param exception
		 * @throws CoreException
		 */
		public void abort(String message, Throwable exception) throws CoreException {
			IStatus status = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR,
					message, exception);
			throw new CoreException(status);
		}

		/**
		 * @param name
		 * @param descriptors
		 */
		public void addRegisterGroup(String name, IPRegisterDescriptor[] descriptors) {
			fRegisterGroups.add(new PRegisterGroup(session, rTasks, name, descriptors));
			setUseDefaultRegisterGroups(false);
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}

		/**
		 * 
		 */
		public void dispose() {
			synchronized (fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while (it.hasNext()) {
					((PRegisterGroup) it.next()).dispose();
				}
				fRegisterGroups.clear();
			}
		}

		/**
		 * @param group
		 */
		public void doAddRegisterGroup(IRegisterGroup group) {
			fRegisterGroups.add(group);
		}

		/**
		 * @param groupName
		 * @param name
		 * @return
		 */
		public IPRegisterDescriptor findDescriptor(String groupName, String name) {
			for (int i = 0; i < fRegisterDescriptors.length; ++i) {
				IPRegisterDescriptor d = fRegisterDescriptors[i];
				if (groupName.equals(d.getGroupName()) && name.equals(d.getName()))
					return d;
			}
			return null;
		}

		/**
		 * @return
		 * @throws DebugException
		 */
		public IPRegisterDescriptor[] getAllRegisterDescriptors() throws DebugException {
			return fRegisterDescriptors;
		}

		/**
		 * @return
		 */
		public IPStackFrame getCurrentFrame() {
			return fCurrentFrame;
		}

		/**
		 * @return
		 */
		public IPDebugTarget getDebugTarget() {
			if (debugTarget == null) {
				debugTarget = session.findDebugTarget(rTasks);
			}
			return debugTarget;
		}

		/**
		 * @return
		 * @throws CoreException
		 */
		public String getMemento() throws CoreException {
			synchronized (fRegisterGroups) {
				if (useDefaultRegisterGroups() || fRegisterGroups == null) {
					return ""; //$NON-NLS-1$
				}
				Document document = DebugPlugin.newDocument();
				Element element = document.createElement(ELEMENT_REGISTER_GROUP_LIST);
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while (it.hasNext()) {
					PRegisterGroup group = (PRegisterGroup) it.next();
					Element child = document.createElement(ELEMENT_REGISTER_GROUP);
					child.setAttribute(ATTR_REGISTER_GROUP_MEMENTO, group.getMemento());
					element.appendChild(child);
				}
				document.appendChild(element);
				return DebugPlugin.serializeDocument(document);
			}
		}

		/**
		 * @param frame
		 * @return
		 * @throws DebugException
		 */
		public IRegisterGroup[] getRegisterGroups(IPStackFrame frame) throws DebugException {
			IRegisterGroup[] groups = fRegisterGroups.toArray(new IRegisterGroup[0]);
			if (getCurrentFrame() != frame) {
				for (int i = 0; i < groups.length; ++i) {
					((PRegisterGroup) groups[i]).resetRegisterValues();
				}
				setCurrentFrame(frame);
			}
			return groups;
		}

		/**
		 * 
		 */
		public void initialize() {
			IPDIRegisterGroup[] groups = new IPDIRegisterGroup[0];
			try {
				groups = getDebugTarget().getPDITarget().getRegisterGroups();
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
			List<IPRegisterDescriptor> list = new ArrayList<IPRegisterDescriptor>();
			for (int i = 0; i < groups.length; ++i) {
				try {
					IPDIRegisterDescriptor[] pdiDescriptors = groups[i].getRegisterDescriptors();
					for (int j = 0; j < pdiDescriptors.length; ++j) {
						list.add(new PRegisterDescriptor(groups[i], pdiDescriptors[j]));
					}
				} catch (PDIException e) {
					PTPDebugCorePlugin.log(e);
				}
			}
			fRegisterDescriptors = list.toArray(new IPRegisterDescriptor[0]);
			createRegisterGroups();
		}

		/**
		 * 
		 */
		public void initializeDefaults() {
			setUseDefaultRegisterGroups(true);
			String current = null;
			int startIndex = 0;
			for (int i = 0; i < fRegisterDescriptors.length; ++i) {
				PRegisterDescriptor d = (PRegisterDescriptor) fRegisterDescriptors[i];
				if (current != null && d.getGroupName().compareTo(current) != 0) {
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

		/**
		 * @param group
		 * @param descriptors
		 */
		public void modifyRegisterGroup(IPPersistableRegisterGroup group, IPRegisterDescriptor[] descriptors) {
			group.setRegisterDescriptors(descriptors);
			((PRegisterGroup) group).fireChangeEvent(DebugEvent.CONTENT);
		}

		/**
		 * 
		 */
		public void removeAllRegisterGroups() {
			synchronized (fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while (it.hasNext()) {
					((PRegisterGroup) it.next()).dispose();
				}
				fRegisterGroups.clear();
			}
			setUseDefaultRegisterGroups(false);
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}

		/**
		 * @param groups
		 */
		public void removeRegisterGroups(IRegisterGroup[] groups) {
			for (int i = 0; i < groups.length; ++i) {
				((PRegisterGroup) groups[i]).dispose();
			}
			fRegisterGroups.removeAll(Arrays.asList(groups));
			setUseDefaultRegisterGroups(false);
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}

		/**
		 * 
		 */
		public void restoreDefaults() {
			synchronized (fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while (it.hasNext()) {
					((PRegisterGroup) it.next()).dispose();
				}
				fRegisterGroups.clear();
				initializeDefaults();
			}
			getDebugTarget().fireChangeEvent(DebugEvent.CONTENT);
		}

		/**
		 * @param currentFrame
		 */
		public void setCurrentFrame(IPStackFrame currentFrame) {
			fCurrentFrame = currentFrame;
		}

		/**
		 * @param useDefaultRegisterGroups
		 */
		public void setUseDefaultRegisterGroups(boolean useDefaultRegisterGroups) {
			fUseDefaultRegisterGroups = useDefaultRegisterGroups;
		}

		/**
		 * 
		 */
		public void targetSuspended() {
			synchronized (fRegisterGroups) {
				Iterator<IRegisterGroup> it = fRegisterGroups.iterator();
				while (it.hasNext()) {
					((PRegisterGroup) it.next()).targetSuspended();
				}
			}
		}

		/**
		 * @return
		 */
		public boolean useDefaultRegisterGroups() {
			return fUseDefaultRegisterGroups;
		}

		/**
		 * 
		 */
		private void createRegisterGroups() {
			ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			try {
				String memento = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS, ""); //$NON-NLS-1$
				if (memento != null && memento.length() > 0) {
					initializeFromMemento(memento);
					return;
				}
			} catch (CoreException e) {
			}
			initializeDefaults();
		}

		/**
		 * @param memento
		 * @throws CoreException
		 */
		private void initializeFromMemento(String memento) throws CoreException {
			Node node = DebugPlugin.parseDocument(memento);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				abort(Messages.PRegisterManager_2, null);
			}
			Element element = (Element) node;
			if (!ELEMENT_REGISTER_GROUP_LIST.equals(element.getNodeName())) {
				abort(Messages.PRegisterManager_3, null);
			}
			Node childNode = element.getFirstChild();
			while (childNode != null) {
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element) childNode;
					if (ELEMENT_REGISTER_GROUP.equals(child.getNodeName())) {
						String groupMemento = child.getAttribute(ATTR_REGISTER_GROUP_MEMENTO);
						PRegisterGroup group = new PRegisterGroup(session, rTasks);
						try {
							group.initializeFromMemento(groupMemento);
							doAddRegisterGroup(group);
						} catch (CoreException e) {
							// skip this group
						}
					}
				}
				childNode = childNode.getNextSibling();
			}
			setUseDefaultRegisterGroups(false);
		}
	}

	private static final String ELEMENT_REGISTER_GROUP_LIST = "registerGroups"; //$NON-NLS-1$
	private static final String ELEMENT_REGISTER_GROUP = "group"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_MEMENTO = "memento"; //$NON-NLS-1$

	private final IPSession session;
	protected final Map<TaskSet, PRegisterSet> fRegisterSetMap = new HashMap<TaskSet, PRegisterSet>();

	public PRegisterManager(IPSession session) {
		this.session = session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#addRegisterGroup
	 * (org.eclipse.ptp.core.util.TaskSet, java.lang.String,
	 * org.eclipse.ptp.debug.core.model.IPRegisterDescriptor[])
	 */
	public void addRegisterGroup(final TaskSet qTasks, final String name, final IPRegisterDescriptor[] descriptors) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				getRegisterSet(qTasks).addRegisterGroup(name, descriptors);
			}
		});
	}

	/**
	 * @param monitor
	 */
	public void dispose(IProgressMonitor monitor) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				synchronized (fRegisterSetMap) {
					Iterator<PRegisterSet> it = fRegisterSetMap.values().iterator();
					while (it.hasNext()) {
						(it.next()).dispose();
					}
					fRegisterSetMap.clear();
				}
			}
		});
	}

	/**
	 * @param qTasks
	 * @param groupName
	 * @param name
	 * @return
	 */
	public IPRegisterDescriptor findDescriptor(TaskSet qTasks, String groupName, String name) {
		return getRegisterSet(qTasks).findDescriptor(groupName, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPSession.class))
			return getSession();
		if (adapter.equals(PRegisterManager.class))
			return this;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.IPRegisterManager#
	 * getAllRegisterDescriptors(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPRegisterDescriptor[] getAllRegisterDescriptors(TaskSet qTasks) throws DebugException {
		return getRegisterSet(qTasks).getAllRegisterDescriptors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#getCurrentFrame
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPStackFrame getCurrentFrame(TaskSet qTasks) {
		return getRegisterSet(qTasks).getCurrentFrame();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#getRegisterGroups
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.internal.debug.core.model.PStackFrame)
	 */
	public IRegisterGroup[] getRegisterGroups(TaskSet qTasks, IPStackFrame frame) throws DebugException {
		return getRegisterSet(qTasks).getRegisterGroups(frame);
	}

	/**
	 * @param qTasks
	 * @return
	 */
	public PRegisterSet getRegisterSet(TaskSet qTasks) {
		synchronized (fRegisterSetMap) {
			PRegisterSet set = fRegisterSetMap.get(qTasks);
			if (set == null) {
				set = new PRegisterSet(qTasks, null);
				fRegisterSetMap.put(qTasks, set);
			}
			return set;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#initialize(org.
	 * eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.internal.debug.core.model.PDebugTarget)
	 */
	public void initialize(TaskSet qTasks, IPDebugTarget debugTarget) {
		synchronized (fRegisterSetMap) {
			PRegisterSet set = new PRegisterSet(qTasks, debugTarget);
			fRegisterSetMap.put(qTasks, set);
			set.initialize();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#modifyRegisterGroup
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup,
	 * org.eclipse.ptp.debug.core.model.IPRegisterDescriptor[])
	 */
	public void modifyRegisterGroup(final TaskSet qTasks, final IPPersistableRegisterGroup group,
			final IPRegisterDescriptor[] descriptors) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).modifyRegisterGroup(group, descriptors);
			}
		});
	}

	/**
	 * @param qTasks
	 */
	public void removeAllRegisterGroups(final TaskSet qTasks) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).removeAllRegisterGroups();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#removeRegisterGroups
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.debug.core.model.IRegisterGroup[])
	 */
	public void removeRegisterGroups(final TaskSet qTasks, final IRegisterGroup[] groups) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				getRegisterSet(qTasks).removeRegisterGroups(groups);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#restoreDefaults
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	public void restoreDefaults(final TaskSet qTasks) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				getRegisterSet(qTasks).restoreDefaults();
			}
		});
	}

	/**
	 * @param qTasks
	 */
	public void save(TaskSet qTasks) {
		try {
			String memto = getRegisterSet(qTasks).getMemento();
			ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS, memto);
			wc.doSave();
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPRegisterManager#targetSuspended
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	public void targetSuspended(TaskSet qTasks) {
		getRegisterSet(qTasks).targetSuspended();
	}

	/**
	 * @return
	 */
	protected IPSession getSession() {
		return session;
	}
}
