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

package org.eclipse.fdt.internal.ui.search.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.fdt.core.CommonLanguageCore;
import org.eclipse.fdt.core.ICLogConstants;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.parser.IParser;
import org.eclipse.fdt.core.parser.ParseError;
import org.eclipse.fdt.core.parser.ast.ASTUtil;
import org.eclipse.fdt.core.parser.ast.IASTFunction;
import org.eclipse.fdt.core.parser.ast.IASTNode;
import org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.fdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.fdt.core.search.ICSearchScope;
import org.eclipse.fdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.fdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.fdt.internal.ui.editor.FortranEditor;
import org.eclipse.fdt.internal.ui.search.CSearchQuery;
import org.eclipse.fdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.fdt.internal.ui.search.CSearchUtil;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;


public abstract class FindAction extends SelectionParseAction {
	public FindAction(FortranEditor editor){
		super( editor );
	}
	
	public FindAction(IWorkbenchSite site){
		super( site );
	}
	
	 /**
	 * @param node
	 */
	 protected CSearchQuery createSearchQuery(String pattern, SearchFor searchFor) {
	 	CSearchQuery op = null;
		ICSearchScope scope = getScope();
		
		if (scope == null)
			return null;
		
		String scopeDescription = getScopeDescription();
	
		//Create a case sensitive search operation - limited by the node
		List search = new LinkedList();
		search.add(searchFor);
		
		CSearchResultCollector collector= new CSearchResultCollector();
		
		LimitTo limitTo = getLimitTo();
		
		op = new CSearchQuery(CommonLanguageCore.getWorkspace(), pattern,true,search,limitTo,scope,scopeDescription,collector);
		return op;
		
	}

	 protected CSearchQuery createSearchQuery( IASTOffsetableNamedElement node ){
	 	String pattern = null;
	 	
	 	if( node instanceof IASTQualifiedNameElement ){
	 		String [] qualNames = ((IASTQualifiedNameElement)node).getFullyQualifiedName();
	 		pattern = "::" + qualNames[0]; //$NON-NLS-1$
	 		for( int i = 1; i < qualNames.length; i++ ){
				 pattern += "::"; //$NON-NLS-1$
				 pattern += qualNames[i];
	 		}
	 	} else {
	 		pattern = node.getName();
	 	}
	 	
	 	if( node instanceof IASTFunction ){
	 		pattern += '(';
	 		String[] parameterTypes = ASTUtil.getFunctionParameterTypes((IASTFunction) node);
	 		for( int i = 0; i < parameterTypes.length; i++ ){
	 			if( i != 0 )
	 				pattern += ", "; //$NON-NLS-1$
	 			pattern += parameterTypes[i];
	 		}
	 		pattern += ')';
	 	}
	 	
	 	return createSearchQuery( pattern, CSearchUtil.getSearchForFromNode(node) );
	 }
	 
	public void run() {
		ISelection sel = getSelection();
		 
	 	if (sel instanceof IStructuredSelection) {
			run((IStructuredSelection) sel);
		} else if (sel instanceof ITextSelection) {
			run((ITextSelection) sel);
		} 
	}
	
	public void run(IStructuredSelection sel){
		Object obj = sel.getFirstElement();
	 	if( obj == null || !(obj instanceof ICElement ) ){
	 		operationNotAvailable();
	 		return;
	 	} else {
	 		clearStatusLine();
	 	}
		ICElement element = (ICElement) obj;
		
		CSearchQuery job = createSearchQuery( element.getElementName(), CSearchUtil.getSearchForFromElement(element));
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQuery(job);
	}
	
	public void run(ITextSelection sel){
		 if(sel == null) {
	 		 return;
		 }
		 
		 SelSearchNode selNode = getSelection( sel );
		 
		 int selectionStart = selNode.selStart;
		 int selectionEnd = selNode.selEnd;
		 
		IFile resourceFile = fEditor.getInputFile();
		IParser parser = setupParser(resourceFile);
		IASTOffsetableNamedElement node = null;
		IParser.ISelectionParseResult result = null;
		
		try{
			result = parser.parse(selectionStart,selectionEnd);
			if( result != null )
				node = result.getOffsetableNamedElement();
		} 
		catch (ParseError er){}
		catch (Exception ex){}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.fdt.internal.core.model.Util.log(null, "Selection Search Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		if (node == null || !( node instanceof IASTNode )){
			operationNotAvailable();
			return;
		} else {
			clearStatusLine();
		}
	
		CSearchQuery job = createSearchQuery(node);
		
		if (job == null)
			return; 
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQuery(job);
	}
	
	abstract protected String getScopeDescription(); 

	abstract protected ICSearchScope getScope();
	
	abstract protected LimitTo getLimitTo();
}
