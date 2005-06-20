/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.fdt.internal.core.model;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IQuickParseCallback;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifierOwner;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.Enumeration;
import org.eclipse.cdt.internal.core.model.Enumerator;
import org.eclipse.cdt.internal.core.model.Field;
import org.eclipse.cdt.internal.core.model.Function;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.model.Include;
import org.eclipse.cdt.internal.core.model.Macro;
import org.eclipse.cdt.internal.core.model.Method;
import org.eclipse.cdt.internal.core.model.MethodDeclaration;
import org.eclipse.cdt.internal.core.model.Namespace;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.cdt.internal.core.model.Structure;
import org.eclipse.cdt.internal.core.model.StructureDeclaration;
import org.eclipse.cdt.internal.core.model.Using;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.internal.core.model.Variable;
import org.eclipse.cdt.internal.core.model.VariableDeclaration;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.StructuralParseCallback;
import org.eclipse.cdt.internal.core.model.TypeDef;
import org.eclipse.fdt.core.parser.FortranParserLanguage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;


public class FortranModelBuilder {
	
	private org.eclipse.fdt.internal.core.model.FortranTranslationUnit translationUnit;
	private Map newElements;
	private IQuickParseCallback quickParseCallback;
	private static char[] EMPTY_CHAR_ARRAY = {};
	// indicator if the unit has parse errors
	private boolean hasNoErrors = false;

	class ProblemCallback extends StructuralParseCallback {
		IProblemRequestor problemRequestor;

		public ProblemCallback(IProblemRequestor requestor) {
			problemRequestor = requestor;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.parser.QuickParseCallback#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
		 */
		public boolean acceptProblem(IProblem problem) {
			// Do not worry for now about problems in other files.
			if (inclusionLevel == 0) {
				problemRequestor.acceptProblem(problem);
			}
			return true;
		}
	}

	public FortranModelBuilder(org.eclipse.fdt.internal.core.model.FortranTranslationUnit tu, Map newElements) {
		this.translationUnit = tu ;
		this.newElements = newElements;
	}

	private IASTCompilationUnit parse(boolean quickParseMode, boolean throwExceptionOnError) throws ParserException
	{
		IProject currentProject = null;
		boolean hasCppNature = true;
		char[] code = EMPTY_CHAR_ARRAY; //$NON-NLS-1$
		
		// get the current project
		if (translationUnit != null && translationUnit.getCProject() != null) {
			currentProject = translationUnit.getCProject().getProject();
		}
		// check the project's nature
		if( currentProject != null )
		{
			hasCppNature = CoreModel.hasCCNature(currentProject);
		}
		// get the code to parse
		try{
			code = translationUnit.getBuffer().getCharacters();
		} catch (CModelException e) {
			
		}

		final IProblemRequestor problemRequestor = translationUnit.getProblemRequestor();
		// use quick or structural parse mode
		ParserMode mode = quickParseMode ? ParserMode.QUICK_PARSE : ParserMode.STRUCTURAL_PARSE;
		if (problemRequestor == null) {
			quickParseCallback = (quickParseMode) ? ParserFactory.createQuickParseCallback() :
				ParserFactory.createStructuralParseCallback();
		} else {
			quickParseCallback = new ProblemCallback(problemRequestor);
		}

		// pick the language
		FortranParserLanguage language = FortranParserLanguage.Fortran;
		
		// create the parser
		IParser parser = null;
		try
		{
			IScannerInfo scanInfo = new ScannerInfo();
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(currentProject);
			if (provider != null){
				IScannerInfo buildScanInfo = null;
				IResource res = translationUnit.getResource();
				if (res != null) {
					buildScanInfo = provider.getScannerInformation(res);
				}
				// Technically we should not do this.  But the managed scanner providers
				// may rely on this behaviour i.e. only provide scannerInfo for projects
				if (buildScanInfo == null) {
					buildScanInfo = provider.getScannerInformation(currentProject);
				}
				if (buildScanInfo != null){
					scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
				}
			}

			CodeReader reader =
				translationUnit.getUnderlyingResource() != null
					? new CodeReader(translationUnit.getUnderlyingResource().getLocation().toOSString(), code)
					: new CodeReader(code);
			parser = ParserFactory.createParser( 
				ParserFactory.createScanner(
					reader,
					scanInfo, 
					mode, 
					language, 
					quickParseCallback, 
					quickParseMode ? new NullLogService() : ParserUtil.getScannerLogService(), null)
				,quickParseCallback, 
				mode, 
				language, 
				ParserUtil.getParserLogService() );
		}
		catch( ParserFactoryError pfe )
		{
			throw new ParserException( CCorePlugin.getResourceString("CModelBuilder.Parser_Construction_Failure")); //$NON-NLS-1$
		}
		// call parse
		if (problemRequestor != null) {
			problemRequestor.beginReporting();
		}
		hasNoErrors = parser.parse(); 
		if (problemRequestor != null) {
			problemRequestor.endReporting();
		}
		if( (!hasNoErrors)  && throwExceptionOnError )
			throw new ParserException(CCorePlugin.getResourceString("CModelBuilder.Parse_Failure")); //$NON-NLS-1$
		return quickParseCallback.getCompilationUnit(); 
	}
	

