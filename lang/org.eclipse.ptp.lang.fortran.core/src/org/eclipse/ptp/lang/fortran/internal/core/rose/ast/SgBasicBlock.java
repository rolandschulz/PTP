package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class SgBasicBlock extends SgScopeStatement implements IASTCompoundStatement {
	
	ArrayList<SgStatement> p_statements = new ArrayList<SgStatement>();
	
	public void append_statement(SgStatement element) {
		p_statements.add(element);
	}

	/************************ CDT DOM **********************/
	
	public void addStatement(IASTStatement statement) {
		append_statement((SgStatement) statement);
	}

	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTStatement[] getStatements() {
		IASTStatement[] a = new IASTStatement[0];
		return p_statements.toArray(a);
	}

}
