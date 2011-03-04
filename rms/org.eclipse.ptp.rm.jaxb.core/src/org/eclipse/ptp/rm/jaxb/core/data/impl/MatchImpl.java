package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Assign;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.exceptions.UnsatisfiedRegexMatchException;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.runnable.ConfigurableRegexTokenizer;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class MatchImpl implements IJAXBNonNLSConstants {

	private final ConfigurableRegexTokenizer tokenizer;
	private final String id;
	private final String linkTo;
	private final String targetName;
	private final String targetType;
	private final boolean errorOnMiss;

	private final List<IAssign> assign;
	private final RegexImpl regex;
	private Object target;

	public MatchImpl(ConfigurableRegexTokenizer tokenizer, Match match) {
		this.tokenizer = tokenizer;
		id = match.getId();
		if (id != null) {
			tokenizer.addMatch(this);
		}
		linkTo = match.getLinkTo();
		targetName = match.getTarget();
		targetType = match.getType();
		errorOnMiss = match.isErrorOnMiss();
		regex = new RegexImpl(match.getRegex());
		List<Assign> assign = match.getAssign();
		this.assign = new ArrayList<IAssign>();
		for (Assign a : assign) {
			this.assign.add(AssignFactory.createIAssign(a));
		}
	}

	public boolean doMatch(String sequence) throws Throwable {
		setTarget();

		String[] matched = regex.getMatched(sequence);
		if (matched == null) {
			if (errorOnMiss) {
				throw new UnsatisfiedRegexMatchException(regex.getExpression());
			}
			return false;
		}

		for (IAssign a : assign) {
			a.assign(target, matched);
		}
		return true;
	}

	public String getId() {
		return id;
	}

	public RegexImpl getRegex() {
		return regex;
	}

	public Object getTarget() {
		return target;
	}

	private void setTarget() throws IllegalStateException {
		target = null;
		if (targetName != null) {
			String name = RMVariableMap.getActiveInstance().getString(targetName);
			target = RMVariableMap.getActiveInstance().getVariables().get(name);
			if (target == null) {
				throw new IllegalStateException(Messages.StreamParserNoSuchVariableError + targetName);
			}
		} else if (linkTo != null) {
			target = tokenizer.getLink(linkTo);
			if (target == null) {
				throw new IllegalStateException(Messages.StreamParserNoSuchLinkError + targetName);
			}
		} else if (targetType != null) {
			if (JOB_ATTRIBUTE.equals(targetType)) {
				target = new JobAttribute();
			} else if (PROPERTY.equals(targetType)) {
				target = new Property();
			}
			if (target != null) {
				tokenizer.addTarget(target);
			}
		} else {
			throw new IllegalStateException(Messages.StreamParserMissingTargetType);
		}
	}
}
