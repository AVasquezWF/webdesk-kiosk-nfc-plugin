import { exec } from "cordova";

export enum Methods {
    setListenerInterval = "setListenerInterval",
    sendReaderCommand = "sendReaderCommand",
    checkIsReady = "checkIsReady",
    addListener = "addListener",
    reconnectReader = "reconnectReader",
    readCard = "readCard",
    init = "init",
}

export type CordovaCallback = (data: any) => any;
export type UseExecTemplate = (success: CordovaCallback, error: CordovaCallback) => void;
export interface KioskReaderPlugin {
    name: string;
    sendReaderCommand: (command: string) => Promise<void>;
    setListenerInterval: (interval: number) => Promise<void>;
    checkIsReady: UseExecTemplate;
    addListener: UseExecTemplate;
    readCard: UseExecTemplate;
    init: UseExecTemplate;
}

export const useKioskReader = (pluginName: string) => {
    const asPromise = <T>(method: Methods, ...args: any) =>
        new Promise<T>((res, rej) => {
            exec(res, rej, pluginName, method, [...args]);
        });

    const useBasicExecutor =
        <T = Methods>(method: T, args: any[] = []): UseExecTemplate =>
            (success, error) => {
                console.log({ pluginName, method });
                exec(success, error, pluginName, method as string, [...args]);
            }

    return { asPromise, useBasicExecutor };
};