	public Map parse(boolean quickParseMode) throws Exception {
		long startTime = System.currentTimeMillis();
		try
		{
			parse(quickParseMode, true);		
		}
			
		catch( ParserException e )
		{
			Util.debugLog( "Parse Exception in FortranModelBuilder", IDebugLogConstants.MODEL );  //$NON-NLS-1$
			//e.printStackTrace();
		}
		Util.debugLog("CModel parsing: "+ ( System.currentTimeMillis() - startTime ) + "ms", IDebugLogConstants.MODEL); //$NON-NLS-1$ //$NON-NLS-2$
		
		startTime = System.currentTimeMillis();
		try
		{ 
			generateModelElements();
			// important to know if the unit has parse errors or not
			translationUnit.getElementInfo().setIsStructureKnown(hasNoErrors && quickParseCallback.hasNoProblems());
		}
		catch( NullPointerException npe )
		{
			Util.debugLog( "NullPointer exception in CModelBuilder", IDebugLogConstants.MODEL);  //$NON-NLS-1$
		}
				 
		// For the debuglog to take place, you have to call
		// Util.setDebugging(true);
		// Or set debug to true in the core plugin preference 
		Util.debugLog("CModel building: "+ ( System.currentTimeMillis() - startTime ) + "ms", IDebugLogConstants.MODEL); //$NON-NLS-1$ //$NON-NLS-2$
		return this.newElements;
		
	}	
	
	private void generateModelElements() throws CModelException{
		Iterator i = quickParseCallback.iterateOffsetableElements();
		while (i.hasNext()){
			IASTOffsetableElement offsetable = (IASTOffsetableElement)i.next();
			if(offsetable instanceof IASTInclusion){
				IASTInclusion inc = (IASTInclusion) offsetable;
				if( ! inc.isImplicit() )
					createInclusion(translationUnit, inc ); 		
			}
			else if(offsetable instanceof IASTMacro){
				
				IASTMacro macro = (IASTMacro) offsetable;
				if( ! macro.isImplicit() )
					createMacro(translationUnit, macro);				
			}else if(offsetable instanceof IASTDeclaration){
				try{
					generateModelElements (translationUnit, (IASTDeclaration) offsetable);
				} catch(ASTNotImplementedException e){
				}
			}
		} 
	}	

