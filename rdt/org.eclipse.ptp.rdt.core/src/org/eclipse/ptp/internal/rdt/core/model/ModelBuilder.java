/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
/*
 * This class is loosely based on CModelBuilder2 
 */

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.core/model
 * Class: org.eclipse.cdt.internal.core.model.CModelBuilder2
 * Version: 1.52
 */

package org.eclipse.ptp.internal.rdt.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IStructureDeclaration;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.Enumeration;
import org.eclipse.cdt.internal.core.model.Enumerator;
import org.eclipse.cdt.internal.core.model.Field;
import org.eclipse.cdt.internal.core.model.Function;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;
import org.eclipse.cdt.internal.core.model.FunctionTemplate;
import org.eclipse.cdt.internal.core.model.FunctionTemplateDeclaration;
import org.eclipse.cdt.internal.core.model.Include;
import org.eclipse.cdt.internal.core.model.Macro;
import org.eclipse.cdt.internal.core.model.Method;
import org.eclipse.cdt.internal.core.model.MethodDeclaration;
import org.eclipse.cdt.internal.core.model.MethodTemplate;
import org.eclipse.cdt.internal.core.model.MethodTemplateDeclaration;
import org.eclipse.cdt.internal.core.model.Namespace;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.cdt.internal.core.model.SourceManipulation;
import org.eclipse.cdt.internal.core.model.Structure;
import org.eclipse.cdt.internal.core.model.StructureDeclaration;
import org.eclipse.cdt.internal.core.model.StructureTemplate;
import org.eclipse.cdt.internal.core.model.StructureTemplateDeclaration;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.core.model.TypeDef;
import org.eclipse.cdt.internal.core.model.Using;
import org.eclipse.cdt.internal.core.model.Variable;
import org.eclipse.cdt.internal.core.model.VariableDeclaration;
import org.eclipse.cdt.internal.core.model.VariableTemplate;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Create a local CModel using a remote CModel
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 *
 * @author vivkong
 *
 */

public class ModelBuilder {		
	private final IProgressMonitor fProgressMonitor;
	private final TranslationUnit fTranslationUnit;
	private HashMap<ISourceReference, int[]> fEqualElements;
	
	/**
	 * Create a model builder for the given translation unit.
	 * 
	 * @param tu  the translation unit
	 * @param monitor the progress monitor
	 */
	public ModelBuilder(TranslationUnit tu, IProgressMonitor monitor) {
		fTranslationUnit= (TranslationUnit)tu;
		fProgressMonitor = monitor;
	}

	
	/**
	 * Build the local CModel using information from remote CModel
	 * @param remoteTU
	 * @throws CModelException 
	 */
	public void buildLocalModel(org.eclipse.ptp.internal.rdt.core.model.TranslationUnit remoteTU) throws CModelException {
		if (isCanceled() || remoteTU == null) {
			return;
		}

		fEqualElements= new HashMap<ISourceReference, int[]>();
				
		// includes
		final IInclude[] includeDirectives = remoteTU.getIncludes();
		for (IInclude includeDirective : includeDirectives) {
			createInclusion(fTranslationUnit, includeDirective);
		}		
		// macros
		final List<ICElement> macros = remoteTU.getChildrenOfType(CElement.C_MACRO);
//		Iterator<ICElement> iterator = macros.iterator();
		for (ICElement macro : macros) {
//		while (iterator.hasNext()) {
			createMacro(fTranslationUnit, (IMacro)macro);
		}
		
		//everything else
		ICElement[] remoteChildren = remoteTU.getChildren();
		for (ICElement child : remoteChildren) {
			
			switch (child.getElementType()) {
				case ICElement.C_INCLUDE:
				case ICElement.C_MACRO:
					break;
				default:
					createElement(fTranslationUnit, child);
			}
		}
				
		fEqualElements.clear();

		if (isCanceled()) {
			return;
		}
	}

