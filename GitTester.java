public class GitTester {

    public static void main(String args[]) throws Exception {
        runCheckoutTest();
    }

    private static void runFullCycleTest() {
        System.out.println("=== GitWrapper Full Cycle Test (excluding checkout) ===");
        GitWrapper gw = new GitWrapper();
        try {
            // 1) Clean slate
            deleteRecursively(new java.io.File("git"));
            deleteQuietly("sampleA.txt");
            deleteQuietly("sampleB.txt");

            // 2) Init
            System.out.println("[init] Initializing repository...");
            gw.init();
            assertExists("git", "git directory");
            assertExists("git/objects", "git/objects");
            assertExists("git/index", "git/index");
            assertExists("git/HEAD", "git/HEAD");
            System.out.println("[init] OK");

            // 3) Create working files
            writeFile("sampleA.txt", "Hello A\n");
            writeFile("sampleB.txt", "Hello B\n");

            // 4) Add files (exercise add method)
            System.out.println("[add] Staging files...");
            try { gw.add("sampleA.txt"); System.out.println("[add] sampleA.txt added"); } 
            catch (Exception e) { System.out.println("[add] sampleA.txt FAILED: " + e.getMessage()); }
            try { gw.add("sampleB.txt"); System.out.println("[add] sampleB.txt added"); } 
            catch (Exception e) { System.out.println("[add] sampleB.txt FAILED: " + e.getMessage()); }
            System.out.println("[add] Done");

            // 5) Commit (exercise commit method)
            System.out.println("[commit] Committing staged changes...");
            String commitHash = null;
            try {
                commitHash = gw.commit("Test Author", "Initial commit from tester");
                System.out.println("[commit] New commit: " + commitHash);
            } catch (Exception e) {
                System.out.println("[commit] FAILED: " + e.getMessage());
            }

            // 6) Basic post-commit checks (best-effort)
            if (commitHash != null) {
                assertExists("git/objects/" + commitHash, "commit object");
            }
            System.out.println("=== Test complete ===");
        } catch (Exception e) {
            System.out.println("[fatal] Unexpected error: " + e.getMessage());
        }
    }

    private static void runCheckoutTest() throws Exception {
        System.out.println("=== GitWrapper Checkout Test ===");
        GitWrapper gw = new GitWrapper();
        try {
            // Clean slate
            deleteRecursively(new java.io.File("git"));
            deleteQuietly("sampleA.txt");
            deleteQuietly("sampleB.txt");
            deleteRecursively(new java.io.File("Inner"));

            // Init
            gw.init();
            assertExists("git", "git directory");

            // Commit 1: create initial files
            writeFile("sampleA.txt", "A1\n");
            new java.io.File("Inner").mkdirs();
            writeFile("Inner/inside.txt", "I1\n");
            gw.add("sampleA.txt");
            gw.add("Inner/inside.txt");
            String c1 = gw.commit("Tester", "commit 1");
            System.out.println("[commit1] " + c1);

            // Commit 2: modify files, add another
            writeFile("sampleA.txt", "A2\n");
            writeFile("Inner/inside.txt", "I2\n");
            writeFile("sampleB.txt", "B2\n");
            gw.add("sampleA.txt");
            gw.add("Inner/inside.txt");
            gw.add("sampleB.txt");
            String c2 = gw.commit("Tester", "commit 2");
            System.out.println("[commit2] " + c2);

            // Now checkout commit 1
            System.out.println("[checkout] Restoring to commit1...");
            gw.checkout(c1);

            // Validate files match commit1 contents and sources still exist
            assertFileContent("sampleA.txt", "A1\n");
            assertFileContent("Inner/inside.txt", "I1\n");
            assertExists("Git.java", "Git.java");
            assertExists("GitWrapper.java", "GitWrapper.java");
            assertExists("README.md", "README.md");
            System.out.println("=== Checkout test complete ===");
        } catch (Exception e) {
            System.out.println("[fatal] Unexpected error: " + e.getMessage());
            throw e;
        }
    }

    private static void assertExists(String path, String label) {
        java.io.File f = new java.io.File(path);
        if (!f.exists()) {
            throw new RuntimeException("Missing " + label + ": " + path);
        }
    }

    private static void writeFile(String path, String content) throws java.io.IOException {
        java.nio.file.Files.write(java.nio.file.Paths.get(path), content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static void assertFileContent(String path, String expected) throws java.io.IOException {
        String actual = java.nio.file.Files.readString(java.nio.file.Paths.get(path), java.nio.charset.StandardCharsets.UTF_8);
        if (!actual.equals(expected)) {
            throw new RuntimeException("Content mismatch for " + path + "\nExpected: " + expected + "\nActual: " + actual);
        }
    }

    private static void deleteQuietly(String path) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) f.delete();
    }

    private static void deleteRecursively(java.io.File file) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            java.io.File[] kids = file.listFiles();
            if (kids != null) {
                for (java.io.File k : kids) deleteRecursively(k);
            }
        }
        file.delete();
    }
    
}


