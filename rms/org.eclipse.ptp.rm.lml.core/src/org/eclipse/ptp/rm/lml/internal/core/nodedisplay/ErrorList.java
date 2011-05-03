package org.eclipse.ptp.rm.lml.internal.core.nodedisplay;

import java.util.ArrayList;

/**
 *This class collects errors, which occur while parsing
 *and checking lml-files
 * 
 * @author karbach
 *
 *
 */
public class ErrorList {
	
	
	private ArrayList<String> errors;
	
	private boolean realerror;
	
	public ErrorList(){
		errors = new ArrayList<String>();
		realerror = false;
	}
	
	/**
	 * An error occurred. Add it to the list
	 * @param errormsg
	 */
	public void addError(String errormsg) {
		errors.add(errormsg);
		realerror = true;
	}
	
	public void addMessage(String msg) {
		errors.add(msg);
	}
	
	/**
	 * @return true, if addError was used, else false
	 */
	public boolean isRealError() {
		return realerror;
	}
	
	public String toString() {
		
		String res = "";
		
		if (realerror) {
			res = "Following errors occurred: \n";
		}
		else res = "No errors: \n";
		
		for (int i = 0; i < errors.size(); i++) {
			res += errors.get(i) + "\n";
		}
		
		return res;
		
	}

}
