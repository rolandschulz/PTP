package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgLocatedNode extends SgNode {

 	// This member is never null and stores the source position of the start of the current construct. 
	Sg_File_Info p_startOfConstruct;
	
 	// This member is never null and stores the source position of the end of the current construct.
	Sg_File_Info p_endOfConstruct;
	
	public String class_name() {
		return "ClassName";
	}
	
	// New function interface for Sg_File_Info data stores starting location of
	// contruct (typically the opening brace or first letter of keyword).
	public Sg_File_Info get_startOfConstruct() {
		return p_startOfConstruct;
	}
	
	// Sets the current source location position of the start of the current construct.
	public void set_startOfConstruct (Sg_File_Info startOfConstruct) {
		p_startOfConstruct = startOfConstruct;
	}
	
	// New function interface for Sg_File_Info data stores ending location of contruct (typically the closing brace). 
	public Sg_File_Info get_endOfConstruct() {
		return p_endOfConstruct;
	}
	
	// This function sets the current source location position of the end of the current construct. 
	void set_endOfConstruct (Sg_File_Info endOfConstruct) {
 		p_endOfConstruct = endOfConstruct;
 	}

}
