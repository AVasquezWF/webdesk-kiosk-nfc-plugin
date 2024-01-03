"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useACROriginalImpl = void 0;
const useReader_1 = require("./useReader");
const useACROriginalImpl = (pluginName) => {
    const { useBasicExecutor } = (0, useReader_1.useKioskReader)(pluginName);
    const events = {
        tagSuccessListener: (result) => { },
        tagFailureListener: (result) => { },
    };
    const state = {
        metadata: {
            type: "unknown",
        },
        AID: "F222222228",
    };
    const start = () => {
        if (cordova.platformId !== "android")
            return;
        const onSuccess = (res) => {
            state.metadata = res.metadata;
            events.tagSuccessListener(res);
        };
        const onError = (res) => {
            events.tagFailureListener(res);
        };
        setTimeout(() => {
            useBasicExecutor("listen")(onSuccess, onError);
        }, 10);
    };
    const setAID = (aid) => {
        state.AID = aid;
    };
    const initReader = useBasicExecutor("initReader");
    const getVersion = useBasicExecutor("getVersion");
    const initNTAG213 = (oldPassword, password, success, failure) => {
        const oldRe = _normalizePassword(oldPassword);
        if (!oldRe.success) {
            failure(oldRe);
            return;
        }
        const newRe = _normalizePassword(password);
        if (!newRe.success) {
            failure(newRe);
            return;
        }
        useBasicExecutor("initTAG213", [oldRe.password, newRe.password])(success, failure);
    };
    const clearLCD = useBasicExecutor("clearLCD");
    const display = (msg, opts, success, failure) => {
        const options = opts || {};
        if (options.bold === undefined)
            options.bold = false;
        if (options.font === undefined)
            options.font = 1;
        if (options.x === undefined)
            options.x = 0;
        if (options.y === undefined)
            options.y = 0;
        useBasicExecutor("display", [
            msg,
            options.x,
            options.y,
            options.bold,
            options.font,
        ])(success, failure);
    };
    const removeTagListener = () => {
        events.tagSuccessListener = () => { };
        events.tagFailureListener = () => { };
    };
    const addTagListener = (success, failure) => {
        events.tagSuccessListener = success;
        events.tagFailureListener = failure;
    };
    const authenticateWithKeyA = (block, keyA, success, failure) => {
        useBasicExecutor("authenticateWithKeyA", [block, keyA])(success, failure);
    };
    const selectFile = (aid, success, failure) => {
        useBasicExecutor("selectFile", [aid])(success, failure);
    };
    const authenticateWithKeyB = (block, keyB, success, failure) => {
        useBasicExecutor("authenticateWithKeyB", [block, keyB])(success, failure);
    };
    const writeAuthenticate = (block, keyA, keyB, success, failure) => {
        useBasicExecutor("writeAuthenticate", [block, keyA, keyB])(success, failure);
    };
    const readUID = useBasicExecutor("readUID");
    const getFirmwareVersion = useBasicExecutor("getFirmwareVersion");
    const getReceivedData = useBasicExecutor("getReceivedData");
    const getLedStatus = useBasicExecutor("getLedStatus");
    const getBatteryLevel = useBasicExecutor("getBatteryLevel");
    const readData = (block, password, success, failure) => {
        if (state.metadata.type === "javacard") {
            selectFile(state.AID, success, failure);
        }
        else {
            var re = _normalizePassword(password);
            if (!re.success) {
                failure(re);
                return;
            }
            useBasicExecutor("readData", [block, re.password])(success, failure);
        }
    };
    const readMobileData = (success, failure) => {
        selectFile(state.AID, success, failure);
    };
    const isReady = useBasicExecutor("isReady");
    // connect reader by address
    const connectReader = (address, success, failure) => {
        useBasicExecutor("connectReader", [address])(success, failure);
    };
    const disconnectReader = useBasicExecutor("disconnectReader");
    const writeData = (block, data, password, success, failure) => {
        if (state.metadata.type === "javacard") {
            failure({ success: false, exception: "javacard" });
        }
        else {
            if (data === undefined || data === null)
                data = "";
            var re = _normalizePassword(password);
            if (!re.success) {
                failure(re);
                return;
            }
            useBasicExecutor("writeData", [block, data, re.password])(success, failure);
        }
    };
    const onCardAbsent = () => { };
    const onReady = () => { };
    const onAttach = (device) => { };
    const onDetach = (device) => { };
    const onBatteryLevelChange = (level) => { };
    const onScan = (device) => { };
    const startScan = useBasicExecutor("startScan");
    const stopScan = useBasicExecutor("stopScan");
    const runCardAbsent = () => {
        const metadata = {};
        onCardAbsent();
    };
    const _normalizePassword = (password) => {
        if (typeof password !== "string")
            password = "";
        if (password === "" || /^[0-9a-fA-F]{8}$/.test(password)) {
            return { success: true, password: password };
        }
        else {
            return { success: false, exception: "Invalid password" };
        }
    };
    return {
        events,
        state,
        start,
        addTagListener,
        isReady,
        readUID,
        //Internally used
        onAttach,
        onDetach,
        onReady,
        onScan,
        runCardAbsent,
    };
};
exports.useACROriginalImpl = useACROriginalImpl;
