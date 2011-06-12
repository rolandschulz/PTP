/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.navigation;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public interface INavigationService {

	OpenDeclarationResult openDeclaration(Scope scope, ITranslationUnit workingCopy, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor);
	
}
