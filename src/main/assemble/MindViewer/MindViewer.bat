
@echo off

@REM ==== CHECK MIND_HOME ===
@REM use the batch path to determine MIND_HOME if not defined.
pushd %~dp0..\
set MINDC_ROOT=%cd%
popd

if "%MIND_HOME%" == "" set MIND_HOME=%MINDC_ROOT%

python %MIND_HOME%\tools\MindViewer\MindViewer.py %*