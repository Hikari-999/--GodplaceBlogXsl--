<script setup>
import { ref, onMounted, onUpdated, watch } from "vue";
import travelSong from "@/assets/Travelers' encore.mp3";

const songs = [
  {
    title: "烤棉花糖",
    url: travelSong,
  },
];

const currentSong = ref(songs[0]);
const currentSongIndex = ref(0);
const isPlaying = ref(false);
const volume = ref(0.5); // 默认音量 50%
const progress = ref(0); // 播放进度

const audioPlayer = ref(null);
const playerPosition = ref({ x: 20, y: 20 }); // 基于 left 和 bottom 定位
const isDragging = ref(false);
const dragOffset = ref({ x: 0, y: 0 }); // 拖拽偏移量（基于 left 和 bottom）

// 播放/暂停切换
const togglePlay = () => {
  if (audioPlayer.value.paused) {
    audioPlayer.value.play();
    isPlaying.value = true;
  } else {
    audioPlayer.value.pause();
    isPlaying.value = false;
  }
};

// 切换音量
const changeVolume = () => {
  audioPlayer.value.volume = volume.value;
};

// 播放下一首
const nextSong = () => {
  currentSongIndex.value = (currentSongIndex.value + 1) % songs.length;
  currentSong.value = songs[currentSongIndex.value];
  audioPlayer.value.load();
  audioPlayer.value.play();
};

// 更新播放进度
const updateProgress = () => {
  if (audioPlayer.value && !isNaN(audioPlayer.value.duration)) {
    progress.value = (audioPlayer.value.currentTime / audioPlayer.value.duration) * 100;
  }
};

// 改变播放进度
const changeProgress = () => {
  if (audioPlayer.value) {
    audioPlayer.value.currentTime = (progress.value * audioPlayer.value.duration) / 100;
  }
};

// 监听音频播放时间更新
onMounted(() => {
  if (audioPlayer.value) {
    audioPlayer.value.addEventListener('timeupdate', updateProgress);
  }
});

// 确保音频元素已更新
onUpdated(() => {
  if (audioPlayer.value) {
    audioPlayer.value.addEventListener('timeupdate', updateProgress);
  }
});

// 拖拽开始
const startDragging = (e) => {
  // 如果点击的是音量滑块或进度条，则不触发拖拽
  if (e.target.type === 'range') return;

  isDragging.value = true;
  // 记录点击时的偏移量
  dragOffset.value = {
    x: e.clientX - playerPosition.value.x,
    y: window.innerHeight - e.clientY - playerPosition.value.y, // 基于 bottom 计算
  };
  window.addEventListener("mousemove", onDragging);
  window.addEventListener("mouseup", stopDragging);
};

// 拖拽中
const onDragging = (e) => {
  if (isDragging.value) {
    const newLeft = e.clientX - dragOffset.value.x;
    const newBottom = window.innerHeight - e.clientY - dragOffset.value.y;

    // 限制播放器不能拖出可视区域
    const safeLeft = Math.max(0, Math.min(newLeft, window.innerWidth - 100));
    const safeBottom = Math.max(0, Math.min(newBottom, window.innerHeight - 100));

    playerPosition.value = {
      x: safeLeft,
      y: safeBottom,
    };
  }
};

// 拖拽结束
const stopDragging = () => {
  isDragging.value = false;
  window.removeEventListener("mousemove", onDragging);
  window.removeEventListener("mouseup", stopDragging);
};


</script>

<template>
  <div
      class="music-player"
      :style="{
      left: playerPosition.x + 'px',
      bottom: playerPosition.y + 'px'
    }"
      @mousedown="startDragging"
  >
    <!-- 用于背景图的独立 div -->
    <div class="background-image" :class="{ rotate: isPlaying }"></div>

    <!-- 歌曲标题和播放/暂停按钮 -->
    <div class="song-info-top">
      <p class="song-title">{{ currentSong.title }}</p>
    </div>

    <audio ref="audioPlayer" :src="currentSong.url" @ended="nextSong"></audio>

    <!-- 播放进度条 -->
    <div class="progress-container">
      <input
          type="range"
          min="0"
          max="100"
          step="1"
          v-model="progress"
          @input="changeProgress"
          @mousedown.stop
          class="progress-bar"
      />
    </div>

    <!-- 音量调节滑块 -->
    <div class="controls">
      <input
          type="range"
          min="0"
          max="1"
          step="0.01"
          v-model="volume"
          @input="changeVolume"
          @mousedown.stop
      />
    </div>

    <!-- 播放/暂停按钮 -->
    <button @click="togglePlay" class="play-button">
      {{ isPlaying ? '♫' : '▷' }}
    </button>
  </div>
</template>




<style scoped>
.music-player {
  position: fixed;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  padding: 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
  z-index: 999999;
  cursor: move;
  transition: transform 0.2s ease;
  font-family: 'Comic Sans MS', 'Arial Rounded MT Bold', sans-serif;
  border: 3px solid #ff69b4;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  text-align: center;
  bottom: 20px;
  left: 20px;
  animation: float 3s ease-in-out infinite;
  overflow: hidden;
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/img/musicplayer/backgroundByMusic.jpg');
  background-size: cover;
  background-position: center;
  z-index: -1;
}

.background-image.rotate {
  animation: rotate 10s linear infinite;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.song-info-top {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #ff1493;
  margin-left: 0;
  margin-top: 15px;
  margin-bottom: 5px;
  font-weight: bold;
  text-shadow: 1px 1px 2px #fff;
}

.play-button {
  margin-bottom: 0px;
  margin-right: -5px;
  font-size: 20px;
  cursor: pointer;
  background: none;
  border: none;
  color: #ff1493;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
  position: relative;
}

.play-button:hover {
  transform: scale(1.2);
}

.play-button::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid #ff1493;
  border-radius: 50%;
  animation: pulse 2s infinite;
  opacity: 0.5;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.2;
  }
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
}

.song-title {
  margin: 0;
  white-space: nowrap;
  text-shadow: 1px 1px 2px #fff;
}

.progress-container {
  width: 100%;
  margin-top: -10px;
  padding: 0;
}

.progress-bar {
  width: 100%;
  accent-color: #ff1493;
  background-color: rgba(255, 255, 255, 0.7);
  height: 5px;
  border-radius: 2.5px;
}

.progress-bar::-webkit-slider-thumb {
  appearance: none;
  width: 12px;
  height: 12px;
  background: #ff1493;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.controls {
  display: flex;
  flex-direction: column;
  margin-top: 5px;
  padding-bottom: 0;
}

.controls input[type="range"] {
  width: 100%;
  accent-color: #ff1493;
  background-color: rgba(255, 255, 255, 0.7);
  height: 5px;
  border-radius: 2.5px;
}

.controls input[type="range"]::-webkit-slider-thumb {
  appearance: none;
  width: 12px;
  height: 12px;
  background: #ff1493;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}
</style>


