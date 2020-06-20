#include <windows.h>
#include <fstream>
#include <string>
#include <io.h>

#define isFileExist _access

#define DLLNAME "client.dll"



void ShowMessageBox(const char* format, ...){
	char messageBoxText[1024];
	va_list args;
	va_start(args, format);
	vsnprintf(messageBoxText, 1023, format, args);

	MessageBoxA(NULL, messageBoxText, "MapleStory", MB_ICONERROR);

	va_end(args);
}


BOOL LaunchMaple(){
	if (isFileExist(DLLNAME, 0) == -1){
		ShowMessageBox("Unable to find ", DLLNAME);
		return false;
	}

	STARTUPINFO		gameStartInfo;
	PROCESS_INFORMATION	gameProcInfo;

	ZeroMemory(&gameStartInfo, sizeof(gameStartInfo));
	ZeroMemory(&gameProcInfo, sizeof(gameProcInfo));

	gameStartInfo.cb = sizeof(gameStartInfo);

	
	if (isFileExist("server.txt", 0) == -1) {
		ShowMessageBox("Unable to find server.txt");
		return false;
	}
	std::ifstream ifs("server.txt");
	std::string szProcess, szCommandLine;
	int i = 0;
	std::getline(ifs, szProcess);
	std::getline(ifs, szCommandLine);

	if (isFileExist(szProcess.c_str(), 0) == -1) {
		ShowMessageBox("Unable to find %s file. Please try to re-install the game.", szProcess.c_str());
		return false;
	}

	

	BOOL isCreated = CreateProcessA((LPCSTR)szProcess.c_str(), (LPSTR)szCommandLine.c_str(),
		NULL, NULL, FALSE,
		CREATE_SUSPENDED,
		NULL, NULL, &gameStartInfo, &gameProcInfo);

	
	if (isCreated){
		HANDLE dwHandleThread = gameProcInfo.hThread;
		HANDLE dwHandleProc = gameProcInfo.hProcess;

		const size_t dwLen = strlen(DLLNAME);

		HMODULE KernelAddress = GetModuleHandle("Kernel32.dll");

		LPVOID LoadLibAddress = (LPVOID)GetProcAddress(KernelAddress, "LoadLibraryA");

		LPVOID RemoteString = (LPVOID)VirtualAllocEx(dwHandleProc, NULL, dwLen, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);

		WriteProcessMemory(dwHandleProc, (LPVOID)RemoteString, DLLNAME, dwLen, NULL);

		CreateRemoteThread(dwHandleProc, NULL, NULL, (LPTHREAD_START_ROUTINE)LoadLibAddress, (LPVOID)RemoteString, NULL, NULL);

		ResumeThread(dwHandleThread);

		CloseHandle(dwHandleThread);
		CloseHandle(dwHandleProc);

		return TRUE;
	}
	
	ShowMessageBox("Unable to CreateProcess");

	return FALSE;
}

BOOL IsElevated()
{
	BOOL fRet = FALSE;
	HANDLE hToken = NULL;

	if (OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hToken)){
		TOKEN_ELEVATION Elevation;
		DWORD cbSize = sizeof(TOKEN_ELEVATION);
		if (GetTokenInformation(hToken, TokenElevation, &Elevation, sizeof(Elevation), &cbSize)){
			fRet = Elevation.TokenIsElevated;
		}
	}

	if (hToken){
		CloseHandle(hToken);
	}

	return fRet;
}

int CALLBACK WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
	if (!IsElevated()){
		ShowMessageBox("Please run as administrator!");
		return 0;
	}

	return LaunchMaple();

};