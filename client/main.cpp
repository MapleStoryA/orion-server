#include <windows.h>
#include <intrin.h>
#include <functional>
#include <dbghelp.h>
#include <iostream>
#include "Config.h"
#include "Hook\Hook.h"
#include "Memory\MemoryEdit.h"
#include "Common\Utils.h"
#include "ClientApp.h"


extern void ApplyPatches();

static int width = 1366;
static int height = 768;


int get_screen_width()
{
	return width;
}

int get_screen_height()
{
	return height;
}


static bool HookCreateWindowExA(bool bEnable) {
    static auto _CreateWindowExA = decltype(&CreateWindowExA)(Hook::GetFuncAddress("User32", "CreateWindowExA"));
	decltype(&CreateWindowExA) Hook = [](DWORD dwExStyle, LPCTSTR lpClassName, LPCTSTR lpWindowName, DWORD dwStyle, int x, int y, int nWidth, int nHeight, HWND hWndParent, HMENU hMenu, HINSTANCE hInstance, LPVOID lpParam) -> HWND {
		auto windowName = lpWindowName;
		auto hwnd = _CreateWindowExA(dwExStyle, lpClassName, windowName, WS_MINIMIZEBOX, x, y, nWidth, nHeight, hWndParent, hMenu, hInstance, lpParam);
		RECT rect;
		
		GetWindowRect(hwnd, &rect);
		
		int screenWidth = GetSystemMetrics(SM_CXSCREEN);
		int screenHeight = GetSystemMetrics(SM_CYSCREEN);
		int windowWidth = rect.right - rect.left;
		int windowHeight = rect.bottom - rect.top;
		int centerX = (screenWidth - windowWidth) / 2;
		int centerY = (screenHeight - windowHeight) / 2;
		
		SetWindowPos(hwnd, NULL, centerX, centerY, 0, 0, SWP_NOZORDER);
		return hwnd;

	};
    return Hook::SetHook(bEnable, reinterpret_cast<void**>(&_CreateWindowExA), Hook);
}


void PatchWindowsMode() {
	// on CWvsApp::SetUp(_DWORD *this) set the pointer to start in Window mode
	// above InitializePCOM
	MemoryEdit::writeByte(0x009A0757 + 6, 0);
}

void PatchScreenSize() {
	// Hook screen size
	DWORD TSingleton_CWvsContext___ms_pInstance = 0x00C2EFA4;
	MemoryEdit::writeInt(TSingleton_CWvsContext___ms_pInstance + 16236, height);
	MemoryEdit::writeInt(TSingleton_CWvsContext___ms_pInstance + 16232, width);

	MemoryEdit::hookCall((DWORD*)0x00936FA0, (DWORD)&get_screen_width);
	MemoryEdit::ret(0x00936FA5, 0);
	MemoryEdit::hookCall((DWORD*)0x00936FB0, (DWORD)&get_screen_height);
	MemoryEdit::ret(0x00936FB5, 0);
}



