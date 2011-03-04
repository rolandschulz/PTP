package org.eclipse.ptp.rm.jaxb.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.Append;
import org.eclipse.ptp.rm.jaxb.core.data.Apply;
import org.eclipse.ptp.rm.jaxb.core.data.Assign;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Regex;
import org.eclipse.ptp.rm.jaxb.core.data.impl.AbstractRangeAssign;
import org.eclipse.ptp.rm.jaxb.core.runnable.ConfigurableRegexTokenizer;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class StreamParserTest extends TestCase implements IJAXBNonNLSConstants {

	private class TestRangeAssign extends AbstractRangeAssign {

		@Override
		protected Object[] getValue(Object previous, String[] values) {
			return null;
		}

		private List<Object> getValid(String[] values) {
			return range.findInRange(values);
		}

		private void setRange(String exp) {
			range = new Range(exp);
		}

		private void setRLen(int len) {
			range.setLen(len);
		}

		private boolean test(int i) {
			return range.isInRange(i);
		}
	}

	private String lineRange;
	private boolean[] oracle;
	private String[] values;
	private final boolean verbose = true;
	private final boolean redirect = false;
	private Apply apply;
	private String target;

	@Override
	public void setUp() {
		RMVariableMap.setActiveInstance(null);
		String name = getName();
		if (name.equals("testRange")) { //$NON-NLS-1$
			// the second number is <, not <=
			lineRange = "3,8,12:15,21, 24:29,40:N-3"; //$NON-NLS-1$
			oracle = new boolean[] { false, false, false, true, false, false, false, false, true, false, false, false, true, true,
					true, false, false, false, false, false, false, true, false, false, true, true, true, true, true, false, false,
					false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, false,
					false, false };
			values = new String[oracle.length];
		} else if (name.equals("testParseQstat")) { //$NON-NLS-1$
			target = "available_queues"; //$NON-NLS-1$
			Property p = new Property();
			p.setName(target);
			RMVariableMap.getActiveInstance().getVariables().put(target, p);
			apply = new Apply();
			apply.setDelim(LINE_SEP);
			Match match = new Match();
			match.setTarget(target);
			Regex regex = new Regex();
			regex.setContent(".*"); //$NON-NLS-1$
			match.setRegex(regex);
			Assign assign = new Assign();
			Append append = new Append();
			append.setDelim(", "); //$NON-NLS-1$
			append.setValues("0"); //$NON-NLS-1$
			assign.setAppend(append);
			assign.setName("value"); //$NON-NLS-1$
			match.getAssign().add(assign);
			apply.getMatch().add(match);
		}
	}

	@Override
	public void tearDown() {

	}

	public void testParseQstat() {
		runTokenizer(getQstatOut());
		Property p = (Property) RMVariableMap.getActiveInstance().getVariables().get(target);
		assertNotNull(p);
		assertNotNull(p.getValue());
		if (verbose) {
			System.out.println(p.getValue());
		}
	}

	public void testRange() {
		TestRangeAssign range = new TestRangeAssign();
		range.setRange(lineRange);
		range.setRLen(50);
		int trues = 0;

		for (int i = 0; i < 50; i++) {
			boolean b = range.test(i);
			if (oracle[i]) {
				trues++;
			}
			if (verbose) {
				System.out.println(" line " + i + " in range: " + b + ", should be " + oracle[i]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			assertEquals(oracle[i], b);
			values[i] = "" + oracle[i]; //$NON-NLS-1$
		}

		List<Object> valid = range.getValid(values);
		for (Object o : valid) {
			assertEquals("true", o); //$NON-NLS-1$
			assertEquals(trues, valid.size());
		}
	}

	private void runTokenizer(InputStream stream) {
		IStreamParserTokenizer t = new ConfigurableRegexTokenizer(apply);
		t.setInputStream(stream);
		if (redirect) {
			t.setRedirectStream(System.out);
		}
		Thread thr = new Thread(t);
		thr.start();
		try {
			thr.join();
		} catch (InterruptedException ignored) {
		}

	}

	private static InputStream getNoiseBeforeJobId() {
		String content = "abe and lincoln will be down for 3 hours Saturday May 14, 2013 from 00:00:00 to 03:00:00" + LINE_SEP //$NON-NLS-1$
				+ "There is currently no way to know what your shell is.  Please look in /etc/passwd" + LINE_SEP //$NON-NLS-1$
				+ "To get your default account, click your heels three times and say, 'There's no place like home!'" + LINE_SEP //$NON-NLS-1$
				+ "429324.honest1" + LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getOpenMPIOut() {
		String content = "mca:mca:base:param:mca_component_disable_dlopen:status:writable" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:help:Whether to attempt to disable opening dynamic components or not" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mca:base:param:mca_component_disable_dlopen:deprecated:no" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:value:1" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:data_source:default value" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:status:writable" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:help:Whether you want MPI API parameters checked at run-time or not.  Possible values are 0 (no checking) and 1 (perform checking at run-time)" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_param_check:deprecated:no" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:value:-1" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:data_source:default value" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:status:writable" //$NON-NLS-1$
				+ LINE_SEP
				+ "mca:mpi:base:param:mpi_yield_when_idle:help:Yield the processor when waiting for MPI communication (for MPI processes, will default to 1 when oversubscribing nodes)" //$NON-NLS-1$
				+ LINE_SEP + "mca:mpi:base:param:mpi_yield_when_idle:deprecated:no" + LINE_SEP //$NON-NLS-1$
				+ "mca:mpi:base:param:mpi_event_tick_rate:value:-1" + LINE_SEP; //$NON-NLS-1$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static InputStream getQstatOut() {
		String content = "normal" + LINE_SEP + "iacat2" + LINE_SEP + "indprio" + LINE_SEP + "lincoln_nomss" + LINE_SEP + "cap1" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ LINE_SEP + "lincoln_debug" + LINE_SEP + "long" + LINE_SEP + "iacat" + LINE_SEP + "industrial" + LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ "lincoln" + LINE_SEP + "wide" + LINE_SEP + "nomss" + LINE_SEP + "debug" + LINE_SEP + "iacat3" + LINE_SEP //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				+ "lincoln_cuda3.2" + LINE_SEP + "fernsler" + LINE_SEP; //$NON-NLS-1$ //$NON-NLS-2$
		return new ByteArrayInputStream(content.getBytes());
	}

	private static String[] regex() {
		String[] result = new String[0];

		try {
			List<String> list = new ArrayList<String>();
			URL url = JAXBInitializationUtils.getURL("data/regex"); //$NON-NLS-1$
			if (url != null) {
				String line = null;
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				while ((line = br.readLine()) != null) {
					list.add(line);
				}
				result = list.toArray(new String[0]);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}

}
