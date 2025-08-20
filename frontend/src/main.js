import { createApp } from 'vue'
import App from './App.vue'

import 'vfonts/Lato.css'        // font
import 'vfonts/FiraCode.css'    // monospaced font
import './style.css'            // свой css

import {
    create,
    NButton,
    NCard,
    NLayout,
    NLayoutHeader,
    NLayoutContent,
    NLayoutFooter,
    NText,
    NImage
} from 'naive-ui'

const naive = create({
    components: [
        NButton,
        NCard,
        NLayout,
        NLayoutHeader,
        NLayoutContent,
        NLayoutFooter,
        NText,
        NImage
    ]
})

const app = createApp(App)

app.use(naive)
app.mount('#app')