	private void createElement(Parent parent, ICElement remoteChild) throws CModelException {
		switch (remoteChild.getElementType()) {
			case ICElement.C_TEMPLATE_STRUCT:
			case ICElement.C_STRUCT:
			case ICElement.C_TEMPLATE_UNION:
			case ICElement.C_UNION:
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_CLASS:
				createStructure(parent, (IStructure)remoteChild);				
				break;
				
			case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			case ICElement.C_STRUCT_DECLARATION:
			case ICElement.C_TEMPLATE_UNION_DECLARATION:
			case ICElement.C_UNION_DECLARATION:
			case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			case ICElement.C_CLASS_DECLARATION:
				createStructureDeclaration(parent, (IStructureDeclaration)remoteChild);
				break;
				
			case ICElement.C_ENUMERATION:
				createEnumeration(parent, (IEnumeration)remoteChild);
				break;
			
			case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			case ICElement.C_FUNCTION_DECLARATION:
				createFunctionDeclaration(parent, (IFunctionDeclaration)remoteChild);
				break;
			
			case ICElement.C_NAMESPACE:
				createNamespace(parent, (INamespace)remoteChild);
				break;
				
			case ICElement.C_FUNCTION:
			case ICElement.C_METHOD:			
			case ICElement.C_TEMPLATE_FUNCTION:			
			case ICElement.C_TEMPLATE_METHOD:
				createFunctionDefinition(parent, (IFunctionDeclaration) remoteChild);
				break;
			
			case ICElement.C_TYPEDEF:
				createTypeDef(parent, (ITypeDef)remoteChild);
				break;
			
			case ICElement.C_FIELD:
			case ICElement.C_TEMPLATE_VARIABLE:
			case ICElement.C_VARIABLE:
			case ICElement.C_VARIABLE_DECLARATION:
				createVariable(parent, (IVariableDeclaration) remoteChild);
				break;
				
			case ICElement.C_USING:
				createUsing(parent, (IUsing)remoteChild);
				break;
				
			case ICElement.C_UNKNOWN_DECLARATION:
			case ICElement.C_VARIABLE_LOCAL:
				System.out.println("local model builder: missed model element"); //$NON-NLS-1$
				break;
		}		
	}

