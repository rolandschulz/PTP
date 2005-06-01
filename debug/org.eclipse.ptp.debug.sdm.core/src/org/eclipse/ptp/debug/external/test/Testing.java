package org.eclipse.ptp.debug.external.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.ptp.debug.external.DebugSession;




public class Testing {
	
	public static void main(String[] args) {
		System.out.println("Hello World");
		
		
	    BufferedReader input = null;
	    ArrayList contents = new ArrayList();
	    File sourceFile = new File("/home/donny/tmp/gt3_debugging_test/test.c");
	    try {
	      //use buffering
	      //this implementation reads one line at a time
	      input = new BufferedReader( new FileReader(sourceFile) );
	      String line = null; //not declared within while loop
	      while (( line = input.readLine()) != null){
	        contents.add(line);
	      }
	    } catch (FileNotFoundException ex) {
	      ex.printStackTrace();
	    } catch (IOException ex) {
		      ex.printStackTrace();
		}
		
		
		
		try {
			
			DebugSession debug = new DebugSession();
			Thread.sleep(3000);
			System.out.println("Hello World 1");
			
			
			
			
			debug.load("/home/donny/tmp/gt3_debugging_test/test");
			//debug.remote("localhost", 4000);
			//debug.load("/home/donny/mpich_test/hello", 3);
			Thread.sleep(3000);
			System.out.println("Hello World 2");

			debug.breakpoint("main");
			Thread.sleep(3000);
			System.out.println("Hello World 3");
					
			debug.run();
			Thread.sleep(3000);
			System.out.println("Hello World 4");

			debug.cont();
			Thread.sleep(3000);
			System.out.println("Hello World 5");

			debug.exit();
			Thread.sleep(3000);
			System.out.println("Hello World 6");
			
		} catch (Exception e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Hello World End");
	}
}