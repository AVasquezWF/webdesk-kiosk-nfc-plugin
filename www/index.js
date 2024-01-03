"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const useACRReader_1 = require("./useACRReader");
const useElatecReader_1 = require("./useElatecReader");
const init = () => {
    window.ACRPlugin = (0, useACRReader_1.useACRReader)();
    window.ElatecPlugin = (0, useElatecReader_1.useElatecReader)();
};
init();
