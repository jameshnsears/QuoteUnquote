# NODE

## 1. Install node locally - Ubuntu 20.04

```bash
export NODE_VERSION=node-v10.22.1-linux-x64
export PATH=$HOME/bin/$NODE_VERSION/bin:$PATH

wget -P $HOME/Downloads https://nodejs.org/dist/latest-v10.x/$NODE_VERSION.tar.xz

tar -C $HOME/bin/ -xf $HOME/Downloads/$NODE_VERSION.tar.xz

# v10.22.1
node -v

# install project dependencies
npm install -g express mocha chai chai-http nyc md5 \
    @google-cloud/firestore markdownlint-cli jsonlint

# update packages
cd cloudLib/gcp
./npm-update.sh
```

---

## 2. GCP credentials

### 2.1. development

* <https://console.cloud.google.com/iam-admin/serviceaccounts?project=virtual-ego-281313>

### 2.2. production

* <https://console.cloud.google.com/iam-admin/serviceaccounts?project=qualified-glow-281314>

---

## 3. Static Analysis

* markdownlint '**/*.md' --ignore functions/node_modules

---

## 4. CLI: Unit Testing / Coverage - development

NOTE: requires express to be uncommented in index.js

* export GOOGLE_APPLICATION_CREDENTIALS=config/virtual-ego-281313.json
* cd functions
* nyc mocha --exit

### 4.1. Skip a suite / test

* prefix describe | it with x to skip - i.e. xit

---

## 5. Useful links

* <https://www.w3schools.com/js>

* <https://nodejs.org/en/knowledge/getting-started/control-flow/what-are-callbacks/>

* <https://www.npmjs.com/package/@google-cloud>

* <https://googleapis.dev/nodejs/storage/latest>

* <https://github.com/googleapis/nodejs-firestore>
