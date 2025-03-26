public class testHiddToken {

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Incorrect number of arguments");
            return;
        }
        String owner = args[0];
        String repo = args[1];
        String accessToken = System.getenv("GITHUB_TOKEN");;
        String localRepoPath = args[3];
        String branchA = args[4];
        String branchB = args[5];

        System.out.println(GitDiff.getChangedFiles(owner, repo, accessToken, localRepoPath, branchA, branchB));
    }
}
