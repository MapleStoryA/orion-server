#include<string>
#include<Windows.h>


class ClientApp {

private:
	HWND hwndMain;
	HWND hwndList;

public:
	ClientApp() {
		hwndMain = NULL;
		hwndList = NULL;
	}

	void Setup(HMODULE hInst);
	void AddLogMessage(std::string message);
	static LRESULT CALLBACK WindowProcedure(HWND hWnd, UINT msg, WPARAM wp, LPARAM lp);
	static LRESULT CALLBACK WindowEventProcedure(HWND hWnd, UINT msg, WPARAM wp, LPARAM lp);
	static ClientApp* GetInstance();
	
};