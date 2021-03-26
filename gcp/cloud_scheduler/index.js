"use strict";

const { Storage } = require('@google-cloud/storage');
const storage = new Storage();

const express = require('express');

const app = express();

app.get('/', (req, res) => {
  storage.bucket("favourites_bucket").getFiles(function (err, files) {
    if (!err) {
      console.log('files.length=' + files.length);

      files.forEach(file => {
        console.log("file.name=" + file.name);
        file.delete();
      });

      res.sendStatus(200);
    } else {
      console.error(err)
      res.sendStatus(400);
    }
  });

});

// var port = 3000;
// app.listen(port, () => console.log(`listening at http://localhost:${port}`));
// module.exports = app

exports.favourites_cleaner = app;
