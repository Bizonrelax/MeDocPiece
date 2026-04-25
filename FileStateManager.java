import java.io.*;
import java.util.Properties;

public class FileStateManager {

    private static final File CONFIG_DIR = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "MeDocPiece");

    public static void saveCheckBoxStates(File javaFile, Properties states) {
        File stateFile = getStateFileFor(javaFile);
        try (FileOutputStream fos = new FileOutputStream(stateFile)) {
            states.store(fos, "Checkbox states for " + javaFile.getName());
        } catch (IOException ignored) {
        }
    }

    public static Properties loadCheckBoxStates(File javaFile) {
        Properties props = new Properties();
        File stateFile = getStateFileFor(javaFile);
        if (stateFile.exists()) {
            try (FileInputStream fis = new FileInputStream(stateFile)) {
                props.load(fis);
            } catch (IOException ignored) {
            }
        }
        return props;
    }

    private static File getStateFileFor(File javaFile) {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
        String name = javaFile.getName().replace(".java", ".states");
        return new File(CONFIG_DIR, name);
    }
}