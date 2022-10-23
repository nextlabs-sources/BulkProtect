<template>
  <div id="app" ref="sidebar-parent">
    <vs-navbar color="#36424A" text-color="rgba(255,255,255,.6)" active-text-color="rgba(255,255,255,1)" class="myNavbar" style="border-bottom: 4px solid #70B55B;">
      <div id="navbar-left" slot="title">
        <!-- <vs-navbar-title> -->
          <img id="logo" alt="Nextlabs logo" src="./assets/logo-white.png">
          <span style="font-weight: 400; font-size: 20px; font-family: 'Segoe UI'; padding-bottom: 4px;">Rights Protection Tool™</span>
      </div>
    <div id="navbar-right" style="padding-right: 8px; display: flex; align-content: center; align-items: center; font-family: 'Open Sans', sans-serif;" v-if="notifs.showEvent">
      <span style="font-size: 0.8rem; margin-right: 8px;">{{ notifs.event }}</span>
      <ClipLoader :size="20" color="white" v-if="notifs.eventType === 'loading'"></ClipLoader>
      <div class="icon-container" style="height: 22px; width: 22px; display: flex; justify-content: center; align-items: center;" v-if="notifs.eventType !== 'loading'">
        <sweetalert-icon :icon="notifs.eventType" />
      </div>
    </div>
    </vs-navbar>
    
    <div id="main-area">
      <vs-sidebar static-position default-index="0" color="primary" class="sidebarx" spacer style="flex: 0 0 210px;">
        <div class="header-sidebar" slot="header">
          <h4 style="align-items:center; display: flex; justify-content: center;">
            <!-- <vs-tooltip text="Click to Logout" position="bottom" > -->
              <div style="align-items:center; display: flex; justify-content: center; cursor: pointer;">
                <vs-icon icon="account_circle" style="padding-right: 4px;"></vs-icon>
                Administrator
              </div>
            <!-- </vs-tooltip> -->
          </h4>
          <!-- <vs-button icon="exit_to_app" color="primary" type="flat">Log Out</vs-button> -->
        </div>
        <vs-sidebar-item index="0" icon="home" @click="showPage(0)">
          Dashboard
        </vs-sidebar-item>
        <vs-sidebar-item index="1" icon="folder_shared" @click="showPage(1)">
          Repositories
        </vs-sidebar-item>
        <vs-sidebar-item index="2" icon="assignment" @click="showPage(2)">
          Rules
        </vs-sidebar-item>
        <vs-sidebar-item index="3" icon="history" @click="showPage(3)">
          Execution History
        </vs-sidebar-item>
        <vs-sidebar-item index="4" icon="settings" @click="showPage(4)">
          Settings
        </vs-sidebar-item>
        
        <div class="footer-sidebar" slot="footer">
          
        </div>
      </vs-sidebar>
      <Home v-if="activePageIndex==0"></Home>
      <Credentials v-if="activePageIndex==1"></Credentials>
      <Rules v-if="activePageIndex==2"></Rules>
      <ExecutionHistory v-if="activePageIndex==3"></ExecutionHistory>
      <Settings v-show="activePageIndex==4"></Settings>
    </div>
  </div>
</template>

<script>
import Home from './components/Home.vue'
import Rules from './components/Rules.vue'
import Credentials from './components/Credentials.vue'
import Settings from './components/Settings.vue'
import ExecutionHistory from './components/ExecutionHistory.vue'
import { ClipLoader } from '@saeris/vue-spinners'
import { mapState } from 'vuex'

export default {
  name: 'app',
  components: {
    Credentials,
    Home,
    Rules,
    Settings,
    ClipLoader,
    ExecutionHistory
  },
  data() {
    return {
      activePageIndex: 0
    }
  },
  computed: {
    ...mapState ({
      notifs: s => s.notify,
    })
  },
  methods: {
    showPage(pageIndex) {
      this.activePageIndex = pageIndex;
    },
  },
  
}
</script>

<style>
:root {
  --vs-primary: rgb(255,255,255)
}

body {
  overflow: hidden;
  font-family: 'Fira Sans', sans-serif;
}

/* body h1,h2,h3,h4,h5,h6 {
  font-family: 'Open Sans', sans-serif;
} */
/* 
body h1 {
  font-weight: 100;
  opacity: .5;
} */

#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  /* background-color: rgb(0.9, 0.9, 0.9); */
  height: 100vh;
  display: flex;
  flex-direction: column;
  color: #5b5777;
  justify-content: stretch;
  align-content: stretch;
  position: absolute;
  top: 0;
  left: 0;
  overflow-x: hidden;
}

h1, h2, h3, h4, h5, h6 {
  font-family: Montserrat;
  font-weight: 100;
}

.vs-navbar {
  color: rgb(255,255,255);
  flex: 0 0 44px;
}

#navbar-left {
  display: flex;
  align-items: center;
}

#logo {
  padding: 0px 10px;
}

#navbar-left a {
  padding: 8px 12px;
}

.vs-tooltip {
  display: inherit !important;
  font-family: 'Open Sans', sans-serif;
  transition: none !important;
  z-index: 100000 !important;
  box-sizing: content-box !important;
  max-width: unset !important;
}

.con-vs-tooltip > .vs-tooltip {
  display: none !important;
}

#main-area {
  flex: 1 1 100%;
  /* height: 100%; */
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  width: 100vw;
  display: flex;
  flex-direction: row;
  /* padding-left: 260px; */
}

.vs-sidebar--background {
  background: rgba(0,0,0,0) !important;
  z-index: 0 !important;
  display: none !important;
}

.vs-sidebar {
  max-width: none !important;
}

.vs-sidebar-item-active {
  /* background: linear-gradient(90deg,transparent,rgba(0,0,0,.05)) !important; */
  background: rgba(0,0,0,.025) !important;
  font-weight: 400 !important;
}

.vs-sidebar--item:hover:not(.vs-sidebar-item-active) {
  /* background: linear-gradient(90deg,transparent,rgba(0,0,0,.05)); */
  background: rgba(0,0,0,.025);
}

.vs-sidebar--item > a {
  font-size: 1rem !important;
  font-family: 'Open Sans', sans-serif !important;
}

.vs-sidebar--item > a > i {
  font-size: 1.2rem !important;
}

.header-sidebar {
  flex-direction: row !important;
  flex-wrap: nowrap !important;
  justify-content: space-between !important;
}

.sa-warning:before,.sa-info:before {
  position: relative !important;
}

.sa {
  transform: scale(0.25);
  margin: unset !important;
}

.ps__rail-y {
  z-index: 2147483647;
}

.ul-tabs span {
  font-family: 'Open Sans', sans-serif !important;
}

.ul-tabs .activeChild button.vs-tabs--btn {
  padding: 10px !important;
}
</style>
