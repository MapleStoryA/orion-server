#include <Windows.h>
#include "detours.h"

namespace Hook {
	BOOL SetHook(BOOL bInstall, PVOID* ppvTarget, PVOID pvDetour);
}