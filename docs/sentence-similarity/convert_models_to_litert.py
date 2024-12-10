from transformers import TFAutoModel

# Convert all-MiniLM-L6-v2
model = TFAutoModel.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")

# Save the model as a TensorFlow .saved_model format
model.save_pretrained("./all-MiniLM-L6-v2-tf", saved_model=True)

# Convert to TensorFlow Lite format
import tensorflow as tf
model = tf.saved_model.load('./all-MiniLM-L6-v2-tf')
converter = tf.lite.TFLiteConverter.from_saved_model('./all-MiniLM-L6-v2-tf')
tflite_model = converter.convert()

# Save the TFLite model
with open('all_MiniLM_L6_v2.tflite', 'wb') as f:
    f.write(tflite_model)
