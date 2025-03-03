; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!
;****************************************************************************
;* Install Script for FreeMind
;****************************************************************************
;* Before using this be sure to download and install Inno Setup from
;* www.jrsoftware.org and ISTool from www.istool.org. These are required to
;* make changes and compile this script. To use the billboard feature please
;* dowload and install the ISX BillBoard DLL.
;****************************************************************************
; Predrag Cuklin 18/06/2009 - Universial Version
;****************************************************************************

#define MyVersion "1.11.1"
#define MyStatus ""
#define MyAppName "Freeplane"
#define MyAppPublisher "Open source"
#define MyAppURL "http://sourceforge.net/projects/freeplane/"
#define MyAppExeName "freeplane.exe"
#define ConfigurationDirectory 'Freeplane\1.10.x'

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{D3941722-C4DD-4509-88C4-0E87F675A859}
AppCopyright=Copyright � 2000-2023 Freeplane team and others
AppName={#MyAppName}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
ArchitecturesInstallIn64BitMode=x64 ia64
OutputDir=.
OutputBaseFilename=Freeplane-Setup
SetupIconFile=Setup.ico
VersionInfoDescription=Free mind mapping software. Fast. Simple. Streamlined.
ChangesAssociations=true
PrivilegesRequired=none
AllowNoIcons=true
ShowTasksTreeLines=true
WindowVisible=true
WizardSmallImageFile=Freeplane_bee.bmp
WizardImageStretch=false
#if MyStatus == ""
  AppVersion={#MyVersion}
#else
  AppVersion={#MyVersion}{#MyStatus}
#endif
UninstallDisplayIcon={app}\freeplane.exe
UninstallDisplayName=Freeplane
DiskSpanning=false
MergeDuplicateFiles=true
Compression=lzma
SolidCompression=true
LanguageDetectionMethod=locale
WizardImageFile=WizModernImage-IS.bmp

[Languages]
Name: English; MessagesFile: compiler:Default.isl,messages_en.isl; LicenseFile: gpl-2.0_english.txt
Name: Croatian; MessagesFile: Croatian.isl,messages_hr.isl; LicenseFile: gpl-2.0_croatian.txt
Name: French; MessagesFile: compiler:Languages\French.isl,messages_fr.isl; LicenseFile: gpl-2.0_french.txt
Name: German; MessagesFile: compiler:Languages\German.isl,messages_de.isl; LicenseFile: gpl-2.0_german.txt
Name: Russian; MessagesFile: compiler:Languages\Russian.isl,messages_ru.isl; LicenseFile: gpl-2.0_russian.txt
Name: Spanish; MessagesFile: compiler:Languages\Spanish.isl,messages_es.isl; LicenseFile: gpl-2.0_english.txt
Name: Portuguese; MessagesFile: compiler:Languages\Portuguese.isl,messages_pt.isl; LicenseFile: gpl-2.0_portuguese.txt

[Tasks]
Name: desktopicon; Description: {cm:CreateDesktopIcon}; GroupDescription: {cm:AdditionalIcons}
Name: quicklaunchicon; Description: {cm:CreateQuickLaunchIcon}; GroupDescription: {cm:AdditionalIcons}
Name: associate; Description: {cm:AssocFileExtension,Freeplane,.mm}; GroupDescription: {cm:AssocingFileExtension,Freeplane,.mm}

[Files]
Source: "..\..\BIN\*"; DestDir: "{app}"; Flags: ignoreversion createallsubdirs recursesubdirs; Excludes: "\*.l4j.ini"
Source: "..\..\BIN\*.l4j.ini"; DestDir: "{app}"; Flags: ignoreversion onlyifdoesntexist

#ifdef includeJavaRuntime
Source: "..\..\java-runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion createallsubdirs recursesubdirs
#endif

[Icons]
Name: {group}\{#MyAppName}; Filename: {app}\{#MyAppExeName}; Tasks: 
Name: {group}\Uninstall Freeplane; Filename: {uninstallexe}; Tasks: 
Name: {commondesktop}\{#MyAppName}; Filename: {app}\{#MyAppExeName}; Tasks: desktopicon
Name: {userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}; Filename: {app}\{#MyAppExeName}; Tasks: quicklaunchicon

[Run]
Filename: {app}\{#MyAppExeName}; Description: {cm:LaunchProgram,{#MyAppName}}; Flags: nowait postinstall skipifsilent

[Registry]
;".myp" is the extension we're associating. "MyProgramFile" is the internal name for the file type as stored in the registry. Make sure you use a unique name for this so you don't inadvertently overwrite another application's registry key.
;"My Program File" above is the name for the file type as shown in Explorer.
;"DefaultIcon" is the registry key that specifies the filename containing the icon to associate with the file type. ",0" tells Explorer to use the first icon from MYPROG.EXE. (",1" would mean the second icon.)
Root: "HKLM"; Subkey: "Software\JavaSoft\Prefs"
Root: "HKCR"; Subkey: "Applications\freeplane.exe"; Flags: deletekey; Tasks: associate
Root: "HKCR"; Subkey: ".mm"; Flags: deletekey; Tasks: associate
Root: "HKLM"; Subkey: "SOFTWARE\Classes\.mm"; Flags: deletekey; Tasks: associate
Root: "HKCU"; Subkey: "Software\Classes\Applications\freeplane.exe"; Flags: deletekey; Tasks: associate
Root: "HKCU"; Subkey: "Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.mm"; Flags: deletekey; Tasks: associate
Root: "HKCR"; Subkey: ".mm"; ValueType: string; ValueData: "FreeplaneApplication"; Flags: uninsdeletekey; Tasks: associate
Root: "HKCR"; Subkey: "freeplane"; ValueType: string; ValueData: "URL:Freeplane protocol"; Flags: uninsdeletekey; Tasks: associate
Root: "HKCR"; Subkey: "freeplane"; ValueType: string; ValueName: "URL Protocol"; Flags: uninsdeletekey; Tasks: associate
Root: "HKCR"; Subkey: "freeplane\Shell\Open\Command"; ValueType: string; ValueData: """{app}\freeplane.exe"" ""%1"""; Flags: uninsdeletevalue; Tasks: associate
Root: "HKCR"; Subkey: "freeplane\DefaultIcon"; ValueType: string; ValueData: "{app}\freeplaneIcons.dll,0"; Flags: uninsdeletevalue; Tasks: associate
Root: "HKCR"; Subkey: "FreeplaneApplication"; ValueType: string; ValueData: "Freeplane mind map"; Flags: uninsdeletekey; Tasks: associate
Root: "HKCR"; Subkey: "FreeplaneApplication\Shell\Open\Command"; ValueType: string; ValueData: """{app}\freeplane.exe"" ""%1"""; Flags: uninsdeletevalue; Tasks: associate
Root: "HKCR"; Subkey: "FreeplaneApplication\DefaultIcon"; ValueType: string; ValueData: "{app}\freeplaneIcons.dll,0"; Flags: uninsdeletevalue; Tasks: associate

[InstallDelete]
Type: filesandordirs; Name: "{app}\core"
Type: filesandordirs; Name: "{app}\plugins"
Type: filesandordirs; Name: "{app}\runtime"

[UninstallDelete]
Type: filesandordirs; Name: "{app}"

[Dirs]
Name: {userappdata}\Freeplane; Flags: uninsneveruninstall; Tasks: ; Languages: 

[Code]
function CmdLineParamExists(const Value: string): Boolean;
var
  I: Integer;
begin
  Result := False;
  for I := 1 to ParamCount do
    if CompareText(ParamStr(I), Value) = 0 then
    begin
      Result := True;
      Exit;
    end;
end;

function DeleteConfigurationFilesForced: Boolean;
begin
  Result := CmdLineParamExists('/DELETE_CONFIGURATION_FILES');
end;

function KeepConfigurationFilesForced: Boolean;
begin
  Result := CmdLineParamExists('/KEEP_CONFIGURATION_FILES');
end;
// ask for delete config file during uninstall
procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
  UserConfigurationDirectory: String;
begin
  case CurUninstallStep of
    usUninstall:
      begin
        UserConfigurationDirectory := ExpandConstant('{userappdata}\{#ConfigurationDirectory}');
        if KeepConfigurationFilesForced or (not DirExists(UserConfigurationDirectory)) or (UninstallSilent and (DeleteConfigurationFilesForced = False)) then begin
          exit;
        end
        else if  (DeleteConfigurationFilesForced = True) OR (MsgBox(ExpandConstant('{cm:DeleteConfigurationFiles,{username}}'), mbConfirmation, MB_YESNO or MB_DEFBUTTON2) = IDYES) then
          begin
             DelTree(UserConfigurationDirectory, True, True, True);
          end
      end;
  end;
end;
