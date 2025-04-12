const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const { Groq } = require('groq-sdk');
require('dotenv').config();

const app = express();
const port = 3000;

const groq = new Groq({
  apiKey: process.env.GROQ_API_KEY,
});

const upload = multer({ dest: 'uploads/' });

app.post('/describe', upload.single('image'), async (req, res) => {
  try {
    const imagePath = req.file.path;
    const imageData = fs.readFileSync(imagePath);

    const base64Image = `data:image/jpeg;base64,${imageData.toString('base64')}`;

    const chatCompletion = await groq.chat.completions.create({
      model: 'meta-llama/llama-4-maverick-17b-128e-instruct',
      messages: [
        {
          role: 'user',
          content: [
            { type: 'text', text: 'Describe this image for a visually impaired person. Act like an onboard assistant which directly tells what the person is seeing dont start sentence with words like the image or scene depicts etc. Describe in short sentences.' },
            { type: 'image_url', image_url: { url: base64Image } },
          ],
        },
      ],
    });

    // Remove uploaded file
    fs.unlinkSync(imagePath);

    const caption = chatCompletion.choices[0].message.content;
    res.json({ caption });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Failed to generate description' });
  }
});

app.listen(port, () => {
  console.log(`Backend running at http://localhost:${port}`);
});