from flask import Flask, render_template, request, jsonify
from capture import save_image_from_base64
from describe import get_description_and_speak

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/analyze', methods=['POST'])
def analyze():
    data = request.get_json()
    b64_image = data.get('image')
    image_path = save_image_from_base64(b64_image)
    result = get_description_and_speak(image_path)
    return jsonify({'description': result})

if __name__ == "__main__":
    app.run(debug=True)
