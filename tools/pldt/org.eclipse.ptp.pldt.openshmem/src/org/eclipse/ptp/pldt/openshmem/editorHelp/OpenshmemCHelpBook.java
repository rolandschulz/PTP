/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.openshmem.Activator;

public class OpenshmemCHelpBook extends CHelpBookImpl {
	private static final String TITLE = "OPEN SHMEM C Help Book";

	public OpenshmemCHelpBook() {
		super(Activator.getPluginId());
		// populate function map
		funcName2FuncInfo.put("shmem_init", new FunctionSummaryImpl("shmem_init", "",
				"Initialize a process to use IBM openshmem library. It is the same as start_pes(0)",
				new FunctionPrototypeSummaryImpl("shmem_init", "void", "void"), null));
		funcName2FuncInfo.put("start_pes", new FunctionSummaryImpl("start_pes", "",
				"Initialize a process to use the IBM  openshmem library",
				new FunctionPrototypeSummaryImpl("start_pes", "void", "int npes"), null));
		funcName2FuncInfo.put("_my_pe", new FunctionSummaryImpl("_my_pe", "",
				"Returns the processing element (PE) number of the calling PE",
				new FunctionPrototypeSummaryImpl("_mp_pe", "int", "void"), null));
		funcName2FuncInfo.put("my_pe", new FunctionSummaryImpl("my_pe", "",
				"Returns the processing element (PE) number of the calling PE",
				new FunctionPrototypeSummaryImpl("mp_pe", "int", "void"), null));
		funcName2FuncInfo.put("shmem_my_pe", new FunctionSummaryImpl("shmem_my_pe", "",
				"Returns the processing element (PE) number of the calling PE",
				new FunctionPrototypeSummaryImpl("shmem_mp_pe", "int", "void"), null));
		funcName2FuncInfo.put("_num_pes", new FunctionSummaryImpl("_num_pes", "",
				"Returns the number of processing elements (PEs) running in the job",
				new FunctionPrototypeSummaryImpl("_num_pes", "int", "void"), null));
		funcName2FuncInfo.put("num_pes", new FunctionSummaryImpl("num_pes", "",
				"Returns the number of processing elements (PEs) running in the job",
				new FunctionPrototypeSummaryImpl("num_pes", "int", "void"), null));
		funcName2FuncInfo.put("shmem_num_pes", new FunctionSummaryImpl("shmem_num_pes", "",
				"Returns the number of processing elements (PEs) running in the job",
				new FunctionPrototypeSummaryImpl("shmem_num_pes", "int", "void"), null));
		funcName2FuncInfo.put("shmem_finalize", new FunctionSummaryImpl("shmem_finalize", "",
				"Cleans up the resources that used by IBM openshmem library",
				new FunctionPrototypeSummaryImpl("shmem_finalize", "void", "void"), null));
		funcName2FuncInfo.put("shmem_double_g", new FunctionSummaryImpl("shmem_double_g", "",
				"Transfers one data item from a remote Processing Element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_double_g", "double", "double *addr, int pe"), null));
		funcName2FuncInfo.put("shmem_float_g", new FunctionSummaryImpl("shmem_float_g", "",
				"Transfers one data item from a remote Processing Element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_float_g", "float", "float *addr, int pe"), null));
		funcName2FuncInfo.put("shmem_int_g", new FunctionSummaryImpl("shmem_int_g", "",
				"Transfers one data item from a remote Processing Element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_int_g", "int", "int *addr, int pe"), null));
		funcName2FuncInfo.put("shmem_long_g", new FunctionSummaryImpl("shmem_long_g", "",
				"Transfers one data item from a remote Processing Element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_long_g", "long", "long *addr, int pe"), null));
		funcName2FuncInfo.put("shmem_short_g", new FunctionSummaryImpl("shmem_short_g", "",
				"Transfers one data item from a remote Processing Element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_short_g", "short", "short *addr, int pe"), null));
		funcName2FuncInfo.put("shmem_double_get", new FunctionSummaryImpl("shmem_double_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_double_get", "void",
						"double *target, const double *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_float_get", new FunctionSummaryImpl("shmem_float_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_float_get", "void",
						"float *target, const float *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_get",
				new FunctionSummaryImpl("shmem_get", "", "Transfers data from a specified processing element (PE)",
						new FunctionPrototypeSummaryImpl("shmem_get", "void",
								"void *target, const void *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_get32", new FunctionSummaryImpl("shmem_get32", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_get32", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_get64", new FunctionSummaryImpl("shmem_get64", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_get64", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_get128", new FunctionSummaryImpl("shmem_get128", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_get128", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_getmem", new FunctionSummaryImpl("shmem_getmem", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_getmem", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_int_get", new FunctionSummaryImpl("shmem_int_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_int_get", "void", "int *target, const int *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_long_get", new FunctionSummaryImpl("shmem_long_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_long_get", "void", "long *target, const long *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_longdouble_get", new FunctionSummaryImpl("shmem_longdouble_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_longdouble_get", "void",
						"long double *target, const long double *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_get", new FunctionSummaryImpl("shmem_longlong_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_longlong_get", "void",
						"long long *target, const long long *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_short_get", new FunctionSummaryImpl("shmem_short_get", "",
				"Transfers data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_short_get", "void",
						"short *target, const short *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_double_iget", new FunctionSummaryImpl("shmem_double_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_double_iget", "void",
						"double *target, const double *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_float_iget", new FunctionSummaryImpl("shmem_float_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_float_iget", "void",
						"float *target, const float *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iget", new FunctionSummaryImpl("shmem_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iget", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iget32", new FunctionSummaryImpl("shmem_iget32", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iget32", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iget64", new FunctionSummaryImpl("shmem_iget64", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iget64", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iget128", new FunctionSummaryImpl("shmem_iget128", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iget128", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_int_iget", new FunctionSummaryImpl("shmem_int_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_int_iget", "void",
						"int *target, const int *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_long_iget", new FunctionSummaryImpl("shmem_long_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_long_iget", "void",
						"long *target, const long *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_longdouble_iget", new FunctionSummaryImpl("shmem_longdouble_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_longdouble_iget", "void",
						"long double *target, const long double *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_iget", new FunctionSummaryImpl("shmem_longlong_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_longlong_iget", "void",
						"long long *target, const long long *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_short_iget", new FunctionSummaryImpl("shmem_short_iget", "",
				"Transfers strided data from a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_short_iget", "void",
						"short *target, const short *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_double_p", new FunctionSummaryImpl("shmem_double_p", "",
				"Transfers one data item to a remote processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_double_p", "void", "double *addr, double value, int pe"), null));
		funcName2FuncInfo.put("shmem_float_p", new FunctionSummaryImpl("shmem_float_p", "",
				"Transfers one data item to a remote processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_float_p", "void", "float *addr, float value, int pe"), null));
		funcName2FuncInfo.put("shmem_long_p", new FunctionSummaryImpl("shmem_long_p", "",
				"Transfers one data item to a remote processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_long_p", "void", "long *addr, long value, int pe"), null));
		funcName2FuncInfo.put("shmem_int_p", new FunctionSummaryImpl("shmem_int_p", "",
				"Transfers one data item to a remote processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_int_p", "void", "int *addr, int value, int pe"), null));
		funcName2FuncInfo.put("shmem_short_p", new FunctionSummaryImpl("shmem_short_p", "",
				"Transfers one data item to a remote processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_short_p", "void", "short *addr, short value, int pe"), null));
		funcName2FuncInfo.put("shmem_double_put", new FunctionSummaryImpl("shmem_double_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_double_put", "void",
						"double *target, const double *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_float_put", new FunctionSummaryImpl("shmem_float_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_float_put", "void",
						"float *target, const float *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_int_put", new FunctionSummaryImpl("shmem_int_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_int_put", "void", "int *target, const int *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_long_put", new FunctionSummaryImpl("shmem_long_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_long_put", "void", "long *target, const long *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_longlong_put", new FunctionSummaryImpl("shmem_longlong_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_longlong_put", "void",
						"long long *target, const long long *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_longdouble_put", new FunctionSummaryImpl("shmem_longdouble_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_longdouble_put", "void",
						"long double *target, const long double *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_put",
				new FunctionSummaryImpl("shmem_put", "", "Transfers data to a specified processing element(PE)",
						new FunctionPrototypeSummaryImpl("shmem_put", "void",
								"void *target, const void *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_put32", new FunctionSummaryImpl("shmem_put32", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_put32", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_put64", new FunctionSummaryImpl("shmem_put64", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_put64", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_put128", new FunctionSummaryImpl("shmem_put128", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_put128", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_putmem", new FunctionSummaryImpl("shmem_putmem", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_putmem", "void", "void *target, const void *source, size_t len, int pe"),
				null));
		funcName2FuncInfo.put("shmem_short_put", new FunctionSummaryImpl("shmem_short_put", "",
				"Transfers data to a specified processing element(PE)",
				new FunctionPrototypeSummaryImpl("shmem_short_put", "void",
						"short *target, const short *source, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_double_iput", new FunctionSummaryImpl("shmem_double_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_double_iput", "void",
						"double *target, const double *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_float_iput", new FunctionSummaryImpl("shmem_float_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_float_iput", "void",
						"float *target, const float *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iput", new FunctionSummaryImpl("shmem_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iput", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iput32", new FunctionSummaryImpl("shmem_iput32", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iput32", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iput64", new FunctionSummaryImpl("shmem_iput64", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iput64", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_iput128", new FunctionSummaryImpl("shmem_iput128", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_iput128", "void",
						"void *target, const void *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_int_iput", new FunctionSummaryImpl("shmem_int_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_int_iput", "void",
						"int *target, const int *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_long_iput", new FunctionSummaryImpl("shmem_long_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_long_iput", "void",
						"long *target, const long *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_longdouble_iput", new FunctionSummaryImpl("shmem_longdouble_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_longdouble_iput", "void",
						"long double *target, const long double *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_iput", new FunctionSummaryImpl("shmem_longlong_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_longlong_iput", "void",
						"long long *target, const long long *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo.put("shmem_short_iput", new FunctionSummaryImpl("shmem_short_iput", "",
				"Transfers strided data to a specified processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_short_iput", "void",
						"short *target, const short *source, ptrdiff_t tst, ptrdiff_t sst, size_t len, int pe"), null));
		funcName2FuncInfo
				.put("shmem_barrier_all",
						new FunctionSummaryImpl(
								"shmem_barrier_all",
								"",
								"Registers the arrival of a processing element (PE) at a barrier and suspends PE execution until all other PEs arrive at the barrier",
								new FunctionPrototypeSummaryImpl("shmem_barrier_all", "void", "void"), null));
		funcName2FuncInfo.put("shmem_barrier", new FunctionSummaryImpl("shmem_barrier", "",
				"Performs a barrier operation on a subset of processing elements (PEs)",
				new FunctionPrototypeSummaryImpl("shmem_barrier", "void",
						"int PE_start, int logPE_stride, int PE_size, long *pSync"), null));
		funcName2FuncInfo.put("shmem_wait", new FunctionSummaryImpl("shmem_wait", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_wait", "void", "long *ivar, long cmp_value"), null));
		funcName2FuncInfo.put("shmem_int_wait", new FunctionSummaryImpl("shmem_int_wait", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_int_wait", "void", "int *var, int value"), null));
		funcName2FuncInfo.put("shmem_long_wait", new FunctionSummaryImpl("shmem_long_wait", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_long_wait", "void", "long *var, long value"), null));
		funcName2FuncInfo.put("shmem_longlong_wait", new FunctionSummaryImpl("shmem_longlong_wait", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_longlong_wait", "void", "long long *var, long long value"), null));
		funcName2FuncInfo.put("shmem_short_wait", new FunctionSummaryImpl("shmem_short_wait", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_short_wait", "void", "short *var, short value"), null));
		funcName2FuncInfo.put("shmem_wait_until", new FunctionSummaryImpl("shmem_wait_until", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_wait_until", "void", "long *ivar, int cmp, long value"), null));
		funcName2FuncInfo.put("shmem_int_wait_until", new FunctionSummaryImpl("shmem_int_wait_until", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_int_wait_until", "void", "int *var, int cond, int value"), null));
		funcName2FuncInfo.put("shmem_long_wait_until", new FunctionSummaryImpl("shmem_long_wait_until", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_long_wait_until", "void", "long *var, int cond, long value"), null));
		funcName2FuncInfo.put("shmem_longlong_wait_until", new FunctionSummaryImpl("shmem_longlong_wait_until", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_longlong_wait_until", "void", "long long *var, int cond, long long value"),
				null));
		funcName2FuncInfo.put("shmem_short_wait_until", new FunctionSummaryImpl("shmem_short_wait_until", "",
				"Waits for a variable on the local processing element (PE) to change",
				new FunctionPrototypeSummaryImpl("shmem_short_wait_until", "void", "short *var, int cond, short value"), null));
		funcName2FuncInfo.put("shmem_fence", new FunctionSummaryImpl("shmem_fence", "", "Assures ordering of delivery of puts",
				new FunctionPrototypeSummaryImpl("shmem_fence", "void", "void"), null));
		funcName2FuncInfo.put("shmem_quiet", new FunctionSummaryImpl("shmem_quiet", "",
				"Waits for completion of all outstanding remote writes issued by the calling processing element (PE)",
				new FunctionPrototypeSummaryImpl("shmem_quiet", "void", "void"), null));
		funcName2FuncInfo.put("shmem_swap", new FunctionSummaryImpl("shmem_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_swap", "long", "long *target, long value, int pe"), null));
		funcName2FuncInfo.put("shmem_double_swap", new FunctionSummaryImpl("shmem_double_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_double_swap", "double", "double *target, double value, int pe"), null));
		funcName2FuncInfo.put("shmem_float_swap", new FunctionSummaryImpl("shmem_float_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_float_swap", "float", "float *target, float value, int pe"), null));
		funcName2FuncInfo.put("shmem_int_swap", new FunctionSummaryImpl("shmem_int_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_int_swap", "int", "int *target, int value, int pe"), null));
		funcName2FuncInfo.put("shmem_long_swap", new FunctionSummaryImpl("shmem_long_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_long_swap", "long", "long *target, long value, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_swap", new FunctionSummaryImpl("shmem_longlong_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_longlong_swap", "long long", "long long *target, long long value, int pe"),
				null));
		funcName2FuncInfo.put("shmem_short_swap", new FunctionSummaryImpl("shmem_short_swap", "",
				"Performs an atomic swap operation to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_short_swap", "short", "short *target, short value, int pe"), null));
		funcName2FuncInfo.put("shmem_int_cswap", new FunctionSummaryImpl("shmem_int_cswap", "",
				"Performs an atomic conditional swap to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_int_cswap", "int", "int *target, int cond, int value, int pe"), null));
		funcName2FuncInfo.put("shmem_long_cswap", new FunctionSummaryImpl("shmem_long_cswap", "",
				"Performs an atomic conditional swap to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_long_cswap", "long", "long *target, long cond, long value, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_cswap", new FunctionSummaryImpl("shmem_longlong_cswap", "",
				"Performs an atomic conditional swap to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_longlong_cswap", "long long",
						"long long *target, long long cond, long long value, int pe"), null));
		funcName2FuncInfo.put("shmem_short_cswap", new FunctionSummaryImpl("shmem_short_cswap", "",
				"Performs an atomic conditional swap to a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_short_cswap", "short", "short *target, short cond, short value, int pe"),
				null));
		funcName2FuncInfo.put("shmem_int_add", new FunctionSummaryImpl("shmem_int_add", "",
				"Performs an atomic add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_int_add", "void", "int *target, int value, int pe"), null));
		funcName2FuncInfo.put("shmem_long_add", new FunctionSummaryImpl("shmem_long_add", "",
				"Performs an atomic add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_long_add", "void", "long *target, long value, int pe"), null));
		funcName2FuncInfo
				.put("shmem_longlong_add",
						new FunctionSummaryImpl("shmem_longlong_add", "",
								"Performs an atomic add operation on a symmetric data object",
								new FunctionPrototypeSummaryImpl("shmem_longlong_add", "void",
										"long long *target, long long value, int pe"), null));
		funcName2FuncInfo.put("shmem_short_add", new FunctionSummaryImpl("shmem_short_add", "",
				"Performs an atomic add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_short_add", "void", "short *target, short value, int pe"), null));
		funcName2FuncInfo.put("shmem_int_fadd", new FunctionSummaryImpl("shmem_int_fadd", "",
				"Performs an atomic fetch-and-add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_int_fadd", "int", "int *target, int value, int pe"), null));
		funcName2FuncInfo.put("shmem_long_fadd", new FunctionSummaryImpl("shmem_long_fadd", "",
				"Performs an atomic fetch-and-add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_long_fadd", "long", "long *target, long value, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_fadd", new FunctionSummaryImpl("shmem_longlong_fadd", "",
				"Performs an atomic fetch-and-add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_longlong_fadd", "long long", "long long *target, long long value, int pe"),
				null));
		funcName2FuncInfo.put("shmem_short_fadd", new FunctionSummaryImpl("shmem_short_fadd", "",
				"Performs an atomic fetch-and-add operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_short_fadd", "short", "short *target, short value, int pe"), null));
		funcName2FuncInfo.put("shmem_int_inc", new FunctionSummaryImpl("shmem_int_inc", "",
				"Performs an atomic increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_int_inc", "void", "int *target, int pe"), null));
		funcName2FuncInfo.put("shmem_long_inc", new FunctionSummaryImpl("shmem_long_inc", "",
				"Performs an atomic increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_long_inc", "void", "long *target, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_inc", new FunctionSummaryImpl("shmem_longlong_inc", "",
				"Performs an atomic increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_longlong_inc", "void", "long long *target, int pe"), null));
		funcName2FuncInfo.put("shmem_short_inc", new FunctionSummaryImpl("shmem_short_inc", "",
				"Performs an atomic increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_short_inc", "void", "short *target, int pe"), null));
		funcName2FuncInfo.put("shmem_int_finc", new FunctionSummaryImpl("shmem_int_finc", "",
				"Performs an atomic fetch-and-increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_int_finc", "int", "int *target, int pe"), null));
		funcName2FuncInfo.put("shmem_long_finc", new FunctionSummaryImpl("shmem_long_finc", "",
				"Performs an atomic fetch-and-increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_long_finc", "long", "long *target, int pe"), null));
		funcName2FuncInfo.put("shmem_longlong_finc", new FunctionSummaryImpl("shmem_longlong_finc", "",
				"Performs an atomic fetch-and-increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_longlong_finc", "long long", "long long *target, int pe"), null));
		funcName2FuncInfo.put("shmem_short_finc", new FunctionSummaryImpl("shmem_short_finc", "",
				"Performs an atomic fetch-and-increment operation on a symmetric data object",
				new FunctionPrototypeSummaryImpl("shmem_short_finc", "short", "short *target, int pe"), null));
		funcName2FuncInfo.put("shmem_clear_lock", new FunctionSummaryImpl("shmem_clear_lock", "",
				"Releases a mutual exclusion memory lock",
				new FunctionPrototypeSummaryImpl("shmem_clear_lock", "void", "long *lock"), null));
		funcName2FuncInfo.put("shmem_set_lock", new FunctionSummaryImpl("shmem_set_lock", "",
				"Sets (acquires) a mutual exclusion memory lock",
				new FunctionPrototypeSummaryImpl("shmem_set_lock", "void", "long *lock"), null));
		funcName2FuncInfo.put("shmem_test_lock", new FunctionSummaryImpl("shmem_test_lock", "",
				"Sets a mutual exclusion memory lock only if it is not currently held by any PE",
				new FunctionPrototypeSummaryImpl("shmem_test_lock", "int", "long *lock"), null));
		funcName2FuncInfo
				.put("shmem_broadcast",
						new FunctionSummaryImpl(
								"shmem_broadcast",
								"",
								"Broadcasts a block of data from one processing element (PE) to one or more target PEs",
								new FunctionPrototypeSummaryImpl("shmem_broadcast", "void",
										"void *target, const void *source, size_t nlong, int PE_root, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_broadcast32",
						new FunctionSummaryImpl(
								"shmem_broadcast32",
								"",
								"Broadcasts a block of data from one processing element (PE) to one or more target PEs",
								new FunctionPrototypeSummaryImpl("shmem_broadcast32", "void",
										"void *target, const void *source, size_t nlong, int PE_root, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_broadcast64",
						new FunctionSummaryImpl(
								"shmem_broadcast64",
								"",
								"Broadcasts a block of data from one processing element (PE) to one or more target PEs",
								new FunctionPrototypeSummaryImpl("shmem_broadcast64", "void",
										"void *target, const void *source, size_t nlong, int PE_root, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_collect",
						new FunctionSummaryImpl(
								"shmem_collect",
								"",
								"Concatenates blocks of data from multiple PEs to an array on every PEs in the active set",
								new FunctionPrototypeSummaryImpl("shmem_collect", "void",
										"void *target, const void *source, size_t nlong, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_collect32",
						new FunctionSummaryImpl(
								"shmem_collect32",
								"",
								"Concatenates blocks of data from multiple PEs to an array on every PEs in the active set",
								new FunctionPrototypeSummaryImpl("shmem_collect32", "void",
										"void *target, const void *source, size_t nlong, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_collect64",
						new FunctionSummaryImpl(
								"shmem_collect64",
								"",
								"Concatenates blocks of data from multiple PEs to an array on every PEs in the active set",
								new FunctionPrototypeSummaryImpl("shmem_collect64", "void",
										"void *target, const void *source, size_t nlong, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_fcollect",
						new FunctionSummaryImpl(
								"shmem_fcollect",
								"",
								"Concatenates blocks of data from multiple PEs to an array on every PEs in the active set",
								new FunctionPrototypeSummaryImpl("shmem_fcollect", "void",
										"void *target, const void *source, size_t nlong, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_fcollect32",
						new FunctionSummaryImpl(
								"shmem_fcollect32",
								"",
								"Concatenates blocks of data from multiple PEs to an array on every PEs in the active set",
								new FunctionPrototypeSummaryImpl("shmem_fcollect32", "void",
										"void *target, const void *source, size_t nlong, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_fcollect64",
						new FunctionSummaryImpl(
								"shmem_fcollect64",
								"",
								"Concatenates blocks of data from multiple PEs to an array on every PEs in the active set",
								new FunctionPrototypeSummaryImpl("shmem_fcollect64", "void",
										"void *target, const void *source, size_t nlong, int PE_start, int logPE_stride, int PE_size, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_and_to_all",
						new FunctionSummaryImpl(
								"shmem_int_and_to_all",
								"",
								"Performs a logical AND function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_and_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_and_to_all",
						new FunctionSummaryImpl(
								"shmem_long_and_to_all",
								"",
								"Performs a logical AND function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_and_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_and_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_and_to_all",
								"",
								"Performs a logical AND function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_and_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_and_to_all",
						new FunctionSummaryImpl(
								"shmem_short_and_to_all",
								"",
								"Performs a logical AND function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_and_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_double_max_to_all",
						new FunctionSummaryImpl(
								"shmem_double_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_double_max_to_all", "void",
										"double *target, double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_float_max_to_all",
						new FunctionSummaryImpl(
								"shmem_float_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_float_max_to_all", "void",
										"float *target, float *source, int nreduce, int PE_start, int logPE_stride, int PE_size, float *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_max_to_all",
						new FunctionSummaryImpl(
								"shmem_int_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_max_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_max_to_all",
						new FunctionSummaryImpl(
								"shmem_long_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_max_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longdouble_max_to_all",
						new FunctionSummaryImpl(
								"shmem_longdouble_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_longdouble_max_to_all",
										"void",
										"long double *target, long double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_max_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_max_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_max_to_all",
						new FunctionSummaryImpl(
								"shmem_short_max_to_all",
								"",
								"Performs a maximum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_max_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_double_min_to_all",
						new FunctionSummaryImpl(
								"shmem_double_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_double_min_to_all", "void",
										"double *target, double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_float_min_to_all",
						new FunctionSummaryImpl(
								"shmem_float_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_float_min_to_all", "void",
										"float *target, float *source, int nreduce, int PE_start, int logPE_stride, int PE_size, float *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_min_to_all",
						new FunctionSummaryImpl(
								"shmem_int_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_min_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_min_to_all",
						new FunctionSummaryImpl(
								"shmem_long_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_min_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_min_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_min_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longdouble_min_to_all",
						new FunctionSummaryImpl(
								"shmem_longdouble_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_longdouble_min_to_all",
										"void",
										"long double *target, long double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_min_to_all",
						new FunctionSummaryImpl(
								"shmem_short_min_to_all",
								"",
								"Performs a minimum function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_min_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_or_to_all",
						new FunctionSummaryImpl(
								"shmem_int_or_to_all",
								"",
								"Performs a logical OR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_or_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_or_to_all",
						new FunctionSummaryImpl(
								"shmem_long_or_to_all",
								"",
								"Performs a logical OR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_or_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_or_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_or_to_all",
								"",
								"Performs a logical OR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_or_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_or_to_all",
						new FunctionSummaryImpl(
								"shmem_short_or_to_all",
								"",
								"Performs a logical OR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_or_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_double_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_double_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_double_prod_to_all", "void",
										"double *target, double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_float_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_float_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_float_prod_to_all", "void",
										"float *target, float *source, int nreduce, int PE_start, int logPE_stride, int PE_size, float *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_int_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_prod_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_long_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_prod_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longdouble_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_longdouble_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_longdouble_prod_to_all",
										"void",
										"long double *target, long double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_prod_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_short_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_prod_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_complexd_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_complexd_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_complexd_prod_to_all",
										"void",
										"double complex *target, double complex *source, int nreduce, int PE_start, int logPE_stride, int PE_size, double complex *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_complexf_prod_to_all",
						new FunctionSummaryImpl(
								"shmem_complexf_prod_to_all",
								"",
								"Performs a product reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_complexf_prod_to_all",
										"void",
										"float complex *target, float complex *source, int nreduce, int PE_start, int logPE_stride, int PE_size, float complex *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_complexd_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_complexd_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_complexd_sum_to_all",
										"void",
										"double complex *target, double complex *source, int nreduce, int PE_start, int logPE_stride, int PE_size, double complex *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_complexf_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_complexf_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_complexf_sum_to_all",
										"void",
										"float complex *target, float complex *source, int nreduce, int PE_start, int logPE_stride, int PE_size, float complex *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_double_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_double_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_double_sum_to_all", "void",
										"double *target, double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_float_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_float_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_float_sum_to_all", "void",
										"float *target, float *source, int nreduce, int PE_start, int logPE_stride, int PE_size, float *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_int_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_sum_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_long_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_sum_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longdouble_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_longdouble_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl(
										"shmem_longdouble_sum_to_all",
										"void",
										"long double *target, long double *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long double *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_sum_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_sum_to_all",
						new FunctionSummaryImpl(
								"shmem_short_sum_to_all",
								"",
								"Performs a sum reduction function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_sum_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_int_xor_to_all",
						new FunctionSummaryImpl(
								"shmem_int_xor_to_all",
								"",
								"Performs a logical XOR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_int_xor_to_all", "void",
										"int *target, int *source, int nreduce, int PE_start, int logPE_stride, int PE_size, int *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_long_xor_to_all",
						new FunctionSummaryImpl(
								"shmem_long_xor_to_all",
								"",
								"Performs a logical XOR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_long_xor_to_all", "void",
										"long *target, long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_longlong_xor_to_all",
						new FunctionSummaryImpl(
								"shmem_longlong_xor_to_all",
								"",
								"Performs a logical XOR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_longlong_xor_to_all", "void",
										"long long *target, long long *source, int nreduce, int PE_start, int logPE_stride, int PE_size, long long *pWrk, long *pSync"),
								null));
		funcName2FuncInfo
				.put("shmem_short_xor_to_all",
						new FunctionSummaryImpl(
								"shmem_short_xor_to_all",
								"",
								"Performs a logical XOR function across a set of processing elements (PEs)",
								new FunctionPrototypeSummaryImpl("shmem_short_xor_to_all", "void",
										"short *target, short *source, int nreduce, int PE_start, int logPE_stride, int PE_size, short *pWrk, long *pSync"),
								null));
		funcName2FuncInfo.put("shmalloc", new FunctionSummaryImpl("shmalloc", "",
				"Allocates a symmetric, remotely accessible object from the Symmetric heap",
				new FunctionPrototypeSummaryImpl("shmalloc", "void", "size_t size"), null));
		funcName2FuncInfo.put("shrealloc", new FunctionSummaryImpl("shrealloc", "",
				"Resizes a symmetric object that was allocated by shmalloc, shmemalign, or shrealloc",
				new FunctionPrototypeSummaryImpl("shrealloc", "void", "void *ptr, size_t size"), null));
		funcName2FuncInfo.put("shfree", new FunctionSummaryImpl("shfree", "",
				"Frees the memory space of a symmetric object that was allocated by shmalloc, shmemalign, or shrealloc",
				new FunctionPrototypeSummaryImpl("shfree", "void", "void *ptr"), null));
		funcName2FuncInfo.put("shmemalign", new FunctionSummaryImpl("shmemalign", "",
				"Allocates a symmetric, remotely accessible object from the Symmetric heap with specified byte alignment",
				new FunctionPrototypeSummaryImpl("shmemalign", "void", "size_t alignment, size_t size"), null));
		funcName2FuncInfo
				.put("shmem_addr_accessible",
						new FunctionSummaryImpl(
								"shmem_addr_accessible",
								"",
								"Determines whether an address is accessible via IBM openshmem data transfers operations from the specified processing element (PE)",
								new FunctionPrototypeSummaryImpl("shmem_addr_accessible", "int", "void *addr, int pe"), null));
		funcName2FuncInfo
				.put("shmem_pe_accessible",
						new FunctionSummaryImpl(
								"shmem_pe_accessible",
								"",
								"Determines whether a processing element (PE) is accessible via IBM openshmem data transfers operations from the local processing element",
								new FunctionPrototypeSummaryImpl("shmem_pe_accessible", "int", "int pe"), null));

		// set title
		setTitle(TITLE);
	}
}
