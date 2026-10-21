<template>
  <div class="container-fluid py-3 h-100 ai-consulting-page animate-fade-in">
    <div class="row g-0 h-100 shadow-lg rounded-4 overflow-hidden bg-theme-card border border-theme">
      
      <div class="col-md-3 border-end border-theme bg-theme-light d-flex flex-column h-100">
        <div class="p-3 border-bottom border-theme d-flex justify-content-between align-items-center bg-theme-card">
          <h6 class="fw-bold mb-0 opacity-75 small text-uppercase tracking-wider text-theme-muted">Lịch sử tư vấn</h6>
          <div class="d-flex gap-1">
            <button class="btn btn-sm btn-outline-danger border-0 rounded-circle" @click="deleteAllSessions" title="Xóa tất cả">
              <i class="bi bi-trash-fill"></i>
            </button>
            <button class="btn btn-sm btn-outline-primary rounded-pill px-3 shadow-none border-0 hover-bg-theme-light fw-bold" @click="createNewSession">
              <i class="bi bi-plus-lg me-1"></i> Mới
            </button>
          </div>
        </div>
        
        <div class="flex-grow-1 overflow-auto p-2 custom-scrollbar">
          <div v-if="sessions.length === 0" class="text-center py-5 text-theme-muted small px-3 opacity-50">
            Chưa có đoạn hội thoại nào.
          </div>
          <div v-else class="list-group list-group-flush gap-1">
            <div v-for="s in sessions" :key="s.id" 
                 class="list-group-item list-group-item-action border border-transparent rounded-3 p-2 session-item px-3 d-flex align-items-center justify-content-between"
                 :class="{ 'active-session shadow-sm': currentSessionId === s.id }"
                 @click="selectSession(s.id)">
              <div class="flex-grow-1 overflow-hidden me-2">
                <div class="fw-medium small text-truncate text-theme-main">{{ s.title }}</div>
                <small class="text-theme-muted extra-small">{{ formatDateShort(s.date) }}</small>
              </div>
              <button class="btn btn-sm text-danger session-delete-btn p-1 opacity-50 hover-opacity-100" @click.stop="deleteSession(s.id)">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      
      <div class="col-md-9 d-flex flex-column h-100 shadow-inner bg-theme-card">
        
        <div class="p-3 border-bottom border-theme d-flex align-items-center justify-content-between z-1 bg-theme-card">
          <div class="d-flex align-items-center gap-3">
            <div class="bot-icon bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 40px; height: 40px;">
              <i class="bi bi-robot fs-5"></i>
            </div>
            <div>
              <h6 class="mb-0 fw-bold text-theme-main">Trợ lý Sức khỏe AI</h6>
              <div class="d-flex align-items-center gap-2">
                <span class="badge bg-success bg-opacity-10 text-success rounded-pill border border-success-subtle" style="font-size: 0.55rem;">Gemini 2.5 Flash</span>
                <small class="text-theme-muted extra-small">Tư vấn thông minh</small>
              </div>
            </div>
          </div>
          <div class="d-flex gap-2">
            <div class="input-group input-group-sm" style="width: 220px;">
              <span class="input-group-text bg-theme-light border-0 text-theme-muted"><i class="bi bi-person-circle"></i></span>
              <select class="form-select border-0 bg-theme-light text-theme-main fw-bold small shadow-none" v-model="selectedProfileId">
                <option v-for="p in profiles" :key="p.id" :value="p.id" class="bg-theme-card">{{ p.fullName }}</option>
              </select>
            </div>
          </div>
        </div>

        
        <div class="flex-grow-1 overflow-auto p-4 bg-chat-pattern custom-scrollbar d-flex flex-column" ref="chatBody">
          
          <div v-if="chatHistory.length === 0" class="m-auto text-center py-5 animate-fade-in" style="max-width: 450px;">
            <div class="welcome-icon mb-4">
              <i class="bi bi-chat-heart text-primary opacity-25" style="font-size: 4rem;"></i>
            </div>
            <h5 class="fw-bold text-theme-main">Bắt đầu trò chuyện cùng AI</h5>
            <p class="text-theme-muted small">Hãy đặt câu hỏi về sức khỏe, chỉ số cơ thể hoặc tìm kiếm lời khuyên từ hồ sơ y tế của bạn.</p>
            <div class="d-flex flex-wrap justify-content-center gap-2 mt-4">
              <button class="btn btn-outline-theme btn-sm rounded-pill px-3 border border-theme text-theme-main" @click="quickAsk('Hồ sơ của tôi có gì cần lưu ý?')">Phân tích hồ sơ</button>
              <button class="btn btn-outline-theme btn-sm rounded-pill px-3 border border-theme text-theme-main" @click="quickAsk('Tôi nên ăn gì để tốt cho tim mạch?')">Dinh dưỡng tim mạch</button>
            </div>
          </div>

          <div v-for="(msg, index) in chatHistory" :key="index" 
               :class="msg.role === 'user' ? 'msg-user-wrapper mb-4 align-self-end' : 'msg-bot-wrapper mb-4 align-self-start w-100'">
            <div class="d-flex gap-3" :class="{'flex-row-reverse': msg.role === 'user'}">
              <div class="msg-avatar shadow-sm flex-shrink-0" :class="msg.role === 'user' ? 'bg-primary text-white' : 'bg-theme-card border border-theme text-primary'">
                <i :class="msg.role === 'user' ? 'bi bi-person-fill' : 'bi bi-robot'"></i>
              </div>
              <div class="msg-bubble-container" :class="{'w-100': msg.role === 'bot'}">
                <div class="msg-bubble shadow-sm" :class="msg.role === 'user' ? 'bg-primary text-white' : 'bg-theme-card border border-theme'">
                  <div class="msg-text text-theme-main" v-html="formatContent(msg.content)"></div>
                  
                  
                  <div v-if="msg.role === 'bot' && (msg.recommendations?.length || msg.warningSigns?.length)" class="mt-3 pt-3 border-top border-theme">
                    <div v-if="msg.warningSigns?.length" class="mb-3">
                      <div class="extra-small fw-bold text-danger mb-2 text-uppercase tracking-tighter"><i class="bi bi-exclamation-triangle-fill"></i> Cảnh báo quan trọng</div>
                      <div class="d-flex flex-wrap gap-2">
                        <span v-for="sign in msg.warningSigns" :key="sign" class="badge bg-danger-subtle text-danger border border-danger-subtle fw-normal py-1 px-2">
                          {{ sign }}
                        </span>
                      </div>
                    </div>
                    <div v-if="msg.recommendations?.length">
                      <div class="extra-small fw-bold text-success mb-2 text-uppercase tracking-tighter"><i class="bi bi-lightbulb-fill"></i> Gợi ý hành động</div>
                      <ul class="list-unstyled small mb-0">
                        <li v-for="rec in msg.recommendations" :key="rec" class="mb-1 d-flex gap-2 text-theme-main">
                          <i class="bi bi-check2 text-success"></i> <span>{{ rec }}</span>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
                <div class="extra-small text-theme-muted mt-1 px-2" :class="{'text-end': msg.role === 'user'}">{{ msg.time || 'Vừa xong' }}</div>
              </div>
            </div>
          </div>

          <div v-if="isTyping" class="msg-bot-wrapper mb-4 align-self-start">
            <div class="d-flex gap-3">
              <div class="msg-avatar shadow-sm bg-theme-card border border-theme text-primary"><i class="bi bi-robot"></i></div>
              <div class="msg-bubble bg-theme-card shadow-sm border border-theme py-2 px-3">
                <div class="typing-dots"><span></span><span></span><span></span></div>
              </div>
            </div>
          </div>
        </div>

        
        <div class="p-3 border-top border-theme chat-input-section bg-theme-card">
          <div class="mx-auto" style="max-width: 800px;">
            <div class="input-container d-flex align-items-end gap-2 p-2 bg-theme-light rounded-4 border border-theme shadow-sm focus-within-primary">
              <textarea class="form-control border-0 bg-transparent text-theme-main shadow-none custom-scrollbar py-2" 
                        v-model="userQuestion" @keydown.enter.prevent="askAi"
                        rows="1" @input="autoResize" ref="textareaRef"
                        placeholder="Hỏi bất cứ điều gì về sức khỏe..."></textarea>
              <button class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" 
                      style="width: 36px; height: 36px;" @click="askAi" :disabled="!userQuestion.trim() || isTyping">
                <i class="bi bi-arrow-up-short fs-4" v-if="!isTyping"></i>
                <span v-else class="spinner-border spinner-border-sm"></span>
              </button>
            </div>
            <div class="text-center mt-2">
              <small class="text-theme-muted" style="font-size: 0.65rem;">
                AI không thay thế bác sĩ chuyên khoa. Nhấn <strong>Enter</strong> để gửi.
              </small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import axios from 'axios';
