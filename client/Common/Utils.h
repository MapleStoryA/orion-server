#include <windows.h>

namespace Utils {
	DWORD GetFuncAddress(LPCSTR lpModule, LPCSTR lpFunc)
	{
		auto mod = LoadLibraryA(lpModule);

		if (!mod)
		{
			return 0;
		}

		auto address = (DWORD)GetProcAddress(mod, lpFunc);

		return (DWORD)GetProcAddress(mod, lpFunc);
	}
}

