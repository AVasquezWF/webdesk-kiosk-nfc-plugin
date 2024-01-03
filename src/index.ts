import { useACRReader } from "./useACRReader"
import { useElatecReader } from "./useElatecReader";

const init = ()=> {
    (window as any).ACRPlugin = useACRReader();
    (window as any).ElatecPlugin = useElatecReader();
}

init();