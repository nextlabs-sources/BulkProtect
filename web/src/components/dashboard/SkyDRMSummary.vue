<template>
  <div id="skydrm-summary">
    <div class="inner">
      <h1 style="flex: 0 0 auto;">Connection Status</h1>
      <span style="font-size: small; opacity: .6;" v-if="connStatus && connStatus.results && connStatus.results.filter(e => !e.result).length > 0">{{ connStatus.results.filter(e => !e.result).length }} Failing</span>
      <perfect-scrollbar :options="{suppressScrollX: true}">
        <div id="skydrm-inner" v-if="connStatus !== null">
          <h3>SkyDRM</h3>
          <div v-if="routerUrl !== 'https://example-skydrm.com/router'" class="content even" style="overflow-wrap: break-word;">
            <span class="conn-info-key">Router URL</span> {{ routerUrl }}<br>
            <span class="conn-info-key">Tenant Name</span> {{ tenantName }} <br>
            <span class="conn-info-key">Connection Status</span> <span :class="{ active: connStatus.skydrmConnection }"> {{ connStatus.skydrmConnection ? 'Active' : 'Failing' }}</span>
          </div>
          <div class="content" v-else>
            SkyDRM has not been configured. Configured it in <a>Settings</a>.
          </div>
          <div v-if="connStatus && connStatus.results && connStatus.results.length > 0">
            <h3>Repositories</h3>
            <div class="content" :key="idx" v-for="(r,idx) in connStatus.results" :class="{ even: idx%2!==0 }">
                <h4 style="font-weight: bold;">{{ r.ruleName }}</h4>
                <div class="content-inner" style="overflow-wrap: break-word;">
                {{ r.fullDirectory }}<br>
                <span class="conn-info-key">Connection Status</span> <span :class="{ active: r.result}"> {{ r.result ? 'Active' : 'Failing' }}</span>
                </div>
            </div>
          </div>
        </div>
      </perfect-scrollbar>
      <span v-if="connStatus" style="text-align: right; font-size: small; font-style: italic;">last updated {{ $moment.utc(connStatus.verificationTime).fromNow() }}</span>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import axios from 'axios'

export default {
  data: () => ({
    connStatus: null
  }),
  computed: {
    ...mapState({
      routerUrl: s => s.config.skyDRMCfg.routerUrl,
      tenantName: s => s.config.skyDRMCfg.systemBucketId
    })
  },
  methods: {
    async refresh () {
      this.$forceUpdate()
      setTimeout(() => {
        this.refresh()
      }, 60000)
    },
    async getConnectionStatus () {
      this.connStatus = (await axios.get('/conntest')).data
      setTimeout(() => {
        this.getConnectionStatus()
      }, 600000)
    }
  },
  async beforeMount() {
   
  },
  async mounted () {
    this.getConnectionStatus()
    this.refresh()
  }
}
</script>

<style>
#skydrm-summary {
  flex: 0 0 480px;
  padding: 12px;
  height: 100%;
  box-sizing: border-box;
  padding-left: 0px;
}

#skydrm-summary .inner {
  background: #ffffff;
  /* border: 1px solid rgba(0,0,0,.25); */
  height: 100%;
  padding: 12px;
  display: flex;
  flex-flow: column nowrap;
  align-content: flex-start;
  width: 480px;
  padding-bottom: 0px;
  border-radius: 5px;
  box-shadow: 0px 0px 20px rgba(0, 0, 0, 0.1);
}

#skydrm-summary .ps {
  flex: 1 1 100% !important;
}

#skydrm-inner {
  width: 100%;
  padding: 12px 6px;
  height: 100%;
}

#skydrm-inner div.content {
  font-size: small;
  margin-left: 12px;
  margin-bottom: 12px;
}

#inner div.content-inner {
  margin-left: 12px;
}

#skydrm-summary .content span.active:not(.conn-info-key) {
  color: rgba(var(--vs-success),1)!important;
  font-weight: bold;
}

#skydrm-summary .content span:not(.active):not(.conn-info-key) {
  color: rgba(var(--vs-danger),1)!important;
  font-weight: bold;
}

#skydrm-summary #skydrm-inner .content.even {
  background: rgba(0,0,0,.03);
}

#skydrm-summary span.conn-info-key {
  opacity: .5;
}
</style>