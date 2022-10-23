<template>
  <div style="display: flex; flex-flow: column nowrap; align-items: center;" v-if="speedEstimates !== null && Object.keys(speedEstimates).length !== 0">
    <span style="font-weight: bold; font-size: medium; padding: 8px 0px;">Repository Speed Estimates</span>
    <div style="display: flex; flex-flow: row nowrap;">
      <div style="display: flex; flex-flow: column nowrap; align-items: right; padding-right: 8px; font-family: 'Open Sans', sans-serif;">
        <span style="text-align: right; padding-bottom: 4px;" v-for="(v, k) in speedEstimates" :key="k">{{k}} </span>
      </div>
      <div style="display: flex; flex-flow: column nowrap;">
        <span style=" position: relative; width: 180px; opacity: .6; padding-bottom: 4px; font-family: 'Open Sans', sans-serif; display: flex; flex-flow: row nowrap;" v-for="(v, k) in speedEstimates" :key="k">
          
          <div class="repo-speed-bar" style="height: 20px;" :style="{flex: '0 0 ' + speedEstimatesBarLengths[k]*180 + 'px', background: 'hsla(' +  speedEstimatesBarLengths[k]*120 + ', 75%, 35%,.5)'}"></div>
          <span style="white-space: nowrap; padding-left: 4px;">{{v | prettyBytes }}/s</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import prettyBytes from 'pretty-bytes'

export default {
  props: ['execHistory'],
  computed: {
    speedEstimates () {
      if (this.execHistory === null) return {}
      var res = {}
      for (let execIdx = this.execHistory.length - 1; execIdx >=0 ; execIdx--) {
        var exec = this.execHistory[execIdx]
        if (exec.completionTime - exec.startTime !== 0 && exec.volumeProcessed !== 0)
        res[exec.rule.repository.repoName] = exec.volumeProcessed*1000/(exec.completionTime - exec.startTime)
      }
      return res
    },
    speedEstimatesBarLengths () {
      var fastest = Math.max(...Object.values(this.speedEstimates))
      var res = {}
      for (var k in this.speedEstimates) {
        res[k] = Math.sqrt(this.speedEstimates[k]/fastest)
      }
      return res
    }
  },
  filters: {
    prettyBytes: prettyBytes
  }
}
</script>

<style>
</style>
