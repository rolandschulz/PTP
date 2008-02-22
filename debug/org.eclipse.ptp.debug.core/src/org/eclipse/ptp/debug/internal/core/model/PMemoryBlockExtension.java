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
import java.util.Set;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.internal.core.PMemoryManager;

/**
 * @author Clement chu
 * 
 */
public class PMemoryBlockExtension extends PDebugElement implements IMemoryBlockExtension {
	private final String fExpression;
	private final BigInteger fBaseAddress;
	private final String fMemorySpaceID;
	private IPDIMemoryBlock fPDIBlock = null;
	private MemoryByte[] fBytes = null;
	private final Set<BigInteger> fChanges = new HashSet<BigInteger>();

	public PMemoryBlockExtension(IPSession session, BitList tasks, BigInteger baseAddress, String memorySpaceID) {
		super(session, tasks);
		fBaseAddress = baseAddress;
		fMemorySpaceID = memorySpaceID;
		fExpression = PMemoryManager.addressToString(baseAddress, memorySpaceID);
	}

	public PMemoryBlockExtension(IPSession session, BitList tasks, String expression, BigInteger baseAddress) {
		this(session, tasks, expression, baseAddress, 1);
	}

	public PMemoryBlockExtension(IPSession session, BitList tasks, String expression, BigInteger baseAddress, int wordSize) {
		super(session, tasks);
		fExpression = expression;
		fBaseAddress = baseAddress;
		fMemorySpaceID = null;
	}

