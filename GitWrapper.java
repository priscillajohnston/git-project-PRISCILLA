import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitWrapper {

    /**
     * Initializes a new Git repository.
     * This method creates the necessary directory structure
     * and initial files (index, HEAD) required for a Git repository.
     * @throws IOException 
     */
    public void init() throws IOException {
        Git.createGitRepository();
    };

    /**
     * Stages a file for the next commit.
     * This method adds a file to the index file.
     * If the file does not exist, it throws an IOException.
     * If the file is a directory, it throws an IOException.
     * If the file is already in the index, it does nothing.
     * If the file is successfully staged, it creates a blob for the file.
     * @param filePath The path to the file to be staged.
     * @throws Exception 
     */
    public void add(String filePath) throws Exception {
        File file = new File(filePath);
        if(!file.exists()){
            throw new Exception("Excuse you! That file does not exist.");
        }
        if(file.isDirectory()){
            throw new Exception("Excuse you! That file is a directory so we cannot add it.");
        }
        Git.addToIndex(file, "");

    };

    /**
     * Creates a commit with the given author and message.
     * It should capture the current state of the repository by building trees based on the index file,
     * writing the tree to the objects directory,
     * writing the commit to the objects directory,
     * updating the HEAD file,
     * and returning the commit hash.
     * 
     * The commit should be formatted as follows:
     * tree: <tree_sha>
     * parent: <parent_sha>
     * author: <author>
     * date: <date>
     * summary: <summary>
     *
     * @param author  The name of the author making the commit.
     * @param message The commit message describing the changes.
     * @return The SHA1 hash of the new commit.
     * @throws IOException 
     */
    public String commit(String author, String message) throws IOException {
        Git.commit(author, message);
        Path headPath = Paths.get("git/HEAD");
        if(!Files.exists(headPath)){
            throw new IOException("Head file does not exist...");
        }
        String headContents = Files.readString(headPath).trim();
        if(headContents.isEmpty()){
            throw new IOException("Bro there is no last commit... unsuccessful!");
        }
        return headContents;
    };

     /**
     * EXTRA CREDIT:
     * Checks out a specific commit given its hash.
     * This method should read the HEAD file to determine the "checked out" commit.
     * Then it should update the working directory to match the
     * state of the repository at that commit by tracing through the root tree and
     * all its children.
     *
     * @param commitHash The SHA1 hash of the commit to check out.
     * @throws Exception 
     */
    public void checkout(String commitHash) throws Exception {
        // Validate the target commit exists and is reachable from HEAD
        File targetCommitFile = new File("git/objects", commitHash);
        if (!targetCommitFile.exists()) {
            throw new Exception("Commit does not exist: " + commitHash);
        }

        Path headPath = Paths.get("git/HEAD");
        if (!Files.exists(headPath)) {
            throw new Exception("HEAD does not exist. Initialize and commit first.");
        }
        String currentHash = Files.readString(headPath, StandardCharsets.UTF_8).trim();
        if (currentHash.isEmpty()) {
            throw new Exception("HEAD is empty. No commits to traverse.");
        }

        boolean found = false;
        while (true) {
            if (currentHash.equals(commitHash)) { found = true; break; }
            File currentCommit = new File("git/objects", currentHash);
            if (!currentCommit.exists()) break;
            List<String> commitLines = Files.readAllLines(currentCommit.toPath(), StandardCharsets.UTF_8);
            if (commitLines.size() < 2) break;
            String parentLine = commitLines.get(1); // "parent: <hash>" or empty
            String[] parts = parentLine.split(": ", 2);
            String parent = parts.length == 2 ? parts[1].trim() : "";
            if (parent.isEmpty()) break; // reached root without match
            currentHash = parent;
        }
        if (!found) {
            throw new Exception("Specified commit is not reachable from HEAD: " + commitHash);
        }

        // Resolve the root tree of the target commit
        List<String> targetLines = Files.readAllLines(targetCommitFile.toPath(), StandardCharsets.UTF_8);
        if (targetLines.isEmpty()) {
            throw new Exception("Commit file is empty: " + commitHash);
        }
        String treeLine = targetLines.get(0); // "tree: <treeHash>"
        String[] treeParts = treeLine.split(": ", 2);
        if (treeParts.length != 2) {
            throw new Exception("Malformed commit (missing tree): " + commitHash);
        }
        String rootTreeHash = treeParts[1].trim();

        // Clean working directory except for git/
        cleanWorkingDirectory();

        // Restore files/dirs from the tree
        restoreTreeInto(rootTreeHash, new File("."));
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

    public static void restoreTree(String hashString) throws Exception {
        restoreTreeInto(hashString, new File("."));
    }

    private static void restoreTreeInto(String treeHash, File baseDir) throws Exception {
        File treeFile = new File("git/objects", treeHash);
        if (!treeFile.exists()) {
            throw new Exception("Tree object not found: " + treeHash);
        }
        List<String> lines = Files.readAllLines(treeFile.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(" ", 3);
            if (parts.length < 3) continue;
            String kind = parts[0];
            String childHash = parts[1];
            String name = parts[2];

            if ("blob".equals(kind)) {
                File outFile = new File(baseDir, name);
                File blobFile = new File("git/objects", childHash);
                String content = Files.readString(blobFile.toPath(), StandardCharsets.UTF_8);
                if (outFile.getParentFile() != null) {
                    outFile.getParentFile().mkdirs();
                }
                Files.write(outFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
            } else if ("tree".equals(kind)) {
                File newDir = new File(baseDir, name);
                newDir.mkdirs();
                restoreTreeInto(childHash, newDir);
            }
        }
    }

    private static void cleanWorkingDirectory() {
        File cwd = new File(".");
        File[] entries = cwd.listFiles();
        if (entries == null) return;
        for (File entry : entries) {
            if (entry.getName().equals("git")) continue;
            if (entry.isDirectory()) {
                deleteRecursively(entry);
                continue;
            }
            String name = entry.getName().toLowerCase();
            if (name.endsWith(".txt")) {
                entry.delete();
            }
        }
    }

    public void alternativeCheckout(String commitHash) throws Exception {
        // to-do: implement functionality here
        File commitFile = new File("git/objects", commitHash);
        if(!commitFile.exists()){
            throw new Exception("There is no commit file to speak of. Try again");
        }
        File headFile = new File("git/HEAD");
        Path headPath = Paths.get("git/HEAD");
        String currentHash = Files.readString(headPath, StandardCharsets.UTF_8);
        
        while(!currentHash.equals(commitHash)){
            File currentCommit = new File("git/objects", currentHash);
            currentHash = Files.readString(currentCommit.toPath()).split("\n")[1].split(" ")[1];
        }
        File checkout = new File("git/objects", currentHash);
        File samplesDir = new File("samples");
            if(samplesDir.exists()){
                deleteRecursively(samplesDir);
            }
        restoreTree(currentHash);
        //loop though to the tree in the file
        //then delete all the files outside of git
    }

    public static void restoreTree2(String hashString) throws Exception{ //maybe give it the directory
        //implement now
        // go to tree file then restore the things inside then loop again and write to text files etc keep lopping
        File treeFile = new File("git/objects", hashString);
        if(!treeFile.exists()){
            throw new Exception("That doesnt exist, try again.");
        }
        List<String> lines = Files.readAllLines(treeFile.toPath(), StandardCharsets.UTF_8);
        for (String string : lines) {
            String[] parts = string.split(" ");
            String blobTree = parts[0];
            String hashOfNext = parts[1];
            String name = parts[2];
            if(blobTree.equals("blob")){
                File textFile = new File("", name);
                File blobFile = new File("git/objects", hashOfNext);
                String content = Files.readString(blobFile.toPath(), StandardCharsets.UTF_8);
                Files.write(textFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
            }
            else if(blobTree.equals("tree")){
                File newDirectory = new File(name);
                newDirectory.mkdir();
                restoreTree2(hashString);
            }
        }
    }
}

