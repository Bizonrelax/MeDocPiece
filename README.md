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
