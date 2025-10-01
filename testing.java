import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public class testing {
    public static void main(String[] args) throws IOException {

        // checks new addToIndex
        testAddToIndex();

        // checks arraylist making
        testArrayList();

        // checks hash update
        testHasher("blobTest", "Samples");

        // //checks cleanup and cycles
        // File gitFile = new File("git");
        // cleanup(gitFile);
        // cycles(gitFile);

        fullReset();

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

    public static boolean cleanup(File file) throws IOException {
        if (file.exists()) {

            File[] files = file.listFiles();
            for (File theFile : files) {
                File current = theFile;
                if (current.isDirectory()) {
                    cleanup(current);
                } else {
                    current.delete();
                }
            }
            file.delete();
            return true;
        }
        return false;
    }

    public static void cycles(File file) throws IOException {
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
        Git.createGitRepository();
        cleanup(file);
    }

    public static void testHasher(String fileName, String folder) throws IOException {
        Git.hashFile(fileName, folder);
    }

    public static void fullReset() throws IOException {
        File file = new File("git");
        cleanup(file);
        Git.createGitRepository();
    }

    public static void testArrayList() throws IOException {
        File indexFile = new File("git", "index");
        ArrayList<String> listy = Git.makeArrayFromIndexHelper(indexFile);
        System.out.println(listy.toString());

    }

    // also tests blob because adding to the index calls BLOB
    public static void testAddToIndex() throws IOException {
        File file = new File("Samples", "blobTest");
        String folderName = "Samples";
        Git.addToIndex(file, folderName);
    }
}
