# gfortran 4.4.0 cannot parse sample.f, so only compile if Intel Fortran is available

.PHONY: all clean

SRC=sample.f

IFORT=`which ifort    2>/dev/null | head -1`
DEBUGSWITCH=-g
OPTSWITCH=-O4
OUTSWITCH=-o

all:
	@if [ "$(IFORT)" != "" ]; then \
	    for file in $(SRC); do \
	        $(IFORT)  $(DEBUGSWITCH) \
	                  $(OUTSWITCH) `echo $$file | sed -e 's/.f/.exe/'` \
	                  $(OPTSWITCH) \
	                  $$file; \
	    done; \
	fi

clean:
	rm -f *.exe *.mod