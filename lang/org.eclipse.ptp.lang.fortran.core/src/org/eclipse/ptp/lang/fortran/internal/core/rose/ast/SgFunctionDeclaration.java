package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class SgFunctionDeclaration extends SgDeclarationStatement implements IASTFunctionDefinition {

 	// This variable stores the string representing the function name.
	protected SgName p_name;

 	// This variable stores flags representing use of inline, virtual, etc
	protected SgFunctionModifier p_functionModifier;
	
	protected SgFunctionParameterList p_parameterList;
	
 	// This variable stores the SgFunctionType.
 	protected SgFunctionType p_type;
 
 	// This variable stores the SgFunctionDefinition. 
 	protected SgFunctionDefinition p_definition;
 
 	protected SgScopeStatement p_scope;
 	
	
	public SgFunctionDeclaration(SgName name, SgFunctionType type, SgFunctionDefinition definition) {
		p_name = name;
		p_type = type;
		p_definition = definition;
		p_parameterList = new SgFunctionParameterList();
	}
	
	SgFunctionDefinition get_definition() {
		return p_definition;
	}
	
	void set_definition(SgFunctionDefinition definition) {
		p_definition = definition;
	}
	
	public SgName get_name() {
		return p_name;
	}
	
	void set_name(SgName name) {
		p_name = name;
	}

	public void set_scope(SgScopeStatement scope) {
		p_scope = scope;
	}
	
	public SgFunctionParameterList get_parameterList() {
		return p_parameterList;
	}
	
	public void set_parameterList(SgFunctionParameterList parameterList) {
		p_parameterList = parameterList;
	}

/**************************** CDT DOM ******************************/	
	
	public IASTStatement getBody() {
		return p_scope;
	}

	public IASTDeclSpecifier getDeclSpecifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTFunctionDeclarator getDeclarator() {
		// TODO Auto-generated method stub
		return null;
	}

	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBody(IASTStatement statement) {
		// TODO Auto-generated method stub
		
	}

	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
		// TODO Auto-generated method stub
		
	}

	public void setDeclarator(IASTFunctionDeclarator declarator) {
		// TODO Auto-generated method stub
		
	}
	
}

