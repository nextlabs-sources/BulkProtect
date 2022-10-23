<template>
  <div id="rules">
    <!-- <div id="loading-layover" style="height:100%; width: 100%; position: absolute; background: rgba(0,0,0,.5); z-index: 10;">
    </div> -->
    <div id="rule-list" style="display: flex; flex-flow: column nowrap;">
      
      <div id="rule-list-header" style="height:48px; display: flex; flex-flow: row nowrap; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,.1); box-shadow: 0 4px 4px -5px rgba(0,0,0,.08); font-family: 'Open Sans', sans-serif !important;">
        <div style="line-height: 48px; padding: 0px 8px;">
          <span :class="{selected: !activeOnly}" style="padding: 0px 8px; border-right: 1px solid rgba(0,0,0,.1); cursor: pointer;" @click="activeOnly = false">All</span>
          <span :class="{selected: activeOnly}" style="padding: 0px 8px; cursor: pointer;" @click="activeOnly = true">Active</span>
        </div>
        <div style="display: flex; flex-flow: row nowrap;">
          <div style="display: flex; flex-flow: row nowrap; align-items: center; cursor: pointer; margin-right: 8px; font-size: 14px;" @click="newRule">
            <vs-icon icon="add" style="padding-right: 0px; font-size: 22px;"></vs-icon>
            New Rule
          </div>
          <vs-input icon-after="true" icon="search" placeholder="Search rules..." v-model="search" style="width: 300px;"></vs-input>
        </div>
      </div>
      <perfect-scrollbar :options="{suppressScrollX: true}">
        <vs-list>
          <!-- <vs-list-header></vs-list-header> -->
          <div style="width: 100%; text-align: center; padding: 32px 0px; font-size: 1.5rem; opacity: .3; font-family: 'Open Sans', sans-serif !important;" v-if="!ruleEditor.editMode && rules.length === 0">No rules</div>
          <vs-list-item :class="{select: rules[ruleEditor.selectedRule] == r}" v-for="(r,idx) in filteredRules" :key="idx" icon="assignment" :title="r.name" :subtitle="r.repository!== null ? r.repository.rootUrl + r.subdirectory : ''" @click.native="select(r)">
            <div style="display: flex; flex-flow: row nowrap;">
              <vs-tooltip v-if="r.active" text="Deactivate">
                <vs-icon style="cursor: pointer;" icon="expand_more" @click.native.stop="changeActiveStatus(r)"></vs-icon>
              </vs-tooltip>
              <vs-tooltip text="Run Now">
                <vs-icon style="cursor: pointer;" icon="play_arrow" @click.native.stop="execute(r)"></vs-icon>
              </vs-tooltip>
              <vs-tooltip text="Delete">
                <vs-icon style="cursor: pointer;" icon="delete_forever" @click.native.stop="delRule(r)"></vs-icon>
              </vs-tooltip>
            </div>
          </vs-list-item>
          <vs-list-item class="select" v-if="ruleEditor.editMode && ruleEditor.selectedRule >= rules.length" icon="assignment" :title="rule.name" :subtitle="rule.repositoryId !== null ? truncate(repoMap[rule.repositoryId].rootUrl + rule.subdirectory) : ''"></vs-list-item>
          <vs-divider style="padding: 0px 8px;" position="center">
            <div style="display: flex; flex-flow: row nowrap; font-size: .85rem; align-items: center; cursor: pointer;" @click="newRule">
              <vs-icon icon="note_add" style="padding-right: 4px;"></vs-icon>
              New Rule
            </div>
          </vs-divider>
        </vs-list>
      </perfect-scrollbar>
    </div>
    
    <div id="rule-config-inner" v-if="ruleEditor.editMode">
      <h1 style="padding: 8px;">Rule Configuration</h1>
      <perfect-scrollbar>
        <div id="rule-config-content">
          <div class="expand">
            <div style="display: flex; flex-flow: row nowrap; width: 100%; padding-right: 8px;">
              <vs-input label="Rule Name" placeholder="" style="flex: 1 0 50%;" v-model="rule.name" class="field"/>
              <div class="con-select field">
                <label class="vs-input--label">Repository</label>
                <div class="input-select-con">
                  <select v-model="rule.repositoryId" class="input-select vs-select--input" style="max-height: 31px">
                    <option :value="item.id" :key="index" v-for="(item,index) in repos">{{ item.repoName }}</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="field" style="padding: 0px 8px; flex: 1 0 50%; box-sizing: border-box;" v-if="rule.repositoryId !== null">
              <label for="" class="vs-input--label" style="width: 100%;">Subdirectory</label><br>
              <div class="prefixed-input" style="display: flex; flex-direction: row; align-items: center; padding-left: 8px;">
                <span style="flex: 1 1 auto; color: rgba(91, 87, 119, 0.6); font-size: .85rem; height: auto; white-space: nowrap;">{{ repoMap[rule.repositoryId].rootUrl | truncateEnd }}</span>
                <vs-input id="subdirectory-input" placeholder="" style="flex: 1 1 100%; margin 0px;" v-model="rule.subdirectory"></vs-input>
              </div>
            </div>
            <div class="footer" style="width: 100%; min-height: 96px;">
              <div class="left" style="display: flex; flex-flow: row wrap; flex: 0 1 content; width: 100%;">
                <div class="field" style="flex: 0 1 auto; display: flex; flex-direction: column; flex-wrap: wrap; padding: 0px 0px; box-sizing: border-box;">
                  <label for="" class="vs-input--label" style="flex: 0 1 auto; width: 120px;">Scheduled Rule</label>
                  <vs-switch style="margin-left: 4px; margin-right: 4px; margin-top: auto; margin-bottom: auto; flex: 0 0 auto; width: 96px;" v-model="rule.active">
                    <span slot="on">Scheduled</span>
                    <span slot="off">On-demand</span>
                  </vs-switch>
                </div>
                <div v-if="rule.active" class="field" style="flex: 0 0 auto;">
                  <label for="" class="vs-input--label">Execution Frequency</label><br>
                  <Selector :items="frequencies" v-model="rule.execFreq" style="padding: 4px 4px;" @click.native="resetDropdownOptions()"></Selector>
                </div>
                <div v-if="showTimeSelector" class="field" style="flex: 0 0 auto;">
                  <label for="" class="vs-input--label">Time</label><br>
                  <VueTimepicker style="height: 20px; font: 400 13.3333px Arial; color: #5b5777;" :minute-interval="5" v-model="timePickerValue" @click.native="hideTimeSelectorOnMinuteClick"></VueTimepicker>
                </div>
                <template v-if="rule.active">
                  <div class="con-select field" v-if="rule.execFreq==='Minutes'" style="padding-left:0">
                    <label class="vs-input--label">Duration</label>
                    <div class="input-select-con">
                      <select v-model="rule.execMinute" class="input-select vs-select--input" style="max-height: 31px">
                        <option :value="index" :key="index" v-for="(index, minuteOption) in options.minutes">{{ minuteOption }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="con-select field" v-if="rule.execFreq==='Hourly'" style="padding-left:0">
                    <label class="vs-input--label">Duration</label>
                    <div class="input-select-con">
                      <select v-model="rule.execHour" class="input-select vs-select--input" style="max-height: 31px">
                        <option :value="index" :key="index" v-for="(index, hourOption) in options.hours">{{ hourOption }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="con-select field" v-if="rule.execFreq==='Monthly'" style="padding-left:0">
                    <label class="vs-input--label">Day</label>
                    <div class="input-select-con">
                      <select v-model="rule.execDay" class="input-select vs-select--input" style="max-height: 31px">
                        <option :key="i" :value="i" v-for="i in 30">{{ i }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="con-select field" v-if="rule.execFreq==='Weekly'" style="padding-left:0">
                    <label class="vs-input--label">Day</label>
                    <div class="input-select-con">
                      <select v-model="rule.execDay" class="input-select vs-select--input" style="max-height: 31px">
                        <option :key="index" :value="index" v-for="(index,day) in daysOfWeek">{{ day }}</option>
                      </select>
                    </div>
                  </div>
                </template>
              </div>
            </div>
            <div class="con-select field">
              <label class="vs-input--label">Action</label>
              <div class="input-select-con">
                <select v-model="rule.action" class="input-select vs-select--input" style="max-height: 31px">
                  <option :key="index" :value="item" :text="item" v-for="(item,index) in actions">{{ item }}</option>
                </select>
              </div>
            </div>
            <EncryptParams v-if="rule.action===actions[0]"></EncryptParams>
            <div v-if="rule.action===actions[0]" class="field" style="flex: 1 0 100%; display: flex; flex-direction: row; flex-wrap: wrap; padding: 0px 8px;">
              <label for="" class="vs-input--label" style="flex: 1 0 100%;">File Extension Criteria</label><br>
              <div style="display: flex; flex-wrap: row nowrap; width: 100%;">
                <vs-switch style="margin-top: auto; margin-bottom: auto; margin-left: 4px; margin-right: 4px; flex: 0 0 80px; width: 80px;" v-model="rule.fileExtCriteriaIncl">
                  <span slot="on">Include</span>
                  <span slot="off">Exclude</span>
                </vs-switch>
                <vs-chips color="rgb(145, 32, 159)" placeholder="Multivalue field. Press enter after each entry." v-model="rule.fileExtCriteria" style="flex: 1 1 auto;">
                  <vs-chip :key="idx" @click="remove(ext)" v-for="(ext, idx) in rule.fileExtCriteria" closable>
                    {{ ext }}
                  </vs-chip>
                </vs-chips>
              </div>
            </div>
          </div>
          <div class="right" style="display: flex; flex-direction: row; align-items: center; justify-content: flex-end; padding: 24px 0px; flex: 1 0 100%;">
              <vs-button v-if="ruleEditor.selectedRule >= rules.length" color="primary" type="border" style="margin: 0px 4px;" :class="{isActive: false}" @click="cancel()">
                Cancel
              </vs-button>
              <vs-button color="primary" type="border" style="margin: 0px 4px;" :class="{isActive: false}" @click="save()">
                Save
              </vs-button>
              <vs-button color="primary" type="border" style="margin: 0px 4px;" :class="{isActive: false}" @click="saveAndRun()">
                Save &amp; Run
              </vs-button>
            </div>
        </div>
      </perfect-scrollbar>
    </div>
    <div id="rule-config-inner" v-else style="display: flex; flex-flow: column nowrap; align-items: center; justify-content: center; opacity: .5; font-family: 'Open Sans', sans-serif !important;">
      <vs-icon icon="not_interested" style="font-size: 5rem;"></vs-icon>
      <span>No rule selected.</span>
      <span>Select a rule or <a style="cursor: pointer; color: rgba(var(--vs-primary),1);" @click="newRule()">create one</a>.</span>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import EncryptParams from './actionparams/EncryptParams.vue'
import Selector from './ui/Selector.vue'
import VueTimepicker from 'vue2-timepicker'

export default {
  components: {
    EncryptParams,
    Selector,
    VueTimepicker
  },
  data: () => ({
    actions: [
      "Rights Protect",
      "Remove Protection"
    ],
    frequencies: [
      "Minutes",
      "Hourly",
      "Daily",
      "Weekly",
      "Monthly"
    ],
    options: {
      minutes: {
        "1 minute": 1,
        "5 minutes": 5,
        "10 minutes": 10,
        "15 minutes": 15,
        "30 minutes": 30
      },
      hours: {
        "1 hour": 1,
        "2 hours": 2,
        "4 hours": 4,
        "8 hours": 8,
        "12 hours": 12
      }
    },
    daysOfWeek: {
      "Sunday":7,
      "Monday":1,
      "Tuesday":2,
      "Wednesday":3,
      "Thursday":4,
      "Friday":5,
      "Saturday":6
    },
    loading: false,
    timePickerValue: {
      HH: "00",
      mm: "00"
    },
    search: "",
    activeOnly: false
  }),
  methods: {
    ...mapActions(['updateRule', 'executeRule', 'deleteRule']),
    remove (item) {
      this.rule.fileExtCriteria.splice(this.rule.fileExtCriteria.indexOf(item), 1)
    },
    removeCls (clsKey) {
      delete this.params.classifications[clsKey]
    },
    flipIncl () {
      this.extIncluding = !this.extIncluding;
    },
    async saveAndRun () {
      await this.save();
      await this.executeRule(this.rules[this.ruleEditor.selectedRule])
    },
    async execute (rule) {
      await this.executeRule(rule)
    },
    async changeActiveStatus (rule) {
      var temp = JSON.parse(JSON.stringify(rule))
      temp.active = !temp.active
      this.loading = true
      this.clearTooltips()
      var idx = this.rules.indexOf(rule)
      await this.updateRule({index: idx, rule: temp})
      this.loading = false
    },
    async delRule (rule) {
      this.loading = true
      this.clearTooltips()
      await this.deleteRule(this.rules.indexOf(rule))
      this.ruleEditor.selectedRule = null
      this.ruleEditor.editMode = false
      this.loading = false
    },
    newRule () {
      this.clear()
      this.ruleEditor.selectedRule = this.rules.length
      this.ruleEditor.editMode = true
    },
    clear () {
      this.ruleEditor.rule = {
        name: "New Rule",
        repositoryId: null,
        subdirectory:"",
        action: this.actions[0],
        params: {keepOriginal: false, transferMetadata: false, classifications: [], type: "EncryptActionParams", filePattern: ""},
        fileExtCriteria: ["txt", "doc", "docx", "pptx", "ppt", "xls", "xlsx"],
        fileExtCriteriaIncl: true,
        active: false,
        execFreq: "Daily",
        execTime: 0,
        execDay: 0,
        execHour: 0,
        execMinute: 0,
      }
      this.timePickerValue = {HH: "00", mm: "00"}
    },
    async save () {
      await this.updateRule({index: this.ruleEditor.selectedRule, rule: this.ruleEditor.rule})
      this.select(this.rules[this.ruleEditor.selectedRule])
    },
    select (rule) {
      this.ruleEditor.selectedRule = this.rules.findIndex(e => e.id === rule.id)
      this.ruleEditor.rule = JSON.parse(JSON.stringify(rule))
      this.ruleEditor.editMode = true
      this.timePickerValue = {HH: Math.floor(this.ruleEditor.rule.execTime/60).toString().padStart(2, '0'), mm: (this.ruleEditor.rule.execTime % 60).toString().padStart(2, '0')}
    },
    selectIndex (i) {
      this.ruleEditor.selectedRule = i
      this.ruleEditor.rule = JSON.parse(JSON.stringify(this.rules[i]))
      this.ruleEditor.editMode = true
    },
    truncate (s) {
      return s.length < 50 ? s : s.substring(0, 50) + "...";
    },
    clearTooltips () {
      var tooltips = document.querySelectorAll('.vs-tooltip')
      tooltips.forEach(e => {
        e.parentNode.removeChild(e)
      })
    },
    cancel () {
      this.ruleEditor.selectedRule = null
      this.ruleEditor.editMode = false
    },
    resetDropdownOptions() {
      this.rule.execDay = 1
      this.rule.execMinute = 1;
      this.rule.execHour = 1;
    },
    hideTimeSelectorOnMinuteClick() {
      var minutes = document.querySelectorAll('ul.minutes > li')
      minutes.forEach(e => {
        e.addEventListener("click", () => {
          var el = document.querySelector('.time-picker-overlay')
          if (el !== null) el.click()
        })
      })
    }
  },
  computed: {
    ...mapState ({
      repos: s => s.config.repositories,
      ruleEditor: s => s.transient.ruleEditor,
      rule: s => s.transient.ruleEditor.rule,
      activeRules: s => s.config.rules.filter(e => e.active),
      inactiveRules : s => s.config.rules.filter(e => !e.active),
      rules: s => s.config.rules,
      repoMap: s => s.config.repositoryMap,
      ruleMap: s => s.config.ruleMap
    }),
    filteredRules () {
      return this.rules.filter(e => this.activeOnly ? e.active : true).filter(e => this.search !== "" ? e.name.toLowerCase().includes(this.search.toLowerCase()) : true)
    },
    showTimeSelector() {
      return this.rule.active && !['Minutes', 'Hourly'].includes(this.rule.execFreq);
    }
  },
  mounted () {
    this.clear()
  },
  filters: {
    truncate (s) {
      return s.length < 50 ? s : s.substring(0, 50) + "...";
    },
    truncateEnd (s) {
      return s.length < 30 ? s : "..." + s.substring(s.length-30);
    }
  },
  watch: {
    "timePickerValue": function (t) {
      try {
        this.rule.execTime = parseInt(t.HH) * 60 + parseInt(t.mm)
      } catch (error) {
        // Ignore
      }
    },
    search () { this.clearTooltips() },
    activeOnly () { this.clearTooltips() },
    "rule.action" : function (t) {
      if (t === 'Remove Protection') {
        this.rule.fileExtCriteriaIncl = true
        this.rule.fileExtCriteria = ['nxl']
      } else {
        if (JSON.stringify(this.rule.fileExtCriteria) === JSON.stringify(['nxl'])) {
          this.rule.fileExtCriteriaIncl = true,
          this.rule.fileExtCriteria = ["txt", "doc", "docx", "pptx", "ppt", "xls", "xlsx"]
        }
      }
    }
  }
}
</script>

<style>
#rules {
  width: 100%;
  display: flex;
  flex-direction: row;
}

#rule-list {
  flex: 1 1 50%;
  border-right: 1px solid rgba(0,0,0,.1);
  text-align: left;
}

#rule-list .vs-list {
  padding: 0px;
}

