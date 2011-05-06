/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 


package org.eclipse.ptp.internal.rdt.core.navigation;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public class LocalNavigationService implements INavigationService {

	public OpenDeclarationResult openDeclaration(Scope scope, ITranslationUnit unit, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor) {
		return null;
	}
}
