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
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 *
 */
public class PInstructionPointerAnnotation extends MarkerAnnotation {
	private BitSet tasks = null;
	private Position position = null;
	
	public PInstructionPointerAnnotation(IMarker marker, Position position) {
		super(marker);
		this.position = position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public Position getPosition() {
		return position;
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
		if (tasks.length == 0) {
			setMessage("");
			return;
		}
		int preTask = tasks[0];
		if (tasks.length == 1) {
			setMessage(msg + preTask);
			return;
		}
		boolean isContinue = false;
		for (int i=1; i<tasks.length; i++) {
			if (preTask == (tasks[i] - 1)) {
				if (!isContinue) {
					if (i == 1)
						msg += preTask;
					msg += "-";
				}
				if (i == (tasks.length - 1))
					msg += tasks[i];

				isContinue = true;
			} else {
				isContinue = false;
				msg += preTask + "," + tasks[i];
			}
			preTask = tasks[i];
		}
		setMessage(msg);
	}
	
	public String getMessage() {
		return getText();
	}
	
	public void setTasks(BitSet tasks) {
		this.tasks = tasks;
	}
	public int[] getTasks() {
		return convertArray(tasks);		
	}
	
	public void addTasks(BitSet bitSet) {
		if (tasks == null)
			tasks = new BitSet();

		tasks.or(bitSet);
	}
	public void removeTasks(BitSet bitSet) {
		tasks.andNot(bitSet);
	}
	public boolean isEmpty() {
		return (tasks.cardinality() == 0);
	}
	
	public boolean contains(BitSet bitSet) {
		return tasks.intersects(bitSet);
	}
	public int[] containTasks(BitSet bitSet) {
		bitSet.and(tasks);
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