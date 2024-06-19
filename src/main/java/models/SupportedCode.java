package models;

public class SupportedCode {
    private String code;
    private String name;

    public SupportedCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
