#include "hook.h"

namespace Hook {


	DWORD GetFuncAddress(LPCSTR lpModule, LPCSTR lpFunc)
	{
		auto mod = LoadLibraryA(lpModule);

		if (!mod) {
			return 0;
		}

		return (DWORD)GetProcAddress(mod, lpFunc);
	}


	BOOL SetHook(BOOL bInstall, PVOID* ppvTarget, PVOID pvDetour)
	{
		if (DetourTransactionBegin() != NO_ERROR)
		{
			return FALSE;
		}

		auto tid = GetCurrentThread();

		if (DetourUpdateThread(tid) == NO_ERROR)
		{
			auto func = bInstall ? DetourAttach : DetourDetach;

			if (func(ppvTarget, pvDetour) == NO_ERROR)
			{
				if (DetourTransactionCommit() == NO_ERROR)
				{
					return TRUE;
				}
			}
		}

		DetourTransactionAbort();
		return FALSE;
	}
}