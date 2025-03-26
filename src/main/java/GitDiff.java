import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GitDiff {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();

    public static ArrayList<String> getChangedFiles(String owner, String repo, String accessToken, String localRepoPath, String branchA, String branchB) {

        try {
            String mergeBase = GitDiff.getMergeBase(owner, repo, accessToken, branchA, branchB);
            Set<String> changesA = GitDiff.getChangesA(owner, repo, branchA, mergeBase, accessToken);
            ArrayList<String> changesB = GitDiff.executeCLI(branchB, mergeBase, localRepoPath);
            ArrayList<String> output = new ArrayList<>();
            for (String file : changesB) {
                if (changesA.contains(file)) {
                    output.add(file);
                }
            }
            return output;
        }
        catch (IOException e) { System.out.println(e.getMessage()); return null; }

    }
    public static String getMergeBase(String owner, String repo, String accessToken, String branchA, String branchB) throws IOException {

        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/compare/" + branchA + "..." + branchB;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + accessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        JsonNode jsonNode = objectMapper.readTree(responseBody);

        JsonNode mergeBaseCommit = jsonNode.get("merge_base_commit");
        if (mergeBaseCommit != null) {
            return mergeBaseCommit.get("sha").asText();
        } else {
            return null;
        }
    }
    public static Set<String> getChangesA(String owner, String repo, String branchA, String mergeBase, String accessToken) throws IOException {

        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/compare/" + mergeBase + "..." + branchA;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + accessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode filesNode = jsonNode.get("files");

        Set<String> changedFiles = new HashSet<>();
        if (filesNode != null) {
            for (JsonNode file : filesNode) {
                changedFiles.add(file.get("filename").asText());
            }
        }

        return changedFiles;
    }
    public static ArrayList<String> executeCLI(String branchB, String mergeBase, String localRepoPath) throws IOException {


        ArrayList<String> command = new ArrayList<>();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command.add("cmd.exe");
            command.add("/c");
        } else {
            command.add("sh");
            command.add("-c");
        }


        command.add("git diff --name-only " + mergeBase + " " + branchB);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(localRepoPath));
        Process process = builder.start();


        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        ArrayList<String> files = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            files.add(line);
        }


        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        return files;
    }

    public static void main(String[] args)
    {
        String owner = "pavladoljska";
        String repo = "test-repo";
        String accessToken = "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";
        String localRepoPath = "C:\\Users\\pavla\\OneDrive\\Desktop\\test-repo"; //"/c/Users/pavla/OneDrive/Desktop/test-repo";
        String branchA = "branchA";
        String branchB = "branchB";

        System.out.println(GitDiff.getChangedFiles(owner, repo, accessToken, localRepoPath, branchA, branchB));

    }
}
