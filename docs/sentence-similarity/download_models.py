from transformers import AutoModel, AutoTokenizer, DistilBertModel, DistilBertTokenizer

# Load the model and tokenizer
model = AutoModel.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")
tokenizer = AutoTokenizer.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")

# Save the model locally
model.save_pretrained("./all-MiniLM-L6-v2")
tokenizer.save_pretrained("./all-MiniLM-L6-v2")

##################

# Load the model and tokenizer
model = DistilBertModel.from_pretrained("distilbert-base-uncased")
tokenizer = DistilBertTokenizer.from_pretrained("distilbert-base-uncased")

# Save the model locally
model.save_pretrained("./DistilBERT")
tokenizer.save_pretrained("./DistilBERT")
