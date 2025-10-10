// src/main.js
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

import "vfonts/Lato.css"; // основной шрифт
import "vfonts/FiraCode.css"; // моноширинный
import "./style.css"; // твой кастомный CSS

// src/main.js (фрагмент)
import {
    create,
    NButton, NCard, NLayout, NLayoutHeader, NLayoutContent, NLayoutFooter,
    NText, NImage, NConfigProvider, NMessageProvider, NSpin, NInput, NGrid, NGridItem,
    NForm, NFormItem, NSelect, NSwitch, NTag
} from "naive-ui";

const naive = create({
    components: [
        NButton, NCard, NLayout, NLayoutHeader, NLayoutContent, NLayoutFooter,
        NText, NImage, NConfigProvider, NMessageProvider, NSpin, NInput, NGrid, NGridItem,
        NForm, NFormItem, NSelect, NSwitch, NTag
    ],
});


const app = createApp(App);
app.use(router);
app.use(naive);
app.mount("#app");
