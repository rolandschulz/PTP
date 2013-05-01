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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.IPMemoryManager;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIMemoryBlockInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIRestartedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeIntegral;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.model.PDebugTarget;
import org.eclipse.ptp.internal.debug.core.model.PExpression;
import org.eclipse.ptp.internal.debug.core.model.PMemoryBlockExtension;
import org.eclipse.ptp.internal.debug.core.model.PStackFrame;
import org.eclipse.ptp.internal.debug.core.model.PThread;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author clement
 * 
 */
public class PMemoryManager implements IAdaptable, IPDIEventListener, IPMemoryManager {
	private class PMemoryBlockRetrievalExtension extends PlatformObject implements IMemoryBlockRetrievalExtension {
		private IPDebugTarget debugTarget = null;
		private final TaskSet mTasks;

		public PMemoryBlockRetrievalExtension(TaskSet mTasks, IPDebugTarget debugTarget) {
			this.mTasks = mTasks;
			this.debugTarget = debugTarget;
		}

		/**
		 * @param message
		 * @param e
		 * @throws CoreException
		 */
		public void abort(String message, Throwable e) throws CoreException {
			final IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
					PTPDebugCorePlugin.INTERNAL_ERROR, message, e);
			throw new CoreException(s);
		}

		/**
		 * @param expressions
		 * @param memorySpaceIDs
		 */
		public void createMemoryBlocks(String[] expressions, String[] memorySpaceIDs) {
			final ArrayList<PMemoryBlockExtension> list = new ArrayList<PMemoryBlockExtension>(expressions.length);
			for (int i = 0; i < expressions.length; ++i) {
				try {
					final BigInteger address = new BigInteger(expressions[i]);
					if (address != null) {
						if (memorySpaceIDs[i] == null) {
							list.add(new PMemoryBlockExtension(session, mTasks, address.toString(16), address));
						} else {
							list.add(new PMemoryBlockExtension(session, mTasks, address, memorySpaceIDs[i]));
						}
					}
				} catch (final NumberFormatException exc) {
					PTPDebugCorePlugin.log(exc);
				}
			}
			DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(list.toArray(new IMemoryBlock[0]));
		}

		/**
		 * 
		 */
		public void dispose() {
		}

