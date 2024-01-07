#include<string>
#include<Windows.h>


class ClientApp {

private:
	HWND hwndMain;
	HWND hwndList;
	HWND hwndTerminateButton;

public:
	ClientApp() {
		hwndMain = NULL;
		hwndList = NULL;
		hwndTerminateButton = NULL;
	}

	void Setup(HMODULE hInst);
	void AddLogMessage(std::string message);
	static LRESULT CALLBACK WindowEventProcedure(HWND hWnd, UINT msg, WPARAM wp, LPARAM lp);
	static ClientApp* GetInstance();
	
};