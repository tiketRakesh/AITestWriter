package init;

import ai.AICSVGenerator;
import ai.AIClient;
import ai.AIClientFactory;
import jira.core.JiraService;
import data.PromptMapper;
import gSheet.GoogleDocsReader;
import gSheet.GoogleSheetReader;
import util.ConfigReader;

import java.util.List;
import java.util.Map;

public class start {

    public static void main(String[] args) throws Exception {
        String verticalName = args[0];
        System.out.println(": Hello World , there is a vertical name " + verticalName);
        String vertical = "Accommodation";        //Read the vertical name value from the cli
        if(1==1){
            return;
        }
        PromptMapper mapper = new PromptMapper();
        List<List<String>> prompts = mapper.getPrompts(vertical);
        if (prompts == null || prompts.isEmpty()) {
            throw new Exception("Prompt not defined for the given vertical name");
        }

        //read the gsheet  and store the values in a data structure
        String sheetName = "Sprint 1"; //Read it from the command line arugement
        Map<Integer, Map<String, String>> data = GoogleSheetReader.readData(sheetName);
        // Put the loop to fetch the test cases

        //AI client
            String aiType = "gemini";  //read from cli
        //String apiKey = "AIzaSyAeUDQ9TJOjo182KIDQG2kS0dGOl_qPmwA"; */ // read from properties files

        //String aiType = "gpt";  //read from cli
        //String apiKey = "sk-proj-JlkzNRQreiWi2XmcPP2dT3BlbkFJMJQyeEhdLIG3l38gmFYw";  // read from properties files
        String apiKey = ConfigReader.getValue("api.key."+aiType);
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty API key for type: " + aiType);
        }
        AIClient aiClient = AIClientFactory.getAIClient(aiType, apiKey);
        try {
            for (Map.Entry<Integer, Map<String, String>> entry : data.entrySet()) {
                // String jiraId = entry.getKey();
                Map<String, String> rowData = entry.getValue();
                String jiraId=rowData.get("JIRA ID").trim();
                if (rowData.get("Type").trim().equals("Jira") && rowData.get("Ready for AI").trim().equals("Yes")) {
                    //call the jira apis
                    String requirement = JiraService.getTheRequirementFromJira(jiraId);
                    // call the gemini apis for the test cases
                    if (rowData.get("TC Type").trim().equals("BE")) {
                        AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(0), jiraId,rowData,aiClient);
                    } else {
                        AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(1), jiraId,rowData,aiClient);
                    }
                } else if (rowData.get("Type").trim().equals("Doc") && rowData.get("Ready for AI").trim().equals("Yes")) {
                    String url = rowData.get("Doc Link").trim();
                    String requirement = GoogleDocsReader.readGoogleDoc(url);
                    // call the gemini apis for the test cases
                    if (rowData.get("TC Type").trim().equals("BE")) {
                        AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(0), jiraId,rowData,aiClient);
                    } else {
                        AICSVGenerator.getResultFromAIAndGenerateCsvFile(requirement, prompts.get(1), jiraId,rowData,aiClient);
                    }
                } else {
                    System.out.println("Type not implemented or case is not ready for AI");
                }
            }

        }finally {
            GoogleSheetReader.updateAIStatus(sheetName,data);
        }
    }


}

