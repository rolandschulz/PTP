/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.tools.vprof.core.vmon;

import java.util.Hashtable;

public class VMonEvents {
	public final static int VMON_PROF	= 1;
	public final static int VMON_PAPI	= 2;
	public final static int VMON_P6 = 3;
	
	public final static int PAPI_PRESET_MASK = 0x80000000;
	
	public final static int PAPI_L1_DCM = (PAPI_PRESET_MASK | 0x00);
	public final static int PAPI_L1_ICM = (PAPI_PRESET_MASK | 0x01);
	public final static int PAPI_L2_DCM = (PAPI_PRESET_MASK | 0x02);
	public final static int PAPI_L2_ICM = (PAPI_PRESET_MASK | 0x03);
	public final static int PAPI_L3_DCM = (PAPI_PRESET_MASK | 0x04);
	public final static int PAPI_L3_ICM = (PAPI_PRESET_MASK | 0x05);
	public final static int PAPI_L1_TCM = (PAPI_PRESET_MASK | 0x06);		
	public final static int PAPI_L2_TCM = (PAPI_PRESET_MASK | 0x07);
	public final static int PAPI_L3_TCM = (PAPI_PRESET_MASK | 0x08);
	public final static int PAPI_CA_SNP = (PAPI_PRESET_MASK | 0x09);
	public final static int PAPI_CA_SHR = (PAPI_PRESET_MASK | 0x0a);
	public final static int PAPI_CA_CLN = (PAPI_PRESET_MASK | 0x0b);
	public final static int PAPI_CA_INV = (PAPI_PRESET_MASK | 0x0c);
	public final static int PAPI_CA_ITV = (PAPI_PRESET_MASK | 0x0d);
	public final static int PAPI_L3_LDM = (PAPI_PRESET_MASK | 0x0e);
	public final static int PAPI_L3_STM = (PAPI_PRESET_MASK | 0x0f);
	
	public final static int PAPI_BRU_IDL = (PAPI_PRESET_MASK | 0x10);
	public final static int PAPI_FXU_IDL = (PAPI_PRESET_MASK | 0x11);
	public final static int PAPI_FPU_IDL = (PAPI_PRESET_MASK | 0x12);
	public final static int PAPI_LSU_IDL = (PAPI_PRESET_MASK | 0x13);
	public final static int PAPI_TLB_DM = (PAPI_PRESET_MASK | 0x14);
	public final static int PAPI_TLB_IM = (PAPI_PRESET_MASK | 0x15);
	public final static int PAPI_TLB_TL = (PAPI_PRESET_MASK | 0x16);
	public final static int PAPI_L1_LDM = (PAPI_PRESET_MASK | 0x17);
	public final static int PAPI_L1_STM = (PAPI_PRESET_MASK | 0x18);
	public final static int PAPI_L2_LDM = (PAPI_PRESET_MASK | 0x19);
	public final static int PAPI_L2_STM = (PAPI_PRESET_MASK | 0x1a);
	public final static int PAPI_BTAC_M = (PAPI_PRESET_MASK | 0x1b);
	public final static int PAPI_PRF_DM = (PAPI_PRESET_MASK | 0x1c);
	public final static int PAPI_L3_DCH = (PAPI_PRESET_MASK | 0x1d);
	public final static int PAPI_TLB_SD = (PAPI_PRESET_MASK | 0x1e);
	public final static int PAPI_CSR_FAL = (PAPI_PRESET_MASK | 0x1f);
	
	public final static int PAPI_CSR_SUC = (PAPI_PRESET_MASK | 0x20);
	public final static int PAPI_CSR_TOT = (PAPI_PRESET_MASK | 0x21);
	public final static int PAPI_MEM_SCY = (PAPI_PRESET_MASK | 0x22);
	public final static int PAPI_MEM_RCY = (PAPI_PRESET_MASK | 0x23);
	public final static int PAPI_MEM_WCY = (PAPI_PRESET_MASK | 0x24);
	public final static int PAPI_STL_ICY = (PAPI_PRESET_MASK | 0x25);
	public final static int PAPI_FUL_ICY = (PAPI_PRESET_MASK | 0x26);
	public final static int PAPI_STL_CCY = (PAPI_PRESET_MASK | 0x27);
	public final static int PAPI_FUL_CCY = (PAPI_PRESET_MASK | 0x28);
	public final static int PAPI_HW_INT = (PAPI_PRESET_MASK | 0x29);
	public final static int PAPI_BR_UCN = (PAPI_PRESET_MASK | 0x2a);
	public final static int PAPI_BR_CN = (PAPI_PRESET_MASK | 0x2b);
	public final static int PAPI_BR_TKN = (PAPI_PRESET_MASK | 0x2c);
	public final static int PAPI_BR_NTK = (PAPI_PRESET_MASK | 0x2d);
	public final static int PAPI_BR_MSP = (PAPI_PRESET_MASK | 0x2e);
	public final static int PAPI_BR_PRC = (PAPI_PRESET_MASK | 0x2f);
	
