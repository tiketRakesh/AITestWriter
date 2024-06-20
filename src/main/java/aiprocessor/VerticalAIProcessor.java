package aiprocessor;
import ai.AICSVGenerator;
import ai.AIClient;
import ai.AIClientFactory;
import data.PromptMapper;
import gSheet.GoogleDocsReader;
import gSheet.GoogleSheetReader;
import jira.core.JiraService;
import util.ConfigReader;
import java.util.List;
import java.util.Map;

public class VerticalAIProcessor {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: VerticalAIProcessor <verticalName> <sprintNumber> <aiType>");
            System.exit(1);
        }
        String verticalName = args[0];
        String sprintNumber = args[1];
        String aiType = args[2];
        PromptMapper mapper = new PromptMapper();
        List<List<String>> prompts = mapper.getPrompts(verticalName);
        if (prompts == null || prompts.isEmpty()) {
            throw new Exception("Prompt not defined for the given vertical name");
        }
        // Read the Google Sheet and store the values in a data structure
        String sheetName = "Sprint " + sprintNumber;
        Map<Integer, Map<String, String>> data = null;
        try {
            data = GoogleSheetReader.readData(sheetName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error in reading the master data sheet");
        }
        // Read the API key from the configuration
        String apiKey = ConfigReader.getValue("api.key." + aiType);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty API key for type: " + aiType);
        }
        AIClient aiClient = AIClientFactory.getAIClient(aiType, apiKey);
        try {
            processRows(data, prompts, aiClient);
        } finally {
            GoogleSheetReader.updateAIStatus(sheetName, data);
        }
        System.exit(0);
    }

    private static void processRows(Map<Integer, Map<String, String>> data, List<List<String>> prompts, AIClient aiClient) throws Exception {
        for (Map.Entry<Integer, Map<String, String>> entry : data.entrySet()) {
            Map<String, String> rowData = entry.getValue();
            String jiraId = rowData.get("JIRA ID").trim();

            if (isReadyForAI(rowData)) {
                String requirement = null;
                try {
                    requirement = fetchRequirement(rowData, jiraId);
                } catch (Exception e) {
                    rowData.put("AI TC Status", "Error in reading the requirement from the source [Jira/Doc]");
                    e.printStackTrace();
                    continue;
                }

                if (rowData.get("TC Type").trim().equals("BE")) {
                    AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(0), jiraId, rowData, aiClient);
                } else {
                    AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(1), jiraId, rowData, aiClient);
                }
            } else {
                System.out.println("Type not implemented or case is not ready for AI");
            }
        }
    }

    private static boolean isReadyForAI(Map<String, String> rowData) {
        return rowData.get("Ready for AI").trim().equals("Yes");
    }

    private static String fetchRequirement(Map<String, String> rowData, String jiraId) throws Exception {
        String requirement;
        if (rowData.get("Type").trim().equals("Jira")) {
            requirement = JiraService.getTheRequirementFromJira(jiraId);
        } else if (rowData.get("Type").trim().equals("Doc")) {
            String url = rowData.get("Doc Link").trim();
            requirement = GoogleDocsReader.readGoogleDoc(url);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + rowData.get("Type").trim());
        }
        return requirement;
    }
}

