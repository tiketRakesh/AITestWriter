package data.constant;

import java.util.ArrayList;
import java.util.List;

public class FlightPromptConstants {
    public static final List<String> INPUT_PROMPT_BE;
    public static final List<String> INPUT_PROMPT_FE;

    static {
        INPUT_PROMPT_BE = new ArrayList<>();
        INPUT_PROMPT_BE.add("Suggest some test cases for this software feature");
        INPUT_PROMPT_BE.add("Add test cases related to non-functional scenarios");
        INPUT_PROMPT_BE.add("Add test cases related to the system failure");

        INPUT_PROMPT_FE = new ArrayList<>();
        INPUT_PROMPT_FE.add("Suggest some test cases for this software feature");
        INPUT_PROMPT_FE.add("Add test cases related to non-functional scenarios");
    }

}
