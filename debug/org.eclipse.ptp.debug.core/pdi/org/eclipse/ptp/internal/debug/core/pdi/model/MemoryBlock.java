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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataWriteMemoryRequest;
import org.eclipse.ptp.internal.debug.core.ExtFormat;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public class MemoryBlock extends SessionObject implements IPDIMemoryBlock {
	private IPDIDataReadMemoryInfo mem;
	private int fWordSize;
	private BigInteger pStartAddress; // cached start address

	private byte[] cBytes; // cached bytes
	private int[] badOffsets;
	private final boolean fIsLittleEndian;
	String expression;
	boolean frozen;

	public MemoryBlock(IPDISession session, TaskSet tasks, String exp, int wordSize, boolean isLittle, IPDIDataReadMemoryInfo info) {
		super(session, tasks);
		expression = exp;
		fWordSize = wordSize;
		frozen = true;
		fIsLittleEndian = isLittle;
		setDataReadMemoryInfo(info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws PDIException {
		return cBytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getDataReadMemoryInfo()
	 */
	public IPDIDataReadMemoryInfo getDataReadMemoryInfo() {
		return mem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getExpression()
	 */
	public String getExpression() {
		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getFlags(int)
	 */
	public synchronized byte getFlags(int offset) {
		if (offset < 0 || offset >= getLength()) {
			throw new IndexOutOfBoundsException();
		}
		if (badOffsets == null) {
			badOffsets = getBadOffsets(mem);
		}
		if (badOffsets != null) {
			for (int badOffset : badOffsets) {
				if (badOffset == offset) {
					return 0;
				}
			}
		}
		return VALID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getLength()
	 */
	public long getLength() {
		try {
			return getBytes().length;
		} catch (PDIException e) {
			// ignore.
		}
		return mem.getTotalBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getStartAddress()
	 */
	public BigInteger getStartAddress() {
		return pStartAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#getWordSize()
	 */
	public int getWordSize() {
		return fWordSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#isFrozen()
	 */
	public boolean isFrozen() {
		return frozen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#refresh()
	 */
	public void refresh() throws PDIException {
		IPDIMemoryManager mgr = session.getMemoryManager();
		BigInteger[] addresses = mgr.update(this, null);
		// Check if this affects other blocks.
		if (addresses.length > 0) {
			IPDIMemoryBlock[] blocks = mgr.getMemoryBlocks(getTasks());
			for (IPDIMemoryBlock block2 : blocks) {
				MemoryBlock block = (MemoryBlock) block2;
				if (!block.equals(this) && block.contains(addresses)) {
					mgr.update(block, null);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#setDataReadMemoryInfo(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIDataReadMemoryInfo)
	 */
	public void setDataReadMemoryInfo(IPDIDataReadMemoryInfo m) {
		pStartAddress = ExtFormat.getBigInteger(m.getAddress());
		cBytes = getBytes(m);
		mem = m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#setFrozen(boolean)
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock#setValue(long, byte[])
	 */
	public void setValue(long offset, byte[] bytes) throws PDIException {
		if (offset >= getLength() || offset + bytes.length > getLength()) {
			throw new PDIException(getTasks(), Messages.MemoryBlock_0);
		}
		for (int i = 0; i < bytes.length; i++) {
			long l = new Byte(bytes[i]).longValue() & 0xff;
			String value = "0x" + Long.toHexString(l); //$NON-NLS-1$
			IPDIDataWriteMemoryRequest request = session.getRequestFactory().getDataWriteMemoryRequest(getTasks(), offset + i,
					expression, ExtFormat.HEXADECIMAL, 1, value);
			session.getEventRequestManager().addEventRequest(request);
			request.waitUntilCompleted(getTasks());
		}
		refresh();

		IPDIRegisterManager regMgr = session.getRegisterManager();
		if (regMgr.isAutoUpdate()) {
			regMgr.update(getTasks());
		}

		IPDIExpressionManager expMgr = session.getExpressionManager();
		if (expMgr.isAutoUpdate()) {
			expMgr.update(getTasks());
		}

		IPDIVariableManager varMgr = session.getVariableManager();
		if (varMgr.isAutoUpdate()) {
			varMgr.update(getTasks());
		}
	}

	/**
	 * @param addr
	 * @return
	 */
	private boolean contains(BigInteger addr) {
		BigInteger start = getStartAddress();
		long length = getLength();
		if (start.compareTo(addr) <= 0 && addr.compareTo(start.add(BigInteger.valueOf(length))) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * @param adds
	 * @return
	 */
	private boolean contains(BigInteger[] adds) {
		for (BigInteger add : adds) {
			if (contains(add)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param m
	 * @return
	 */
	private int[] getBadOffsets(IPDIDataReadMemoryInfo m) {
		int[] offsets = new int[0];
		if (m == null) {
			return offsets;
		}

		IPDIMemory[] miMem = m.getMemories();
		for (IPDIMemory element : miMem) {
			int[] data = element.getBadOffsets();
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

	/**
	 * @param m
	 * @return
	 */
	private byte[] getBytes(IPDIDataReadMemoryInfo m) {
		byte[] bytes = new byte[0];
		if (m == null) {
			return bytes;
		}

		IPDIMemory[] miMem = m.getMemories();
		for (IPDIMemory element : miMem) {
			long[] data = element.getData();
			if (data != null && data.length > 0) {
				for (long element2 : data) {
					byte[] bs = longToBytes(element2);
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

	/**
	 * @param v
	 * @return
	 */
	private byte[] longToBytes(long v) {
		// Calculate the number of bytes needed
		int count = 1;
		long value = v;
		for (count = 1; (value /= 0x100) > 0; ++count) {
			;
		}

		// Reset the wordSize if incorrect.
		if (fWordSize != count) {
			fWordSize = count;
		}

		byte[] bytes = new byte[count];
		if (fIsLittleEndian) {
			for (int i = count - 1; i >= 0; --i) {
				int shift = i * count;
				bytes[i] = (byte) ((v >>> shift) & 0xFF);
			}
			// bytes[7] = (byte)((v >>> 56) & 0xFF);
			// bytes[6] = (byte)((v >>> 48) & 0xFF);
			// bytes[5] = (byte)((v >>> 40) & 0xFF);
			// bytes[4] = (byte)((v >>> 32) & 0xFF);
			// bytes[3] = (byte)((v >>> 24) & 0xFF);
			// bytes[2] = (byte)((v >>> 16) & 0xFF);
			// bytes[1] = (byte)((v >>> 8) & 0xFF);
			// bytes[0] = (byte)((v >>> 0) & 0xFF);
		} else {
			for (int i = 0; i < count; ++i) {
				int shift = (count - i - 1) * count;
				bytes[i] = (byte) ((v >>> shift) & 0xFF);
			}
			// bytes[0] = (byte)((v >>> 56) & 0xFF);
			// bytes[1] = (byte)((v >>> 48) & 0xFF);
			// bytes[2] = (byte)((v >>> 40) & 0xFF);
			// bytes[3] = (byte)((v >>> 32) & 0xFF);
			// bytes[4] = (byte)((v >>> 24) & 0xFF);
			// bytes[5] = (byte)((v >>> 16) & 0xFF);
			// bytes[6] = (byte)((v >>> 8) & 0xFF);
			// bytes[7] = (byte)((v >>> 0) & 0xFF);
		}
		return bytes;
	}
}
