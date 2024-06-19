package models;
import java.util.List;
public record SupportedCodesXRAPI(
        String result,
        List<SupportedCodeXRAPI> supported_codes
) {
}

