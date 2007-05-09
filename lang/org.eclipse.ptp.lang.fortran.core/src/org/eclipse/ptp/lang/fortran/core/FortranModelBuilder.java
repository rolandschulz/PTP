/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.lang.fortran.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTDeclarationAmbiguity;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.model.ASTHolderTUInfo;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
//import org.eclipse.cdt.internal.core.model.CElementInfo;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.core.model.Using;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Build TranslationUnit structure from an <code>IASTTranslationUnit</code>.
 *
 * @since 4.0
 */
public class FortranModelBuilder implements IContributedModelBuilder {

//	private static final boolean PRINT_PROBLEMS= false;
//
//	private static class ProblemPrinter implements IProblemRequestor {
//		public void acceptProblem(IProblem problem) {
//			System.err.println("PROBLEM: " + problem.getMessage()); //$NON-NLS-1$
//		}
//		public void beginReporting() {
//		}
//		public void endReporting() {
//		}
//		public boolean isActive() {
//			return true;
//		}
//	}
//
//	/**
//	 * Adapts {@link IASTProblem} to {@link IProblem).
//	 */
//	private static class ProblemAdapter implements IProblem {
//
//		private IASTProblem fASTProblem;
//
//		/**
//		 * @param problem
//		 */
//		public ProblemAdapter(IASTProblem problem) {
//			fASTProblem= problem;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#checkCategory(int)
//		 */
//		public boolean checkCategory(int bitmask) {
//			return fASTProblem.checkCategory(bitmask);
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getArguments()
//		 */
//		public String getArguments() {
//			return fASTProblem.getArguments();
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getID()
//		 */
//		public int getID() {
//			return fASTProblem.getID();
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getMessage()
//		 */
//		public String getMessage() {
//			return fASTProblem.getMessage();
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getOriginatingFileName()
//		 */
//		public char[] getOriginatingFileName() {
//			return fASTProblem.getContainingFilename().toCharArray();
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getSourceEnd()
//		 */
//		public int getSourceEnd() {
//			IASTFileLocation location= fASTProblem.getFileLocation();
//			if (location != null) {
//				return location.getNodeOffset() + location.getNodeLength() - 1;
//			}
//			return -1;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getSourceLineNumber()
//		 */
//		public int getSourceLineNumber() {
//			IASTFileLocation location= fASTProblem.getFileLocation();
//			if (location != null) {
//				return location.getStartingLineNumber();
//			}
//			return -1;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#getSourceStart()
//		 */
//		public int getSourceStart() {
//			IASTFileLocation location= fASTProblem.getFileLocation();
//			if (location != null) {
//				return location.getNodeOffset();
//			}
//			return -1;
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#isError()
//		 */
//		public boolean isError() {
//			return fASTProblem.isError();
//		}
//
//		/*
//		 * @see org.eclipse.cdt.core.parser.IProblem#isWarning()
//		 */
//		public boolean isWarning() {
//			return fASTProblem.isWarning();
//		}
//
//	}
//
	private final TranslationUnit fTranslationUnit;
	private String fTranslationUnitFileName;
//	private ASTAccessVisibility fCurrentVisibility;
//	private Stack fVisibilityStack;
	private IProgressMonitor fProgressMonitor;

	/**
	 * Create a model builder for the given translation unit.
	 * 
	 * @param tu  the translation unit (must be a {@link TranslationUnit}
	 * @param monitor the progress monitor
	 */
	public FortranModelBuilder(ITranslationUnit tu, IProgressMonitor monitor) {
		fTranslationUnit= (TranslationUnit) tu;
		fProgressMonitor= monitor;
	}

	
	/*
	 * @see org.eclipse.cdt.core.model.IContributedModelBuilder#parse(boolean)
	 */
	public void parse(boolean quickParseMode) throws Exception {
		final IIndexManager indexManager= CCorePlugin.getIndexManager();
		IIndex index= indexManager.getIndex(fTranslationUnit.getCProject(), IIndexManager.ADD_DEPENDENCIES);
		
		try {
			if (index != null) {
				try {
					index.acquireReadLock();
				} catch (InterruptedException ie) {
					index= null;
				}
			}
			checkCanceled();
			long startTime= System.currentTimeMillis();
//			final CElementInfo elementInfo= fTranslationUnit.getElementInfo();
			int parseFlags= quickParseMode ? ITranslationUnit.AST_SKIP_ALL_HEADERS : ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
//			if (!(elementInfo instanceof ASTHolderTUInfo)) {
//				parseFlags |= ITranslationUnit.AST_SKIP_FUNCTION_BODIES;
//			}
			final IASTTranslationUnit ast= fTranslationUnit.getAST(index, parseFlags);
//			Util.debugLog("CModelBuilder2: parsing " //$NON-NLS-1$
//					+ fTranslationUnit.getElementName()
//					+ " mode="+ (quickParseMode ? "skip all " : "skip indexed ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//					+ " time="+ ( System.currentTimeMillis() - startTime ) + "ms", //$NON-NLS-1$ //$NON-NLS-2$
//					IDebugLogConstants.MODEL, false);

//			if (elementInfo instanceof ASTHolderTUInfo) {
//				((ASTHolderTUInfo)elementInfo).fAST= ast;
//			}

			checkCanceled();
			if (ast == null) {
				return;
			}
			startTime= System.currentTimeMillis();
//			buildModel(ast);
//			elementInfo.setIsStructureKnown(true);
//			Util.debugLog("CModelBuilder2: building " //$NON-NLS-1$
//					+"children="+ fTranslationUnit.getElementInfo().internalGetChildren().size() //$NON-NLS-1$
//					+" time="+ (System.currentTimeMillis() - startTime) + "ms", //$NON-NLS-1$ //$NON-NLS-2$
//					IDebugLogConstants.MODEL, false);
		} finally {
			if (index != null) {
				index.releaseReadLock();
			}
		}
	}

	private boolean isCanceled() {
		return fProgressMonitor != null && fProgressMonitor.isCanceled();
	}

	private void checkCanceled() {
		if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
//			Util.debugLog("CModelBuilder2: cancelled ", IDebugLogConstants.MODEL, false); //$NON-NLS-1$
			throw new OperationCanceledException();
		}
	}