	public final static int PAPI_FMA_INS = (PAPI_PRESET_MASK | 0x30);
	public final static int PAPI_TOT_IIS = (PAPI_PRESET_MASK | 0x31);
	public final static int PAPI_TOT_INS = (PAPI_PRESET_MASK | 0x32);
	public final static int PAPI_INT_INS = (PAPI_PRESET_MASK | 0x33);
	public final static int PAPI_FP_INS = (PAPI_PRESET_MASK | 0x34);
	public final static int PAPI_LD_INS = (PAPI_PRESET_MASK | 0x35);
	public final static int PAPI_SR_INS = (PAPI_PRESET_MASK | 0x36);
	public final static int PAPI_BR_INS = (PAPI_PRESET_MASK | 0x37);
	public final static int PAPI_VEC_INS = (PAPI_PRESET_MASK | 0x38);
	public final static int PAPI_RES_STL = (PAPI_PRESET_MASK | 0x39);
	public final static int PAPI_FP_STAL = (PAPI_PRESET_MASK | 0x3a);
	public final static int PAPI_TOT_CYC = (PAPI_PRESET_MASK | 0x3b);
	public final static int PAPI_LST_INS = (PAPI_PRESET_MASK | 0x3c);
	public final static int PAPI_SYC_INS = (PAPI_PRESET_MASK | 0x3d);
	public final static int PAPI_L1_DCH = (PAPI_PRESET_MASK | 0x3e);
	public final static int PAPI_L2_DCH = (PAPI_PRESET_MASK | 0x3f);
	
	public final static int PAPI_L1_DCA = (PAPI_PRESET_MASK | 0x40);
	public final static int PAPI_L2_DCA = (PAPI_PRESET_MASK | 0x41);
	public final static int PAPI_L3_DCA = (PAPI_PRESET_MASK | 0x42);
	public final static int PAPI_L1_DCR = (PAPI_PRESET_MASK | 0x43);
	public final static int PAPI_L2_DCR = (PAPI_PRESET_MASK | 0x44);
	public final static int PAPI_L3_DCR = (PAPI_PRESET_MASK | 0x45);
	public final static int PAPI_L1_DCW = (PAPI_PRESET_MASK | 0x46);
	public final static int PAPI_L2_DCW = (PAPI_PRESET_MASK | 0x47);
	public final static int PAPI_L3_DCW = (PAPI_PRESET_MASK | 0x48);
	public final static int PAPI_L1_ICH = (PAPI_PRESET_MASK | 0x49);
	public final static int PAPI_L2_ICH = (PAPI_PRESET_MASK | 0x4a);
	public final static int PAPI_L3_ICH = (PAPI_PRESET_MASK | 0x4b);
	public final static int PAPI_L1_ICA = (PAPI_PRESET_MASK | 0x4c);
	public final static int PAPI_L2_ICA = (PAPI_PRESET_MASK | 0x4d);
	public final static int PAPI_L3_ICA = (PAPI_PRESET_MASK | 0x4e);
	public final static int PAPI_L1_ICR = (PAPI_PRESET_MASK | 0x4f);

