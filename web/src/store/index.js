import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'
import VueAxios from 'vue-axios'

Vue.use(Vuex)
Vue.use(VueAxios, axios)

axios.defaults.baseURL = '/rpt';

export default new Vuex.Store({
  state: {
    transient: {
      fileCount: 0,
      fileVolume: 0,
      loggedInUser: "Administrator",
      ruleEditor: {
        rule: null,
        selectedRule: null,
        editMode: false
      },
      connStatus: null,
    },
    config: {
      repositories: [],
      repositoryMap: {},
      rules: [],
      ruleMap: {},
      skyDRMCfg: {
        routerUrl: "https://skydrm.com/router",
        appId: 2,
        appKey: "7888A043D256FF40736646CD21A48442",
        systemBucketId: ""
      },
      historyRetentionTime: 0,
    },
    notify: {
      event: "",
      eventType: "",
      showEvent: false,
      eventTimeout: null
    },
    history: {
      ruleExec: null
    },
    enums: {
      authTypes: ['HTTP_NTLM', 'CIFS', 'AZURE'],
      repoTypes: ['SHARED_FOLDER', 'SHAREPOINT', 'AZURE_FILE_STORAGE'],
      skydrmClassifications: []
    }
  },
  getters: {},
  mutations: {
    updateConfiguration(state, configuration) {
      state.config = configuration
      // Rebuild the id-entity maps
      state.config.repositoryMap = state.config.repositories.reduce((o, e) => { o[e.id] = e; return o }, {})
      state.config.rulesMap = state.config.rules.reduce((o, e) => { o[e.id] = e; return o }, {})
    }
  },
  actions: {
    async getConfiguration (ctx) {
      var resp = await axios.get('/config')
      ctx.commit('updateConfiguration', resp.data)
    },
    async getExecHistory (ctx) {
      var resp = await axios.get('/history/all')
      ctx.state.history.ruleExec = resp.data
    },
    async getClassificationProfile (ctx) {
      var resp = await axios.get('/clskeys')
      ctx.state.enums.skydrmClassifications = resp.data.tags
    },
    async updateRepository (ctx, { index, repo }) {
      var config = JSON.parse(JSON.stringify(ctx.state.config))
      if (index >= config.repositories) {
        config.repositories.push(repo)
      } else {
        config.repositories[index] = repo
      }
      await ctx.dispatch('updateConfiguration', config)
    },
    async updateRule (ctx, { index, rule }) {
      var config = JSON.parse(JSON.stringify(ctx.state.config))
      if (index >= config.rules) {
        config.rules.push(rule)
      } else {
        config.rules[index] = rule
      }
      await ctx.dispatch('updateConfiguration', config)
    },
    async deleteRepository (ctx, index) {
      var config = JSON.parse(JSON.stringify(ctx.state.config))
      config.repositories.splice(index, 1)
      await ctx.dispatch('updateConfiguration', config)
    },
    async deleteRule (ctx, index) {
      var config = JSON.parse(JSON.stringify(ctx.state.config))
      config.rules.splice(index, 1)
      await ctx.dispatch('updateConfiguration', config)
    },
    async updateSkyDRMConfiguration (ctx, cfg) {
      var config = JSON.parse(JSON.stringify(ctx.state.config))
      config.skyDRMCfg = cfg;
      await ctx.dispatch('updateConfiguration', config)
    },
    async updateConfiguration (ctx, config) {
      await axios.post('/config', config)
      await ctx.dispatch('getConfiguration')
    },
    async executeRule (ctx, rule) {
      await axios.post('/execute', rule)
    },
    async handleWebSocketDataMessage (ctx, { type, data }) {
      switch (type) {
        case 'RuleExecutionResult':
          ctx.state.history.ruleExec.unshift(data)
          break
        case 'ConnectionVerificationResult':
          ctx.state.transient.connStatus = data
          break
        case 'Configuration':
          // Ignore for now
          break
        default:
          break
          // console.log('Unhandled websocket data message of type ' + type + ': ' + data)
      }
    }
  }
})