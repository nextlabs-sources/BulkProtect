<template>
  <div id="history">
    <div id="header">
      <h1 style="margin: 12px">Rule Execution History</h1>
      <div id="filters" style="display: flex; flex-flow: row nowrap; width: 50%;">
        <div class="con-select">
          <label class="vs-select--label">Filter by Rule Name</label>
          <select v-model="filters[0]" style="max-height:31px" class="input-select vs-select--input">
            <option :value="item" :key="index" v-for="(item,index) in ruleNames">{{ item }}</option>
          </select>
        </div>
        <div class="con-select">
          <label class="vs-select--label">Filter by Repo Type</label>
          <select v-model="filters[1]" style="max-height:31px" class="input-select vs-select--input">
            <option :value="item" :key="index" v-for="(item,index) in repoTypes">{{ item }}</option>
          </select>
        </div>
        <div class="con-select">
          <label class="vs-select--label">Filter by Result</label>
          <select v-model="filters[2]" style="max-height:31px" class="input-select vs-select--input">
            <option :value="item" :key="index" v-for="(item,index) in results">{{ item }}</option>
          </select>
        </div>
      </div>
    </div>
    <perfect-scrollbar v-if="execs !== null">
      <vs-table :data="execs" style="background-color: white; width: 100%;" :hoverFlat="true" ref="table">
        <template slot="thead">
          <vs-th @click.native="sort('name')">
            Rule Name
            <vs-icon v-if="sortField==='name'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th style="max-length: 200px;" @click.native="sort('directory')">
            Target Directory
            <vs-icon v-if="sortField==='directory'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th style="cursor: pointer;" @click.native="sort('repoType')">
            Repository Type
            <vs-icon v-if="sortField==='repoType'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('action')">
            Rule Action
            <vs-icon v-if="sortField==='action'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('startTime')">
            Start Time
            <vs-icon v-if="sortField==='startTime'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('completionTime')">
            End Time 
            <vs-icon v-if="sortField==='completionTime'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('duration')">
            Duration 
            <vs-icon v-if="sortField==='duration'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('result')">
            Result 
            <vs-icon v-if="sortField==='result'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('processed')">
            Files Processed 
            <vs-icon v-if="sortField==='processed'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th @click.native="sort('failed')">
            Failed 
            <vs-icon v-if="sortField==='failed'" :icon="sortDirection ? 'arrow_drop_down' : 'arrow_drop_up'"></vs-icon>
          </vs-th>
          <vs-th>Errors</vs-th>
        </template>
        <template slot-scope="{data}">
          <vs-tr :state="tr.succeeded ? '' : '' " :key="indextr" v-for="(tr,indextr) in data" v-show="fil(tr)">
            <vs-td :data="tr.rule.name">{{ tr.rule.name }}</vs-td>
            <vs-tooltip :text="tr.rule.fullDirectory" position="bottom" v-if="tr.rule.fullDirectory.length > 60">
              <vs-td style="cursor: pointer;" :data="tr.rule.fullDirectory">{{ tr.rule.fullDirectory | truncate }}</vs-td>
            </vs-tooltip>
            <vs-td v-else :data="tr.rule.fullDirectory">{{ tr.rule.fullDirectory }}</vs-td>
            <vs-td :data="tr.rule.repository.repoType">{{tr.rule.repository.repoType}}</vs-td>
            <vs-td :data="tr.rule.action">{{tr.rule.action === '' ? 'TEST' : tr.rule.action }}</vs-td>
            <vs-td :data="tr.startTime">{{tr.startTime | moment("MMM D YYYY, h:mm:ss a") }}</vs-td>
            <vs-td :data="tr.completionTime">{{tr.completionTime | moment("MMM D YYYY, h:mm:ss a") }}</vs-td>
            <vs-td :data="tr.duration">{{ tr.completionTime - tr.startTime | duration('humanize') }}</vs-td>
            <vs-td :data="tr.succeeded ? 'Success' : 'Fail'">
              <span style="border-radius: 2px; padding: 2px 8px; color: rgba(255,255,255,.98);" :style="{background: tr.succeeded ? 'rgba(var(--vs-success),1)' : 'rgba(var(--vs-danger),1)' }">
                {{ tr.succeeded ? 'Success' : 'Fail' }}
              </span>
            </vs-td>
            <vs-td :data="tr.processed">{{tr.processed}}</vs-td>
            <vs-td :data="tr.failed">{{tr.failed}}</vs-td>
            <vs-td :data="''">
              <div style="display: flex; flex-direction: row; flex-wrap: no-wrap; padding-right: 32px;" v-if="tr.failed !== 0 && tr.succeeded || !tr.succeeded">
                <vs-tooltip text="View Errors">
                  <a @click.stop="" :href="'history/' + tr.logFilename + '.log'" :style="{color: tr.succeeded ? 'rgba(var(--vs-warning),1)' : 'rgba(var(--vs-danger),1)' }" target="_blank">
                    <vs-icon style="cursor: pointer;" icon="report_problem" :style="{color: tr.succeeded ? 'rgba(var(--vs-warning),1)' : 'rgba(var(--vs-danger),1)' }"></vs-icon>
                  </a>
                </vs-tooltip>
              </div>
            </vs-td>
            <template class="expand" slot="expand">
              <div class="con-expand" style="width: 100%; display: flex; flex-flow: row nowrap; justify-content: flex-start; align-items: center; padding-left: 12px;">
                <div class="rule-details" style="text-align: left; display: flex; flex-flow: column nowrap; justify-content: flex-start; padding-right: 12px;">
                  <div class="rule-details-pair">
                    <div class="rule-details-key">Rule Name </div>
                    <span class="rule-details-value" style="font-size: x-large; font-weight: medium;">{{ tr.rule.name }}</span>
                  </div>
                  <div class="rule-details-pair">
                    <div class="rule-details-key">Extension Criteria </div>
                    <span class="rule-details-value">
                      {{ tr.rule.fileExtCriteriaIncl ? 'Include ' : 'Exclude ' }}
                      {{ tr.rule.fileExtCriteria.join(', ') }}
                    </span>
                  </div>
                  <div class="rule-details-pair" v-if="false">
                    <div class="rule-details-key">Schedule </div>
                    <span class="rule-details-value">Daily at 1400hrs</span>
                  </div>
                  <template v-if="tr.rule.params !== null && tr.rule.params.type === 'EncryptActionParams'">
                    <div class="rule-details-pair" v-if="tr.rule.params !== null && tr.rule.params.keepOriginal !== null">
                      <div class="rule-details-key">Keep Original File </div>
                      <span class="rule-details-value">{{ tr.rule.params.keepOriginal ? 'True' : 'False' }}</span>
                    </div>
                    <div class="rule-details-pair" v-if="tr.rule.params !== null && tr.rule.params.transferMetadata!== null">
                      <div class="rule-details-key">Transfer File Metadata </div>
                      <span class="rule-details-value">{{ tr.rule.params.transferMetadata ? 'True' : 'False' }}</span>
                    </div>
                    <div class="rule-details-pair" v-if="tr.rule.params !== null && tr.rule.params.classifications!== null && tr.rule.params.classifications.length > 0">
                      <div class="rule-details-key">Classifications </div>
                      <span class="rule-details-value">
                        {{ tr.rule.params.classifications.map(e => e.key + ': ' + e.value.join(', ')).join(' | ') }}
                      </span>
                    </div>
                  </template>
                </div>
                  
                <div style="display: flex; flex-flow: column nowrap; align-items: center; padding-right: 12px;" v-if="tr.extMap !== null && tr.extMap !== undefined && Object.keys(tr.extMap).length !== 0">
                  <span style="font-weight: bold; font-size: larger; padding: 0px 48px;">Repository File Composition</span>
                  <FileExtPie :extMap="tr.extMap" :showLegend="true" style="width: 180px;"></FileExtPie>
                </div>
                <div style="display: flex; flex-flow: column nowrap; flex: 1 1 auto;">
                  <div style="display: flex; flex-flow: row nowrap; flex: 0 1 auto;">
                    <div class="exec-stat">
                      <span style="font-weight: bold; font-size: larger; opacity: .5;">Total Files</span>
                      <span style="font-size: 3rem; font-weight: 100;">{{ tr.total }}</span>
                    </div>
                    <div class="exec-stat">
                      <span style="font-weight: bold; font-size: larger; opacity: .5;">Files Processed</span>
                      <span style="font-size: 3rem; font-weight: 100;">{{ tr.processed }}</span>
                    </div>
                    <div class="exec-stat" style="flex: 0 1 320px;">
                      <span style="font-weight: bold; font-size: larger; opacity: .5;">Vol. of Files Processed</span>
                      <span style="font-size: 3rem; font-weight: 100;">{{ tr.volumeProcessed | prettyBytes }}</span>
                    </div>
                  </div>
                  <div style="display: flex; flex-flow: row nowrap;">
                    <div class="exec-stat">
                      <span style="font-weight: bold; font-size: larger; opacity: .5;">Skipped</span>
                      <span style="font-size: 3rem; font-weight: 100;">{{ tr.skipped }}</span>
                    </div>
                    <div class="exec-stat">
                      <span style="font-weight: bold; font-size: larger; opacity: .5;">Failed</span>
                      <span style="font-size: 3rem; font-weight: 100;">{{ tr.failed }}</span>
                    </div>
                    <div class="exec-stat" style="flex: 0 1 320px;" v-if="(tr.completionTime - tr.startTime) !== 0 && tr.volumeProcessed > 0">
                      <span style="font-weight: bold; font-size: larger; opacity: .5;">Processing Speed</span>
                      <span style="font-size: 3rem; font-weight: 100;">{{ tr.volumeProcessed*1000/(tr.completionTime - tr.startTime) | prettyBytes }}/s</span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </vs-tr>
        </template>
      </vs-table>
    </perfect-scrollbar>
    <div v-else style="flex: 1 1 100%;">
      Spinner
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import prettyBytes from 'pretty-bytes'
import FileExtPie from './ui/FileExtPie.vue'

