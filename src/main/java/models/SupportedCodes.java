package models;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;


import java.util.List;
import java.util.stream.Collectors;

public class SupportedCodes {

    private List<SupportedCode> supported_codes;
    public int maxNameLenght = 0;

    public SupportedCodes(SupportedCodesXRAPI supportedCodesXRAPI) {
        this.supported_codes = supportedCodesXRAPI.supported_codes().stream()
                .map(supportedCodeXRAPI -> new SupportedCode(supportedCodeXRAPI.code(), supportedCodeXRAPI.name()))
                .collect(Collectors.toList());
        getLongestNameLenght();
    }


    public boolean isSupported(String code) {
        for (SupportedCode supportedCode : supported_codes) {
            if (supportedCode.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    //looks for the name that matches the code.
    public String searchName(String code) {
        for (SupportedCode supportedCode : supported_codes) {
            if (supportedCode.getCode().equals(code)) {
                return supportedCode.getName();
            }
        }
        return "Unsupported";
    }

    public void getLongestNameLenght() {
        for (SupportedCode supportedCode : supported_codes) {
            if (supportedCode.getName().length() > maxNameLenght) maxNameLenght = supportedCode.getName().length();
        }
    }

    @Override
    public String toString() {
        StringBuilder codesString = new StringBuilder();
        var terminalWidth = jline.TerminalFactory.get().getWidth();


        int codeCount = 0;
        int codesPLine = 4;

        for (SupportedCode supportedCode : supported_codes) {
            int hyphenCount = maxNameLenght - supportedCode.getName().length() + 1;

            codesString.append(supportedCode.getName())
                    .append("-".repeat(hyphenCount))
                    .append(" ")
                    .append(supportedCode.getCode())
                    .append(" ".repeat(4));

            codeCount += 1;

            if (codeCount >= codesPLine) {
                codesString.append(System.lineSeparator());
                codeCount = 0; // Reset the counter
            }
        }

// If you want to ensure the last line doesn't end with an unnecessary line separator, you might want to trim it
        if (codesString.length() > 0 && codesString.charAt(codesString.length() - 1) == '\n') {
            codesString.setLength(codesString.length() - 1);
        }
        return codesString.toString();
    }
}
