package org.eclipse.sexpr;

import java.util.Vector;

public class SExprList extends SExprElement {
	
	private Vector listVec;
	
	public SExprList() {
		listVec = new Vector();
	}
	
	public void add(SExprElement el) {
		listVec.add(el);
	}
	
	public int size() {
		return listVec.size();
	}
	
	public SExprElement elementAt(int i) {
		return (SExprElement)listVec.elementAt(i);
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		
		convertToString(listVec, str);
		
		return str.toString();
	}
	
	private void convertToString(Vector vec, StringBuffer s) {
		s.append("(");
		
		for (int i = 0; i < vec.size(); i++) {
			Object obj = vec.elementAt(i);
			if (i > 0)
				s.append(" ");
			if (obj instanceof SExprList) {
				convertToString(((SExprList)obj).listVec, s);
			} else if (obj instanceof SExprAtom) {
				s.append(((SExprAtom)obj).toString());
			}
		}
			
		s.append(")");
	}

}
