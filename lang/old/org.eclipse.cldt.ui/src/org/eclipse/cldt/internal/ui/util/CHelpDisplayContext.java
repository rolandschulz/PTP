/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.internal.ui.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cldt.core.model.ITranslationUnit;
import org.eclipse.cldt.internal.ui.FortranHelpProviderManager;
import org.eclipse.cldt.internal.ui.editor.FortranEditor;
import org.eclipse.cldt.internal.ui.text.FortranWordFinder;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.cldt.ui.ICHelpResourceDescriptor;
import org.eclipse.cldt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 * @since 2.1
 */
public class CHelpDisplayContext implements IContext {
	private IHelpResource[] fHelpResources;
	private String fText;
	
	public static void displayHelp(String contextId, FortranEditor editor) throws CoreException {
		String selected = getSelectedString(editor);
		IContext context= HelpSystem.getContext(contextId);
		if (context != null) {
			if (selected != null && selected.length() > 0) {
				context= new CHelpDisplayContext(context, editor, selected);
			}
			WorkbenchHelp.displayHelp(context);
		}
	}
	
	private static String getSelectedString(FortranEditor editor){
		String expression = null;
		try{
			ITextSelection selection = (ITextSelection)editor.getSite().getSelectionProvider().getSelection();
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			IRegion region = FortranWordFinder.findWord(document, selection.getOffset());
			expression = document.get(region.getOffset(), region.getLength());
		}
		catch(Exception e){
		}
		return expression;
	}

	public CHelpDisplayContext(IContext context, final FortranEditor editor , String selected) throws CoreException {

		List helpResources= new ArrayList();
		
		ICHelpInvocationContext invocationContext = new ICHelpInvocationContext() {

			public IProject getProject() {
				ITranslationUnit unit = getTranslationUnit();
				if (unit != null) {
					return unit.getCProject().getProject();
				}
				return null;
			}

			public ITranslationUnit getTranslationUnit() {
				IEditorInput editorInput= editor.getEditorInput();
				return FortranUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
			}	
		};

		if (context != null) {
			IHelpResource[] resources= context.getRelatedTopics();
			if (resources != null){
				helpResources.addAll(Arrays.asList(resources));
			}
		}

		ICHelpResourceDescriptor providerResources[] = FortranHelpProviderManager.getDefault().getHelpResources(invocationContext,selected);
		if(providerResources != null){
			for(int i = 0; i < providerResources.length; i++){
				helpResources.addAll(Arrays.asList(providerResources[i].getHelpResources()));
			}
		}

		fHelpResources= (IHelpResource[]) helpResources.toArray(new IHelpResource[helpResources.size()]);
		if (fText == null || fText.length() == 0) {
			if (context != null) {
				fText= context.getText();
			}
		}
		if (fText != null && fText.length() == 0) {
			fText= null; 
		}
	}

	private boolean doesNotExist(URL url) {
		if (url.getProtocol().equals("file")) { //$NON-NLS-1$
			File file= new File(url.getFile());
			return !file.exists();
		}
		return false;
	}

	public IHelpResource[] getRelatedTopics() {
		return fHelpResources;
	}

	public String getText() {
		return fText;
	}
}

