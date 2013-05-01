package org.eclipse.ptp.internal.debug.core.pdi.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluateExpressionRequest;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

/**
 * @author clement
 *
 */
/**
 * @author greg
 * 
 */
public class MultiExpressions extends SessionObject implements IPDIMultiExpressions {
	class MExpression implements IPDIExpression {
		private final int id;
		private IAIF aif;

		public MExpression(int id) {
			this.id = id;
		}

		public void dispose() throws PDIException {
			aif = null;
		}

		public boolean equals(IPDIExpression expr) {
			if (expr instanceof MExpression) {
				return ((MExpression) expr).getId() == id;
			}
			return false;
		}

		public IAIF getAIF() throws PDIException {
			return aif;
		}

		public String getExpressionText() {
			return expr;
		}

		public int getId() {
			return id;
		}

		public void setAIF(IAIF aif) {
			this.aif = aif;
		}
	}

	private final String expr;
	private boolean enabled;
	private final Map<Integer, IPDIExpression> expressions = new HashMap<Integer, IPDIExpression>();

	public MultiExpressions(IPDISession session, TaskSet tasks, String ex, boolean enabled) {
		super(session, tasks);
		this.expr = ex;
		this.enabled = enabled;
		initital();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#addExpression(org.eclipse.ptp.debug.core.pdi.model.IPDIExpression)
	 */
	public void addExpression(IPDIExpression expression) {
		if (expression instanceof MExpression) {
			Integer id = new Integer(((MExpression) expression).getId());
			expressions.put(id, expression);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#cleanExpressionsValue(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void cleanExpressionsValue(TaskSet tasks, IProgressMonitor monitor) {
		monitor.setTaskName(NLS.bind(Messages.MultiExpressions_0, expr));
		for (int task : tasks.toArray()) {
			IPDIExpression expression = getExpression(task);
			if (expression != null) {
				expression.setAIF(null);
			}
			monitor.worked(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getAIF(org.eclipse.ptp.debug.core.pdi.model.IPDIExpression)
	 */
	public IAIF getAIF(IPDIExpression expression) throws PDIException {
		return expression.getAIF();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getExpression(int)
	 */
	public IPDIExpression getExpression(int task) {
		return expressions.get(new Integer(task));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getExpressions()
	 */
	public IPDIExpression[] getExpressions() {
		return expressions.values().toArray(new IPDIExpression[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#getExpressionText()
	 */
	public String getExpressionText() {
		return expr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#removeExpression(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void removeExpression(TaskSet tasks) {
		for (int task : tasks.toArray()) {
			expressions.remove(new Integer(task));
		}
	}

	public void removeExpression(IPDIExpression expression) {
		if (expression instanceof MExpression) {
			Integer id = new Integer(((MExpression) expression).getId());
			expressions.remove(id);
		}
	}

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#shutdown()
	 */
	public void shutdown() {
		expressions.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions#updateExpressionsValue(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void updateExpressionsValue(TaskSet tasks, IProgressMonitor monitor) throws PDIException {
		monitor.setTaskName(NLS.bind(Messages.MultiExpressions_1, expr));
		IPDIEvaluateExpressionRequest request = session.getRequestFactory().getEvaluateExpressionRequest(tasks, expr);
		session.getEventRequestManager().addEventRequest(request);
		Map<TaskSet, Object> results = request.getResultMap(tasks);
		for (TaskSet sTasks : results.keySet()) {
			Object obj = results.get(sTasks);
			if (!(obj instanceof IAIF)) {
				// set unknown aif for tasks
				for (int task : sTasks.toArray()) {
					IPDIExpression expression = getExpression(task);
					if (expression == null) {
						continue;
					}
					expression.setAIF(AIFFactory.UNKNOWNAIF());
				}
				continue;
				// throw new PDIException(tasks, "Updating expression value error");
			}

			IAIF aif = (IAIF) obj;
			for (int task : sTasks.toArray()) {
				IPDIExpression expression = getExpression(task);
				if (expression == null) {
					continue;
				}
				if (monitor.isCanceled()) {
					expression.setAIF(AIFFactory.UNKNOWNAIF());
					throw new PDIException(tasks, Messages.MultiExpressions_2);
				}
				expression.setAIF(aif);
				monitor.worked(1);
			}
		}
	}

	/**
	 * 
	 */
	private void initital() {
		for (int task : getTasks().toArray()) {
			addExpression(new MExpression(task));
		}
	}
}
