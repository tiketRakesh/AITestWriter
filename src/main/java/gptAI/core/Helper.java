package gptAI.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import csv.CSVWriterUtil;
import gptAI.dto.Message;
import gptAI.dto.RequestBody;
import gptAI.parser.CsvDataParser;
import gptAI.parser.GPTResponseParser;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Helper {

    public static void getResultFromGptAIAndGenerateCsvFile(String requirement, List<String> inputPrompt, String JiraId, Map<String, String> rowData) throws IOException {
        //call the gemini  api to get all the content
        requirement = requirement + inputPrompt.get(0);
        Response geminiRes = getResponseFromGptApi(requirement);

        List<String> geminiRequestList = new ArrayList<>();
        geminiRequestList.add(requirement);
        List<String> gptResultList = new ArrayList<>();

        //12 March 2024
        //Parse the result from the gemini api response
        String gptResult = GPTResponseParser.parseContent(geminiRes.asString());
        gptResultList.add(gptResult);

        //Read the predefined steps for fining the result from the gemini
        //add one more requirement of adding non-functional test cases
        // Next task : make this method generic that can make multiple request based on the  inputPrompt array size .
        for (int i = 1; i < inputPrompt.size(); i++) {
            geminiRequestList.add(inputPrompt.get(i));
            String geminiResI = getResponseFromChatGPTApiMultipleReq(geminiRequestList, gptResultList);
            geminiResI = GPTResponseParser.parseContent(geminiResI);
            gptResultList.add(geminiResI);
        }

        String filePath = "target/" + JiraId + "_" + System.currentTimeMillis() + ".csv";
        for (String res : gptResultList) {
            //No need to write custom parser to write data in the CSV , just dump the data in the csv file
            List<String> tmpDataList = CsvDataParser.parse(res);
            CSVWriterUtil.appendListToCSV(tmpDataList, filePath,rowData);
        }
    }



    public static Response getResponseFromGptApi(String text) {
        // Replace with your actual API key
        String baseUrl = "https://api.openai.com/v1/chat/completions";
        String apiKey = "sk-proj-JlkzNRQreiWi2XmcPP2dT3BlbkFJMJQyeEhdLIG3l38gmFYw"; // Replace "YourAPIKey" with your actual API key
        // Construct the request body using Java objects
        // Create the message
        Message message = new Message("user", text);
        // Create the request body
        RequestBody requestBody = new RequestBody("gpt-4o", Arrays.asList(message));
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .urlEncodingEnabled(false)
                .contentType(ContentType.JSON)
                .body(requestBody).log().all()
                .post();

        System.out.println("Response code: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody().asString());

        return response;
    }




    private static String getResponseFromChatGPTApiMultipleReq(List<String> userRequests, List<String> chatGPTResult) {
        ObjectMapper mapper = new ObjectMapper();
        String baseUrl = "https://api.openai.com/v1/chat/completions";
        String apiKey = "sk-proj-JlkzNRQreiWi2XmcPP2dT3BlbkFJMJQyeEhdLIG3l38gmFYw"; // Replace "YourAPIKey" with your actual API key

        // Construct the request body using POJO classes
        List<Message> messages = new ArrayList<>();

        // Add initial user message
        messages.add(new Message("user", userRequests.get(0)));

        // Add system responses and follow-up user messages
        for (int i = 0; i < chatGPTResult.size(); i++) {
            messages.add(new Message("assistant", chatGPTResult.get(i)));
            if (i + 1 < userRequests.size()) {
                messages.add(new Message("user", userRequests.get(i + 1)));
            }
        }

        // Create the request body
        RequestBody requestBody = new RequestBody("gpt-4", messages);

        // Convert the request body to JSON
        String updatedRequestBodyJson = null;
        try {
            updatedRequestBodyJson = mapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Send the request
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(ContentType.JSON)
                .body(updatedRequestBodyJson).log().all()
                .post();

        System.out.println("Response code multi : " + response.getStatusCode());
        System.out.println("Response body multi : " + response.getBody().asString());
        return response.asString();
    }

}
