/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.ptp.debug.internal.core; 

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.ptp.debug.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.CExpression;
import org.eclipse.ptp.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.ptp.debug.internal.core.model.CStackFrame;
import org.eclipse.ptp.debug.internal.core.model.CThread;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements the memory retrieval features based on the CDI model.
 */
public class PMemoryBlockRetrievalExtension implements IMemoryBlockRetrievalExtension {

	private static final String MEMORY_BLOCK_EXPRESSION_LIST = "memoryBlockExpressionList"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK_EXPRESSION = "expression"; //$NON-NLS-1$
	private static final String ATTR_MEMORY_BLOCK_EXPRESSION_TEXT = "text"; //$NON-NLS-1$

	PDebugTarget fDebugTarget;

	/** 
	 * Constructor for CMemoryBlockRetrievalExtension. 
	 */
	public PMemoryBlockRetrievalExtension( PDebugTarget debugTarget ) {
		fDebugTarget = debugTarget;
	}

	protected PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	
	public void initialize() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			String memento = config.getAttribute( IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, "" ); //$NON-NLS-1$
			if ( memento != null && memento.trim().length() != 0 )
				initializeFromMemento( memento );
		}
		catch( CoreException e ) {
			PTPDebugCorePlugin.log( e );
		}
	}

	private void initializeFromMemento( String memento ) throws CoreException {
		Element root = DebugPlugin.parseDocument( memento );
		if ( root.getNodeName().equalsIgnoreCase( MEMORY_BLOCK_EXPRESSION_LIST ) ) {
			List expressions = new ArrayList();
			NodeList list = root.getChildNodes();
			int length = list.getLength();
			for( int i = 0; i < length; ++i ) {
				Node node = list.item( i );
				short type = node.getNodeType();
				if ( type == Node.ELEMENT_NODE ) {
					Element entry = (Element)node;
					if ( entry.getNodeName().equalsIgnoreCase( MEMORY_BLOCK_EXPRESSION ) ) {
						String exp = entry.getAttribute( ATTR_MEMORY_BLOCK_EXPRESSION_TEXT );
						expressions.add( exp );
					}
				}
			}
			createMemoryBlocks( (String[])expressions.toArray( new String[expressions.size()] ) );
			return;
		}
		abort( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.3" ), null ); //$NON-NLS-1$
	}

	private void createMemoryBlocks( String[] expressions ) {
		ArrayList list = new ArrayList( expressions.length );
		for ( int i = 0; i < expressions.length; ++i ) {
			IAddress address = getDebugTarget().getAddressFactory().createAddress( expressions[i] );
			if ( address != null ) {
				list.add( new CMemoryBlockExtension( getDebugTarget(), address.toHexAddressString(), address.getValue() ) );
			}
		}
		DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks( (IMemoryBlock[])list.toArray( new IMemoryBlock[list.size()] ) );
	}

	public String getMemento() throws CoreException {
		IMemoryBlock[] blocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks( getDebugTarget() );
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement( MEMORY_BLOCK_EXPRESSION_LIST );
		for ( int i = 0; i < blocks.length; ++i ) {
			if ( blocks[i] instanceof IMemoryBlockExtension ) {
				Element child = document.createElement( MEMORY_BLOCK_EXPRESSION );
				try {
					child.setAttribute( ATTR_MEMORY_BLOCK_EXPRESSION_TEXT, ((IMemoryBlockExtension)blocks[i]).getBigBaseAddress().toString() );
					element.appendChild( child );
				}
				catch( DebugException e ) {
					PTPDebugCorePlugin.log( e.getStatus() );
				}
			}
		}
		document.appendChild( element );
		return DebugPlugin.serializeDocument( document );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtensionRetrieval#getExtendedMemoryBlock(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
	 */
	public IMemoryBlockExtension getExtendedMemoryBlock( String expression, Object selected ) throws DebugException {
		String address = null;
		CExpression exp = null;
		String msg = null;
		try {
			if (selected instanceof IDebugElement) {
				IDebugElement debugElement = (IDebugElement)selected;
				CStackFrame frame = getStackFrame( debugElement );
				if ( frame != null ) {
					// We need to provide a better way for retrieving the address of expression
					ICDIExpression cdiExpression = frame.getCDITarget().createExpression( expression );
					exp = new CExpression( frame, cdiExpression, null );
					IValue value = exp.getValue();
					if ( value instanceof ICValue ) {
						ICType type = ((ICValue)value).getType();
						if ( type != null && (type.isPointer() || type.isIntegralType()) ) {
							address = value.getValueString();
							exp.dispose();
							IDebugTarget target = debugElement.getDebugTarget();
							if ( target instanceof PDebugTarget ) {
								if ( address != null ) {
									// ???
									BigInteger a = ( address.startsWith( "0x" ) ) ? new BigInteger( address.substring( 2 ), 16 ) : new BigInteger( address ); //$NON-NLS-1$
									return new CMemoryBlockExtension( (PDebugTarget)target, expression, a );
								}
							}
						}
						else {
							msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.1" ), new String[] { expression } ); //$NON-NLS-1$
						}
					}
					else {
						msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.2" ), new String[] { expression } ); //$NON-NLS-1$
					}
				}
			}
		}
		catch( CDIException e ) {
			msg = e.getMessage();
		}
		catch( NumberFormatException e ) {
			msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockRetrievalExtension.0" ), new String[] { expression, address } ); //$NON-NLS-1$
		}
		throw new DebugException( new Status( IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null ) );
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
	public IMemoryBlock getMemoryBlock( long startAddress, long length ) throws DebugException {
		String expression = Long.toHexString(startAddress);
		BigInteger address = new BigInteger(expression, 16);
		expression += "0x"; //$NON-NLS-1$
		return new CMemoryBlockExtension( getDebugTarget(), expression, address );
	}

	private CStackFrame getStackFrame( IDebugElement selected ) throws DebugException {
		if ( selected instanceof CStackFrame ) {
			return (CStackFrame)selected;
		}
		if ( selected instanceof CThread ) {
			IStackFrame frame = ((CThread)selected).getTopStackFrame();
			if ( frame instanceof CStackFrame )
				return (CStackFrame)frame;
		}
		return null;
	}

	public void save() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute( IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_MEMORY_BLOCKS, getMemento() );
			wc.doSave();
		}
		catch( CoreException e ) {
			PTPDebugCorePlugin.log( e.getStatus() );
		}
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException {
		IStatus s = new Status( IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, message, e );
		throw new CoreException( s );
	}

	public void dispose() {
	}
}