	/**
	 * @param memoryBlock
	 * @param addresses
	 */
	public void changes(IPDIMemoryBlock memoryBlock, BigInteger[] addresses) {
		IPDIMemoryBlock block = getPDIBlock();
		if (block != null && fBytes != null) {
			if (memoryBlock.equals(block)) {
				MemoryByte[] memBytes = (MemoryByte[]) fBytes.clone();
				try {
					BigInteger start = getRealBlockAddress();
					long length = block.getLength();
					byte[] newBytes = block.getBytes();
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
				} catch (PDIException e) {
					DebugPlugin.log(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#connect(java.lang.Object)
	 */
	public void connect(Object object) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#disconnect(java.lang.Object)
	 */
	public void disconnect(Object object) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#dispose()
	 */
	public void dispose() {
		fChanges.clear();
		IPDIMemoryBlock pdiBlock = getPDIBlock();
		if (pdiBlock != null) {
			try {
				getPDISession().getMemoryManager().removeBlocks(getTasks(), new IPDIMemoryBlock[] { pdiBlock });
			} catch (PDIException e) {
				PTPDebugCorePlugin.log(e);
			}
			fPDIBlock = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.model.PDebugElement#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IMemoryBlockRetrieval.class))
			return getMemoryBlockRetrieval();
		return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressableSize()
	 */
	public int getAddressableSize() throws DebugException {
		if (getPDIBlock() == null) {
			try {
				setPDIBlock(createPDIBlock(fBaseAddress, 100));
			} catch (PDIException e) {
				targetRequestFailed(e.getMessage(), null);
			}
		}
		return getPDIBlock().getWordSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressSize()
	 */
	public int getAddressSize() {
		return PDebugUtils.getAddressSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigBaseAddress()
	 */
	public BigInteger getBigBaseAddress() {
		return fBaseAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigLength()
	 */
	public BigInteger getBigLength() throws DebugException {
		IPDIMemoryBlock block = getPDIBlock();
		if (block != null) {
			BigInteger length = new BigInteger(Long.toHexString(block.getLength()), 16);
			return length;
		}
		return BigInteger.ZERO;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long length) throws DebugException {
		IPDIMemoryBlock pdiBlock = getPDIBlock();
		if (pdiBlock == null
				|| pdiBlock.getStartAddress().compareTo(address) > 0
				|| pdiBlock.getStartAddress().add(BigInteger.valueOf(pdiBlock.getLength())).compareTo(
						address.add(BigInteger.valueOf(length))) < 0) {
			synchronized (this) {
				byte[] bytes = null;
				try {
					pdiBlock = getPDIBlock();
					if (pdiBlock == null
							|| pdiBlock.getStartAddress().compareTo(address) > 0
							|| pdiBlock.getStartAddress().add(BigInteger.valueOf(pdiBlock.getLength())).compareTo(
									address.add(BigInteger.valueOf(length))) < 0) {
						if (pdiBlock != null) {
							disposePDIBlock();
							fBytes = null;
						}
						setPDIBlock(createPDIBlock(address, length));
					}
					bytes = getPDIBlock().getBytes();
				} catch (PDIException e) {
					targetRequestFailed(e.getMessage(), null);
				}
				fBytes = new MemoryByte[bytes.length];
				for (int i = 0; i < bytes.length; ++i) {
					fBytes[i] = createMemoryByte(bytes[i], getPDIBlock().getFlags(i), hasChanged(getRealBlockAddress().add(
							BigInteger.valueOf(i))));
				}
			}
		}
		MemoryByte[] result = new MemoryByte[0];
		if (fBytes != null) {
			int offset = address.subtract(getRealBlockAddress()).intValue();
			int offsetInBytes = offset * pdiBlock.getWordSize();
			long lengthInBytes = length * pdiBlock.getWordSize();
			if (offset >= 0) {
				int size = (fBytes.length - offsetInBytes >= lengthInBytes) ? (int) lengthInBytes : fBytes.length - offsetInBytes;
				if (size > 0) {
					result = new MemoryByte[size];
					System.arraycopy(fBytes, offsetInBytes, result, 0, size);
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromOffset(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromOffset(BigInteger unitOffset, long addressableUnits) throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getConnections()
	 */
	public Object[] getConnections() {
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getExpression()
	 */
	public String getExpression() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
	 */
	public long getLength() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockEndAddress()
	 */
	public BigInteger getMemoryBlockEndAddress() throws DebugException {
		return null;// return null to mean not bounded ... according to the spec
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockRetrieval()
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
		return (IMemoryBlockRetrieval) getDebugTarget().getAdapter(IMemoryBlockRetrieval.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockStartAddress()
	 */
	public BigInteger getMemoryBlockStartAddress() throws DebugException {
		return null; // return null to mean not bounded ... according to the
						// spec
	}

	/**
	 * @return
	 */
	public String getMemorySpaceID() {
		return fMemorySpaceID;
	}

	/**
	 * @return
	 */
	public IPDIMemoryBlock getPDIBlock() {
		return fPDIBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
	 */
	public long getStartAddress() {
		return 0;
	}

	/**
	 * 
	 */
	public void resetChanges() {
		if (fBytes != null) {
			BigInteger[] changes = (BigInteger[]) fChanges.toArray(new BigInteger[fChanges.size()]);
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
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setBaseAddress(java.math.BigInteger)
	 */
	public void setBaseAddress(BigInteger address) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setValue(java.math.BigInteger, byte[])
	 */
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
		IPDIMemoryBlock block = getPDIBlock();
		if (block != null) {
			BigInteger base = getBigBaseAddress();
			BigInteger real = getRealBlockAddress();
			long realOffset = base.add(offset).subtract(real).longValue();
			try {
				block.setValue(realOffset, bytes);
			} catch (PDIException e) {
				targetRequestFailed(e.getMessage(), null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
	 */
	public void setValue(long offset, byte[] bytes) throws DebugException {
		setValue(BigInteger.valueOf(offset), bytes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportsChangeManagement()
	 */
	public boolean supportsChangeManagement() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return true;
	}

	/**
	 * @param value
	 * @param pdiFlags
	 * @param changed
	 * @return
	 */
	private MemoryByte createMemoryByte(byte value, byte pdiFlags, boolean changed) {
		byte flags = 0;
		if ((pdiFlags & IPDIMemoryBlock.VALID) != 0) {
			flags |= MemoryByte.HISTORY_KNOWN | MemoryByte.ENDIANESS_KNOWN;
			if ((pdiFlags & IPDIMemoryBlock.READ_ONLY) != 0) {
				flags |= MemoryByte.READABLE;
			} else {
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

	/**
	 * @param address
	 * @param length
	 * @return
	 * @throws PDIException
	 */
	private IPDIMemoryBlock createPDIBlock(BigInteger address, long length) throws PDIException {
		IPDIMemoryBlock block = getPDISession().getMemoryManager().createMemoryBlock(getTasks(), address.toString(), (int) length,
				1);
		block.setFrozen(false);
		return block;
	}

	/**
	 * 
	 */
	private void disposePDIBlock() {
		IPDIMemoryBlock block = getPDIBlock();
		if (block != null) {
			try {
				getPDISession().getMemoryManager().removeBlocks(getTasks(), new IPDIMemoryBlock[] { block });
			} catch (PDIException e) {
				DebugPlugin.log(e);
			}
			setPDIBlock(null);
		}
	}

	/**
	 * @return
	 */
	private long getBlockSize() {
		IPDIMemoryBlock block = getPDIBlock();
		return (block != null) ? block.getLength() : 0;
	}

	/**
	 * @return
	 */
	private BigInteger getRealBlockAddress() {
		IPDIMemoryBlock block = getPDIBlock();
		return (block != null) ? block.getStartAddress() : BigInteger.ZERO;
	}

	/**
	 * @param address
	 * @return
	 */
	private boolean hasChanged(BigInteger address) {
		return fChanges.contains(address);
	}

	/**
	 * @return
	 */
	private boolean isBigEndian() {
		// TODO always true
		return true;
	}

	/**
	 * @param addresses
	 */
	private void saveChanges(BigInteger[] addresses) {
		fChanges.addAll(Arrays.asList(addresses));
	}

	/**
	 * @param pdiBlock
	 */
	private void setPDIBlock(IPDIMemoryBlock pdiBlock) {
		fPDIBlock = pdiBlock;
	}
}
