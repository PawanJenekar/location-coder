package com.app.LocationCoder;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <path-to-json-file>");
            System.exit(1);
        }

        String prnNumber = args[0];
        String jsonFilePath = args[1];

        // Validate PRN number
        if (prnNumber.trim().isEmpty()) {
            System.err.println("PRN Number cannot be empty.");
            System.exit(1);
        }

        // Generate random string
        String randomString = generateRandomString(8);

        // Read and parse JSON file
        String destinationValue = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
            destinationValue = findFirstDestination(rootNode);
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            System.exit(1);
        }

        if (destinationValue == null) {
            System.err.println("Key 'destination' not found in the JSON file.");
            System.exit(1);
        }

        // Generate MD5 hash
        String concatenatedString = prnNumber + destinationValue + randomString;
        String hash = generateMD5Hash(concatenatedString);

        // Print output
        System.out.println(hash + ";" + randomString);
    }

    private static String findFirstDestination(JsonNode node) {
        if (node.isObject()) {
            for (JsonNode childNode : node) {
                if (childNode.has("destination")) {
                    return childNode.get("destination").asText();
                }
                String result = findFirstDestination(childNode);
                if (result != null) {
                    return result;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                String result = findFirstDestination(arrayElement);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            randomString.append(characters.charAt(random.nextInt(characters.length())));
        }
        return randomString.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}