export default {
  components: {
    FileExtPie
  },
  data: () => ({
    repositoryTypes: [
      "Windows Shared Folder",
      "Sharepoint",
      "Azure File Storage"
    ],
    authenticationTypes: [
      "NTLM",
      "Azure",
      "Sharepoint Online"
    ],
    filters: ["All", "All", "All"],
    sortField: "completionTime",
    sortDirection: false
  }),
  filters: {
    truncate (s) {
      return s.length < 30 ? s : s.substring(0, 60) + "...";
    },
    prettyBytes: prettyBytes, 
  },
  methods: {
    sort: function (fieldname) {
      if (this.sortField===fieldname) this.sortDirection = !this.sortDirection;
      else {
        this.sortField = fieldname
        this.sortDirection = true
      }
    },
    fil: function (e) {
      var filterFuncs = [
        e => this.filters[0] === 'All' ? true : e.rule.name === this.filters[0],
        e => this.filters[1] === 'All' ? true : e.rule.repository.repoType === this.filters[1],
        e => this.filters[2] === 'All' ? true : (e.succeeded ? 'Success' : 'Fail') === this.filters[2],
      ]
      return filterFuncs.every(f => f(e))
    }
  },
  computed: {
    ...mapState ({
      execsRaw: s => s.history.ruleExec === null ? null : Array.prototype.slice.call(s.history.ruleExec),
      ruleNames: s => s.history.ruleExec === null ? [] : ['All'].concat(Array.from((new Set(s.history.ruleExec.map(e => e.rule.name))).keys())),
      repoTypes: s => s.history.ruleExec === null ? [] : ['All'].concat(Array.from((new Set(s.history.ruleExec.map(e => e.rule.repository.repoType))).keys())),
      results: s => s.history.ruleExec === null ? [] : ['All'].concat(Array.from((new Set(s.history.ruleExec.map(e => e.succeeded ? 'Success' : 'Fail'))).keys())),
    }),
    execs () { 
      var keyFunc = x => x.failed;
      if (this.sortField === 'name') keyFunc = x => x.rule.name.toLowerCase()
      else if (this.sortField === 'directory') keyFunc = x => x.rule.fullDirectory.toLowerCase()
      else if (this.sortField === 'repoType') keyFunc = x => x.rule.repository.repoType
      else if (this.sortField === 'action') keyFunc = x => x.rule.action
      else if (this.sortField === 'startTime') keyFunc = x => x.startTime
      else if (this.sortField === 'completionTime') keyFunc = x => x.completionTime
      else if (this.sortField === 'duration') keyFunc = x => x.completionTime - x.startTime
      else if (this.sortField === 'result') keyFunc = x => x.succeeded
      else if (this.sortField === 'processed') keyFunc = x => x.processed
      var sortFunc = this.sortDirection ? ((a,b) => (keyFunc(a) < keyFunc(b) ? -1 : 1)) : ((a,b) => (keyFunc(a) < keyFunc(b) ? 1 : -1));
      return this.execsRaw === null ? null : Array.prototype.slice.call(this.execsRaw).sort(sortFunc); 
    }
  }
}
</script>

