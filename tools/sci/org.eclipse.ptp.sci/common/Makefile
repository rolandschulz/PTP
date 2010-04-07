include ../makefile.define

CPPFLAGS = $(MY_FLAGS) -fPIC

ifeq ($(ARCH), i686)
    LIBRARY = libcmn.a
    LIBRARY64 = 
else
    LIBRARY = libcmn.a
    LIBRARY64 = libcmn64.a
endif

all : $(LIBRARY) $(LIBRARY64)

$(LIBRARY) : $(OBJS) 
	@$(AR) $(ARFLAGS) $(LIBRARY) $(OBJS)

$(LIBRARY64) : $(OBJS64)
	@$(AR) $(AR64FLAGS) $(LIBRARY64) $(OBJS64)

install: $(LIBRARY) $(LIBRARY64)

clean :
	rm -f $(LIBRARY) $(LIBRARY64) $(OBJS) $(OBJS64) core *.d