	public final static int PAPI_L2_ICR = (PAPI_PRESET_MASK | 0x50);
	public final static int PAPI_L3_ICR = (PAPI_PRESET_MASK | 0x51);
	public final static int PAPI_L1_ICW = (PAPI_PRESET_MASK | 0x52);
	public final static int PAPI_L2_ICW = (PAPI_PRESET_MASK | 0x53);
	public final static int PAPI_L3_ICW = (PAPI_PRESET_MASK | 0x54);
	public final static int PAPI_L1_TCH = (PAPI_PRESET_MASK | 0x55);
	public final static int PAPI_L2_TCH = (PAPI_PRESET_MASK | 0x56);
	public final static int PAPI_L3_TCH = (PAPI_PRESET_MASK | 0x57);
	public final static int PAPI_L1_TCA = (PAPI_PRESET_MASK | 0x58);
	public final static int PAPI_L2_TCA = (PAPI_PRESET_MASK | 0x59);
	public final static int PAPI_L3_TCA = (PAPI_PRESET_MASK | 0x5a);
	public final static int PAPI_L1_TCR = (PAPI_PRESET_MASK | 0x5b);
	public final static int PAPI_L2_TCR = (PAPI_PRESET_MASK | 0x5c);
	public final static int PAPI_L3_TCR = (PAPI_PRESET_MASK | 0x5d);
	public final static int PAPI_L1_TCW = (PAPI_PRESET_MASK | 0x5e);
	public final static int PAPI_L2_TCW = (PAPI_PRESET_MASK | 0x5f);

	public final static int PAPI_L3_TCW = (PAPI_PRESET_MASK | 0x60);
	public final static int PAPI_FML_INS = (PAPI_PRESET_MASK | 0x61);
	public final static int PAPI_FAD_INS = (PAPI_PRESET_MASK | 0x62);
	public final static int PAPI_FDV_INS = (PAPI_PRESET_MASK | 0x63);
	public final static int PAPI_FNV_INS = (PAPI_PRESET_MASK | 0x64);
	public final static int PAPI_FP_OPS = (PAPI_PRESET_MASK | 0x65);

	public final static int P6_DATA_MEM_REFS = 0x43;
	public final static int P6_DCU_LINES_IN = 0x45;
	public final static int P6_DCU_M_LINES_IN = 0x46;
	public final static int P6_DCU_M_LINES_OUT = 0x47;
	public final static int P6_DCU_MISS_OUTSTANDING = 0x48;
	public final static int P6_IFU_IFETCH = 0x80;
	public final static int P6_IFU_IFETCH_MISS = 0x81;
	public final static int P6_ITLB_MISS = 0x85;
	public final static int P6_IFU_MEM_STALL = 0x86;
	public final static int P6_ILD_STALL = 0x87;
	public final static int P6_L2_IFETCH = 0x28;
	public final static int P6_L2_LD = 0x29;
	public final static int P6_L2_ST = 0x2a;
	public final static int P6_L2_LINES_IN = 0x24;
	public final static int P6_L2_LINES_OUT = 0x26;
	public final static int P6_L2_M_LINES_INM = 0x25;
	public final static int P6_L2_M_LINES_OUTM = 0x27;
	public final static int P6_L2_RQSTS = 0x2e;
	public final static int P6_L2_ADS = 0x21;
	public final static int P6_L2_DBUS_BUSY = 0x22;
	public final static int P6_L2_DBUS_BUSY_RD = 0x23;
	public final static int P6_BUS_DRDY_CLOCKS = 0x62;
	public final static int P6_BUS_LOCK_CLOCKS = 0x63;
	public final static int P6_BUS_REQ_OUTSTANDING = 0x60;
	public final static int P6_BUS_TRAN_BRD = 0x65;
	public final static int P6_BUS_TRAN_RFO = 0x66;
	public final static int P6_BUS_TRANS_WB = 0x67;
	public final static int P6_BUS_TRAN_IFETCH = 0x68;
	public final static int P6_BUS_TRAN_INVAL = 0x69;
	public final static int P6_BUS_TRAN_PWR = 0x6a;
	public final static int P6_BUS_TRANS_P = 0x6b;
	public final static int P6_BUS_TRANS_IO = 0x6c;
	public final static int P6_BUS_TRANS_DEF = 0x6d;
	public final static int P6_BUS_TRAN_BURST = 0x6e;
	public final static int P6_BUS_TRAN_ANY = 0x70;
	public final static int P6_BUS_TRAN_MEM = 0x6f;
	public final static int P6_BUS_DATA_RCV = 0x64;
	public final static int P6_BUS_BNR_DRV = 0x61;
	public final static int P6_BUS_HIT_DRV = 0x7a;
	public final static int P6_BUS_HITM_DRV = 0x7b;
	public final static int P6_BUS_SNOOP_STALL = 0x7e;
	public final static int P6_FLOPS = 0xc1;
	public final static int P6_FP_COMP_OPS_EXE = 0x10;
	public final static int P6_FP_ASSIST = 0x11;
	public final static int P6_MUL = 0x12;
	public final static int P6_DIV = 0x13;
	public final static int P6_CYCLES_DIV_BUSY = 0x14;
	public final static int P6_LD_BLOCKS = 0x03;
	public final static int P6_SB_DRAINS = 0x04;
	public final static int P6_MISALIGN_MEM_REF = 0x05;
	public final static int P6_INST_RETIRED = 0xc0;
	public final static int P6_UOPS_RETIRED = 0xc2;
	public final static int P6_INST_DECODER = 0xd0;
	public final static int P6_HW_INT_RX = 0xc8;
	public final static int P6_CYCLES_INT_MASKED = 0xc6;
	public final static int P6_CYCLES_INT_PENDING_AND_MASKED = 0xc7;
	public final static int P6_BR_INST_RETIRED = 0xc4;    
	public final static int P6_BR_MISS_PRED_RETIRED = 0xc5;
	public final static int P6_BR_TAKEN_RETIRED = 0xc9;
	public final static int P6_BR_MISS_PRED_TAKEN_RET = 0xca;
	public final static int P6_BR_INST_DECODED = 0xe0;
	public final static int P6_BTB_MISSES = 0xe2;
	public final static int P6_BR_BOGUS = 0xe4;
	public final static int P6_BACLEARS = 0xe6;
	public final static int P6_RESOURCE_STALLS = 0xa2;
	public final static int P6_PARTIAL_RAT_STALLS = 0xd2;
	public final static int P6_SEGMENT_REG_LOADS = 0x06;
	public final static int P6_CPU_CLK_UNHALTED = 0x79;

