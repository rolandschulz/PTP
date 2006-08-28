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
package org.eclipse.ptp.debug.external.core.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.ptp.debug.core.ExtFormat;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock;
import org.eclipse.ptp.debug.external.core.cdi.event.MemoryChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.DataReadMemoryInfo;
import org.eclipse.ptp.debug.external.core.cdi.model.MemoryBlock;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.commands.DataReadMemoryCommand;

/**
 * @author Clement chu
 * 
 */
public class MemoryManager extends Manager {
	IPCDIMemoryBlock[] EMPTY_MEMORY_BLOCKS = {};
	Map blockMap;

	/** Constructor
	 * @param session
	 */
	public MemoryManager(Session session) {
		super(session, true);
		blockMap = new Hashtable();
	}
	public void shutdown() {
		blockMap.clear();
	}
	/** Get memory block list
	 * @param target
	 * @return
	 */
	synchronized List getMemoryBlockList(Target target) {
		List blockList = (List)blockMap.get(target);
		if (blockList == null) {
			blockList = Collections.synchronizedList(new ArrayList());
			blockMap.put(target, blockList);
		}
		return blockList;
	}

	/* (non-Javadoc)
	 * This method will be call by the eventManager.processSuspended() every time the
	 * inferior comes to a Stop/Suspended.  It will allow to look at the blocks that
	 * are registered and fired any event if changed.
	 * Note: Frozen blocks are not updated.
	 * @see org.eclipse.ptp.debug.external.core.cdi.Manager#update(org.eclipse.ptp.debug.external.core.cdi.model.Target)
	 */
	public void update(Target target) {
		List blockList = getMemoryBlockList(target);
		MemoryBlock[] blocks = (MemoryBlock[]) blockList.toArray(new MemoryBlock[blockList.size()]);
		List eventList = new ArrayList(blocks.length);
		for (int i = 0; i < blocks.length; i++) {
			if (! blocks[i].isFrozen()) {
				try {
					update(blocks[i], eventList);
				} catch (PCDIException e) {
				}
			}
		}
		IPCDIEvent[] events = (IPCDIEvent[])eventList.toArray(new IPCDIEvent[0]);
		target.getDebugger().fireEvents(events);
	}

	/** update one Block.
	 * @param block
	 * @param aList
	 * @return
	 * @throws PCDIException
	 */
	public BigInteger[] update(MemoryBlock block, List aList) throws PCDIException {
		MemoryBlock newBlock = cloneBlock(block);
		boolean newAddress = ! newBlock.getStartAddress().equals(block.getStartAddress());
		BigInteger[] array = compareBlocks(block, newBlock);
		// Update the block MIDataReadMemoryInfo.
		block.setDataReadMemoryInfo(newBlock.getDataReadMemoryInfo());
		Target target = (Target)block.getTarget();
		if (array.length > 0 || newAddress) {
			if (aList != null) {
				aList.add(new MemoryChangedEvent(target.getSession(), target.getTask(), block, array));
			} else {
				// fire right away.
				target.getDebugger().fireEvent(new MemoryChangedEvent(target.getSession(), target.getTask(), block, array));
			}
		}
		return array;
	}

	/**
	 * Compare two blocks and return an array of all _addresses_ that are different.
	 * This method is not smart it always assume that:
	 * @param oldBlock - oldBlock.getStartAddress() == newBlock.getStartAddress;
	 * @param newBlock - oldBlock.getLength() == newBlock.getLength();
	 * @return Long[] array of modified addresses.
	 * @throws PCDIException
	 */
	BigInteger[] compareBlocks(MemoryBlock oldBlock, MemoryBlock newBlock) throws PCDIException {
		byte[] oldBytes = oldBlock.getBytes();
		byte[] newBytes = newBlock.getBytes();
		List aList = new ArrayList(newBytes.length);
		BigInteger distance = newBlock.getStartAddress().subtract(oldBlock.getStartAddress());
		//IPF_TODO enshure it is OK here
		int diff = distance.intValue();
		if ( Math.abs(diff) <  newBytes.length) {
			for (int i = 0; i < newBytes.length; i++) {
				if (i + diff < oldBytes.length && i + diff >= 0) {
					if (oldBytes[i + diff] != newBytes[i]) {
						aList.add(newBlock.getStartAddress().add(BigInteger.valueOf(i)));
					}
				}
			}
		}
		return (BigInteger[]) aList.toArray(new BigInteger[aList.size()]);
	}
	/**
	 * Use the same expression and length of the original block
	 * to create a new MemoryBlock.  The new block is not register
	 * with the MemoryManager.
	 * @param block
	 * @return
	 * @throws PCDIException
	 */
	MemoryBlock cloneBlock(MemoryBlock block) throws PCDIException {
		Target target = (Target)block.getTarget();
		String exp = block.getExpression();
		int wordSize = block.getWordSize();
		boolean little = target.isLittleEndian();
		DataReadMemoryInfo info = createDataReadMemoryInfo(target, exp, (int)block.getLength(), wordSize);
		return new MemoryBlock(target, exp, wordSize, little, info);
	}

	/**
	 * Post a -data-read-memory to gdb/mi.
	 */
	DataReadMemoryInfo createDataReadMemoryInfo(Target target, String exp, int units, int wordSize) throws PCDIException {
		DataReadMemoryCommand command = new DataReadMemoryCommand(target.getTask(), 0, exp, ExtFormat.HEXADECIMAL, wordSize, 1, units, null);
		target.getDebugger().postCommand(command);
		DataReadMemoryInfo info = command.getDataReadMemoryInfo();
		if (info == null) {
			throw new PCDIException("No data memory info found");
		}
		return info;
	}

	public IPCDIMemoryBlock createMemoryBlock(Target target, String address, int units, int wordSize) throws PCDIException {
		boolean little = target.isLittleEndian();
		DataReadMemoryInfo info = createDataReadMemoryInfo(target, address, units, wordSize);
		IPCDIMemoryBlock block = new MemoryBlock(target, address, wordSize, little, info);
		List blockList = getMemoryBlockList(target);
		blockList.add(block);
		//IPCDISession session = target.getSession();
		//session.getDebugger().fireEvent(new MemoryCreatedEvent(session, block.getStartAddress(), block.getLength()));
		return block;
	}
	public IPCDIMemoryBlock[] getMemoryBlocks(Target target) throws PCDIException {
		List blockList = getMemoryBlockList(target);
		return (IPCDIMemoryBlock[]) blockList.toArray(new IPCDIMemoryBlock[blockList.size()]);
	}

	public void removeAllBlocks(Target target) throws PCDIException {
		IPCDIMemoryBlock[] blocks = getMemoryBlocks(target);
		removeBlocks(target, blocks);
	}

	public void removeBlocks(Target target, IPCDIMemoryBlock[] memoryBlocks) throws PCDIException {
		List blockList = (List)blockMap.get(target);
		if (blockList != null) {
			blockList.removeAll(Arrays.asList(memoryBlocks));
		}
	}
}
