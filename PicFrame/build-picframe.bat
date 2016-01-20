@REM :::::::::::::::::::::::::::::::::::::::::::
@REM ::     SCRIPT TO CREATE PICFRAME APK     ::
@REM :::::::::::::::::::::::::::::::::::::::::::
@ECHO off
CLS
ECHO.
ECHO =============================
ECHO Running Admin shell
ECHO =============================
ECHO.
@REM ::::::::::::::::::::::::::::::::::::::::::
@REM ::Automatically check & get admin rights::
@REM ::::::::::::::::::::::::::::::::::::::::::
:checkPrivileges
NET FILE 1>NUL 2>NUL
IF '%errorlevel%' == '0'	GOTO gotPrivileges
ELSE						GOTO getPrivileges
:getPrivileges
IF '%1'=='ELEV' (shift & GOTO gotPrivileges)
ECHO.
ECHO **************************************
ECHO Invoking UAC for Privilege Escalation
ECHO **************************************
ECHO.
SETLOCAL DisableDelayedExpansion
SET "batchPath=%~0"
SETLOCAL EnableDelayedExpansion
ECHO Set UAC = CreateObject^("Shell.Application"^) > "%temp%\OEgetPrivileges.vbs"
ECHO UAC.ShellExecute "!batchPath!", "ELEV", "", "runas", 1 >> "%temp%\OEgetPrivileges.vbs"
"%temp%\OEgetPrivileges.vbs"
EXIT /B
ENDLOCAL
@REM #######################################################################################
@REM ::::::::::::::::::::::::::::
@REM START SCRIPT TO CREATE APK::
@REM ::::::::::::::::::::::::::::
:gotPrivileges
TITLE Script to create PicFrame APK
SETLOCAL EnableDelayedExpansion
@REM :::VARIABLES:::
@REM checking for a ANDROID_HOME
SET androidHome=empty
SET androidHomeFound=FALSE
IF DEFINED ANDROID_HOME SET androidHomeFound=TRUE
@REM checking for a JAVA_HOME
SET javaHome=empty
SET javaHomeFound=FALSE
IF DEFINED JAVA_HOME SET javaHomeFound=TRUE
SETLOCAL
@REM misc
SET tab=	
SET everthingOK=TRUE
SET "currPath=%~dp0"
@REM Android SDK Version and Path
SET platformVersionNeeded=21
SET "platformVersionNeededString=Android 5.0.1"
SET "supportLibraryV4Path=%androidHome%/extras/android/m2repository/com/android/support/support-v4/23.0.1"
SET "supportLibraryV7Path=%androidHome%/extras/android/m2repository/com/android/support/appcompat-v7/21.0.0"
SET "support-v4String=support-v4:23.0.1"
SET "support-v7String=appcompat-v7:21.0.0"
SET "platformPath=%androidHome%/platforms/android-%platformVersionNeeded%/"
SET "platformPath=%androidHome%/platforms/android-%platformVersionNeeded%/"
@REM Input variables
SET input=empty
SET checkI=FALSE
@REM Build options
SET apkType=DEBUG
SET gradleArguments=assembleDebug
SET filenameInfo=debug
SET "propertiesFile=%currPath%/local.properties"
@REM Keystore variables
SET keystoreFile=empty
SET keyAlias=empty


:TOP
CLS
SET input=empty
ECHO.
ECHO ^Welcome to the PicFrame build script.
ECHO ^It prepares and creates a picframe apk.
ECHO.
ECHO ^Verifying home variables:
IF %javaHome%==empty    GOTO :JAVA
IF %androidHome%==empty GOTO :ANDROID

:JAVA
IF !javaHomeFound!==TRUE (
	ECHO ^Java directory....
	ECHO ^JAVA_HOME: "%JAVA_HOME%"
	@REM escape brackets, esp. in case of JAVA_HOME being located in window's Program Files x86 folder
	SET "javaHome=%JAVA_HOME%"
	SET javaHome=!javaHome:^(="^("!
	SET javaHome=!javaHome:^)="^)"!
	ECHO ^Press any key to proceed.
	PAUSE>NUL
)  ELSE IF !javaHomeFound!==FALSE (
	ECHO ^Java Home Variable is not set.
	ECHO ^Please set the path to your jdk in your home variables and restart the script.
	GOTO :EXIT
@Rem	ECHO ^JAVA_HOME environment variable is not set.
@Rem	ECHO ^Please enter the path to your java jdk folder:
@Rem	SET /p input="Enter path: "
@Rem	IF !input!==empty (
@Rem		GOTO :TOP
@Rem	) ELSE (
@Rem		SET "javaHome=!input!"
@Rem	)
)

