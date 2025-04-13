package com.example.opticview.api;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqApiClient {

    public interface GroqResponseListener {
        void onSuccess(String description);
        void onFailure(String error);
    }

    // Accepts Bitmap instead of URL
    public static void sendImagetoGroq(Bitmap bitmap, GroqResponseListener listener) {
        String apiKey = "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";

        // Convert Bitmap to Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);  // 70% quality
        byte[] imageBytes = outputStream.toByteArray();
        String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        OkHttpClient client = new OkHttpClient();

        try {
            // Image part
            JSONObject imageObj = new JSONObject();
            imageObj.put("type", "image_url");

            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", "data:image/jpeg;base64," + base64Image);
            imageObj.put("image_url", imageUrlObj);

            // Text prompt
            JSONObject textObj = new JSONObject();
            textObj.put("type", "text");
            textObj.put("text", "You are a smart assistant helping a visually impaired person understand their surroundings. " +
                    "Describe everything clearly and simply. Mention nearby people, objects, signs, and movement. " +
                    "Alert about any potential danger like vehicles, stairs, edges, fire, or approaching individuals. " +
                    "Use short, clear, and easy-to-understand language suitable for spoken audio. Avoid mentioning the word 'image'.");

            JSONArray content = new JSONArray();
            content.put(textObj);
            content.put(imageObj);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", content);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject body = new JSONObject();
            body.put("model", "meta-llama/llama-4-maverick-17b-128e-instruct");
            body.put("messages", messages);

            RequestBody requestBody = RequestBody.create(
                    MediaType.get("application/json"),
                    body.toString()
            );

            Request request = new Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    listener.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        listener.onFailure("Server error: " + response.code() + " - " + response.body().string());
                        return;
                    }

                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray choices = json.getJSONArray("choices");
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.getJSONObject("message");
                        String content = message.getString("content");

                        listener.onSuccess(content.trim());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFailure("Parsing error: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            listener.onFailure("JSON error: " + e.getMessage());
        }
    }
}
