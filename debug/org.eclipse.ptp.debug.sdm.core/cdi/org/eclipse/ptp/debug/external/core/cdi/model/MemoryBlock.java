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
package org.eclipse.ptp.debug.external.core.cdi.model;

import java.math.BigInteger;
import org.eclipse.ptp.debug.core.ExtFormat;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock;
import org.eclipse.ptp.debug.external.core.cdi.MemoryManager;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.commands.DataWriteMemoryCommand;

/**
 * @author Clement chu
 * 
 */
public class MemoryBlock extends PObject implements IPCDIMemoryBlock {
	String expression;
	boolean frozen;
	boolean dirty;

	private DataReadMemoryInfo mem;
	private int fWordSize;
	private BigInteger cStartAddress; //cached start address
	private byte[] cBytes; //cached bytes
	private int[] badOffsets;
	private boolean fIsLittleEndian;

	/** Constructor
	 * @param target
	 * @param exp
	 * @param wordSize
	 * @param isLittle
	 * @param info
	 */
	public MemoryBlock(Target target, String exp, int wordSize, boolean isLittle, DataReadMemoryInfo info) {
		super(target);
		expression = exp;
		fWordSize = wordSize;
		frozen = true;
		fIsLittleEndian = isLittle;
		setDataReadMemoryInfo(info);
	}
	/**
	 * @return the expression use to create the block.
	 */
	public String getExpression() {
		return expression;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#getWordSize()
	 * @return the size of each memory word in bytes. 
	 */
	public int getWordSize() {
		return fWordSize;
	}
	/**
	 * Reset the internal DataReadMemoryInfo. All modifications into mem info should be don using this method
	 * @param m
	 */
	public void setDataReadMemoryInfo(DataReadMemoryInfo m) {
		cStartAddress = ExtFormat.getBigInteger(m.getAddress());
		cBytes = getBytes(m);
		mem = m;
	}
	/**
	 * @return the internal DataReadMemoryInfo.
	 */
	public DataReadMemoryInfo getDataReadMemoryInfo() {
		return mem;
	}
	/**
	 * @param adds
	 * @return true if any address in the array is within the block.
	 */
	public boolean contains(BigInteger[] adds) {
		for (int i = 0; i < adds.length; i++) {
			if (contains(adds[i])) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param addr
	 * @return true if the address is within the block.
	 */
	public boolean contains(BigInteger addr) {
		BigInteger start = getStartAddress();
		long length = getLength();
		if ( start.compareTo(addr) <= 0 && addr.compareTo(start.add(BigInteger.valueOf(length))) <= 0 ) {
			return true;
		}
		return false;
	}
	/**
	 * Use by the EventManager to check fire events when doing refresh().
	 * @return
	 */
	public boolean isDirty() {
		return dirty;
	}
	/**
	 * Use by the EventManager to check fire events when doing refresh().
	 * @param d
	 */
	public void setDirty(boolean d) {
		dirty = d;
	}
	/**
	 * @param m
	 * @return
	 */
	private byte[] getBytes(DataReadMemoryInfo m) {
		byte[] bytes = new byte[0];

		// sanity.
		if (m == null) {
			return bytes;
		}

		// collect the data
		Memory[] miMem = m.getMemories();
		for (int i = 0; i < miMem.length; ++i) {
			long[] data = miMem[i].getData();
			if (data != null && data.length > 0) {
//				int blen = bytes.length;
//				byte[] newBytes = new byte[blen + data.length];
//				System.arraycopy(bytes, 0, newBytes, 0, blen);
//				for (int j = 0; j < data.length; ++j, ++blen) {
//					newBytes[blen] = (byte)data[j];
//				}
//				bytes = newBytes;
				for (int j = 0; j < data.length; ++j) {
					byte[] bs = longToBytes(data[j]);
					// grow the array
					int blen = bytes.length;
					byte[] newBytes = new byte[blen + bs.length];
					System.arraycopy(bytes, 0, newBytes, 0, blen);
					System.arraycopy(bs, 0, newBytes, blen, bs.length);
					bytes = newBytes;
				}
			}
		}
		return bytes;
	}

	private int[] getBadOffsets(DataReadMemoryInfo m) {
		int[] offsets = new int[0];

		// sanity
		if (m == null) {
			return offsets;
		}

		// collect the data
		Memory[] miMem = m.getMemories();
		for (int i = 0; i < miMem.length; i++) {
			int[] data = miMem[i].getBadOffsets();
			if (data.length > 0) {
				int olen = offsets.length;
				int[] newOffsets = new int[olen + data.length];
				System.arraycopy(offsets, 0, newOffsets, 0, olen);
				System.arraycopy(data, 0, newOffsets, olen, data.length);
				offsets = newOffsets;
			}
		}
		return offsets;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws PCDIException {
		return cBytes;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#refresh()
	 */
	public void refresh() throws PCDIException {
		Target target = (Target)getTarget();
		MemoryManager mgr = ((Session)target.getSession()).getMemoryManager();
		setDirty(true);
		BigInteger[] addresses = mgr.update(this, null);
		// Check if this affects other blocks.
		if (addresses.length > 0) {
			IPCDIMemoryBlock[] blocks = mgr.getMemoryBlocks(target);
			for (int i = 0; i < blocks.length; i++) {
				MemoryBlock block = (MemoryBlock)blocks[i];
				if (! block.equals(this) && block.contains(addresses)) {
					block.setDirty(true);
					mgr.update(block, null);
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#getLength()
	 */
	public long getLength() {
		try {
			// use this instead.  If the wordSize
			// given does not match the hardware,
			// counting the bytes will be correct.
			return getBytes().length;
		} catch (PCDIException e) {
			// ignore.
		}
		return mem.getTotalBytes();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#getStartAddress()
	 */
	public BigInteger getStartAddress() {
		return cStartAddress;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#isFrozen()
	 */
	public boolean isFrozen() {
		return frozen;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#setFrozen(boolean)
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#setValue(long, byte[])
	 */
	public void setValue(long offset, byte[] bytes) throws PCDIException {
		if (offset >= getLength() || offset + bytes.length > getLength()) {
			throw new PCDIException("No bad offset found");
		}
		
		Target target = (Target)getTarget();
		for (int i = 0; i < bytes.length; i++) {
			long l = new Byte(bytes[i]).longValue() & 0xff;
			String value = "0x" + Long.toHexString(l);
PDebugUtils.println("----------- DataWriteMemoryCommand is called --------------");
			DataWriteMemoryCommand command = new DataWriteMemoryCommand(target.getTask(), offset + i, expression, ExtFormat.HEXADECIMAL, 1, value);
			target.getDebugger().postCommand(command);
			if (command.getDataWriteMemoryInfo() == null) {
				throw new PCDIException("No response");
			}
		}
		// If the assign was succesfull fire a MIChangedEvent() via refresh.
		refresh();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock#getFlags(int)
	 */
	public synchronized byte getFlags(int offset) {
		if (offset < 0 || offset >= getLength()) {
			throw new IndexOutOfBoundsException();
		}
		if (badOffsets == null) {
			badOffsets = getBadOffsets(mem);
		}
		if (badOffsets != null) {
			for (int i = 0; i < badOffsets.length; ++i) {
				if (badOffsets[i] == offset) {
					return 0;
				}
			}
		}
		return VALID;
	}

	/**
	 * We should use the wordSize ... but ...
	 * The problem: the user may not have the right wordsize
	 * For example on some DSP the user set the wordSize to be 1 byte
	 * but in fact GDB is reading 2 bytes.
	 * So let do some guessing since the data(long) may have a bigger value then one byte.
	 */
	private byte[] longToBytes(long v) {		
		// Calculate the number of bytes needed
		int count = 1;
		long value = v;
		for (count = 1; (value /= 0x100) > 0; ++count)
			;

		// Reset the wordSize if incorrect.
		if (fWordSize != count) {
			fWordSize = count;
		}

		byte[] bytes = new byte[count];
		if (fIsLittleEndian) {
			for (int i = count - 1; i >= 0; --i) {
				int shift = i * count;
				bytes[i] = (byte)((v >>> shift) & 0xFF);
			}
//			bytes[7] = (byte)((v >>> 56) & 0xFF);
//			bytes[6] = (byte)((v >>> 48) & 0xFF);
//			bytes[5] = (byte)((v >>> 40) & 0xFF);
//			bytes[4] = (byte)((v >>> 32) & 0xFF);
//			bytes[3] = (byte)((v >>> 24) & 0xFF);
//			bytes[2] = (byte)((v >>> 16) & 0xFF);
//			bytes[1] = (byte)((v >>>  8) & 0xFF);
//			bytes[0] = (byte)((v >>>  0) & 0xFF);			
		} else {
			for (int i = 0; i < count; ++i) {
				int shift = (count - i - 1) * count;
				bytes[i] = (byte)((v >>> shift) & 0xFF);
			}
//			bytes[0] = (byte)((v >>> 56) & 0xFF);
//			bytes[1] = (byte)((v >>> 48) & 0xFF);
//			bytes[2] = (byte)((v >>> 40) & 0xFF);
//			bytes[3] = (byte)((v >>> 32) & 0xFF);
//			bytes[4] = (byte)((v >>> 24) & 0xFF);
//			bytes[5] = (byte)((v >>> 16) & 0xFF);
//			bytes[6] = (byte)((v >>>  8) & 0xFF);
//			bytes[7] = (byte)((v >>>  0) & 0xFF);			
		}
		return bytes;
	}
}
