/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.fdt.internal.core.search.matching;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.fdt.core.CommonLanguageCore;
import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.core.model.CoreModel;
import org.eclipse.fdt.core.model.IWorkingCopy;
import org.eclipse.fdt.core.parser.CodeReader;
import org.eclipse.fdt.core.parser.IParser;
import org.eclipse.fdt.core.parser.IProblem;
import org.eclipse.fdt.core.parser.IScanner;
import org.eclipse.fdt.core.parser.IScannerInfo;
import org.eclipse.fdt.core.parser.IScannerInfoProvider;
import org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.fdt.core.parser.ParserFactory;
import org.eclipse.fdt.core.parser.ParserFactoryError;
import org.eclipse.fdt.core.parser.ParserLanguage;
import org.eclipse.fdt.core.parser.ParserMode;
import org.eclipse.fdt.core.parser.ParserUtil;
import org.eclipse.fdt.core.parser.ScannerInfo;
import org.eclipse.fdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.fdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTClassReference;
import org.eclipse.fdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTCodeScope;
import org.eclipse.fdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTEnumerator;
import org.eclipse.fdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.fdt.core.parser.ast.IASTField;
import org.eclipse.fdt.core.parser.ast.IASTFieldReference;
import org.eclipse.fdt.core.parser.ast.IASTFunction;
import org.eclipse.fdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.fdt.core.parser.ast.IASTInclusion;
import org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.fdt.core.parser.ast.IASTMacro;
import org.eclipse.fdt.core.parser.ast.IASTMethod;
import org.eclipse.fdt.core.parser.ast.IASTMethodReference;
import org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.fdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.fdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTParameterReference;
import org.eclipse.fdt.core.parser.ast.IASTReference;
import org.eclipse.fdt.core.parser.ast.IASTScope;
import org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.fdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.fdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.fdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.fdt.core.parser.ast.IASTVariable;
import org.eclipse.fdt.core.parser.ast.IASTVariableReference;
import org.eclipse.fdt.core.search.ICSearchPattern;
import org.eclipse.fdt.core.search.ICSearchResultCollector;
import org.eclipse.fdt.core.search.ICSearchScope;
import org.eclipse.fdt.core.search.IMatch;
import org.eclipse.fdt.core.search.IMatchLocator;
import org.eclipse.fdt.internal.core.search.AcceptMatchOperation;
import org.eclipse.fdt.internal.core.search.indexing.IndexProblemHandler;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MatchLocator implements IMatchLocator{

	
	ArrayList matchStorage;
	
	public static boolean VERBOSE = false;
	/**
	 * 
	 */
	public MatchLocator( ICSearchPattern pattern, ICSearchResultCollector collector, ICSearchScope scope) {
		super();
		searchPattern = pattern;
		resultCollector = collector;
		searchScope = scope;	
	}

	public boolean acceptProblem(IProblem problem) 								{ return IndexProblemHandler.ruleOnProblem(problem, ParserMode.COMPLETE_PARSE );	}
	public void acceptUsingDirective(IASTUsingDirective usageDirective) 		{	}
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) 	{	}
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) 			{	}
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {}

	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) 	{	}
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {	}

	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) 	{}
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) 		{	}
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) 	{	}
	
	public void enterCodeBlock(IASTCodeScope scope) {	}
	public void exitCodeBlock(IASTCodeScope scope) 	{	}
	
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec){
		pushScope( linkageSpec );	
	}

	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec){
		popScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.fdt.internal.core.parser.ast.complete.ASTParameterReference)
	 */
	public void acceptParameterReference(IASTParameterReference reference)
	{	
		check( REFERENCES, reference );        
	}
	

	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) 
	{
		check( REFERENCES, reference );	
	}
	
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef){
		lastDeclaration = typedef;
		check( DECLARATIONS, typedef );
	}
	
	public void acceptTypedefReference( IASTTypedefReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptEnumeratorReference(IASTEnumeratorReference reference){
			check( REFERENCES, reference );	
	}
		
	public void acceptMacro(IASTMacro macro){
		check( DECLARATIONS, macro );	
	}
	
	public void acceptVariable(IASTVariable variable){
		lastDeclaration = variable;
		
		check( DECLARATIONS, variable );	
		//A declaration is a definition unless...:
		//it contains the extern specifier or a linkage-spec and no initializer
		if( variable.getInitializerClause() != null ||
		    ( !variable.isExtern() && !(currentScope instanceof IASTLinkageSpecification) ) ){
			check( DEFINITIONS, variable );
		}
	}
	
	public void acceptField(IASTField field){
		lastDeclaration = field;
		if( currentScope instanceof IASTClassSpecifier ){
			check( DECLARATIONS, field ); 	   
			if( !field.isStatic() ){
				check( DEFINITIONS, field ); 
			}
		} else {
			check( DEFINITIONS, field );
		}
	}
	
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration){
		lastDeclaration = enumeration; 
		check( DECLARATIONS, enumeration );
		Iterator iter = enumeration.getEnumerators();
		while( iter.hasNext() ){
			IASTEnumerator enumerator = (IASTEnumerator) iter.next();
			lastDeclaration = enumerator;
			check ( DECLARATIONS, enumerator );
		}  
	}
		
	public void acceptFunctionDeclaration(IASTFunction function){
		lastDeclaration = function;
		check( DECLARATIONS, function );
	}
	
	public void acceptMethodDeclaration(IASTMethod method){
		lastDeclaration = method;
		check( DECLARATIONS, method );
	}
		
	public void acceptClassReference(IASTClassReference reference) {
		check( REFERENCES, reference );
	}
	
	public void acceptNamespaceReference( IASTNamespaceReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptVariableReference( IASTVariableReference reference ){
		check( REFERENCES, reference );		
	}
	
	public void acceptFieldReference( IASTFieldReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptEnumerationReference( IASTEnumerationReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptFunctionReference( IASTFunctionReference reference ){
		check( REFERENCES, reference );
	}
	
	public void acceptMethodReference( IASTMethodReference reference ){
		check( REFERENCES, reference );	
	}
	
	public void enterFunctionBody(IASTFunction function){
		lastDeclaration = function;
		
		if( !function.previouslyDeclared() )
			check( DECLARATIONS, function );
			
		check( DEFINITIONS, function );
		
		Iterator parms =function.getParameters();
		while (parms.hasNext()){
			Object tempParm = parms.next();
			if (tempParm instanceof IASTParameterDeclaration){
				check( DECLARATIONS, ((IASTParameterDeclaration)tempParm));
			}
		}
		pushScope( function );
	}
	
	public void enterMethodBody(IASTMethod method) {
		lastDeclaration = method;
		if( !method.previouslyDeclared() )
			check( DECLARATIONS, method );
			
		check( DEFINITIONS, method );
		
		
		Iterator parms =method.getParameters();
		while (parms.hasNext()){
			Object tempParm = parms.next();
			if (tempParm instanceof IASTParameterDeclaration){
				check( DECLARATIONS, ((IASTParameterDeclaration)tempParm));
			}
		}
		pushScope( method );
	}
	
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		pushScope( compilationUnit );
	}
	
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		lastDeclaration = namespaceDefinition;
		check( DECLARATIONS, namespaceDefinition );
		check( DEFINITIONS, namespaceDefinition );
		pushScope( namespaceDefinition );			
	}

	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		lastDeclaration = classSpecification;
		check( DECLARATIONS, classSpecification );
		pushScope( classSpecification );		
	}
	
	public void exitFunctionBody(IASTFunction function) {
		popScope();	
	}

	public void exitMethodBody(IASTMethod method) {
		popScope();	
	}

	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		check(DECLARATIONS, classSpecification);
		popScope();
	}

	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		popScope();
	}

	public void exitCompilationUnit(IASTCompilationUnit compilationUnit){
		popScope();
	}
	
	public void enterInclusion(IASTInclusion inclusion) {
		String includePath = inclusion.getFullFileName();

		IPath path = new Path( includePath );
		IResource resource = null;
	
		if( workspaceRoot != null ){
			resource = workspaceRoot.getFileForLocation( path );
//			if( resource == null ){
//				//TODO:What to do if the file is not in the workspace?				
			//				IFile file = currentResource.getProject().getFile(
			// inclusion.getName() );
//				try{
//					file.createLink( path, 0, null );
//				} catch ( CoreException e ){
//					file = null;
//				}
//				resource = file;
//			}
		}
		
		resourceStack.addFirst( ( currentResource != null ) ? (Object)currentResource : (Object)currentPath );
		
		currentResource = resource;
		currentPath = ( resource == null ) ? path : null;
	}

	public void exitInclusion(IASTInclusion inclusion) {
		Object obj = resourceStack.removeFirst();
		if( obj instanceof IResource ){
			currentResource = (IResource)obj;
			currentPath = null;
		} else {
			currentPath = (IPath) obj;
			currentResource = null;
		}
	}
		
   
	public void locateMatches( String [] paths, IWorkspace workspace, IWorkingCopy[] workingCopies ) throws InterruptedException{
		
		matchStorage = new ArrayList();
		workspaceRoot = (workspace != null) ? workspace.getRoot() : null;
		
		HashMap wcPaths = new HashMap();
		int wcLength = (workingCopies == null) ? 0 : workingCopies.length;
		if( wcLength > 0 ){
			String [] newPaths = new String[ wcLength ];
			
			for( int i = 0; i < wcLength; i++ ){
				IWorkingCopy workingCopy = workingCopies[ i ];
				String path = workingCopy.getOriginalElement().getPath().toString();	
				wcPaths.put( path, workingCopy );
				newPaths[ i ] = path;
			}
			
			int len = paths.length;
			String [] tempArray = new String[ len + wcLength ];
			System.arraycopy( paths, 0, tempArray, 0, len );
			System.arraycopy( newPaths, 0, tempArray, len, wcLength );
			paths = tempArray;
		}
		
		Arrays.sort( paths );
		
		int length = paths.length;
		if( progressMonitor != null ){
			progressMonitor.beginTask( "", length ); //$NON-NLS-1$
		}
		
		for( int i = 0; i < length; i++ ){
			if( progressMonitor != null ) {
				if( progressMonitor.isCanceled() ){
					throw new InterruptedException();
				} else {
					progressMonitor.worked( 1 );
				}
			}
			
			String pathString = paths[ i ];
			
			//skip duplicates
			if( i > 0 && pathString.equals( paths[ i - 1 ] ) ) continue;
			
			if  (!searchScope.encloses(pathString)) continue;
			
			CodeReader reader = null;
			
			realPath = null; 
			IProject project = null;
			
			if( workspaceRoot != null ){
				IWorkingCopy workingCopy = (IWorkingCopy)wcPaths.get( pathString );
				
				if( workingCopy != null ){
					currentResource = workingCopy.getResource();
					if ( currentResource != null && currentResource.isAccessible() ) {
						reader = new CodeReader(currentResource.getLocation().toOSString(), workingCopy.getContents()); 
						realPath = currentResource.getLocation();
						project = currentResource.getProject();
					} else {
						continue;
					}
				} else {
					currentResource = workspaceRoot.findMember( pathString, true );

					InputStream contents = null;
					try{
						if( currentResource != null ){
							if (currentResource.isAccessible() && currentResource instanceof IFile) {
								IFile file = (IFile) currentResource;
								contents = file.getContents();
								reader = new CodeReader(currentResource.getLocation().toOSString(), file.getCharset(), contents);
								realPath = currentResource.getLocation();
								project = file.getProject();
							} else {
								continue;
							}
						}
					} catch ( CoreException e ){
						continue;
					} catch ( IOException e ) {
						continue;
					} finally {
						if (contents != null) {
							try {
								contents.close();
							} catch (IOException io) {
								// ignore.
							}
						}
					}
				}
			}
			if( currentResource == null ) {
				try {
					IPath path = new Path( pathString );
					currentPath = path;
					reader = new CodeReader(pathString);
					realPath = currentPath; 
				} catch (IOException e) {
					continue;
				}
			}
			
			//Get the scanner info
			IScannerInfo scanInfo = new ScannerInfo();
			IScannerInfoProvider provider = FortranCorePlugin.getDefault().getScannerInfoProvider(project);
			if (provider != null){
				IScannerInfo buildScanInfo = provider.getScannerInformation(currentResource != null ? currentResource : project);
				if( buildScanInfo != null )
					scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
			}
			
			ParserLanguage language = null;
			if( project != null ){
				language = ParserLanguage.C;
			} else {
				//TODO no project, what language do we use?
				language = ParserLanguage.CPP;
			}
			
			IParser parser = null;
			try
			{
				IScanner scanner = ParserFactory.createScanner( reader, scanInfo, ParserMode.COMPLETE_PARSE, language, this, ParserUtil.getScannerLogService(), null );
				parser  = ParserFactory.createParser( scanner, this, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
			}
			catch( ParserFactoryError pfe )
			{
				
			}
			
			if (VERBOSE)
			  MatchLocator.verbose("*** New Search for path: " + pathString); //$NON-NLS-1$
			  
			
			try{ 
				parser.parse();
			}
			catch(Exception ex){
				if (VERBOSE){
					ex.printStackTrace();
				}
			}
			catch(VirtualMachineError vmErr){
				if (VERBOSE){
					MatchLocator.verbose("MatchLocator VM Error: "); //$NON-NLS-1$
					vmErr.printStackTrace();
				}
			} finally { 
				scopeStack.clear();
				resourceStack.clear();
				lastDeclaration = null;
				currentScope = null;
				parser = null;
			}
			
			if( matchStorage.size() > 0 ){
				AcceptMatchOperation acceptMatchOp = new AcceptMatchOperation( resultCollector, matchStorage );
				try {
					CommonLanguageCore.getWorkspace().run(acceptMatchOp,null);
				} catch (CoreException e) {}
				
				matchStorage.clear();
			}
		}
	}
	
	protected void report( ISourceElementCallbackDelegate node, int accuracyLevel ){
		try {
			if( currentResource != null && !searchScope.encloses(currentResource.getFullPath().toOSString() ) ){
				return;
			}
			
			int offset = 0;
			int end = 0;
			
			if( node instanceof IASTReference ){
				IASTReference reference = (IASTReference) node;
				offset = reference.getOffset();
				end = offset + reference.getName().length();
				if (VERBOSE)
					MatchLocator.verbose("Report Match: " + reference.getName()); //$NON-NLS-1$
			} else if( node instanceof IASTOffsetableNamedElement ){
				IASTOffsetableNamedElement offsetableElement = (IASTOffsetableNamedElement) node;
				offset = offsetableElement.getNameOffset() != 0 ? offsetableElement.getNameOffset() 
															    : offsetableElement.getStartingOffset();
				end = offsetableElement.getNameEndOffset();
				if( end == 0 ){
					end = offset + offsetableElement.getName().length();
				}
																						  
				if (VERBOSE)
					MatchLocator.verbose("Report Match: " + offsetableElement.getName()); //$NON-NLS-1$
			}
		
			IMatch match = null;
			ISourceElementCallbackDelegate object = null;
			
			if( node instanceof IASTReference ){
				if( currentScope instanceof IASTFunction || currentScope instanceof IASTMethod ){
					object = (ISourceElementCallbackDelegate) currentScope;
				} else {
					object = lastDeclaration;
				}
			} else {
				if( currentScope instanceof IASTFunction || currentScope instanceof IASTMethod ){
					//local declaration, only report if not being filtered
					if( shouldExcludeLocalDeclarations ){
						return;
					}
					
					object = (ISourceElementCallbackDelegate) currentScope;
				} else {
					object = node;
				}
			}
			
			if( currentResource != null ){
				match = resultCollector.createMatch( currentResource, offset, end, object, null );
			} else if( currentPath != null ){
				match = resultCollector.createMatch( currentPath, offset, end, object, realPath );
			}
			if( match != null ){
				//Save till later
				//resultCollector.acceptMatch( match );
				matchStorage.add(match);
			}
		
		} catch (CoreException e) {
		}
	}

	private void check( LimitTo limit, ISourceElementCallbackDelegate node ){
		if( !searchPattern.canAccept( limit ) )
			return;
			
		int level = ICSearchPattern.IMPOSSIBLE_MATCH;
		
		if( node instanceof IASTReference ){
			level = searchPattern.matchLevel( ((IASTReference)node).getReferencedElement(), limit );
		} else  {
			level = searchPattern.matchLevel(  node, limit );
		} 
		
		if( level != ICSearchPattern.IMPOSSIBLE_MATCH )
		{
			report( node, level );
		} 
	}
	
	private void pushScope( IASTScope scope ){
		scopeStack.addFirst( currentScope );
		currentScope = scope;
	}
	
	private IASTScope popScope(){
		IASTScope oldScope = currentScope;
		currentScope = (scopeStack.size() > 0 ) ? (IASTScope) scopeStack.removeFirst() : null;
		return oldScope;
	}
	
	public void setShouldExcludeLocalDeclarations( boolean exclude ){
		shouldExcludeLocalDeclarations = exclude;
	}
	
	private boolean shouldExcludeLocalDeclarations = false;
	
	private ISourceElementCallbackDelegate lastDeclaration;
	
	private ICSearchPattern 		searchPattern;
	private ICSearchResultCollector resultCollector;
	private IProgressMonitor 		progressMonitor;
	private IPath					currentPath 	= null;
	private ICSearchScope 			searchScope;
	private IWorkspaceRoot 			workspaceRoot;
	private IPath 					realPath; 
	
	private IResource 				currentResource = null;
	private LinkedList 				resourceStack = new LinkedList();
	
	private IASTScope				currentScope = null;
	private LinkedList				scopeStack = new LinkedList();

	/* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType){
		check( DECLARATIONS, elaboratedType );	
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.fdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}

	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log);  //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath,workingCopies);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.search.IMatchLocator#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
}
