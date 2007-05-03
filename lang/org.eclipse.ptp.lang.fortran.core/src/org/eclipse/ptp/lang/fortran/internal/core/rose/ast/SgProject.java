package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import java.util.ArrayList;

public class SgProject extends SgSupport {
	
	ArrayList<SgFile> p_fileList;

	public SgProject() {
		p_fileList = new ArrayList<SgFile>();
	}

	public ArrayList<SgFile> get_fileList() {
		return p_fileList;
	}
	
	public void set_fileList(ArrayList<SgFile> fileList) {
		p_fileList = fileList;
	}

}
