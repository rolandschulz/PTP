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

package org.eclipse.ptp.debug.core;

import java.util.BitSet;

/**
 * Represents a set of tasks (processes) that can be controlled as a group by the debugger
 * 
 * @since 4.0
 */
public class TaskSet extends BitSet {
	private static final long serialVersionUID = 8213303688193251561L;

	private final int fNumberOfTasks;

	/**
	 * Creates a new task set. The set is initially empty.
	 */
	public TaskSet() {
		super();
		fNumberOfTasks = size();
	}

	/**
	 * Creates a task set whose initial size is large enough to explicitly
	 * represent tasks with indices in the range <code>0</code> through <code>ntasks-1</code>. All tasks are initially
	 * <code>false</code>.
	 * 
	 * @param ntasks
	 *            the initial size of the task set.
	 * @exception NegativeArraySizeException
	 *                if the specified initial size is negative.
	 */
	public TaskSet(int ntasks) {
		super(ntasks);
		fNumberOfTasks = ntasks;
	}

	/**
	 * Construct a TaskSet and initialize from a hex string representation.
	 * 
	 * @param nbits
	 *            number of tasks in the {@code TaskSet}
	 * @param str
	 *            hex string representation of tasks in the {@code TaskSet}
	 * @exception NegativeArraySizeException
	 *                if the specified initial size is negative.
	 */
	public TaskSet(int ntasks, String str) {
		super(ntasks);
		fromHexString(str);
		fNumberOfTasks = ntasks;
	}

	/**
	 * Clear the bits at each position given by the array of indices
	 * 
	 * @param indexes
	 *            array of indices to clear
	 */
	public void clear(int[] indices) throws IndexOutOfBoundsException {
		for (int indice : indices) {
			clear(indice);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return super.clone();
	}

	/**
	 * Create a copy of the {@code TaskSet}
	 * 
	 * @return a copy of the {@code TaskSet}
	 */
	public TaskSet copy() {
		return (TaskSet) clone();
	}

	/**
	 * Set the tasks at each position given by the array of indices
	 * 
	 * @param indices
	 *            array of indices to clear
	 */
	public void set(int[] indices) throws IndexOutOfBoundsException {
		for (int indice : indices) {
			set(indice);
		}
	}

	/**
	 * Convert the {@code TaskSet} to an array containing the indices of all
	 * tasks. If there are no tasks, an empty array is returned.
	 * 
	 * @return array containing the indices of all set bits.
	 */
	public int[] toArray() {
		int[] retValue = new int[cardinality()];
		for (int i = nextSetBit(0), j = 0; i >= 0; i = nextSetBit(i + 1), j++) {
			retValue[j] = i;
		}
		return retValue;
	}

	/**
	 * Convert the {@code TaskSet} into a hex string representation. The number
	 * of characters in the string is always even.
	 * 
	 * @return hex string representation of TaskSet
	 */
	public String toHexString() {
		String res = ""; //$NON-NLS-1$

		if (isEmpty()) {
			res = "00"; //$NON-NLS-1$
		} else {
			int bytes = (taskSize() - 1) / 8 + 1;
			int bit = bytes * 8 - 1;
			for (int i = 0; i < bytes * 2; i++) {
				int nib = 0;
				for (int mask = (1 << 3); mask > 0; mask >>= 1) {
					if (get(bit--)) {
						nib |= mask;
					}
				}
				res += Integer.toHexString(nib & 0xf);
			}
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isEmpty()) {
			return "{}"; //$NON-NLS-1$
		}

		int[] bits = toArray();

		String msg = "{"; //$NON-NLS-1$
		int rangeStart = bits[0];
		msg += rangeStart;
		boolean isContinue = false;
		for (int i = 1; i < bits.length; i++) {
			if (rangeStart == (bits[i] - 1)) {
				rangeStart = bits[i];
				isContinue = true;
				if (i == (bits.length - 1)) {
					msg += "-" + bits[i]; //$NON-NLS-1$
					break;
				}
				continue;
			}
			if (isContinue) {
				msg += "-" + rangeStart; //$NON-NLS-1$
			}
			msg += "," + bits[i]; //$NON-NLS-1$
			isContinue = false;
			rangeStart = bits[i];
		}
		return msg + "}"; //$NON-NLS-1$
	}

	/**
	 * Get the fixed number of tasks represented by the {@code TaskSet}. This
	 * differs from {@link #size()} which is the number of bits of space used by
	 * the {@code TaskSet} and {@link #length()} which is the index of the
	 * highest bit plus one.
	 * 
	 * @return initial number of bits
	 */
	public int taskSize() {
		return fNumberOfTasks;
	}

	/**
	 * Convert a hex representation of a byte to it's actual value
	 * 
	 * @param b
	 *            hex representation of a byte
	 * @return byte
	 */
	private byte fromHex(byte b) {
		if (b >= 48 && b <= 57) {
			return (byte) (b - 48);
		}
		if (b >= 65 && b <= 70) {
			return (byte) (b - 65 + 10);
		}
		if (b >= 97 && b <= 102) {
			return (byte) (b - 97 + 10);
		}

		return (byte) 0;
	}

	/**
	 * Set the tasks specified in a hex representation of the {@code TaskSet}
	 * 
	 * @param str
	 *            hex representation of the {@code TaskSet}
	 */
	private void fromHexString(String str) {
		byte[] chars = str.getBytes();
		int bit = 0;
		for (int i = chars.length - 1; i >= 0; i--) {
			byte hex = fromHex(chars[i]);
			for (int mask = 1; mask <= (1 << 3); mask <<= 1) {
				if ((mask & hex) == mask) {
					set(bit);
				}
				bit++;
			}
		}
	}
}
