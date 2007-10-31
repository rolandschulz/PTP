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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIMemoryManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.external.core.ExtFormat;
import org.eclipse.ptp.debug.internal.core.pdi.event.ChangedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.CreatedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.MemoryBlockInfo;
import org.eclipse.ptp.debug.internal.core.pdi.model.DataReadMemoryInfo;
import org.eclipse.ptp.debug.internal.core.pdi.model.MemoryBlock;
import org.eclipse.ptp.debug.internal.core.pdi.request.DataReadMemoryRequest;

/**
 * @author Clement chu
 * 
 */
public class MemoryManager extends Manager implements IPDIMemoryManager {
	IPDIMemoryBlock[] EMPTY_MEMORY_BLOCKS = {};
	Map<BitList, List<IPDIMemoryBlock>> blockMap;

	public MemoryManager(Session session) {
		super(session, true);
		blockMap = new Hashtable<BitList, List<IPDIMemoryBlock>>();
	}
	public void shutdown() {
		blockMap.clear();
	}
	private synchronized List<IPDIMemoryBlock> getMemoryBlockList(BitList qTasks) {
		List<IPDIMemoryBlock> blockList = blockMap.get(qTasks);
		if (blockList == null) {
			blockList = Collections.synchronizedList(new ArrayList<IPDIMemoryBlock>());
			blockMap.put(qTasks, blockList);
		}
		return blockList;
	}
	public void update(BitList qTasks) {
		List<IPDIMemoryBlock> blockList = getMemoryBlockList(qTasks);
		MemoryBlock[] blocks = (MemoryBlock[]) blockList.toArray(new MemoryBlock[blockList.size()]);
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>(blocks.length);
		for (int i = 0; i < blocks.length; i++) {
			if (! blocks[i].isFrozen()) {
				try {
					update(blocks[i], eventList);
				} catch (PDIException e) {
				}
			}
		}
		IPDIEvent[] events = (IPDIEvent[])eventList.toArray(new IPDIEvent[0]);
		session.getEventManager().fireEvents(events);
	}
	public BigInteger[] update(MemoryBlock block, List<IPDIEvent> aList) throws PDIException {
		MemoryBlock newBlock = cloneBlock(block);
		boolean newAddress = ! newBlock.getStartAddress().equals(block.getStartAddress());
		BigInteger[] array = compareBlocks(block, newBlock);
		block.setDataReadMemoryInfo(newBlock.getDataReadMemoryInfo());
		if (array.length > 0 || newAddress) {
			IPDIEvent event = new ChangedEvent(new MemoryBlockInfo(session, block.getTasks(), array, block));
			if (aList != null) {
				aList.add(event);
			} else {
				session.getEventManager().fireEvents(new IPDIEvent[] { event });
			}
		}
		return array;
	}
	BigInteger[] compareBlocks(MemoryBlock oldBlock, MemoryBlock newBlock) throws PDIException {
		byte[] oldBytes = oldBlock.getBytes();
		byte[] newBytes = newBlock.getBytes();
		List<BigInteger> aList = new ArrayList<BigInteger>(newBytes.length);
		BigInteger distance = newBlock.getStartAddress().subtract(oldBlock.getStartAddress());
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
	MemoryBlock cloneBlock(MemoryBlock block) throws PDIException {
		String exp = block.getExpression();
		int wordSize = block.getWordSize();
		DataReadMemoryInfo info = createDataReadMemoryInfo(block.getTasks(), exp, (int)block.getLength(), wordSize);
		return new MemoryBlock(session, block.getTasks(), exp, wordSize, true, info);
	}
	DataReadMemoryInfo createDataReadMemoryInfo(BitList qTasks, String exp, int units, int wordSize) throws PDIException {
		DataReadMemoryRequest request = new DataReadMemoryRequest(session, qTasks, 0, exp, ExtFormat.HEXADECIMAL, wordSize, 1, units, null);
		session.getEventRequestManager().addEventRequest(request);
		return request.getDataReadMemoryInfo(qTasks);
	}
	public IPDIMemoryBlock createMemoryBlock(BitList qTasks, String address, int units, int wordSize) throws PDIException {
		DataReadMemoryInfo info = createDataReadMemoryInfo(qTasks, address, units, wordSize);
		IPDIMemoryBlock block = new MemoryBlock(session, qTasks, address, wordSize, true, info);
		List<IPDIMemoryBlock> blockList = getMemoryBlockList(qTasks);
		blockList.add(block);
		IPDIEvent event = new CreatedEvent(new MemoryBlockInfo(session, qTasks, new BigInteger[] { block.getStartAddress() }, block));
		session.getEventManager().fireEvents(new IPDIEvent[] { event });
		return block;
	}
	public IPDIMemoryBlock[] getMemoryBlocks(BitList qTasks) throws PDIException {
		List<IPDIMemoryBlock> blockList = getMemoryBlockList(qTasks);
		return (IPDIMemoryBlock[]) blockList.toArray(new IPDIMemoryBlock[blockList.size()]);
	}
	public void removeAllBlocks(BitList qTasks) throws PDIException {
		IPDIMemoryBlock[] blocks = getMemoryBlocks(qTasks);
		removeBlocks(qTasks, blocks);
	}
	public void removeBlocks(BitList qTasks, IPDIMemoryBlock[] memoryBlocks) throws PDIException {
		List<IPDIMemoryBlock> blockList = blockMap.get(qTasks);
		if (blockList != null) {
			blockList.removeAll(Arrays.asList(memoryBlocks));
		}
	}
}