	private void generateModelElements (Parent parent, IASTDeclaration declaration) throws CModelException, ASTNotImplementedException
	{
		if(declaration instanceof IASTNamespaceDefinition ) {
			generateModelElements(parent, (IASTNamespaceDefinition) declaration);
		}

		if(declaration instanceof IASTAbstractTypeSpecifierDeclaration ) {
			generateModelElements(parent, (IASTAbstractTypeSpecifierDeclaration) declaration);
		}

		if(declaration instanceof IASTTemplateDeclaration ) {
			generateModelElements(parent, (IASTTemplateDeclaration) declaration);
		}

		if(declaration instanceof IASTTypedefDeclaration ) {
			generateModelElements(parent, (IASTTypedefDeclaration) declaration);
		}
		
		if(declaration instanceof IASTLinkageSpecification) {
			generateModelElements(parent, (IASTLinkageSpecification)declaration);
		}

		if (declaration instanceof IASTUsingDirective) {
			createUsingDirective(parent, (IASTUsingDirective)declaration);
		}

		if (declaration instanceof IASTUsingDeclaration) {
			createUsingDeclaration(parent, (IASTUsingDeclaration)declaration);
		}
		createSimpleElement(parent, declaration, false);
	}
	
	private void generateModelElements (Parent parent, IASTNamespaceDefinition declaration) throws CModelException, ASTNotImplementedException{
		// IASTNamespaceDefinition 
		IParent namespace = createNamespace(parent, declaration);
		Iterator nsDecls = declaration.getDeclarations();
		while (nsDecls.hasNext()){
			IASTDeclaration subNsDeclaration = (IASTDeclaration) nsDecls.next();
			generateModelElements((Parent)namespace, subNsDeclaration);			
		}
	}

	private void generateModelElements (Parent parent, IASTLinkageSpecification declaration) throws CModelException, ASTNotImplementedException{
		// IASTLinkageSpecification
		Iterator nsDecls = declaration.getDeclarations();
		while (nsDecls.hasNext()){
			IASTDeclaration subNsDeclaration = (IASTDeclaration) nsDecls.next();
			generateModelElements(parent, subNsDeclaration);			
		}
	}
	
	private void generateModelElements (Parent parent, IASTAbstractTypeSpecifierDeclaration abstractDeclaration) throws CModelException, ASTNotImplementedException
	{
		// IASTAbstractTypeSpecifierDeclaration 
		CElement element = createAbstractElement(parent, abstractDeclaration, false, true);
	}

	private void generateModelElements (Parent parent, IASTTemplateDeclaration templateDeclaration) throws CModelException, ASTNotImplementedException
	{				
		// Template Declaration 
		IASTDeclaration declaration = templateDeclaration.getOwnedDeclaration();
		if (declaration instanceof IASTAbstractTypeSpecifierDeclaration){
			IASTAbstractTypeSpecifierDeclaration abstractDeclaration = (IASTAbstractTypeSpecifierDeclaration)declaration ;
			CElement element = createAbstractElement(parent, abstractDeclaration , true, true);
			if (element instanceof SourceManipulation) {
				SourceManipulation sourceRef = (SourceManipulation)element;
				// set the element position		
				sourceRef.setPos(templateDeclaration.getStartingOffset(), templateDeclaration.getEndingOffset() - templateDeclaration.getStartingOffset());
				sourceRef.setLines( templateDeclaration.getStartingLine(), templateDeclaration.getEndingLine() );
				// set the template parameters				
				String[] parameterTypes = ASTUtil.getTemplateParameters(templateDeclaration);
				ITemplate classTemplate = (ITemplate) element;
				classTemplate.setTemplateParameterTypes(parameterTypes);				
			}
		} else if (declaration instanceof IASTClassSpecifier){
			// special case for Structural parse
			IASTClassSpecifier classSpecifier = (IASTClassSpecifier)declaration ;
			CElement element = createClassSpecifierElement(parent, classSpecifier , true);
			if (element instanceof SourceManipulation) {
				SourceManipulation sourceRef = (SourceManipulation)element;
				// set the element position		
				sourceRef.setPos(templateDeclaration.getStartingOffset(), templateDeclaration.getEndingOffset() - templateDeclaration.getStartingOffset());
				sourceRef.setLines( templateDeclaration.getStartingLine(), templateDeclaration.getEndingLine() );
				// set the template parameters				
				String[] parameterTypes = ASTUtil.getTemplateParameters(templateDeclaration);
				ITemplate classTemplate = (ITemplate) element;
				classTemplate.setTemplateParameterTypes(parameterTypes);				
			}
		}
		ITemplate template = null;
		template = (ITemplate) createSimpleElement(parent, declaration, true);
	 	 
 		if (template instanceof SourceManipulation){
			SourceManipulation sourceRef = (SourceManipulation)template;
			// set the element position		
			sourceRef.setPos(templateDeclaration.getStartingOffset(), templateDeclaration.getEndingOffset() - templateDeclaration.getStartingOffset());
			sourceRef.setLines( templateDeclaration.getStartingLine(), templateDeclaration.getEndingLine() );
			// set the template parameters
			String[] parameterTypes = ASTUtil.getTemplateParameters(templateDeclaration);	
			template.setTemplateParameterTypes(parameterTypes);				
		}
	}

