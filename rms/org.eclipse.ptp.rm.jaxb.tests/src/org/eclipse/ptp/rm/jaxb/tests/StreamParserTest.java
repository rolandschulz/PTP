package org.eclipse.ptp.rm.jaxb.tests;

import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Add;
import org.eclipse.ptp.rm.jaxb.core.data.Range;
import org.eclipse.ptp.rm.jaxb.core.data.StreamParser;
import org.eclipse.ptp.rm.jaxb.core.data.Token;
import org.eclipse.ptp.rm.jaxb.core.runnable.StreamParserImpl;
import org.junit.Test;

public class StreamParserTest extends TestCase implements IJAXBNonNLSConstants {

	private String lineRange;
	private boolean[] oracle;
	private final boolean verbose = false;
	private StreamParser parser;

	@Override
	public void setUp() {
		if (getName().equals("testRange")) { //$NON-NLS-1$
			lineRange = "3,8,12:15,21, 24:29,40:N"; //$NON-NLS-1$
			oracle = new boolean[] { false, false, false, true, false, false, false, false, true, false, false, false, true, true,
					true, true, false, false, false, false, false, true, false, false, true, true, true, true, true, true, false,
					false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true,
					true, true };
		} else if (getName().equals("testParser")) { //$NON-NLS-1$
			parser = new StreamParser();
			parser.setName("test-parser"); //$NON-NLS-1$
			parser.setRange("0:N"); //$NON-NLS-1$
			parser.setRedirect(true);
			parser.setStderr(false);
			Token t = new Token();
			Add add = new Add();
			add.setName("available_queues"); //$NON-NLS-1$
			t.getAdd().add(add);
			parser.setToken(t);
		}
	}

	@Override
	public void tearDown() {

	}

	@Test
	public void testParser() {
		try {
			Process p = Runtime.getRuntime().exec("ssh abe.ncsa.uiuc.edu qstat -Q -f | grep Queue: | cut -d ' ' -f 2"); //$NON-NLS-1$
			StreamParserImpl spl = new StreamParserImpl(parser, p.getInputStream());
			spl.setOut(new OutputStreamWriter(System.out));
			spl.start();
			try {
				p.waitFor();
				spl.join();
			} catch (InterruptedException ignored) {
			}
			Throwable internal = spl.getInternalError();
			if (internal != null) {
				throw internal;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			assert (null == t);
		}
	}

	@Test
	public void testRange() {
		Range lr = new Range(lineRange);
		for (int i = 0; i < 50; i++) {
			boolean b = lr.isInRange(i);
			if (verbose) {
				System.out.println(" line " + i + " in range: " + b); //$NON-NLS-1$//$NON-NLS-2$
			}
			assertEquals(oracle[i], b);
		}
	}

}
