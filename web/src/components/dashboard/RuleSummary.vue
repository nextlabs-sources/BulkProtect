<template>
  <div id="rule-summary">
    <div class="inner">
      <h1>Active Rules</h1>
      <span style="font-size: small; opacity: .6;">{{ activeRules.length }} Active | {{ rules.length }} Total</span>
      <perfect-scrollbar v-if="activeRules.length != 0">
        <vs-list>
          <!-- <vs-list-header icon="check_circle" title="Active" color="success"></vs-list-header> -->
          <vs-list-item v-for="(rule, idx) in activeRules" :key="idx" :title="rule.name" :subtitle="'Scheduled to execute ' + $moment.utc(rule.nextScheduledTime).fromNow() + ' from now at ' + $moment(rule.nextScheduledTime).format('h:mm a')"></vs-list-item>
          <!-- <vs-list-item v-for="i in 6" v-bind:key="i" icon="assignment" title="Veronika Ossi" subtitle="Has not watched anything recently"></vs-list-item> -->
        </vs-list>
      </perfect-scrollbar>
      <div v-else style="display: flex; flex-flow: column nowrap; align-items: center; justify-content: center; opacity: .5; flex: 1 1 100%; font-family: 'Open Sans', sans-serif !important;">
        <vs-icon icon="not_interested" style="font-size: 5rem;"></vs-icon>
        <span>No currently active rules.</span>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'

export default {
  methods: {
    ...mapActions(['getConfiguration']),
    async refresh () {
      if (this.activeRules.some(e => (new Date()).getTime() > e.nextScheduledTime)) {
        await this.getConfiguration()
      }
      setTimeout(() => {
        this.$forceUpdate()
        this.refresh()
      }, 60000)
    }
  },
  computed: {
    ...mapState({
      rules: s => s.config.rules,
      activeRules: s => s.config.rules.filter(e => e.active),
    }),
  },
  mounted () {
    this.refresh()
  }
}
</script>

<style>
#rule-summary {
  flex: 0 0 300px;
  padding: 12px;
  overflow: hidden;
  height: 100%;
  box-sizing: border-box;
  padding-left: 0px;
}

#rule-summary .inner {
  background: #ffffff;
  /* border: 1px solid rgba(0,0,0,.25); */
  border-radius: 5px;
  height: 100%;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: stretch;
  box-shadow: 0px 0px 20px rgba(0, 0, 0, 0.1);
}

#rule-summary .vs-list--subtitle {
  opacity: .6;
}
</style>