	private void generateModelElements (Parent parent, IASTTypedefDeclaration declaration) throws CModelException, ASTNotImplementedException
	{
		TypeDef typeDef = createTypeDef(parent, declaration);
		IASTAbstractDeclaration abstractDeclaration = declaration.getAbstractDeclarator();
		CElement element = createAbstractElement(parent, abstractDeclaration, false, true);
	}
		
	private CElement createClassSpecifierElement(Parent parent, IASTClassSpecifier classSpecifier, boolean isTemplate)throws ASTNotImplementedException, CModelException{
		CElement element = null;
		IParent classElement = createClass(parent, classSpecifier, isTemplate);
		element = (CElement) classElement;
				
		// create the sub declarations 
		Iterator j  = classSpecifier.getDeclarations();
		while (j.hasNext()){
			IASTDeclaration subDeclaration = (IASTDeclaration)j.next();
			generateModelElements((Parent)classElement, subDeclaration);					
		} // end while j	
		return element;
	}
	
	private CElement createAbstractElement(Parent parent, IASTTypeSpecifierOwner abstractDeclaration, boolean isTemplate, boolean isDeclaration)throws ASTNotImplementedException, CModelException{
		CElement element = null;
		if (abstractDeclaration != null){
			IASTTypeSpecifier typeSpec = abstractDeclaration.getTypeSpecifier();
			// IASTEnumerationSpecifier
			if (typeSpec instanceof IASTEnumerationSpecifier) {
				IASTEnumerationSpecifier enumSpecifier = (IASTEnumerationSpecifier) typeSpec;
				IParent enumElement = createEnumeration (parent, enumSpecifier);
				element = (CElement) enumElement;
			}
			// IASTClassSpecifier
			else if (typeSpec instanceof IASTClassSpecifier) {
				IASTClassSpecifier classSpecifier = (IASTClassSpecifier) typeSpec;
				element = createClassSpecifierElement (parent, classSpecifier, isTemplate);
			} else if (isDeclaration && typeSpec instanceof IASTElaboratedTypeSpecifier) {
				// This is not a model element, so we don't create anything here.
				// However, do we need to do anything else?
				IASTElaboratedTypeSpecifier elabSpecifier = (IASTElaboratedTypeSpecifier) typeSpec;
				element = createElaboratedTypeSpecifier(parent, elabSpecifier);				
			}
		}				
		return element;		
	}
	
	private CElement createSimpleElement(Parent parent, IASTDeclaration declaration, boolean isTemplate)throws CModelException, ASTNotImplementedException{

		CElement element = null;
		if (declaration instanceof IASTVariable)
		{
			element = createVariableSpecification(parent, (IASTVariable)declaration, isTemplate); 
		}	
		// function or method 
		else if(declaration instanceof IASTFunction ) 
		{
			element = createFunctionSpecification(parent, (IASTFunction)declaration, isTemplate);
		}		
		return element;
	}
	
