<template>
  <div id="credentials" style="flex: 1 1 100%;">
    <div style="width: 100%; background: white;overflow-y: auto;overflow-x: hidden;">
      <h1 style="margin: 12px; background: white; width: 100%;"> Repositories</h1>
      <perfect-scrollbar style="background: white; width: 100%;" v-if="repos.length > 0 || newRepo === true">
        <vs-table :data="repos" style="background-color: white; width: 100%; max-width: 100%;" :hoverFlat="true">
          <template slot="thead">
            <vs-th style="width: 10%;">Repository Name</vs-th>
            <vs-th style="width: 22%;">Repository Root URL</vs-th>
            <vs-th style="width: 15%;">Repository Type</vs-th>
            <vs-th style="width: 10%;">Domain</vs-th>
            <vs-th style="width: 15%;">Username</vs-th>
            <vs-th style="width: 10%;">Password</vs-th>
            <vs-th style="width: 10%;">Authentication Type</vs-th>
            <vs-th style="width: 8%;">Actions</vs-th>
          </template>
          <template slot-scope="{data}" >
            <vs-tr v-for="(credential,idx) in data" :key="idx" :index="idx">
              <template v-if="!isEditRow(idx)">
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
                      <vs-icon style="cursor: pointer;" icon="edit" @click.native.stop="edit(idx, credential)"></vs-icon>
                    </vs-tooltip>
                    <vs-tooltip text="Delete" ref="deleteButton">
                      <vs-icon style="cursor: pointer;" icon="delete_forever" @click.native.stop="deleteRepo(idx, credential)"></vs-icon>
                    </vs-tooltip>
                  </div>
                </vs-td>
              </template>
              <template v-else>
                <vs-td :data="''"><vs-input v-model="editCred.repoName"></vs-input></vs-td>
                <vs-td :data="''"><vs-input v-model="editCred.rootUrl"></vs-input></vs-td>
                <vs-td :data="''">
                  <select v-model="editCred.repoType" style="max-height: 31px" class="input-select vs-select--input">
                    <option :value="item" :key="index" v-for="(item,index) in repoTypes">{{ item }}</option>
                  </select>
                </vs-td>
                <vs-td :data="''"><vs-input v-model="editCred.domain"></vs-input></vs-td>
                <vs-td :data="''"><vs-input v-model="editCred.username"></vs-input></vs-td>
                <vs-td :data="''"><vs-input type="password" v-model="editCred.password"></vs-input></vs-td>
                <vs-td :data="''">
                  <select v-model="editCred.authType" style="max-height: 31px" class="input-select vs-select--input">
                    <option :value="item" :key="index" v-for="(item,index) in authTypes">{{ item }}</option>
                  </select>
                </vs-td>
                <vs-td :data="''">
                  <div style="display: flex; flex-direction: row; flex-wrap: no-wrap;">
                    <vs-tooltip text="Save" ref="saveButton">
                      <vs-icon style="cursor: pointer;" icon="save" @click.native.stop="save(idx)"></vs-icon>
                    </vs-tooltip>
                    <vs-tooltip text="Cancel" ref="cancelButton-{{ idx }}">
                      <vs-icon style="cursor: pointer;" icon="cancel" @click.native.stop="cancel(idx)"></vs-icon>
                    </vs-tooltip>
                  </div>
                </vs-td>
              </template>
            </vs-tr>
            <vs-tr v-if="newRepo">
              <vs-td :data="''"><vs-input v-model="editCred.repoName"></vs-input></vs-td>
              <vs-td :data="''"><vs-input v-model="editCred.rootUrl"></vs-input></vs-td>
              <vs-td :data="''">
                <select v-model="editCred.repoType" style="max-height:31px" class="input-select vs-select--input">
                  <option :value="item" :key="index" v-for="(item,index) in repoTypes">{{ item }}</option>
                </select>
              </vs-td>
              <vs-td :data="''"><vs-input v-model="editCred.domain"></vs-input></vs-td>
              <vs-td :data="''"><vs-input v-model="editCred.username"></vs-input></vs-td>
              <vs-td :data="''"><vs-input type="password" v-model="editCred.password"></vs-input></vs-td>
              <vs-td :data="''">
                <select v-model="editCred.authType" style="max-height:31px" class="input-select vs-select--input">
                  <option :value="item" :key="index" v-for="(item,index) in authTypes">{{ item }}</option>
                </select>
              </vs-td>
              <vs-td :data="''">
                <div style="display: flex; flex-direction: row; flex-wrap: no-wrap;">
                  <vs-tooltip text="Save" ref="saveButton">
                    <vs-icon style="cursor: pointer;" icon="save" @click.native.stop="save(data.length)" title="Save"></vs-icon>
                  </vs-tooltip>
                  <vs-tooltip text="Cancel" ref="cancelButton">
                    <vs-icon style="cursor: pointer;" icon="cancel" @click.native.stop="cancel(data.length)"></vs-icon>
                  </vs-tooltip>
                </div>
              </vs-td>
            </vs-tr>
          </template>
        </vs-table>
      </perfect-scrollbar>
      <vs-divider position="center" style="width: 100%; padding: 0px 12px; background: white;">
        <div style="display: flex; flex-flow: row nowrap; font-size: .85rem; align-items: center; cursor: pointer;" @click="newRepository">
          <vs-icon icon="create_new_folder" style="padding-right: 4px;"></vs-icon>
          New Repository
        </div>
      </vs-divider>
    </div>
  </div>
