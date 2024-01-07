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
#ifdef HD_CLIENT_ENABLED
	PatchWindowsMode();
	PatchScreenSize();
	HookCreateWindowExA(true);
	// 1 hook: BOOL __cdecl is_vehicle(signed int a1)
	
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
	// 174
	MemoryEdit::writeInt(0x005E8188 + 1, 1273);
	// CRASH CRASH
	MemoryEdit::writeInt(0x005E8452 + 1, 1280);
	
	MemoryEdit::writeInt(0x005EBB33 + 1, height);
	MemoryEdit::writeInt(0x005EBB3A + 1, width);

	// 175
	MemoryEdit::writeInt(0x0060116B + 1, height);
	MemoryEdit::writeInt(0x00601170 + 1, width);
	
	// 176
	MemoryEdit::writeInt(0x006017DA + 1, height);
	MemoryEdit::writeInt(0x006017DF + 1, width);

	// 177
	MemoryEdit::writeInt(0x00601E29 + 1, height);
	MemoryEdit::writeInt(0x00601E2E + 1, width);
	// 178
	MemoryEdit::writeInt(0x00602235 + 1, height);
	MemoryEdit::writeInt(0x0060223A + 1, width);
	// 179
	MemoryEdit::writeInt(0x006024F5 + 1, height);
	MemoryEdit::writeInt(0x006024FA + 1, width);
	
	// 180 jmp to nmcom.dll
	// 181 maybe login stuff
	// 182
	MemoryEdit::writeInt(0x0060C5FB + 1, width);
	// 183
	MemoryEdit::writeInt(0x0060C604 + 2, width / 2);
	// 184
	MemoryEdit::writeInt(0x0060C613 + 2, height);
	// 185
	MemoryEdit::writeInt(0x0060C61C + 2, height / 2);
	// 186
	
	MemoryEdit::writeInt(0x0060C632 + 2, height / 2);
	// 187
	MemoryEdit::writeInt(0x0060C64D + 1, width);
	// 188
	MemoryEdit::writeInt(0x0060C656 + 2, width / 2);
	// 189
	MemoryEdit::writeInt(0x0060C665 + 2, height);
	MemoryEdit::writeInt(0x0060C66C + 1, height / 2);
	// 190
	MemoryEdit::writeInt(0x0060C683 + 2, -height / 2);
	
	// 191
	MemoryEdit::writeInt(0x0060C69E + 2, width);
	MemoryEdit::writeInt(0x0060C6A5 + 2, height / 2);
	
	// 192
	MemoryEdit::writeInt(0x0060C6B5 + 1, width);
	MemoryEdit::writeInt(0x0060C6BC + 1, -height / 2);
	// 193
	MemoryEdit::writeInt(0x0060C6C7 + 2, height / 2);
	// 194
	MemoryEdit::writeInt(0x0060C6EF + 1, width);
	// 195
	MemoryEdit::writeInt(0x0060C6F8 + 1, width / 2);
	// 196
	MemoryEdit::writeInt(0x0060C707 + 1, height);
	MemoryEdit::writeInt(0x0060C70E + 1, width / 2);
	// 197
	MemoryEdit::writeInt(0x0060C719 + 2, height / 2);
	// 198
	MemoryEdit::writeInt(0x0060C812 + 1, width);
	MemoryEdit::writeInt(0x0060C819 + 2, width / 2);
	// 199
	MemoryEdit::writeInt(0x0060C82F + 1, height / 2);
	MemoryEdit::writeInt(0x0060C828 + 2, height);
	// 200
	MemoryEdit::writeInt(0x0060D589 + 2, - height / 2);
	// 201
	MemoryEdit::writeInt(0x0060D5B3 + 2, height);
	MemoryEdit::writeInt(0x0060D5BA + 2, width);
	MemoryEdit::writeInt(0x0060D5C2 + 1, -height / 2);
	// 202
	MemoryEdit::writeInt(0x0060E526 + 1, width / 2);
	// 203
	MemoryEdit::writeInt(0x0060E5A6 + 1, height / 2);
	// 204
	MemoryEdit::writeInt(0x0060E626 + 1, height / 2);
	// 205
	MemoryEdit::writeInt(0x0060E6A6 + 1, width / 2);
	// 206
	MemoryEdit::writeInt(0x006115CA + 4, width);
	// 207
	MemoryEdit::writeInt(0x006115D6 + 4, height);
	// 208
	MemoryEdit::writeInt(0x00611734 + 4, width);
	// 209
	MemoryEdit::writeInt(0x00611740 + 4, height);
	// 210 211 212 213 214 215
	// 216
	MemoryEdit::writeInt(0x00661BC2 + 1, height);
	// 217 hooker
	// 218 noper
	// 219 noper
	// 220 
	// 221 - 226 noper or jumper
	// 227
	MemoryEdit::writeInt(0x006E2188 + 1, 36);
	// 228 hooker
	// 229
	MemoryEdit::writeByteArray(0x006E2188, "94 35 77 8B C6 5E 5B 59");
	// 230 item patch?
	// 231 - 239 more item patch
	// 240 Jumper
	// 241 some mul stuff
	// 242 jump
	// 243 jump
	// 244 jump
	// 245 246 jump
	// 247 noper
	// 248 hooker
	// 249 jumper
	// 250 random stuffs
	// 251
	MemoryEdit::writeInt(0x0073D26D + 1, 361);
	// 252
	MemoryEdit::writeInt(0x0073D285 + 3, 680);
	// 253
	MemoryEdit::writeInt(0x0073D41B + 1, 361);
    // 254
	MemoryEdit::writeInt(0x0073D433 + 3, 680);
	// 255 item stuff
	// 256
	// 257 hooker
	// 258 noper
	// 259 noper
	// 260
	MemoryEdit::writeInt(0x007521D0 + 1, height);
	// 261
	MemoryEdit::writeInt(0x007521E3 + 1, width);
	// 262
	MemoryEdit::writeInt(0x0075E146 + 1, width);
	MemoryEdit::writeInt(0x0075E141 + 1, height);
	// 263
	MemoryEdit::writeInt(0x0075EFC0 + 1, width);
	// 264
	MemoryEdit::writeInt(0x00765B21 + 3, width);
	// 265
	MemoryEdit::writeInt(0x00765FC8 + 1, width);
	// 266
	MemoryEdit::writeInt(0x0076617B + 2, width);
	// 267
	MemoryEdit::writeInt(0x0076CFB3 + 1, width);
	MemoryEdit::writeInt(0x0076CFAB + 1, height);
	// 268
	MemoryEdit::writeInt(0x0076ED79 + 1, width);
	MemoryEdit::writeInt(0x0076ED71 + 1, height);
	// 269
	MemoryEdit::writeInt(0x0077317D + 1, height);
	// 270
	MemoryEdit::writeInt(0x00773187 + 1, width);
	// 271
	MemoryEdit::writeInt(0x007738D1 + 1, height);
	MemoryEdit::writeInt(0x007738D9 + 1, width);
	// 272
	MemoryEdit::writeByteArray(0x007850C3, "39 C0 90 90 90 90 90");
	// 273
	MemoryEdit::writeByteArray(0x007850F4, "83 E8 00 90 90 90");
	// 274
	MemoryEdit::writeByteArray(0x00786584, "39 C0 00 90 90 90 90 90");
	// 275
	MemoryEdit::writeByteArray(0x007865B5, "83 E8 00 90 90 90");
	// 276
	MemoryEdit::writeByteArray(0x00786A01, "39 C0 90 90 90 90 90");
	// 277
	MemoryEdit::writeByteArray(0x00786A32, "83 E8 00 90 90 90");
	// 278
	MemoryEdit::writeInt(0x00788AA2 + 1, width);
	// 279
	MemoryEdit::writeInt(0x00788AB5 + 1, height);
	// 280 // Something send packet
	// 281 ?? packet something
	// 282 - item scrolls related?
	// 283 - again
	// 284 - item stuff
	// 285 - again
	// 286 - admin account can't throw away money
	MemoryEdit::writeByteArray(0x007A6AB1, "E9 9A 00 00 00 00");
	// 287 - custom stuff I guess
	// 288
	MemoryEdit::writeInt(0x007B0724 + 4, height / 2);
	// 289
	MemoryEdit::writeInt(0x007B0755 + 1, height / 2);
	// 290
	MemoryEdit::writeInt(0x007B0786 + 4, width / 2);
	// 291
	MemoryEdit::writeInt(0x007B07BA + 1, width / 2);
	// 292
	MemoryEdit::writeByteArray(0x007B1024, "02 00 00 00 F7 E1 90 90 90 89 96 7C 15 00 00 8D");
	// 293 scroll stuff
	// 294 other requests msg
	// 295 same
	// 296 same
	// 297 same
	// 298 same
	// 299 same
	// 300
	MemoryEdit::writeInt(0x007CE782 + 1, 568);
	MemoryEdit::writeInt(0x007CE788 + 1, 1202);
	// 301
	MemoryEdit::writeInt(0x007CE8A2 + 1, 464);
	MemoryEdit::writeInt(0x007CE8A8 + 1, 1273);
	// 302 chat stuff?
	// 303
	// 304 packet stuff
	// 305
	// 306
	MemoryEdit::writeInt(0x007E152A + 2, height);
	// 307
	MemoryEdit::writeInt(0x007E1541 + 1, 729);
	// 308
	MemoryEdit::writeInt(0x007E154D + 2, width);
	MemoryEdit::writeInt(0x007E1555 + 2, width - 70);
	// 309 packet
	// 310 same
	// 311 // party stuff
	// 312
	// 313  all party stuff i guess level related
	// 314 
	// 315 jumper
	// 316
	// 317
	MemoryEdit::writeInt(0x0081D647 + 1, 540);
	// 318 hooker window related
	// 319
	MemoryEdit::writeInt(0x0081DE33 + 1, 611);
	// 320
	MemoryEdit::writeInt(0x0081DF07 + 1, 1010);
	// 321
	MemoryEdit::writeInt(0x0081E016 + 1, 443);
	// 322
	MemoryEdit::writeInt(0x0081E45F + 1, 1010);
	// 323 jumper
	// 324
	// 325
	MemoryEdit::writeInt(0x00821BC0 + 1, height);
	// 326
	MemoryEdit::writeInt(0x00821BD3 + 1, width);
	// 327 jumper
	// 328 jumper
	// 329
	MemoryEdit::writeInt(0x0082FB7E + 1, width);
	MemoryEdit::writeInt(0x0082FB79 + 1, height);
	// 330 jumper
	// 331 jumper
	// 332
	// 333
	// 334
	MemoryEdit::writeInt(0x008433EB + 1, 370);
	MemoryEdit::writeInt(0x008433F0 + 1, 525);
	// 335
	MemoryEdit::writeInt(0x00844972 + 2, 200);
	// 336
	MemoryEdit::writeInt(0x00844B2B + 1, 200);
	// 337
	MemoryEdit::writeInt(0x00845518 + 2, 0x1863B300); // float 4.40
	// 338
	MemoryEdit::writeInt(0x00845BFD + 2, 0x7FADCE49);
	// 339
	MemoryEdit::writeInt(0x00845C0A + 3, 0x7FADCE49);
	// 340
	MemoryEdit::writeInt(0x00845C19 + 1, 0x7FADCE49);
	// 341
	MemoryEdit::writeInt(0x00845C22 + 1, 0x7FADCE49);
	// 342
	MemoryEdit::writeInt(0x00846C74 + 3, 400);
	// 343
	MemoryEdit::writeInt(0x00846FA2 + 2, 300);
	// 344
	MemoryEdit::writeInt(0x00846FAD + 3, 300);
	// 345 - 350
	MemoryEdit::writeInt(0x00847840 + 1, 183);
	MemoryEdit::writeInt(0x008478A0 + 1, 183);
	MemoryEdit::writeInt(0x00847900 + 1, 183);
	MemoryEdit::writeInt(0x00847960 + 1, 183);
	MemoryEdit::writeInt(0x008479C0 + 1, 183);
	MemoryEdit::writeInt(0x00847A20 + 1, 183);
	// 351 - 353
	MemoryEdit::writeByte(0x00847AC1 + 1, 127);
	MemoryEdit::writeByte(0x00847BA3 + 1, 127);
	MemoryEdit::writeByte(0x00847C85 + 1, 127);
	// 354
	MemoryEdit::writeInt(0x008481CB + 1, 185);
	// 355
	MemoryEdit::writeInt(0x00849D77 + 1, 207);
	// 356
	MemoryEdit::writeInt(0x00849ED5 + 1, 186);
	// 357
	MemoryEdit::writeInt(0x0084A1A8 + 1, 206);
	// 358
	MemoryEdit::writeByte(0x0084A67A + 1, 127);
	// 359
	MemoryEdit::writeInt(0x0084A709 + 1, 200);
	// 360 - 363 - I believe it's stats related from 999 to 32K
	MemoryEdit::writeInt(0x0084D994 + 1, 32767);
	MemoryEdit::writeInt(0x0084D9C1 + 1, 32767);
	MemoryEdit::writeInt(0x0084D9EE + 1, 32767);
	MemoryEdit::writeInt(0x0084DA1B + 1, 32767);
	// 364
	MemoryEdit::writeInt(0x0085041E + 1, -1213);
	MemoryEdit::writeInt(0x00850424 + 1, -595);
	// 365 - 370
	MemoryEdit::writeInt(0x00850675 + 2, 675);
	MemoryEdit::writeInt(0x0085068C + 2, 675);
	MemoryEdit::writeInt(0x008506AE + 2, 675);
	MemoryEdit::writeInt(0x008506F2 + 2, 595);
	MemoryEdit::writeInt(0x008506FE + 2, 675);
	MemoryEdit::writeInt(0x00850711 + 2, 675);
	// 371
	MemoryEdit::writeInt(0x0085210F + 1, 688);
	// 372
	MemoryEdit::writeInt(0x00852185 + 1, width / 2);
	// 373
	MemoryEdit::writeInt(0x008532AA + 1, 675);
	// 374
	MemoryEdit::writeInt(0x0085334B + 1, 675);
	// 375
	MemoryEdit::writeInt(0x008533CD + 1, width / 2);
	// 376
	MemoryEdit::writeInt(0x008533D9 + 1, 1139);
	// 377
	MemoryEdit::writeInt(0x00853464 + 1, 687);
	// 378
	MemoryEdit::writeInt(0x00853470 + 1, 1165);
	// 379
	MemoryEdit::writeInt(0x00854796 + 2, -727);
	// 380
	MemoryEdit::writeInt(0x0085484C + 2, -728);
	// 381
	MemoryEdit::writeInt(0x008549C6 + 1, width);
	MemoryEdit::writeInt(0x008549C1 + 1, 746);
	// 382
	MemoryEdit::writeInt(0x00854A2D + 1, 722);
	// 383
	MemoryEdit::writeInt(0x00854AE4 + 1, 713);
	// 384
	MemoryEdit::writeInt(0x00854BD5 + 1, 728);
	// 385
	MemoryEdit::writeInt(0x00855ED7 + 1, 748);
	// 386
	MemoryEdit::writeInt(0x008562C7 + 1, 748);
	// 387
	MemoryEdit::writeByte(0x00856878 + 1, 0xBD);
	// 388
	MemoryEdit::writeInt(0x00856920 + 1, 1213);
	// 389
	MemoryEdit::writeInt(0x00856A7D + 1, width / 2);
	MemoryEdit::writeInt(0x00856A82 + 1, 1334);
	// 390
	MemoryEdit::writeInt(0x00856C5C + 1, 687);
	// 391
	MemoryEdit::writeInt(0x00856D02 + 1, 687);
	// 392
	MemoryEdit::writeInt(0x00857125 + 1, width / 2);
	MemoryEdit::writeInt(0x0085712A + 3, 0x000004A0);
	// 393
	MemoryEdit::writeInt(0x0085785D + 1, 746);
	MemoryEdit::writeInt(0x00857862 + 1, 1366);
	// 394
	MemoryEdit::writeInt(0x008581D3 + 1, 749);
	// 395
	MemoryEdit::writeInt(0x00858B16 + 1, 581);
	// 396
	MemoryEdit::writeInt(0x00859440 + 1, 749);
	// 397
	MemoryEdit::writeInt(0x00859D44 + 1, 749);
	// 398
	MemoryEdit::writeInt(0x0085A1D5 + 1, 712);
	// 399
	MemoryEdit::writeInt(0x0085A28A + 1, 717);
	// 400
	MemoryEdit::writeInt(0x0085A335 + 1, 716);
	// 401
	MemoryEdit::writeInt(0x0085A41E + 1, 717);
	// 402
	MemoryEdit::writeInt(0x0085A4C0 + 1, 712);
	// 403
	MemoryEdit::writeInt(0x0085A5DE + 1, 717);
	// 404
	MemoryEdit::writeInt(0x0085A6A1 + 1, 717);
	// 405
	MemoryEdit::writeInt(0x0085A701 + 1, 717);
	// 407
	MemoryEdit::writeInt(0x0085A8AA + 1, 716);
	// 408 -> int stuff about skill?
	// 409
	MemoryEdit::writeInt(0x0085AA8C + 1, 717);
	// 410 -> skill again?
	// 411
	MemoryEdit::writeInt(0x0085AC16 + 1, 717);
	// 412
	MemoryEdit::writeInt(0x0085ACB8 + 1, 716);
	// 413
	MemoryEdit::writeInt(0x0085ADBB + 1, 716);
	// 414
	MemoryEdit::writeInt(0x0085AEA1 + 1, 717);
	// 415 -> Int stuff?
	// 416
	MemoryEdit::writeInt(0x0085B02D + 1, 717);
	// 417
	MemoryEdit::writeInt(0x0085B0D6 + 1, 716);
	// 418
	MemoryEdit::writeInt(0x0085EC73 + 1, 677);
	// 419
	MemoryEdit::writeInt(0x0085ECEF + 1, 678);
	// 420
	MemoryEdit::writeInt(0x0085ED14 + 1, 748); // Client view port went up with this one
	// 421
	MemoryEdit::writeInt(0x0085ED3A + 6, 654);
	// 422
	MemoryEdit::writeInt(0x0085EDA8 + 1, 678);
	// 423
	MemoryEdit::writeInt(0x0085EDCD + 1, 748);
	// 424
	MemoryEdit::writeInt(0x0085EDFD + 6, 681); // crashing?
	// 425
	MemoryEdit::writeInt(0x0085EE6E + 1, 681);
	// 426
	MemoryEdit::writeInt(0x0085EE90 + 1, 748);
	// 427
	MemoryEdit::writeInt(0x0085F0CE + 1, 677);
	// 428
	MemoryEdit::writeInt(0x0085F151 + 1, 678);
	// 429 - no idea
	// 430
	MemoryEdit::writeInt(0x00862313 + 2, 701);
	MemoryEdit::writeInt(0x0086231A + 1, 1213);
	// 431
	MemoryEdit::writeInt(0x00862D6F + 1, 746);
	MemoryEdit::writeInt(0x00862D74 + 1,  width);
	// 432
	MemoryEdit::writeInt(0x008630AC + 1, 746);
	MemoryEdit::writeInt(0x008630B1 + 1, width);
	// 433
	MemoryEdit::writeInt(0x00863973 + 1, 735);
	// 434
	MemoryEdit::writeInt(0x00863E1C + 1, 749);
	// 435
	MemoryEdit::writeInt(0x0086419D + 1, 749);
	// 436
	MemoryEdit::writeInt(0x00864530 + 1, 749);
	// 437
	MemoryEdit::writeInt(0x0086499C + 1, 749);
	// 438
	MemoryEdit::writeInt(0x00864A5F + 1, 711);
	MemoryEdit::writeInt(0x00864A64 + 1, 1139);
	// 439
	MemoryEdit::writeInt(0x00864B0B + 1, 711);
	MemoryEdit::writeInt(0x00864B10 + 1, 1195);
	// 440
	MemoryEdit::writeInt(0x00864BDC + 1, 711);
	MemoryEdit::writeInt(0x00864BE1 + 1, 1251);
	// 441
	MemoryEdit::writeInt(0x00864CAD + 1, 711);
	MemoryEdit::writeInt(0x00864CB2 + 1, 1307);
    // 442
	MemoryEdit::writeInt(0x00864D3E + 1, width / 2);
	MemoryEdit::writeInt(0x00864D43 + 1, 1184);
	// 443
	MemoryEdit::writeInt(0x00864DCF + 1, width / 2);
	MemoryEdit::writeInt(0x00864DD4 + 1, 1214);
	// 444
	MemoryEdit::writeInt(0x00864E60 + 1, width / 2);
	MemoryEdit::writeInt(0x00864E65 + 1, 1244);
	// 445
	MemoryEdit::writeInt(0x00864EF1 + 1, width / 2);
	MemoryEdit::writeInt(0x00864EF6 + 1, 1274);
	// 446
	MemoryEdit::writeInt(0x00864F82 + 1, width / 2);
	MemoryEdit::writeInt(0x00864F87 + 1, 1304);
	// 447
	MemoryEdit::writeInt(0x00865076 + 1, width / 2);
	MemoryEdit::writeInt(0x0086507B + 1, 1334);
    // 448
	MemoryEdit::writeInt(0x0086516A + 1, width / 2);
	MemoryEdit::writeInt(0x0086516F + 1, 1139);
	// 449
	MemoryEdit::writeByte(0x00865267 + 2, 127);
	// 450
	MemoryEdit::writeInt(0x00867F34 + 2, width - 1);
	MemoryEdit::writeInt(0x00867F3C + 1, width - 1);
	// 451
	MemoryEdit::writeInt(0x00867F4C + 2, height - 1);
	MemoryEdit::writeInt(0x00867F54 + 1, height - 1);
	
	// 452
	MemoryEdit::writeByte(0x00870EFE + 1, 14);
	// 453 - hooker gm stuff?
	// 454
	MemoryEdit::writeInt(0x00867F4C + 2, width);
	MemoryEdit::writeInt(0x00867F54 + 1, width);
	// 455
	MemoryEdit::writeInt(0x00887F74 + 2, height);
	MemoryEdit::writeInt(0x00887F7C + 1, height);
	// 456
	MemoryEdit::writeInt(0x008883CD + 2, width);
	MemoryEdit::writeInt(0x008883D5 + 1, width);
	// 457
	MemoryEdit::writeInt(0x008883F1 + 2, height);
	MemoryEdit::writeInt(0x008883F9 + 1, height);
	// 458
	MemoryEdit::writeInt(0x0088BEA8 + 2, width);
	// 459
	MemoryEdit::writeInt(0x0088BED4 + 1, width);

	// 460
	MemoryEdit::writeInt(0x0088BEF0 + 2, width);
	MemoryEdit::writeInt(0x0088BEF7 + 1, width);

	// 461
	MemoryEdit::writeInt(0x0088BF17 + 2, height);
	MemoryEdit::writeInt(0x0088BF1F + 1, height);

	// 462 noper
	// 463
	// 464
	// 465
	// 466
	// MemoryEdit::writeInt(0x0088D18E + 1, 0); Potential stuff?
	// 467 changer
	MemoryEdit::writeByte(0x00892549 + 3, 127);
	// 468
	MemoryEdit::writeByte(0x0089274E + 3, 127);
	// 469 hoooker
	// 470 hooker
	// 471 - 479 Must be reviewed, too many random stuff
	// 480
	MemoryEdit::writeInt(0x008FC045 + 5, 1316);
	// 481
	MemoryEdit::writeInt(0x008FC46F + 1, 1316);
	// 482
	MemoryEdit::writeInt(0x008FC9DE + 1, width);
	// 483
	MemoryEdit::writeInt(0x008FCBF7 + 1, width);
	// 484 - 488 random patches, review
	// 489 - calculation stuff
	// 490 - same
	// 491 492 hooks
	// 493 494 jmps
	// 495 nops
	// 496 patches
	// 497
	// 498 nops
	// 499
	// 500 - 544 random stuff must review
	// 545
	MemoryEdit::writeInt(0x00942C8F + 1, height);
	MemoryEdit::writeInt(0x00942C87 + 4, 549);
	// 546
	MemoryEdit::writeInt(0x00942C9F + 4, 550);
	// 547
	MemoryEdit::writeInt(0x00943502 + 1, height);
	// 548
	MemoryEdit::writeInt(0x0094351D + 1, 465);
	// 549
	MemoryEdit::writeInt(0x00947BBE + 2, height);
	MemoryEdit::writeInt(0x00947BC6 + 1, height);

	// 550
	MemoryEdit::writeInt(0x00947BD4 + 2, width);
	MemoryEdit::writeInt(0x00947BDC + 1, width - 100);



	// 551
	MemoryEdit::writeInt(0x009525AA + 2, height);
	MemoryEdit::writeInt(0x009525B2 + 1, height);
	// 552
	MemoryEdit::writeInt(0x009525C0 + 2, width);
	MemoryEdit::writeInt(0x009525C8 + 1, width - 100);

	// 553
	MemoryEdit::writeInt(0x0095422A + 1, width);
	// 554
	MemoryEdit::writeInt(0x00954237 + 1, height);

	// 560
	MemoryEdit::writeInt(0x00864D3E + 1, width / 2);
	// 562
	MemoryEdit::writeInt(0x00864D43 + 1, 1184);
	// 563
	MemoryEdit::writeInt(0x0098AEB0 + 1, -width / 2);
	MemoryEdit::writeInt(0x0098AEA8 + 1, -height / 2);

	// 567
	MemoryEdit::writeInt(0x00997A6D + 1, width);
	MemoryEdit::writeInt(0x00997A68 + 1, height);

	// 568
	MemoryEdit::writeInt(0x0099CB4E + 3, width);
	MemoryEdit::writeInt(0x0099CB55 + 3, height);
	// 632 633
	MemoryEdit::writeInt(0x009BE584 + 6, width);
	MemoryEdit::writeInt(0x009BE594 + 6, height);
	
	

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

