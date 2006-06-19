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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIMemoryChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIRestartedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;
import org.eclipse.ptp.debug.core.model.IExecFileInfo;

/**
 * @author Clement chu
 * 
 */
public class PTPMemoryBlockExtension extends PDebugElement implements IMemoryBlockExtension, IPCDIEventListener {
	private String fExpression;
	private BigInteger fBaseAddress;
	private IPCDIMemoryBlock fCDIBlock;
	private MemoryByte[] fBytes = null;
	private HashSet fChanges = new HashSet();
	private int fWordSize;

	/** Constructor for PTPMemoryBlockExtension
	 * @param target
	 * @param expression
	 * @param baseAddress
	 */
	public PTPMemoryBlockExtension(PDebugTarget target, String expression, BigInteger baseAddress) {
		this(target, expression, baseAddress, 1);
	}

	/** Constructor for PTPMemoryBlockExtension
	 * @param target
	 * @param expression
	 * @param baseAddress
	 * @param wordSize
	 */
	public PTPMemoryBlockExtension(PDebugTarget target, String expression, BigInteger baseAddress, int wordSize) {
		super(target);
		fExpression = expression;
		fBaseAddress = baseAddress;
		fWordSize = wordSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getExpression()
	 */
	public String getExpression() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigBaseAddress()
	 */
	public BigInteger getBigBaseAddress() {
		return fBaseAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressSize()
	 */
	public int getAddressSize() {
		return ((PDebugTarget)getDebugTarget()).getAddressFactory().createAddress(getBigBaseAddress()).getSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressableSize()
	 */
	public int getAddressableSize() throws DebugException {
		IPCDIMemoryBlock block = getCDIBlock();
		return (block != null) ? block.getWordSize() : fWordSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setBaseAddress(java.math.BigInteger)
	 */
	public void setBaseAddress(BigInteger address) throws DebugException {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromOffset(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromOffset(BigInteger unitOffset, long addressableUnits) throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long length) throws DebugException {
		IPCDIMemoryBlock cdiBlock = getCDIBlock();
		if (cdiBlock == null || 
			 cdiBlock.getStartAddress().compareTo(address) > 0 || 
			 cdiBlock.getStartAddress().add(BigInteger.valueOf(cdiBlock.getLength())).compareTo(address.add(BigInteger.valueOf(length))) < 0) {
			synchronized(this) {
				byte[] bytes = null;
				try {
					cdiBlock = getCDIBlock();
					if (cdiBlock == null || 
						 cdiBlock.getStartAddress().compareTo(address) > 0 || 
						 cdiBlock.getStartAddress().add(BigInteger.valueOf(cdiBlock.getLength())).compareTo(address.add(BigInteger.valueOf(length))) < 0) {
						if (cdiBlock != null) {
							disposeCDIBlock();
							fBytes = null;
						}
						setCDIBlock(createCDIBlock(address, length, fWordSize));
					}
					bytes = getCDIBlock().getBytes();
				}
				catch(PCDIException e) {
					targetRequestFailed(e.getMessage(), null);
				}
				fBytes = new MemoryByte[bytes.length];
				for (int i = 0; i < bytes.length; ++i) {
					fBytes[i] = createMemoryByte(bytes[i], getCDIBlock().getFlags(i), hasChanged(getRealBlockAddress().add(BigInteger.valueOf(i))));
				}
			}
		}
		MemoryByte[] result = new MemoryByte[0];
		if (fBytes != null) {
			int offset = address.subtract(getRealBlockAddress()).intValue();
			if (offset >= 0) {
				int size = (fBytes.length - offset >= length) ? (int)length : fBytes.length - offset;
				if (size > 0) {
					result = new MemoryByte[size];
					System.arraycopy(fBytes, offset, result, 0, size);
				}
			}
		}
		return result;
	}

	private boolean isBigEndian() {
		IExecFileInfo info = (IExecFileInfo)getDebugTarget().getAdapter(IExecFileInfo.class);
		if (info != null) {
			return !info.isLittleEndian();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockRetrieval()
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
		return (IMemoryBlockRetrieval)getDebugTarget().getAdapter(IMemoryBlockRetrieval.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener#handleDebugEvents(org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent[])
	 */
	public void handleDebugEvents(IPCDIEvent[] events) {
		for(int i = 0; i < events.length; i++) {
			IPCDIEvent event = events[i];
			IPCDIObject source = event.getSource(getCDITarget().getTargetID());
			if (source == null)
				continue;
			if (source.getTarget().equals(getCDITarget())) {
				if (event instanceof IPCDIResumedEvent || event instanceof IPCDIRestartedEvent) {
					resetChanges();
				}
				else if (event instanceof IPCDIMemoryChangedEvent) {
					if (source instanceof IPCDIMemoryBlock && source.equals(getCDIBlock())) {
						handleChangedEvent((IPCDIMemoryChangedEvent)event);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
	 */
	public long getStartAddress() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
	 */
	public long getLength() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
	 */
	public void setValue(long offset, byte[] bytes) throws DebugException {
		setValue(BigInteger.valueOf(offset), bytes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setValue(java.math.BigInteger, byte[])
	 */
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
		IPCDIMemoryBlock block = getCDIBlock();
		if (block != null) {
			BigInteger base = getBigBaseAddress();
			BigInteger real = getRealBlockAddress();
			long realOffset = base.add(offset).subtract(real).longValue();
			try {
				block.setValue(realOffset, bytes);
			}
			catch(PCDIException e) {
				targetRequestFailed(e.getDetailMessage(), null);
			}
		}
	}

	private IPCDIMemoryBlock createCDIBlock(BigInteger address, long length, int wordSize) throws PCDIException {
		IPCDIMemoryBlock block = ((PDebugTarget)getDebugTarget()).getCDITarget().createMemoryBlock(address.toString(), (int)length, wordSize);
		block.setFrozen(false);
		getCDISession().getEventManager().addEventListener(this);
		return block;
	}

	private void disposeCDIBlock() {
		IPCDIMemoryBlock block = getCDIBlock();
		if (block != null) {
			try {
				((PDebugTarget)getDebugTarget()).getCDITarget().removeBlocks(new IPCDIMemoryBlock[]{ block });
			}
			catch(PCDIException e) {
				DebugPlugin.log(e);
			}
			setCDIBlock(null);
			getCDISession().getEventManager().removeEventListener(this);
		}
	}

	private IPCDIMemoryBlock getCDIBlock() {
		return fCDIBlock;
	}

	private void setCDIBlock(IPCDIMemoryBlock cdiBlock) {
		fCDIBlock = cdiBlock;
	}

	private BigInteger getRealBlockAddress() {
		IPCDIMemoryBlock block = getCDIBlock();
		return (block != null) ? block.getStartAddress() : BigInteger.ZERO;
	}

	private long getBlockSize() {
		IPCDIMemoryBlock block = getCDIBlock();
		return (block != null) ? block.getLength() : 0;
	}

	private void handleChangedEvent(IPCDIMemoryChangedEvent event) {
		IPCDIMemoryBlock block = getCDIBlock();
		if (block != null && fBytes != null) {
			MemoryByte[] memBytes = (MemoryByte[])fBytes.clone();
			try {
				BigInteger start = getRealBlockAddress();
				long length = block.getLength();
				byte[] newBytes = block.getBytes();
				BigInteger[] addresses = event.getAddresses();
				saveChanges(addresses);
				for (int i = 0; i < addresses.length; ++i) {
					fChanges.add(addresses[i]);
					if (addresses[i].compareTo(start) >= 0 && addresses[i].compareTo(start.add(BigInteger.valueOf(length))) < 0) {
						int index = addresses[i].subtract(start).intValue();
						if (index >= 0 && index < memBytes.length && index < newBytes.length) {
							memBytes[index].setChanged(true);
							memBytes[index].setValue(newBytes[index]);
						}
					}
				}
				fBytes = memBytes;
				fireChangeEvent(DebugEvent.CONTENT);
			}
			catch(PCDIException e) {
				DebugPlugin.log(e);
			}			
		}
	}

	private void saveChanges(BigInteger[] addresses) {
		fChanges.addAll(Arrays.asList(addresses));
	}

	private boolean hasChanged(BigInteger address) {
		return fChanges.contains(address);
	}

	private void resetChanges() {
		if (fBytes != null) {
			BigInteger[] changes = (BigInteger[])fChanges.toArray(new BigInteger[fChanges.size()]);
			for (int i = 0; i < changes.length; ++i) {
				BigInteger real = getRealBlockAddress();
				if (real.compareTo(changes[i]) <= 0 && real.add(BigInteger.valueOf(getBlockSize())).compareTo(changes[i]) > 0) {
					int index = changes[i].subtract(real).intValue();
					if (index >= 0 && index < fBytes.length) {
						fBytes[index].setChanged(false); 
					}
				}
			}
		}
		fChanges.clear();
		fireChangeEvent(DebugEvent.CONTENT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportsChangeManagement()
	 */
	public boolean supportsChangeManagement() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#connect(java.lang.Object)
	 */
	public void connect(Object object) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#disconnect(java.lang.Object)
	 */
	public void disconnect(Object object) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getConnections()
	 */
	public Object[] getConnections() {
		// TODO Auto-generated method stub
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#dispose()
	 */
	public void dispose() {
		fChanges.clear();
		IPCDIMemoryBlock cdiBlock = getCDIBlock();
		if (cdiBlock != null) {
			try {
				((PDebugTarget)getDebugTarget()).getCDITarget().removeBlocks(new IPCDIMemoryBlock[] {cdiBlock});
			}
			catch(PCDIException e) {
				PTPDebugCorePlugin.log(e);
			}
			fCDIBlock = null;
		}
		getCDISession().getEventManager().removeEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IMemoryBlockRetrieval.class.equals(adapter))
			return getMemoryBlockRetrieval();
		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockStartAddress()
	 */
	public BigInteger getMemoryBlockStartAddress() throws DebugException {
		return null; // return null to mean not bounded ... according to the spec
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockEndAddress()
	 */
	public BigInteger getMemoryBlockEndAddress() throws DebugException {
		return null;// return null to mean not bounded ... according to the spec
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigLength()
	 */
	public BigInteger getBigLength() throws DebugException {
		IPCDIMemoryBlock block = getCDIBlock();
		if (block != null) {
			BigInteger length = new BigInteger(Long.toHexString(block.getLength()), 16);
			return length;
		}
		return BigInteger.ZERO;
	}

	private MemoryByte createMemoryByte(byte value, byte cdiFlags, boolean changed) {
		byte flags = 0;
		if ((cdiFlags & IPCDIMemoryBlock.VALID) != 0) {
			flags |= MemoryByte.HISTORY_KNOWN | MemoryByte.ENDIANESS_KNOWN;
			if ((cdiFlags & IPCDIMemoryBlock.READ_ONLY) != 0) {
				flags |= MemoryByte.READABLE;
			}
			else {
				flags |= MemoryByte.READABLE | MemoryByte.WRITABLE;
			}
			if (isBigEndian()) {
				flags |= MemoryByte.BIG_ENDIAN;
			}
			if (changed)
				flags |= MemoryByte.CHANGED;
		}
		return new MemoryByte(value, flags);
	}
}
