package org.eclipse.ptp.debug.external.simulator;

import java.io.IOException;
import java.io.InputStream;

public class SimInputStream extends InputStream {

	int counter = 0;
	int max;
	
	int pause;
	String str;
	int strLen;
	
	public SimInputStream(String s, int times, int delay) {
		super();
		max = times;
		str = s;
		pause = delay;
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
				Thread.sleep(pause * 1000);
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
