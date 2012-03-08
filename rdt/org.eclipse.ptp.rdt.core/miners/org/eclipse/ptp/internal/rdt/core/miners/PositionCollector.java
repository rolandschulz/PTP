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

package org.eclipse.ptp.internal.rdt.core.miners;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;

import org.eclipse.ptp.internal.rdt.core.miners.SemanticHighlighting;
import org.eclipse.ptp.internal.rdt.core.miners.SemanticHighlightings;


/* Pulled from SemanticHighlightingReconciler with SemanticToken added as a nested class.*/
public class PositionCollector extends CPPASTVisitor {

	public final class SemanticToken {

		/** AST node */
		private IASTNode fNode;

		/** Binding */
		private IBinding fBinding;
		/** Is the binding resolved? */
		private boolean fIsBindingResolved= false;

		/** AST root */
		private IASTTranslationUnit fRoot;
		private boolean fIsRootResolved= false;

		/**
		 * @return Returns the binding, can be <code>null</code>.
		 */
		public IBinding getBinding() {
			if (!fIsBindingResolved) {
				fIsBindingResolved= true;
				if (fNode instanceof IASTName)
					fBinding= ((IASTName)fNode).resolveBinding();
			}
			
			return fBinding;
		}

		/**
		 * @return the AST node
		 */
		public IASTNode getNode() {
			return fNode;
		}
		
		/**
		 * @return the AST root
		 */
		public IASTTranslationUnit getRoot() {
			if (!fIsRootResolved) {
				fIsRootResolved= true;
				if (fNode != null) {
					fRoot= fNode.getTranslationUnit();
				}
			}
			return fRoot;
		}

		/**
		 * Update this token with the given AST node.
		 * <p>
		 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
		 * </p>
		 *
		 * @param node the AST node
		 */
		public void update(IASTNode node) {
			clear();
			fNode= node;
		}

		/**
		 * Clears this token.
		 * <p>
		 * NOTE: Allowed to be used by {@link SemanticHighlightingReconciler} only.
		 * </p>
		 */
		public void clear() {
			fNode= null;
			fBinding= null;
			fIsBindingResolved= false;
			fRoot= null;
			fIsRootResolved= false;
		}
	}
	
	protected SemanticHighlighting[] fJobSemanticHighlightings;
	
	ArrayList<ArrayList<Integer>> positionList = new ArrayList<ArrayList<Integer>>();
	
	/** The semantic token */
	private SemanticToken fToken= new SemanticToken();
	private int fMinLocation;
	
	public PositionCollector(boolean visitImplicitNames) {
		
		fJobSemanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
		includeInactiveNodes = true;
		fMinLocation= -1;
		shouldVisitTranslationUnit= true;
		shouldVisitNames= true;
		shouldVisitDeclarations= true;
		shouldVisitExpressions= true;
		shouldVisitStatements= true;
		shouldVisitDeclarators= true;
		shouldVisitNamespaces= true;
		shouldVisitImplicitNames = visitImplicitNames;
		shouldVisitImplicitNameAlternates = visitImplicitNames;
	}
	
	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#leave(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	@Override
	public int leave(IASTDeclaration declaration) {
		return PROCESS_CONTINUE;
	}
	
	
	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
	 */
	@Override
	public int visit(IASTTranslationUnit tu) {
		
		// visit macro definitions
		IASTPreprocessorMacroDefinition[] macroDefs= tu.getMacroDefinitions();
		for (IASTPreprocessorMacroDefinition macroDef : macroDefs) {
			if (macroDef.isPartOfTranslationUnitFile()) {
				visitNode(macroDef.getName());
			}
		}
		fMinLocation= -1;

		// visit macro expansions
		IASTPreprocessorMacroExpansion[] macroExps= tu.getMacroExpansions();
		for (IASTPreprocessorMacroExpansion macroExp : macroExps) {
			if (macroExp.isPartOfTranslationUnitFile()) {
				IASTName macroRef= macroExp.getMacroReference();
				visitNode(macroRef);
				IASTName[] nestedMacroRefs= macroExp.getNestedMacroReferences();
				for (IASTName nestedMacroRef : nestedMacroRefs) {
					visitNode(nestedMacroRef);
				}
			}
		}
		fMinLocation= -1;

		// visit ordinary code
		return super.visit(tu);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	@Override
	public int visit(IASTDeclaration declaration) {
		if (!declaration.isPartOfTranslationUnitFile()) {
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}
	
	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		if (!namespace.isPartOfTranslationUnitFile()) {
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	@Override
	public int visit(IASTDeclarator declarator) {
		return PROCESS_CONTINUE;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	@Override
	public int visit(IASTStatement statement) {
		return PROCESS_CONTINUE;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	@Override
	public int visit(IASTName name) {
		if (visitNode(name)) {
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}
	
	private boolean visitNode(IASTNode node) {
		boolean consumed= false;
		fToken.update(node);
		for (int i= 0, n= fJobSemanticHighlightings.length; i < n; ++i) {
			SemanticHighlighting semanticHighlighting= fJobSemanticHighlightings[i];
			if (semanticHighlighting.consumes(fToken)) {
				if (node instanceof IASTName) {
					addNameLocation((IASTName)node, i);
				} else {
					addNodeLocation(node.getFileLocation(), i);
				}
				consumed= true;
				break;
			}
		}
		fToken.clear();
		return consumed;
	}
	
	/**
	 * Add the a location range for the given name.
	 * 
	 * @param name  The name
	 * @param highlighting The highlighting
	 */
	private void addNameLocation(IASTName name,  int highlightingStyleIndex) {
		IASTImageLocation imageLocation= name.getImageLocation();
		if (imageLocation != null) {
			if (imageLocation.getLocationKind() != IASTImageLocation.MACRO_DEFINITION) {
				int offset= imageLocation.getNodeOffset();
				if (offset >= fMinLocation) {
					int length= imageLocation.getNodeLength();
					if (offset > -1 && length > 0) {
						fMinLocation= offset + length;
						addPosition(offset, length, highlightingStyleIndex);
					}
				}
			}
		} else {
			// fallback in case no image location available
			IASTNodeLocation[] nodeLocations= name.getNodeLocations();
			if (nodeLocations.length == 1 && !(nodeLocations[0] instanceof IASTMacroExpansionLocation)) {
				addNodeLocation(nodeLocations[0], highlightingStyleIndex);
			}
		}
	}

	/**
	 * Add the a location range for the given highlighting.
	 * 
	 * @param nodeLocation  The node location
	 * @param highlighting The highlighting
	 */
	private void addNodeLocation(IASTNodeLocation nodeLocation, int highlightingStyleIndex) {
		if (nodeLocation == null) {
			return;
		}
		int offset= nodeLocation.getNodeOffset();
		if (offset >= fMinLocation) {
			int length= nodeLocation.getNodeLength();
			if (offset > -1 && length > 0) {
				fMinLocation= offset + length;
				addPosition(offset, length, highlightingStyleIndex);
			}
		}
	}

	/**
	 * Add a position with the given range.
	 * 
	 * @param offset The range offset
	 * @param length The range length
	 * @param highlighting The highlighting
	 */
	private void addPosition(int offset, int length, int highlightingStyleIndex) {
		ArrayList<Integer> u = new ArrayList<Integer>();
		u.addAll(Arrays.asList(new Integer[]{offset,length,highlightingStyleIndex}));
		positionList.add(u);
	}
		
		
	public ArrayList<ArrayList<Integer>> getPositions(){
		return positionList;
	}
	
}