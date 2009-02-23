.PHONY: all clean
ALL_SRC=$(filter-out xlf-bug.f03 xlf-crash.f03, $(wildcard *.f03))

############ WHICH FILES ARE COMPILED DEPENDS ON THE COMPILER #############

XLF_SRC=$(ALL_SRC)

NAGFOR_SRC=$(filter-out 03-destructor.f03 string.f03, $(ALL_SRC))

###########################################################################

XLF   =`which xlf    2>/dev/null | head -1`
NAGFOR=`which nagfor 2>/dev/null | head -1`
DEBUGSWITCH=-g
OPTSWITCH=-O4
OUTSWITCH=-o

all:
	@if [ "$(NAGFOR)" != "" ]; then \
	    for file in $(NAGFOR_SRC); do \
	        $(NAGFOR)  $(DEBUGSWITCH) \
	                   $(OUTSWITCH) `echo $$file | sed -e 's/.f03/.exe/'` \
	                   $(OPTSWITCH) \
	                   $$file; \
	    done; \
	elif [ "$(XLF)" != "" ]; then \
	    for file in $(XLF_SRC); do \
	        $(XLF)     $(DEBUGSWITCH) \
	                   $(OUTSWITCH) `echo $$file | sed -e 's/.f03/.exe/'` \
	                   $(OPTSWITCH) \
	                   $$file; \
	    done; \
	fi

clean:
	rm -f *.exe *.mod