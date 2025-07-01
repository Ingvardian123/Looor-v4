@echo off
echo Building UraniumCraft plugin...
echo.

REM Проверяем наличие Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven не найден! Убедитесь, что Maven установлен и добавлен в PATH.
    pause
    exit /b 1
)

REM Очищаем и собираем проект
echo Cleaning and compiling...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Ошибка компиляции!
    pause
    exit /b 1
)

REM Упаковываем в JAR
echo Packaging...
call mvn package

if %ERRORLEVEL% NEQ 0 (
    echo Ошибка упаковки!
    pause
    exit /b 1
)

echo.
echo Сборка завершена успешно!
echo JAR файл находится в папке target/
echo.

REM Показываем информацию о созданном файле
if exist "target\UraniumCraft-2.0.0.jar" (
    echo Создан файл: UraniumCraft-2.0.0.jar
    for %%I in ("target\UraniumCraft-2.0.0.jar") do echo Размер: %%~zI bytes
) else (
    echo Файл не найден в target/
)

pause
