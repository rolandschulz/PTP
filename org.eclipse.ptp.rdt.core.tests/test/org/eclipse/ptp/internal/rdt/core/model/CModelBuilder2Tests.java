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

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IStructureDeclaration;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IVariableDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.tests.util.ModelUtil;
import org.junit.Assert;
import org.junit.Test;

public class CModelBuilder2Tests {
	protected TranslationUnit buildModel(ILanguage language, String name, String code) throws CoreException, DOMException {
		return ModelUtil.buildModel(language, name, code);
	}
	
	@Test public void testFunction() throws Exception {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "int main() {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertFunction(children[0], ICElement.C_FUNCTION, "main", new String[0], "int");
	}

	@Test public void testFunctionDeclaration() throws Exception {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "int main();");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertFunction(children[0], ICElement.C_FUNCTION_DECLARATION, "main", new String[0], "int");
	}
	
	@Test public void testUninitializedVariable() throws Exception {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "double variable; static int staticVariable; const long constVariable; volatile short volatileVariable; extern int externVariable;");
		ICElement[] children = unit.getChildren();
		assertEquals(5, children.length);

		assertVariable(children[0], "variable", "double", false, false, false);
		assertVariable(children[1], "staticVariable", "int", true, false, false);
		assertVariable(children[2], "constVariable", "const long", false, true, false);
		assertVariable(children[3], "volatileVariable", "volatile short", false, false, true);
		assertVariable(children[4], ICElement.C_VARIABLE_DECLARATION, "externVariable", "int", false, false, false);
	}

	@Test public void testVariable() throws Exception {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "double variable = 1;");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertVariable(children[0], "variable", "double", false, false, false);
	}

	@Test public void testVariableTemplate() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "template <bool threads, int inst> char* default_alloc_template<threads, inst>::S_start_free = 0;");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertVariable(children[0], ICElement.C_TEMPLATE_VARIABLE, "default_alloc_template<threads,inst>::S_start_free", "char*", false, false, false);
		assertTemplate(children[0], ICElement.C_TEMPLATE_VARIABLE, "default_alloc_template<threads,inst>::S_start_free", new String[] { "bool", "int" });
	}
	
	@Test public void testMultiVariable() throws Exception {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "float x, y;");
		ICElement[] children = unit.getChildren();
		assertEquals(2, children.length);
		
		assertEquals("x", children[0].getElementName());
		assertEquals(ICElement.C_VARIABLE, children[0].getElementType());
		assertEquals("y", children[1].getElementName());
		assertEquals(ICElement.C_VARIABLE, children[1].getElementType());
	}
	
	@Test public void testEmptyNamespace() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "namespace N {}");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertNamespace(children[0], "N");
		ICElement[] namespaceChildren = ((INamespace)children[0]).getChildren();
		assertEquals(0, namespaceChildren.length);
	}
	
	@Test public void testNamespace() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "namespace N { int a; double b; }");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertNamespace(children[0], "N");
		ICElement[] namespaceChildren = ((INamespace)children[0]).getChildren();
		assertEquals(2, namespaceChildren.length);
		
		assertVariable(namespaceChildren[0], "a", "int", false, false, false);
		assertVariable(namespaceChildren[1], "b", "double", false, false, false);
	}
	
	@Test public void testClassDeclaration() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C;");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertCompositeType(children[0], ICElement.C_CLASS_DECLARATION, "C", false, false);
	}

	@Test public void testStructDeclaration() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "struct S;");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertCompositeType(children[0], ICElement.C_STRUCT_DECLARATION, "S", false, false);
	}

	@Test public void testUnionDeclaration() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "union U;");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertCompositeType(children[0], ICElement.C_UNION_DECLARATION, "U", false, false);
	}

	@Test public void testEmptyClass() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_CLASS, "C", true, false);
		IStructure structure = (IStructure) children[0];
		assertEquals(false, structure.isAbstract());
		ICElement[] classChildren = structure.getChildren();
		assertEquals(0, classChildren.length);
	}
	
	@Test public void testEmptyStruct() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "struct S {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_STRUCT, "S", true, false);
		IStructure structure = (IStructure) children[0];
		assertEquals(false, structure.isAbstract());
		ICElement[] classChildren = structure.getChildren();
		assertEquals(0, classChildren.length);
	}
	
	@Test public void testEmptyUnion() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "union U {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_UNION, "U", true, false);
		IStructure structure = (IStructure) children[0];
		assertEquals(false, structure.isAbstract());
		ICElement[] classChildren = structure.getChildren();
		assertEquals(0, classChildren.length);
	}

	@Test public void testClass() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C { int privateField; public: C(); C(int p); ~C(); };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_CLASS, "C", true, false);
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(4, classChildren.length);
		assertField(classChildren[0], "privateField", "int", ASTAccessVisibility.PRIVATE, false, false, false, false);
		assertConstructor(classChildren[1], ICElement.C_METHOD_DECLARATION, "C", ASTAccessVisibility.PUBLIC, false, false, new String[0], "");
		assertConstructor(classChildren[2], ICElement.C_METHOD_DECLARATION, "C", ASTAccessVisibility.PUBLIC, false, false, new String[] { "int" }, "");
		assertDestructor(classChildren[3], ICElement.C_METHOD_DECLARATION, "~C", ASTAccessVisibility.PUBLIC, false, false, "");
	}
	
	@Test public void testAbstractClass() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C { protected: virtual C() = 0; virtual C(int p); public: virtual ~C(); };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_CLASS, "C", true, false);
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(3, classChildren.length);
		assertConstructor(classChildren[0], ICElement.C_METHOD_DECLARATION, "C", ASTAccessVisibility.PROTECTED, true, true, new String[0], "virtual");
		assertConstructor(classChildren[1], ICElement.C_METHOD_DECLARATION, "C", ASTAccessVisibility.PROTECTED, true, false, new String[] { "int" }, "virtual");
		assertDestructor(classChildren[2], ICElement.C_METHOD_DECLARATION, "~C", ASTAccessVisibility.PUBLIC, true, false, "virtual");
	}
	
	@Test public void testPureVirtualDestructor() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C { public: virtual ~C() = 0; };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_CLASS, "C", true, false);
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(1, classChildren.length);
		assertDestructor(classChildren[0], ICElement.C_METHOD_DECLARATION, "~C", ASTAccessVisibility.PUBLIC, true, true, "virtual");
	}

	@Test public void testStruct() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "struct S { int publicField; };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_STRUCT, "S", true, false);
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(1, classChildren.length);
		assertField(classChildren[0], "publicField", "int", ASTAccessVisibility.PUBLIC, false, false, false, false);
	}
	
	@Test public void testUnion() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "union U { int publicField; };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_UNION, "U", true, false);
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(1, classChildren.length);
		assertField(classChildren[0], "publicField", "int", ASTAccessVisibility.PUBLIC, false, false, false, false);
	}

	@Test public void testField() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C {private: int privateField; protected: double protectedField; public: float publicField; static short staticField; const double constField; volatile int volatileField; const volatile long constVolatileField; mutable long mutableField; };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		assertCompositeType(children[0], ICElement.C_CLASS, "C", true, false);
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(8, classChildren.length);
		
		assertField(classChildren[0], "privateField", "int", ASTAccessVisibility.PRIVATE, false, false, false, false);
		assertField(classChildren[1], "protectedField", "double", ASTAccessVisibility.PROTECTED, false, false, false, false);
		assertField(classChildren[2], "publicField", "float", ASTAccessVisibility.PUBLIC, false, false, false, false);
		assertField(classChildren[3], "staticField", "short", ASTAccessVisibility.PUBLIC, true, false, false, false);
		assertField(classChildren[4], "constField", "const double", ASTAccessVisibility.PUBLIC, false, true, false, false);
		assertField(classChildren[5], "volatileField", "volatile int", ASTAccessVisibility.PUBLIC, false, false, true, false);
		assertField(classChildren[6], "constVolatileField", "const volatile long", ASTAccessVisibility.PUBLIC, false, true, true, false);
		assertField(classChildren[7], "mutableField", "long", ASTAccessVisibility.PUBLIC, false, false, false, true);
	}

	@Test public void testInclude() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "#include <stdio.h>\n#include \"other.h\"");
		ICElement[] children = unit.getChildren();
		
		assertEquals(2, children.length);
		assertInclude(children[0], "stdio.h", true);
		assertInclude(children[1], "other.h", false);
	}
	
	@Test public void testAnonymousEnumeration() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "enum { first = 1, second, third };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertEnumeration(children[0], "");
		IEnumeration enumeration = (IEnumeration) children[0];
		ICElement[] enumChildren = enumeration.getChildren();
		assertEquals(3, enumChildren.length);
		assertEnumerator(enumChildren[0], "first", "1");
		assertEnumerator(enumChildren[1], "second", null);
		assertEnumerator(enumChildren[2], "third", null);
	}
	
	@Test public void testEnumeration() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "enum MyEnum { first, second, third };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertEnumeration(children[0], "MyEnum");
		IEnumeration enumeration = (IEnumeration) children[0];
		ICElement[] enumChildren = enumeration.getChildren();
		assertEquals(3, enumChildren.length);
		assertEnumerator(enumChildren[0], "first", null);
		assertEnumerator(enumChildren[1], "second", null);
		assertEnumerator(enumChildren[2], "third", null);
	}

	@Test public void testPlainDefine() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "#define ONE 1");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertMacro(children[0], "ONE");
	}
	
	@Test public void testFunctionStyleDefine() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "#define PRINT(string,msg)  printf(string, msg)");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertMacro(children[0], "PRINT");
	}
	
	@Test public void testNestedNamespace() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "namespace MyPackage { class Hello { namespace MyNestedPackage { class Y {}; } }; }");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertNamespace(children[0], "MyPackage");
		ICElement[] namespaceChildren = ((INamespace) children[0]).getChildren();
		assertEquals(1, namespaceChildren.length);
		assertCompositeType(namespaceChildren[0], ICElement.C_CLASS, "Hello", true, false);
		ICElement[] classChildren = ((IStructure) namespaceChildren[0]).getChildren();
		assertEquals(1, classChildren.length);
		assertNamespace(classChildren[0], "MyNestedPackage");
		ICElement[] nestedChildren = ((INamespace) classChildren[0]).getChildren();
		assertEquals(1, nestedChildren.length);
		assertCompositeType(nestedChildren[0], ICElement.C_CLASS, "Y", true, false);
	}
	
	@Test public void testDerivedClass() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class Y {}; class X : public Y { double privateField; public: X(int x) : Y(x) {} };");
		ICElement[] children = unit.getChildren();
		assertEquals(2, children.length);
		assertCompositeType(children[0], ICElement.C_CLASS, "Y", true, false);
		assertCompositeType(children[1], ICElement.C_CLASS, "X", true, false);
		
		IStructure derived = (IStructure) children[1];
		assertEquals(ASTAccessVisibility.PUBLIC, derived.getSuperClassAccess("Y"));
		Assert.assertArrayEquals(new String[] { "Y" }, derived.getSuperClassesNames());
	}
	
	@Test public void testTypedef() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "struct MyStruct { int sint; }; typedef struct MyStruct myStruct;");
		ICElement[] children = unit.getChildren();
		assertEquals(2, children.length);
		assertCompositeType(children[0], ICElement.C_STRUCT, "MyStruct", true, false);
		assertTypedef(children[1], "myStruct", "struct MyStruct");
	}
	
	@Test public void testElaboratedType() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "typedef struct { int ss; } myTypedef;");
		ICElement[] children = unit.getChildren();
		assertEquals(2, children.length);
		assertCompositeType(children[0], ICElement.C_STRUCT, "", true, false);
		assertTypedef(children[1], "myTypedef", "struct");
	}
	
	@Test public void testFunctionPointer() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "static void * (*orig_malloc_hook)(const char *file, int line, size_t size);");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertVariable(children[0], "orig_malloc_hook", "void*(*)(const char*, int, size_t)", true, false, false);
	}
	
	@Test public void testTemplateFunction() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "template<class A, typename B=C> A aTemplatedFunction( B bInstance );");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);		
		assertTemplate(children[0], ICElement.C_TEMPLATE_FUNCTION_DECLARATION, "aTemplatedFunction", new String[] { "A", "B" });
		assertFunction(children[0], ICElement.C_TEMPLATE_FUNCTION_DECLARATION, "aTemplatedFunction", new String[] { "B" }, "A");
		
		IFunctionDeclaration function = (IFunctionDeclaration) children[0];
		Assert.assertArrayEquals(new String[] { "B" }, function.getParameterTypes());
	}
	
	@Test public void testTemplateMethod() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class enclosing { public: template<class A, typename B=C> A aTemplatedMethod( B bInstance ); };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertCompositeType(children[0], ICElement.C_CLASS, "enclosing", true, false);
		ICElement[] classChildren = ((IStructure) children[0]).getChildren();
		assertEquals(1, classChildren.length);
		assertTemplate(classChildren[0], ICElement.C_TEMPLATE_METHOD_DECLARATION, "aTemplatedMethod", new String[] { "A", "B" });
		assertMethod(classChildren[0], ICElement.C_TEMPLATE_METHOD_DECLARATION, "aTemplatedMethod", ASTAccessVisibility.PUBLIC, false, false, false, false, new String[] { "B" }, "A");
	}
	
	@Test public void testTemplateClass() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "template<class T, typename Tibor = junk> class myarray {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertCompositeType(children[0], ICElement.C_TEMPLATE_CLASS, "myarray", true, true);
		assertTemplate(children[0], ICElement.C_TEMPLATE_CLASS, "myarray", new String[] { "T", "Tibor" });
	}
	
	@Test public void testTemplateStruct() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "template<class T, typename Tibor = junk> struct mystruct {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);		
		assertCompositeType(children[0], ICElement.C_TEMPLATE_STRUCT, "mystruct", true, true);
		assertTemplate(children[0], ICElement.C_TEMPLATE_STRUCT, "mystruct", new String[] { "T", "Tibor" });
	}
	
	@Test public void testTemplateUnion() throws CoreException, DOMException {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "template<class T, typename Tibor = junk> union myunion {};");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertCompositeType(children[0], ICElement.C_TEMPLATE_UNION, "myunion", true, true);
		assertTemplate(children[0], ICElement.C_TEMPLATE_UNION, "myunion", new String[] { "T", "Tibor" });
	}

	@Test public void testArray() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "int myArray [5][];");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertVariable(children[0], "myArray", "int[][]", false, false, false);
	}
	
	@Test public void testArrayParameter() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "int main(int argc, char * argv[]);");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		assertFunction(children[0], ICElement.C_FUNCTION_DECLARATION, "main", new String[] { "int", "char*[]" }, "int");
	}
	
	@Test public void testBug180815() throws CoreException, DOMException {
		ILanguage language = getCLanguage();
		ITranslationUnit unit = buildModel(language , "test", "struct bug180815 { int i,j; } bug180815_var0, bug180815_var1;");
		ICElement[] children = unit.getChildren();
		assertEquals(3, children.length);
		assertCompositeType(children[0], ICElement.C_STRUCT, "bug180815", true, false);
		assertVariable(children[1], "bug180815_var0", "struct bug180815", false, false, false);
		assertVariable(children[2], "bug180815_var1", "struct bug180815", false, false, false);
	}
	
	@Test public void testConstructor() throws Exception {
		ILanguage language = getCPPLanguage();
		ITranslationUnit unit = buildModel(language , "test", "class C { C::C(); C::~C(); };");
		ICElement[] children = unit.getChildren();
		assertEquals(1, children.length);
		
		IStructure structure = (IStructure) children[0];
		ICElement[] classChildren = structure.getChildren();
		assertEquals(2, classChildren.length);
		
		assertConstructor(classChildren[0], ICElement.C_METHOD_DECLARATION, "C", ASTAccessVisibility.PRIVATE, false, false, new String[0], "");
	}
	
	protected void assertInclude(ICElement element, String name, boolean isSystemInclude) {
		assertEquals(ICElement.C_INCLUDE, element.getElementType());
		assertEquals(name, element.getElementName());
		IInclude include = (IInclude) element;
		assertEquals(name, include.getIncludeName());
		assertEquals(isSystemInclude, include.isStandard());
	}
	protected void assertCompositeType(ICElement element, int type, String name, boolean isDefinition, boolean isTemplate) throws CModelException {
		assertEquals(type, element.getElementType());
		assertEquals(name, element.getElementName());
		IStructureDeclaration structure = (IStructureDeclaration) element;
		
		if (isTemplate) {
			if (isDefinition) {
				assertEquals(type == ICElement.C_TEMPLATE_CLASS, structure.isClass());
				assertEquals(type == ICElement.C_TEMPLATE_STRUCT, structure.isStruct());
				assertEquals(type == ICElement.C_TEMPLATE_UNION, structure.isUnion());
			} else {
				assertEquals(type == ICElement.C_TEMPLATE_CLASS_DECLARATION, structure.isClass());
				assertEquals(type == ICElement.C_TEMPLATE_STRUCT_DECLARATION, structure.isStruct());
				assertEquals(type == ICElement.C_TEMPLATE_UNION_DECLARATION, structure.isUnion());
			}
		} else {
			if (isDefinition) {
				assertEquals(type == ICElement.C_CLASS, structure.isClass());
				assertEquals(type == ICElement.C_STRUCT, structure.isStruct());
				assertEquals(type == ICElement.C_UNION, structure.isUnion());
			} else {
				assertEquals(type == ICElement.C_CLASS_DECLARATION, structure.isClass());
				assertEquals(type == ICElement.C_STRUCT_DECLARATION, structure.isStruct());
				assertEquals(type == ICElement.C_UNION_DECLARATION, structure.isUnion());
			}
		}
	}

	protected void assertVariable(ICElement element, String name, String typeName, boolean isStatic, boolean isConst, boolean isVolatile) throws CModelException {
		assertVariable(element, ICElement.C_VARIABLE, name, typeName, isStatic, isConst, isVolatile);
	}
	
	protected void assertVariable(ICElement element, int type, String name, String typeName, boolean isStatic, boolean isConst, boolean isVolatile) throws CModelException {
		assertEquals(type, element.getElementType());
		IVariableDeclaration variable = (IVariableDeclaration) element;
		assertEquals(name, variable.getElementName());
		assertEquals(isStatic, variable.isStatic());
		assertEquals(isConst, variable.isConst());
		assertEquals(isVolatile, variable.isVolatile());
		Assert.assertNotNull(variable.getTypeName());
		assertEquals(typeName, variable.getTypeName());
	}

	protected void assertField(ICElement element, String name, String typeName, ASTAccessVisibility visibility, boolean isStatic, boolean isConst, boolean isVolatile, boolean isMutable) throws CModelException {
		assertVariable(element, ICElement.C_FIELD, name, typeName, isStatic, isConst, isVolatile);
		IField field = (IField) element;
		assertEquals(visibility, field.getVisibility());
		assertEquals(isMutable, field.isMutable());
	}
	
	protected void assertConstructor(ICElement element, int type, String name, ASTAccessVisibility visibility, boolean isVirtual, boolean isPureVirtual, String[] expectedParameterTypes, String typeName) throws CModelException {
		assertMethod(element, type, name, visibility, true, false, isVirtual, isPureVirtual, expectedParameterTypes, typeName);
	}

	protected void assertDestructor(ICElement element, int type, String name, ASTAccessVisibility visibility, boolean isVirtual, boolean isPureVirtual, String typeName) throws CModelException {
		assertMethod(element, type, name, visibility, false, true, isVirtual, isPureVirtual, new String[0], typeName);
	}

	protected void assertMethod(ICElement element, int type, String name, ASTAccessVisibility visibility, boolean isConstructor, boolean isDestructor, boolean isVirtual, boolean isPureVirtual, String[] expectedParameterTypes, String returnType) throws CModelException {
		assertEquals(name, element.getElementName());
		assertEquals(type, element.getElementType());
		IMethodDeclaration method = (IMethodDeclaration) element;
		assertEquals(visibility, method.getVisibility());
		assertEquals(isConstructor, method.isConstructor());
		assertEquals(isDestructor, method.isDestructor());
		assertEquals(isVirtual, method.isVirtual());
		assertEquals(isPureVirtual, method.isPureVirtual());
		
		String[] types = method.getParameterTypes();
		Assert.assertArrayEquals(expectedParameterTypes, types);
		assertEquals(returnType, method.getReturnType());
	}

	protected void assertFunction(ICElement element, int type, String name, String[] expectedParameterTypes, String returnType) throws CModelException {
		assertEquals(type, element.getElementType());
		assertEquals(name, element.getElementName());
		IFunctionDeclaration method = (IFunctionDeclaration) element;
		
		String[] types = method.getParameterTypes();
		Assert.assertArrayEquals(expectedParameterTypes, types);
		assertEquals(returnType, method.getReturnType());
	}

	protected void assertEnumerator(ICElement element, String name, String value) {
		assertEquals(ICElement.C_ENUMERATOR, element.getElementType());
		assertEquals(name, element.getElementName());
		IEnumerator enumerator = (IEnumerator) element;
		assertEquals(value, enumerator.getConstantExpression());
	}

	protected void assertEnumeration(ICElement element, String name) {
		assertEquals(ICElement.C_ENUMERATION, element.getElementType());
		assertEquals(name, element.getElementName());
	}

	protected void assertMacro(ICElement element, String name) {
		assertEquals(ICElement.C_MACRO, element.getElementType());
		assertEquals(name, element.getElementName());
	}

	protected void assertNamespace(ICElement element, String name) {
		assertEquals(name, element.getElementName());
		assertEquals(ICElement.C_NAMESPACE, element.getElementType());
	}
	
	protected void assertTypedef(ICElement element, String name, String typename) {
		assertEquals(ICElement.C_TYPEDEF, element.getElementType());
		assertEquals(name, element.getElementName());
		ITypeDef typedef = (ITypeDef) element;
		assertEquals(typename, typedef.getTypeName());
	}

	protected void assertTemplate(ICElement element, int type, String name, String[] templateParameterTypes) {
		assertEquals(type, element.getElementType());
		assertEquals(name, element.getElementName());
		ITemplate template = (ITemplate) element;
		Assert.assertArrayEquals(templateParameterTypes, template.getTemplateParameterTypes());
	}
	
	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}
	
	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}
}