import { toast } from '@/utils/toast';

const userQuestion = ref('');
const isTyping = ref(false);
const chatHistory = ref([]);
const chatBody = ref(null);
const textareaRef = ref(null);
const sessions = ref([]);
const currentSessionId = ref(null);
const profiles = ref([]);
const selectedProfileId = ref(null);

const isDarkMode = computed(() => document.documentElement.getAttribute('data-bs-theme') === 'dark');

const autoResize = () => {
  const el = textareaRef.value;
  el.style.height = 'auto';
  el.style.height = (el.scrollHeight > 150 ? 150 : el.scrollHeight) + 'px';
};

const formatContent = (text) => {
  if (!text) return '';
  return text.replace(/\n/g, '<br/>');
};

const formatDateShort = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) + ' ' + d.getHours() + ':' + String(d.getMinutes()).padStart(2, '0');
};

const scrollToBottom = () => {
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight;
  }
};

const fetchInitialData = async () => {
  try {
    const [profRes, sessionRes] = await Promise.all([
      axios.get('/api/user-profile/list'),
      axios.get('/api/community/chat-sessions')
    ]);
    profiles.value = profRes.data.myProfiles || [];
    if (profiles.value.length > 0) selectedProfileId.value = profiles.value[0].id;
    sessions.value = sessionRes.data;
  } catch (err) { console.error(err); }
};

