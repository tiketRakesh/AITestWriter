package gSheet;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleDocsReader {

    private static final String CREDENTIALS_FILE_PATH = "/oauth2.json";
    // need to change this with ticket service account once api for doc is enabled , Need to raise a request to the tiket IT team .


    public static String extractDocumentId(String url) {
        String regex = "https://docs\\.google\\.com/document/d/([a-zA-Z0-9-_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid Google Docs URL: " + url);
        }
    }

    public static String getAccessToken() throws IOException {
        InputStream credentialsStream = SheetsServiceUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (credentialsStream == null) {
            throw new FileNotFoundException("Credentials file not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/documents.readonly"));
        credentials.refreshIfExpired();
        AccessToken token = credentials.getAccessToken();
        return token.getTokenValue();
    }

    public static String readGoogleDoc(String documentUrl) throws IOException {
        String documentId = extractDocumentId(documentUrl);
        System.out.println("document id is "+documentId);
        String accessToken = getAccessToken();
        String url = "https://docs.googleapis.com/v1/documents/" + documentId;
        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .get(url);
        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.asString());
            JSONArray bodyContent = jsonResponse.getJSONObject("body").getJSONArray("content");
            StringBuilder documentText = new StringBuilder();

            for (int i = 0; i < bodyContent.length(); i++) {
                JSONObject element = bodyContent.getJSONObject(i);
                if (element.has("paragraph")) {
                    JSONArray elements = element.getJSONObject("paragraph").getJSONArray("elements");
                    for (int j = 0; j < elements.length(); j++) {
                        JSONObject textRun = elements.getJSONObject(j).getJSONObject("textRun");
                        if (textRun.has("content")) {
                            documentText.append(textRun.getString("content"));
                        }
                    }
                }
            }
            return documentText.toString();
        } else {
            throw new IOException("Failed to retrieve document: " + response.statusLine());
        }
    }
}
