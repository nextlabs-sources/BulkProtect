<template>
  <div id="settings">
    <div id="settings-inner">
      <div class="settings-group">
        <h1>SkyDRM Settings</h1>
        <div v-if="config.skyDRMCfg">
            <vs-input :success="false" success-text="Connection Successful." val-icon-success="done" label="Router URL" placeholder="eg. https://example.skydrm.com:8443/router" v-model="config.skyDRMCfg.routerUrl" class="field"/>
            <vs-input label="App ID" placeholder="" type="password" v-model="config.skyDRMCfg.appId" class="field"/>
            <vs-input label="App Key" placeholder="" type="password" v-model="config.skyDRMCfg.appKey" class="field"/>
            <vs-input label="Tenant Name" placeholder="" v-model="config.skyDRMCfg.systemBucketId" class="field"/>
        </div>
      </div>
      <div class="settings-group" v-if="config.ruleParams">
        <h1>General Settings</h1>
        <vs-input label="Execution History Retention Time (Days)" placeholder="30" type="number" v-model="config.historyRetentionTime"/>
        <div class="field" style="flex: 1 0 100%; display: flex; flex-direction: row; flex-wrap: wrap; padding-left: 0px; padding-right: 0px; margin: 12px;">
          <label for="" class="vs-input--label" style="flex: 1 0 100%;">Document Properties to Transfer</label><br>
          <div style="display: flex; flex-wrap: row nowrap; width: 100%;">
            <vs-chips color="rgb(145, 32, 159)" placeholder="Multivalue field. Press enter after each entry. Leave empty to transfer all metadata." v-model="config.ruleParams.clsKeys" style="flex: 1 1 auto;">
              <vs-chip :key="idx" @click="remove(config.ruleParams.clsKeys, idx)" v-for="(k, idx) in config.ruleParams.clsKeys" closable>
                {{ k }}
              </vs-chip>
            </vs-chips>
          </div>
        </div>
        <div class="field" style="display: flex; flex-flow: row nowrap; margin: 12px; width: 100%;">
          <div style="flex: 0 0 auto; display: flex; flex-direction: row; flex-wrap: wrap; margin: 0px;">
            <label for="" class="vs-input--label" style="flex: 1 0 100%;">Split By Delimiter</label><br>
            <vs-switch style="margin-left: 4px; margin-right: 4px; margin-top: 12px; flex: 0 0 auto;" v-model="config.ruleParams.useDelimiter">
              <span slot="on"></span>
              <span slot="off"></span>
            </vs-switch>
          </div>
          <div style="flex: 1 1 auto; display: flex; flex-direction: row; flex-wrap: wrap; padding-left: 0px; padding-right: 0px; ">
            <label for="" class="vs-input--label" style="flex: 1 0 100%;">Document Property Value Delimiters</label><br>
            <div style="display: flex; flex-wrap: row nowrap; width: 100%;">
              <vs-chips color="rgb(145, 32, 159)" placeholder="Multivalue field. Press enter after each entry. First delimiter contained in classification value will be used." v-model="config.ruleParams.delimiters" style="flex: 1 1 auto;">
                <vs-chip :key="idx" @click="remove(config.ruleParams.delimiters, idx)" v-for="(k, idx) in config.ruleParams.delimiters" closable>
                  {{ k }}
                </vs-chip>
              </vs-chips>
            </div>
          </div>
        </div>
        <div class="field" style="display: flex; flex-flow: row nowrap; margin: 12px; width: 100%;">
          <div style="flex: 0 0 auto; display: flex; flex-direction: row; flex-wrap: wrap; margin: 0px;">
            <label for="" class="vs-input--label" style="flex: 1 0 100%;">Hide Nxl Extension</label><br>
            <vs-switch style="margin-left: 4px; margin-right: 4px; margin-top: 12px; flex: 0 0 auto;" v-model="config.ruleParams.hideNxlExt">
              <span slot="on"></span>
              <span slot="off"></span>
            </vs-switch>
          </div>
        </div>
      </div>
      <div style="display: flex; flex-direction: row; align-items: center; justify-content: flex-end; padding-top: 22px; flex: 1 0 auto; width: 100%;">
        <vs-button color="primary" type="border" style="margin: 0px 4px;" :class="{isActive: false}" @click="save()">
          Save
        </vs-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'

export default {
  data: () => ({
    config: {}
  }),
  computed: {
    ...mapState({
      skydrmConfig: s => s.config.skyDRMCfg,
      storeConfig: s => s.config
    })
  },
  methods: {
    ...mapActions(['updateConfiguration']),
    async save () {
      await this.updateConfiguration(this.config);
    },
    remove (arr, idx) {
      arr.splice(idx, 1)
    }
  },
  watch: {
    storeConfig(j) {
      this.config = JSON.parse(JSON.stringify(j))
    }
  }
}
</script>

<style>
#settings {
  padding: 12px;
  width: 100%;
}

#settings-inner {
  margin-left: auto;
  margin-right: auto;
  max-width: 800px;
  text-align: left;
  display: flex;
  flex-flow: column nowrap;
}

#settings-inner .settings-group {
  width: 100%;
  display: flex;
  flex-flow: row wrap;
}

#settings-inner .vs-input--label,
#settings-inner .vs-select--label {
  font-size: 1rem !important;
  line-height: 32px;
}

#settings-inner .vs-input {
  flex: 1 0 100%;
  margin: 12px;
}

#settings-inner h1 {
  flex: 1 0 100%;
  /* margin-bottom: 8px; */
}

#settings .con-chips {
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 5px;
  padding: 0px !important;
}
</style>