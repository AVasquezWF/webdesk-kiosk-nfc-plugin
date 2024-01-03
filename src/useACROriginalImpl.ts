import { useKioskReader, CordovaCallback, UseExecTemplate } from "./useReader";

export const useACROriginalImpl = (pluginName: string) => {
    const { useBasicExecutor } = useKioskReader(pluginName);

    const events = {
        tagSuccessListener: (result: any) => { },
        tagFailureListener: (result: any) => { },
    };

    const state = {
        metadata: {
            type: "unknown",
        },
        AID: "F222222228",
    };

    const start = () => {
        if (cordova.platformId !== "android") return;

        const onSuccess: CordovaCallback = (res) => {
            state.metadata = res.metadata;
            events.tagSuccessListener(res);
        };
        const onError: CordovaCallback = (res) => {
            events.tagFailureListener(res);
        };

        setTimeout(() => {
            useBasicExecutor("listen")(onSuccess, onError);
        }, 10);
    };

    const setAID = (aid: string) => {
        state.AID = aid;
    };

    const initReader: UseExecTemplate = useBasicExecutor("initReader");
    const getVersion: UseExecTemplate = useBasicExecutor("getVersion");

    const initNTAG213 = (
        oldPassword: string,
        password: string,
        success: CordovaCallback,
        failure: CordovaCallback
    ) => {
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

        useBasicExecutor("initTAG213", [oldRe.password, newRe.password])(
            success,
            failure
        );
    };

    const clearLCD: UseExecTemplate = useBasicExecutor("clearLCD");

    interface DisplayOptions {
        bold: boolean;
        font: number;
        x: number;
        y: number;
    }
    const display = (
        msg: string,
        opts: DisplayOptions,
        success: CordovaCallback,
        failure: CordovaCallback
    ) => {
        const options: DisplayOptions = opts || {};
        if (options.bold === undefined) options.bold = false;
        if (options.font === undefined) options.font = 1;
        if (options.x === undefined) options.x = 0;
        if (options.y === undefined) options.y = 0;
        useBasicExecutor("display", [
            msg,
            options.x,
            options.y,
            options.bold,
            options.font,
        ])(success, failure);
    };

    const removeTagListener: UseExecTemplate = () => {
        events.tagSuccessListener = () => { };
        events.tagFailureListener = () => { };
    };

    const addTagListener: UseExecTemplate = (success, failure) => {
        events.tagSuccessListener = success;
        events.tagFailureListener = failure;
    };

    const authenticateWithKeyA = (
        block: any,
        keyA: any,
        success: (data: any) => any,
        failure: (err: any) => any
    ) => {
        useBasicExecutor("authenticateWithKeyA", [block, keyA])(
            success,
            failure
        );
    };

    const selectFile = (
        aid: string,
        success: { (data: any): any; (data: any): any },
        failure: { (data: any): any; (err: any): any }
    ) => {
        useBasicExecutor("selectFile", [aid])(success, failure);
    };

    const authenticateWithKeyB = (
        block: any,
        keyB: any,
        success: (data: any) => any,
        failure: (err: any) => any
    ) => {
        useBasicExecutor("authenticateWithKeyB", [block, keyB])(
            success,
            failure
        );
    };

    const writeAuthenticate = (
        block: any,
        keyA: any,
        keyB: any,
        success: (data: any) => any,
        failure: (err: any) => any
    ) => {
        useBasicExecutor("writeAuthenticate", [block, keyA, keyB])(
            success,
            failure
        );
    };

    const readUID: UseExecTemplate = useBasicExecutor("readUID");

    const getFirmwareVersion: UseExecTemplate =
        useBasicExecutor("getFirmwareVersion");

    const getReceivedData: UseExecTemplate =
        useBasicExecutor("getReceivedData");

    const getLedStatus: UseExecTemplate = useBasicExecutor("getLedStatus");

    const getBatteryLevel: UseExecTemplate =
        useBasicExecutor("getBatteryLevel");

    const readData = (
        block: any,
        password: string,
        success: CordovaCallback,
        failure: CordovaCallback
    ) => {
        if (state.metadata.type === "javacard") {
            selectFile(state.AID, success, failure);
        } else {
            var re = _normalizePassword(password);
            if (!re.success) {
                failure(re);
                return;
            }
            useBasicExecutor("readData", [block, re.password])(
                success,
                failure
            );
        }
    };

    const readMobileData: UseExecTemplate = (success, failure) => {
        selectFile(state.AID, success, failure);
    };

    const isReady: UseExecTemplate = useBasicExecutor("isReady");

    // connect reader by address
    const connectReader = (
        address: any,
        success: CordovaCallback,
        failure: CordovaCallback
    ) => {
        useBasicExecutor("connectReader", [address])(success, failure);
    };

    const disconnectReader = useBasicExecutor("disconnectReader");

    const writeData = (
        block: any,
        data: string | null | undefined,
        password: string,
        success: CordovaCallback,
        failure: CordovaCallback
    ) => {
        if (state.metadata.type === "javacard") {
            failure({ success: false, exception: "javacard" });
        } else {
            if (data === undefined || data === null) data = "";

            var re = _normalizePassword(password);
            if (!re.success) {
                failure(re);
                return;
            }

            useBasicExecutor("writeData", [block, data, re.password])(
                success,
                failure
            );
        }
    };

    const onCardAbsent = () => { };
    const onReady = () => { };
    const onAttach = (device: any) => { };
    const onDetach = (device: any) => { };
    const onBatteryLevelChange = (level: any) => { };
    const onScan = (device: any) => { };

    const startScan: UseExecTemplate = useBasicExecutor("startScan");
    const stopScan: UseExecTemplate = useBasicExecutor("stopScan");

    const runCardAbsent = () => {
        const metadata = {};
        onCardAbsent();
    };

    const _normalizePassword = (password: string) => {
        if (typeof password !== "string") password = "";

        if (password === "" || /^[0-9a-fA-F]{8}$/.test(password)) {
            return { success: true, password: password };
        } else {
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
