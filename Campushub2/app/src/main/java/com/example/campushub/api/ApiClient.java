package com.example.campushub.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL =
            "http://192.168.0.4:8000/";

    public static String post(String endpoint, String jsonBody) throws Exception {

        URL url = new URL(BASE_URL + endpoint);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );
        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (OutputStream outputStream =
                     connection.getOutputStream()) {

            outputStream.write(
                    jsonBody.getBytes(StandardCharsets.UTF_8)
            );
        }

        int responseCode = connection.getResponseCode();

        BufferedReader reader;

        if (responseCode >= 200 && responseCode < 400) {
            reader = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    )
            );
        } else {
            reader = new BufferedReader(
                    new InputStreamReader(
                            connection.getErrorStream()
                    )
            );
        }

        StringBuilder response = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }
}