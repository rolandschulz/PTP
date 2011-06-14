/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionTemplate;
import org.eclipse.cdt.core.model.IFunctionTemplateDeclaration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IMethodTemplate;
import org.eclipse.cdt.core.model.IMethodTemplateDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IStructureTemplate;
import org.eclipse.cdt.core.model.IStructureTemplateDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.ptp.internal.rdt.core.tests.util.ModelUtil;

@SuppressWarnings("restriction")
public class BindingAdapterTests extends TestCase {
	protected ILanguage getLanguage() {
		return GPPLanguage.getDefault();
	}

	protected static class NameCollector extends CPPASTVisitor {
		private List<IASTName> fNames;
		
		public NameCollector() {
			fNames = new LinkedList<IASTName>();
			shouldVisitNames = true;
		}
		
		@Override
		public int visit(IASTName name) {
			fNames.add(name);
			return PROCESS_CONTINUE;
		}
		
		public List<IASTName> getNames() {
			return Collections.unmodifiableList(fNames);
		}
	}
	
	private List<IASTName> collectNames(IASTTranslationUnit ast) {
		NameCollector collector = new NameCollector();
		ast.accept(collector);
		return collector.getNames();
	}
	
	
	public void assertName(IASTName name, Class<?> modelClass, Class<?> parentClass) throws CModelException, DOMException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		IBinding binding = name.resolveBinding();
		ITranslationUnit parentUnit = new TranslationUnit(null, "unit", null, null);
		ICElement element = BindingAdapter.adaptBinding(parentUnit, binding, true);
		assertTrue(modelClass.isAssignableFrom(element.getClass()));
		assertEquals(binding.getName(), element.getElementName());

		if (element instanceof ISourceReference) {
			ISourceReference reference = (ISourceReference) element;
			ITranslationUnit unit = reference.getTranslationUnit();
			assertNotNull(unit);
		}
		
		ICElement parent = element.getParent();
		assertNotNull(parent);
		assertTrue(parentClass.isAssignableFrom(parent.getClass()));
	}


	public void testEnumeration() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "enum E { a, b, c };");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(0), IEnumeration.class, ITranslationUnit.class);
		assertName(names.get(1), IEnumerator.class, ITranslationUnit.class);
		assertName(names.get(2), IEnumerator.class, ITranslationUnit.class);
		assertName(names.get(3), IEnumerator.class, ITranslationUnit.class);
	}
	
	
	public void testFunction() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "int f(double x) { return 1; }");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(0), IFunction.class, ITranslationUnit.class);
	}

	
	public void testFunctionTemplate() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "template<class T> int f(T x) { return 1; }");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(1), IFunctionTemplate.class, ITranslationUnit.class);
	}

	
	public void testFunctionTemplateDeclaration() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "template<class T> int f(T x);");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(1), IFunctionTemplateDeclaration.class, ITranslationUnit.class);
	}
	
	
	public void testNamespace() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "namespace N { int x; }");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(0), INamespace.class, ITranslationUnit.class);
	}

	
	public void testStructure() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "struct S { static int f; S(); ~S(); void m(double x); template<class T> T f(T x) { return x; }; }; S::S() {}");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(0), IStructure.class, ITranslationUnit.class);
		assertName(names.get(1), IField.class, IStructure.class);
		assertName(names.get(2), IMethodDeclaration.class, IStructure.class);
		assertName(names.get(3), IMethodDeclaration.class, IStructure.class);
		assertName(names.get(4), IMethodDeclaration.class, IStructure.class);
		assertName(names.get(8), IMethodTemplateDeclaration.class, IStructure.class);
		assertName(names.get(12), IMethod.class, IStructure.class);
	}

	
	public void testStructureTemplateDeclaration() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "template<class U> struct S;");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(1), IStructureTemplateDeclaration.class, ITranslationUnit.class);
	}

	
	public void testStructureTemplate() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "template<class U> struct S { static U f; S(); ~S(); void m(double x); template<class T> T f(T x) { return x; } template<class V> void setData(V data); }; template<class V> void S::setData(V data) {}");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(1), IStructureTemplate.class, ITranslationUnit.class);
		assertName(names.get(19), IMethodTemplate.class, IStructure.class);
	}

	
	public void testTypeDef() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "typedef int word;");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(0), ITypeDef.class, ITranslationUnit.class);
	}

	
	public void testVariable() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "static int x = 1;");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(0), IVariable.class, ITranslationUnit.class);
	}

	
	public void testFieldSpecialization() throws Exception {
		IASTTranslationUnit ast = ModelUtil.buildAST(getLanguage(), "", "template <bool threads, int inst> class default_alloc_template {static char* S_start_free; }; template <bool threads, int inst> char* default_alloc_template<threads, inst>::S_start_free = 0;");
		List<IASTName> names = collectNames(ast);
		assertName(names.get(6), Field.class, IStructure.class);
	}
}
