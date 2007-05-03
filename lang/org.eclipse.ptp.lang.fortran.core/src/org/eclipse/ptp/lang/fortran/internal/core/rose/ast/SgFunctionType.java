package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgFunctionType extends SgType {
	
	SgType p_return_type;
	boolean p_has_ellipses;
	
	public SgFunctionType(SgType return_type, boolean has_ellipses) {
		p_return_type = return_type;
		p_has_ellipses = has_ellipses;
		assert(p_has_ellipses == false);
	}

	public SgName get_mangled() {
		// TODO FIXME
		return null;
	}

}
