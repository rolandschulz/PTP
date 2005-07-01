package org.eclipse.sexpr.tests;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.eclipse.sexpr.SExpr;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			FileReader f = new FileReader("org/eclipse/sexpr/tests/tests");
			SExpr s = new SExpr(f);
			while (s.parse()) {
				System.out.println("sexpr = " + s.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
