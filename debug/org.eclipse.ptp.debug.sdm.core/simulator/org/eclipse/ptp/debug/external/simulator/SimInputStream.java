package org.eclipse.ptp.debug.external.simulator;

import java.io.IOException;
import java.io.InputStream;

public class SimInputStream extends InputStream {

	boolean finished;
	Queue queue;
	
	String str;
	int strLen;
	
	public SimInputStream() {
		super();
		queue = new Queue();
		finished = false;
		str = null;
		strLen = -2;
	}
	
	public void printString(String s) {
		queue.addItem(s);
	}
	
	public void destroy() {
		finished = true;
		queue.addItem("destroy");
	}
	
	public int read() throws IOException {
		if (strLen == 0) {
			strLen--;
			return '\n';
		}
		
		if (strLen == -1) {
			strLen--;
			return -1;
		}
			
		if (strLen == -2) {
			try {
				if (finished) {
					return -1;
				}
				Thread.sleep(1000);
				str = (String) queue.removeItem();
				if (str.equals("destroy")) {
					return -1;
				}
				strLen = str.length();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
			
		int chr = str.charAt(str.length() - strLen);
		strLen--;
		return chr;
	}
}