<style>
  #history {
    text-align: left;
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  #history > #header {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
  }

  #history #inner {
    text-align: left;
    max-width: 1024px;
    margin-left: auto;
    margin-right: auto;
  }

  #history .expand {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
  }

  #history .vs-input {
    padding: 0px 8px;
  }

  .isActive {
    background: transparent !important;
  }

  #history .ps {
    width: 100%;
    /* padding: 0px 12px; */
    flex: 1 1 fit-content;
  }

  #history .tr-table-state-danger {
    box-shadow: none !important;
  }

  #history .con-vs-tooltip {
    margin-bottom: -6px;
  }

  #history .rule-details-value {
    font-weight: bold;
  }

  #history .rule-details-pair {
    flex: 0 1 auto;
    padding: 6px 0px 0px 0px;
    max-width: 360px;
  }

  #history .error-log {
    font-family: 'Fira Code', monospace;
    font-size: 10px;
    font-weight: 700;
    line-height: 10px;
  }

  #history .vs-table-text {
    cursor: pointer;
    user-select: none;
  }

  #history .rule-details-key {
     min-width: 240px;
     opacity: .5;
  }

  #history .exec-stat {
    display: flex;
    flex-flow: column nowrap;
    align-items: left;
    padding-right: 24px;
    flex: 0 1 160px;
  }
</style>