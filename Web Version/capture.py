import base64
import re

def save_image_from_base64(b64_data, filename="static/capture.jpg"):
    b64_data = re.sub('^data:image/.+;base64,', '', b64_data)
    with open(filename, "wb") as f:
        f.write(base64.b64decode(b64_data))
    return filename
