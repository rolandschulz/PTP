/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cldt.internal.ui.editor;

import org.eclipse.cldt.core.model.ITranslationUnit;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * ITranslationUnitEditorInput
 */
public interface ITranslationUnitEditorInput extends IStorageEditorInput, ILocationProvider {

	ITranslationUnit getTranslationUnit();

}