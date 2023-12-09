#include <windows.h>
#include <intrin.h>
#include <functional>
#include <dbghelp.h>
#include "Config.h"
#include "StringPool.h"
#include "Hook\Hook.h"
#include "Memory\MemoryEdit.h"
#include "Common\Utils.h"
#include "CLog.h"


CLog *Logger = new CLog("./LOG.txt");

extern void clientPatches();


bool Hook_SetUnhandledExceptionFilter(bool bEnable)
{
	static auto _SetUnhandledExceptionFilter =
		decltype(&SetUnhandledExceptionFilter)(Utils::GetFuncAddress("KERNEL32", "SetUnhandledExceptionFilter"));


	decltype(&SetUnhandledExceptionFilter) Hook = [](LPTOP_LEVEL_EXCEPTION_FILTER lpFilter) -> LPTOP_LEVEL_EXCEPTION_FILTER
	{
		auto addy = (DWORD)_ReturnAddress();
		Logger->Log("[SetUnhandledExceptionFilter] RET [%#08x]", addy);

		return _SetUnhandledExceptionFilter(lpFilter);
	};

	return Hook::SetHook(bEnable, reinterpret_cast<void**>(&_SetUnhandledExceptionFilter), Hook);

}


class CMSException {
public:
	DWORD m_hr;
	void CMSException_(DWORD hr) {
		this->m_hr = hr;
		Logger->Log("Exception with code %d from [%#08x]", hr, (DWORD)_ReturnAddress());
	}
};

bool Hook_GetModuleFileNameW(bool bEnable) {
	static decltype(&GetModuleFileNameW) _GetModuleFileNameW = &GetModuleFileNameW;

	decltype(&GetModuleFileNameW) GetModuleFileNameW_Hook = [](HMODULE hModule, LPWSTR lpFileName, DWORD dwSize) -> DWORD {
		auto len = _GetModuleFileNameW(hModule, lpFileName, dwSize);
		Logger->Log("Loading module %s", lpFileName);
		/* Check to see if the length is invalid (zero) */
		if (!len) {
			/* Try again without the provided module for a fixed result */
			len = _GetModuleFileNameW(NULL, lpFileName, dwSize);
		}

		return len;
	};

	return Hook::SetHook(bEnable, reinterpret_cast<void**>(&_GetModuleFileNameW), GetModuleFileNameW_Hook);
}




bool Hook_CreateWindowExA(bool bEnable)
{
	static auto _CreateWindowExA = decltype(&CreateWindowExA)(Utils::GetFuncAddress("USER32", "CreateWindowExA"));

	decltype(&CreateWindowExA) Hook = [](DWORD dwExStyle, LPCTSTR lpClassName, LPCTSTR lpWindowName, DWORD dwStyle, int x, int y, int nWidth, int nHeight, HWND hWndParent, HMENU hMenu, HINSTANCE hInstance, LPVOID lpParam) -> HWND
	{
		auto windowName = lpWindowName;
		auto ret = (DWORD)_ReturnAddress();
		if (!strcmp(lpClassName, "StartUpDlgClass"))
		{

			//Start up.
		}
		else if (!strcmp(lpClassName, "MapleStoryClass"))
		{
			windowName = "MapleStory";
			Logger->Log("Creating MapleStory window.");
		}

		return _CreateWindowExA(dwExStyle, lpClassName, windowName, dwStyle, x, y, nWidth, nHeight, hWndParent, hMenu, hInstance, lpParam);
	};

	return Hook::SetHook(bEnable, reinterpret_cast<void**>(&_CreateWindowExA), Hook);
}

bool Hook_DirectInput8Create(bool bEnable)
{
	typedef HRESULT(WINAPI *pDirectInput8Create)(HINSTANCE, DWORD, REFIID, LPVOID*, LPUNKNOWN);
	static auto _DirectInput8Create =
		(pDirectInput8Create)(Utils::GetFuncAddress("DINPUT8", "DirectInput8Create"));

	pDirectInput8Create Hook = [](HINSTANCE hinst, DWORD dwVersion, REFIID riidltf, LPVOID * ppvOut, LPUNKNOWN punkOuter) -> HRESULT
	{
		auto addy = (DWORD)_ReturnAddress();
		auto delay = 1000;
		Sleep(delay);
		return _DirectInput8Create(hinst, dwVersion, riidltf, ppvOut, punkOuter);
	};

	return Hook::SetHook(bEnable, reinterpret_cast<void**>(&_DirectInput8Create), Hook);
}


void clientPatches() {
	// Hook and log all CMSExceptions.
	auto ptr_CmsException = &CMSException::CMSException_;
	MemoryEdit::hook(0x0056171C, reinterpret_cast<DWORD&>(ptr_CmsException));

	// Remove logo
	MemoryEdit::nop(0x006B1F7D, 21);
	// AP Check Removal (nAP > 200, "Please use AP")
	MemoryEdit::changeByte((BYTE*)0x00B82429, 0xEB);
	// Let GM/Admins Drop Items (default condition is 0x74/JE)
	MemoryEdit::changeByte((BYTE*)0x00531515, 0xEB);
	// Let GM/Admins Drop Mesos (default condition is 0x74/JE)
	MemoryEdit::changeByte((BYTE*)0x00917505, 0xEB);
	// Let GM/Admins Attack (default condition is 0x74/JE)
	MemoryEdit::changeByte((BYTE*)0x00A7B859, 0xEB);
	MemoryEdit::changeByte((BYTE*)0x00A838A8, 0xEB);
	MemoryEdit::changeByte((BYTE*)0x00A882E4, 0xEB);
	MemoryEdit::changeByte((BYTE*)0x00A95DA6, 0xEB);
	MemoryEdit::changeByte((BYTE*)0x00A8C554, 0xEB);
	// Delete Character No-PIC bypass (Fake PIC)
	MemoryEdit::changeByte((BYTE*)0x00675C15, 0xEB);

}


BOOL APIENTRY DllMain(HMODULE hModule,
	DWORD  ul_reason_for_call,
	LPVOID lpReserved
)
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
		Logger->Log("DLL attached to host executable.");
#if ENABLE_CONSOLE == 1
		AllocConsole();
		freopen("CONIN$", "r", stdin);
		freopen("CONOUT$", "w", stdout);
		freopen("CONOUT$", "w", stderr);
#endif
		clientPatches();
		Hook_SetUnhandledExceptionFilter(true);
		Hook_CreateWindowExA(true);
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
	return TRUE;
}

