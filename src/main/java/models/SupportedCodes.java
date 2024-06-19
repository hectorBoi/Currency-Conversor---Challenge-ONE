package models;

import java.util.List;
import java.util.stream.Collectors;

public class SupportedCodes {

    private List<SupportedCode> supported_codes;
    public int maxNameLenght = 0;
    public SupportedCodes(SupportedCodesXRAPI supportedCodesXRAPI){
        this.supported_codes = supportedCodesXRAPI.supported_codes().stream()
                .map(supportedCodeXRAPI -> new SupportedCode(supportedCodeXRAPI.code(), supportedCodeXRAPI.name()))
                .collect(Collectors.toList());
        getLongestNameLenght();
    }


    public boolean isSupported(String code) {
        for (SupportedCode supportedCode : supported_codes) {
            if(supportedCode.getCode().equals(code)){
                return true;
            }
        }
        return false;
    }

    //looks for the name that matches the code.
    public String searchName(String code){
        for (SupportedCode supportedCode : supported_codes) {
            if(supportedCode.getCode().equals(code)) {
                return supportedCode.getName();
            }
        }
        return "Unsupported";
    }

    public void getLongestNameLenght(){
        for (SupportedCode supportedCode : supported_codes) {
            if(supportedCode.getName().length() > maxNameLenght) maxNameLenght = supportedCode.getName().length();
        }
    }

    @Override
    public String toString() {

        StringBuilder codesString = new StringBuilder();

        supported_codes.forEach(supportedCode ->{
            int hyphenCount = maxNameLenght - supportedCode.getName().length() + 1;
            codesString.append(supportedCode.getName())
                    .append(" ")
                    .append("-".repeat(hyphenCount))
                    .append(" ")
                    .append(supportedCode.getCode())
                    .append(System.lineSeparator());
                }
        );
        return codesString.toString();
    }
}
