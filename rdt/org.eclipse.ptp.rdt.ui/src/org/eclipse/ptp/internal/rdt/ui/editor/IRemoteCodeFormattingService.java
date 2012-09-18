/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteDefaultCodeFormatterOptions;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Vivian Kong
 *
 */
public interface IRemoteCodeFormattingService {
	/**
	 * Format <code>source</code>,
	 * and returns a text edit that correspond to the difference between the given string and the formatted string.
	 * It returns null if the given string cannot be formatted.
	 * 
	 * @param tu 
	 * @param source 
	 * @param length 
	 * @param offset 
	 * @param preferences 
	 * @return the text edit
	 * @throws CoreException 
	 */
	TextEdit computeCodeFormatting(ITranslationUnit tu, String source, RemoteDefaultCodeFormatterOptions preferences, int offset, int length, IProgressMonitor monitor) throws CoreException;
}
