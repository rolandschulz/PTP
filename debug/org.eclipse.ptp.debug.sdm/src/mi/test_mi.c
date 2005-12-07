#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIString.h"
#include "MIResultRecord.h"
#include "MIOutput.h"

int main(int argc, char *argv[])
{
	char *line;
	char *p;
	int needs_nl = 0;
	size_t len;
	MIOutput *out;
	MIString *str;
	
	FILE *fp = fopen("src/mi/test.input", "r");
	while ((p = fgetln(fp, &len)) != NULL) {
		if (p[len-1] != '\n')
			needs_nl = 1;
		line = malloc(len+needs_nl+1);
		memcpy(line, p, len+needs_nl);
		if (needs_nl)
			line[len] = '\n';
		line[len+needs_nl] = '\0'; 
		printf("line is <%s>", line);
		out = MIParse(line);
		if (out->rr != NULL) {
			str = MIResultRecordToString(out->rr);
			printf("rr = %s\n", MIStringToCString(str));
			MIStringFree(str);
		}
		MIOutputFree(out);
	}
	return 0;
}