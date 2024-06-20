package ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import geminiAI.dto.Content;
import geminiAI.dto.ContentPart;
import geminiAI.dto.RequestBody;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import util.ConfigReader;

import java.util.ArrayList;
import java.util.List;

public class GeminiClient implements AIClient{

    private String apiKey;
    private  String baseUrl= ConfigReader.getValue("baseUrl.gemini");
    public GeminiClient(String apiKey) {
        this.apiKey = apiKey;
    }


    @Override
    public Response getResponse(String text) {
       // String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

        // Create a single content part for user
        ContentPart part = new ContentPart(text);
        List<ContentPart> parts = new ArrayList<>();
        parts.add(part);

        // Add parts to content
        Content content = new Content(null, parts);
        List<Content> contents = new ArrayList<>();
        contents.add(content);

        // Create request body
        RequestBody requestBody = new RequestBody(contents);

        // Send request
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .queryParam("key", apiKey).urlEncodingEnabled(false)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .log().all()
                .post();

        System.out.println("Response code: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody().asString());

        return response;
    }


    @Override
    public Response getResponseMultipleReq(List<String> userRequests, List<String> previousResults) {
        ObjectMapper mapper = new ObjectMapper();
       // String baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

        // List to hold contents
        List<Content> contents = new ArrayList<>();

        // Create user request part
        List<ContentPart> userParts1 = new ArrayList<>();
        userParts1.add(new ContentPart(userRequests.get(0)));
        Content userContent1 = new Content("user", userParts1);
        contents.add(userContent1);

        // Create model response part
        List<ContentPart> modelParts = new ArrayList<>();
        modelParts.add(new ContentPart(previousResults.get(0)));
        Content modelContent = new Content("model", modelParts);
        contents.add(modelContent);

        // Create another user request part
        List<ContentPart> userParts2 = new ArrayList<>();
        userParts2.add(new ContentPart(userRequests.get(1)));
        Content userContent2 = new Content("user", userParts2);
        contents.add(userContent2);

        // Create request body
        RequestBody requestBody = new RequestBody(contents);
        String updatedRequestBodyJson = null;

        try {
            updatedRequestBodyJson = mapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Send request
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .queryParam("key", apiKey).urlEncodingEnabled(false)
                .contentType(ContentType.JSON)
                .body(updatedRequestBodyJson)
                .log().all()
                .post();
        return response;
    }


    @Override
    public String parseContent(String aiResult) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(aiResult);
        JsonNode partsNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0);
        String text = partsNode.path("text").asText();
        return text;
    }
}
