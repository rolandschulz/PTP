/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.log;

import org.eclipse.core.resources.IFile;

/*
 * Essentially a struct that holds data from an emacs pattern, and where that pattern was found
 */
public class TwoThreadInfo {
	private final ThreadInfo threadInfo0, threadInfo1;
	private final IFile logFile;
	private final int line;

	public TwoThreadInfo(ThreadInfo threadInfo0, ThreadInfo threadInfo1, IFile logFile, int line) {
		this.threadInfo0 = threadInfo0;
		this.threadInfo1 = threadInfo1;
		this.logFile = logFile;
		this.line = line;
	}

	public IFile getFile() {
		return this.logFile;
	}

	public int getLine() {
		return this.line;
	}

	public ThreadInfo getThreadInfo0() {
		return threadInfo0;
	}

	public ThreadInfo getThreadInfo1() {
		return threadInfo1;
	}

}
