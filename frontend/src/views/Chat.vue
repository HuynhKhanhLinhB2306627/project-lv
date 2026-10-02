<template>
  <div class="chat-page animate-fade-in">
    <div class="chat-container bg-theme-card p-0 overflow-hidden shadow-sm border border-theme">
      <div class="row g-0 h-100">
        
        
        <div class="col-md-4 col-lg-3 border-end border-theme bg-theme-light chat-sidebar d-flex flex-column">
          <div class="p-3 border-bottom border-theme bg-theme-card">
            <h5 class="fw-bold mb-3 text-theme-main">Trò chuyện</h5>
            <div class="input-group shadow-sm rounded-pill overflow-hidden">
              <span class="input-group-text bg-theme-light border-0"><i class="bi bi-search text-theme-muted"></i></span>
              <input type="text" class="form-control bg-theme-light border-0 text-theme-main shadow-none" 
                     v-model="searchQuery" @input="handleSearch"
                     placeholder="Tìm người để nhắn tin..." />
            </div>
          </div>
          
          <div class="room-list flex-grow-1 overflow-auto bg-theme-card custom-scrollbar">
            
            <div v-if="searchQuery.trim()" class="search-results border-bottom border-theme bg-theme-light bg-opacity-50">
              <div v-for="user in searchResults" :key="user.id" 
                   class="room-item p-3 border-bottom border-theme" 
                   @click="startChatWith(user)">
                <div class="d-flex align-items-center gap-3">
                  <img :src="getAvatarUrl(user.avatar, user.fullName)" class="room-avatar border border-theme" />
                  <div class="flex-grow-1 overflow-hidden">
                    <h6 class="mb-0 fw-bold text-truncate text-theme-main">{{ user.fullName }}</h6>
                    <p class="mb-0 small text-theme-muted text-truncate">{{ user.email }}</p>
                  </div>
                </div>
              </div>
              <div v-if="searchResults.length === 0" class="p-3 text-center text-theme-muted small">
                Không tìm thấy người dùng
              </div>
            </div>

            
            <div v-for="room in rooms" :key="room.id" 
                 class="room-item p-3 border-bottom border-theme position-relative" 
                 :class="{ 'active-room': activeRoom?.id === room.id }"
                 @click="selectRoom(room)">
              <div class="d-flex align-items-center gap-3">
                <img :src="getAvatarUrl(room.avatar, room.name)" class="room-avatar border border-theme" />
                <div class="flex-grow-1 overflow-hidden">
                  <h6 class="mb-0 fw-bold text-truncate text-theme-main">{{ room.name }}</h6>
                  <p class="mb-0 small text-theme-muted text-truncate">Nhấn để trò chuyện</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        
        <div class="col-md-8 col-lg-9 d-flex flex-column position-relative bg-theme-card">
          
          <div v-if="!isConnected" class="socket-status-alert text-center py-1 bg-warning text-dark small fw-medium">
            <i class="bi bi-exclamation-triangle-fill me-1"></i>
            Mất kết nối server, đang thử lại...
          </div>

          <div v-if="!activeRoom" class="welcome-screen d-flex flex-column align-items-center justify-content-center h-100 text-theme-muted">
            <i class="bi bi-chat-dots display-1 opacity-25 mb-3"></i>
            <h4 class="fw-bold">Chọn một đoạn chat để bắt đầu</h4>
          </div>

          <template v-else>
            
            <div class="chat-header p-3 border-bottom border-theme d-flex justify-content-between align-items-center shadow-sm z-1 bg-theme-card">
              <div class="d-flex align-items-center gap-3">
                <img :src="getAvatarUrl(activeRoom.avatar, activeRoom.name)" class="room-avatar border border-theme" />
                <div>
                  <h6 class="mb-0 fw-bold text-theme-main">{{ activeRoom.name }}</h6>
                  <small v-if="isBlockedByMe" class="text-danger fw-medium">Bạn đã chặn người này</small>
                  <small v-else-if="isBlockedByTarget" class="text-danger fw-medium">Xin lỗi, hiện tại tôi không muốn nhận tin nhắn này.</small>
                </div>
              </div>
              <div class="dropdown" v-if="activeRoom.targetUserId">
                <button class="btn btn-light bg-theme-light text-theme-main border-theme btn-sm rounded-circle shadow-sm" type="button" data-bs-toggle="dropdown">
                  <i class="bi bi-three-dots-vertical"></i>
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow-lg border border-theme bg-theme-card p-2">
                  <li>
                    <button class="dropdown-item text-danger rounded-2 fw-medium" @click="toggleBlock">
                      <i class="bi bi-slash-circle me-2"></i>
                      {{ isBlockedByMe ? 'Bỏ chặn người này' : 'Chặn người này' }}
                    </button>
                  </li>
                </ul>
              </div>
            </div>

            
            <div class="chat-history flex-grow-1 overflow-auto p-4 bg-theme-light bg-opacity-50 custom-scrollbar position-relative" 
                 ref="msgContainer" @scroll="handleScroll">
              <div v-for="(msg, index) in messages" :key="msg.id || msg.tempId || index" 
                   class="msg-wrapper mb-3 d-flex align-items-end gap-2" 
                   :class="isMyMessage(msg) ? 'justify-content-end' : 'justify-content-start'"
                   :style="{ opacity: msg.isPending ? 0.7 : 1 }">
                
                
                <img v-if="!isMyMessage(msg)" 
                     :src="getAvatarUrl(msg.avatar, msg.sender)" 
                     class="message-avatar shadow-sm border border-white"
                     loading="eager" />

                <div class="msg-bubble shadow-sm" :class="isMyMessage(msg) ? 'mine bg-primary text-white' : 'other bg-theme-card text-theme-main border border-theme'">
                  {{ msg.content }}
                  <div class="msg-time mt-1 opacity-75 small d-flex align-items-center gap-1" style="font-size: 0.65rem;">
                    {{ msg.time }}
                    <i v-if="msg.isPending" class="bi bi-clock-history"></i>
                    <i v-if="msg.isError" class="bi bi-exclamation-circle text-danger"></i>
                    <button
                      v-if="isMyMessage(msg) && msg.id && !msg.isPending"
                      type="button"
                      class="btn btn-link btn-sm p-0 ms-1 delete-msg-btn"
                      title="Xóa tin nhắn"
                      @click.stop="requestDeleteMessage(msg)"
                    >
                      <i class="bi bi-trash3"></i>
                    </button>
                  </div>
                </div>
              </div>

              
              <button v-if="showScrollDownBtn" 
                      class="btn btn-primary btn-sm rounded-circle position-absolute scroll-down-btn shadow-lg animate-bounce"
                      @click="scrollToBottom(true)">
                <i class="bi bi-arrow-down"></i>
              </button>
            </div>

            
            <div class="chat-input-area p-3 border-top border-theme bg-theme-card shadow-inner">
              <div v-if="isBlockedByMe || isBlockedByTarget" class="blocked-chat-notice rounded-3 px-3 py-2 d-flex align-items-center gap-2">
                <i class="bi bi-slash-circle text-danger"></i>
                <span class="small text-theme-main">
                  {{ isBlockedByMe ? 'Bạn đã chặn người dùng này.' : 'Xin lỗi, hiện tại tôi không muốn nhận tin nhắn này.' }}
                </span>
              </div>
              <div v-else class="d-flex align-items-end gap-2">
                <textarea class="form-control rounded-4 border-0 bg-theme-light py-2 px-3 flex-grow-1 text-theme-main shadow-none chat-textarea" 
                       v-model="newMessage" @keydown.enter.exact.prevent="sendMessage" 
                       @input="autoResizeTextarea"
                       :disabled="!isConnected"
                       rows="1"
                       :placeholder="!isConnected ? 'Đang kết nối lại...' : 'Nhập tin nhắn... (Enter để gửi, Shift+Enter xuống dòng)'"
                       ref="chatTextarea"></textarea>
                <button class="btn btn-primary rounded-circle send-btn shadow-sm flex-shrink-0" @click="sendMessage" 
                        :disabled="!newMessage.trim() || !isConnected">
                  <i class="bi bi-send-fill"></i>
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { toast } from '@/utils/toast';

