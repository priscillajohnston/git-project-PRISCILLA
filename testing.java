import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class testing {
    public static void main(String[] args) throws IOException {
        checkExists();
        cleanup();
        cycles();
    }

    public static boolean checkExists() {
        File git = new File("git");
        if (!git.exists()) {
            return false;
        }
        File objects = new File("git/objects");
        if (!objects.exists()) {
            return false;
        }
        File index = new File("git/index");
        if (!index.exists()) {
            return false;
        }
        File head = new File("git/HEAD");
        if (!head.exists()) {
            return false;
        }
        return true;
    }

    public static boolean cleanup() throws IOException {
        if (checkExists()) {
            File gitFile = new File("git");
        File[] files = gitFile.listFiles();
        for(File file: files ){
            File current = file;
            current.delete();
        }
       gitFile.delete();
            return true;
        }
        return false;
    }


    public static void cycles() throws IOException {
        cleanup();
        Git.createGitRepository();
        cleanup();
        Git.createGitRepository();
        cleanup();
        Git.createGitRepository();
        cleanup();
        Git.createGitRepository();
        cleanup();
    }
}