	private Using createUsing(Parent parent, IUsing remoteChild) throws CModelException {

		// create the element		
        Using element= new Using(parent, remoteChild.getElementName(), remoteChild.isDirective());
		setIndex(element);
		element.setActive(remoteChild.isActive());

		// add to parent
		parent.addChild(element);

		// set positions
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;

			setBodyPosition(element, remoteElement);
			setIdentifierPosition(element, remoteElement);
		}
		return element;
	}

	private FunctionDeclaration createFunctionDefinition(Parent parent, IFunctionDeclaration remoteChild) throws CModelException {

		boolean isTemplate = false;
		final String simpleName= remoteChild.getElementName();
		final String[] parameterTypes= remoteChild.getParameterTypes();
		final String returnType= remoteChild.getReturnType();

		final FunctionDeclaration element;
		
		if (remoteChild.getElementType() == ICElement.C_TEMPLATE_METHOD || remoteChild.getElementType() == ICElement.C_METHOD) {
			// method
			final MethodDeclaration methodElement;
			final String methodName = simpleName;
			
			if (remoteChild.getElementType() == ICElement.C_TEMPLATE_METHOD) {
				methodElement= new MethodTemplate(parent, methodName);
				isTemplate = true;
			} else {
				methodElement= new Method(parent, methodName);
			}
			element= methodElement;
			// establish identity attributes before getElementInfo()
			methodElement.setParameterTypes(parameterTypes);
			methodElement.setReturnType(returnType);
			methodElement.setConst(remoteChild.isConst());
			setIndex(element);

			final IMethod remoteMethod = (IMethod) remoteChild;

			methodElement.setVirtual(remoteMethod.isVirtual());
			methodElement.setInline(remoteMethod.isInline());
			methodElement.setFriend(remoteMethod.isFriend());
			methodElement.setVolatile(remoteMethod.isVolatile());
			methodElement.setVisibility(remoteMethod.getVisibility());
			methodElement.setPureVirtual(remoteMethod.isPureVirtual());
			methodElement.setConstructor(remoteMethod.isConstructor());
			methodElement.setDestructor(remoteMethod.isDestructor());
		} else {
			String functionName= simpleName;
			
			if (remoteChild.getElementType() == ICElement.C_TEMPLATE_FUNCTION) {
				// template function
				element= new FunctionTemplate(parent, functionName);
			} else {
				// function
				element= new Function(parent, functionName);
			}
			element.setParameterTypes(parameterTypes);
			element.setReturnType(returnType);
			setIndex(element);
			
			element.setConst(remoteChild.isConst());
		}
		element.setActive(remoteChild.isActive());

		element.setStatic(remoteChild.isStatic());

		// add to parent
		parent.addChild(element);

		// set positions
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			if (!isTemplate)
				setBodyPosition(element, remoteElement);
			setIdentifierPosition(element, remoteElement);
		}
		return element;
		
	}

	private VariableDeclaration createVariable(Parent parent, IVariableDeclaration remoteChild) throws CModelException {

		final String variableName= remoteChild.getElementName();
		boolean isTemplate = false;
		final VariableDeclaration element;
		
		if (remoteChild.getElementType() == ICElement.C_FIELD) {
			final IField remoteField = (IField) remoteChild;
			// field
			Field newElement= new Field(parent, variableName);
			setIndex(newElement);
			newElement.setMutable(remoteField.isMutable());
			
			newElement.setTypeName(remoteField.getTypeName());
			newElement.setVisibility(remoteField.getVisibility());
			newElement.setConst(remoteField.isConst());
			newElement.setVolatile(remoteField.isVolatile());
			element= newElement;
		} else {
			switch(remoteChild.getElementType()) {
				case ICElement.C_TEMPLATE_VARIABLE:
					isTemplate = true;
					VariableTemplate newElement= new VariableTemplate(parent, variableName);
					element= newElement;
					break;
				case ICElement.C_VARIABLE:
					Variable newElement1= new Variable(parent, variableName);
					element= newElement1;
					break;
				default:
					VariableDeclaration newElement2= new VariableDeclaration(parent, variableName);
					element= newElement2;
					break;
			}
			setIndex(element);
		
			element.setTypeName(remoteChild.getTypeName());
			element.setConst(remoteChild.isConst());
			element.setVolatile(remoteChild.isVolatile());
		}
		
		element.setActive(remoteChild.isActive());
		element.setStatic(remoteChild.isStatic());
		
		// add to parent
		parent.addChild(element);

		// set positions
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			if (!isTemplate)
				setBodyPosition(element, remoteElement);
			setIdentifierPosition(element, remoteElement);
		}
		
		return element;
	}

	private FunctionDeclaration createFunctionDeclaration(Parent parent, IFunctionDeclaration remoteChild) throws CModelException {
	
		boolean isTemplate = false;
		final String functionName= remoteChild.getElementName();
		final String[] parameterTypes= remoteChild.getParameterTypes();
		final String returnType= remoteChild.getReturnType();

		final FunctionDeclaration element;
				
		if (remoteChild.getElementType() == ICElement.C_TEMPLATE_METHOD_DECLARATION || remoteChild.getElementType() == ICElement.C_METHOD_DECLARATION) {
			final MethodDeclaration methodElement;
			if (remoteChild.getElementType() == ICElement.C_TEMPLATE_METHOD_DECLARATION) {
				methodElement = new MethodTemplateDeclaration(parent, functionName);
				isTemplate = true;
			} else {
				methodElement= new MethodDeclaration(parent, functionName);
			}
			element= methodElement;
			// establish identity attributes before getElementInfo()
			
			final IMethodDeclaration remoteMethodDecl = (IMethodDeclaration)remoteChild;
			
			methodElement.setParameterTypes(parameterTypes);
			methodElement.setReturnType(returnType);
			methodElement.setConst(remoteChild.isConst());
			setIndex(element);
			
			methodElement.setVirtual(remoteMethodDecl.isVirtual());
			methodElement.setInline(remoteMethodDecl.isInline());
			methodElement.setFriend(remoteMethodDecl.isFriend());
			methodElement.setVolatile(remoteMethodDecl.isVolatile());
			methodElement.setVisibility(remoteMethodDecl.getVisibility());
			methodElement.setPureVirtual(remoteMethodDecl.isPureVirtual());
			methodElement.setConstructor(remoteMethodDecl.isConstructor());
			methodElement.setDestructor(remoteMethodDecl.isDestructor());
		}
		
		else { //if (remoteChild.getElementType() == ICElement.C_TEMPLATE_FUNCTION_DECLARATION || remoteChild.getElementType() == ICElement.C_FUNCTION_DECLARATION) {
			if (remoteChild.getElementType() == ICElement.C_TEMPLATE_FUNCTION_DECLARATION) {
				element = new FunctionTemplateDeclaration(parent, functionName);
				isTemplate = true;
			} else {
				element = new FunctionDeclaration(parent, functionName);
			}
			element.setParameterTypes(parameterTypes);
			element.setReturnType(returnType);
			setIndex(element);
			
			element.setConst(remoteChild.isConst());
		}

		
		element.setActive(remoteChild.isActive());		
		element.setStatic(remoteChild.isStatic());

		// add to parent
		parent.addChild(element);

		// hook up the offsets
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			if (!isTemplate)
				setBodyPosition(element, remoteElement);
			setIdentifierPosition(element, remoteElement);
		}

		return element;
		
	}

	private TypeDef createTypeDef(Parent parent, ITypeDef remoteChild) throws CModelException {
		
        final TypeDef element= new TypeDef(parent, remoteChild.getElementName());
		setIndex(element);
		element.setActive(remoteChild.isActive());

        element.setTypeName(remoteChild.getTypeName());

		// add to parent
		parent.addChild(element);

		// set positions
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			setBodyPosition(element, remoteElement);
			setIdentifierPosition(element, remoteElement);
		}
		
		return element;
	}

	private void createNamespace(Parent parent, INamespace remoteChild) throws CModelException {
		// create element
		final String type= Keywords.NAMESPACE;
		final String nsName= remoteChild.getElementName();
		final Namespace element= new Namespace(parent, nsName);
		setIndex(element);
		element.setActive(remoteChild.isActive());

		// add to parent
		parent.addChild(element);
		
		// set positions		
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			setBodyPosition(element, remoteElement);
			if (nsName.length() > 0)
				setIdentifierPosition(element, remoteElement);
			else
				element.setIdPos(remoteElement.getIdStartPos(), remoteElement.getIdLength());
		}
		
		element.setTypeName(type);
		
		ICElement[] grandchildren = remoteChild.getChildren();
		for (ICElement grandchild : grandchildren){
			createElement(element, grandchild);
		}		
	}

	private Enumeration createEnumeration(Parent parent, IEnumeration remoteChild) throws CModelException {
		// create element
		final String type= Keywords.ENUM;
		final String enumName= remoteChild.getElementName();
		final Enumeration element= new Enumeration (parent, enumName);
		setIndex(element);
		element.setActive(remoteChild.isActive());
		
		// add to parent
		parent.addChild(element);
		
		List<ICElement> grandchildren = remoteChild.getChildrenOfType(ICElement.C_ENUMERATOR);
		Iterator<ICElement> iterator = grandchildren.iterator();
		while (iterator.hasNext()) {
			createEnumerator(element, (IEnumerator)iterator.next());
		}
		
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			setBodyPosition(element, remoteElement);
			if (enumName.length() > 0)
				setIdentifierPosition(element, remoteElement);
			else {
				element.setIdPos(remoteElement.getIdStartPos(), remoteElement.getIdLength());
			}
		}
		element.setTypeName(type);
		return element;
		
	}

	private Enumerator createEnumerator(Parent parent, IEnumerator remoteChild) throws CModelException {
		final Enumerator element= new Enumerator (parent, remoteChild.getElementName());
		setIndex(element);
		element.setActive(remoteChild.isActive());
		element.setConstantExpression(remoteChild.getConstantExpression());

		// add to parent
		parent.addChild(element);
		
		// set positions		
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			setBodyPosition(element, remoteElement);
			setIdentifierPosition(element, remoteElement);
		}
		return element;
	}

	private Structure createStructure(Parent parent, IStructure remoteChild) throws CModelException {
		final String type = remoteChild.getTypeName();
		boolean isTemplate = false;
		
		switch (remoteChild.getElementType()) {
			case ICElement.C_TEMPLATE_STRUCT:
			case ICElement.C_TEMPLATE_UNION:
			case ICElement.C_TEMPLATE_CLASS:
				isTemplate = true;
				break;
		}
		
		final String className = remoteChild.getElementName();
		final Structure element;
		
		if (!isTemplate) {					
			Structure classElement = new Structure(parent, remoteChild.getElementType(), className);
			element = classElement;
		} else {
			StructureTemplate classTemplate = new StructureTemplate(parent, remoteChild.getElementType(), className);
			element = classTemplate;
		}		
		
		setIndex(element);
				
		element.setActive(remoteChild.isActive());
		String[] superClasses = remoteChild.getSuperClassesNames();
		for (String superClass : superClasses) {
			element.addSuperClass(superClass, remoteChild.getSuperClassAccess(superClass));
		}
		
		element.setTypeName(type);
		
		parent.addChild(element);
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			if (!isTemplate)
				setBodyPosition(element, remoteElement);
			if (className.length() > 0) {
				setIdentifierPosition(element, remoteElement);
			} else {
				element.setIdPos(remoteElement.getIdStartPos(), remoteElement.getIdLength());
			}
			
		}
		
		// add members
		final ICElement[] grandchildren= remoteChild.getChildren();
		for (ICElement grandchild : grandchildren) {
			createElement(element, grandchild);
		}

		return element;
	}
	
	private StructureDeclaration createStructureDeclaration(Parent parent, IStructureDeclaration remoteChild) throws CModelException {
		final String type = remoteChild.getTypeName();
		boolean isTemplate = false;;
		
		switch (remoteChild.getElementType()) {
			case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			case ICElement.C_TEMPLATE_UNION_DECLARATION:
			case ICElement.C_TEMPLATE_CLASS_DECLARATION:
				isTemplate = true;
				break;
		}
		
		final String className = remoteChild.getElementName();
		final StructureDeclaration element;
		
		if (!isTemplate) {					
			element = new StructureDeclaration(parent, className, remoteChild.getElementType());
		} else {
			element = new StructureTemplateDeclaration(parent, remoteChild.getElementType(), className);
		}		
		
		setIndex(element);
		element.setActive(remoteChild.isActive());
		element.setTypeName(type);
		parent.addChild(element);
		if (remoteChild instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)remoteChild;
			setBodyPosition(element, remoteElement);
			if (className.length() > 0)
				setIdentifierPosition(element, remoteElement);
			else {
				element.setIdPos(remoteElement.getIdStartPos(), remoteElement.getIdLength());
			}
		}
		
		return element;
	}

	private Include createInclusion(Parent parent, IInclude inclusion) throws CModelException  {
		// create element
		String name = inclusion.getElementName();
		Include element= new Include(parent, name, inclusion.isStandard());
		element.setFullPathName(inclusion.getFullFileName());
		setIndex(element);
		element.setActive(inclusion.isActive());
		element.setResolved(inclusion.isResolved());
		// if there is a duplicate include, also set the index

		// add to parent
		parent.addChild(element);
		// set positions
		if (inclusion instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)inclusion;
			setIdentifierPosition(element, remoteElement);
			setBodyPosition(element, remoteElement);
		}
		return element;
		
	}
	
	private Macro createMacro(Parent parent, IMacro macro) throws CModelException{
		// create element
		String name = macro.getElementName();
		Macro element= new Macro(parent, name);
		setIndex(element);
		element.setActive(macro.isActive());
		// add to parent
		parent.addChild(element);
		// set positions
		if (macro instanceof org.eclipse.ptp.internal.rdt.core.model.SourceManipulation) {
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement = (org.eclipse.ptp.internal.rdt.core.model.SourceManipulation)macro;
			setIdentifierPosition(element, remoteElement);
			setBodyPosition(element, remoteElement);
		}
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			element.setFunctionStyle(true);
		}
		
		return element;
	}
	
	/**
	 * Utility method to set the body position of an element from a remote element.
	 *
	 * @param element
	 * @param remoteElement
	 * @throws CModelException
	 */
	private void setBodyPosition(SourceManipulation element, org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement) throws CModelException {
		setBodyPosition(element, remoteElement.getSourceManipulationInfo());
	}

	/**
	 * Utility method to set the body position of an element from a remote element.
	 *
	 * @param info
	 * @param remoteElement
	 */
	private void setBodyPosition(SourceManipulation element,
			org.eclipse.ptp.internal.rdt.core.model.SourceManipulationInfo remoteInfo){
	
		int nodeOffset = remoteInfo.getStartPos();
		int nodeLength = remoteInfo.getLength();
		int startLine = remoteInfo.getStartLine();
		int endLine = remoteInfo.getEndLine();
		
		element.setPos(nodeOffset, nodeLength);
		element.setLines(startLine, endLine);
	}
	
	/**
	 * Utility method to set the identifier position of an element from a remote element.
	 *
	 * @param element
	 * @param remoteElement
	 * @throws CModelException
	 */
	private void setIdentifierPosition(SourceManipulation element, org.eclipse.ptp.internal.rdt.core.model.SourceManipulation remoteElement) throws CModelException {
		setIdentifierPosition(element, remoteElement.getSourceManipulationInfo());
	}
	
	/**
	 * Utility method to set the identifier position of an element from a remote element.
	 *
	 * @param info
	 * @param remoteInfo
	 */
	private void setIdentifierPosition(SourceManipulation element, org.eclipse.ptp.internal.rdt.core.model.SourceManipulationInfo remoteInfo) {

		int nodeOffset = remoteInfo.getIdStartPos();
		int nodeLength = remoteInfo.getIdLength();
		element.setIdPos(nodeOffset, nodeLength);
	}
	
	private void setIndex(SourceManipulation element) {
		int[] idx= fEqualElements.get(element);
		if (idx == null) {
			idx= new int[] {0};
			fEqualElements.put(element, idx);
		} else {
			element.setIndex(++idx[0]);
		}
	}
	
	private boolean isCanceled() {
		return fProgressMonitor != null && fProgressMonitor.isCanceled();
	}
}
