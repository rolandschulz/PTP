/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cldt.internal.ui.search.actions;

import java.io.IOException;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.model.CoreModel;
import org.eclipse.cldt.core.model.IWorkingCopy;
import org.eclipse.cldt.core.parser.CodeReader;
import org.eclipse.cldt.core.parser.IParser;
import org.eclipse.cldt.core.parser.IScannerInfo;
import org.eclipse.cldt.core.parser.IScannerInfoProvider;
import org.eclipse.cldt.core.parser.NullSourceElementRequestor;
import org.eclipse.cldt.core.parser.ParserFactory;
import org.eclipse.cldt.core.parser.ParserFactoryError;
import org.eclipse.cldt.core.parser.ParserLanguage;
import org.eclipse.cldt.core.parser.ParserMode;
import org.eclipse.cldt.core.parser.ParserUtil;
import org.eclipse.cldt.core.parser.ScannerInfo;
import org.eclipse.cldt.internal.ui.editor.FortranEditor;
import org.eclipse.cldt.internal.ui.search.CSearchMessages;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author aniefer
 * Created on Jun 2, 2004
 */
public class SelectionParseAction extends Action {

	protected IWorkbenchSite fSite;
	protected FortranEditor fEditor;

	public SelectionParseAction() {
		super();
	}
	
	public SelectionParseAction( FortranEditor editor ) {
		super();
		fEditor=editor;
		fSite=editor.getSite();
	}
	
	public SelectionParseAction(IWorkbenchSite site){
		super();
		fSite=site;
	}

	protected IParser setupParser(IFile resourceFile) {
		

		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = FortranCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(resourceFile);
		  if (buildScanInfo != null){
			scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		  }
		}
		
		//C or CPP?
		ParserLanguage language = ParserLanguage.C;
		
		IWorkingCopy workingCopy = null;
		if( fEditor.isDirty() ){
			IWorkingCopy [] workingCopies = FortranUIPlugin.getSharedWorkingCopies();
			if( workingCopies != null ){
				for( int i = 0; i < workingCopies.length; i++ ){
					if( workingCopies[i].getUnderlyingResource().equals( resourceFile ) ){
						workingCopy = workingCopies[i];
						break;
					}
				}
			}
		}
		
		IParser parser = null;
		CodeReader reader = null;
		try {
			if( workingCopy == null )
				reader = new CodeReader(resourceFile.getLocation().toOSString(), resourceFile.getCharset() );
			else 
				reader = new CodeReader(resourceFile.getLocation().toOSString(), workingCopy.getContents());
		} catch (IOException e) {
			e.printStackTrace();
		} catch ( CoreException e ) {
            e.printStackTrace();
        }
		
		try
		{
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner( reader, scanInfo, ParserMode.SELECTION_PARSE, language, new NullSourceElementRequestor(), ParserUtil.getScannerLogService(), null ), 
							new NullSourceElementRequestor(), ParserMode.SELECTION_PARSE, language, ParserUtil.getParserLogService() );
			
		} catch( ParserFactoryError pfe ){}
		
	   return parser;
	 }

	protected void operationNotAvailable() {
		IStatusLineManager statusManager = null;
		 if (fSite instanceof IViewSite){
		 	statusManager = ((IViewSite) fSite).getActionBars().getStatusLineManager();
		 }
		 else if (fSite instanceof IEditorSite){
		 	statusManager = ((IEditorSite) fSite).getActionBars().getStatusLineManager();
		 }	
		 if( statusManager != null )
		 	statusManager.setErrorMessage(CSearchMessages.getString("CSearchOperation.operationUnavailable.message"));//$NON-NLS-1$
	}
	protected void clearStatusLine() {
		IStatusLineManager statusManager = null;
		 if (fSite instanceof IViewSite){
		 	statusManager = ((IViewSite) fSite).getActionBars().getStatusLineManager();
		 }
		 else if (fSite instanceof IEditorSite){
		 	statusManager = ((IEditorSite) fSite).getActionBars().getStatusLineManager();
		 }	
		 if( statusManager != null )
		 	statusManager.setErrorMessage( "" ); //$NON-NLS-1$
	}

	//TODO: Change this to work with qualified identifiers
	public SelSearchNode getSelection( int fPos ) {
		IDocumentProvider prov = ( fEditor != null ) ? fEditor.getDocumentProvider() : null;
 		IDocument doc = ( prov != null ) ? prov.getDocument(fEditor.getEditorInput()) : null;
 		
 		if( doc == null )
 			return null;
 		 
		int pos= fPos;
		char c;
		int fStartPos =0, fEndPos=0;
		String selectedWord=null;
		
		try{
			while (pos >= 0) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			fStartPos= pos + 1;
			
			pos= fPos;
			int length= doc.getLength();
			while (pos < length) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			fEndPos= pos;
			selectedWord = doc.get(fStartPos, (fEndPos - fStartPos));
		}
		catch(BadLocationException e){
		}
		
		SelSearchNode sel = new SelSearchNode();
		sel.selText = selectedWord;
		sel.selStart = fStartPos;
		sel.selEnd = fEndPos;
	
		return sel;		
	}
		
	/**
	  * Return the selected string from the editor
	  * @return The string currently selected, or null if there is no valid selection
	  */
	protected SelSearchNode getSelection( ITextSelection textSelection ) {
		if( textSelection == null )
			return null;
		
		 String seltext = textSelection.getText();
		 SelSearchNode sel = null;
		 if ( seltext == null || seltext.length() == 0 ) {
	 		 int selStart =  textSelection.getOffset();
	 		 sel = getSelection(selStart);
		 } else {
		 	sel = new SelSearchNode();
		 	sel.selText= seltext;
		 	sel.selStart = textSelection.getOffset();
		 	sel.selEnd = textSelection.getOffset() + textSelection.getLength();
		 }
		 return sel;
	}
	
	protected ISelection getSelection() {
		ISelection sel = null;
		if (fSite != null && fSite.getSelectionProvider() != null ){
			sel = fSite.getSelectionProvider().getSelection();
		}
		
		return sel;
	}
	
	class SelSearchNode{
	 	protected String selText;
	 	protected int selStart;
	 	protected int selEnd;
	}

}
