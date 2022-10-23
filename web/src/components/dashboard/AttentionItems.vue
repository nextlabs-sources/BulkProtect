<template>
  <div id="attention-items">
    <div class="inner">
      <div style="width: 100%; display: flex; flex-flow: row nowrap; justify-content: space-between;">
        <h1>Rule Executions <span style="opacity: .5; font-size: medium;">(Last 24 Hours)</span></h1>
        <div id="fail-filter-selector" style="padding: 0px 8px; font-family: 'Open Sans', sans-serif !important;">
          <span :class="{selected: failOnly}" style="padding: 0px 8px; border-right: 1px solid rgba(0,0,0,.1); cursor: pointer;" @click="failOnly = true">Failures</span>
          <span :class="{selected: !failOnly}" style="padding: 0px 8px; cursor: pointer;" @click="failOnly = false">All</span>
        </div>
      </div>
      <perfect-scrollbar style="z-index: 1;" v-if="execs !== null && execs.length > 0">
        <vs-table :data="execs" style="background-color: white; width: 100%; z-index: 0;" :hoverFlat="true">
          <template slot="thead">
            <vs-th>Rule Name</vs-th>
            <vs-th style="max-length: 200px;">Target Directory</vs-th>
            <vs-th>Rule Action</vs-th>
            <vs-th>Start Time</vs-th>
            <vs-th>End Time</vs-th>
            <vs-th>Duration</vs-th>
            <vs-th>Result</vs-th>
            <vs-th>Errors</vs-th>
          </template>
          <template slot-scope="{data}">
            <vs-tr :state="tr.succeeded ? '' : '' " :key="indextr" v-for="(tr,indextr) in data">
              <vs-td :data="tr.rule.name">{{ tr.rule.name }}</vs-td>
              <vs-tooltip :text="tr.rule.fullDirectory" position="bottom" v-if="tr.rule.fullDirectory.length > 30">
                <vs-td style="cursor: pointer;" :data="tr.rule.fullDirectory">{{ tr.rule.fullDirectory }}</vs-td>
              </vs-tooltip>
              <vs-td v-else :data="tr.rule.fullDirectory">{{ tr.rule.fullDirectory }}</vs-td>
              <vs-td :data="tr.rule.action">{{ tr.rule.action }}</vs-td>
              <vs-td :data="tr.startTime">{{tr.startTime | moment("MMM D YYYY, h:mm:ss a") }}</vs-td>
              <vs-td :data="tr.completionTime">{{tr.completionTime | moment("MMM D YYYY, h:mm:ss a") }}</vs-td>
              <vs-td :data="tr.duration">{{ tr.completionTime - tr.startTime | duration('humanize') }}</vs-td>
              <vs-td :data="tr.succeeded ? 'Success' : 'Fail'">
                <span style="border-radius: 2px; padding: 2px 8px; color: rgba(255,255,255,.98);" :style="{background: tr.succeeded ? 'rgba(var(--vs-success),1)' : 'rgba(var(--vs-danger),1)' }">
                  {{ tr.succeeded ? 'Success' : 'Fail' }}
                </span>
              </vs-td>
              <vs-td :data="''">
                <div style="display: flex; flex-direction: row; flex-wrap: no-wrap;" v-if="tr.failed !== 0 && tr.succeeded || !tr.succeeded">
                  <vs-tooltip text="View Errors">
                    <a @click.stop="" :href="'history/' + tr.logFilename + '.log'" :style="{color: tr.succeeded ? 'rgba(var(--vs-warning),1)' : 'rgba(var(--vs-danger),1)' }" target="_blank">
                      <vs-icon style="cursor: pointer;" icon="report_problem" :style="{color: tr.succeeded ? 'rgba(var(--vs-warning),1)' : 'rgba(var(--vs-danger),1)' }"></vs-icon>
                    </a>
                  </vs-tooltip>
                </div>
              </vs-td>
              <!-- <template class="expand" slot="expand">
                <div class="con-expand">Details</div>
              </template> -->
            </vs-tr>
          </template>
        </vs-table>
      </perfect-scrollbar>
      <div v-else-if="execs === null" style="width: 100%; flex: 1 1 100%; display: flex; align-items: center; justify-content: center;">
        <ClipLoader :size="80" color="rgba(0,0,0, .3)"></ClipLoader>
      </div>
      <div v-else style="width: 100%; flex: 1 1 100%; display: flex; align-items: center; justify-content: center; flex-flow: column nowrap; opacity: .5; font-family: 'Open Sans', sans-serif !important;">
        <vs-icon icon="not_interested" style="font-size: 5rem;"></vs-icon>
        <span v-if="failOnly">No failures during last 24 hours.</span>
        <span v-else>No rules executed in last 24 hours.</span>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { ClipLoader } from '@saeris/vue-spinners'

export default {
  components: {
    ClipLoader
  },
  data: () => ({
    failOnly: true
  }),
  filters: {
    truncate (s) {
      return s.length < 50 ? s : s.substring(0, 50) + "...";
    }
  },
  computed: {
    ...mapState ({
      execsRaw: s => s.history.ruleExec === null ? null : s.history.ruleExec
    }),
    execs () {
      return this.execsRaw === null ? null : this.execsRaw.filter(e => (e.completionTime > Date.now() - 86400000) && (this.failOnly ? !e.succeeded : true))
    }
  }
}
</script>

<style>
#attention-items {
  flex: 1 0 45%;
  padding: 12px;
  overflow: hidden;
  height: 50%;
  box-sizing: border-box;
  padding-top: 0px;
}

#attention-items .inner {
  background: #ffffff;
  /* border: 1px solid rgba(0,0,0,.25); */
  height: 100%;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: stretch;
  border-radius: 5px;
  box-shadow: 0px 0px 20px rgba(0, 0, 0, 0.1);
}

#attention-items-inner {
  height: 100%;
}

#attention-items-container {
  overflow-y: auto;
  height: auto;
}

.vs-collapse {
  overflow-y: auto;
  height: 100%;
}

.vs-collapse-item--header .item-header  {
  display: flex;
  align-items: center;
}

.vs-collapse-item--header .item-header .vs-icon {
  font-size: 1.2rem!important;
  padding: 0px 6px;
}

#attention-items .tr-table-state-danger {
  box-shadow: none !important;
}

#attention-items .vs-table tr:nth-child(even) {
  background: rgba(0,0,0,.03);
}

#attention-items #fail-filter-selector span:hover {
  color: rgba(var(--vs-primary),1);
}

#attention-items #fail-filter-selector span.selected {
  color: rgba(var(--vs-primary),1);
}

#attention-items .con-vs-tooltip {
  margin-bottom: -6px;
}
</style>