#rule-config-inner {
  text-align: left;
  margin-left: auto;
  margin-right: auto;
  flex: 1 0 50%;
  display: flex;
  flex-direction: column;
  padding: 0px 8px;
  min-width: 600px;
}

#rule-config-inner #rule-config-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1 0 100%;
}

#rule-config-inner .expand {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: flex-start;
}

#rule-config-inner .footer {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  flex-wrap: wrap;
  padding: 0px 8px;
}

#rule-config-inner .field {
  margin-bottom: 16px;
  margin-top: 16px;
}

#rule-config-inner .vs-input--label, 
#rule-config-inner .vs-select--label {
  font-size: 1rem !important;
  line-height: 32px;
}

#rule-config-inner .vs-con-input {
  padding-left: 2px;
}

#rules .vs-input,.con-select {
  padding: 0px 8px;
}

.isActive {
  background: transparent !important;
}

#rules #rule-config-inner .ps {
  height: 100%;
  width: 100%;
}

#rules .con-chips {
  box-shadow: none !important;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 5px;
  padding: 0px !important;
}

#rules .vs-list--header {
  box-shadow: 0 4px 4px -5px rgba(0,0,0,.08) !important;
  border-bottom: 2px solid rgba(0,0,0,.04);
  border-top: 2px solid rgba(0,0,0,.04);
}

