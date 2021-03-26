'use strict';

const app = require('../index');

const assert = require('assert');
const chaiHttp = require("chai-http");

const chai = require("chai");
var expect = chai.expect;

chai.use(chaiHttp);

describe('========================= test suite - /', () => {
    it("invoke", done => {
        chai
            .request(app)
            .get('/')
            .end((err, res) => {
                expect(res).to.have.status(200);
                done();
            });
    });
});
