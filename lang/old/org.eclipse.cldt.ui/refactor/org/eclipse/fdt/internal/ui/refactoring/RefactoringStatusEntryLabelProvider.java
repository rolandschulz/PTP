/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.ui.refactoring;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.fdt.internal.corext.refactoring.base.RefactoringStatusEntry;
import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.internal.ui.util.Strings;

public class RefactoringStatusEntryLabelProvider extends LabelProvider{
		public String getText(Object element){
			return Strings.removeNewLine(((RefactoringStatusEntry)element).getMessage());
		}
		public Image getImage(Object element){
			RefactoringStatusEntry entry= (RefactoringStatusEntry)element;
			if (entry.isFatalError())
				return FortranPluginImages.get(FortranPluginImages.IMG_OBJS_REFACTORING_FATAL);
			else if (entry.isError())
				return FortranPluginImages.get(FortranPluginImages.IMG_OBJS_REFACTORING_ERROR);
			else if (entry.isWarning())	
				return FortranPluginImages.get(FortranPluginImages.IMG_OBJS_REFACTORING_WARNING);
			else 
				return FortranPluginImages.get(FortranPluginImages.IMG_OBJS_REFACTORING_INFO);
		}
}
