import Vue from 'vue'
import App from './App.vue'
import Vuesax from 'vuesax'
import axios from 'axios'
import VueAxios from 'vue-axios'
import PerfectScrollbar from 'vue2-perfect-scrollbar'
import store from './store'
import 'vue2-perfect-scrollbar/dist/vue2-perfect-scrollbar.css'
import SweetAlertIcons from 'vue-sweetalert-icons'
import { mapState } from 'vuex'
import ReconnectingWebSocket from 'reconnecting-websocket'
import moment from 'vue-moment'
import duration from 'vue-moment'
import 'vue2-timepicker/dist/VueTimepicker.css'
import vueNumeralFilterInstaller from 'vue-numeral-filter';

Vue.use(Vuesax, {
  theme:{
    colors:{
      primary:'#39974A',
      success:'#70B55B',
      danger:'#F25C68',
      warning:'#FFCC66',
      dark:'rgb(36, 33, 69)'
    }
  }
})
Vue.use(PerfectScrollbar)
Vue.use(VueAxios, axios)
Vue.use(SweetAlertIcons)
Vue.use(moment, duration)
Vue.use(vueNumeralFilterInstaller)

import '../fonts/fonts.css'
import 'vuesax/dist/vuesax.css'
import 'material-icons/iconfont/material-icons.css'

Vue.config.productionTip = false

const wsEndpoint = 'wss://' + window.location.hostname + '/rptsock'
const socket = new ReconnectingWebSocket(wsEndpoint)

new Vue({
  render: h => h(App),
  store,
  created() {
    store.dispatch('getConfiguration')
    store.dispatch('getExecHistory')
    store.dispatch('getClassificationProfile')


  },
  mounted () {
    var store = this.$store;
    socket.addEventListener('message', ev => {
      var data = JSON.parse(ev.data);
      if (data.msgType !== 'data') {
        store.state.notify.showEvent = true;
        store.state.notify.event = data.msg;
        store.state.notify.eventType = data.msgType;
        if (store.state.notify.eventTimeout != null) {
          clearTimeout(store.state.notify.eventTimeout);
        }
        store.state.notify.eventTimeout = setTimeout(() => {
          store.state.notify.showEvent = false
        }, 15000);
      } else {
        store.dispatch('handleWebSocketDataMessage', data)
      }
    })
  },
  computed: mapState ({
    notifs: s => s.notify,
  })
}).$mount('#app')