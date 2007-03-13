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
import java.util.List;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExpression;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PExpression;
import org.eclipse.ptp.debug.internal.core.model.PStackFrame;
import org.eclipse.ptp.debug.internal.core.model.PTPMemoryBlockExtension;
import org.eclipse.ptp.debug.internal.core.model.PThread;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Clement chu
 * 
 */
public class PTPMemoryBlockRetrievalExtension extends PlatformObject implements IMemoryBlockRetrievalExtension {
	private static final String MEMORY_BLOCK_EXPRESSION_LIST = "memoryBlockExpressionList";
	private static final String MEMORY_BLOCK_EXPRESSION = "expression";
	private static final String ATTR_MEMORY_BLOCK_EXPRESSION_TEXT = "text";

	PDebugTarget fDebugTarget;

	/** Constructor
	 * @param debugTarget
	 */
	public PTPMemoryBlockRetrievalExtension(PDebugTarget debugTarget) {
		fDebugTarget = debugTarget;
	}
	/** Get Debug target
	 * @return
	 */
	protected PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	/** Initial
	 * 
	 */
	public void initialize() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			String memento = config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, "");
			if (memento != null && memento.trim().length() != 0)
				initializeFromMemento(memento);
		}
		catch(CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}

	private void initializeFromMemento(String memento) throws CoreException {
		Element root = DebugPlugin.parseDocument(memento);
		if (root.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION_LIST)) {
			List<String> expressions = new ArrayList<String>();
			NodeList list = root.getChildNodes();
			int length = list.getLength();
			for(int i = 0; i < length; ++i) {
				Node node = list.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element entry = (Element)node;
					if (entry.getNodeName().equalsIgnoreCase(MEMORY_BLOCK_EXPRESSION)) {
						String exp = entry.getAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT);
						expressions.add(exp);
					}
				}
			}
			createMemoryBlocks((String[])expressions.toArray(new String[expressions.size()]));
			return;
		}
		abort(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.3"), null);
	}

	private void createMemoryBlocks(String[] expressions) {
		ArrayList<PTPMemoryBlockExtension> list = new ArrayList<PTPMemoryBlockExtension>(expressions.length);
		for (int i = 0; i < expressions.length; ++i) {
			IAddress address = getDebugTarget().getAddressFactory().createAddress(expressions[i]);
			if (address != null) {
				list.add(new PTPMemoryBlockExtension(getDebugTarget(), address.toHexAddressString(), address.getValue()));
			}
		}
		DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks((IMemoryBlock[])list.toArray(new IMemoryBlock[list.size()]));
	}

	/** Get memento
	 * @return
	 * @throws CoreException
	 */
	public String getMemento() throws CoreException {
		IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(getDebugTarget());
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement(MEMORY_BLOCK_EXPRESSION_LIST);
		for (int i=0; i<blocks.length; ++i) {
			if (blocks[i] instanceof IMemoryBlockExtension) {
				Element child = document.createElement(MEMORY_BLOCK_EXPRESSION);
				try {
					child.setAttribute(ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, ((IMemoryBlockExtension)blocks[i]).getBigBaseAddress().toString());
					element.appendChild(child);
				}
				catch(DebugException e) {
					PTPDebugCorePlugin.log(e.getStatus());
				}
			}
		}
		document.appendChild(element);
		return DebugPlugin.serializeDocument(document);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtensionRetrieval#getExtendedMemoryBlock(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
	 */
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object selected) throws DebugException {
		String address = null;
		PExpression exp = null;
		String msg = null;
		try {
			if (selected instanceof IDebugElement) {
				IDebugElement debugElement = (IDebugElement)selected;
				PStackFrame frame = getStackFrame(debugElement);
				if (frame != null) {
					// We need to provide a better way for retrieving the address of expression
					IPCDIExpression cdiExpression = frame.getCDITarget().createExpression(expression);
					exp = new PExpression(frame, cdiExpression, null);
					IValue value = exp.getValue();
					if (value instanceof IPValue) {
						IPType type = ((IPValue)value).getType();
						if (type != null && (type.isPointer() || type.isIntegralType())) {
							address = value.getValueString();
							exp.dispose();
							IDebugTarget target = debugElement.getDebugTarget();
							if (target instanceof PDebugTarget) {
								if (address != null) {
									// ???
									BigInteger a = (address.startsWith("0x")) ? new BigInteger(address.substring(2), 16) : new BigInteger(address);
									return new PTPMemoryBlockExtension((PDebugTarget)target, expression, a);
								}
							}
						}
						else {
							msg = MessageFormat.format(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.1"), new String[] { expression });
						}
					}
					else {
						msg = MessageFormat.format(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.2"), new String[] { expression });
					}
				}
			}
		}
		catch(PCDIException e) {
			msg = e.getMessage();
		}
		catch(NumberFormatException e) {
			msg = MessageFormat.format(InternalDebugCoreMessages.getString("PTPMemoryBlockRetrievalExtension.0"), new String[] { expression, address });
		}
		throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		String expression = Long.toHexString(startAddress);
		BigInteger address = new BigInteger(expression, 16);
		expression += "0x";
		return new PTPMemoryBlockExtension(getDebugTarget(), expression, address);
	}

	private PStackFrame getStackFrame(IDebugElement selected) throws DebugException {
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

	/** Save ILaunchConfiguration 
	 * 
	 */
	public void save() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, getMemento());
			wc.doSave();
		}
		catch(CoreException e) {
			PTPDebugCorePlugin.log(e.getStatus());
		}
	}

	private void abort(String message, Throwable e) throws CoreException {
		IStatus s = new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, e);
		throw new CoreException(s);
	}

	public void dispose() {}
}
