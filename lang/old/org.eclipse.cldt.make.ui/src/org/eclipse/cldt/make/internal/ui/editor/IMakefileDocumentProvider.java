/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
/*
 */
package org.eclipse.cldt.make.internal.ui.editor;

import org.eclipse.cldt.make.core.makefile.IMakefile;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 */
public interface IMakefileDocumentProvider extends IDocumentProvider {

	/**
	 * Shuts down this provider.
	 */
	void shutdown();

	/**
	 * Returns the working copy for the given element.
	 *
	 * @param element the element
	 * @return the working copy for the given element
	 */
	IMakefile getWorkingCopy(Object element);

}