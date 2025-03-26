public class testCLI {

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Incorrect number of arguments");
            return;
        }
        String owner = args[0];
        String repo = args[1];
        String accessToken = args[2];
        String localRepoPath = args[3];
        String branchA = args[4];
        String branchB = args[5];

        System.out.println(GitDiff.getChangedFiles(owner, repo, accessToken, localRepoPath, branchA, branchB));
    }
}
