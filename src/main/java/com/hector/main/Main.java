package com.hector.main;

import com.google.gson.*;
import models.ConversionRecord;
import models.SupportedCodeXRAPI;
import models.SupportedCodes;
import models.SupportedCodesXRAPI;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String API_KEY = "ce6c2232170336d587d2f02d";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY;
    private static final String STANDARD_REQUEST_URL = BASE_URL + "/latest/USD";
    private static final String CODES_REQUEST_URL = BASE_URL + "/codes";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static List<ConversionRecord> conversionRecords = new ArrayList<>();
    private static final String RECORDS_FILE_PATH = "records/conversionRecords.json";
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        loadConversionRecords();

        SupportedCodes currentSupportedCodes = getSupportedCodes();
        if (currentSupportedCodes == null) return;

        while(true){
            if(conversionRecords.isEmpty()){
                System.out.println("\nWrite 'C' to start a new Conversation");
            }else{
                System.out.println("\nWrite 'H' to access conversion History,\nWrite 'C' to start a new Conversation");
            }
            switch (teclado.nextLine().strip().toUpperCase()) {
                case "H" -> printConversionRecords(conversionRecords);
                case "C" -> {
                    //Conversion Routine
                    while (true) {
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
                        var resultingAmount = amount * desiredConversionRate;
                        System.out.println(amount + " " + currentSupportedCodes.searchName(baseCurrency) + " are equal to " + resultingAmount + " " + currentSupportedCodes.searchName(targetCurrency));
                        System.out.println(amount + " " + baseCurrency + " => " + resultingAmount + " " + targetCurrency + "\n");

                        var conversionRecord = new ConversionRecord(baseCurrency, amount, targetCurrency, resultingAmount, new Date());
                        addConversionRecord(conversionRecord);
                        saveConversionRecords();
                        System.out.println("To continue with the next conversion press Enter, otherwise write EXIT to leave the program");
                        String nextInput = teclado.nextLine().strip().toUpperCase();
                        if (nextInput.equals("EXIT")) {
                            System.out.println(conversionRecords);
                            return;
                        }
                    }
                }
                case "EXIT" -> {
                    System.out.println("--------------------------------------------------------");
                    return;
                }
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
                return jsonElement.getAsJsonObject().get("conversion_rate").getAsFloat();
            } else {
                System.out.println("Failed to retrieve conversion rate");
                return 0;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    private static void addConversionRecord(ConversionRecord conversionRecord) {
        conversionRecords.add(conversionRecord);
    }

    private static void saveConversionRecords() {
        try (Writer writer = new FileWriter(RECORDS_FILE_PATH)) {
            GSON.toJson(conversionRecords, writer);
        } catch (IOException e) {
            System.out.println("Error occurred while saving the file\n" + e.getMessage() );
        }
    }

    private static void loadConversionRecords() {
    try (Reader reader = new FileReader(RECORDS_FILE_PATH)){
        ConversionRecord[] recordsArray = GSON.fromJson(reader, ConversionRecord[].class);
        if (recordsArray != null) {
            conversionRecords = new ArrayList<>(List.of(recordsArray));
        }
    }catch(FileNotFoundException e) {
        System.out.println("No existing conversion records found.");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }
    private static void printConversionRecords(List<ConversionRecord> conversionRecords) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        if (conversionRecords.isEmpty()) {
            System.out.println("No conversion records found.");
            return;
        }

        System.out.println("Conversion Records:");
        System.out.println("--------------------------------------------------------");

        for (ConversionRecord record : conversionRecords) {
            String formattedDate = dateFormat.format(record.getTimeConversion());
            System.out.println("Base Currency: " + record.getBaseCurrency());
            System.out.println("Target Currency: " + record.getTargetCurrency());
            System.out.println("Base Amount: " + record.getBaseAmount());
            System.out.println("Resulting Amount: " + record.getResultingAmount());
            System.out.println("Time of Conversion: " + formattedDate);
            System.out.println("--------------------------------------------------------");
        }
    }

}