//	/**
//	 * Build the model from the given AST.
//	 * @param ast
//	 * @throws CModelException
//	 * @throws DOMException
//	 */
//	private void buildModel(IASTTranslationUnit ast) throws CModelException, DOMException {
//		fTranslationUnitFileName= ast.getFilePath();
//		fVisibilityStack= new Stack();
//
//		// includes
//		final IASTPreprocessorIncludeStatement[] includeDirectives= ast.getIncludeDirectives();
//		for (int i= 0; i < includeDirectives.length; i++) {
//			IASTPreprocessorIncludeStatement includeDirective= includeDirectives[i];
//			if (isLocalToFile(includeDirective)) {
//				createInclusion(fTranslationUnit, includeDirective);
//			}
//		}
//		// macros
//		final IASTPreprocessorMacroDefinition[] macroDefinitions= ast.getMacroDefinitions();
//		for (int i= 0; i < macroDefinitions.length; i++) {
//			IASTPreprocessorMacroDefinition macroDefinition= macroDefinitions[i];
//			if (isLocalToFile(macroDefinition)) {
//				createMacro(fTranslationUnit, macroDefinition);
//			}
//		}
//		// declarations
//		final IASTDeclaration[] declarations= ast.getDeclarations();
//		for (int i= 0; i < declarations.length; i++) {
//			IASTDeclaration declaration= declarations[i];
//			if (isLocalToFile(declaration)) {
//				createDeclaration(fTranslationUnit, declaration);
//			}
//		}
//
//		// sort by offset
//		final List children= fTranslationUnit.getElementInfo().internalGetChildren();
//		Collections.sort(children, new Comparator() {
//			public int compare(Object o1, Object o2) {
//				try {
//					final SourceManipulationInfo info1= ((SourceManipulation)o1).getSourceManipulationInfo();
//					final SourceManipulationInfo info2= ((SourceManipulation)o2).getSourceManipulationInfo();
//					int delta= info1.getStartPos() - info2.getStartPos();
//					if (delta == 0) {
//						delta= info1.getIdStartPos() - info2.getIdStartPos();
//					}
//					return delta;
//				} catch (CModelException exc) {
//					return 0;
//				}
//			}});
//
//		if (isCanceled()) {
//			return;
//		}
//
//		// report problems
//		IProblemRequestor problemRequestor= fTranslationUnit.getProblemRequestor();
//		if (problemRequestor == null && PRINT_PROBLEMS) {
//			problemRequestor= new ProblemPrinter();
//		}
//		if (problemRequestor != null && problemRequestor.isActive()) {
//			problemRequestor.beginReporting();
//			final IASTProblem[] ppProblems= ast.getPreprocessorProblems();
//			IASTProblem[] problems= ppProblems;
//			for (int i= 0; i < problems.length; i++) {
//				IASTProblem problem= problems[i];
//				if (isLocalToFile(problem)) {
//					problemRequestor.acceptProblem(new ProblemAdapter(problem));
//				} else if (PRINT_PROBLEMS) {
//					System.err.println("PREPROCESSOR PROBLEM: " + problem.getMessage()); //$NON-NLS-1$
//				}
//			}
//			problems= CPPVisitor.getProblems(ast);
//			for (int i= 0; i < problems.length; i++) {
//				IASTProblem problem= problems[i];
//				if (isLocalToFile(problem)) {
//					problemRequestor.acceptProblem(new ProblemAdapter(problem));
//				} else if (PRINT_PROBLEMS) {
//					System.err.println("PROBLEM: " + problem.getMessage()); //$NON-NLS-1$
//				}
//			}
//			problemRequestor.endReporting();
//		}
//	}
//
//	private boolean isLocalToFile(IASTNode node) {
//		return fTranslationUnitFileName.equals(node.getContainingFilename());
//	}
//
//	private Include createInclusion(Parent parent, IASTPreprocessorIncludeStatement inclusion) throws CModelException{
//		// create element
//		final IASTName name= inclusion.getName();
//		Include element= new Include(parent, ASTStringUtil.getSimpleName(name), inclusion.isSystemInclude());
//		element.setFullPathName(inclusion.getPath());
//		element.setActive(inclusion.isActive());
//		element.setResolved(inclusion.isResolved());
//		// add to parent
//		parent.addChild(element);
//		// set positions
//		setIdentifierPosition(element, name);
//		setBodyPosition(element, inclusion);
//		return element;
//	}
//
//	private Macro createMacro(Parent parent, IASTPreprocessorMacroDefinition macro) throws CModelException{
//		// create element
//		final IASTName name= macro.getName();
//		Macro element= new  Macro(parent, ASTStringUtil.getSimpleName(name));
//		// add to parent
//		parent.addChild(element);
//		// set positions
//		setIdentifierPosition(element, name);
//		setBodyPosition(element, macro);
//		return element;
//	}
//
//	private void createDeclaration(Parent parent, IASTDeclaration declaration) throws CModelException, DOMException {
//		if (declaration instanceof IASTFunctionDefinition) {
//			createFunctionDefinition(parent, (IASTFunctionDefinition)declaration, false);
//		} else if (declaration instanceof IASTSimpleDeclaration) {
//			createSimpleDeclarations(parent, (IASTSimpleDeclaration)declaration, false);
//		} else if (declaration instanceof ICPPASTVisiblityLabel) {
//			handleVisibilityLabel((ICPPASTVisiblityLabel)declaration);
//		} else if(declaration instanceof ICPPASTNamespaceDefinition) {
//			createNamespace(parent, (ICPPASTNamespaceDefinition) declaration);
//		} else if (declaration instanceof ICPPASTNamespaceAlias) {
//			// TODO [cmodel] namespace alias?
//		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
//			createTemplateDeclaration(parent, (ICPPASTTemplateDeclaration)declaration);
//		} else if (declaration instanceof ICPPASTTemplateSpecialization) {
//			// TODO [cmodel] template specialization?
//		} else if (declaration instanceof ICPPASTExplicitTemplateInstantiation) {
//			// TODO [cmodel] explicit template instantiation?
//		} else if (declaration instanceof ICPPASTUsingDeclaration) {
//			createUsingDeclaration(parent, (ICPPASTUsingDeclaration)declaration);
//		} else if (declaration instanceof ICPPASTUsingDirective) {
//			createUsingDirective(parent, (ICPPASTUsingDirective)declaration);
//		} else if (declaration instanceof ICPPASTLinkageSpecification) {
//			createLinkageSpecification(parent, (ICPPASTLinkageSpecification)declaration);
//		} else if (declaration instanceof IASTASMDeclaration) {
//			// TODO [cmodel] asm declaration?
//		} else if (declaration instanceof IASTProblemDeclaration) {
//			// TODO [cmodel] problem declaration?
//		} else if (declaration instanceof IASTAmbiguousDeclaration) {
//			// TODO [cmodel] ambiguous declaration?
//		} else if (declaration instanceof IASTDeclarationAmbiguity) {
//			// TODO [cmodel] declaration ambiguity?
//		} else {
//			assert false : "TODO: " + declaration.getClass().getName(); //$NON-NLS-1$
//		}
//	}
//
//
//	/**
//	 * Handle extern "C" and related kinds.
//	 *
//	 * @param parent
//	 * @param declaration
//	 * @throws CModelException
//	 * @throws DOMException
//	 */
//	private void createLinkageSpecification(Parent parent, ICPPASTLinkageSpecification linkageDeclaration) throws CModelException, DOMException {
//		IASTDeclaration[] declarations= linkageDeclaration.getDeclarations();
//		for (int i= 0; i < declarations.length; i++) {
//			IASTDeclaration declaration= declarations[i];
//			if (linkageDeclaration.getFileLocation() != null || isLocalToFile(declaration)) {
//				createDeclaration(parent, declaration);
//			}
//		}
//	}
//
//	private CElement[] createSimpleDeclarations(Parent parent, IASTSimpleDeclaration declaration, boolean isTemplate) throws CModelException, DOMException {
//		final IASTDeclSpecifier declSpecifier= declaration.getDeclSpecifier();
//		final IASTDeclarator[] declarators= declaration.getDeclarators();
//		final CElement[] elements;
//		boolean isCompositeType= declSpecifier instanceof IASTCompositeTypeSpecifier || declSpecifier instanceof IASTEnumerationSpecifier;
//		if (declarators.length == 0) {
//			elements= new CElement[1];
//			final CElement element= createSimpleDeclaration(parent, declSpecifier, null, isTemplate);
//			elements[0]= element;
//		} else if (declarators.length == 1 && isCompositeType) {
//			elements= new CElement[declarators.length];
//			final IASTDeclarator declarator= declarators[0];
//			CElement element= createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
//			if (element instanceof IParent) {
//				parent= (Parent)element;
//				if (!isTemplate) {
//					setBodyPosition((SourceManipulation)element, declSpecifier.getParent());
//				}
//			}
//			elements[0]= element;
//			createSimpleDeclaration(parent, declSpecifier, null, isTemplate);
//		} else {
//			if (isCompositeType) {
//				createSimpleDeclaration(parent, declSpecifier, null, isTemplate);
//			}
//			elements= new CElement[declarators.length];
//			for (int i= 0; i < declarators.length; i++) {
//				final IASTDeclarator declarator= declarators[i];
//				final CElement element= createSimpleDeclaration(parent, declSpecifier, declarator, isTemplate);
//				if (!isTemplate && element instanceof SourceManipulation) {
//					setBodyPosition((SourceManipulation)element, declarator);
//				}
//				elements[i]= element;
//			}
//		}
//		return elements;
//	}
//
//	private CElement createSimpleDeclaration(Parent parent, IASTDeclSpecifier declSpecifier, IASTDeclarator declarator, boolean isTemplate) throws CModelException, DOMException {
//		if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
//			if (declarator != null) {
//				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
//			} else {
//				return createCompositeType(parent, (IASTCompositeTypeSpecifier)declSpecifier, isTemplate);
//			}
//		} else if (declSpecifier instanceof IASTElaboratedTypeSpecifier) {
//			if (declarator != null) {
//				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
//			} else {
//				return createElaboratedTypeDeclaration(parent, (IASTElaboratedTypeSpecifier)declSpecifier, isTemplate);
//			}
//		} else if (declSpecifier instanceof IASTEnumerationSpecifier) {
//			if (declarator != null) {
//				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
//			} else {
//				return createEnumeration(parent, (IASTEnumerationSpecifier)declSpecifier);
//			}
//		} else if (declSpecifier instanceof IASTNamedTypeSpecifier) {
//			return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
//		} else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
//			return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
//		} else {
//			assert false : "TODO: " + declSpecifier.getClass().getName(); //$NON-NLS-1$
//		}
//		return null;
//	}
//
//	private CElement createTypedefOrFunctionOrVariable(Parent parent, IASTDeclSpecifier declSpecifier,
//			IASTDeclarator declarator, boolean isTemplate) throws CModelException {
//		if (declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
//			return createTypeDef(parent, declSpecifier, declarator);
//		}
//		if (declarator != null) {
//			if (declarator instanceof IASTFunctionDeclarator && !hasNestedPointerOperators(declarator)) {
//				return createFunctionDeclaration(parent, declSpecifier, (IASTFunctionDeclarator)declarator, isTemplate);
//			}
//		}
//		return createVariable(parent, declSpecifier, declarator, isTemplate);
//	}
//
//	private boolean hasNestedPointerOperators(IASTDeclarator declarator) {
//		declarator= declarator.getNestedDeclarator();
//		while (declarator != null) {
//			if (declarator.getPointerOperators().length > 0) {
//				return true;
//			}
//			declarator= declarator.getNestedDeclarator();
//		}
//		return false;
//	}
//
//	private void createNamespace(Parent parent, ICPPASTNamespaceDefinition declaration) throws CModelException, DOMException{
//		// create element
//		final String type= Keywords.NAMESPACE;
//		final IASTName name= declaration.getName();
//		String nsName= ASTStringUtil.getQualifiedName(name);
//		final Namespace element= new Namespace (parent, nsName);
//		// add to parent
//		parent.addChild(element);
//		// set positions
//		if (name != null && nsName.length() > 0) {
//			setIdentifierPosition(element, name);
//		} else {
//			final IASTFileLocation nsLocation= getMinFileLocation(declaration.getNodeLocations());
//			if (nsLocation != null) {
//				element.setIdPos(nsLocation.getNodeOffset(), type.length());
//			}
//		}
//		setBodyPosition(element, declaration);
//
//		element.setTypeName(type);
//
//		IASTDeclaration[] nsDeclarations= declaration.getDeclarations();
//		for (int i= 0; i < nsDeclarations.length; i++) {
//			IASTDeclaration nsDeclaration= nsDeclarations[i];
//			if (declaration.getFileLocation() != null || isLocalToFile(nsDeclaration)) {
//				createDeclaration(element, nsDeclaration);
//			}
//		}
//	}
//
//	private StructureDeclaration createElaboratedTypeDeclaration(Parent parent, IASTElaboratedTypeSpecifier elaboratedTypeSpecifier, boolean isTemplate) throws CModelException{
//		// create element
//		final String type;
//		final int kind;
//		switch (elaboratedTypeSpecifier.getKind()) {
//		case IASTElaboratedTypeSpecifier.k_struct:
//			kind= (isTemplate) ? ICElement.C_TEMPLATE_STRUCT_DECLARATION : ICElement.C_STRUCT_DECLARATION;
//			type= Keywords.STRUCT;
//			break;
//		case IASTElaboratedTypeSpecifier.k_union:
//			kind= (isTemplate) ? ICElement.C_TEMPLATE_UNION_DECLARATION : ICElement.C_UNION_DECLARATION;
//			type= Keywords.UNION;
//			break;
//		case ICPPASTElaboratedTypeSpecifier.k_class:
//			kind= (isTemplate) ? ICElement.C_TEMPLATE_CLASS_DECLARATION : ICElement.C_CLASS_DECLARATION;
//			type= Keywords.CLASS;
//			break;
//		case IASTElaboratedTypeSpecifier.k_enum:
//			// do we need a C_ENUM_DECLARATION?
//			kind= ICElement.C_CLASS_DECLARATION;
//			type= Keywords.ENUM;
//			break;
//		default:
//			kind= ICElement.C_CLASS_DECLARATION;
//			type= ""; //$NON-NLS-1$
//			break;
//		}
//
//		final IASTName astClassName= elaboratedTypeSpecifier.getName();
//		final String className= ASTStringUtil.getSimpleName(astClassName);
//
//		StructureDeclaration element;
//		if (isTemplate) {
//			element= new StructureTemplateDeclaration(parent, kind, className);
//		} else {
//			element= new StructureDeclaration(parent, className, kind);
//		}
//		element.setTypeName(type);
//
//		// add to parent
//		parent.addChild(element);
//
//		// set positions
//		if (className.length() > 0) {
//			setIdentifierPosition(element, astClassName);
//		} else {
//			final IASTFileLocation classLocation= getMinFileLocation(elaboratedTypeSpecifier.getNodeLocations());
//			if (classLocation != null) {
//				element.setIdPos(classLocation.getNodeOffset(), type.length());
//			}
//		}
//		setBodyPosition(element, elaboratedTypeSpecifier);
//		return element;
//	}
//
//	private Enumeration createEnumeration(Parent parent, IASTEnumerationSpecifier enumSpecifier) throws CModelException{
//		// create element
//		final String type= Keywords.ENUM;
//		final IASTName astEnumName= enumSpecifier.getName();
//		final String enumName= ASTStringUtil.getSimpleName(astEnumName);
//		final Enumeration element= new Enumeration (parent, enumName);
//		// add to parent
//		parent.addChild(element);
//		final IASTEnumerator[] enumerators= enumSpecifier.getEnumerators();
//		for (int i= 0; i < enumerators.length; i++) {
//			final IASTEnumerator enumerator= enumerators[i];
//			createEnumerator(element, enumerator);
//		}
//		// set enumeration position
//		if (astEnumName != null && enumName.length() > 0) {
//			setIdentifierPosition(element, astEnumName);
//		} else {
//			final IASTFileLocation enumLocation= enumSpecifier.getFileLocation();
//			element.setIdPos(enumLocation.getNodeOffset(), type.length());
//		}
//		setBodyPosition(element, enumSpecifier);
//		element.setTypeName(type);
//		return element;
//	}
//
//	private Enumerator createEnumerator(Parent enumarator, IASTEnumerator enumDef) throws CModelException{
//		final IASTName astEnumName= enumDef.getName();
//		final Enumerator element= new Enumerator (enumarator, ASTStringUtil.getSimpleName(astEnumName));
//		IASTExpression initialValue= enumDef.getValue();
//		if(initialValue != null){
//			element.setConstantExpression(ASTSignatureUtil.getExpressionString(initialValue));
//		}
//		// add to parent
//		enumarator.addChild(element);
//		// set positions
//		setIdentifierPosition(element, astEnumName);
//		setBodyPosition(element, enumDef);
//		return element;
//	}
//
//	private Structure createCompositeType(Parent parent, IASTCompositeTypeSpecifier compositeTypeSpecifier, boolean isTemplate) throws CModelException, DOMException{
//		// create element
//		final String type;
//		final int kind;
//		final ASTAccessVisibility defaultVisibility;
//		switch (compositeTypeSpecifier.getKey()) {
//		case IASTCompositeTypeSpecifier.k_struct:
//			kind= (isTemplate) ? ICElement.C_TEMPLATE_STRUCT : ICElement.C_STRUCT;
//			type= Keywords.STRUCT;
//			defaultVisibility= ASTAccessVisibility.PUBLIC;
//			break;
//		case IASTCompositeTypeSpecifier.k_union:
//			kind= (isTemplate) ? ICElement.C_TEMPLATE_UNION : ICElement.C_UNION;
//			type= Keywords.UNION;
//			defaultVisibility= ASTAccessVisibility.PUBLIC;
//			break;
//		case ICPPASTCompositeTypeSpecifier.k_class:
//			kind= (isTemplate) ? ICElement.C_TEMPLATE_CLASS : ICElement.C_CLASS;
//			type= Keywords.CLASS;
//			defaultVisibility= ASTAccessVisibility.PRIVATE;
//			break;
//		default:
//			kind= ICElement.C_CLASS;
//			type= ""; //$NON-NLS-1$
//			defaultVisibility= ASTAccessVisibility.PUBLIC;
//			break;
//		}
//
//		final IASTName astClassName= compositeTypeSpecifier.getName();
//		final String className= ASTStringUtil.getSimpleName(astClassName);
//
//		final Structure element;
//		if (!isTemplate) {
//			Structure classElement= new Structure(parent, kind, className);
//			element= classElement;
//		} else {
//			StructureTemplate classTemplate= new StructureTemplate(parent, kind, className);
//			element= classTemplate;
//		}
//
//		if (compositeTypeSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
//			// store super classes names
//			final ICPPASTCompositeTypeSpecifier cppCompositeTypeSpecifier= (ICPPASTCompositeTypeSpecifier)compositeTypeSpecifier;
//			ICPPASTBaseSpecifier[] baseSpecifiers= cppCompositeTypeSpecifier.getBaseSpecifiers();
//			for (int i= 0; i < baseSpecifiers.length; i++) {
//				final ICPPASTBaseSpecifier baseSpecifier= baseSpecifiers[i];
//				final IASTName baseName= baseSpecifier.getName();
//				final ASTAccessVisibility visibility;
//				switch(baseSpecifier.getVisibility()) {
//				case ICPPASTBaseSpecifier.v_public:
//					visibility= ASTAccessVisibility.PUBLIC;
//					break;
//				case ICPPASTBaseSpecifier.v_protected:
//					visibility= ASTAccessVisibility.PROTECTED;
//					break;
//				case ICPPASTBaseSpecifier.v_private:
//					visibility= ASTAccessVisibility.PRIVATE;
//					break;
//				default:
//					visibility= ASTAccessVisibility.PUBLIC;
//				}
//				element.addSuperClass(ASTStringUtil.getSimpleName(baseName), visibility);
//			}
//		}
//
//		element.setTypeName(type);
//
//		// add to parent
//		parent.addChild(element);
//		// set positions
//		if(!isTemplate){
//			setBodyPosition(element, compositeTypeSpecifier);
//		}
//		if (className.length() > 0) {
//			setIdentifierPosition(element, astClassName);
//		} else {
//			final IASTFileLocation classLocation= getMinFileLocation(compositeTypeSpecifier.getNodeLocations());
//			if (classLocation != null) {
//				if (compositeTypeSpecifier.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
//					// fix positions for typedef struct (heuristically)
//					final int delta= Keywords.TYPEDEF.length() + 1;
//					element.setIdPos(classLocation.getNodeOffset() + delta, type.length());
//					if(!isTemplate){
//						final SourceManipulationInfo info= element.getSourceManipulationInfo();
//						info.setPos(info.getStartPos() + delta, info.getLength() - delta);
//					}
//				} else {
//					element.setIdPos(classLocation.getNodeOffset(), type.length());
//				}
//			}
//		}
//		// add members
//		pushDefaultVisibility(defaultVisibility);
//		try {
//			final IASTDeclaration[] memberDeclarations= compositeTypeSpecifier.getMembers();
//			for (int i= 0; i < memberDeclarations.length; i++) {
//				IASTDeclaration member= memberDeclarations[i];
//				if (compositeTypeSpecifier.getFileLocation() != null || isLocalToFile(member)) {
//					createDeclaration(element, member);
//				}
//			}
//		} finally {
//			popDefaultVisibility();
//		}
//		return element;
//	}
//
//	private TypeDef createTypeDef(Parent parent, IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) throws CModelException{
//		IASTDeclarator nestedDeclarator= declarator;
//		while (nestedDeclarator.getNestedDeclarator() != null) {
//			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
//		}
//		final IASTName astTypedefName= nestedDeclarator.getName();
//		if (astTypedefName == null) {
//			return null;
//		}
//		// create the element
//		String name= ASTStringUtil.getSimpleName(astTypedefName);
//
//        final TypeDef element= new TypeDef(parent, name);
//
//        String typeName= ASTStringUtil.getSignatureString(declSpecifier, declarator);
//		element.setTypeName(typeName);
//
//		// add to parent
//		parent.addChild(element);
//
//		// set positions
//		final SourceManipulationInfo info= element.getSourceManipulationInfo();
//		if (name.length() > 0) {
//			setIdentifierPosition(info, astTypedefName);
//		} else {
//			setIdentifierPosition(info, declSpecifier);
//		}
//		if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
//			setBodyPosition(info, astTypedefName);
//		} else {
//			setBodyPosition(info, declSpecifier.getParent());
//		}
//		return element;
//	}
//
//	private VariableDeclaration createVariable(Parent parent, IASTDeclSpecifier specifier, IASTDeclarator declarator, boolean isTemplate) throws CModelException {
//		if (declarator == null) {
//			return null;
//		}
//		IASTDeclarator nestedDeclarator= declarator;
//		while (nestedDeclarator.getNestedDeclarator() != null) {
//			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
//		}
//		final IASTName astVariableName= nestedDeclarator.getName();
//		if (astVariableName == null) {
//			return null;
//		}
//		final String variableName= ASTStringUtil.getQualifiedName(astVariableName);
//
//		final VariableDeclaration element;
//		final SourceManipulationInfo info;
//
//		if (declarator instanceof IASTFieldDeclarator || parent instanceof IStructure
//				|| FortranModelBuilder.getScope(astVariableName) instanceof ICPPClassScope) {
//			// field
//			Field newElement= new Field(parent, variableName);
//			if (specifier instanceof ICPPASTDeclSpecifier) {
//				final ICPPASTDeclSpecifier cppSpecifier= (ICPPASTDeclSpecifier)specifier;
//				newElement.setMutable(cppSpecifier.getStorageClass() == ICPPASTDeclSpecifier.sc_mutable);
//			}
//			newElement.setTypeName(ASTStringUtil.getSignatureString(specifier, declarator));
//			final FieldInfo fieldInfo= (FieldInfo)newElement.getElementInfo();
//			fieldInfo.setVisibility(getCurrentVisibility());
//			element= newElement;
//			info= fieldInfo;
//		} else {
//			if (isTemplate) {
//				// template variable
//				VariableTemplate newElement= new VariableTemplate(parent, variableName);
//				element= newElement;
//			} else {
//				if (specifier.getStorageClass() == IASTDeclSpecifier.sc_extern) {
//					// variable declaration
//					VariableDeclaration newElement= new VariableDeclaration(parent, variableName);
//					element= newElement;
//				} else {
//					// variable
//					Variable newElement= new Variable(parent, variableName);
//					element= newElement;
//				}
//			}
//			element.setTypeName(ASTStringUtil.getSignatureString(specifier, declarator));
//			info= element.getSourceManipulationInfo();
//		}
//		element.setConst(specifier.isConst());
//		element.setVolatile(specifier.isVolatile());
//		// TODO [cmodel] correctly resolve isStatic
//		element.setStatic(specifier.getStorageClass() == IASTDeclSpecifier.sc_static);
//		// add to parent
//		parent.addChild(element);
//
//		// set positions
//		setIdentifierPosition(info, astVariableName);
//		if (!isTemplate) {
//			if (specifier instanceof IASTCompositeTypeSpecifier) {
//				setBodyPosition(info, astVariableName);
//			} else {
//				setBodyPosition(info, specifier.getParent());
//			}
//		}
//		return element;
//	}
//
//	private FunctionDeclaration createFunctionDefinition(Parent parent, IASTFunctionDefinition functionDeclaration, boolean isTemplate) throws CModelException, DOMException {
//		final IASTFunctionDeclarator declarator= functionDeclaration.getDeclarator();
//		IASTDeclarator nestedDeclarator= declarator;
//		while (nestedDeclarator.getNestedDeclarator() != null) {
//			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
//		}
//		final IASTName name= nestedDeclarator.getName();
//        if (name == null) {
//            // Something is wrong, skip this element
//            return null;
//        }
//
//        final IASTDeclSpecifier declSpecifier= functionDeclaration.getDeclSpecifier();
//
//		final String functionName= ASTStringUtil.getSimpleName(name);
//		final String[] parameterTypes= ASTStringUtil.getParameterSignatureArray(declarator);
//		final String returnType= ASTStringUtil.getTypeString(declSpecifier, declarator);
//
//		final FunctionDeclaration element;
//		final FunctionInfo info;
//
//		if (declarator instanceof ICPPASTFunctionDeclarator) {
//
//			final ICPPASTFunctionDeclarator cppFunctionDeclarator= (ICPPASTFunctionDeclarator)declarator;
//			final IASTName simpleName;
//			if (name instanceof ICPPASTQualifiedName) {
//				final ICPPASTQualifiedName quName= (ICPPASTQualifiedName)name;
//				simpleName= quName.getLastName();
//			} else {
//				simpleName= name;
//			}
//			IScope scope= null;
//			// try to avoid expensive resolution of scope and binding
//			boolean isMethod= parent instanceof IStructure;
//			if (!isMethod && name instanceof ICPPASTQualifiedName) {
//				final IASTName[] names= ((ICPPASTQualifiedName)name).getNames();
//				if (isTemplate) {
//					for (int i= 0; i < names.length; i++) {
//						if (names[i] instanceof ICPPASTTemplateId) {
//							isMethod= true;
//							break;
//						}
//					}
//				}
//				if (!isMethod) {
//					scope= CPPVisitor.getContainingScope(simpleName);
//			        isMethod= scope instanceof ICPPClassScope;
//				}
//			}
//			if (isMethod) {
//				// method
//				final MethodDeclaration methodElement;
//				if (isTemplate) {
//					methodElement= new MethodTemplate(parent, ASTStringUtil.getQualifiedName(name));
//				} else {
//					methodElement= new Method(parent, ASTStringUtil.getQualifiedName(name));
//				}
//				element= methodElement;
//				// establish identity attributes before getElementInfo()
//				methodElement.setParameterTypes(parameterTypes);
//				methodElement.setReturnType(returnType);
//				methodElement.setConst(cppFunctionDeclarator.isConst());
//				final MethodInfo methodInfo= methodElement.getMethodInfo();
//				info= methodInfo;
//				ICPPMethod methodBinding= null;
//				if (scope != null) {
//					final IBinding binding= simpleName.resolveBinding();
//					if (binding instanceof ICPPMethod) {
//						methodBinding= (ICPPMethod)binding;
//					}
//				}
//				if (methodBinding != null) {
//					methodInfo.setVirtual(methodBinding.isVirtual());
//					methodInfo.setInline(methodBinding.isInline());
//					methodInfo.setFriend(((ICPPASTDeclSpecifier)declSpecifier).isFriend());
//					methodInfo.setVolatile(cppFunctionDeclarator.isVolatile());
//					methodInfo.setVisibility(adaptVisibilityConstant(methodBinding.getVisibility()));
//					methodInfo.setPureVirtual(false);
//					methodElement.setConstructor(methodBinding instanceof ICPPConstructor);
//					methodElement.setDestructor(methodBinding.isDestructor());
//				} else {
//					if (declSpecifier instanceof ICPPASTDeclSpecifier) {
//						final ICPPASTDeclSpecifier cppDeclSpecifier= (ICPPASTDeclSpecifier)declSpecifier;
//						methodInfo.setVirtual(cppDeclSpecifier.isVirtual());
//						methodInfo.setInline(cppDeclSpecifier.isInline());
//						methodInfo.setFriend(cppDeclSpecifier.isFriend());
//					}
//					methodInfo.setVolatile(cppFunctionDeclarator.isVolatile());
//					methodInfo.setVisibility(getCurrentVisibility());
//					methodInfo.setPureVirtual(false);
//					final boolean isConstructor;
//					if (scope != null) {
//						isConstructor= CPPVisitor.isConstructor(scope, declarator);
//					} else {
//						isConstructor= parent.getElementName().equals(functionName);
//					}
//					methodElement.setConstructor(isConstructor);
//					methodElement.setDestructor(functionName.charAt(0) == '~');
//				}
//			} else {
//				if (isTemplate) {
//					// template function
//					element= new FunctionTemplate(parent, ASTStringUtil.getQualifiedName(name));
//				} else {
//					// function
//					element= new Function(parent, ASTStringUtil.getQualifiedName(name));
//				}
//				element.setParameterTypes(parameterTypes);
//				element.setReturnType(returnType);
//				info= element.getFunctionInfo();
//				info.setConst(cppFunctionDeclarator.isConst());
//			}
//
//		} else {
//			element= new Function(parent, functionName);
//			element.setParameterTypes(parameterTypes);
//			element.setReturnType(returnType);
//			info= element.getFunctionInfo();
//		}
//
//		// TODO [cmodel] correctly resolve isStatic
//		info.setStatic(declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static);
//
//		// add to parent
//		parent.addChild(element);
//
//		// set positions
//		setIdentifierPosition(info, name);
//		if (!isTemplate) {
//			setBodyPosition(info, functionDeclaration);
//		}
//		return element;
//	}
//
//	private FunctionDeclaration createFunctionDeclaration(Parent parent, IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator, boolean isTemplate) throws CModelException {
//		IASTDeclarator nestedDeclarator= declarator;
//		while (nestedDeclarator.getNestedDeclarator() != null) {
//			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
//		}
//		final IASTName name= nestedDeclarator.getName();
//        if (name == null) {
//            // Something is wrong, skip this element
//            return null;
//        }
//
//		final String functionName= ASTStringUtil.getSimpleName(name);
//		final String[] parameterTypes= ASTStringUtil.getParameterSignatureArray(declarator);
//		final String returnType= ASTStringUtil.getTypeString(declSpecifier, declarator);
//
//		final FunctionDeclaration element;
//		final FunctionInfo info;
//
//		if (declarator instanceof ICPPASTFunctionDeclarator) {
//			final ICPPASTFunctionDeclarator cppFunctionDeclarator= (ICPPASTFunctionDeclarator)declarator;
//			if (parent instanceof IStructure) {
//				// method
//				final MethodDeclaration methodElement;
//				if (isTemplate) {
//					methodElement= new MethodTemplateDeclaration(parent, functionName);
//				} else {
//					methodElement= new MethodDeclaration(parent, functionName);
//				}
//				element= methodElement;
//				// establish identity attributes before getElementInfo()
//				methodElement.setParameterTypes(parameterTypes);
//				methodElement.setReturnType(returnType);
//				methodElement.setConst(cppFunctionDeclarator.isConst());
//				final MethodInfo methodInfo= methodElement.getMethodInfo();
//				info= methodInfo;
//				if (declSpecifier instanceof ICPPASTDeclSpecifier) {
//					final ICPPASTDeclSpecifier cppDeclSpecifier= (ICPPASTDeclSpecifier)declSpecifier;
//					methodInfo.setVirtual(cppDeclSpecifier.isVirtual());
//					methodInfo.setInline(cppDeclSpecifier.isInline());
//					methodInfo.setFriend(cppDeclSpecifier.isFriend());
//				}
//				methodInfo.setVolatile(cppFunctionDeclarator.isVolatile());
//				methodInfo.setVisibility(getCurrentVisibility());
//				methodInfo.setPureVirtual(cppFunctionDeclarator.isPureVirtual());
//				methodElement.setConstructor(functionName.equals(parent.getElementName()));
//				methodElement.setDestructor(functionName.charAt(0) == '~');
//			} else {
//				if (isTemplate) {
//					element= new FunctionTemplateDeclaration(parent, functionName);
//				} else {
//					element= new FunctionDeclaration(parent, functionName);
//				}
//				element.setParameterTypes(parameterTypes);
//				element.setReturnType(returnType);
//				info= (FunctionInfo)element.getElementInfo();
//				info.setConst(cppFunctionDeclarator.isConst());
//			}
//		} else if (declarator instanceof IASTStandardFunctionDeclarator) {
//			if (isTemplate) {
//				element= new FunctionTemplateDeclaration(parent, functionName);
//			} else {
//				element= new FunctionDeclaration(parent, functionName);
//			}
//			element.setParameterTypes(parameterTypes);
//			element.setReturnType(returnType);
//			info= (FunctionInfo)element.getElementInfo();
//		} else {
//			assert false;
//			return null;
//		}
//
//		// TODO [cmodel] correctly resolve isStatic
//		info.setStatic(declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static);
//
//		// add to parent
//		parent.addChild(element);
//
//		// hook up the offsets
//		setIdentifierPosition(info, name);
//		if (!isTemplate) {
//			setBodyPosition(info, declarator);
//		}
//		return element;
//	}
//
//	private Using createUsingDirective(Parent parent, ICPPASTUsingDirective usingDirDeclaration) throws CModelException{
//		// create the element
//		IASTName name= usingDirDeclaration.getQualifiedName();
//        Using element= new Using(parent, ASTStringUtil.getQualifiedName(name), true);
//
//		// add to parent
//		parent.addChild(element);
//
//		// set positions
//		setIdentifierPosition(element, name);
//		setBodyPosition(element, usingDirDeclaration);
//		return element;
//	}
//
//	private Using createUsingDeclaration(Parent parent, ICPPASTUsingDeclaration usingDeclaration) throws CModelException{
//		// create the element
//		IASTName name= usingDeclaration.getName();
//		Using element= new Using(parent, ASTStringUtil.getSimpleName(name), false);
//
//		// add to parent
//		parent.addChild(element);
//
//		// set positions
//		setIdentifierPosition(element, name);
//		setBodyPosition(element, usingDeclaration);
//		return element;
//	}
//
//	/**
//	 * Utility method to set the body position of an element from an AST node.
//	 *
//	 * @param element
//	 * @param astNode
//	 * @throws CModelException 
//	 */
//	private void setBodyPosition(SourceManipulation element, IASTNode astNode) throws CModelException {
//		setBodyPosition(element.getSourceManipulationInfo(), astNode);
//	}
//
//	/**
//	 * Utility method to set the body position of an element from an AST node.
//	 *
//	 * @param info
//	 * @param astNode
//	 */
//	private void setBodyPosition(SourceManipulationInfo info, IASTNode astNode) {
//		final IASTFileLocation location= astNode.getFileLocation();
//		if (location != null) {
//			info.setPos(location.getNodeOffset(), location.getNodeLength());
//			info.setLines(location.getStartingLineNumber(), location.getEndingLineNumber());
//		} else {
//			final IASTNodeLocation[] locations= astNode.getNodeLocations();
//			final IASTFileLocation minLocation= getMinFileLocation(locations);
//			if (minLocation != null) {
//				final IASTFileLocation maxLocation= getMaxFileLocation(locations);
//				if (maxLocation != null) {
//					final int startOffset= minLocation.getNodeOffset();
//					final int endOffset= maxLocation.getNodeOffset() + maxLocation.getNodeLength();
//					info.setPos(startOffset, endOffset - startOffset);
//					final int startLine= minLocation.getStartingLineNumber();
//					final int endLine= maxLocation.getEndingLineNumber();
//					info.setLines(startLine, endLine);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Utility method to set the identifier position of an element from an AST name.
//	 *
//	 * @param element
//	 * @param astName
//	 * @throws CModelException 
//	 */
//	private void setIdentifierPosition(SourceManipulation element, IASTNode astName) throws CModelException {
//		setIdentifierPosition(element.getSourceManipulationInfo(), astName);
//	}
//
//	/**
//	 * Utility method to set the identifier position of an element from an AST name.
//	 *
//	 * @param info
//	 * @param astName
//	 */
//	private void setIdentifierPosition(SourceManipulationInfo info, IASTNode astName) {
//		final IASTFileLocation location= astName.getFileLocation();
//		if (location != null) {
//			assert fTranslationUnitFileName.equals(location.getFileName());
//			info.setIdPos(location.getNodeOffset(), location.getNodeLength());
//		} else {
//			final IASTNodeLocation[] locations= astName.getNodeLocations();
//			final IASTFileLocation minLocation= getMinFileLocation(locations);
//			if (minLocation != null) {
//				final IASTFileLocation maxLocation= getMaxFileLocation(locations);
//				if (maxLocation != null) {
//					final int startOffset= minLocation.getNodeOffset();
//					final int endOffset= maxLocation.getNodeOffset() + maxLocation.getNodeLength();
//					info.setIdPos(startOffset, endOffset - startOffset);
//				}
//			}
//		}
//	}
//
//	private static IASTFileLocation getMaxFileLocation(IASTNodeLocation[] locations) {
//		if (locations == null || locations.length == 0) {
//			return null;
//		}
//		final IASTNodeLocation nodeLocation= locations[locations.length-1];
//		if (nodeLocation instanceof IASTFileLocation) {
//			return (IASTFileLocation)nodeLocation;
//		} else if (nodeLocation instanceof IASTMacroExpansion) {
//			IASTNodeLocation[] macroLocations= ((IASTMacroExpansion)nodeLocation).getExpansionLocations();
//			return getMaxFileLocation(macroLocations);
//		}
//		return null;
//	}
//
//	private static IASTFileLocation getMinFileLocation(IASTNodeLocation[] locations) {
//		if (locations == null || locations.length == 0) {
//			return null;
//		}
//		final IASTNodeLocation nodeLocation= locations[0];
//		if (nodeLocation instanceof IASTFileLocation) {
//			return (IASTFileLocation)nodeLocation;
//		} else if (nodeLocation instanceof IASTMacroExpansion) {
//			IASTNodeLocation[] macroLocations= ((IASTMacroExpansion)nodeLocation).getExpansionLocations();
//			return getMinFileLocation(macroLocations);
//		}
//		return null;
//	}
//
//	/**
//	 * Handle the special "declaration" visibility label
//	 * @param visibilityLabel
//	 */
//	private void handleVisibilityLabel(ICPPASTVisiblityLabel visibilityLabel) {
//		setCurrentVisibility(adaptVisibilityConstant(visibilityLabel.getVisibility()));
//	}
//
//	/**
//	 * Convert the given <code>ICPPASTVisiblityLabel</code> visibility constant
//	 * into an <code>ASTAccessVisibility</code>.
//	 *
//	 * @param visibility
//	 * @return the corresponding <code>ASTAccessVisibility</code>
//	 */
//	private ASTAccessVisibility adaptVisibilityConstant(int visibility) {
//		switch(visibility) {
//		case ICPPASTVisiblityLabel.v_public:
//			return ASTAccessVisibility.PUBLIC;
//		case ICPPASTVisiblityLabel.v_protected:
//			return ASTAccessVisibility.PROTECTED;
//		case ICPPASTVisiblityLabel.v_private:
//			return ASTAccessVisibility.PRIVATE;
//		}
//		assert false : "Unknown visibility"; //$NON-NLS-1$
//		return ASTAccessVisibility.PUBLIC;
//	}
//
//	private void setCurrentVisibility(ASTAccessVisibility visibility) {
//		fCurrentVisibility= visibility;
//	}
//	private ASTAccessVisibility getCurrentVisibility() {
//		if (fCurrentVisibility == null) {
//			return ASTAccessVisibility.PUBLIC;
//		}
//		return fCurrentVisibility;
//	}
//
//	/**
//	 * Pop the default visibility from the outer scope.
//	 */
//	private void popDefaultVisibility() {
//		if (!fVisibilityStack.isEmpty()) {
//			setCurrentVisibility((ASTAccessVisibility)fVisibilityStack.pop());
//		}
//	}
//
//	/**
//	 * Push given visibility as default class/struct/union visibility.
//	 *
//	 * @param visibility
//	 */
//	private void pushDefaultVisibility(ASTAccessVisibility visibility) {
//		fVisibilityStack.push(getCurrentVisibility());
//		setCurrentVisibility(visibility);
//	}
//
//	/**
//	 * Determine the scope for given name.
//	 * 
//	 * @param astName
//	 * @return the scope or <code>null</code>
//	 */
//	private static IScope getScope(IASTName astName) {
//		IBinding binding= astName.getBinding();
//		if (binding != null) {
//			try {
//				return binding.getScope();
//			} catch (DOMException e) {
//				return null;
//			}
//		}
//		return null;
//	}

}
