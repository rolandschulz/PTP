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
package org.eclipse.ptp.internal.debug.core.pdi.manager;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataReadMemoryRequest;
import org.eclipse.ptp.internal.debug.core.ExtFormat;

/**
 * @author Clement chu
 * 
 */
public class MemoryManager extends AbstractPDIManager implements IPDIMemoryManager {
	IPDIMemoryBlock[] EMPTY_MEMORY_BLOCKS = {};
	Map<TaskSet, List<IPDIMemoryBlock>> blockMap;

	public MemoryManager(IPDISession session) {
		super(session, true);
		blockMap = new Hashtable<TaskSet, List<IPDIMemoryBlock>>();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager#createMemoryBlock(org.eclipse.ptp.core.util.TaskSet, java.lang.String, int, int)
	 */
	public IPDIMemoryBlock createMemoryBlock(TaskSet qTasks, String address, int units, int wordSize) throws PDIException {
		IPDIDataReadMemoryInfo info = createDataReadMemoryInfo(qTasks, address, units, wordSize);
		IPDIMemoryBlock block = session.getModelFactory().newMemoryBlock(session, qTasks, address, wordSize, true, info);
		List<IPDIMemoryBlock> blockList = getMemoryBlockList(qTasks);
		blockList.add(block);
		IPDIEvent event = session.getEventFactory().newCreatedEvent(
				session.getEventFactory().newMemoryBlockInfo(session, qTasks, new BigInteger[] { block.getStartAddress() }, block));
		session.getEventManager().fireEvents(new IPDIEvent[] { event });
		return block;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager#getMemoryBlocks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDIMemoryBlock[] getMemoryBlocks(TaskSet qTasks) throws PDIException {
		List<IPDIMemoryBlock> blockList = getMemoryBlockList(qTasks);
		return (IPDIMemoryBlock[]) blockList.toArray(new IPDIMemoryBlock[blockList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager#removeAllBlocks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void removeAllBlocks(TaskSet qTasks) throws PDIException {
		IPDIMemoryBlock[] blocks = getMemoryBlocks(qTasks);
		removeBlocks(qTasks, blocks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager#removeBlocks(org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock[])
	 */
	public void removeBlocks(TaskSet qTasks, IPDIMemoryBlock[] memoryBlocks) throws PDIException {
		List<IPDIMemoryBlock> blockList = blockMap.get(qTasks);
		if (blockList != null) {
			blockList.removeAll(Arrays.asList(memoryBlocks));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		blockMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void update(TaskSet qTasks) {
		List<IPDIMemoryBlock> blockList = getMemoryBlockList(qTasks);
		IPDIMemoryBlock[] blocks = (IPDIMemoryBlock[]) blockList.toArray(new IPDIMemoryBlock[blockList.size()]);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager#update(org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock, java.util.List)
	 */
	public BigInteger[] update(IPDIMemoryBlock block, List<IPDIEvent> aList) throws PDIException {
		IPDIMemoryBlock newBlock = cloneBlock(block);
		boolean newAddress = ! newBlock.getStartAddress().equals(block.getStartAddress());
		BigInteger[] array = compareBlocks(block, newBlock);
		block.setDataReadMemoryInfo(newBlock.getDataReadMemoryInfo());
		if (array.length > 0 || newAddress) {
			IPDIEvent event = session.getEventFactory().newChangedEvent(
					session.getEventFactory().newMemoryBlockInfo(session, block.getTasks(), array, block));
			if (aList != null) {
				aList.add(event);
			} else {
				session.getEventManager().fireEvents(new IPDIEvent[] { event });
			}
		}
		return array;
	}
	
	/**
	 * @param block
	 * @return
	 * @throws PDIException
	 */
	private IPDIMemoryBlock cloneBlock(IPDIMemoryBlock block) throws PDIException {
		String exp = block.getExpression();
		int wordSize = block.getWordSize();
		IPDIDataReadMemoryInfo info = createDataReadMemoryInfo(block.getTasks(), exp, (int)block.getLength(), wordSize);
		return session.getModelFactory().newMemoryBlock(session, block.getTasks(), exp, wordSize, true, info);
	}
	
	/**
	 * @param oldBlock
	 * @param newBlock
	 * @return
	 * @throws PDIException
	 */
	private BigInteger[] compareBlocks(IPDIMemoryBlock oldBlock, IPDIMemoryBlock newBlock) throws PDIException {
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
	
	/**
	 * @param qTasks
	 * @param exp
	 * @param units
	 * @param wordSize
	 * @return
	 * @throws PDIException
	 */
	private IPDIDataReadMemoryInfo createDataReadMemoryInfo(TaskSet qTasks, String exp, int units, int wordSize) throws PDIException {
		IPDIDataReadMemoryRequest request = session.getRequestFactory().getDataReadMemoryRequest(session, qTasks, 0, exp, ExtFormat.HEXADECIMAL, wordSize, 1, units, null);
		session.getEventRequestManager().addEventRequest(request);
		return request.getDataReadMemoryInfo(qTasks);
	}
	
	/**
	 * @param qTasks
	 * @return
	 */
	private synchronized List<IPDIMemoryBlock> getMemoryBlockList(TaskSet qTasks) {
		List<IPDIMemoryBlock> blockList = blockMap.get(qTasks);
		if (blockList == null) {
			blockList = Collections.synchronizedList(new ArrayList<IPDIMemoryBlock>());
			blockMap.put(qTasks, blockList);
		}
		return blockList;
	}
}