const createNewSession = () => {
  currentSessionId.value = null;
  chatHistory.value = [];
};

const selectSession = async (id) => {
  currentSessionId.value = id;
  try {
    const res = await axios.get(`/api/community/chat-sessions/${id}`);
    chatHistory.value = res.data.messages.map(m => ({
      role: m.role.toLowerCase(),
      content: m.content,
      time: formatDateShort(m.date)
    }));
    nextTick(scrollToBottom);
  } catch (err) { console.error(err); }
};

const deleteSession = async (id) => {
  if (!await toast.confirm("Xác nhận xóa", "Xóa cuộc hội thoại này?")) return;
  try {
    await axios.delete(`/api/community/chat-sessions/${id}`);
    sessions.value = sessions.value.filter(s => s.id !== id);
    if (currentSessionId.value === id) createNewSession();
  } catch (err) { console.error(err); }
};

const deleteAllSessions = async () => {
  if (sessions.value.length === 0) return;
  if (!await toast.confirm("Xác nhận xóa", "Xóa toàn bộ lịch sử tư vấn? Hành động này không thể hoàn tác.")) return;
  try {
    await axios.delete('/api/community/chat-sessions');
    sessions.value = [];
    createNewSession();
  } catch (err) { console.error(err); }
};

const askAi = async () => {
  if (!userQuestion.value.trim() || isTyping.value) return;

  const question = userQuestion.value;
  chatHistory.value.push({ role: 'user', content: question, time: 'Vừa xong' });
  userQuestion.value = '';
  if (textareaRef.value) textareaRef.value.style.height = 'auto';
  isTyping.value = true;
  nextTick(scrollToBottom);

  try {
    const res = await axios.post('/ai-consulting/ask', {
      question: question,
      profileId: selectedProfileId.value,
      sessionId: currentSessionId.value
    });

    const data = res.data;
    if (data.sessionId && !currentSessionId.value) {
      currentSessionId.value = data.sessionId;
      const sessionRes = await axios.get('/api/community/chat-sessions');
      sessions.value = sessionRes.data;
    }

    chatHistory.value.push({ 
      role: 'bot', 
      content: data.answer,
      recommendations: data.recommendations,
      warningSigns: data.warningSigns,
      time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
    });
  } catch (err) {
    const errorMessage = err.response?.data?.answer 
      || err.response?.data?.message 
      || "Hệ thống AI tạm thời bận. Vui lòng thử lại sau! 🙏";
    
    chatHistory.value.push({ 
      role: 'bot', 
      content: errorMessage,
      time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
    });
  } finally {
    isTyping.value = false;
    nextTick(scrollToBottom);
  }
};

