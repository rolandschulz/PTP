package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IStreamParserTokenizer;
import org.eclipse.ptp.rm.jaxb.core.data.Apply;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.impl.ApplyImpl;
import org.eclipse.ptp.rm.jaxb.core.data.impl.MatchImpl;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ConfigurableRegexTokenizer implements IStreamParserTokenizer, IJAXBNonNLSConstants, Runnable {
	private Map<String, MatchImpl> matchIndex;
	private List<Object> targets;
	private Throwable error;
	private InputStream in;
	private OutputStream out;
	private final ApplyImpl apply;

	public ConfigurableRegexTokenizer(Apply apply) {
		this.apply = new ApplyImpl(this, apply);
	}

	public void addMatch(MatchImpl match) {
		String id = match.getId();
		if (id == null) {
			return; // shouldn't happen
		}
		if (matchIndex == null) {
			matchIndex = new HashMap<String, MatchImpl>();
		}
		matchIndex.put(id, match);
	}

	public void addTarget(Object target) {
		if (targets == null) {
			targets = new ArrayList<Object>();
		}
		targets.add(target);
	}

	public List<Object> getCreatedTargets() {
		return targets;
	}

	public Throwable getInternalError() {
		return error;
	}

	public Object getLink(String matchId) {
		if (matchIndex == null) {
			return null;
		}
		MatchImpl match = matchIndex.get(matchId);
		if (match == null) {
			return null;
		}
		return match.getTarget();
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedWriter bw = null;
			if (out != null) {
				bw = new BufferedWriter(new OutputStreamWriter(out));
			}
			apply.doApply(br, bw);
		} catch (Throwable t) {
			error = t;
			// we do not close the streams here
		}
		postProcess();
	}

	public void setInputStream(InputStream stream) {
		in = stream;
	}

	public void setRedirectStream(OutputStream stream) {
		out = stream;
	}

	private void postProcess() {
		if (targets == null) {
			return;
		}

		Map<String, Object> discovered = RMVariableMap.getActiveInstance().getDiscovered();
		for (Object t : targets) {
			if (t instanceof Property) {
				discovered.put(((Property) t).getName(), t);
			} else if (t instanceof JobAttribute) {
				discovered.put(((JobAttribute) t).getName(), t);
			}
		}
	}
}
