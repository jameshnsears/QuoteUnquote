"use strict";

const express = require('express');

const common = require('./common');
const save = require('./save');
const receive = require('./receive');

const app = express();
app.use(express.json());

app.post('/save', (req, res) => {
    console.info("/save=" + JSON.stringify(req.body));

    if (common.areCodesValid(req) === false || common.isDigestsValid(req) === false) {
        res.status(400).json({ 'error': 'JSON not valid' });
    } else {
        /*
        // saving to a bucket is quick
        save.saveToBucket(
            "favourites_bucket",
            req.body["code"],
            req.body["digests"]);
        */

        save.saveToFirebase(
            "favourites_collection",
            req.body["code"],
            req.body["digests"]);

        res.sendStatus(200);
    }
});

app.post('/receive', (req, res) => {
    console.info("/receive=" + JSON.stringify(req.body));

    if (common.areCodesValid(req) === false) {
        res.status(400).json({ 'error': 'JSON not valid' });
    } else {
        /*
        // but a bucket can take upto 30s to reveal what's been saved in it
        receive.receiveFromBucket(
            "favourites_bucket",
            req.body["code"], 
            function(digests) {
            res.status(200).json(digests);
        });
        */

        receive.receiveFromFirebase(
            "favourites_collection",
            req.body["code"],
            function (digests) {
                res.status(200).json(digests);
            });
    }
});

// var port = 3000;
// app.listen(port, () => console.log(`listening at http://localhost:${port}`));
// module.exports = app

exports.favourites = app;
