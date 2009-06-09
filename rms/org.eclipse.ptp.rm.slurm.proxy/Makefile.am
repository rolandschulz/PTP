
bin_PROGRAMS =	ptp_slurm_proxy

ptp_slurm_proxy_SOURCES =	src/ptp_slurm_proxy.c src/srun_opt.h 
ptp_slurm_proxy_CFLAGS =	$(AM_CFLAGS)
ptp_slurm_proxy_CPPFLAS =  -I$(SLURMDIR)/include
ptp_slurm_proxy_LDFLAGS = -L$(SLURMDIR)/lib
ptp_slurm_proxy_LDADD = 	-lslurm -lpthread
