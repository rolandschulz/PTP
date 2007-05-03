package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgInitializedName extends SgSupport {
	
	protected SgName p_name = null;
	protected SgType p_typeptr = null;
	protected SgInitializer p_initptr = null;
	
	Sg_File_Info p_startOfConstruct;

 	// New function interface for Sg_File_Info data stores starting location of contruct (typically the opening brace or first letter of keyword). 
	public Sg_File_Info get_startOfConstruct() {
		return p_startOfConstruct;
	}
	
	public void set_startOfConstruct (Sg_File_Info startOfConstruct) {
		p_startOfConstruct = startOfConstruct;
	}
	
	public SgInitializedName(SgName name, SgType type, SgInitializer initializer) {
		p_name = name;
		p_typeptr = type;
		p_initptr = initializer;
	}
	
	public SgName get_name() {
		return p_name;
	}

}