const route = useRoute();
const rooms = ref([]);
const activeRoom = ref(null);
const messages = ref([]);
const newMessage = ref('');
const chatTextarea = ref(null);
const currentUser = ref({ email: '', fullName: '', avatar: '' });
const msgContainer = ref(null);
const showScrollDownBtn = ref(false);

const searchQuery = ref('');
const searchResults = ref([]);
const isConnected = ref(false);

const isBlockedByMe = computed(() => !!activeRoom.value?.blockedByMe);
const isBlockedByTarget = computed(() => !!activeRoom.value?.blockedByTarget);

let stompClient = null;
let currentSubscription = null;

const isMyMessage = (msg) => {
  if (!msg || !msg.senderEmail || !currentUser.value.email) return false;
  return msg.senderEmail.toLowerCase() === currentUser.value.email.toLowerCase();
};

const getAvatarUrl = (avatar, name) => {
  if (!avatar) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'User')}&background=random&size=128`;
  }
  
  if (avatar.startsWith('http')) return avatar;

  
  let baseUrl = axios.defaults.baseURL || '';
  if (!baseUrl.endsWith('/')) baseUrl += '/';
  
  return `${baseUrl}uploads/${avatar}`;
};

const fetchInitialData = async () => {
  try {
    const meRes = await axios.get('/api/chat/me');
    currentUser.value = {
      email: meRes.data.email,
      fullName: meRes.data.fullName,
      avatar: meRes.data.avatar || null
    };
    
    await fetchRooms();
    connectWebSocket();
    checkUrlForRoom();
  } catch (err) { console.error("Lỗi lấy thông tin ban đầu:", err); }
};

const fetchRooms = async () => {
  try {
    const roomsRes = await axios.get('/api/chat/rooms');
    
    rooms.value = (roomsRes.data || []).map(r => ({
        ...r,
        blockedByMe: !!r.blockedByMe,
        blockedByTarget: !!r.blockedByTarget
    }));
  } catch (err) { console.error("Lỗi lấy danh sách phòng:", err); }
};

const checkUrlForRoom = async () => {
  const roomId = route.query.roomId;
  if (roomId) {
    let room = rooms.value.find(r => r.id == roomId);
    if (!room) {
      try {
        const res = await axios.get(`/api/chat/rooms/${roomId}`);
        room = {
            ...res.data,
            blockedByMe: !!res.data.blockedByMe,
            blockedByTarget: !!res.data.blockedByTarget
        };
        rooms.value.unshift(room);
      } catch (e) { return; }
    }
    if (room) selectRoom(room);
  }
};

watch(() => route.query.roomId, (newRoomId) => {
  if (newRoomId && activeRoom.value?.id != newRoomId) {
    checkUrlForRoom();
  }
});

const selectRoom = async (room) => {
  if (currentSubscription) {
    currentSubscription.unsubscribe();
    currentSubscription = null;
  }

  
  const existingRoom = rooms.value.find(r => r.id === room.id);
  activeRoom.value = existingRoom || room;
  
  messages.value = [];
  searchQuery.value = '';
  searchResults.value = [];
  
  try {
    
    const roomRes = await axios.get(`/api/chat/rooms/${activeRoom.value.id}`);
    if (roomRes.data) {
        
        
        const rIndex = rooms.value.findIndex(r => r.id === activeRoom.value.id);
        const current = rIndex !== -1 ? rooms.value[rIndex] : activeRoom.value;
        const mergedRoom = {
          ...current,
          ...roomRes.data,
          avatar: roomRes.data.avatar || current?.avatar || null,
          blockedByMe: !!roomRes.data.blockedByMe,
          blockedByTarget: !!roomRes.data.blockedByTarget
        };

        if (rIndex !== -1) {
          rooms.value[rIndex] = mergedRoom;
        } else {
          rooms.value.unshift(mergedRoom);
        }
        activeRoom.value = { ...mergedRoom };
    }

    const history = await axios.get(`/api/chat/history/${activeRoom.value.id}`);
    messages.value = history.data;
    scrollToBottom(true);
    subscribeToRoom(activeRoom.value.id);
  } catch (err) { console.error("Lỗi chọn phòng:", err); }
};

const connectWebSocket = () => {
  if (stompClient) {
    try { stompClient.disconnect(); } catch (e) {}
  }

  let wsUrl = axios.defaults.baseURL || '';
  if (!wsUrl.endsWith('/')) wsUrl += '/';
  wsUrl += 'ws';
    
  const socket = new SockJS(wsUrl);
  stompClient = Stomp.over(socket);
  stompClient.debug = null;
  
  const token = localStorage.getItem('accessToken');
  const headers = token ? { 'Authorization': 'Bearer ' + token } : {};
  
  stompClient.connect(headers, (frame) => {
    isConnected.value = true;
    if (activeRoom.value) {
      subscribeToRoom(activeRoom.value.id);
    }
  }, (error) => {
    isConnected.value = false;
    setTimeout(connectWebSocket, 5000);
  });
};

const subscribeToRoom = (roomId) => {
  if (!stompClient || !isConnected.value) return;
  
  if (currentSubscription) {
    currentSubscription.unsubscribe();
  }

  currentSubscription = stompClient.subscribe(`/topic/messages/${roomId}`, (res) => {
    try {
      const msg = JSON.parse(res.body);
      if (msg) handleIncomingMessage(msg, roomId);
    } catch (e) { console.error("Lỗi parse tin nhắn nhận được:", e); }
  });
};

const handleIncomingMessage = (msg, roomId) => {
  
  if (msg.type === 'SYSTEM' && (msg.content === 'BLOCK' || msg.content === 'UNBLOCK')) {
    const isBlockAction = msg.content === 'BLOCK';
    const isByMe = msg.senderEmail && currentUser.value.email && 
                   msg.senderEmail.toLowerCase() === currentUser.value.email.toLowerCase();
    
    
    rooms.value = rooms.value.map(r => {
      if (r.id == roomId) {
        
        const updatedRoom = { ...r };
        if (isByMe) updatedRoom.blockedByMe = isBlockAction;
        else updatedRoom.blockedByTarget = isBlockAction;
        
        
        if (activeRoom.value && activeRoom.value.id == roomId) {
          activeRoom.value = updatedRoom;
        }
        return updatedRoom;
      }
      return r;
    });
    return;
  }
  
  
  if (msg.type === 'SYSTEM' && typeof msg.content === 'string' && msg.content.startsWith('DELETE:')) {
    const deletedId = Number(msg.content.replace('DELETE:', ''));
    if (!Number.isNaN(deletedId)) {
      const idx = messages.value.findIndex(m => m.id === deletedId);
      if (idx !== -1) {
        messages.value.splice(idx, 1);
      }
    }
    return;
  }
  
  
  if (msg.type === 'ERROR') {
    const isByMe = msg.senderEmail && currentUser.value.email && 
                   msg.senderEmail.toLowerCase() === currentUser.value.email.toLowerCase();
    
    if (isByMe && msg.tempId) {
      
      const pendingIdx = messages.value.findIndex(m => m.tempId === msg.tempId && m.isPending);
      if (pendingIdx !== -1) {
        messages.value.splice(pendingIdx, 1);
      }
      
      
      if (msg.content === 'BLOCKED_BY_USER') {
        toast.error("Không thể gửi tin nhắn. Người này đã chặn bạn.");
        
        if (activeRoom.value && activeRoom.value.id == roomId) {
          activeRoom.value = { ...activeRoom.value, blockedByTarget: true };
          const rIdx = rooms.value.findIndex(r => r.id == roomId);
          if (rIdx !== -1) rooms.value[rIdx] = { ...rooms.value[rIdx], blockedByTarget: true };
        }
      } else if (msg.content === 'YOU_BLOCKED_THEM') {
        toast.error("Không thể gửi tin nhắn. Bạn đã chặn người này.");
      } else if (msg.content === 'DELETE_NOT_ALLOWED') {
        toast.error("Bạn chỉ có thể xóa tin nhắn của chính mình.");
      } else if (msg.content === 'MESSAGE_NOT_FOUND') {
        toast.warning("Tin nhắn không còn tồn tại.");
      }
    }
    return;
  }

  const pendingIdx = messages.value.findIndex(m => m.tempId === msg.tempId && m.isPending);
  if (pendingIdx !== -1) {
    messages.value[pendingIdx] = { ...msg, isPending: false };
  } else {
    if (!messages.value.some(m => m.id === msg.id)) {
      messages.value.push(msg);
      scrollToBottom();
    }
  }
};

const autoResizeTextarea = () => {
  if (chatTextarea.value) {
    chatTextarea.value.style.height = 'auto';
    chatTextarea.value.style.height = Math.min(chatTextarea.value.scrollHeight, 150) + 'px';
  }
};

const sendMessage = () => {
  const content = newMessage.value.trim();
  
  if (!content || !stompClient || !isConnected.value || isBlockedByMe.value || isBlockedByTarget.value) {
    return;
  }
  
  const tempId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
  const now = new Date();
  const timeStr = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');

  const optimisticMsg = {
    tempId: tempId,
    content: content,
    sender: currentUser.value.fullName,
    senderEmail: currentUser.value.email,
    avatar: currentUser.value.avatar,
    time: timeStr,
    type: 'CHAT',
    isPending: true
  };
  
  messages.value.push(optimisticMsg);
  scrollToBottom(true);
  newMessage.value = '';
  
  
  if (chatTextarea.value) {
    chatTextarea.value.style.height = 'auto';
  }

  const msgObj = {
    content: content,
    type: 'CHAT',
    tempId: tempId
  };
  
  try {
    stompClient.send(`/app/chat.sendMessage/${activeRoom.value.id}`, {}, JSON.stringify(msgObj));
  } catch (err) {
    console.error("Lỗi khi gọi stompClient.send:", err);
    optimisticMsg.isError = true;
    optimisticMsg.isPending = false;
  }
};

const requestDeleteMessage = async (msg) => {
  if (!msg?.id || !activeRoom.value?.id || !stompClient || !isConnected.value) return;

  const ok = await toast.confirm("Xóa tin nhắn", "Bạn có chắc muốn xóa tin nhắn này?");
  if (!ok) return;

  try {
    stompClient.send(
      `/app/chat.deleteMessage/${activeRoom.value.id}`,
      {},
      JSON.stringify({ id: msg.id, type: 'SYSTEM' })
    );
  } catch (err) {
    console.error("Lỗi khi gửi yêu cầu xóa tin nhắn:", err);
    toast.error("Không thể xóa tin nhắn. Vui lòng thử lại.");
  }
};

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    return;
  }
  try {
    const res = await axios.get(`/api/users/search?keyword=${encodeURIComponent(searchQuery.value)}`);
    searchResults.value = res.data;
  } catch (err) { console.error(err); }
};

const startChatWith = async (user) => {
  try {
    const res = await axios.get(`/api/chat/room/with/${user.id}`);
    const room = {
      ...res.data,
      
      avatar: res.data?.avatar || user.avatar || null
    };
    if (!rooms.value.find(r => r.id === room.id)) {
      rooms.value.unshift(room);
    }
    selectRoom(room);
  } catch (err) { console.error(err); }
};

const toggleBlock = async () => {
  if (!activeRoom.value || !activeRoom.value.id || !activeRoom.value.targetUserId) return;
  
  const targetId = activeRoom.value.targetUserId;
  const roomId = activeRoom.value.id;
  const currentlyBlocked = isBlockedByMe.value;
  
  const confirmMsg = currentlyBlocked 
    ? "Bạn muốn bỏ chặn người này?" 
    : "Bạn muốn chặn người này? Bạn sẽ không thể gửi tin nhắn cho họ.";
  const confirmTitle = currentlyBlocked ? "Xác nhận gỡ chặn" : "Xác nhận chặn";
    
  if (await toast.confirm(confirmTitle, confirmMsg)) {
    try {
      const url = currentlyBlocked ? `/api/chat/unblock/${targetId}` : `/api/chat/block/${targetId}`;
      await axios.post(url);
      
      const newStatus = !currentlyBlocked;
      
      
      const rIndex = rooms.value.findIndex(r => r.id == roomId);
      if (rIndex !== -1) {
        rooms.value[rIndex] = { ...rooms.value[rIndex], blockedByMe: newStatus };
      }
      
      activeRoom.value = { ...activeRoom.value, blockedByMe: newStatus };
      
    } catch (err) { 
      console.error("Lỗi khi thực hiện chặn/gỡ chặn:", err);
      toast.error("Không thể thực hiện thao tác này. Vui lòng thử lại.");
    }
  }
};

const handleScroll = () => {
  if (!msgContainer.value) return;
  const { scrollTop, scrollHeight, clientHeight } = msgContainer.value;
  showScrollDownBtn.value = (scrollHeight - scrollTop - clientHeight) > 200;
};

const scrollToBottom = (force = false) => {
  nextTick(() => {
    if (msgContainer.value) {
      const { scrollTop, scrollHeight, clientHeight } = msgContainer.value;
      const isNearBottom = (scrollHeight - scrollTop - clientHeight) < 150;
      if (force || isNearBottom) {
        msgContainer.value.scrollTo({
          top: msgContainer.value.scrollHeight,
          behavior: force ? 'smooth' : 'auto'
        });
      }
    }
  });
};

onMounted(fetchInitialData);
onUnmounted(() => { 
  if (currentSubscription) currentSubscription.unsubscribe();
  if (stompClient) {
    try { stompClient.disconnect(); } catch (e) {}
  }
});
</script>

<style scoped>
.chat-page { height: calc(100vh - 130px); display: flex; flex-direction: column; overflow: hidden; }
.chat-container { flex: 1; display: flex; flex-direction: column; border-radius: 12px; overflow: hidden; min-height: 0; height: 100%; }
.row.g-0.h-100 { height: 100% !important; flex-wrap: nowrap; }
.chat-sidebar, .col-md-8 { height: 100%; display: flex; flex-direction: column; overflow: hidden; }
.room-list { flex: 1; overflow-y: auto; }
.chat-history { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; }
.chat-input-area { flex-shrink: 0; background: var(--bg-card); z-index: 5; }
.blocked-chat-notice {
  background: var(--bg-light);
  border: 1px solid var(--border-color);
}
.room-item { cursor: pointer; transition: 0.2s; }
.room-item:hover { background-color: var(--hover-bg) !important; }
.active-room { background-color: rgba(13, 110, 253, 0.1) !important; }
.room-avatar { width: 45px; height: 45px; border-radius: 50%; flex-shrink: 0; }
.msg-bubble { 
  max-width: 75%; 
  padding: 0.75rem 1rem; 
  border-radius: 1rem; 
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
}
.msg-bubble.mine { border-bottom-right-radius: 0.25rem; }
.msg-bubble.other { border-bottom-left-radius: 0.25rem; }
.send-btn { width: 45px; height: 45px; display: flex; align-items: center; justify-content: center; }
.delete-msg-btn {
  color: inherit !important;
  opacity: 0.75;
  text-decoration: none !important;
  line-height: 1;
}
.delete-msg-btn:hover {
  opacity: 1;
  color: #dc3545 !important;
}
.chat-textarea {
  resize: none;
  min-height: 42px;
  max-height: 150px;
  overflow-y: auto;
  line-height: 1.5;
}
.chat-textarea:disabled {
  background-color: var(--bg-light) !important;
  color: var(--text-muted) !important;
  opacity: 1;
}
.z-1 { z-index: 1; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 10px; }
.shadow-inner { box-shadow: inset 0 5px 10px -5px rgba(0,0,0,0.05); }
.border-theme { border-color: var(--border-color) !important; }


.message-avatar { 
  width: 32px; 
  height: 32px; 
  border-radius: 50%; 
  object-fit: cover; 
  flex-shrink: 0; 
  margin-bottom: 5px; 
  transition: none !important; 
  animation: none !important;
  will-change: auto;
}

.scroll-down-btn { bottom: 20px; right: 25px; width: 35px; height: 35px; z-index: 10; display: flex; align-items: center; justify-content: center; transition: all 0.3s ease; }
.animate-bounce { animation: bounce 2s infinite; }
@keyframes bounce { 0%, 20%, 50%, 80%, 100% {transform: translateY(0);} 40% {transform: translateY(-5px);} 60% {transform: translateY(-3px);} }
.socket-status-alert { z-index: 100; position: sticky; top: 0; width: 100%; border-bottom: 1px solid rgba(0,0,0,0.05); }

@media (max-width: 767.98px) {
  .chat-page {
    height: auto;
    min-height: calc(100dvh - 120px);
    overflow: visible;
  }

  .chat-container {
    height: auto;
    min-height: calc(100dvh - 140px);
  }

  .row.g-0.h-100 {
    flex-wrap: wrap;
    height: auto !important;
  }

  .chat-sidebar {
    width: 100%;
    max-height: 230px;
    border-right: 0 !important;
    border-bottom: 1px solid var(--border-color);
  }

  .col-md-8 {
    width: 100%;
    min-height: 56dvh;
  }

  .chat-history {
    padding: 0.8rem;
  }

  .chat-input-area {
    padding: 0.6rem !important;
  }

  .msg-bubble {
    max-width: 88%;
    font-size: 0.88rem;
    padding: 0.6rem 0.8rem;
  }

  .room-avatar {
    width: 38px;
    height: 38px;
  }

  .send-btn {
    width: 40px;
    height: 40px;
  }
}
</style>
