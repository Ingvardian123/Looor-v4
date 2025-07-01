#!/bin/bash

echo "Building UraniumCraft plugin..."
echo

# Проверяем наличие Maven
if ! command -v mvn &> /dev/null; then
    echo "Maven не найден! Убедитесь, что Maven установлен и добавлен в PATH."
    exit 1
fi

# Очищаем и собираем проект
echo "Cleaning and compiling..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Ошибка компиляции!"
    exit 1
fi

# Упаковываем в JAR
echo "Packaging..."
mvn package

if [ $? -ne 0 ]; then
    echo "Ошибка упаковки!"
    exit 1
fi

echo
echo "Сборка завершена успешно!"
echo "JAR файл находится в папке target/"
echo

# Показываем информацию о созданном файле
if [ -f "target/UraniumCraft-2.0.0.jar" ]; then
    echo "Создан файл: UraniumCraft-2.0.0.jar"
    echo "Размер: $(stat -f%z target/UraniumCraft-2.0.0.jar 2>/dev/null || stat -c%s target/UraniumCraft-2.0.0.jar) bytes"
else
    echo "Файл не найден в target/"
fi
