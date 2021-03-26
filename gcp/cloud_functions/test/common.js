'use strict';

const common = require('../common');

const assert = require('assert');
const chai = require("chai");
var expect = chai.expect;

describe('========================= test suite - common', () => {
    it('checkCRC invalid', () => {
        expect(common.checkCRC("0123456789")).to.equal(false);
    })

    it('checkCRC valid', () => {
        expect(common.checkCRC("012345672e")).to.equal(true);
    })

    it('checkCRC valid', () => {
        expect(common.checkCRC("7654321030")).to.equal(true);
    })
});