:ANDROID
IF !androidHomeFound!==TRUE (
	ECHO ^Android SDK....
	ECHO ^ANDROID_HOME: "%ANDROID_HOME%"
	SET "androidHome=%ANDROID_HOME%"
	@REM escape brackets, esp. in case of ANDROID_HOME being located in window's Program Files x86 folder
	SET androidHome=!androidHome:^(="^("!
	SET androidHome=!androidHome:^)="^)"!
	ECHO ^Press any key to proceed.
	PAUSE>NUL
) ELSE IF !androidHomeFound!==FALSE (
	ECHO ^ANDROID_HOME environment variable is not set.
	ECHO ^Please enter the path to your android sdk.
	SET /p input="Enter path: "
	IF !input!==empty (
		GOTO :TOP
	) ELSE (
		SET "androidHome=!input!"
		SET androidHome=!androidHome:^(="^("!
		SET androidHome=!androidHome:^)="^)"!
	)
)

SET "platformPath=%androidHome%/platforms/android-%platformVersionNeeded%/"
ECHO !platformPath!

ECHO android: !androidHome! -- java: !javaHome!... & pause


:versionCheck
CLS
ECHO ^Checking for the required android platform version.
ECHO !platformPath!
IF EXIST !platformPath! (
	ECHO ^Required version '%platformVersionNeededString% -- API %platformVersionNeeded%' found.
	ECHO.
) ELSE (
	ECHO ^Couldn't find the required android platform version.
	ECHO ^Please download '%platformVersionNeededString% -- API %platformVersionNeeded%'.
	ECHO ^Once successfully downloaded, please press any key.
	PAUSE>NUL
	GOTO :versionCheck
)

SET "supportLibraryV4Path=%androidHome%/extras/android/m2repository/com/android/support/support-v4/23.0.1/"
SET "supportLibraryV7Path=%androidHome%/extras/android/m2repository/com/android/support/appcompat-v7/21.0.0/"

ECHO ^Checking for the required support libraries...

:supportLibV4Check
ECHO ^Checking for '%support-v4String%'...
IF EXIST !supportLibraryV4Path! (
	ECHO ^Required support library '%support-v4String%' found.
	ECHO.
) ELSE (
	ECHO ^Couldn't find the support library at !supportLibraryV4Path!
	ECHO ^Please download Android Support Repository.
	ECHO ^Once successfully downloaded, please press any key.
	PAUSE>NUL
	GOTO :supportLibV4Check
)

:supportLibV7Check
ECHO ^Checking for '%support-v7String%'...
IF EXIST !supportLibraryV7Path! (
	ECHO ^Required support library '%support-v7String%' found.
	ECHO ^Press any key to proceed.
	PAUSE>NUL
) ELSE (
	ECHO ^Couldn't find the support library at !supportLibraryV7Path!
	ECHO ^Please download Android Support Repository.
	ECHO ^Once successfully downloaded, please press any key.
	PAUSE>NUL
	GOTO :supportLibV7Check
)