</template>

<script>
// import CredentialsRow from './CredentialsRow.vue'
import { mapActions, mapState } from 'vuex'

export default {
  components: {
    // CredentialsRow
  },
  data: () => ({
    newRepo: false,
    showActions: true,
    editCred: {repoName: "", rootUrl: "", domain: "", username: "", password: "", repoType: "", authType: ""},
    repoEditStates: []
  }),
  computed: {
    ...mapState ({
      repos: s => s.config.repositories,
      repoTypes: s => s.enums.repoTypes,
      authTypes: s => s.enums.authTypes,
      rules: s => s.config.rules
    }),
  },
  watch: {
    repos () {
      this.newRepo = false
    }
  },
  methods: {
    ...mapActions([
      'updateRepository',
      'deleteRepository'
    ]),
    newRepository() {
      if (!this.showActions) {
        // When editing, prevent adding a new repo action
        // On edit, showAction becomes false.
        return;
      }
      this.editCred = {repoName: "", rootUrl: "", domain: "", username: "", password: "", repoType: "", authType: ""};
      this.newRepo = true
      this.showActions = false
    },
    cancel(repoIndex) {
      if (this.repoEditStates[repoIndex]) {
        this.repoEditStates[repoIndex].edit = false;
      }
      this.newRepo = false
      this.showActions = true
    },
    edit(repoIndex, credential) {
      this.repoEditStates[repoIndex].edit = true;
      if (this.credential !== null) {
        this.editCred = window.structuredClone(credential);
      }
      this.showActions = false
    },
    isEditRow(repoIndex) {
      if (this.repoEditStates.length === 0 || !this.repoEditStates[repoIndex]) {
        return false;
      }
      return this.repoEditStates.find(repo => repo.index === repoIndex).edit;
    },
    async save(repoIndex) {
      await this.updateRepository({index: repoIndex, repo: this.editCred})
      if (this.repoEditStates[repoIndex]) {
        this.repoEditStates[repoIndex].edit = false;
      }
      this.showActions = true
    },
    async deleteRepo(repoIndex, credential) {
      if (this.rules.some(r => r.repositoryId === credential.id)) {
        this.$vs.notify({
          title: "Cannot delete",
          text: "Repository is in use by some rules.",
          color: "danger",
          icon: "block"
        })
      } else { await this.deleteRepository(repoIndex) }
    }
  },
  beforeMount () {
    this.repoEditStates = [];
    this.repos.forEach((repo, index) => {
      this.repoEditStates.push({ index, edit: false })
    });
  },
  filters: {
    truncate (s) {
      return s.length < 30 ? s : s.substring(0, 60) + "...";
    }
  }
  // TODO work on add new repo button
}
</script>

<style>
#credentials {
  text-align: left;
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

#credentials #inner {
  text-align: left;
  max-width: 1024px;
  margin-left: auto;
  margin-right: auto;
}

#credentials .expand {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

#credentials .isActive {
  background: transparent !important;
}

#credentials .ps {
  /* width: 100%; */
  padding: 0px 12px;
  flex: 1 1 fit-content;
}

#credentials .editable .vs-input{
  width: 100% !important;
  padding: 4px;
}

#credentials .editable td {
  padding: 0px !important;
}

#credentials .vs-table {
  table-layout: fixed;
}

#credentials .vs-table .td {
  word-break: break-word;
}

#credentials .vs-con-input-label,
#credentials .con-select {
  width: auto;
}
</style>