	private Hashtable byType;
	private Hashtable byName;
	
	public class VMonEvent {
		private int event_class;
		private int event_type;
		private String event_name;
		private String event_description;
	
		public VMonEvent(int ev_class, int ev_type, String name, String desc) {
			event_class = ev_class;
			event_type = ev_type;
			event_name = name;
			event_description = desc;
		}
		
		public int getEventClass() {
			return event_class;
		}
		
		public int getEventType() {
			return event_type;
		}
		
		public String getEventName() {
			return event_name;
		}
		
		public String getEventDescription() {
			return event_description;
		}
	}
	
	VMonEvents() {
		byName = new Hashtable(136);
		byType = new Hashtable(136);
		
		addEvent(VMON_PROF, 0, "PROF","SVr4 profile");
		addEvent(VMON_PAPI, PAPI_L1_DCM,  "PAPI_L1_DCM", "Level 1 data cache misses");
		addEvent(VMON_PAPI, PAPI_L1_ICM,  "PAPI_L1_ICM", "Level 1 instruction cache misses");
		addEvent(VMON_PAPI, PAPI_L2_DCM,  "PAPI_L2_DCM", "Level 2 data cache misses");
		addEvent(VMON_PAPI, PAPI_L2_ICM,  "PAPI_L2_ICM", "Level 2 instruction cache misses");
		addEvent(VMON_PAPI, PAPI_L3_DCM,  "PAPI_L3_DCM", "Level 3 data cache misses");
		addEvent(VMON_PAPI, PAPI_L3_ICM,  "PAPI_L3_ICM", "Level 3 instruction cache misses");
		addEvent(VMON_PAPI, PAPI_L1_TCM,  "PAPI_L1_TCM", "Level 1 total cache misses");
		addEvent(VMON_PAPI, PAPI_L2_TCM,  "PAPI_L2_TCM", "Level 2 total cache misses");
		addEvent(VMON_PAPI, PAPI_L3_TCM,  "PAPI_L3_TCM", "Level 3 total cache misses");
		addEvent(VMON_PAPI, PAPI_CA_SNP,  "PAPI_CA_SNP", "Snoops");
		addEvent(VMON_PAPI, PAPI_CA_SHR,  "PAPI_CA_SHR", "Request access to shared cache line (SMP)");
		addEvent(VMON_PAPI, PAPI_CA_CLN,  "PAPI_CA_CLN", "Request access to clean  cache line (SMP)");
		addEvent(VMON_PAPI, PAPI_CA_INV,  "PAPI_CA_INV", "Cache Line Invalidation (SMP)");
		addEvent(VMON_PAPI, PAPI_CA_ITV,  "PAPI_CA_ITV", "Cache Line Intervention (SMP)");
		
		addEvent(VMON_PAPI, PAPI_L3_LDM,  "PAPI_L3_LDM", "Level 3 load misses ");
		addEvent(VMON_PAPI, PAPI_L3_STM,  "PAPI_L3_STM", "Level 3 store misses ");
		addEvent(VMON_PAPI, PAPI_BRU_IDL, "PAPI_BRU_IDL", "Cycles branch units are idle");
		addEvent(VMON_PAPI, PAPI_FXU_IDL, "PAPI_FXU_IDL", "Cycles integer units are idle");
		addEvent(VMON_PAPI, PAPI_FPU_IDL, "PAPI_FPU_IDL", "Cycles floating point units are idle");
		addEvent(VMON_PAPI, PAPI_LSU_IDL, "PAPI_LSU_IDL", "Cycles load/store units are idle");
		addEvent(VMON_PAPI, PAPI_TLB_DM,  "PAPI_TLB_DM",  "Data translation lookaside buffer misses");
		addEvent(VMON_PAPI, PAPI_TLB_IM,  "PAPI_TLB_IM", "Instr translation lookaside buffer misses");
		addEvent(VMON_PAPI, PAPI_TLB_TL,  "PAPI_TLB_TL", "Total translation lookaside buffer misses");
		addEvent(VMON_PAPI, PAPI_L1_LDM,  "PAPI_L1_LDM", "Level 1 load misses ");
		addEvent(VMON_PAPI, PAPI_L1_STM,  "PAPI_L1_STM", "Level 1 store misses ");
		addEvent(VMON_PAPI, PAPI_L2_LDM,  "PAPI_L2_LDM", "Level 2 load misses ");
		addEvent(VMON_PAPI, PAPI_L2_STM,  "PAPI_L2_STM", "Level 2 store misses ");
		addEvent(VMON_PAPI, PAPI_L1_DCH,  "PAPI_L1_DCH", "Level 1 D cache hits");
		addEvent(VMON_PAPI, PAPI_L2_DCH,  "PAPI_L2_DCH", "Level 2 D cache hits");
		addEvent(VMON_PAPI, PAPI_L3_DCH,  "PAPI_L3_DCH", "Level 3 D cache hits");
		addEvent(VMON_PAPI, PAPI_TLB_SD,  "PAPI_TLB_SD", "Xlation lookaside buffer shootdowns (SMP)");
		addEvent(VMON_PAPI, PAPI_CSR_FAL, "PAPI_CSR_FAL", "Failed store conditional instructions");
		addEvent(VMON_PAPI, PAPI_CSR_SUC, "PAPI_CSR_SUC", "Successful store conditional instructions");
		addEvent(VMON_PAPI, PAPI_CSR_TOT, "PAPI_CSR_TOT", "Total store conditional instructions");
		addEvent(VMON_PAPI, PAPI_MEM_SCY, "PAPI_MEM_SCY", "Cycles Stalled Waiting for Memory Access");
		addEvent(VMON_PAPI, PAPI_MEM_RCY, "PAPI_MEM_RCY", "Cycles Stalled Waiting for Memory Read");
		addEvent(VMON_PAPI, PAPI_MEM_WCY, "PAPI_MEM_WCY", "Cycles Stalled Waiting for Memory Write");
		addEvent(VMON_PAPI, PAPI_STL_ICY, "PAPI_STL_ICY", "Cycles with No Instruction Issue");
		addEvent(VMON_PAPI, PAPI_FUL_ICY, "PAPI_FUL_ICY", "Cycles with Maximum Instruction Issue");
		addEvent(VMON_PAPI, PAPI_STL_CCY, "PAPI_STL_CCY", "Cycles with No Instruction Completion");
		addEvent(VMON_PAPI, PAPI_FUL_CCY, "PAPI_FUL_CCY", "Cycles with Maximum Instruction Completion");
		addEvent(VMON_PAPI, PAPI_HW_INT,  "PAPI_HW_INT", "Hardware interrupts ");
		addEvent(VMON_PAPI, PAPI_BR_UCN,  "PAPI_BR_UCN", "Unconditional branch instructions executed");
		addEvent(VMON_PAPI, PAPI_BR_CN,   "PAPI_BR_CN", "Conditional branch instructions executed");
		addEvent(VMON_PAPI, PAPI_BR_TKN,  "PAPI_BR_TKN", "Conditional branch instructions taken");
		addEvent(VMON_PAPI, PAPI_BR_NTK,  "PAPI_BR_NTK", "Conditional branch instructions not taken");
		addEvent(VMON_PAPI, PAPI_BR_MSP,  "PAPI_BR_MSP", "Conditional branch instructions mispred");
		addEvent(VMON_PAPI, PAPI_BR_PRC,  "PAPI_BR_PRC", "Conditional branch instructions corr. pred");
		addEvent(VMON_PAPI, PAPI_FMA_INS, "PAPI_FMA_INS", "FMA instructions completed");
		addEvent(VMON_PAPI, PAPI_TOT_IIS, "PAPI_TOT_IIS", "Total instructions issued");
		addEvent(VMON_PAPI, PAPI_TOT_INS, "PAPI_TOT_INS", "Total instructions executed");
		addEvent(VMON_PAPI, PAPI_INT_INS, "PAPI_INT_INS", "Integer instructions executed");
		addEvent(VMON_PAPI, PAPI_FP_INS,  "PAPI_FP_INS", "Floating point instructions executed");
		addEvent(VMON_PAPI, PAPI_LD_INS,  "PAPI_LD_INS", "Load instructions executed");
		addEvent(VMON_PAPI, PAPI_SR_INS,  "PAPI_SR_INS", "Store instructions executed");
		addEvent(VMON_PAPI, PAPI_BR_INS,  "PAPI_BR_INS", "Total branch instructions executed");
		addEvent(VMON_PAPI, PAPI_VEC_INS, "PAPI_VEC_INS", "Vector/SIMD instructions executed");
		addEvent(VMON_PAPI, PAPI_FML_INS, "PAPI_FML_INS", "Floating point multiply instructions");
		addEvent(VMON_PAPI, PAPI_FAD_INS, "PAPI_FAD_INS", "Floating point add instructions");
		addEvent(VMON_PAPI, PAPI_FDV_INS, "PAPI_FDV_INS", "Floating point divide instructions");
		addEvent(VMON_PAPI, PAPI_FNV_INS, "PAPI_FNV_INS", "Floating point inverse instructions");
		addEvent(VMON_PAPI, PAPI_FP_OPS,  "PAPI_FP_OPS", "Floating point operations");
		addEvent(VMON_PAPI, PAPI_RES_STL, "PAPI_RES_STL", "Any resource stalls");
		addEvent(VMON_PAPI, PAPI_FP_STAL, "PAPI_FP_STAL", "FP units are stalled ");
		addEvent(VMON_PAPI, PAPI_TOT_CYC, "PAPI_TOT_CYC", "Total cycles");
		addEvent(VMON_PAPI, PAPI_LST_INS, "PAPI_LST_INS", "Total load/store inst. executed");
		addEvent(VMON_PAPI, PAPI_SYC_INS, "PAPI_SYC_INS", "Sync. inst. executed ");


		addEvent(VMON_P6, P6_DATA_MEM_REFS, "P6_DATA_MEM_REFS", "all memory references, cachable and noncachable");
		addEvent(VMON_P6, P6_DCU_LINES_IN, "P6_DCU_LINES_IN", "total lines allocated in the DCU");
		addEvent(VMON_P6, P6_DCU_M_LINES_IN, "P6_DCU_M_LINES_IN", "number of M state lines allocated in DCU");
		addEvent(VMON_P6, P6_DCU_M_LINES_OUT, "P6_DCU_M_LINES_OUT", "number of M state lines evicted from the DCU");
		addEvent(VMON_P6, P6_DCU_MISS_OUTSTANDING, "P6_DCU_MISS_OUTSTANDING", "weighted number of cycles while DCU miss outstanding");
		addEvent(VMON_P6, P6_IFU_IFETCH, "P6_IFU_IFETCH", "number of cachable and noncachable instruction fetches");
		addEvent(VMON_P6, P6_IFU_IFETCH_MISS, "P6_IFU_IFETCH_MISS", "number of instruction fetch misses");
		addEvent(VMON_P6, P6_ITLB_MISS, "P6_ITLB_MISS", "number of ITLB misses");
		addEvent(VMON_P6, P6_IFU_MEM_STALL,  "P6_IFU_MEM_STALL", "cycles instruction fetch pipe is stalled");
		addEvent(VMON_P6, P6_ILD_STALL, "P6_ILD_STALL", "cycles instruction length decoder is stalled");
		addEvent(VMON_P6, P6_L2_IFETCH, "P6_L2_IFETCH", "number of L2 instruction fetches");
		addEvent(VMON_P6, P6_L2_LD,  "P6_L2_LD", "number of L2 data loads");
		addEvent(VMON_P6, P6_L2_ST, "P6_L2_ST", "number of L2 data stores");
		addEvent(VMON_P6, P6_L2_LINES_IN, "P6_L2_LINES_IN", "number of lines allocated in L2");
		addEvent(VMON_P6, P6_L2_LINES_OUT, "P6_L2_LINES_OUT", "number of lines removed from L2");
		addEvent(VMON_P6, P6_L2_M_LINES_INM, "P6_L2_M_LINES_INM", "number of modified lines allocated in L2");
		addEvent(VMON_P6, P6_L2_M_LINES_OUTM, "P6_L2_M_LINES_OUTM", "number of modified lines removed from L2");
		addEvent(VMON_P6, P6_L2_RQSTS, "P6_L2_RQSTS", "number of L2 requests");
		addEvent(VMON_P6, P6_L2_ADS, "P6_L2_ADS", "number of L2 address strobes");
		addEvent(VMON_P6, P6_L2_DBUS_BUSY, "P6_L2_DBUS_BUSY", "number of cycles data bus was busy");
		addEvent(VMON_P6, P6_L2_DBUS_BUSY_RD, "P6_L2_DBUS_BUSY_RD", "cycles data bus was busy in xfer from L2 to CPU");
		addEvent(VMON_P6, P6_BUS_DRDY_CLOCKS, "P6_BUS_DRDY_CLOCKS", "number of clocks DRDY is asserted");
		addEvent(VMON_P6, P6_BUS_LOCK_CLOCKS, "P6_BUS_LOCK_CLOCKS", "number of clocks LOCK is asserted");
		addEvent(VMON_P6, P6_BUS_REQ_OUTSTANDING, "P6_BUS_REQ_OUTSTANDING", "number of bus requests outstanding");
		addEvent(VMON_P6, P6_BUS_TRAN_BRD, "P6_BUS_TRAN_BRD", "number of burst read transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_RFO, "P6_BUS_TRAN_RFO", "number of read for ownership transactions");
		addEvent(VMON_P6, P6_BUS_TRANS_WB, "P6_BUS_TRANS_WB", "number of write back transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_IFETCH, "P6_BUS_TRAN_IFETCH", "number of instruction fetch transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_INVAL, "P6_BUS_TRAN_INVAL", "number of invalidate transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_PWR, "P6_BUS_TRAN_PWR", "number of partial write transactions");
		addEvent(VMON_P6, P6_BUS_TRANS_P, "P6_BUS_TRANS_P", "number of partial transactions");
		addEvent(VMON_P6, P6_BUS_TRANS_IO, "P6_BUS_TRANS_IO", "number of I/O transactions");
		addEvent(VMON_P6, P6_BUS_TRANS_DEF, "P6_BUS_TRANS_DEF", "number of deferred transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_BURST, "P6_BUS_TRAN_BURST", "number of burst transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_ANY, "P6_BUS_TRAN_ANY", "number of all transactions");
		addEvent(VMON_P6, P6_BUS_TRAN_MEM, "P6_BUS_TRAN_MEM", "number of memory transactions");
		addEvent(VMON_P6, P6_BUS_DATA_RCV, "P6_BUS_DATA_RCV", "bus cycles this processor is receiving data");
		addEvent(VMON_P6, P6_BUS_BNR_DRV, "P6_BUS_BNR_DRV", "bus cycles this processor is driving BNR pin");
		addEvent(VMON_P6, P6_BUS_HIT_DRV, "P6_BUS_HIT_DRV", "bus cycles this processor is driving HIT pin");
		addEvent(VMON_P6, P6_BUS_HITM_DRV, "P6_BUS_HITM_DRV", "bus cycles this processor is driving HITM pin");
		addEvent(VMON_P6, P6_BUS_SNOOP_STALL, "P6_BUS_SNOOP_STALL", "cycles during bus snoop stall");
		addEvent(VMON_P6, P6_FLOPS, "P6_FLOPS", "number of computational FP operations retired");
		addEvent(VMON_P6, P6_FP_COMP_OPS_EXE, "P6_FP_COMP_OPS_EXE", "number of computational FP operations executed");
		addEvent(VMON_P6, P6_FP_ASSIST, "P6_FP_ASSIST", "number of FP execptions handled by microcode");
		addEvent(VMON_P6, P6_MUL, "P6_MUL", "number of multiplies");
		addEvent(VMON_P6, P6_DIV, "P6_DIV", "number of divides");
		addEvent(VMON_P6, P6_CYCLES_DIV_BUSY, "P6_CYCLES_DIV_BUSY", "cycles divider is busy");
		addEvent(VMON_P6, P6_LD_BLOCKS, "P6_LD_BLOCKS", "number of store buffer blocks");
		addEvent(VMON_P6, P6_SB_DRAINS, "P6_SB_DRAINS", "number of store buffer drain cycles");
		addEvent(VMON_P6, P6_MISALIGN_MEM_REF, "P6_MISALIGN_MEM_REF", "number of misaligned data memory references");
		addEvent(VMON_P6, P6_INST_RETIRED, "P6_INST_RETIRED", "number of instructions retired");
		addEvent(VMON_P6, P6_UOPS_RETIRED, "P6_UOPS_RETIRED", "number of UOPs retired");
		addEvent(VMON_P6, P6_INST_DECODER, "P6_INST_DECODER", "number of instructions decoded");
		addEvent(VMON_P6, P6_HW_INT_RX, "P6_HW_INT_RX", "number of hardware interrupts received");
		addEvent(VMON_P6, P6_CYCLES_INT_MASKED, "P6_CYCLES_INT_MASKED", "cycles interrupts are disabled");
		addEvent(VMON_P6, P6_CYCLES_INT_PENDING_AND_MASKED, "P6_CYCLES_INT_PENDING_AND_MASKED", "cycles interrupts are disabled with pending interrupts");
		addEvent(VMON_P6, P6_BR_INST_RETIRED, "P6_BR_INST_RETIRED", "number of branch instructions retired");    
		addEvent(VMON_P6, P6_BR_MISS_PRED_RETIRED, "P6_BR_MISS_PRED_RETIRED", "number of mispredicted branches retired");
		addEvent(VMON_P6, P6_BR_TAKEN_RETIRED, "P6_BR_TAKEN_RETIRED", "number of taken branches retired");
		addEvent(VMON_P6, P6_BR_MISS_PRED_TAKEN_RET, "P6_BR_MISS_PRED_TAKEN_RET", "number of taken mispredicted branches retired");
		addEvent(VMON_P6, P6_BR_INST_DECODED,  "P6_BR_INST_DECODED", "number of branch instructions decoded");
		addEvent(VMON_P6, P6_BTB_MISSES, "P6_BTB_MISSES", "number of branches that miss the BTB");
		addEvent(VMON_P6, P6_BR_BOGUS, "P6_BR_BOGUS", "number of bogus branches");
		addEvent(VMON_P6, P6_BACLEARS, "P6_BACLEARS", "number of times BACLEAR is asserted");
		addEvent(VMON_P6, P6_RESOURCE_STALLS, "P6_RESOURCE_STALLS", "cycles during resource related stalls");
		addEvent(VMON_P6, P6_PARTIAL_RAT_STALLS, "P6_PARTIAL_RAT_STALLS", "cycles or events for partial stalls");
		addEvent(VMON_P6, P6_SEGMENT_REG_LOADS, "P6_SEGMENT_REG_LOADS", "number of segment register loads");
		addEvent(VMON_P6, P6_CPU_CLK_UNHALTED, "P6_CPU_CLK_UNHALTED", "clocks processor is not halted");
	}
	
	private void addEvent(int ev_class, int ev_type, String name, String desc) {
		VMonEvent event = new VMonEvent(ev_class, ev_type, name, desc);
		byName.put(name, event);
		byType.put(new Integer(ev_type), event);
	}
	
	public VMonEvent findEventByName(String name) {
		return (VMonEvent)byName.get(name);
	}

	public VMonEvent findEventByType(int type) {
		return (VMonEvent)byName.get(new Integer(type));
	}
}
