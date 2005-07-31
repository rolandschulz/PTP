package org.eclipse.ptp.debug.external.simulator;

import java.io.IOException;
import java.io.InputStream;

public class SimInputStream extends InputStream {

	int counter = 0;
	
	int max = 1000; /* print "max" lines */
	int delay = 1; /* pause for "delay" second between each line */
	
	String str;
	int strLen;
	
	public SimInputStream(String s) {
		super();
		str = s;
		strLen = s.length();
	}
	
	public int read() throws IOException {
		// Auto-generated method stub
		//System.out.println("SimInputStream.read()");

		counter++;
		
		if (counter > ((strLen + 2) * max)) {
			return -1;
		}
		else if ((counter % (strLen + 2)) == strLen + 1) {
			try {
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
			}
			return '\n';
		}
		else if ((counter % (strLen + 2)) == 0)
			return -1;
		else {
			int pos = counter % (strLen + 2);
			return str.charAt(pos - 1);
		}
	}
}
