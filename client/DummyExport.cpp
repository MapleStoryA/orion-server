#include "DummyExport.h"
#include <iostream>
__declspec(dllexport) void init_client() {
	printf("Initializing client...\n");
}