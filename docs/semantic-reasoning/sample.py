"""
python3 -m venv venv
source venv/bin/activate

pip install sentence-transformers scikit-learn
pip freeze > requirements.txt
pip install -r requirements.txt
"""

from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

model = SentenceTransformer('all-MiniLM-L6-v2')

# Corpus
corpus = [
    "The only limit to our realization of tomorrow is our doubts of today.",
    "In the middle of every difficulty lies opportunity.",
    "Success is not final, failure is not fatal: It is the courage to continue that counts.",
]

# Input query
query = "Every difficulty brings opportunity."

corpus_embeddings = model.encode(corpus)
query_embedding = model.encode([query])

# Compute cosine similarities
similarities = cosine_similarity(query_embedding, corpus_embeddings)

# Retrieve similar quotes
similarity_threshold = 0.3  # Adjust this threshold based on your preference
results = [
    (corpus[idx], similarity) 
    for idx, similarity in enumerate(similarities[0]) 
    if similarity > similarity_threshold
]

# Display results sorted by similarity
results.sort(key=lambda x: x[1], reverse=True)

for quote, score in results:
    print(f"{quote} (Similarity: {score:.2f})")
