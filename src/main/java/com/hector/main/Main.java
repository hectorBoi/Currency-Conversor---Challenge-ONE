package com.hector.main;

import com.google.gson.*;
import models.SupportedCodeXRAPI;
import models.SupportedCodes;
import models.SupportedCodesXRAPI;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String API_KEY = "ce6c2232170336d587d2f02d";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY;
    private static final String STANDARD_REQUEST_URL = BASE_URL + "/latest/USD";
    private static final String CODES_REQUEST_URL = BASE_URL + "/codes";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        SupportedCodes currentSupportedCodes = getSupportedCodes();
        if (currentSupportedCodes == null) return;

        while(true){
            System.out.println("The currently supported codes are the following");
            System.out.println(currentSupportedCodes);

            String baseCurrency = getCurrency(teclado, currentSupportedCodes, "Select the base currency to convert or write EXIT to finish");
            if (baseCurrency == null) return;

            String targetCurrency = getCurrency(teclado, currentSupportedCodes, "Select the target currency or write EXIT to finish");
            if (targetCurrency == null) return;

            float amount = getAmount(teclado, currentSupportedCodes, baseCurrency);
            if (amount <= 0) return;

            String pairConversionURL = BASE_URL + "/pair/" + baseCurrency + "/" + targetCurrency;

            // Additional API call to get the conversion rate (if needed)
            float desiredConversionRate = getConversionRate(pairConversionURL);
            System.out.println("Conversion rate: " + desiredConversionRate);
            var resultingAmount = amount*desiredConversionRate;
            System.out.println(amount + " " + currentSupportedCodes.searchName(baseCurrency) + " are equal to " + resultingAmount + " " + currentSupportedCodes.searchName(targetCurrency));
            System.out.println(amount + " " + baseCurrency + " => " + resultingAmount + " " + targetCurrency + "\n");

            System.out.println("To continue with the next conversion press Enter, otherwise write EXIT to leave the program");
            String nextInput = teclado.nextLine().strip().toUpperCase();
            switch(nextInput){
                case "EXIT":
                    return;
                default:
                    break;

            }
        }

    }

    @org.jetbrains.annotations.Nullable
    private static SupportedCodes getSupportedCodes() {
        try {
            HttpResponse<String> response = sendRequest(CODES_REQUEST_URL);
            JsonElement jsonElement = JsonParser.parseString(response.body());
            String result = jsonElement.getAsJsonObject().get("result").getAsString();
            JsonArray supportedCodesArray = jsonElement.getAsJsonObject().getAsJsonArray("supported_codes");

            List<SupportedCodeXRAPI> supportedCodesList = new ArrayList<>();
            for (JsonElement element : supportedCodesArray) {
                JsonArray codePair = element.getAsJsonArray();
                String code = codePair.get(0).getAsString();
                String name = codePair.get(1).getAsString();
                supportedCodesList.add(new SupportedCodeXRAPI(code, name));
            }

            SupportedCodesXRAPI xrapiSupportedCodes = new SupportedCodesXRAPI(result, supportedCodesList);
            return new SupportedCodes(xrapiSupportedCodes);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.out.println("It was not possible to connect with the API");
            return null;
        }
    }

    private static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String getCurrency(Scanner teclado, SupportedCodes supportedCodes, String prompt) {
        System.out.println(prompt);
        while (true) {
            String input = teclado.nextLine().strip().toUpperCase();
            if ("EXIT".equals(input)) {
                return null;
            } else if (supportedCodes.isSupported(input)) {
                System.out.println(supportedCodes.searchName(input) + " SELECTED");
                return input;
            } else {
                System.out.println("UNSUPPORTED code, enter a different one or write 'EXIT'");
            }
        }
    }

    private static float getAmount(Scanner teclado, SupportedCodes supportedCodes, String baseCurrency) {
        System.out.println("Enter the amount in " + supportedCodes.searchName(baseCurrency));
        while (true) {
            try {
                float amount = Float.parseFloat(teclado.nextLine());
                if (amount > 0) {
                    return amount;
                } else {
                    System.out.println("Amount must be positive. Enter again: ");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid float value: ");
            }
        }
    }

    private static float getConversionRate(String pairConversionURL) {
        try {
            HttpResponse<String> response = sendRequest(pairConversionURL);
            String body = response.body();

            // Parse the JSON response
            JsonElement jsonElement = JsonParser.parseString(body);
            String result = jsonElement.getAsJsonObject().get("result").getAsString();

            if ("success".equals(result)) {
                float conversionRate = jsonElement.getAsJsonObject().get("conversion_rate").getAsFloat();
                return conversionRate;
            } else {
                System.out.println("Failed to retrieve conversion rate");
                return 0;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

}
