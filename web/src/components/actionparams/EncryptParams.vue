<template>
  <div style="width: 100%;">
    <div class="action-params" style="display: flex; flex-direction: row; flex-wrap: wrap;">
      <div class="field" style="flex: 0 0 auto; display: flex; flex-direction: row; flex-wrap: wrap; padding: 0px 8px;">
        <label for="" class="vs-input--label" style="flex: 1 0 100%;">Keep Original File</label><br>
        <vs-switch style="margin-top: auto; margin-bottom: auto; margin-left: 4px; margin-right: 4px; flex: 0 0 auto;" v-model="params.keepOriginal">
          <span slot="on"></span>
          <span slot="off"></span>
        </vs-switch>
      </div>
      <div class="field" style="flex: 0 0 auto; display: flex; flex-direction: row; flex-wrap: wrap; padding: 0px 8px;">
        <label for="" class="vs-input--label" style="flex: 1 0 100%;">Transfer File Metadata</label><br>
        <vs-switch style="margin-top: auto; margin-bottom: auto; margin-left: 4px; margin-right: 4px; flex: 0 0 auto;" v-model="params.transferMetadata">
          <span slot="on"></span>
          <span slot="off"></span>
        </vs-switch>
      </div>
    </div>
    <div class="field" style="flex: 1 0 100%; display: flex; flex-direction: row; flex-wrap: wrap; padding: 0px 8px;">
      <label for="" class="vs-input--label" style="flex: 1 0 100%;">Document Classifications</label><br>
      <div style="display: flex; flex-flow: row nowrap; width: 100%; padding-bottom: 4px; align-items: center;" v-for="(cls,idx) in params.classifications" :key="idx">
        <div class="con-select">
          <select v-model="cls.key" class="input-select vs-select--input">
            <option :key="k" :value="k" v-for="(v,k) in classificationKeys">{{ k }}</option>
          </select>
        </div>
        <vs-chips color="rgb(145, 32, 159)" placeholder="Multivalue field. Press enter after each entry" v-model="params.classifications[idx].value" style="flex: 1 1 65%;">
          <vs-chip :key="clidx" @click="removeClassificationValue(idx, clidx)" v-for="(cl, clidx) in params.classifications[idx].value" closable>
            {{ cl }}
          </vs-chip>
        </vs-chips>
        <vs-icon icon="delete_forever" style="line-height: 32px; padding: 0px 2px; cursor: pointer;" @click.native="removeClassification(idx)"></vs-icon>
      </div>
      <vs-divider style="padding: 0px 8px;" position="center">
        <div style="display: flex; flex-flow: row nowrap; font-size: .85rem; align-items: center; cursor: pointer;" @click="addClassification">
          <vs-icon icon="playlist_add" style="padding-right: 4px;"></vs-icon>
          Add Classification
        </div>
      </vs-divider>
    </div>
    <div class="field" style="flex: 1 0 100%; display: flex; flex-direction: row; flex-wrap: wrap;">
        <vs-input label="File Pattern" placeholder="" style="flex: 1 0 50%;" v-model="params.filePattern" class="field"/>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  data: () => ({
  }),
  computed: {
    ...mapState({
      rule: s => s.transient.ruleEditor.rule,
      params: s => s.transient.ruleEditor.rule.params,
      classificationKeys: s => s.enums.skydrmClassifications
    })
  },
  methods: {
    addClassification() {
      this.params.classifications.push({key: "", value: []})
    },
    removeClassification(idx) {
      this.params.classifications.splice(idx, 1)
    },
    removeClassificationValue(cidx, clidx) {
      this.params.classifications[cidx].value.splice(clidx, 1)
    }
  },
  beforeMount () {
    if (this.rule.params === null) {
      this.rule.params = {keepOriginal: false, transferMetadata: false, classifications: [], type: "EncryptActionParams", filePattern: ""}
    }
  },
  destroyed () {
    this.rule.params = null
  }
}

</script>

<style>
</style>