SET androidHome=!androidHome:"^("=^^(!
SET androidHome=!androidHome:"^)"=^^)!
SET javaHome=!javaHome:"^)"=^)!
SET javaHome=!javaHome:"^("=^(!

:createAPK
SET checkI=FALSE
CLS
ECHO.
CHOICE /C:DR /M "Do you want to build a '[d]ebug' or '[r]elease' apk?"
IF %ERRORLEVEL%==1 GOTO :Debug
IF %ERRORLEVEL%==2 GOTO :Release
GOTO :createAPK

:Release
SET apkType=RELEASE
SET gradleArguments=assembleRelease
ECHO ^To build a release apk, you first need to
ECHO ^create a keystore file with a key and a password.
CHOICE /C:YN /M "Do you want to use an existing .keystore file?"
IF %ERRORLEVEL%==1 GOTO :existingKeystore
IF %ERRORLEVEL%==2 GOTO :newKeystore
CLS
GOTO :Release

:newKeystore
SET input=empty
CLS
ECHO ^For now the new keyStore file will be in this directory.
ECHO ^After this script finished, feel free to move it.
SET /p input="Please enter a name for the new keyStore: "
IF !input!==empty GOTO :newKeystore
IF !input!==''    GOTO :newKeystore
SET "keystoreFile=!input!.keystore"
SET input=empty
SET /p input="Please enter a new alias for the key in the keyStore: "
IF !input!==empty GOTO :newKeystore
IF !input!==''    GOTO :newKeystore
SET "keyAlias=!input!"
ECHO ^New key will now be generated: KeystoreFile:%keystoreFile% - Alias:%keyAlias%
@REM generate Keystore file
ECHO !javaHome!...
"!javaHome!\bin\keytool" -genkey -v -keystore %currPath%/%keystoreFile% -alias %keyAlias% -keyalg RSA -keysize 2048 -validity 10000
ECHO !javaHome!\bin\keytool
IF NOT !ERRORLEVEL!==0 (
	ECHO ^Key generation failed, keytool exited with an error.
	GOTO :EXIT
)
GOTO :Debug

:existingKeystore
SET input=empty
ECHO ^Please enter the full path plus extension to your keystore file:
SET /p input="Path: "
IF %input%==empty cls & GOTO :existingKeystore
IF %input%==''    cls & GOTO :existingKeystore
SET "keystoreFile=%input%"
SET input=empty
SET /p input="Please enter the alias for the key in the keyStore: "
IF %input%==empty cls & GOTO :existingKeystore
IF %input%==''    cls & GOTO :existingKeystore
SET "keyAlias=%input%"
GOTO :Debug

:Debug
@REM Actual build process here
ECHO ^Creating/Overwriting local.properties file.
ECHO ^Writing '%androidHome%' into properties.file.
@REM remove following two echos
SET str=%androidHome%
echo.%str%
SET str=%str:\=\\%
SET str=%str::=\:%
echo.%str%
ECHO ^sdk.dir=%str%> %propertiesFile%
@REM Build .apk file
cd %currPath%
call %currPath%/gradlew.bat %gradleArguments%

ECHO.
ECHO.
IF NOT %ERRORLEVEL%==0 (
	ECHO ^Build failed, gradlew exited with an error.
	GOTO :EXIT
)

SET "pathToAPK=%currPath%\app\build\outputs\apk\"
SET "oldFilename=app-release-unsigned.apk"
IF %apkType%==RELEASE (
	"!javaHome!\bin\jarsigner" -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore %keystoreFile% %pathToAPK%%oldFilename% %keyAlias%
	IF NOT %ERRORLEVEL%==0 (
		ECHO ^Signing of the apk failed, jarsigner exited with an error.
		GOTO :EXIT
	)
	SET filenameInfo=signed
)
ECHO.
ECHO.
@REM Move the .apk into project root
IF %apkType%==RELEASE (
	move %pathToAPK%app-releas*.apk .\
) ELSE (
	move %pathToAPK%app-debug.apk .\
)
IF NOT %ERRORLEVEL%==0 (
	ECHO ^Moving the file failed, please check this directory:
	ECHO ^.\app\build\outputs\apk\
	SET everthingOK=FALSE
)
@REM Rename the .apk to a more appealing filename
ren .\app*.apk PicFrame-%filenameInfo%.apk
IF NOT %ERRORLEVEL%==0 (
	ECHO ^Renaming the file failed, please check for an .apk file
	SET everthingOK=FALSE
)
IF %everthingOK%==TRUE (
	ECHO ^Build finished, your generated .apk file should be located in:
	ECHO ^: %currPath%
)

ECHO.
ECHO ^Thank you for using this script.
ECHO ^We hope you enjoy our app 'PicFrame'.

:EXIT
ECHO.
ECHO ^Press any key, to leave this script.
PAUSE>NUL
EXIT