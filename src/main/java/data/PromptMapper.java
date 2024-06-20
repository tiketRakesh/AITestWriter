package data;

import data.constant.AccommodationPromptConstants;
import data.constant.FlightPromptConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main class for prompt mapping
public class PromptMapper {

    private Map<String, List<List<String>>> promptMapping;

    public PromptMapper() {
        promptMapping = new HashMap<>();
        initializeMappings();
    }

    private void initializeMappings() {
        // Add mappings for Accommodation
        List<List<String>> accommodationPrompts = new ArrayList<>();
        accommodationPrompts.add(AccommodationPromptConstants.INPUT_PROMPT_BE);
        accommodationPrompts.add(AccommodationPromptConstants.INPUT_PROMPT_FE);
        promptMapping.put("Accommodation", accommodationPrompts);

        // Add mappings for Flight
        List<List<String>> flightPrompts = new ArrayList<>();
        flightPrompts.add(FlightPromptConstants.INPUT_PROMPT_BE);
        flightPrompts.add(FlightPromptConstants.INPUT_PROMPT_FE);
        promptMapping.put("Flight", flightPrompts);
    }

    public List<List<String>> getPrompts(String key) {
        return promptMapping.get(key);
    }

    public Map<String, List<List<String>>> getPromptMapping() {
        return promptMapping;
    }
}