	private StructureDeclaration createElaboratedTypeSpecifier(Parent parent, IASTElaboratedTypeSpecifier typeSpec) throws CModelException{
		// create element
		ASTClassKind classkind = typeSpec.getClassKind();
		int kind = -1;
		if (classkind == ASTClassKind.CLASS) {
			kind = ICElement.C_CLASS_DECLARATION;
		} else if (classkind == ASTClassKind.STRUCT) {
			kind = ICElement.C_STRUCT_DECLARATION;
		} else if (classkind == ASTClassKind.UNION) {
			kind = ICElement.C_UNION_DECLARATION;
		}
		String className = (typeSpec.getName() == null)
		? "" //$NON-NLS-1$
		: typeSpec.getName().toString();				
		StructureDeclaration element = new StructureDeclaration(parent, className, kind);

		// add to parent
		parent.addChild(element);
		// set position
		element.setIdPos(typeSpec.getNameOffset(), typeSpec.getNameEndOffset() - typeSpec.getNameOffset());
		element.setPos(typeSpec.getStartingOffset(), typeSpec.getEndingOffset() - typeSpec.getStartingOffset());
		element.setLines(typeSpec.getStartingLine(), typeSpec.getEndingLine());
		this.newElements.put(element, element.getElementInfo());
		return element;
	}