const quickAsk = (text) => {
  userQuestion.value = text;
  askAi();
};

onMounted(fetchInitialData);
</script>

<style scoped>
.ai-consulting-page { height: calc(100vh - 100px); min-height: 600px; }
.bg-chat-pattern { background-color: var(--bg-card); background-image: radial-gradient(var(--border-color) 0.5px, transparent 0.5px); background-size: 20px 20px; }
.shadow-inner { box-shadow: inset 5px 0 10px -5px rgba(0,0,0,0.02); }


.session-item { border-radius: 8px !important; margin-bottom: 2px; cursor: pointer; transition: 0.2s; border: 1px solid transparent !important; }
.session-item:hover { background-color: var(--hover-bg) !important; }
.session-item:hover .session-delete-btn { opacity: 0.6; }
.active-session { background-color: var(--bg-card) !important; border-color: var(--border-color) !important; color: var(--primary-color) !important; }
.session-delete-btn:hover { opacity: 1 !important; color: #dc3545 !important; }


.msg-avatar { width: 34px; height: 34px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1rem; }
.msg-bubble-container { max-width: 85%; }
.msg-bubble { 
  padding: 12px 18px; 
  border-radius: 18px; 
  font-size: 0.95rem; 
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
}
.msg-user-wrapper .msg-bubble { border-top-right-radius: 4px; }
.msg-bot-wrapper .msg-bubble { border-top-left-radius: 4px; }


.chat-input-section { position: relative; }
.input-container { transition: border-color 0.2s, box-shadow 0.2s; }
.focus-within-primary:focus-within { border-color: var(--primary-color) !important; box-shadow: 0 0 0 3px rgba(13, 110, 253, 0.1) !important; }
textarea { max-height: 150px; overflow-y: auto; scrollbar-width: none; }


.typing-dots span { display: inline-block; width: 5px; height: 5px; background: #adb5bd; border-radius: 50%; margin-right: 3px; animation: bounce 1.4s infinite ease-in-out; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }
.z-1 { z-index: 1; }
.btn-outline-theme:hover { background-color: var(--hover-bg); }
.border-theme { border-color: var(--border-color) !important; }

@media (max-width: 767.98px) {
  .ai-consulting-page {
    height: auto !important;
    min-height: calc(100dvh - 120px);
    padding: 0.6rem !important;
  }

  .ai-consulting-page > .row {
    height: auto !important;
    min-height: calc(100dvh - 140px);
  }

  .ai-consulting-page .col-md-3 {
    max-height: 220px;
    border-right: 0 !important;
    border-bottom: 1px solid var(--border-color);
  }

  .ai-consulting-page .col-md-9 {
    min-height: 58vh;
  }

  .ai-consulting-page .p-4 {
    padding: 0.9rem !important;
  }

  .ai-consulting-page .input-group {
    width: 100% !important;
  }

  .msg-bubble-container {
    max-width: 92%;
  }

  .msg-bubble {
    padding: 10px 12px;
    font-size: 0.9rem;
    line-height: 1.45;
  }
}
</style>
