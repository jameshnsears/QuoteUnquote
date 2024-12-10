import torch
from transformers import AutoModel, AutoTokenizer

# Load pre-trained language model and tokenizer
model_name = "all-MiniLM-L6-v2"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModel.from_pretrained(model_name)

# Sample corpus of quotations
corpus = [
    "The only limit to our realization of tomorrow is our doubts of today.",
    "In the middle of every difficulty lies opportunity.",
    "Success is not final, failure is not fatal: It is the courage to continue that counts.",
]

# Query string
query = "Every difficulty brings opportunity."

# Tokenize the query and corpus
inputs = tokenizer(query, return_tensors="pt")
corpus_inputs = tokenizer(corpus, return_tensors="pt", padding=True, truncation=True)

# Generate embeddings
with torch.no_grad():
    query_embeddings = model(**inputs).last_hidden_state.mean(dim=1)
    corpus_embeddings = model(**corpus_inputs).last_hidden_state.mean(dim=1)

# Calculate cosine similarity
def cosine_similarity(a, b):
    return torch.dot(a, b) / (torch.norm(a) * torch.norm(b))

similarity_scores = []
for corpus_embedding in corpus_embeddings:
    similarity_scores.append(cosine_similarity(query_embeddings, corpus_embedding).item())

# Rank and retrieve the top-k similar quotations
top_k = 3
top_indices = torch.argsort(torch.tensor(similarity_scores), descending=True)[:top_k]
top_quotations = [corpus[i] for i in top_indices]

print(top_quotations)
