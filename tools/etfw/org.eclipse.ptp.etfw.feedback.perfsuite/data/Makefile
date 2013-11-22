
SHELL=/bin/bash
all: build run process

build:
	mpicc -g -O2 -o testfeedback testfeedback.c

run:
	source ${HOME}/tmp/nightly-test/perfsuite-install/bin/psenv.sh; \
	mpirun -np 4 ${HOME}/tmp/nightly-test/perfsuite-install/bin/psrun -f -C -c papi_profile_cycles.xml ./testfeedback

process:
	${HOME}/tmp/nightly-test/perfsuite-install/bin/psprocess -x -o mhpr.xml testfeedback.*.xml

clean:
	rm -rf testfeedback mhpr.xml testfeedback.*.xml