#rules .vs-list--item {
  border-bottom: none;
  cursor: pointer;
}

#rules .vs-list--item.select {
  /* background: linear-gradient(90deg,transparent,rgba(0,0,0,.02)); */
  background: rgba(0,0,0,.02);
  border-right: 3px solid rgba(var(--vs-primary),1)!important;
}

#rules .vs-list--item:hover {
  /* background: linear-gradient(90deg,transparent,rgba(0,0,0,.02)); */
  background: rgba(0,0,0,.02);
  /* border-right: 3px solid rgba(var(--vs-primary),1)!important; */
}

#rules .vs-list--title {
  font-weight: 400;
}

#rules .time-picker > input {
  height: 28px;
  border-radius: 5px;
  margin-top: -2px;
}

#rules .date-time-picker #DateTimePicker {
  height: 20px !important;
}

#rules .vs-input--switch {
  margin: 0px 4px;
}

#rules .vs-switch--text {
  font-size: .9em !important;
}

#rules .footer .field {
  padding-right: 24px !important;
}

#rules .vs-con-input input.hasIcon {
  margin-top: 5px;
}

#rules #rule-list-header span:hover {
  color: rgba(var(--vs-primary),1);
}

#rules #rule-list-header span.selected {
  color: rgba(var(--vs-primary),1);
}

#rules .vs-list--subtitle {
  opacity: .5 !important;
}
</style>