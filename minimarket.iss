[Setup]
AppName=MiniMarket Yuly
AppVersion=1.0
DefaultDirName={autopf}\MiniMarket Yuly
DefaultGroupName=MiniMarket Yuly
UninstallDisplayIcon={app}\MiniMarket.exe
Compression=lzma2
SolidCompression=yes
OutputDir=C:\Users\arnie\.gemini\antigravity-ide\scratch\minimarket-arquitectura-unificado\target
OutputBaseFilename=MiniMarket_Setup
SetupIconFile=C:\Users\arnie\.gemini\antigravity-ide\scratch\minimarket-arquitectura-unificado\images\minimarket.ico
PrivilegesRequired=lowest

[Files]
Source: "C:\Users\arnie\.gemini\antigravity-ide\scratch\minimarket-arquitectura-unificado\target\MiniMarket.exe"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\MiniMarket Yuly"; Filename: "{app}\MiniMarket.exe"; WorkingDir: "{app}"
Name: "{autodesktop}\MiniMarket Yuly"; Filename: "{app}\MiniMarket.exe"; WorkingDir: "{app}"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Crear un acceso directo en el Escritorio"; GroupDescription: "Accesos directos adicionales:"

[Run]
Filename: "{app}\MiniMarket.exe"; Description: "Ejecutar MiniMarket Yuly"; WorkingDir: "{app}"; Flags: postinstall nowait skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{%USERPROFILE}\.minimarket"
