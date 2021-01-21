# GCP

## 1. Install GCP cli globally - Ubuntu 20.04

```bash
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] \
    https://packages.cloud.google.com/apt cloud-sdk main" \
    | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list

sudo apt install apt-transport-https ca-certificates gnupg

curl https://packages.cloud.google.com/apt/doc/apt-key.gpg \
    | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -

sudo apt update && sudo apt install google-cloud-sdk

gcloud init

# login via Web UI & choose development env (virtual-ego-28131)

# choose us_central1-*
```

## 2. Configure Service Account

* create IAM Service Account: service_account_01
  * assign role: Storage Admin + Storage Object Admin

### 2.1. Cloud Functions

* cloud functions (in us_central1):
  * permissions: allUsers + Cloud Function Invoker

```bash
gcloud functions list
```

---

## 3. Deployment

```bash
# if changing project - dev / prod
gcloud init
```

### 3.1. Cloud Functions / Firestore

```bash
cd functions
gcloud functions deploy favourites --runtime nodejs10 --trigger-http

# with permissions: allUsers + Cloud Function Invoker

# invoke via Unit Tests / curl...
# https://us-central1-virtual-ego-281313.cloudfunctions.net/favourites
```
