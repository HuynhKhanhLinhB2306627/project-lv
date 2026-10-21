<template>
  <div class="ai-chat-widget">
    
    <div class="chat-icon-trigger shadow-lg border border-white border-opacity-25" @click="toggleChat" :class="{ 'active': isOpen }">
      <i v-if="!isOpen" class="bi bi-robot fs-3"></i>
      <i v-else class="bi bi-x-lg fs-4"></i>
      <div v-if="!isOpen" class="notification-dot"></div>
    </div>

    
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window shadow-2xl rounded-4 overflow-hidden d-flex flex-column bg-theme-card border border-theme">
        <div class="chat-header p-3 bg-primary text-white d-flex justify-content-between align-items-center shadow-sm">
          <div class="d-flex align-items-center gap-2">
            <div class="header-bot-avatar" @click="goToAiConsulting" title="Mở trang chi tiết tư vấn">
              <i class="bi bi-robot"></i>
            </div>
            <div>
              <h6 class="mb-0 fw-bold">Chatbot Tư vấn Sức khỏe</h6>
              <small class="opacity-75">Trực tuyến</small>
            </div>
          </div>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-link text-white p-1" @click="toggleChat">
              <i class="bi bi-dash-lg"></i>
            </button>
          </div>
        </div>

        <div class="chat-body flex-grow-1 overflow-auto p-3 custom-scrollbar bg-theme-light bg-opacity-50" ref="chatBody">
          <div class="welcome-msg mb-4 text-center p-3 rounded-4 bg-theme-card border border-theme shadow-sm">
            <div class="welcome-bot-avatar mx-auto mb-2">
              <i class="bi bi-robot"></i>
            </div>
            <p class="small text-theme-muted mb-0">Xin chào! Tôi là trợ lý AI. Tôi có thể giúp bạn giải đáp thắc mắc về sức khỏe dựa trên hồ sơ của bạn.</p>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="msg-wrapper mb-3 d-flex"
            :class="msg.role === 'user' ? 'justify-content-end' : 'justify-content-start align-items-start gap-2'"
          >
            <div v-if="msg.role === 'bot'" class="bot-avatar">
              <i class="bi bi-robot"></i>
            </div>
            <div class="msg-bubble shadow-sm" :class="msg.role === 'user' ? 'mine bg-primary text-white' : 'other bg-theme-card text-theme-main border border-theme'">
              <div v-html="formatContent(msg.content)"></div>
            </div>
          </div>

          <div v-if="loading" class="msg-wrapper mb-3 d-flex justify-content-start align-items-start gap-2">
            <div class="bot-avatar">
              <i class="bi bi-robot"></i>
            </div>
            <div class="msg-bubble other bg-theme-card border border-theme shadow-sm py-2 px-3">
              <div class="typing-dots"><span></span><span></span><span></span></div>
            </div>
          </div>
        </div>

        <div class="chat-footer p-3 bg-theme-card border-top border-theme">
          <div class="input-group shadow-sm rounded-pill overflow-hidden border border-theme bg-theme-light">
            <input type="text" class="form-control border-0 bg-transparent py-2 px-3 text-theme-main shadow-none" 
                   v-model="inputMsg" @keyup.enter="sendMsg" placeholder="Nhập câu hỏi..." />
            <button class="btn btn-primary border-0 px-3" @click="sendMsg" :disabled="!inputMsg.trim() || loading">
              <i class="bi bi-send-fill"></i>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import axios from 'axios';

const isOpen = ref(false);
const loading = ref(false);
const inputMsg = ref('');
const messages = ref([]);
const chatBody = ref(null);

const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    nextTick(scrollToBottom);
  }
};

const formatContent = (text) => {
  if (!text) return '';
  return text.replace(/\n/g, '<br/>');
};

const scrollToBottom = () => {
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight;
  }
};

const goToAiConsulting = () => {
  window.location.href = '/ai-consulting';
};

const sendMsg = async () => {
  if (!inputMsg.value.trim() || loading.value) return;

  const text = inputMsg.value;
  messages.value.push({ role: 'user', content: text });
  inputMsg.value = '';
  loading.value = true;
  nextTick(scrollToBottom);

  try {
    const res = await axios.post('/ai-consulting/ask', {
      question: text,
      
      profileId: null 
    });
    messages.value.push({ role: 'bot', content: res.data.answer });
  } catch (err) {
    messages.value.push({ role: 'bot', content: 'Xin lỗi, hệ thống đang gặp sự cố. Bạn vui lòng thử lại sau nhé!' });
  } finally {
    loading.value = false;
    nextTick(scrollToBottom);
  }
};
</script>

<style scoped>
.ai-chat-widget { position: fixed; bottom: 25px; right: 25px; z-index: 1050; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }

.chat-icon-trigger {
  width: 60px; height: 60px; background: #0d6efd; color: white; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  transition: all 0.3s;
}
.chat-icon-trigger:hover { transform: scale(1.1); background: #0a58ca; }
.chat-icon-trigger.active { background: #6c757d; }

.notification-dot {
  position: absolute; top: 2px; right: 2px; width: 12px; height: 12px;
  background: #ffc107; border-radius: 50%; border: 2px solid white;
}

.chat-window {
  width: min(350px, calc(100vw - 40px));
  height: min(500px, calc(100dvh - 120px));
  position: absolute;
  bottom: 75px;
  right: 0;
}

.header-bot-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #ffffff;
  color: #0d6efd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.1rem;
  border: 2px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}

.bot-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #0d6efd;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 0.85rem;
  margin-top: 2px;
}

.welcome-bot-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #0d6efd;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.msg-bubble { max-width: 85%; padding: 10px 14px; border-radius: 18px; font-size: 0.9rem; line-height: 1.4; }
.msg-bubble.mine { border-bottom-right-radius: 4px; }
.msg-bubble.other { border-top-left-radius: 4px; }

.typing-dots span { display: inline-block; width: 6px; height: 6px; background: #adb5bd; border-radius: 50%; margin-right: 3px; animation: bounce 1.4s infinite ease-in-out; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s ease; }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(20px) scale(0.95); }

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }
.border-theme { border-color: var(--border-color) !important; }

@media (max-width: 576px) {
  .chat-window {
    width: calc(100vw - 24px);
    right: -8px;
    height: min(70dvh, calc(100dvh - 110px));
  }
}
</style>
