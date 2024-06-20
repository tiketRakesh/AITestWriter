package jira.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import util.ConfigReader;

public class JiraService {

    public static String getTheRequirementFromJira(String JiraId) throws JsonProcessingException {
        //call the jira api to get all the content
        String jsonResponse = getJiraContent(JiraId);
        return getRequiremntTextFromTheJiraApiResponse(jsonResponse);

    }


    private static String getJiraContent(String JiraId) {
        // Set base URI
        RestAssured.baseURI = "https://borobudur.atlassian.net/rest/api/3";
        // Authorization token
        String authToken = getAuthToken();//"cmFrZXNoLnNpbmdoQHRpa2V0LmNvbTpBVEFUVDN4RmZHRjBoSlNjRXIzajdrYWtpSFZlb3FjRVZQSk5nWUFCby02NVVUUVBjbVlWUHE2b1Ytd2M4eV80WnlVcUJYN2RsSWpwaC1GS1ZsVFJMYVQzYlhHNWZFbnlLcmFEdl90WFFiM1FFT3lGbi1ZcDdUMGZfOGNPZlJUX0FHX0FVZ09sSXZ6aVhGTzBMYmhLZEQ1RUloOWJyUVRFcmFDZTRPZy1KRHJYZkprbGNaREdzVk09OEIxNzUyNzM=";
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty Jira Auth token key :" + authToken);
        }
        // Send request and get response
        return RestAssured.given()
                .header("singleRequest.Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Basic " + authToken)
                .header("Cookie", "atlassian.xsrf.token=dfadd0249b3f13070f62043f3746fe5e4fb843c5_lin")
                .body("{\"fields\": [\"description\"]}")
                .when()
                .get("/issue/" + JiraId + "")
                .then()
                .extract().response().asString();
    }

    private static String getAuthToken() {
        // Attempt to read from environment variable
        String authToken = System.getenv("jira.auth.key");
        if (authToken != null && !authToken.isEmpty()) {
            return authToken;
        }
        // Fallback to reading from properties file
        return ConfigReader.getValue("jira.auth.key");
    }


    private static String getRequiremntTextFromTheJiraApiResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Parse JSON string to JsonNode
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        // Fetch the value of the description object
        JsonNode descriptionNode = jsonNode.path("fields").path("description");
        // Check if descriptionNode is not null and if it has value
        // Check if descriptionNode is not null and if it has value
        if (!descriptionNode.isMissingNode() && descriptionNode.isObject()) {
            // Fetch the content array
            JsonNode contentArray = descriptionNode.path("content");

            // Check if contentArray is not null and if it is an array
            if (contentArray.isArray() && contentArray.size() > 0) {
                // Fetch the first item of the content array
                JsonNode firstItem = contentArray.get(0);

                // Check if firstItem is not null and if it has a text field
                if (firstItem.isObject() && firstItem.has("type") && firstItem.get("type").asText().equals("paragraph")) {
                    // Fetch the text field under the paragraph
                    JsonNode textNode = firstItem.path("content").get(0).path("text");

                    // Check if textNode is not null and if it has a text value
                    if (!textNode.isMissingNode() && textNode.isTextual()) {
                        // Get the text value and print
                        return textNode.asText();
                    } else {
                        System.out.println("Text value not found or is empty.");
                    }
                } else {
                    System.out.println("Paragraph type not found or is empty.");
                }
            } else {
                System.out.println("singleRequest.Content array not found or is empty.");
            }
        } else {
            System.out.println("Description not found or is empty.");
        }
        return null;
    }



}