void ApplyPatches() {
	PatchWindowsMode();
	PatchScreenSize();
	HookCreateWindowExA(true);

	// 5
	MemoryEdit::writeInt(0x0044309D + 1, height);
	MemoryEdit::writeInt(0x004430A2 + 1, width);
	// 6
	MemoryEdit::writeInt(0x00443193 + 1, height);
	MemoryEdit::writeInt(0x00443198 + 1, width);
	// 7
	MemoryEdit::writeInt(0x00443228 + 1, -height / 2);
	MemoryEdit::writeInt(0x0044322D + 1, -width / 2);
	// 8
	MemoryEdit::writeInt(0x00444517 + 1, height);
	MemoryEdit::writeInt(0x0044451C + 1, width);
	// 9
	MemoryEdit::writeInt(0x0044460B + 1, height);
	MemoryEdit::writeInt(0x00444610 + 1, width);
	// 10
	MemoryEdit::writeInt(0x004446A4 + 1, -height / 2);
	MemoryEdit::writeInt(0x004446AC + 1, -width / 2);
	// 11
	MemoryEdit::writeInt(0x00457D6F + 1, height / 2);
	MemoryEdit::writeInt(0x00457D76 + 1, width / 2);
	// 12
	MemoryEdit::writeInt(0x00457F64 + 1, height / 2);
	MemoryEdit::writeInt(0x00457F6B + 1, width / 2);

	// 13 => Does a ret, what for?
	// 14 
	MemoryEdit::writeInt(0x0046B3CC + 1, width - 225);
	// 15
	MemoryEdit::writeInt(0x0046B50F + 1, width + 100);
	// 16
	MemoryEdit::writeInt(0x0046C0FF + 1, width + 100);
	// 17
	MemoryEdit::writeInt(0x0046C243 + 1, width - 225);
	// 18 => Does a jump, what for?
	// 19 => same
	// 20 / 21
	MemoryEdit::writeInt(0x00486BBE + 1, height);
	MemoryEdit::writeInt(0x00486BD1 + 1, width);
    // 22
	MemoryEdit::writeInt(0x00490E4B + 1, 716);
	// 23
	MemoryEdit::writeInt(0x004932D2 + 1, 716);
	// 24 => nopes
	// 25 => nopes
	// 26 => nopes
	// 27 => nopes
	// 28 => nopes
	// 29
	MemoryEdit::writeInt(0x004A5C76 + 1, width);
	// 30 => jump
	// 31 => jump
	MemoryEdit::writeInt(0x004A5C76 + 1, width);
	// 32 => something with version
	//MemoryEdit::writeByte(0x004AB902, 8);
	// 33 => something with version
	// 34
	MemoryEdit::writeInt(0x004DCD5A + 4, width);
	// 35, 36
	MemoryEdit::writeInt(0x004DCEE0 + 2, width);
	MemoryEdit::writeInt(0x004DCEE8 + 1, width);
	MemoryEdit::writeInt(0x004DCEF6 + 2, height);
	// 37 => jmp
	// 38
	MemoryEdit::writeInt(0x004E28FA + 2, width);
	// 39,40
	MemoryEdit::writeInt(0x004E2A88 + 1, width);
	MemoryEdit::writeInt(0x004E2A80 + 2, width);
	MemoryEdit::writeInt(0x004E2A96 + 2, height);
	// 41
	MemoryEdit::writeInt(0x004EC596 + 1, height / 2);
	MemoryEdit::writeInt(0x004EC59B + 1, width / 2);
	// 42, 43
	MemoryEdit::writeInt(0x004F9998 + 1, height);
	MemoryEdit::writeInt(0x004F99A6 + 1, width);
	// 44
	MemoryEdit::writeInt(0x004F9FB8 + 1, height);
	MemoryEdit::writeInt(0x004F9FBF + 1, width);
	// 45 => JUMP
	// 46 => JUMP
	// 47 => JUMP
	// 48, 49 => JUMP
	// 50 => JUMP
	// 51 => 65 NOP
	// 66 => JMP
	// 67 / 68 / 69 => 80 => NOP
	// 81
	MemoryEdit::writeInt(0x00514E08 + 1, height);
	// 82, 83
	MemoryEdit::writeInt(0x0051EF5C + 1, 660);
	MemoryEdit::writeInt(0x0051EF64 + 1, 979);
    MemoryEdit::writeInt(0x0051F228 + 1, 660);
	MemoryEdit::writeInt(0x0051F230 + 1, 979);
	// 84
	MemoryEdit::writeInt(0x0051F50C + 1, 676);
	MemoryEdit::writeInt(0x0051F513 + 1, 1030);
	// 85
	MemoryEdit::writeInt(0x0051F51C + 1, 660);
	MemoryEdit::writeInt(0x0051F521 + 1, 979);
	// 86
	MemoryEdit::writeInt(0x0051F655 + 1, 666);
	MemoryEdit::writeInt(0x0051F65B + 1, 1029);
    // 87 - 90
	MemoryEdit::writeInt(0x0051F930 + 1, 979);
	MemoryEdit::writeInt(0x0051F928 + 1, 660);

	MemoryEdit::writeInt(0x0051FC00 + 1, 979);
	MemoryEdit::writeInt(0x0051FBF8 + 1, 660);

	MemoryEdit::writeInt(0x0051FED8 + 1, 979);
	MemoryEdit::writeInt(0x0051FED0 + 1, 660);

	MemoryEdit::writeInt(0x005201A0 + 1, 979);
	MemoryEdit::writeInt(0x00520198 + 1, 660);
	// 91 - 94
	MemoryEdit::writeInt(0x005204E4 + 1, 676);
	MemoryEdit::writeInt(0x005204EC + 1, 1030);
	MemoryEdit::writeInt(0x0052079D + 1, 660);
	MemoryEdit::writeInt(0x005207A5 + 1, 660);
	MemoryEdit::writeInt(0x00520A52 + 1, 660);
	MemoryEdit::writeInt(0x00520A5A + 1, 979);
	MemoryEdit::writeInt(0x00520D9E + 1, 676);
	MemoryEdit::writeInt(0x00520DA6 + 1, 1030);
	// 95, 96
	MemoryEdit::writeInt(0x0052104B + 1, 676);
	MemoryEdit::writeInt(0x00521069 + 1, 1030);
	// 97
	MemoryEdit::writeInt(0x00521322 + 1, 676);
	MemoryEdit::writeInt(0x0052132A + 1, 1030);
	// 98
	MemoryEdit::writeInt(0x005215F8 + 1, 660);
	MemoryEdit::writeInt(0x00521600 + 1, 979);
	// 99
	MemoryEdit::writeInt(0x005240E1 + 1, 666);
	MemoryEdit::writeInt(0x005240E9 + 1, 1030);
	// 100 JMP
	// 101 NOP
	// 102 NOP
	// 103 NOP
	// 104 
	MemoryEdit::writeInt(0x00529D6F + 1, 554);
	// 105
	MemoryEdit::writeInt(0x00529D99 + 1, 554);
	// 106
	MemoryEdit::writeInt(0x0052BC76 + 1, width);
	// 107 108 nop
	// 109
	MemoryEdit::writeInt(0x00530682 + 1, width);
	// 110
	MemoryEdit::writeInt(0x005312CE + 1, 1359);
	// 111
	MemoryEdit::writeInt(0x00531C2F + 1, width / 2);
	// 112
	MemoryEdit::writeInt(0x00533F92 + 1, width);
	MemoryEdit::writeInt(0x00533F8D + 1, height);
	// 113
	MemoryEdit::writeInt(0x005344F8 + 1, -height / 2);
	MemoryEdit::writeInt(0x00534500 + 1, -width / 2);
	// 114
	MemoryEdit::writeInt(0x0053496C + 1, height);
	MemoryEdit::writeInt(0x00534971 + 1, width);
	// 115
	MemoryEdit::writeInt(0x0053499B + 1, 600);
	MemoryEdit::writeInt(0x00534996 + 1, 600);
	// 116
	MemoryEdit::writeInt(0x00534AA0 + 2, 264);
	MemoryEdit::writeInt(0x00534AA7 + 2, 617);
	// 117
	MemoryEdit::writeInt(0x00534C17 + 2, 264);
	MemoryEdit::writeInt(0x00534C1D + 2, 617);
	// 118
	MemoryEdit::writeInt(0x00540F93 + 1, width / 2);
	// 119
	MemoryEdit::writeInt(0x00548E33 + 1, 601);
	// 120
	MemoryEdit::writeInt(0x00549940 + 1, 452);
	// 121
	MemoryEdit::writeInt(0x005499FC + 1, width / 2);
	// 122
	MemoryEdit::writeInt(0x00549AB5 + 1, 914);
	// 123
	MemoryEdit::writeInt(0x00549DE5 + 1, 600);
	// 124
	MemoryEdit::writeInt(0x00549EA1 + 1, 765);
	// 125
	MemoryEdit::writeInt(0x0054A72B + 1, 628);
	// 126
	MemoryEdit::writeByte(0x0054DA66 + 1, 0x7F);
	// 127
	MemoryEdit::writeInt(0x0054DA7A + 2, 0x00000208);
	// 128
	MemoryEdit::writeByte(0x0054DD44 + 2, 0x7F);
	MemoryEdit::writeInt(0x0054DD48 + 2, 0x00000208);
	// 129
	MemoryEdit::writeByte(0x0054E007 + 2, 0x7F);
	MemoryEdit::writeInt(0x0054E00B + 2, 0x00000208);
	// 130
	MemoryEdit::writeInt(0x0054E1E6 + 1, height);
	MemoryEdit::writeInt(0x0054E1EB + 1, width);
	// 131
	MemoryEdit::writeInt(0x0054E281 + 1, height);
	MemoryEdit::writeInt(0x0054E286 + 1, width);
	// 132
	MemoryEdit::writeInt(0x0054E796 + 1, -width / 2);
	MemoryEdit::writeInt(0x0054E78E + 1, -height / 2);
	// 133
	MemoryEdit::writeInt(0x005603E7 + 1, width);
	// 134 => packet or something
	// 135 => same
	// 136
	MemoryEdit::writeInt(0x005694A4 + 2, -width / 2);
	MemoryEdit::writeInt(0x0056949D + 2, height / 2);
	// 137 / 138
	MemoryEdit::writeInt(0x0056977E + 1, width);
	MemoryEdit::writeInt(0x00569787 + 1, width);

	// 139
	MemoryEdit::writeInt(0x005697A0 + 1, height);
	MemoryEdit::writeInt(0x005697A7 + 1, height);

	// 140,141,142,143
	MemoryEdit::writeInt(0x0056A503 + 1, width);
	MemoryEdit::writeInt(0x0056A50D + 1, width);
	MemoryEdit::writeInt(0x0056A516 + 1, height);
	MemoryEdit::writeInt(0x0056A51F + 1, height);

	// 144
	MemoryEdit::writeInt(0x0056B491 + 2, width / 2);

	// 145
	MemoryEdit::writeInt(0x0056B497 + 6, width / 2);

	// 146,147,148,149
	MemoryEdit::writeInt(0x005739D4 + 1, height);
	MemoryEdit::writeInt(0x005739E7 + 1, width);
	MemoryEdit::writeInt(0x00574A56 + 1, height);
	MemoryEdit::writeInt(0x00574A69 + 1, width);
	// 150
 	MemoryEdit::writeInt(0x00575E19 + 1, width);
	MemoryEdit::writeInt(0x00575E06 + 1, height); // Aqui mouse começou a ir << e acima
	// 156
	MemoryEdit::writeInt(0x00577B36 + 1, height);
	MemoryEdit::writeInt(0x00577B49 + 1, width);
	// 154,155
	MemoryEdit::writeInt(0x00578D56 + 1, height);
	MemoryEdit::writeInt(0x00578D69 + 1, width);
	// 156, 157 => nope
	// 158,159
	MemoryEdit::writeInt(0x00586633 + 1, height);
	MemoryEdit::writeInt(0x00586646 + 1, width);
	// 160,161
	MemoryEdit::writeInt(0x00587D27 + 1, height);
	MemoryEdit::writeInt(0x00587D39 + 1, width);
	// 162 => unknow weird patching
	// 163 => hook
	// 164 => nope
	// 165 => nope
	// 167
	MemoryEdit::writeInt(0x005C26CF + 1, width);
	MemoryEdit::writeInt(0x005C26C7 + 1, height);
	// 168 => JUMP
	// 169
	MemoryEdit::writeInt(0x005CF269 + 1, -width / 2);
	MemoryEdit::writeInt(0x005CF271 + 1, -height / 2);
	// 170
	MemoryEdit::writeByteArray(0x005E4BBF, "2E 02 00 00 8b f1 b9 25 01 00 00 51 50 50 51");
	// 171 nope
	// 172
	MemoryEdit::writeInt(0x005E5774 + 1, height);
	MemoryEdit::writeInt(0x005E5779 + 1, width);
	// 173
	MemoryEdit::writeInt(0x005E80DA + 1, height);
	MemoryEdit::writeInt(0x005E80DF + 1, width);
	// 174 175
	MemoryEdit::writeInt(0x005E8188, 1273);
	MemoryEdit::writeInt(0x005E8452, 1280);
	// 176
	MemoryEdit::writeInt(0x005EBB33 + 1, height);
	MemoryEdit::writeInt(0x005EBB3A + 1, width);


	// CWvsApp::CreateWndManager(_DWORD *this)
	MemoryEdit::writeInt(0x00997A6D + 1, width);
	MemoryEdit::writeInt(0x00997A68 + 1, width);
	// CWvsApp::CreateMainWindow(_DWORD *this)
	MemoryEdit::writeInt(0x0099CB4E + 3, height);
	MemoryEdit::writeInt(0x0099CB55 + 3, width);


	/*
	// CWvsApp::CreateWndManager(_DWORD *this)
	MemoryEdit::writeInt(0x00997A6D + 1, width);
	MemoryEdit::writeInt(0x00997A68 + 1, width);
	// CWvsApp::CreateMainWindow(_DWORD *this)
	MemoryEdit::writeInt(0x0099CB4E + 3, height);
	MemoryEdit::writeInt(0x0099CB55 + 3, width);


	MemoryEdit::writeInt(0x004430A2 + 1, width);
	MemoryEdit::writeInt(0x0044309D + 1, height);

	MemoryEdit::writeInt(0x00443198 + 1, width);
	MemoryEdit::writeInt(0x00443193 + 1, height);

	MemoryEdit::writeInt(0x0044451C + 1, width);
	MemoryEdit::writeInt(0x0444517 + 1, height);

	MemoryEdit::writeInt(0x00444610 + 1, width);
	MemoryEdit::writeInt(0x0044460B + 1, height);

    MemoryEdit::writeInt(0x004446A4 + 1, -height / 2);
    MemoryEdit::writeInt(0x004446AC + 1, -width / 2);


	MemoryEdit::writeInt(0x00457D6F + 1, height);
    MemoryEdit::writeInt(0x00457D76 + 1, width);
    
	MemoryEdit::writeInt(0x00457F64 + 1, height / 2);
	MemoryEdit::writeInt(0x00457D6F + 1, width / 2);
	

	MemoryEdit::writeInt(0x00457F64 + 1, height / 2);
	MemoryEdit::writeInt(0x00457F6B + 1, width / 2);

	MemoryEdit::writeInt(0x0046B3CC + 1, 1141);
	MemoryEdit::writeInt(0x0046B50F + 1, 1466);
	MemoryEdit::writeInt(0x0046C0FF + 1, 1466);
	MemoryEdit::writeInt(0x0046C243 + 1, 1141);

	MemoryEdit::writeByte(0x0046D358, 0x75);

	MemoryEdit::writeInt(0x00486BBE, height);
	MemoryEdit::writeInt(0x00486BD1, width);
	MemoryEdit::writeInt(0x00490E4B, height / 2);
	MemoryEdit::writeInt(0x004932D2, height / 2);
	*/

#ifdef HD_CLIENT_ENABLED
	ClientApp::GetInstance()->AddLogMessage("Setting resolution to " + std::to_string(width) + " : " + std::to_string(height));
	PatchScreenSize();
	// CWvsApp::CreateWndManager(_DWORD *this)
	MemoryEdit::writeInt(0x00997A6D + 1, width);
	MemoryEdit::writeInt(0x00997A68 + 1, width);
	

	MemoryEdit::writeInt(0x0099CB4E + 3, height);
	MemoryEdit::writeInt(0x0099CB55 + 3, width);

	

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


	// CInputSystem::Init
	MemoryEdit::writeInt(0x0056B491 + 4, width / 2);
	MemoryEdit::writeInt(0x0056B497 + 4, height / 2);

	// CInputSystem::SetCursorVectorPos
	// MemoryEdit::writeInt(0x0056949D + 2, height / 2);
	// MemoryEdit::writeInt(0x0056949D + 2, height / 2);
	MemoryEdit::writeInt(0x00486BD1 + 1, width);
	MemoryEdit::writeInt(0x00486BBE + 1, height);

#endif

	// SecurityClient
	DWORD* TSingleton_CSecurityClient_ms_pInstance = reinterpret_cast<DWORD*>(0xC33D48);
	*TSingleton_CSecurityClient_ms_pInstance = NULL;
	MemoryEdit::ret(0x9EE3E0);
	//MemoryEdit::ret(0x9F30F0);
	//MemoryEdit::ret(0x009F9CF1);
	//MemoryEdit::ret(0x009EE390, 0xC);

	// Change the map in login to MapLogin1
	MemoryEdit::writeInt(0x005CE7F5 + 1, 0x20241020);
	// Remove NCOM
	MemoryEdit::ret(0x997AA0);
	// whatever
	//MemoryEdit::ret(0x5D6A90);
	// Patch CCrc32::GetCrc32
	MemoryEdit::ret(0x004B3640);

	
	
}

BOOL APIENTRY DllMain(HMODULE hModule,
	DWORD  ul_reason_for_call,
	LPVOID lpReserved
)
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
		#ifdef ENABLE_CLIENT_APP_WINDOW
				ClientApp::GetInstance()->Setup(hModule);
				ClientApp::GetInstance()->AddLogMessage("Starting Orion Client");
		#endif
		ApplyPatches();
		break;
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
	return TRUE;
}

