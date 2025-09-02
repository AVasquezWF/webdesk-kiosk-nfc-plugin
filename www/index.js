"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const useACRReader_1 = require("./useACRReader");
const useElatecReader_1 = require("./useElatecReader");
const useFingerprintReader_1 = require("./useFingerprintReader");
const _window = window;
const init = () => {
    _window.ACRPlugin = (0, useACRReader_1.useACRReader)();
    _window.ElatecPlugin = (0, useElatecReader_1.useElatecReader)();
    _window.FingerprintPlugin = (0, useFingerprintReader_1.useFingerprintReader)();
};
init();
