<template>
  <div class="map-layout-container border-top border-theme bg-theme-main">
    
    <header class="map-header bg-theme-card border-bottom border-theme px-4 py-3 z-3">
      <div class="container-fluid p-0">
        <div class="row align-items-center g-3">
          <div class="col-xl-3 col-lg-4">
            <div class="brand-box d-flex align-items-center gap-3">
              <div class="icon-pulse bg-success text-white shadow-sm">
                <i class="bi bi-geo-alt-fill"></i>
              </div>
              <div class="overflow-hidden">
                <h5 class="fw-bold mb-0 text-theme-main text-truncate">Bản đồ Y tế</h5>
                <div class="status-indicator d-flex align-items-center gap-1">
                  <span class="dot" :class="{ 'pulse': loading }"></span>
                  <span class="extra-small text-theme-muted">{{ statusText }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="col-xl-9 col-lg-8">
            <div class="d-flex flex-wrap justify-content-lg-end align-items-center gap-3">
              <div class="search-engine-box bg-theme-light rounded-pill border border-theme shadow-sm">
                <i class="bi bi-search text-theme-muted ms-3"></i>
                <input v-model="addressQuery" @keyup.enter="handleSearch" type="text" 
                       class="form-control border-0 bg-transparent shadow-none text-theme-main" placeholder="Tìm thành phố hoặc tên bệnh viện...">
                <button class="btn btn-success rounded-pill px-4 fw-bold m-1" @click="handleSearch" :disabled="loading">
                  TÌM KIẾM
                </button>
              </div>

              <button class="btn btn-primary rounded-pill px-4 fw-bold shadow-sm d-flex align-items-center gap-2" @click="getUserLocation">
                <i class="bi bi-crosshair2"></i> <span>GẦN TÔI</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    
    <main class="map-main-content">
      
      <section class="map-view-area position-relative">
        <div id="map" class="absolute-fill"></div>

        
        <transition name="fade">
          <div v-if="showSearchHere" class="search-here-floating">
            <button class="btn btn-warning shadow-lg fw-bold rounded-pill px-4 py-2 border-0" @click="searchCurrentArea">
              <i class="bi bi-arrow-repeat me-2"></i> TÌM TẠI KHU VỰC NÀY
            </button>
          </div>
        </transition>

        
        <div class="map-custom-controls d-none d-md-flex">
          <button @click="map && map.setZoom(map.getZoom() + 1)" class="bg-theme-card border border-theme text-theme-main shadow-sm"><i class="bi bi-plus-lg"></i></button>
          <button @click="map && map.setZoom(map.getZoom() - 1)" class="bg-theme-card border border-theme text-theme-main shadow-sm"><i class="bi bi-dash-lg"></i></button>
        </div>
      </section>

      
      <aside class="map-sidebar bg-theme-card border-start border-theme shadow-lg d-flex flex-column">
        <div class="sidebar-header p-4 border-bottom border-theme">
          <div class="d-flex justify-content-between align-items-center mb-1">
            <h6 class="fw-bold text-theme-main mb-0">
              <i class="bi bi-building-check me-2 text-success"></i> 
              {{ loading ? 'Đang quét...' : 'Cơ sở y tế' }}
            </h6>
            <div v-if="loading" class="spinner-border spinner-border-sm text-success"></div>
          </div>
          <div v-if="!loading" class="extra-small text-theme-muted">Phạm vi 50km • {{ facilities.length }} kết quả</div>
        </div>

        <div class="sidebar-body flex-grow-1 overflow-auto custom-scrollbar bg-theme-light p-2">
          
          <div v-if="loading && facilities.length === 0" class="p-2">
            <div v-for="i in 5" :key="i" class="skeleton-card-modern mb-3"></div>
          </div>

          
          <div v-else-if="facilities.length === 0 && !loading" class="empty-state-box p-5 text-center bg-theme-card rounded-4 m-2">
            <i class="bi bi-geo-alt-fill opacity-25 text-theme-muted d-block mb-3" style="font-size: 4rem;"></i>
            <h6 class="fw-bold text-theme-main">Không có dữ liệu</h6>
            <p class="text-theme-muted extra-small mb-0">Không tìm thấy cơ sở y tế nào trong bán kính 50km tại khu vực này.</p>
          </div>

          
          <transition-group name="staggered-list">
            <div v-for="f in facilities" :key="f.id" 
                 class="facility-card-elite rounded-4 mb-3 bg-theme-card border border-theme shadow-sm"
                 :class="{ 'is-selected': selectedFacilityId === f.id }"
                 @click="focusFacility(f)">
              <div class="p-3">
                <div class="d-flex gap-3">
                  <div class="elite-icon-box" :class="getTypeColor(f.type)">
                    <i :class="getIcon(f.type)"></i>
                  </div>
                  <div class="flex-grow-1 overflow-hidden">
                    <div class="d-flex justify-content-between align-items-start mb-1">
                      <span class="elite-type-tag">{{ translateType(f.type) }}</span>
                      <span class="elite-distance text-success fw-bold">{{ formatDistance(f.distance) }}</span>
                    </div>
                    <h6 class="fw-bold text-theme-main mb-1 text-truncate-2 lh-base">{{ f.name }}</h6>
                    <p class="extra-small text-theme-muted mb-3 text-truncate-2">
                      <i class="bi bi-geo-alt me-1"></i>{{ f.address }}
                    </p>
                    
                    <div class="d-flex align-items-center justify-content-between pt-2 border-top border-theme-light">
                      <a v-if="f.phone" :href="'tel:' + f.phone" class="btn-circle-elite bg-primary bg-opacity-10 text-primary" title="Gọi ngay" @click.stop>
                        <i class="bi bi-telephone-fill"></i>
                      </a>
                      <div v-else></div>
                      
                      <a :href="'https://www.google.com/maps/dir/?api=1&destination=' + f.latitude + ',' + f.longitude" 
                         target="_blank" class="btn btn-sm btn-success rounded-pill px-3 fw-bold d-flex align-items-center gap-2" @click.stop>
                        <i class="bi bi-cursor-fill"></i> CHỈ ĐƯỜNG
                      </a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </transition-group>

          <div class="text-center py-4 opacity-25 small text-theme-muted" v-if="facilities.length > 0">
            -- Đã tải hết --
          </div>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { toast } from '@/utils/toast';

const loading = ref(false);
const facilities = ref([]);
const addressQuery = ref('');
const statusText = ref('Sẵn sàng');
const selectedFacilityId = ref(null);
const showSearchHere = ref(false);

let map = null;
let userMarker = null;
let markers = [];
let lastSearchPos = null;
let placesService = null;

onMounted(() => { loadGoogleMaps(); });

const loadGoogleMaps = () => {
  if (window.google && window.google.maps) {
    initMap();
    return;
  }
  const script = document.createElement('script');
  script.src = `https://maps.googleapis.com/maps/api/js?key=${import.meta.env.VITE_GOOGLE_MAPS_API_KEY}&v=weekly&libraries=places`;
  script.async = true;
  script.defer = true;
  script.onload = () => initMap();
  document.head.appendChild(script);
};

const initMap = async () => {
  const CAN_THO = { lat: 10.037, lng: 105.788 };
  
  const { Map } = await window.google.maps.importLibrary("maps");
  placesService = (await window.google.maps.importLibrary("places")).Place;

  map = new Map(document.getElementById('map'), {
    center: CAN_THO,
    zoom: 14,
    disableDefaultUI: true,
    zoomControl: false,
  });

  map.addListener('dragend', () => {
    if (!lastSearchPos) return;
    showSearchHere.value = true;
  });
  
  getUserLocation();
};

const getUserLocation = () => {
  if (navigator.geolocation) {
    statusText.value = 'Đang tìm GPS...';
    navigator.geolocation.getCurrentPosition((pos) => {
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;
      updateUserMarker(lat, lng);
      fetchRealtimeFacilities(lat, lng);
    }, () => { 
      statusText.value = 'GPS lỗi';
      fetchRealtimeFacilities(10.037, 105.788);
    });
  }
};

const updateUserMarker = (lat, lng) => {
  if (!map) return;
  const pos = { lat, lng };
  if (userMarker) {
    userMarker.setPosition(pos);
  } else {
    userMarker = new window.google.maps.Marker({
      position: pos,
      map: map,
      icon: {
        path: window.google.maps.SymbolPath.CIRCLE,
        scale: 8,
        fillColor: '#4285F4',
        fillOpacity: 1,
        strokeColor: 'white',
        strokeWeight: 2,
      },
      title: 'Vị trí của bạn',
      zIndex: 1000
    });
  }
  map.panTo(pos);
};

const handleSearch = async () => {
  if (!addressQuery.value) return;
  loading.value = true;
  statusText.value = 'Đang tìm kiếm...';
  
  try {
    const request = {
      textQuery: addressQuery.value + ', Việt Nam',
      fields: ["displayName", "location"],
      maxResultCount: 1
    };

    const { places } = await placesService.searchByText(request);

    if (places && places.length > 0) {
      const location = places[0].location;
      map.setCenter(location);
      map.setZoom(15);
      fetchRealtimeFacilities(location.lat(), location.lng());
    } else {
      toast.warning("Không tìm thấy địa điểm!");
      loading.value = false;
      statusText.value = 'Hoàn tất';
    }
  } catch (error) {
    console.error("Lỗi tìm kiếm text:", error);
    toast.error("Lỗi khi tìm kiếm địa điểm.");
    loading.value = false;
    statusText.value = 'Lỗi API';
  }
};

const searchCurrentArea = () => {
  const center = map.getCenter();
  fetchRealtimeFacilities(center.lat(), center.lng());
};

const fetchRealtimeFacilities = async (lat, lng, radius = 5000) => {
  loading.value = true;
  facilities.value = [];
  addMarkers();
  statusText.value = 'Đang quét Google Maps...';
  lastSearchPos = { lat, lng };
  showSearchHere.value = false;
  
  try {
    const request = {
      fields: ["displayName", "location", "formattedAddress", "types"],
      locationRestriction: {
        center: new window.google.maps.LatLng(lat, lng),
        radius: radius,
      },
      includedPrimaryTypes: ["hospital", "pharmacy", "medical_clinic", "dentist", "doctor"],
      maxResultCount: 20,
    };

    const { places } = await placesService.searchNearby(request);

    if (places && places.length > 0) {
      facilities.value = places.map(p => {
        let type = 'CLINIC';
        const types = p.types || [];
        if (types.includes('hospital')) type = 'HOSPITAL';
        else if (types.includes('pharmacy')) type = 'PHARMACY';

        const pLat = p.location.lat();
        const pLng = p.location.lng();

        return {
          id: p.id,
          name: p.displayName || 'Cơ sở y tế',
          address: p.formattedAddress || 'Không rõ địa chỉ',
          latitude: pLat,
          longitude: pLng,
          type: type,
          rating: 0,
          phone: '',
          distance: calculateDistance(lat, lng, pLat, pLng)
        };
      }).sort((a, b) => a.distance - b.distance);
      
      addMarkers();
    }
    statusText.value = 'Hoàn tất';
  } catch (error) {
    console.error("Lỗi tìm kiếm Places API (New):", error);
    toast.error("Không thể lấy dữ liệu bản đồ. Vui lòng thử lại sau.");
    statusText.value = 'Lỗi API';
  } finally {
    loading.value = false;
  }
};

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
          Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
          Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
};

