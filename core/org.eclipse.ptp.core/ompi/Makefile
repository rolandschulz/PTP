# SETUP ECLIPSE TO UNDERSTAND WHERE TO FIND THE JNILIB
## IN THE RUN.. FOR THE NEW INSTANCE OF ECLIPSE/PTP SET THE VM ARGUMENTS
#-Djava.library.path=${project_loc}/org.eclipse.ptp.core/ompi

UNAME = $(shell uname -s)

ifeq ($(UNAME), Darwin)
	BUILD_PLATFORM = darwin_powerpc
endif

ifndef BUILD_PLATFORM
	error Not supported on $(UNAME)
endif

include makefile.$(BUILD_PLATFORM)
