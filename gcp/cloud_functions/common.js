"use strict";

const md5 = require('md5');

function isCodeValid(req) {
    if (typeof req.body["code"] === "undefined") {
        console.warn("code - undefined");
        return false;
    }

    if (req.body["code"].length !== 10) {
        console.warn("code - length");
        return false;
    }

    if (checkCRC(req.body["code"]) == false) {
        console.warn("code - crc");
        return false;
    }

    return true;
}

function isDigestsValid(req) {
    if (typeof req.body["digests"] === "undefined") {
        console.warn("digests - undefined");
        return false;
    }

    return true;
}

function checkCRC(code) {
    var expectedCRC = md5(code.substr(0, 8)).substr(0, 2);
    var acutalCRC = code.substr(8, 10);
    
    if (acutalCRC !== expectedCRC) {
        console.warn("checkCRC - expectedCRC=" + expectedCRC + "; acutalCRC=" + acutalCRC);
        return false;
    }

    return true;
}

module.exports = { areCodesValid: isCodeValid, isDigestsValid, checkCRC };