let infoWindow = null;
const addMarkers = () => {
  if (!map) return;
  markers.forEach(m => m.setMap(null));
  markers = [];
  if (!infoWindow) infoWindow = new window.google.maps.InfoWindow();

  facilities.value.forEach(f => {
    const marker = new window.google.maps.Marker({
      position: { lat: f.latitude, lng: f.longitude },
      map: map,
      title: f.name
    });
    
    marker.addListener('click', () => {
      infoWindow.setContent(`
        <div class="elite-popup">
          <h6 class="popup-title">${f.name}</h6>
          <p class="popup-address">${f.address}</p>
          <div class="popup-info">Cách bạn: <b>${formatDistance(f.distance)}</b></div>
          <a href="https://www.google.com/maps/dir/?api=1&destination=${f.latitude},${f.longitude}" target="_blank" class="elite-popup-btn">CHỈ ĐƯỜNG</a>
        </div>
      `);
      infoWindow.open(map, marker);
    });
    markers.push(marker);
  });
};

const focusFacility = (f) => {
  selectedFacilityId.value = f.id;
  map.panTo({ lat: f.latitude, lng: f.longitude });
  map.setZoom(17);
  
  const m = markers.find(mk => {
    const pos = mk.getPosition();
    return Math.abs(pos.lat() - f.latitude) < 0.0001 && Math.abs(pos.lng() - f.longitude) < 0.0001;
  });
  
  if (m) window.google.maps.event.trigger(m, 'click');
};

