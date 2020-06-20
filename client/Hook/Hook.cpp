#include "hook.h"

namespace Hook {

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