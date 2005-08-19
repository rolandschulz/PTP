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
package org.eclipse.ptp.debug.internal.ui;

import java.util.BitSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 *
 */
public class PInstructionPointerAnnotation extends MarkerAnnotation {
	private BitSet tBitSet = null;
	
	public PInstructionPointerAnnotation(IMarker marker) {
		super(marker);
	}
	
	public void setMessage(String message) {
		try {
			getMarker().setAttribute(IMarker.MESSAGE, message);
		} catch (CoreException e) {}
		setText(message);
	}
	public void setMessage() {
		String msg = "Suspended on process: ";
		int[] tasks = getTasks();
		for (int i=0; i<tasks.length; i++) {
			msg += tasks[i];
			if (i < tasks.length - 1)
				msg += ", ";
		}
		setMessage(msg);
	}
	
	public String getMessage() {
		return getText();
	}
	
	public void setTasks(BitSet tBitSet) {
		this.tBitSet = tBitSet;
	}
	public int[] getTasks() {
		return convertArray(tBitSet);		
	}
	
	public void addTasks(BitSet bitSet) {
		if (tBitSet == null)
			tBitSet = new BitSet();

		tBitSet.or(bitSet);
	}
	public void removeTasks(BitSet bitSet) {
		tBitSet.andNot(bitSet);
	}
	public boolean contains(BitSet bitSet) {
		return tBitSet.intersects(bitSet);
	}
	public int[] containTasks(BitSet bitSet) {
		bitSet.and(tBitSet);
		return convertArray(bitSet);		
	}
	public int[] convertArray(BitSet bitSet) {
		int[] intArray = new int[bitSet.cardinality()];
		for(int i=bitSet.nextSetBit(0), j=0; i>=0; i=bitSet.nextSetBit(i+1), j++) {
			intArray[j] = i;
		}
		return intArray;		
	}
}