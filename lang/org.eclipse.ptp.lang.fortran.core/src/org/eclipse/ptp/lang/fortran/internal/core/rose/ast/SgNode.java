package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class SgNode implements IASTNode {

	// Contains relationship properties with parent, needed for IASTNode interface
    private ASTNodeProperty property;
    
    private int offset;
    private int length;

 	// symbol table specific to function types. 
	protected static SgFunctionTypeTable p_globalFunctionTypeTable = new SgFunctionTypeTable();

	protected SgNode p_parent = null;
	
	public static SgFunctionTypeTable get_globalFunctionTypeTable() {
		return p_globalFunctionTypeTable;
	}
	
	public static void set_globalFunctionTypeTable(SgFunctionTypeTable globalFunctionTypeTable) {
		p_globalFunctionTypeTable = globalFunctionTypeTable;
	}
	
	public SgNode get_parent() {
		return p_parent;
	}
	
	public void set_parent(SgNode parent) {
		p_parent = parent;
	}

	public boolean accept(ASTVisitor visitor) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(IASTNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getContainingFilename() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTFileLocation getFileLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTNodeLocation[] getNodeLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTNode getParent() {
		return get_parent();
	}

	public ASTNodeProperty getPropertyInParent() {
		return property;
	}

	public String getRawSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setParent(IASTNode node) {
		if (node.getClass() == SgNode.class) {
			set_parent((SgNode) node);
		} // TODO - what about alternative
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		this.property = property;
	}

    public void setOffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
        //this.locations = null; // private IASTNodeLocation[] locations = null;
    }
    
}
