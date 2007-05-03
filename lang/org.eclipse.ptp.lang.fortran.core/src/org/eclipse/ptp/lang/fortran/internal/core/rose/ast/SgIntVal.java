package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgIntVal extends SgValueExp {
	int val;
	String text;

	// TODO - move parent to where it belongs, could be in SgNode, need to consider DOM interface as well
	private SgNode parent;
	public void set_parent(SgNode parent) {
		this.parent = parent;
	}

	// TODO - where does this go
	private SgIntVal kindType;
	public void set_kind(SgIntVal kt) {
		kindType = kt;
	}

	public SgIntVal(int i, String text) {
		this.val = i;
		this.text = text;
	}
	
	public String toString() {
		String str = "  SgIntVal: " + val + " text = " + text;
		if (kindType != null) {
			str += kindType.toString();
		}
		return str;
	}

}
