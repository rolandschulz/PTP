package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgFile extends SgSupport {
	
	protected SgGlobal		p_root;			// This is the global scope
	protected Sg_File_Info	p_file_info;	// This object permits the file name map mechanism to be used on SgFile objects.
	
	public SgFile() {
		p_root = null;
		p_file_info = null;
	}
	

	public void set_root(SgGlobal scope) {
		this.p_root = scope;
	}

	public Sg_File_Info get_file_info() {
		return p_file_info;
	}
	
	public void set_file_info(Sg_File_Info file_info) {
		p_file_info = file_info;
	}
	
    public void set_sourceFileNameWithPath(String filename) {
    	// TODO
    }
    
    
    public void set_sourceFileNameWithoutPath(String filename) {
    	// TODO
    }
    
    
    public void set_unparse_output_filename(String filename) {
    	// TODO
    }
    
}
