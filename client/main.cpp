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

extern void ApplyPatches();



void ApplyPatches() {
	int height = 800;
    int width = 600;
	// CWvsApp::CreateMainWindow
	MemoryEdit::writeInt(0x0099CB4E + 3, height);
	MemoryEdit::writeInt(0x0099CB55 + 3, width);
	MemoryEdit::writeByte(0x009A0757 + 6, (BYTE)0);
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
		ApplyPatches();
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
	return TRUE;
}

