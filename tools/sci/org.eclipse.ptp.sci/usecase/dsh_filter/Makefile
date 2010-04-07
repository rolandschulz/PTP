CC = g++

CPPFLAGS = 

OTHER_FLAGS = -ldl -lpthread -lsci

all: all_32 all_64

all_32: dsh_fe dsh_be dsh_filter.so dsh_tests

all_64: dsh_fe64 dsh_be64 dsh_filter64.so dsh_tests64

dsh_fe: dsh_fe.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m32 $(CPPFLAGS) -o $@ dsh_fe.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_be: dsh_be.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m32 $(CPPFLAGS) -o $@ dsh_be.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_filter.so: dsh_filter.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m32 $(CPPFLAGS) -fpic -shared -o $@ dsh_filter.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_tests: dsh_tests.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m32 $(CPPFLAGS) -o $@ dsh_tests.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_fe64: dsh_fe.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m64 -D__64BIT__ $(CPPFLAGS) -o $@ dsh_fe.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_be64: dsh_be.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m64 -D__64BIT__ $(CPPFLAGS) -o $@ dsh_be.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_filter64.so: dsh_filter.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m64 -D__64BIT__ $(CPPFLAGS) -fpic -shared -o $@ dsh_filter.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

dsh_tests64: dsh_tests.cpp levenshtein.cpp dsh_packer.cpp dsh_header.hpp dsh_aggregator.hpp
	$(CC) -g -m64 -D__64BIT__ $(CPPFLAGS) -o $@ dsh_tests.cpp levenshtein.cpp dsh_packer.cpp $(OTHER_FLAGS)

clean: clean_32 clean_64

clean_32:
	@rm -f *.o dsh_fe dsh_be dsh_filter.so dsh_tests

clean_64:
	@rm -f *.o dsh_fe64 dsh_be64 dsh_filter64.so dsh_tests64
