<template>
  <div id="event-summary">
    <h1>Rule Execution History</h1>
    <perfect-scrollbar ref="eventList">
      <vs-list>
        <!-- <vs-list-header icon="supervisor_account" title="Group 1"></vs-list-header> -->
        <vs-list-item v-for="(msg, idx) in events" :key="idx" icon="check" v-bind:class="{success: isSuccess(msg)}" title="Event" :subtitle="msg"></vs-list-item>
      </vs-list>
    </perfect-scrollbar>
  </div>
</template>

<script>

export default {
  data: () => ({
    events: []
  }),
  methods: {
    isSuccess (msg) {
      return msg.includes("Completed")
    }
  }
}
</script>

<style>
#event-summary {
  flex: 0 0 65%;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: stretch;
  overflow: hidden;
  height: 50%;
  background: #ffffff;
  /* border: 1px solid rgba(0,0,0,.25); */
  padding: 12px;
  box-sizing: border-box;
  border-radius: 5px;
  box-shadow: 0px 0px 20px rgba(0, 0, 0, 0.05);
}

.vs-list--item.success {
  background-color: rgba(70, 201, 58, 0.1);
  border: 1px solid rgba(70, 201, 58, 1);
}

.vs-list--item.failed {
  background-color: rgba(255, 71, 87, 0.1);
  border: 1px solid rgba(255, 71, 87, 1);
}

.vs-list--item.warning {
  background-color: rgba(255, 186, 0, 0.1);
  border: 1px solid rgba(255, 186, 0, 1);
}
</style>