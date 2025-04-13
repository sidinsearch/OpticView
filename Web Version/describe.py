import base64
from groq import Groq
import os

API_KEY = "YOUR KEY FROM GROQ"
MODEL_NAME = "meta-llama/llama-4-scout-17b-16e-instruct"
client = Groq(api_key=API_KEY)

# Check if we're on Render or a local environment
is_render = os.getenv("RENDER", "false") == "true"

def image_to_base64(path):
    with open(path, "rb") as f:
        return base64.b64encode(f.read()).decode('utf-8')

def get_description_and_speak(image_path):
    b64_img = image_to_base64(image_path)

    # Sending the image and prompt to the model
    response = client.chat.completions.create(
        model=MODEL_NAME,
        messages=[{
            "role": "user",
            "content": [
                {
                    "type": "text",
                    "text": (
                        "You are a smart assistant helping a visually impaired person understand their surroundings. "
                        "Describe everything clearly and simply. Mention nearby people, objects, signs, and movement. "
                        "Alert about any potential danger like vehicles, stairs, edges, fire, or approaching individuals. "
                        "Use short, clear, and easy-to-understand language suitable for spoken audio. Avoid mentioning the word 'image'."
                    )
                },
                {
                    "type": "image_url",
                    "image_url": {"url": f"data:image/jpeg;base64,{b64_img}"}
                }
            ]
        }],
        stream=True
    )

    full_response = ""
    for chunk in response:
        content = chunk.choices[0].delta.content
        if content:
            full_response += content

    return full_response
