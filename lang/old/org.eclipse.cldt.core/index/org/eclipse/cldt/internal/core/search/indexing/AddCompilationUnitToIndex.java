/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cldt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.cldt.internal.core.index.IIndex;
import org.eclipse.cldt.internal.core.index.impl.IFileDocument;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class AddCompilationUnitToIndex extends AddFileToIndex {
	char[] contents;

	public AddCompilationUnitToIndex(IFile resource, IPath indexedContainer, IndexManager manager, boolean checkEncounteredHeaders) {
		super(resource, indexedContainer, manager, checkEncounteredHeaders);
	}
	protected boolean indexDocument(IIndex index) throws IOException {
		if (!initializeContents()) return false;
		index.add(new IFileDocument(resource, this.contents), new SourceIndexer(resource, manager.getTimeout()));
		return true;
	}
	public boolean initializeContents() {
		if (this.contents == null) {
			try {
				IPath location = resource.getLocation();
				if (location != null)
					this.contents = org.eclipse.cldt.internal.core.Util.getFileCharContent(location.toFile(), null);
			} catch (IOException e) {
			}
		}
		return this.contents != null;
	}
}
