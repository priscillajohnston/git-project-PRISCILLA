public class GitTester {

    public static void main(String args[]) throws Exception {
    
        // /* Your tester code goes here */
        // GitWrapper gw = new GitWrapper();
        // gw.add("myProgram/hello.txt");
        // gw.add("myProgram/inner/world.txt");
        // gw.commit("John Doe", "Initial commit");
        // //gw.checkout("1234567890");

        Git.createGitRepository();
        testing.createSampleFilesNested();
        testing.testAddToIndex();
        testing.testAddToIndex2();
        testing.testAddToIndexNested();
        testing.testWorkingList();
        testing.testCommit();
        testing.testSecondCommitAfterModification();
        testing.fullReset();

    }
    
}
