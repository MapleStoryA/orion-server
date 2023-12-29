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

static int height = 1080;
static int width = 1980;


void PatchWindowsMode() {
	// on CWvsApp::SetUp(_DWORD *this) set the pointer to start in Window mode
	// above InitializePCOM
	MemoryEdit::writeByte(0x009A0757 + 6, 0);
}

int get_screen_width()
{
	return width;
}

int get_screen_height()
{
	return height;
}

void ApplyPatches(){
	PatchWindowsMode();
	// CWvsApp::CreateWndManager(_DWORD *this)
	MemoryEdit::writeInt(0x00997A6D + 1, height);
	MemoryEdit::writeInt(0x00997A68 + 1, width);
	// CWvsApp::CreateMainWindow(_DWORD *this)

	MemoryEdit::writeInt(0x0099CB4E + 3, height);
	MemoryEdit::writeInt(0x0099CB55 + 3, width);

	MemoryEdit::writeInt(0x004430A2 + 1, height);
	MemoryEdit::writeInt(0x0044309D + 1, width);

	MemoryEdit::writeInt(0x00443198 + 1, height);
	MemoryEdit::writeInt(0x00443193 + 1, width);

	MemoryEdit::writeInt(0x0044451C + 1, height);
	MemoryEdit::writeInt(0x00444517 + 1, width);

	MemoryEdit::writeInt(0x00444610 + 1, height);
	MemoryEdit::writeInt(0x0044460B + 1, width);

	MemoryEdit::writeInt(0x00533F92 + 1, height);
	MemoryEdit::writeInt(0x00533F8D + 1, width);

	MemoryEdit::writeInt(0x00534971 + 1, height);
	MemoryEdit::writeInt(0x0053496C + 1, width);

	MemoryEdit::writeInt(0x0054E1EB + 1, height);
	MemoryEdit::writeInt(0x0054E1E6 + 1, width);

	MemoryEdit::writeInt(0x0054E286 + 1, height);
	MemoryEdit::writeInt(0x0054E281 + 1, width);

	MemoryEdit::writeInt(0x005E5779 + 1, height);
	MemoryEdit::writeInt(0x005E5774 + 1, width);

	MemoryEdit::writeInt(0x005E80DF + 1, height);
	MemoryEdit::writeInt(0x005E80DA + 1, width);

	MemoryEdit::writeInt(0x00601170 + 1, height);
	MemoryEdit::writeInt(0x0060116B + 1, width);

	MemoryEdit::writeInt(0x006017DF + 1, height);
	MemoryEdit::writeInt(0x006017DA + 1, width);

	MemoryEdit::writeInt(0x00601E2E + 1, height);
	MemoryEdit::writeInt(0x00601E29 + 1, width);

	MemoryEdit::writeInt(0x0060223A + 1, height);
	MemoryEdit::writeInt(0x00602235 + 1, width);

	MemoryEdit::writeInt(0x006024FA + 1, height);
	MemoryEdit::writeInt(0x006024F5 + 1, width);

	MemoryEdit::writeInt(0x0075E146 + 1, height);
	MemoryEdit::writeInt(0x0075E141 + 1, width);

	MemoryEdit::writeInt(0x0082FB7E + 1, height);
	MemoryEdit::writeInt(0x0082FB79 + 1, width);

	MemoryEdit::writeInt(0x00857862 + 1, height);
	MemoryEdit::writeInt(0x0085785D + 1, width);

	MemoryEdit::writeInt(0x004430A2 + 1, height);
	MemoryEdit::writeInt(0x0044309D + 1, width);

	//
	DWORD TSingleton_CWvsContext___ms_pInstance = 0x00C2EFA4;
	MemoryEdit::writeInt(TSingleton_CWvsContext___ms_pInstance + 16236, height);
	MemoryEdit::writeInt(TSingleton_CWvsContext___ms_pInstance + 16232, width);

	MemoryEdit::hookCall((BYTE*)0x00936FA0, (DWORD)&get_screen_width);
	MemoryEdit::ret(0x00936FA5, 0);
	MemoryEdit::hookCall((BYTE*)0x00936FB0, (DWORD)&get_screen_height);
	MemoryEdit::ret(0x00936FB5, 0);

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

