package com.example.opticview.api;

public class GroqApiClient {
    public static void sendImagetoGroq(Bitmap image, Callback callback) {
        String base64Image = "data:image/jpeg;base64," + encodeImageToBase64(image);
        String apiKey = BuildConfig.GROQ_API_KEY;
        OkHttpClient client = new OkHttpClient();

        JSONObject imageObj = new JSONObject();
        JSONObject message = new JSONObject();
        JSONArray content = new JSONArray();

        try {
            imageObj.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", base64Image);
            imageObj.put("image_url", imageUrl);

            JSONObject textObj = new JSONObject();
            textObj.put("type", "text");
            textObj.put("text", "Describe this image for a visually impaired person.");

            content.put(textObj);
            content.put(imageObj);

            message.put("role", "user");
            message.put("content", content);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject body = new JSONObject();
            body.put("model", "llava");
            body.put("messages", messages);

            RequestBody requestBody = RequestBody.create(body.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(callback);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
