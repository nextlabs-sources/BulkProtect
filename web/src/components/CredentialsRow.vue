<template>
  <vs-tr :data="credential" v-if="credential!==null && credential!==undefined && !editable">
    <vs-td :data="credential.repoName">{{credential.repoName}}</vs-td>
    <vs-td :data="credential.rootUrl">{{credential.rootUrl | truncate }}</vs-td>
    <vs-td :data="credential.repoType">{{credential.repoType}}</vs-td>
    <vs-td :data="credential.domain">{{credential.domain}}</vs-td>
    <vs-td :data="credential.username">{{credential.username}}</vs-td>
    <vs-td :data="credential.password">••••••••</vs-td>
    <vs-td :data="credential.authType">{{credential.authType}}</vs-td>
    <vs-td :data="credential">
      <div style="display: flex; flex-direction: row; flex-wrap: no-wrap;" v-if="showActions">
        <vs-tooltip text="Edit">
          <vs-icon style="cursor: pointer;" icon="edit" @click.native.stop="edit()"></vs-icon>
        </vs-tooltip>
        <vs-tooltip text="Delete" ref="deleteButton">
          <vs-icon style="cursor: pointer;" icon="delete_forever" @click.native.stop="deleteRepo()"></vs-icon>
        </vs-tooltip>
      </div>
    </vs-td>
  </vs-tr>
  <vs-tr v-else class="editable">
    <vs-td :data="''"><vs-input v-model="editCred.repoName"></vs-input></vs-td>
    <vs-td :data="''"><vs-input v-model="editCred.rootUrl"></vs-input></vs-td>
    <vs-td :data="''">
      <vs-select v-model="editCred.repoType" style="flex: 1 0 100%" class="field">
        <vs-select-item :key="index" :value="item" :text="item" v-for="(item,index) in repoTypes" />
      </vs-select>
    </vs-td>
    <vs-td :data="''"><vs-input v-model="editCred.domain"></vs-input></vs-td>
    <vs-td :data="''"><vs-input v-model="editCred.username"></vs-input></vs-td>
    <vs-td :data="''"><vs-input type="password" v-model="editCred.password"></vs-input></vs-td>
    <vs-td :data="''">
      <vs-select v-model="editCred.authType" style="flex: 1 0 100%" class="field">
        <vs-select-item :key="index" :value="item" :text="item" v-for="(item,index) in authTypes" />
      </vs-select>
    </vs-td>
    <vs-td :data="''">
      <div style="display: flex; flex-direction: row; flex-wrap: no-wrap;">
        <vs-tooltip text="Save" ref="saveButton">
          <vs-icon style="cursor: pointer;" icon="save" @click.native.stop="save()"></vs-icon>
        </vs-tooltip>
        <vs-tooltip text="Cancel" ref="cancelButton">
          <vs-icon style="cursor: pointer;" icon="cancel" @click.native.stop="cancel()"></vs-icon>
        </vs-tooltip>
      </div>
    </vs-td>
  </vs-tr>
</template>

<script>
// This vue component is no longer in use, moved into credentials because vs-tr cannot exists inside a component separately
import { mapState, mapActions } from 'vuex'

export default {
  props: ['credential', 'index', 'showActions', 'newRepo'],
  data: () => ({
    editable: false,
    editCred: {repoName: "", rootUrl: "", domain: "", username: "", password: "", repoType: "", authType: ""},
  }),
  methods: {
    ...mapActions(['updateRepository','deleteRepository']),
    edit () {
      if (this.credential !== null) {
        this.editCred = JSON.parse(JSON.stringify(this.credential))
      }
      this.editable = true
      this.$emit('edit')
    },
    async save () {
      await this.updateRepository({index: this.index, repo: this.editCred})
      this.editable = false
      this.$emit('save')
    },
    clicktr () {
    },
    new () {
      this.editCred = {repoName: "", rootUrl: "", domain: "", username: "", password: "", repoType: "", authType: ""}
    },
    cancel () {
      this.clearTooltips()
      this.editable = false
      this.$emit('cancel')
    },
    clearTooltips() {
      var el = this.$refs.cancelButton
      if(el!==undefined) el.$el.dispatchEvent(new Event('mouseleave'))
      el = this.$refs.saveButton
      if(el!==undefined) el.$el.dispatchEvent(new Event('mouseleave'))
      el = this.$refs.deleteButton
      if(el!==undefined) el.$el.dispatchEvent(new Event('mouseleave'))
    },
    async deleteRepo() {
      if (this.rules.some(r => r.repositoryId === this.credential.id)) {
        this.$vs.notify({
          title: "Cannot delete",
          text: "Repository is in use by some rules.",
          color: "danger",
          icon: "block"
        })
      } else { await this.deleteRepository(this.index) }
    }
  },
  created () {
    if (this.credential === null) {
      this.editable = true
      this.$emit('edit')
    }
  },
  computed: {
    ...mapState ({
      repoTypes: s => s.enums.repoTypes,
      authTypes: s => s.enums.authTypes,
      rules: s => s.config.rules
    }),
  },
  watch: {
    newRepo () {
      this.new()
      this.edit()
    }
  },
  beforeDestroy() {
    this.clearTooltips()
  },
  filters: {
    truncate (s) {
      return s.length < 30 ? s : s.substring(0, 60) + "...";
    }
  }
}
</script>

<style>
</style>