const formatDistance = (d) => (!d ? '0 m' : d < 1 ? (d * 1000).toFixed(0) + ' m' : d.toFixed(1) + ' km');
const translateType = (t) => {
  if (t === 'HOSPITAL') return 'Bệnh viện';
  if (t === 'PHARMACY') return 'Nhà thuốc';
  if (t === 'VACCINATION_CENTER') return 'Trạm y tế';
  return 'Phòng khám';
};
const getTypeColor = (t) => (t === 'HOSPITAL' ? 'bg-danger' : t === 'PHARMACY' ? 'bg-success' : t === 'VACCINATION_CENTER' ? 'bg-warning' : 'bg-info');
const getIcon = (t) => (t === 'HOSPITAL' ? 'bi-hospital' : t === 'PHARMACY' ? 'bi-capsule-pill' : t === 'VACCINATION_CENTER' ? 'bi-shield-plus' : 'bi-heart-pulse');
</script>

<style scoped>

.map-layout-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 65px);
  width: 100%;
  overflow: hidden;
}

.map-header {
  height: auto;
  flex-shrink: 0;
}

.map-main-content {
  display: flex;
  flex-grow: 1;
  overflow: hidden; 
  position: relative;
}


.map-view-area {
  flex-grow: 1;
  height: 100%;
}

.absolute-fill {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
}


.map-sidebar {
  width: 380px;
  height: 100%;
  flex-shrink: 0;
  z-index: 10;
}

