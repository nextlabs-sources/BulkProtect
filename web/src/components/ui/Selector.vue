<template>
  <div class="ui-selector">
    <div v-for="(item, idx) in items" :key="idx" class="select-item" :class="{isSelected: item === value}" @click="$emit('input', item)">{{ item }}</div>
  </div>
</template>

<script>
export default {
  props: ['items','value']
}
</script>

<style>
.ui-selector {
  display: flex;
  flex-flow: row nowrap;
  height: 32px;
}

.ui-selector .select-item {
  padding: 0px 12px;
  border-color: rgba(var(--vs-primary),1);
  border-style: solid;
  height: 24px;
  line-height: 24px;
  font-size: .85rem;
  cursor: pointer !important;
  background: white;
  z-index: 1;
  font-family: Arial;
}

.ui-selector .select-item.isSelected {
  background: rgba(var(--vs-primary),1) !important;
  color: #fff;
}

.ui-selector .select-item:hover:not(.isSelected) {
  background: rgba(var(--vs-primary),.05) !important;
}

.ui-selector .select-item:not(:last-child) {
  border-width: 1px 0px 1px 1px;
}

.ui-selector .select-item:last-child {
  border-width: 1px 1px 1px 1px;
  border-radius: 0px 6px 6px 0px;
  padding-right: 16px;
}

.ui-selector .select-item:first-child {
  border-radius: 6px 0px 0px 6px;
  padding-left: 16px;
}
</style>