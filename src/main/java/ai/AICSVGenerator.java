package ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import csv.CSVWriterUtil;
import gptAI.parser.CsvDataParser;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AICSVGenerator {

    public static void getResultFromAIAndGenerateCsvFile(String requirement, List<String> inputPrompt, String JiraId, Map<String, String> rowData, AIClient aiClient) throws IOException {
        requirement = requirement + inputPrompt.get(0);
        Response aiRes = aiClient.getResponse(requirement);
        if (aiRes.statusCode()!=200){
            rowData.put("AI TC Status", aiRes.statusCode()+" : Something wrong with the AI server");
            return;
        }
        List<String> requestList = new ArrayList<>();
        requestList.add(requirement);
        List<String> gptResultList = new ArrayList<>();

        String gptResult = null;
        try {
            gptResult = aiClient.parseContent(aiRes.asString());
        } catch (Exception e) {
            rowData.put("AI TC Status", "AI response parsing issue");
            e.printStackTrace();
            return;
        }
        gptResultList.add(gptResult);
        for (int i = 1; i < inputPrompt.size(); i++) {
            requestList.add(inputPrompt.get(i));
            Response geminiResI = aiClient.getResponseMultipleReq(requestList, gptResultList);
            if (geminiResI.statusCode()!=200){
                rowData.put("AI TC Status", aiRes.statusCode()+" : Something wrong with the AI server Multi-Request");
                return;
            }
            String geminiRes = null;
            try {
                geminiRes = aiClient.parseContent(geminiResI.asString());
            } catch (Exception e) {
                rowData.put("AI TC Status", aiRes.statusCode()+" : Something wrong with the AI response body , Parsing failed");
                e.printStackTrace();
                return;
            }
            gptResultList.add(geminiRes);
        }
        String filePath = "target/" + JiraId + "_" + System.currentTimeMillis() + ".csv";
        for (String res : gptResultList) {
            List<String> tmpDataList = CsvDataParser.parse(res);
            CSVWriterUtil.appendListToCSV(tmpDataList, filePath, rowData);
        }
    }
}
