<template>
  <input
    :type="isDateTime ? 'datetime-local' : 'date'"
    :value="modelValue"
    @input="handleInput"
    :placeholder="placeholder"
  />
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  mode: {
    type: String,
    default: 'date' 
  },
  placeholder: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['update:modelValue']);

const isDateTime = computed(() => props.mode === 'datetime');

const handleInput = (event) => {
  emit('update:modelValue', event.target.value);
};
</script>
