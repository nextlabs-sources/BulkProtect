<template>
  <div id="home">
    <div style="display: flex; flex-flow: row nowrap; flex: 0 0 55%; height: 55%;">
      <Statistics></Statistics>
      <RuleSummary></RuleSummary>
      <SkyDRMSummary></SkyDRMSummary>
    </div>
    <AttentionItems></AttentionItems>
  </div>
</template>

<script>
import RuleSummary from './dashboard/RuleSummary.vue'
import Statistics from './dashboard/Statistics.vue'
import SkyDRMSummary from './dashboard/SkyDRMSummary.vue'
import AttentionItems from './dashboard/AttentionItems.vue'

export default {
  components: {
    RuleSummary,
    Statistics,
    SkyDRMSummary,
    AttentionItems
  },
  data:()=>({})
}
</script>

<style>
#home {
  text-align: left;
  display: flex;
  height: 100%;
  flex-flow: column nowrap;
  /* padding: 12px; */
  flex-grow: 0;
  align-content: stretch;
  justify-content: stretch;
  background: #f2f3f5;
  width: 100%;
}

header {
  background-color: white;
}

.td-check {
  width: 48px;
}
</style>