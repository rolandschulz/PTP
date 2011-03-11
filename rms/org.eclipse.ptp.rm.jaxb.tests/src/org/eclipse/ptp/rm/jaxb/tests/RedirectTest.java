package org.eclipse.ptp.rm.jaxb.tests;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@SuppressWarnings("restriction")
public class RedirectTest {

	private static class StreamReader extends Thread {
		private final PipedInputStream in = new PipedInputStream();

		@Override
		public void run() {
			BufferedInputStream bis = new BufferedInputStream(in, 4096);
			byte[] buffer = new byte[4096];
			int read = 0;
			while (true) {
				try {
					read = bis.read(buffer, 0, buffer.length);
					if (read == -1) {
						break;
					}
				} catch (EOFException eof) {
					break;
				} catch (IOException t) {
					t.printStackTrace();
					break;
				} finally {

				}
				System.err.print(new String(buffer, 0, read));
				// we do not close the streams here
			}
			try {
				bis.close();
			} catch (IOException t) {
				t.printStackTrace();
			}
		}
	}

	private static class StreamSplitter extends Thread {

		private final InputStream in;
		private PipedOutputStream pout;
		private BufferedOutputStream bout;

		private StreamSplitter(InputStream in) {
			this.in = in;
		}

		@Override
		public void run() {
			BufferedInputStream bin = new BufferedInputStream(in);
			if (pout != null) {
				bout = new BufferedOutputStream(pout, 4096);
			}
			OutputStream[] out = new OutputStream[] { System.out, bout };

			while (true) {
				try {
					int i = bin.read();
					if (i == -1) {
						break;
					}
					for (int k = 0; k < out.length; k++) {
						if (out[k] != null) {
							out[k].write(i);
							out[k].flush();
						}
					}
				} catch (EOFException eof) {
					break;
				} catch (IOException t) {
					t.printStackTrace();
					break;
				} finally {

				}
				// we do not close the streams here
			}
			if (bout != null) {
				try {
					bout.close();
				} catch (IOException t) {
					t.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Throwable {
		try {
			Process p = Runtime.getRuntime().exec(new String[] { "/Users/arossi/qstat" });
			StreamReader r = new StreamReader();
			StreamSplitter s = new StreamSplitter(p.getInputStream());
			s.pout = new PipedOutputStream(r.in);
			s.start();
			r.start();
			try {
				p.waitFor();
			} catch (InterruptedException t) {
			}

			try {
				s.join();
			} catch (InterruptedException t) {
			}
			try {
				r.join();
			} catch (InterruptedException t) {
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
