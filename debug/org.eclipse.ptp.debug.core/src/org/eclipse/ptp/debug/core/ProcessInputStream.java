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

import java.io.InputStream;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;

/**
 * @author Clement chu
 */
public class ProcessInputStream extends InputStream implements IJobChildListener {
	protected List<String> buffers;
	protected int pos;
	protected int count;
	private String currentBuffer;
	private final IPJob job;
	private final int processJobRank;

	/**
	 * @since 4.0
	 */
	public ProcessInputStream(IPJob job, int processJobRank) {
		this.job = job;
		this.processJobRank = processJobRank;
		buffers = Collections.synchronizedList(new LinkedList<String>());
		job.addChildListener(this);
	}

	public void addInput(String buffer) {
		synchronized (buffers) {
			buffers.add(buffer == null ? "" : buffer); //$NON-NLS-1$
			buffers.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() {
		addInput(""); //$NON-NLS-1$
		job.removeChildListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IProcessListener#handleEvent(
	 * org.eclipse.ptp.core.elements.events.IProcessChangeEvent)
	 */
	/**
	 * @since 4.0
	 */
	public void handleEvent(IChangedProcessEvent e) {
		final BitSet processChangedSet = e.getProcesses();
		// check to see if this event is concerned with
		// the process that we care about
		if (!processChangedSet.get(processJobRank)) {
			return;
		}
		if (job.getProcessState(processJobRank) == ProcessAttributes.State.COMPLETED) {
			close();
		}

		StringAttribute stdoutAttr = e.getAttributes().getAttribute(ProcessAttributes.getStdoutAttributeDefinition());
		if (stdoutAttr != null) {
			addInput(stdoutAttr.getValue() + "\n"); //$NON-NLS-1$
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.INewProcessEvent)
	 */
	/**
	 * @since 4.0
	 */
	public void handleEvent(INewProcessEvent e) {
		// not interested
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
	 */
	/**
	 * @since 4.0
	 */
	public void handleEvent(IRemoveProcessEvent e) {
		// not interested
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() {
		synchronized (buffers) {
			if (count <= pos) {
				currentBuffer = getBuffer();
			}

			return (pos < count) ? (currentBuffer.charAt(pos++) & 0xFF) : -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte b[], int off, int len) {
		synchronized (buffers) {
			String buffer = getBuffer();
			if (b == null) {
				throw new NullPointerException();
			} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			}
			if (pos >= count) {
				return -1;
			}
			if (pos + len > count) {
				len = count - pos;
			}
			if (len <= 0) {
				return 0;
			}
			int cnt = len;
			while (--cnt >= 0) {
				b[off++] = (byte) buffer.charAt(pos++);
			}
			return len;
		}
	}

	public void reset(int len) {
		synchronized (buffers) {
			pos = 0;
			count = len;
		}
	}

	public void restart() {
		job.addChildListener(this);
	}

	private String getBuffer() {
		synchronized (buffers) {
			String buffer;
			while (buffers.isEmpty()) {
				try {
					buffers.wait();
				} catch (InterruptedException e) {
					buffer = ""; //$NON-NLS-1$
				}
			}
			buffer = buffers.remove(0);
			reset(buffer.length());
			return buffer;
		}
	}
}
