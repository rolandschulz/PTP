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

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
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
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
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
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PExpression;
import org.eclipse.ptp.debug.internal.core.model.PMemoryBlockExtension;
import org.eclipse.ptp.debug.internal.core.model.PStackFrame;
import org.eclipse.ptp.debug.internal.core.model.PThread;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author clement
 *
 */
public class PMemoryManager implements IAdaptable, IPDIEventListener {
	class PMemoryBlockRetrievalExtension extends PlatformObject implements IMemoryBlockRetrievalExtension {
		PDebugTarget debugTarget = null;
		BitList mTasks;
		
		PMemoryBlockRetrievalExtension(BitList mTasks, PDebugTarget debugTarget) {
			this.mTasks = mTasks;
			this.debugTarget = debugTarget;
		}
		PDebugTarget getDebugTarget() {
			if (debugTarget == null)
				debugTarget = session.findDebugTarget(mTasks);
			return debugTarget;
		}
		void initialize() {
			ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			try {
				String memento = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, "");
				if (memento != null && memento.trim().length() != 0)
					initializeFromMemento(memento);
			}
			catch(CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
		}
		void parseMementoExprItem(Element element, List<String> expressions, List<String> memorySpaceIDs) {
			NodeList list = element.getChildNodes();
			int length = list.getLength();
			String exp = null;
			String memorySpaceID = null;
			for(int i = 0; i < length; ++i) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element entry = (Element)node;
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
		void initializeFromMemento(String memento) throws CoreException {
			Element root = DebugPlugin.parseDocument(memento);
			if (root.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION_LIST)) {
				List<String> expressions = new ArrayList<String>();
				List<String> memorySpaceIDs = new ArrayList<String>();
				NodeList list = root.getChildNodes();
				int length = list.getLength();
				for(int i = 0; i < length; ++i) {
					Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element entry = (Element)node;
						if (entry.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION)) {
							parseMementoExprItem(entry, expressions, memorySpaceIDs);
						}
					}
				}
				createMemoryBlocks((String[])expressions.toArray(new String[0]), (String[])memorySpaceIDs.toArray(new String[0]));
				return;
			}
			abort(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.3"), null);
		}
		public boolean supportsStorageRetrieval() {
			return true;
		}
		void createMemoryBlocks(String[] expressions, String[] memorySpaceIDs) {
			ArrayList<PMemoryBlockExtension> list = new ArrayList<PMemoryBlockExtension>(expressions.length);
			for (int i = 0; i < expressions.length; ++i) {
				try {
					BigInteger address = new BigInteger(expressions[i]);
					if (address != null) {
						if (memorySpaceIDs[i] == null) {
							list.add(new PMemoryBlockExtension(session, mTasks, address.toString(16), address));
						}
						else {
							list.add(new PMemoryBlockExtension(session, mTasks, address, memorySpaceIDs[i]));
						}
					}
				}
				catch (NumberFormatException exc) {
					PTPDebugCorePlugin.log(exc);
				}
			}
			DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks((IMemoryBlock[])list.toArray(new IMemoryBlock[0]));
		}
		String getMemento() throws CoreException {
			IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(getDebugTarget());
			Document document = DebugPlugin.newDocument();
			Element exprList = document.createElement(MEMORY_BLOCK_EXPRESSION_LIST);
			for (int i=0; i<blocks.length; ++i) {
				if (blocks[i] instanceof IMemoryBlockExtension) {
					IMemoryBlockExtension memBlockExt = (IMemoryBlockExtension)blocks[i];
					Element exprItem = document.createElement( MEMORY_BLOCK_EXPRESSION_ITEM );
					exprList.appendChild(exprItem);

					BigInteger addrBigInt = null;
					String memorySpaceID = null;
					if (hasMemorySpaces()) {
						try {
							StringBuffer sbuf = new StringBuffer();
							addrBigInt = stringToAddress(memBlockExt.getExpression(), sbuf);
							memorySpaceID = sbuf.toString();
						} 
						catch (CoreException e) {
						}
					}
					Element child = document.createElement(MEMORY_BLOCK_EXPRESSION);
					try {
						if (addrBigInt != null && memorySpaceID != null) {
							child.setAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, addrBigInt.toString());
						}
						else {
							child.setAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, memBlockExt.getBigBaseAddress().toString());
						}
						exprItem.appendChild(child);
					}
					catch(DebugException e) {
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
		public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object selected) throws DebugException {
			String address = null;
			PExpression exp = null;
			String msg = null;
			try {
				if (selected instanceof IDebugElement) {
					IDebugElement debugElement = (IDebugElement)selected;
					IDebugTarget target = debugElement.getDebugTarget();
					if (!(target instanceof PDebugTarget)) {
						throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null)); 
					}
					try {
						return new PMemoryBlockExtension(session, mTasks, expression, new BigInteger(expression, 16));
					} catch (NumberFormatException nfexc) {
					}
					
					PStackFrame frame = getStackFrame(debugElement);
					if (frame != null) {
						// We need to provide a better way for retrieving the address of expression
						IPDITargetExpression pdiExpression = session.getPDISession().getExpressionManager().createExpression(frame.getTasks(), expression);
						exp = new PExpression(frame, pdiExpression, null);
						IValue value = exp.getValue();
						if (value instanceof IPValue) {
							IAIF aif = ((IPValue)value).getAIF();
							if (aif != null && (aif.getType() instanceof IAIFTypePointer || aif.getType() instanceof ITypeIntegral || aif.getType() instanceof IAIFTypeArray)) {
								address = aif.getValue().getValueString();
								if (address != null) {
									BigInteger a = (address.startsWith("0x")) ? new BigInteger(address.substring(2), 16) : new BigInteger(address);
									return new PMemoryBlockExtension(session, mTasks, expression, a);
								}
							}
							else {
								msg = MessageFormat.format(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.1"), new Object[] { expression });
							}
						}
						else {
							msg = MessageFormat.format(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.2"), new Object[] { expression });
						}
					}
				}
			}
			catch(PDIException pe) {
				msg = pe.getMessage();
			}
			catch(AIFException e) {
				msg = e.getMessage();
			}
			catch(NumberFormatException e) {
				msg = MessageFormat.format(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.0"), new Object[] { expression, address });
			}
			finally {
				if (exp != null) {
					exp.dispose();
				}
			}
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null));
		}
		public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
			String expression = Long.toHexString(startAddress);
			BigInteger address = new BigInteger(expression, 16);
			expression += "0x";
			return new PMemoryBlockExtension(session, mTasks, expression, address);
		}
		IMemoryBlockExtension getMemoryBlockWithMemorySpaceID(String address, String memorySpaceID, Object selected) throws DebugException {
			String msg = null;
			try {
				if (selected instanceof IDebugElement) {
					IDebugElement debugElement = (IDebugElement)selected;
					IDebugTarget target = debugElement.getDebugTarget();
					if (target instanceof PDebugTarget) {
						if (address != null) {
							BigInteger addr = (address.startsWith("0x")) ? new BigInteger(address.substring(2), 16) : new BigInteger(address);
							return new PMemoryBlockExtension(session, mTasks, addr, memorySpaceID);
						}
					}
				}
			}
			catch(NumberFormatException e) {
				msg = MessageFormat.format(InternalDebugCoreMessages.getString("PMemoryBlockRetrievalExtension.4"), new Object[] { address });
			}
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null));
		}
		PStackFrame getStackFrame(IDebugElement selected) throws DebugException {
			if (selected instanceof PStackFrame) {
				return (PStackFrame)selected;
			}
			if (selected instanceof PThread) {
				IStackFrame frame = ((PThread)selected).getTopStackFrame();
				if (frame instanceof PStackFrame)
					return (PStackFrame)frame;
			}
			return null;
		}
		void abort(String message, Throwable e) throws CoreException {
			IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, e);
			throw new CoreException(s);
		}
		void dispose() {}
		
		boolean hasMemorySpaces() {
			return getMemorySpaces().length > 0;
		}
		String [] getMemorySpaces() {
			return new String[0];
		}		
	}
	private static final String MEMORY_BLOCK_EXPRESSION_LIST = "memoryBlockExpressionList";
	private static final String MEMORY_BLOCK_EXPRESSION_ITEM = "memoryBlockExpressionItem";	
	private static final String MEMORY_BLOCK_EXPRESSION = "expression";
	private static final String MEMORY_BLOCK_MEMSPACEID = "memorySpaceID";
	private static final String ATTR_MEMORY_BLOCK_MEMSPACEID_TEXT = "text";	
	private static final String ATTR_MEMORY_BLOCK_EXPRESSION_TEXT = "text";
	
	protected Map<BitList, PMemoryBlockRetrievalExtension> fMemoryRetrievalMap;
	private PSession session;
	
	public PMemoryManager(PSession session) {
		this.session = session;
	}
	public void initialize(IProgressMonitor monitor) {
		fMemoryRetrievalMap = new Hashtable<BitList, PMemoryBlockRetrievalExtension>();
		session.getPDISession().getEventManager().addEventListener(this);
	}	
	public IPSession getSession() {
		return session;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class))
			return getSession();
		if (adapter.equals(PMemoryManager.class))
			return this;
		return null;
	}
	public void dispose(IProgressMonitor monitor) {
		fMemoryRetrievalMap.clear();
		session.getPDISession().getEventManager().removeEventListener(this);
	}
	public PMemoryBlockRetrievalExtension getMemoryRetrieval(BitList qTasks) {
		synchronized (fMemoryRetrievalMap) {
			PMemoryBlockRetrievalExtension set = (PMemoryBlockRetrievalExtension)fMemoryRetrievalMap.get(qTasks);
			if (set == null) {
				set = new PMemoryBlockRetrievalExtension(qTasks, null);
				fMemoryRetrievalMap.put(qTasks, set);
			}
			return set;
		}
	}
	public void dispose(BitList qTasks) {
		getMemoryRetrieval(qTasks).dispose();
	}
	public void initialize(BitList qTasks, PDebugTarget debugTarget) {
		synchronized (fMemoryRetrievalMap) {
			PMemoryBlockRetrievalExtension set = new PMemoryBlockRetrievalExtension(qTasks, debugTarget);
			fMemoryRetrievalMap.put(qTasks, set);
			set.initialize();
		}
	}
	public void initializeFromMemento(BitList qTasks, String memento) throws CoreException {
		getMemoryRetrieval(qTasks).initializeFromMemento(memento);
	}
	public IMemoryBlockExtension getExtendedMemoryBlock(BitList qTasks, String expression, Object selected) throws DebugException {
		return getMemoryRetrieval(qTasks).getExtendedMemoryBlock(expression, selected);
	}
	public boolean supportsStorageRetrieval(BitList qTasks) {
		return true;
	}
	public IMemoryBlock getMemoryBlock(BitList qTasks, long startAddress, long length) throws DebugException {
		return getMemoryRetrieval(qTasks).getMemoryBlock(startAddress, length);
	}
	public IMemoryBlockExtension getMemoryBlockWithMemorySpaceID(BitList qTasks, String address, String memorySpaceID, Object selected) throws DebugException {
		return getMemoryRetrieval(qTasks).getMemoryBlockWithMemorySpaceID(address, memorySpaceID, selected);
	}
	public void save(BitList qTasks) {
		try {
			String memto = getMemoryRetrieval(qTasks).getMemento();
			ILaunchConfiguration config = session.getLaunch().getLaunchConfiguration();
			if (config != null) {
				ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, memto);
				wc.doSave();
			}
		}
		catch(CoreException e) {
			PTPDebugCorePlugin.log(e.getStatus());
		}
	}	
	public static String addressToString(BigInteger address, String memorySpaceID) {
		return memorySpaceID + ":0x" + address.toString(16);
	}
	public static BigInteger stringToAddress(String str, StringBuffer memorySpaceID_out) throws CoreException {
		int index = str.lastIndexOf(':');
		
		// minimum is "<space>:0x?"
		if (index == -1 || str.length() <= index + 3 || str.charAt(index+1) != '0' || str.charAt(index+2) != 'x') {
			IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, InternalDebugCoreMessages.getString("PMemoryBlockRetrievalExtension.5"), null);
			throw new CoreException(s);
		}
		memorySpaceID_out.setLength(0);
		memorySpaceID_out.append(str.substring(0, index));
		return new BigInteger(str.substring(index+3), 16);
	}
	/****************************************
	 * IPDIEventListener
	 ****************************************/
	public void handleDebugEvents(IPDIEvent[] events) {
		for(int i = 0; i < events.length; i++) {
			IPDIEvent event = events[i];
			if (!fMemoryRetrievalMap.containsKey(event.getTasks()))
				continue;
			
			if (event instanceof IPDIResumedEvent || event instanceof IPDIRestartedEvent) {
				doResetChange(event);
			}
			else if (event instanceof IPDIChangedEvent) {
				handleChangedEvent((IPDIChangedEvent)event);
			}
		}
	}
	private void doResetChange(final IPDIEvent event) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				PDebugTarget debugTarget = getMemoryRetrieval(event.getTasks()).getDebugTarget();
				IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(debugTarget);
				for (IMemoryBlock block : blocks) {
					if (block instanceof IMemoryBlockExtension) {
						((PMemoryBlockExtension)block).resetChanges();
					}
				}
			}
		});
	}
	private void handleChangedEvent(final IPDIChangedEvent event) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				IPDISessionObject reason = ((IPDIChangedEvent)event).getReason();
				if (reason instanceof IPDIMemoryBlockInfo) {
					PDebugTarget debugTarget = getMemoryRetrieval(event.getTasks()).getDebugTarget();
					IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(debugTarget);
					for (IMemoryBlock block : blocks) {
						if (block instanceof IMemoryBlockExtension) {
							((PMemoryBlockExtension)block).changes(((IPDIMemoryBlockInfo)reason).getMemoryBlock(), ((IPDIMemoryBlockInfo)reason).getAddresses());
						}
					}
				}
			}
		});
	}	
}
