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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import java.math.BigInteger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.external.core.ExtFormat;
import org.eclipse.ptp.debug.internal.core.pdi.ExpressionManager;
import org.eclipse.ptp.debug.internal.core.pdi.MemoryManager;
import org.eclipse.ptp.debug.internal.core.pdi.RegisterManager;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;
import org.eclipse.ptp.debug.internal.core.pdi.VariableManager;
import org.eclipse.ptp.debug.internal.core.pdi.request.DataWriteMemoryRequest;

/**
 * @author Clement chu
 * 
 */
public class MemoryBlock extends SessionObject implements IPDIMemoryBlock {
	String expression;
	boolean frozen;
	boolean dirty;

	private DataReadMemoryInfo mem;
	private int fWordSize;
	private BigInteger pStartAddress; //cached start address
	private byte[] cBytes; //cached bytes
	private int[] badOffsets;
	private boolean fIsLittleEndian;

	public MemoryBlock(Session session, BitList tasks, String exp, int wordSize, boolean isLittle, DataReadMemoryInfo info) {
		super(session, tasks);
		expression = exp;
		fWordSize = wordSize;
		frozen = true;
		fIsLittleEndian = isLittle;
		setDataReadMemoryInfo(info);
	}
	public String getExpression() {
		return expression;
	}
	public int getWordSize() {
		return fWordSize;
	}
	public void setDataReadMemoryInfo(DataReadMemoryInfo m) {
		pStartAddress = ExtFormat.getBigInteger(m.getAddress());
		cBytes = getBytes(m);
		mem = m;
	}
	public DataReadMemoryInfo getDataReadMemoryInfo() {
		return mem;
	}
	public boolean contains(BigInteger[] adds) {
		for (int i = 0; i < adds.length; i++) {
			if (contains(adds[i])) {
				return true;
			}
		}
		return false;
	}
	public boolean contains(BigInteger addr) {
		BigInteger start = getStartAddress();
		long length = getLength();
		if ( start.compareTo(addr) <= 0 && addr.compareTo(start.add(BigInteger.valueOf(length))) <= 0 ) {
			return true;
		}
		return false;
	}
	public boolean isDirty() {
		return dirty;
	}
	public void setDirty(boolean d) {
		dirty = d;
	}
	private byte[] getBytes(DataReadMemoryInfo m) {
		byte[] bytes = new byte[0];
		if (m == null) {
			return bytes;
		}

		Memory[] miMem = m.getMemories();
		for (int i = 0; i < miMem.length; ++i) {
			long[] data = miMem[i].getData();
			if (data != null && data.length > 0) {
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
		if (m == null) {
			return offsets;
		}

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
	public byte[] getBytes() throws PDIException {
		return cBytes;
	}
	public void refresh() throws PDIException {
		MemoryManager mgr = session.getMemoryManager();
		setDirty(true);
		BigInteger[] addresses = mgr.update(this, null);
		// Check if this affects other blocks.
		if (addresses.length > 0) {
			IPDIMemoryBlock[] blocks = mgr.getMemoryBlocks(getTasks());
			for (int i = 0; i < blocks.length; i++) {
				MemoryBlock block = (MemoryBlock)blocks[i];
				if (! block.equals(this) && block.contains(addresses)) {
					block.setDirty(true);
					mgr.update(block, null);
				}
			}
		}
	}
	public long getLength() {
		try {
			return getBytes().length;
		} catch (PDIException e) {
			// ignore.
		}
		return mem.getTotalBytes();
	}
	public BigInteger getStartAddress() {
		return pStartAddress;
	}
	public boolean isFrozen() {
		return frozen;
	}
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	public void setValue(long offset, byte[] bytes) throws PDIException {
		if (offset >= getLength() || offset + bytes.length > getLength()) {
			throw new PDIException(getTasks(), "No bad offset found");
		}
		for (int i = 0; i < bytes.length; i++) {
			long l = new Byte(bytes[i]).longValue() & 0xff;
			String value = "0x" + Long.toHexString(l);
			DataWriteMemoryRequest request = new DataWriteMemoryRequest(session, getTasks(), offset + i, expression, ExtFormat.HEXADECIMAL, 1, value);
			session.getEventRequestManager().addEventRequest(request);
			request.waitUntilCompleted(getTasks());
		}
		refresh();

		RegisterManager regMgr = session.getRegisterManager();
		if (regMgr.isAutoUpdate()) {
			regMgr.update(getTasks());
		}
		
		ExpressionManager expMgr = session.getExpressionManager();
		if (expMgr.isAutoUpdate()) {
			expMgr.update(getTasks());
		}
		
		VariableManager varMgr = session.getVariableManager();
		if (varMgr.isAutoUpdate()) {
			varMgr.update(getTasks());
		}
	}
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
