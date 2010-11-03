/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.search;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class RemoteSearchTextSelectionQuery extends RemoteSearchQuery {
	private static final long serialVersionUID = 1L;

	private ITranslationUnit tu;
	private String selection;
	private int offset;
	private int length;
	
	public RemoteSearchTextSelectionQuery(ICElement[] scope, ITranslationUnit tu, String selection, int offset, int length, int flags) {
		super(scope, flags | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		this.tu = tu;
		this.selection = selection;
		this.offset = offset;
		this.length = length;
	}

	public void runWithIndex(final IIndex parseIndex,  final IIndex searchScopeindex, IIndexLocationConverter converter, IProgressMonitor monitor) throws CoreException, InterruptedException {
		fConverter = converter;
	
		IBinding binding = null;
		parseIndex.acquireReadLock();
		try{
			IASTTranslationUnit ast = tu.getAST(parseIndex, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);

			if (ast != null) {
				IASTName searchName= ast.getNodeSelector(null).findEnclosingName(offset, length);
				if (searchName != null) {
					selection= searchName.toString();
					binding= searchName.resolveBinding();
					if (binding instanceof IProblemBinding == false) {
						if (binding != null) {
							IScope scope= null;
							try {
								scope = binding.getScope();
							} catch (DOMException e) {
							}
							if (scope instanceof ICPPBlockScope || scope instanceof ICFunctionScope) {
								createLocalMatches(ast, binding);
								
							}
						}
						binding = parseIndex.findBinding(searchName);
						
					}
				}
			}
		}finally{
			parseIndex.releaseReadLock();
		}
		if (binding != null) {
			searchScopeindex.acquireReadLock();
			try{
				createMatches(searchScopeindex, binding);
			}finally{
				searchScopeindex.releaseReadLock();
			}
			
			
		}
			
	}

	public String getSelection() {
		return selection;
	}
}
