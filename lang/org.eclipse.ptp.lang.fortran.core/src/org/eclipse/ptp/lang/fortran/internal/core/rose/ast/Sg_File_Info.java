package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class Sg_File_Info extends SgSupport {
	
	protected int p_line = 0;
	protected int p_col = 0;
	
	public static Sg_File_Info generateDefaultFileInfoForTransformationNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Sg_File_Info() {
		
	}

	public Sg_File_Info(String filename, int line, int col) {
		// TODO - filename
		p_line = line;
		p_col = col;
	}

}