		/**
		 * @return
		 */
		public IPDebugTarget getDebugTarget() {
			if (debugTarget == null) {
				debugTarget = session.findDebugTarget(mTasks);
			}
			return debugTarget;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension#
		 * getExtendedMemoryBlock(java.lang.String, java.lang.Object)
		 */
		public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object selected) throws DebugException {
			String address = null;
			PExpression exp = null;
			String msg = null;
			try {
				if (selected instanceof IDebugElement) {
					final IDebugElement debugElement = (IDebugElement) selected;
					final IDebugTarget target = debugElement.getDebugTarget();
					if (!(target instanceof PDebugTarget)) {
						throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
								DebugException.REQUEST_FAILED, msg, null));
					}
					try {
						return new PMemoryBlockExtension(session, mTasks, expression, new BigInteger(expression, 16));
					} catch (final NumberFormatException nfexc) {
					}

					final PStackFrame frame = getStackFrame(debugElement);
					if (frame != null) {
						// We need to provide a better way for retrieving the
						// address of expression
						final IPDITargetExpression pdiExpression = session.getPDISession().getExpressionManager()
								.createExpression(frame.getTasks(), expression);
						exp = new PExpression(frame, pdiExpression, null);
						final IValue value = exp.getValue();
						if (value instanceof IPValue) {
							final IAIF aif = ((IPValue) value).getAIF();
							if (aif != null
									&& (aif.getType() instanceof IAIFTypePointer || aif.getType() instanceof ITypeIntegral || aif
											.getType() instanceof IAIFTypeArray)) {
								address = aif.getValue().getValueString();
								if (address != null) {
									final BigInteger a = (address.startsWith("0x")) ? new BigInteger(address.substring(2), 16) //$NON-NLS-1$
											: new BigInteger(address);
									return new PMemoryBlockExtension(session, mTasks, expression, a);
								}
							} else {
								msg = NLS.bind(Messages.PMemoryManager_0, new Object[] { expression });
							}
						} else {
							msg = NLS.bind(Messages.PMemoryManager_1, new Object[] { expression });
						}
					}
				}
			} catch (final PDIException pe) {
				msg = pe.getMessage();
			} catch (final AIFException e) {
				msg = e.getMessage();
			} catch (final NumberFormatException e) {
				msg = NLS.bind(Messages.PMemoryManager_2, new Object[] { expression, address });
			} finally {
				if (exp != null) {
					exp.dispose();
				}
			}
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
					DebugException.REQUEST_FAILED, msg, null));
		}

		/**
		 * @return
		 * @throws CoreException
		 */
		public String getMemento() throws CoreException {
			final IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(getDebugTarget());
			final Document document = DebugPlugin.newDocument();
			final Element exprList = document.createElement(MEMORY_BLOCK_EXPRESSION_LIST);
			for (int i = 0; i < blocks.length; ++i) {
				if (blocks[i] instanceof IMemoryBlockExtension) {
					final IMemoryBlockExtension memBlockExt = (IMemoryBlockExtension) blocks[i];
					final Element exprItem = document.createElement(MEMORY_BLOCK_EXPRESSION_ITEM);
					exprList.appendChild(exprItem);

					BigInteger addrBigInt = null;
					String memorySpaceID = null;
					if (hasMemorySpaces()) {
						try {
							final StringBuffer sbuf = new StringBuffer();
							addrBigInt = stringToAddress(memBlockExt.getExpression(), sbuf);
							memorySpaceID = sbuf.toString();
						} catch (final CoreException e) {
						}
					}
					Element child = document.createElement(MEMORY_BLOCK_EXPRESSION);
					try {
						if (addrBigInt != null && memorySpaceID != null) {
							child.setAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, addrBigInt.toString());
						} else {
							child.setAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, memBlockExt.getBigBaseAddress().toString());
						}
						exprItem.appendChild(child);
					} catch (final DebugException e) {
						PTPDebugCorePlugin.log(e.getStatus());
					}
					if (memorySpaceID != null) {
						child = document.createElement(MEMORY_BLOCK_MEMSPACEID);
						child.setAttribute(ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT, memorySpaceID);
						exprItem.appendChild(child);
					}
				}
			}
			document.appendChild(exprList);
			return DebugPlugin.serializeDocument(document);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock
		 * (long, long)
		 */
		public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
			String expression = Long.toHexString(startAddress);
			final BigInteger address = new BigInteger(expression, 16);
			expression += "0x"; //$NON-NLS-1$
			return new PMemoryBlockExtension(session, mTasks, expression, address);
		}

		/**
		 * @param address
		 * @param memorySpaceID
		 * @param selected
		 * @return
		 * @throws DebugException
		 */
		public IMemoryBlockExtension getMemoryBlockWithMemorySpaceID(String address, String memorySpaceID, Object selected)
				throws DebugException {
			String msg = null;
			try {
				if (selected instanceof IDebugElement) {
					final IDebugElement debugElement = (IDebugElement) selected;
					final IDebugTarget target = debugElement.getDebugTarget();
					if (target instanceof PDebugTarget) {
						if (address != null) {
							final BigInteger addr = (address.startsWith("0x")) ? new BigInteger(address.substring(2), 16) //$NON-NLS-1$
									: new BigInteger(address);
							return new PMemoryBlockExtension(session, mTasks, addr, memorySpaceID);
						}
					}
				}
			} catch (final NumberFormatException e) {
				msg = NLS.bind(Messages.PMemoryManager_3, new Object[] { address });
			}
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
					DebugException.REQUEST_FAILED, msg, null));
		}

		/**
		 * @return
		 */
		public String[] getMemorySpaces() {
			return new String[0];
		}

		/**
		 * @param selected
		 * @return
		 * @throws DebugException
		 */
		public PStackFrame getStackFrame(IDebugElement selected) throws DebugException {
			if (selected instanceof PStackFrame) {
				return (PStackFrame) selected;
			}
			if (selected instanceof PThread) {
				final IStackFrame frame = ((PThread) selected).getTopStackFrame();
				if (frame instanceof PStackFrame) {
					return (PStackFrame) frame;
				}
			}
			return null;
		}

		/**
		 * @return
		 */
		public boolean hasMemorySpaces() {
			return getMemorySpaces().length > 0;
		}

		/**
		 * 
		 */
		public void initialize() {
			final ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			try {
				final String memento = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, ""); //$NON-NLS-1$
				if (memento != null && memento.trim().length() != 0) {
					initializeFromMemento(memento);
				}
			} catch (final CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
		}

		/**
		 * @param memento
		 * @throws CoreException
		 */
		public void initializeFromMemento(String memento) throws CoreException {
			final Element root = DebugPlugin.parseDocument(memento);
			if (root.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION_LIST)) {
				final List<String> expressions = new ArrayList<String>();
				final List<String> memorySpaceIDs = new ArrayList<String>();
				final NodeList list = root.getChildNodes();
				final int length = list.getLength();
				for (int i = 0; i < length; ++i) {
					final Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						final Element entry = (Element) node;
						if (entry.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION)) {
							parseMementoExprItem(entry, expressions, memorySpaceIDs);
						}
					}
				}
				createMemoryBlocks(expressions.toArray(new String[0]), memorySpaceIDs.toArray(new String[0]));
				return;
			}
			abort(Messages.PMemoryManager_4, null);
		}

		/**
		 * @param element
		 * @param expressions
		 * @param memorySpaceIDs
		 */
		public void parseMementoExprItem(Element element, List<String> expressions, List<String> memorySpaceIDs) {
			final NodeList list = element.getChildNodes();
			final int length = list.getLength();
			String exp = null;
			String memorySpaceID = null;
			for (int i = 0; i < length; ++i) {
				final Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					final Element entry = (Element) node;
					if (entry.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION)) {
						exp = entry.getAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT);
					} else if (entry.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_MEMSPACEID)) {
						memorySpaceID = entry.getAttribute(ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT);
					}
				}
			}
			if (exp != null) {
				expressions.add(exp);
				memorySpaceIDs.add(memorySpaceID);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#
		 * supportsStorageRetrieval()
		 */
		public boolean supportsStorageRetrieval() {
			return true;
		}
	}

	private static final String ATTR_MEMORY_BLOCK_EXPRESSION_TEXT = "text"; //$NON-NLS-1$
	private static final String ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT = "text"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_EXPRESSION = "expression"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_EXPRESSION_ITEM = "memoryBlockExpressionItem"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_EXPRESSION_LIST = "memoryBlockExpressionList"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_MEMSPACEID = "memorySpaceID"; //$NON-NLS-1$

	/**
	 * @param address
	 * @param memorySpaceID
	 * @return
	 */
	public static String addressToString(BigInteger address, String memorySpaceID) {
		return memorySpaceID + ":0x" + address.toString(16); //$NON-NLS-1$
	}

	/**
	 * @param str
	 * @param memorySpaceID_out
	 * @return
	 * @throws CoreException
	 */
	public static BigInteger stringToAddress(String str, StringBuffer memorySpaceID_out) throws CoreException {
		final int index = str.lastIndexOf(':');

		// minimum is "<space>:0x?"
		if (index == -1 || str.length() <= index + 3 || str.charAt(index + 1) != '0' || str.charAt(index + 2) != 'x') {
			final IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
					PTPDebugCorePlugin.INTERNAL_ERROR, NLS.bind(Messages.PMemoryManager_5, str), null);
			throw new CoreException(s);
		}
		memorySpaceID_out.setLength(0);
		memorySpaceID_out.append(str.substring(0, index));
		return new BigInteger(str.substring(index + 3), 16);
	}

	private final IPSession session;
	protected final Map<TaskSet, PMemoryBlockRetrievalExtension> fMemoryRetrievalMap = new HashMap<TaskSet, PMemoryBlockRetrievalExtension>();

	public PMemoryManager(IPSession session) {
		this.session = session;
		session.getPDISession().getEventManager().addEventListener(this);
	}

	/**
	 * @param monitor
	 */
	public void dispose(IProgressMonitor monitor) {
		fMemoryRetrievalMap.clear();
		session.getPDISession().getEventManager().removeEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPMemoryManager#dispose(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	public void dispose(TaskSet qTasks) {
		getMemoryRetrieval(qTasks).dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class)) {
			return getSession();
		}
		if (adapter.equals(PMemoryManager.class)) {
			return this;
		}
		return null;
	}

	/**
	 * @param qTasks
	 * @param expression
	 * @param selected
	 * @return
	 * @throws DebugException
	 */
	public IMemoryBlockExtension getExtendedMemoryBlock(TaskSet qTasks, String expression, Object selected) throws DebugException {
		return getMemoryRetrieval(qTasks).getExtendedMemoryBlock(expression, selected);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPMemoryManager#getMemoryBlock(org
	 * .eclipse.ptp.core.util.TaskSet, long, long)
	 */
	public IMemoryBlock getMemoryBlock(TaskSet qTasks, long startAddress, long length) throws DebugException {
		return getMemoryRetrieval(qTasks).getMemoryBlock(startAddress, length);
	}

	/**
	 * @param qTasks
	 * @param address
	 * @param memorySpaceID
	 * @param selected
	 * @return
	 * @throws DebugException
	 */
	public IMemoryBlockExtension getMemoryBlockWithMemorySpaceID(TaskSet qTasks, String address, String memorySpaceID,
			Object selected) throws DebugException {
		return getMemoryRetrieval(qTasks).getMemoryBlockWithMemorySpaceID(address, memorySpaceID, selected);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPMemoryManager#getMemoryRetrieval
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	public PMemoryBlockRetrievalExtension getMemoryRetrieval(TaskSet qTasks) {
		synchronized (fMemoryRetrievalMap) {
			PMemoryBlockRetrievalExtension set = fMemoryRetrievalMap.get(qTasks);
			if (set == null) {
				set = new PMemoryBlockRetrievalExtension(qTasks, null);
				fMemoryRetrievalMap.put(qTasks, set);
			}
			return set;
		}
	}

	/**
	 * @return
	 */
	public IPSession getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (final IPDIEvent event2 : events) {
			final IPDIEvent event = event2;
			if (!fMemoryRetrievalMap.containsKey(event.getTasks())) {
				continue;
			}

			if (event instanceof IPDIResumedEvent || event instanceof IPDIRestartedEvent) {
				doResetChange(event);
			} else if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent) event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPMemoryManager#initialize(org.eclipse
	 * .ptp.core.util.TaskSet,
	 * org.eclipse.ptp.internal.debug.core.model.PDebugTarget)
	 */
	public void initialize(TaskSet qTasks, IPDebugTarget debugTarget) {
		synchronized (fMemoryRetrievalMap) {
			final PMemoryBlockRetrievalExtension set = new PMemoryBlockRetrievalExtension(qTasks, debugTarget);
			fMemoryRetrievalMap.put(qTasks, set);
			set.initialize();
		}
	}

	/**
	 * @param qTasks
	 * @param memento
	 * @throws CoreException
	 */
	public void initializeFromMemento(TaskSet qTasks, String memento) throws CoreException {
		getMemoryRetrieval(qTasks).initializeFromMemento(memento);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPMemoryManager#save(org.eclipse.
	 * ptp.core.util.TaskSet)
	 */
	public void save(TaskSet qTasks) {
		try {
			final String memto = getMemoryRetrieval(qTasks).getMemento();
			final ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			if (config != null) {
				final ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, memto);
				wc.doSave();
			}
		} catch (final CoreException e) {
			PTPDebugCorePlugin.log(e.getStatus());
		}
	}

	/**
	 * @param qTasks
	 * @return
	 */
	public boolean supportsStorageRetrieval(TaskSet qTasks) {
		return true;
	}

	/**
	 * @param event
	 */
	private void doResetChange(final IPDIEvent event) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				final IPDebugTarget debugTarget = getMemoryRetrieval(event.getTasks()).getDebugTarget();
				final IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(debugTarget);
				for (final IMemoryBlock block : blocks) {
					if (block instanceof IMemoryBlockExtension) {
						((PMemoryBlockExtension) block).resetChanges();
					}
				}
			}
		});
	}

	/**
	 * @param event
	 */
	private void handleChangedEvent(final IPDIChangedEvent event) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				final IPDISessionObject reason = (event).getReason();
				if (reason instanceof IPDIMemoryBlockInfo) {
					final IPDebugTarget debugTarget = getMemoryRetrieval(event.getTasks()).getDebugTarget();
					final IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(debugTarget);
					for (final IMemoryBlock block : blocks) {
						if (block instanceof IMemoryBlockExtension) {
							((PMemoryBlockExtension) block).changes(((IPDIMemoryBlockInfo) reason).getMemoryBlock(),
									((IPDIMemoryBlockInfo) reason).getAddresses());
						}
					}
				}
			}
		});
	}
}
