import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class testing {
    public static void main(String[] args) throws IOException {
        testCommitOnce();

        //tree testing 
        // testTreeMakerOneNested();

        //checks indexContains
        // testIndexContains();

        // checks new addToIndex
        // testAddToIndexEmpty();
        // testAddToIndex2();
        // testAddToIndexNested();
        // testAddToIndex();

        // checks arraylist making
        // testArrayList();

        // // checks hash update
        // testHasher("blobTest", "Samples");

        // //checks cleanup and cycles
        // File gitFile = new File("git");
        // cleanup(gitFile);
        // cycles(gitFile);

        //fullReset();

    }

    public static void robustReset(){
        try{
            File objectsDir = new File("git/objects");
            if(objectsDir.exists() && objectsDir.isDirectory()){
                for (File random : objectsDir.listFiles()) {
                    if(random.isFile()){
                        random.delete();
                    }
                }
            }
            File indexFile = new File("git", "index");
            if(indexFile.exists()){
                Files.write(indexFile.toPath(), new byte[0]);
            }
            File samplesDir = new File("samples");
            if(samplesDir.exists()){
                deleteRecursively(samplesDir);
            }
            System.out.println("SUCCCCCESS");
        }
        catch (Exception e){
            System.out.println("Uh oh... look what happened: " + e.getMessage());
        }
    }

    public static void deleteRecursively(File file){
        if(file.exists()){
            if(file.isDirectory()){
                for (File subFile : file.listFiles()) {
                    deleteRecursively(subFile);
                }
            }
            file.delete();
        }
    }

    public static void testCommitOnce() throws IOException{
        Git.createGitRepository();
        createSampleFilesNested();
        testAddToIndex();
        testAddToIndex2();
        testAddToIndexNested();
        testWorkingList();
        testCommit();
        testSecondCommitAfterModification();
        robustReset();
    }

    public static void testCommitTwice() throws IOException{
        Git.createGitRepository();
        createSampleFilesNested();
        testAddToIndex();
        testAddToIndex2();
        testAddToIndexNested();
        testWorkingList();
        testCommit();
        testSecondCommitAfterModification();
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
        File file = new File("Samples", "hello.txt");
        String folderName = "Samples";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndex2() throws IOException {
        File file = new File("Samples", "test.txt");
        String folderName = "Samples";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndexEmpty() throws IOException {
        File file = new File("", "random.txt");
        String folderName = "";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndexNested() throws IOException {
        File file = new File("Samples/inner", "inside.txt");
        String folderName = "Samples/inner";
        Git.addToIndex(file, folderName);
    }

    public static void testAddToIndexNested2() throws IOException {
        File file = new File("Samples/inner", "committylitty.txt");
        String folderName = "Samples/inner";
        Git.addToIndex(file, folderName);
    }

    public static List<File> createSampleFilesNested() throws IOException {
        File samplesDir = new File("Samples");
        if (!samplesDir.exists()) {
            samplesDir.mkdir();
        }
        List<File> files = new ArrayList<>();
        String[] mainFileNames = {"hello", "test"};
        String[] mainContents = {"HELLOO PRISCILLAA", "6767"};
        for (int i = 0; i < mainFileNames.length; i++) {
            File file = new File(samplesDir, mainFileNames[i] + ".txt");
            Files.write(file.toPath(), mainContents[i].getBytes());
            files.add(file);
        }
        File subDir = new File(samplesDir, "Inner");
        if (!subDir.exists()) {
            subDir.mkdir();
        }
        File insideFile = new File(subDir, "inside.txt");
        Files.write(insideFile.toPath(), "hihi".getBytes());
        files.add(insideFile);
        return files;
    }

    public static void testIndexContains() throws IOException{
        File indexFile = new File("git", "index");
        indexFile.createNewFile();
        ArrayList<String> listy = Git.makeArrayFromIndexHelper(indexFile);
        String toCheck = "c65f99f8c5376adadddc46d5cbcf5762f9e55eb7 Samples/blobTest";
        System.out.println(Git.indexContains(indexFile, listy, toCheck));
    }

    public static void testAddModified() throws IOException{
        File file = new File("Samples/inner", "hi.txt");
        String folderName = "Samples/inner";
        Git.addToIndex(file, folderName);
    }

    public static void testTreeMakerOneNested() throws IOException{
        File file = new File("Samples");
        Git.makeTree("Samples");
    }

    public static void testWorkingList() throws IOException{
        System.out.println(Git.makeTree());
    }

    public static void testCommit() throws IOException{
        Git.commit("ellika", "best commit ever");
    }

    public static void testSecondCommitAfterModification() throws IOException{
        testAddToIndex2();
        Git.commit("talia", "best ever");
    }
}