@media (max-width: 992px) {
  .map-sidebar { width: 320px; }
}

@media (max-width: 767.98px) {
  .map-layout-container {
    height: auto;
    min-height: calc(100dvh - 56px);
  }

  .map-header {
    padding: 0.75rem !important;
  }

  .map-header h5 {
    font-size: 1.05rem;
  }

  .map-main-content {
    flex-direction: column;
    height: auto;
    overflow: visible;
  }

  .map-view-area {
    height: 52dvh;
    min-height: 300px;
    order: 1;
  }

  .map-sidebar {
    width: 100%;
    height: 48dvh;
    min-height: 280px;
    border-left: none !important;
    border-top: 1px solid var(--border-color);
    order: 2;
  }

  .sidebar-header {
    padding: 0.8rem !important;
  }

  .search-engine-box {
    max-width: 100%;
    width: 100%;
  }

  .search-engine-box input {
    min-width: 0;
  }

  .search-engine-box .btn {
    padding-left: 0.85rem !important;
    padding-right: 0.85rem !important;
    font-size: 0.78rem;
  }

  .map-header .btn.btn-primary {
    width: 100%;
    justify-content: center;
  }

  .search-here-floating .btn {
    font-size: 0.78rem;
    padding: 0.4rem 0.8rem !important;
  }
}

.sidebar-body {
  overscroll-behavior: contain; 
}


.icon-pulse { width: 45px; height: 45px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #ccc; }
.dot.pulse { background: #198754; animation: blink 1s infinite; }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.3; } 100% { opacity: 1; } }

.search-engine-box {
  display: flex;
  align-items: center;
  max-width: 550px;
  flex-grow: 1;
  padding: 2px;
}


.facility-card-elite {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 1px solid var(--border-color) !important;
}
.facility-card-elite:hover {
  transform: scale(1.02);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
  border-color: var(--primary-color) !important;
}
.facility-card-elite.is-selected {
  background-color: var(--hover-bg);
  border-left: 5px solid var(--primary-color) !important;
}

.elite-icon-box {
  width: 50px;
  height: 50px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.4rem;
  flex-shrink: 0;
}

.elite-type-tag { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 1px; color: var(--text-muted); opacity: 0.8; }
.btn-circle-elite { width: 38px; height: 38px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: none; transition: 0.2s; }
.btn-circle-elite:hover { transform: scale(1.1); }


.skeleton-card-modern { height: 140px; border-radius: 20px; background: linear-gradient(90deg, var(--bg-light) 25%, var(--hover-bg) 50%, var(--bg-light) 75%); background-size: 200% 100%; animation: skeleton-loading 1.5s infinite; }
@keyframes skeleton-loading { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }


.search-here-floating { position: absolute; top: 20px; left: 50%; transform: translateX(-50%); z-index: 1000; }
.map-custom-controls { position: absolute; bottom: 30px; right: 20px; z-index: 1000; display: flex; flex-direction: column; gap: 10px; }
.map-custom-controls button { width: 48px; height: 48px; border-radius: 15px; border: none; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; transition: 0.2s; }
.map-custom-controls button:hover { transform: translateY(-3px); }


:deep(.elite-popup) { padding: 5px; }
:deep(.popup-title) { font-weight: 800; color: var(--text-main); margin-bottom: 4px; font-size: 1.1rem; }
:deep(.popup-address) { font-size: 0.8rem; color: var(--text-muted); margin-bottom: 12px; line-height: 1.4; }
:deep(.popup-info) { font-size: 0.85rem; color: #198754; margin-bottom: 12px; }
:deep(.elite-popup-btn) { display: block; text-align: center; background: #198754; color: white; padding: 10px; border-radius: 10px; text-decoration: none; font-weight: bold; }

:deep(.user-dot) { width: 14px; height: 14px; background: #0d6efd; border: 2px solid white; border-radius: 50%; position: relative; z-index: 2; }
:deep(.user-pulse) { position: absolute; width: 40px; height: 40px; background: rgba(13, 110, 253, 0.2); border-radius: 50%; animation: pulse-blue 2s infinite; margin-top: -13px; margin-left: -13px; }
@keyframes pulse-blue { 0% { transform: scale(0.5); opacity: 1; } 100% { transform: scale(1.5); opacity: 0; } }
:deep(.marker-pin) { width: 36px; height: 36px; border-radius: 50% 50% 50% 0; transform: rotate(-45deg); display: flex; align-items: center; justify-content: center; border: 2px solid white; color: white; }
:deep(.marker-pin i) { transform: rotate(45deg); }

.staggered-list-enter-active { transition: all 0.4s ease-out; }
.staggered-list-enter-from { opacity: 0; transform: translateY(20px); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--border-color); border-radius: 10px; }
.text-truncate-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
