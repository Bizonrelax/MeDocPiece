# MeDocPiece

A small, focused Java Swing application that lets you quickly inject
`JOptionPane.showMessageDialog` calls into the beginning of selected
methods in any `.java` file – and just as easily remove them later.

## Features

- **Graphical interface** – pick a `.java` file, see all its methods.
- **Selection of methods** – checkboxes for each method, with “Select all” / “Deselect all” / “Invert” buttons.
- **One‑click insertion** – adds a pre‑formatted message box (with customizable font size) at the start of the method body.
- **Safe removal** – deletes only the blocks previously inserted by MeDocPiece, leaving the rest of the code intact.
- **Automatic imports** – adds required `import` statements (`javax.swing.JLabel`, `java.awt.Font`, `javax.swing.JOptionPane`) if they are missing.
- **Persistent configuration** – remembers the last opened file path, selected checkboxes, and UI font sizes between sessions.
- **Sortable method list** – sort methods by declaration order, name, presence of a marker, or method “weight” (AST node count).
- **Visual marker** – an icon indicates which methods already contain an inserted message.

## How to use

1. Run the program (see [Building](#building)).
2. Click **Browse** and select a `.java` file.
3. Tick the methods you want to modify.
4. Click **Add messages** (or **Remove all messages** when all checkboxes are unchecked).
5. The file will be updated – all changes are marked with special comments, ensuring only MeDocPiece’s blocks are touched.

## Building

1. Clone the repository.
2. Open the project in Eclipse (or any Java IDE).
3. Add `javaparser-core-3.28.0.jar` (or newer) to the build path.  
   Download from [Maven Central](https://central.sonatype.com/artifact/com.github.javaparser/javaparser-core).
4. Compile and run `MethodMessenger.java`.

## License

This project is released under **The Unlicense** – see the [LICENSE](LICENSE) file for details.  
You are free to use, modify, and distribute it without any restrictions or obligations.


___
___
___


MeDocPiece
Небольшое, сфокусированное приложение на Java Swing, позволяющее быстро вставлять вызовы JOptionPane.showMessageDialog в начало выбранных методов любого .java-файла — и так же легко удалять их впоследствии.
Возможности
Графический интерфейс – выберите .java-файл и просмотрите все его методы.
Выбор методов – чекбоксы для каждого метода, с кнопками «Выбрать все» / «Снять все» / «Инвертировать».
Вставка в один клик – добавляет предварительно отформатированное окно сообщения (с настраиваемым размером шрифта) в начало тела метода.
Безопасное удаление – удаляет только те блоки, которые были ранее вставлены MeDocPiece, оставляя остальной код нетронутым.
Автоматические импорты – добавляет необходимые операторы import (javax.swing.JLabel, java.awt.Font, javax.swing.JOptionPane), если они отсутствуют.
Сохранение конфигурации – запоминает путь к последнему открытому файлу, выбранные чекбоксы и размеры шрифтов интерфейса между сессиями.
Сортируемый список методов – сортировка методов по порядку объявления, имени, наличию маркера или «весу» метода (количество узлов AST).
Визуальный маркер – иконка показывает, какие методы уже содержат вставленное сообщение.
Как использовать
Запустите программу (см. Сборка).
Нажмите Обзор и выберите .java-файл.
Отметьте методы, которые хотите изменить.
Нажмите Добавить сообщения (или Удалить все сообщения, если все чекбоксы сняты).
Файл будет обновлён – все изменения помечены специальными комментариями, что гарантирует обработку только блоков, вставленных MeDocPiece.
Сборка
Клонируйте репозиторий.
Откройте проект в Eclipse (или любой другой Java IDE).
Добавьте javaparser-core-3.28.0.jar (или более новую версию) в путь сборки.
Скачать можно на Maven Central.
Скомпилируйте и запустите MethodMessenger.java.
Лицензия
Этот проект выпущен под The Unlicense – подробности в файле LICENSE.
Вы можете свободно использовать, модифицировать и распространять его без каких-либо ограничений или обязательств.
