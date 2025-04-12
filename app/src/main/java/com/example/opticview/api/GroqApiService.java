package com.example.opticview.api;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class GroqApiService {

    private static final String API_URL = "https://your-api-url.com/analyze"; // 🔁 Replace with your endpoint

    public static void sendImageToApi(Context context, String base64Image) {
        try {
            JSONObject body = new JSONObject();
            body.put("image", base64Image);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL,
                    body,
                    response -> Log.d("GroqApiService", "API response: " + response.toString()),
                    error -> Log.e("GroqApiService", "API error", error)
            );

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);

        } catch (JSONException e) {
            Log.e("GroqApiService", "JSON Error", e);
        }
    }
}
