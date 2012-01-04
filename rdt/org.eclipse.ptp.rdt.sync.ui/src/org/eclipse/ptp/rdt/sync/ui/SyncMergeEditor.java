/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IProgressMonitor;

public class SyncMergeEditor {
	public void open() {
		CompareUI.openCompareEditor(new FileCompareInput());
	}
	
	private class FileCompareInput extends CompareEditorInput {
		public FileCompareInput() {
			super(new CompareConfiguration());
		}

		protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