	private Include createInclusion(Parent parent, IASTInclusion inclusion) throws CModelException{
		// create element
		Include element = new Include(parent, inclusion.getName(), !inclusion.isLocal());
		element.setFullPathName(inclusion.getFullFileName());
		// add to parent
		parent.addChild(element);
		// set position
		element.setIdPos(inclusion.getNameOffset(), inclusion.getNameEndOffset() - inclusion.getNameOffset());
		element.setPos(inclusion.getStartingOffset(), inclusion.getEndingOffset() - inclusion.getStartingOffset());
		element.setLines( inclusion.getStartingLine(), inclusion.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	private Macro createMacro(Parent parent, IASTMacro macro) throws CModelException{
		// create element
		org.eclipse.cdt.internal.core.model.Macro element = new  Macro(parent, macro.getName());
		// add to parent
		parent.addChild(element);		
		// set position
		element.setIdPos(macro.getNameOffset(), macro.getNameEndOffset() - macro.getNameOffset());
		element.setPos(macro.getStartingOffset(), macro.getEndingOffset() - macro.getStartingOffset());
		element.setLines( macro.getStartingLine(), macro.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;
		
	}
	
	private Namespace createNamespace(Parent parent, IASTNamespaceDefinition nsDef) throws CModelException{
		// create element
		String type = "namespace"; //$NON-NLS-1$
		String nsName = (nsDef.getName() == null )  
						? ""  //$NON-NLS-1$
						: nsDef.getName().toString();
		Namespace element = new Namespace (parent, nsName );
		// add to parent
		parent.addChild(element);
		element.setIdPos(nsDef.getNameOffset(), 
		(nsName.length() == 0) ? type.length() : (nsDef.getNameEndOffset() - nsDef.getNameOffset()));
		element.setPos(nsDef.getStartingOffset(), nsDef.getEndingOffset() - nsDef.getStartingOffset());
		element.setLines( nsDef.getStartingLine(), nsDef.getEndingLine() );
		element.setTypeName(type);
		this.newElements.put(element, element.getElementInfo());		
		return element;
	}

	private Enumeration createEnumeration(Parent parent, IASTEnumerationSpecifier enumSpecifier) throws CModelException{
		// create element
		String type = "enum"; //$NON-NLS-1$
		String enumName = (enumSpecifier.getName() == null )
						  ? ""  //$NON-NLS-1$
						  : enumSpecifier.getName().toString();
		Enumeration element = new Enumeration (parent, enumName );
		// add to parent
		parent.addChild(element);
		Iterator i  = enumSpecifier.getEnumerators();
		while (i.hasNext()){
			// create sub element
			IASTEnumerator enumDef = (IASTEnumerator) i.next();
			createEnumerator(element, enumDef);
		}
		// set enumeration position
		element.setIdPos(enumSpecifier.getNameOffset(), 
		(enumName.length() == 0) ? type.length() : (enumSpecifier.getNameEndOffset() - enumSpecifier.getNameOffset() ));
		element.setPos(enumSpecifier.getStartingOffset(), enumSpecifier.getEndingOffset() - enumSpecifier.getStartingOffset());
		element.setLines( enumSpecifier.getStartingLine(), enumSpecifier.getEndingLine() );
		element.setTypeName(type);
		 
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	private Enumerator createEnumerator(Parent enumarator, IASTEnumerator enumDef) throws CModelException{
		Enumerator element = new Enumerator (enumarator, enumDef.getName().toString());
		IASTExpression initialValue = enumDef.getInitialValue();
		if(initialValue != null){
			element.setConstantExpression( ASTUtil.getExpressionString( initialValue ) );
		}
		// add to parent
		enumarator.addChild(element);
		// set enumerator position
		element.setIdPos(enumDef.getStartingOffset(), (enumDef.getNameEndOffset() - enumDef.getNameOffset()));
		element.setPos(enumDef.getStartingOffset(), enumDef.getEndingOffset() - enumDef.getStartingOffset());
		element.setLines( enumDef.getStartingLine(), enumDef.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;		
	}
	
	private Structure createClass(Parent parent, IASTClassSpecifier classSpecifier, boolean isTemplate) throws CModelException{
		// create element
		String className = ""; //$NON-NLS-1$
		String type = ""; //$NON-NLS-1$
		int kind = ICElement.C_CLASS;
		ASTClassKind classkind = classSpecifier.getClassKind();
		if(classkind == ASTClassKind.CLASS){
			if(!isTemplate)
				kind = ICElement.C_CLASS;
			else
				kind = ICElement.C_TEMPLATE_CLASS;
			type = "class"; //$NON-NLS-1$
			className = (classSpecifier.getName() == null )
						? "" //$NON-NLS-1$
						: classSpecifier.getName().toString();				
		}
		if(classkind == ASTClassKind.STRUCT){
			if(!isTemplate)
				kind = ICElement.C_STRUCT;
			else
				kind = ICElement.C_TEMPLATE_STRUCT;
			type = "struct"; //$NON-NLS-1$
			className = (classSpecifier.getName() == null ) 
						? ""  //$NON-NLS-1$
						: classSpecifier.getName().toString();				
		}
		if(classkind == ASTClassKind.UNION){
			if(!isTemplate)
				kind = ICElement.C_UNION;
			else
				kind = ICElement.C_TEMPLATE_UNION;
			type = "union"; //$NON-NLS-1$
			className = (classSpecifier.getName() == null )
						? ""  //$NON-NLS-1$
						: classSpecifier.getName().toString();				
		}
		
		Structure element;	
		Structure classElement = new Structure( parent, kind, className );
		element = classElement;
		
		// store super classes names
		Iterator baseClauses = classSpecifier.getBaseClauses();
		while (baseClauses.hasNext()){
			IASTBaseSpecifier baseSpec = (IASTBaseSpecifier)baseClauses.next();
			element.addSuperClass(baseSpec.getParentClassName(), baseSpec.getAccess()); 
		}

		// add to parent
		parent.addChild(element);
		// set element position 
		element.setIdPos( classSpecifier.getNameOffset(), 
		(className.length() == 0) ? type.length() : (classSpecifier.getNameEndOffset() - classSpecifier.getNameOffset() ));
		element.setTypeName( type );
		if(!isTemplate){
			// set the element position
			element.setPos(classSpecifier.getStartingOffset(), classSpecifier.getEndingOffset() - classSpecifier.getStartingOffset());
		}
		element.setLines( classSpecifier.getStartingLine(), classSpecifier.getEndingLine() );
		
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	private TypeDef createTypeDef(Parent parent, IASTTypedefDeclaration typeDefDeclaration) throws CModelException{
		// create the element
		String name = typeDefDeclaration.getName();
        
        TypeDef element = new TypeDef( parent, name );
        
        StringBuffer typeName = new StringBuffer(ASTUtil.getType(typeDefDeclaration.getAbstractDeclarator()));
		element.setTypeName(typeName.toString());
		
		// add to parent
		parent.addChild(element);

		// set positions
		element.setIdPos(typeDefDeclaration.getNameOffset(), (typeDefDeclaration.getNameEndOffset() - typeDefDeclaration.getNameOffset()));	
		element.setPos(typeDefDeclaration.getStartingOffset(), typeDefDeclaration.getEndingOffset() - typeDefDeclaration.getStartingOffset());
		element.setLines( typeDefDeclaration.getStartingLine(), typeDefDeclaration.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;	
	}

	private VariableDeclaration createVariableSpecification(Parent parent, IASTVariable varDeclaration, boolean isTemplate)throws CModelException, ASTNotImplementedException
    {
		String variableName = varDeclaration.getName(); 
		if((variableName == null) || (variableName.length() <= 0)){
			// something is wrong, skip this element
			return null;
		}
		
		IASTAbstractDeclaration abstractDeclaration = varDeclaration.getAbstractDeclaration();
    	CElement abstractElement = createAbstractElement (parent, abstractDeclaration , isTemplate, false);

		VariableDeclaration element = null;
		if(varDeclaration instanceof IASTField){
			IASTField fieldDeclaration = (IASTField) varDeclaration;
			// field
			Field newElement = new Field( parent, variableName);
			newElement.setMutable(fieldDeclaration.isMutable());
			newElement.setVisibility(fieldDeclaration.getVisiblity());
			element = newElement;			
		}
		else {
			if(varDeclaration.isExtern()){
				// variableDeclaration
				VariableDeclaration newElement = new VariableDeclaration( parent, variableName );
				element = newElement;
			}
			else {
				// variable
				Variable newElement = new Variable( parent, variableName );
				element = newElement;				
			}
		}
		element.setTypeName ( ASTUtil.getType(varDeclaration.getAbstractDeclaration()) );
		element.setConst(varDeclaration.getAbstractDeclaration().isConst());
		element.setVolatile(varDeclaration.getAbstractDeclaration().isVolatile());
		element.setStatic(varDeclaration.isStatic());
		// add to parent
		parent.addChild( element ); 	

		// set position
		element.setIdPos( varDeclaration.getNameOffset(), (varDeclaration.getNameEndOffset() - varDeclaration.getNameOffset()) );
		if(!isTemplate){
			// set element position
			element.setPos(varDeclaration.getStartingOffset(), varDeclaration.getEndingOffset() - varDeclaration.getStartingOffset());
		}
		element.setLines( varDeclaration.getStartingLine(), varDeclaration.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;
	}

	private FunctionDeclaration createFunctionSpecification(Parent parent, IASTFunction functionDeclaration, boolean isTemplate) throws CModelException
    {    	
		String name = functionDeclaration.getName();
        if ((name == null) || (name.length() <= 0)) {
            // Something is wrong, skip this element
            return null;             
        } 

		// get parameters types
		String[] parameterTypes = ASTUtil.getFunctionParameterTypes(functionDeclaration);
		
		FunctionDeclaration element = null;
		
		if( functionDeclaration instanceof IASTMethod )
		{
			IASTMethod methodDeclaration = (IASTMethod) functionDeclaration;
			MethodDeclaration methodElement = null;
			if (methodDeclaration.hasFunctionBody())
			{
				// method
				Method newElement = new Method( parent, name );
				methodElement = newElement;
			}
			else
			{
				// method declaration
				MethodDeclaration newElement = new MethodDeclaration( parent, name );
				methodElement = newElement;
			}
			methodElement.setParameterTypes(parameterTypes);
			methodElement.setReturnType( ASTUtil.getType(functionDeclaration.getReturnType()) );
			methodElement.setStatic(functionDeclaration.isStatic());

			// Common settings for method declaration
			methodElement.setVisibility(methodDeclaration.getVisiblity());
			methodElement.setVolatile(methodDeclaration.isVolatile());
			methodElement.setConst(methodDeclaration.isConst());
			methodElement.setVirtual(methodDeclaration.isVirtual());
			methodElement.setPureVirtual(methodDeclaration.isPureVirtual());
			methodElement.setInline(methodDeclaration.isInline());
			methodElement.setFriend(methodDeclaration.isFriend());
			methodElement.setConstructor(methodDeclaration.isConstructor());
			methodElement.setDestructor(methodDeclaration.isDestructor());
			element = methodElement;				
		}
		else // instance of IASTFunction 
		{
			FunctionDeclaration functionElement = null;
			if (functionDeclaration.hasFunctionBody())
			{				
				// function
				Function newElement = new Function( parent, name );
				functionElement = newElement;				

			}
			else
			{
				// functionDeclaration
				FunctionDeclaration newElement = new FunctionDeclaration( parent, name );
				functionElement = newElement;				
			}
			functionElement.setParameterTypes(parameterTypes);
			functionElement.setReturnType( ASTUtil.getType(functionDeclaration.getReturnType()) );
			functionElement.setStatic(functionDeclaration.isStatic());
			element = functionElement;
		}						
		// add to parent
		parent.addChild( element ); 	

		// hook up the offsets
		element.setIdPos( functionDeclaration.getNameOffset(), (functionDeclaration.getNameEndOffset() - functionDeclaration.getNameOffset()) );
		if(!isTemplate){
			// set the element position		
			element.setPos(functionDeclaration.getStartingOffset(), functionDeclaration.getEndingOffset() - functionDeclaration.getStartingOffset());	
		}
		element.setLines( functionDeclaration.getStartingLine(), functionDeclaration.getEndingLine() );

		this.newElements.put(element, element.getElementInfo());
		return element;
	}

	private Using createUsingDirective(Parent parent, IASTUsingDirective usingDirDeclaration) throws CModelException{
		// create the element
		String name = usingDirDeclaration.getNamespaceName();
        
        Using element = new Using( parent, name, true );
		
		// add to parent
		parent.addChild(element);

		// set positions
		element.setIdPos(usingDirDeclaration.getNameOffset(), (usingDirDeclaration.getNameEndOffset() - usingDirDeclaration.getNameOffset()));	
		element.setPos(usingDirDeclaration.getStartingOffset(), usingDirDeclaration.getEndingOffset() - usingDirDeclaration.getStartingOffset());
		element.setLines(usingDirDeclaration.getStartingLine(), usingDirDeclaration.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;	
	}

	private Using createUsingDeclaration(Parent parent, IASTUsingDeclaration usingDeclaration) throws CModelException{
		// create the element
		String name = usingDeclaration.usingTypeName();
        
        Using element = new Using(parent, name, false);
		
		// add to parent
		parent.addChild(element);

		// set positions
		element.setIdPos(usingDeclaration.getNameOffset(), (usingDeclaration.getNameEndOffset() - usingDeclaration.getNameOffset()));	
		element.setPos(usingDeclaration.getStartingOffset(), usingDeclaration.getEndingOffset() - usingDeclaration.getStartingOffset());
		element.setLines(usingDeclaration.getStartingLine(), usingDeclaration.getEndingLine() );
		this.newElements.put(element, element.getElementInfo());
		return element;	
	}

	/**
	 * @return Returns the newElements.
	 */
	public Map getNewElements() {
		return newElements;
	}
}
