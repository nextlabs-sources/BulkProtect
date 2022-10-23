<template>
  <div id="statistics">
    <div class="inner" style="display: flex; flex-flow: column nowrap;">
      <h1 style="padding-top: 12px; padding-left: 12px;">Metrics</h1>
      <vs-tabs v-if="execHistory!==null" :position="'bottom'" :alignment="'right'" style="flex: 1 1 auto; display: flex; flex-flow: column nowrap;" v-model="tabIdx">
        <vs-tab label="Last 24 Hours">
          <perfect-scrollbar>
            <div id="metrics-container" style="flex: 1 1 auto; display: flex; flex-flow: column nowrap;" v-if="execHistory.length > 0">
              <div style="flex: 1 1 auto; display: flex; flex-flow: row-nowrap; justify-content: space-evenly; align-items: center;" id="infographics">
                <div style="display: flex; flex-flow: column nowrap; align-items: center;" v-if="aggregateFileTypeComposition !== null && Object.keys(aggregateFileTypeComposition).length !== 0">
                  <span style="font-weight: bold; font-size: medium; padding: 16px 0px;">Aggregated File Type Count</span>
                  <FileExtPie :extMap="aggregateFileTypeComposition" :showLegend="false" style="width: 12em;"></FileExtPie>
                </div>
                <SpeedEstimates :execHistory="execHistory"></SpeedEstimates>
              </div>
              <div style="flex: 1 1 auto; display: flex; flex-flow: row-nowrap; justify-content: space-evenly;">
                <div class="numeric-metric">
                  <span style="font-size: 3vw;">{{ totalFilesProcessed | numeral('0a') }}</span>
                  <span>Files Encrypted</span>
                </div>
                <div class="numeric-metric">
                  <span style="font-size: 3vw; white-space: nowrap;">{{ totalVolumeProcessed | prettyBytes }}</span>
                  <span>of Data Protected</span>
                </div>
              </div>
            </div>
            <div v-else style="width: 100%; flex: 1 1 100%; display: flex; align-items: center; justify-content: center; flex-flow: column nowrap; opacity: .5; height: 100%; font-family: 'Open Sans', sans-serif !important;">
              <vs-icon icon="not_interested" style="font-size: 5rem;"></vs-icon>
              <span>No rules executed in last 24 hours.</span>
            </div>
          </perfect-scrollbar>
        </vs-tab>
        <vs-tab label="All-Time">
          <perfect-scrollbar>
            <div id="metrics-container" style="flex: 1 1 auto; display: flex; flex-flow: column nowrap;">
              <div style="flex: 1 1 auto; display: flex; flex-flow: row-nowrap; justify-content: space-evenly; align-items: center;" id="infographics">
                <div style="display: flex; flex-flow: column nowrap; align-items: center;" v-if="aggregateFileTypeComposition !== null && Object.keys(aggregateFileTypeComposition).length !== 0">
                  <span style="font-weight: bold; font-size: medium; padding: 16px 0px;">Aggregated File Type Count</span>
                  <FileExtPie :extMap="aggregateFileTypeComposition" :showLegend="false" style="width: 12em;"></FileExtPie>
                </div>
                <SpeedEstimates :execHistory="execHistory"></SpeedEstimates>
              </div>
              <div style="flex: 1 1 auto; display: flex; flex-flow: row-nowrap; justify-content: space-evenly;">
                <div class="numeric-metric">
                  <span style="font-size: 3vw;">{{ totalFilesProcessed | numeral('0a') }}</span>
                  <span>Files Encrypted</span>
                </div>
                <div class="numeric-metric">
                  <span style="font-size: 3vw; white-space: nowrap;">{{ totalVolumeProcessed | prettyBytes }}</span>
                  <span>of Data Protected</span>
                </div>
              </div>
            </div>
          </perfect-scrollbar>
        </vs-tab>
      </vs-tabs>
      
      <div id="metrics-container" v-else style="flex: 1 1 100%; display: flex; justify-content: center; align-items:center;">
        <ClipLoader :size="80" color="rgba(0,0,0, .3)"></ClipLoader>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import prettyBytes from 'pretty-bytes'
import { ClipLoader } from '@saeris/vue-spinners'
import FileExtPie from '../ui/FileExtPie.vue'
import SpeedEstimates from '../ui/SpeedEstimates.vue'

export default {
  components: {
    ClipLoader,
    FileExtPie,
    SpeedEstimates
  },
  data: () => ({
    tabIdx: 0,
  }),
  computed: {
    ...mapState({
      execHistoryFull: s => s.history.ruleExec
    }),
    execHistory () {
      return this.execHistoryFull === null ? null : this.execHistoryFull.filter(e => this.tabIdx === 0 ? e.completionTime > Date.now() - 86400000 : true)
    },
    totalFilesProcessed () {
      return this.execHistory.reduce((a,e) => a + (e.rule.action==='Rights Protect' ? e.processed : 0), 0)
    },
    totalVolumeProcessed () {
      return this.execHistory.reduce((a,e) => a + (e.rule.action==='Rights Protect' ? e.volumeProcessed : 0), 0)
    },
    lastRepositoryExecs () {
      if (this.execHistory === null) return []
      var res = {}
      for (let execIdx = this.execHistory.length - 1; execIdx >=0 ; execIdx--) {
        var exec = this.execHistory[execIdx]
        res[exec.rule.fullDirectory] = exec
      }
      return Object.values(res)
    },
    aggregateFileTypeComposition () {
      return this.lastRepositoryExecs.reduce((a,e) => {
        if (e.extMap === null || e.extMap === undefined || Object.keys(e.extMap).length === 0) return a;
        for (var key in e.extMap) {
          if (key in a) {
            a[key] = a[key] + e.extMap[key]
          } else {
            a[key] = e.extMap[key]
          }
        }
        return a;
      }, {})
    }
  },
  filters: {
    prettyBytes: prettyBytes
  }
}
</script>

<style>
#statistics {
  flex: 1 1 auto;
  padding: 12px;
  overflow: hidden;
  height: 100%;
  box-sizing: border-box;
}

#statistics .inner {
  background: #ffffff;
  /* border: 1px solid rgba(0,0,0,.25); */
  border-radius: 5px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: stretch;
  box-shadow: 0px 0px 20px rgba(0, 0, 0, 0.1);
}

#statistics .numeric-metric {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 12px;
  /* flex: 0 0 50%;
  width: 50%; */
}

@media screen and (max-width: 1280px) {
  #statistics #infographics {
    display: none !important;
  }
}

#statistics .con-slot-tabs {
  flex: 1 1 auto;
}

#statistics .ul-tabs {
  box-shadow: none !important;
}

#statistics .vs-tabs--content {
  height: 100%;
}

#statistics .ps {
  height: 100%;
}
</style>