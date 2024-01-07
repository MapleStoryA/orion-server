@echo off
:: Define variables for file paths and executable names
set MAPLESTORY_EXE=C:\Nexon\MapleStoryV92\MapleStory.exe
set CLIENT_DLL_SOURCE=C:\dev\projects\orion-server\client\Release\client.dll
set CLIENT_DLL_DESTINATION=C:\Nexon\MapleStoryV92\

:: Forcefully terminate the MapleStory.exe process
taskkill /f /im MapleStory.exe

:: Copy the client.dll file from the source to the destination, replacing the existing file if it exists
copy /y %CLIENT_DLL_SOURCE% %CLIENT_DLL_DESTINATION%

:: Display a message confirming the successful copying of the file
echo File has been copied.

:: Pause for 1 second without allowing a break
timeout /t 1 /nobreak

:: Launch the MapleStory.exe process
start "" %MAPLESTORY_EXE%

:: Display a message indicating that the MapleStory.exe process has been started
echo MapleStory.exe has been started.
