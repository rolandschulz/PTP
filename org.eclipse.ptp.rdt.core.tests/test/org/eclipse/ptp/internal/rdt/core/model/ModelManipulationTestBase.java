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

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionTemplate;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IStructureTemplate;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.ptp.internal.rdt.core.tests.util.ModelUtil;
import org.junit.Assert;
import org.junit.Test;

public abstract class ModelManipulationTestBase 
{
	protected ILanguage getLanguage() {
		return GPPLanguage.getDefault();
	}
	
	protected abstract void manipulate(ICElement element) throws Exception;
	
	@Test
	public void testEnumeration() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "enum E { a, b, c };");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IEnumeration);
		IEnumeration e = (IEnumeration) children[0];
		manipulate(e);
	}

	@Test
	public void testFunction() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "int f(double x) { return 1; }");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IFunction);
		IFunction e = (IFunction) children[0];
		manipulate(e);
	}

	@Test
	public void testFunctionTemplate() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "template<class T> int f(T x) { return 1; }");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IFunctionTemplate);
		IFunctionTemplate e = (IFunctionTemplate) children[0];
		manipulate(e);
	}

	@Test
	public void testInclude() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "#include <stdio.h>");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IInclude);
		IInclude e = (IInclude) children[0];
		manipulate(e);
	}

	@Test
	public void testMacro() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "#define X(a,b) (a + b)");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IMacro);
		IMacro e = (IMacro) children[0];
		manipulate(e);
	}

	@Test
	public void testNamespace() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "namespace N { int x; }");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof INamespace);
		INamespace e = (INamespace) children[0];
		manipulate(e);
		
	}

	@Test
	public void testStructure() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "struct S { static int f; S(); ~S(); void m(double x); template<class T> T f(T x) { return x; } };");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IStructure);
		IStructure e = (IStructure) children[0];
		manipulate(e);
	}

	@Test
	public void testStructureTemplate() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "template<class U> struct S { static U f; S(); ~S(); void m(double x); template<class T> T f(T x) { return x; } };");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IStructureTemplate);
		IStructureTemplate e = (IStructureTemplate) children[0];
		manipulate(e);
	}

	@Test
	public void testTranslationUnit() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "int x;");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		manipulate(model);
	}

	@Test
	public void testTypeDef() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "typedef int word;");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof ITypeDef);
		ITypeDef e = (ITypeDef) children[0];
		manipulate(e);
	}

	@Test
	public void testUsing() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "using namespace std;");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IUsing);
		IUsing e = (IUsing) children[0];
		manipulate(e);
	}

	@Test
	public void testVariable() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "static int x = 1;");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IVariable);
		IVariable e = (IVariable) children[0];
		manipulate(e);
	}

	@Test
	public void testVariableTemplate() throws Exception {
		TranslationUnit model = ModelUtil.buildModel(getLanguage(), "", "template <bool threads, int inst> char* default_alloc_template<threads, inst>::S_start_free = 0;");
		ICElement[] children = model.getChildren();
		Assert.assertEquals(1, children.length);
		Assert.assertTrue(children[0] instanceof IVariable);
		Assert.assertTrue(children[0] instanceof ITemplate);
		IVariable e = (IVariable) children[0];
		manipulate(e);
		Assert.assertTrue(e instanceof ITemplate);
	}

}
