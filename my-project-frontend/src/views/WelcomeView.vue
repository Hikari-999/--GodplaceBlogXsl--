<template>
  <div class="welcome-container">
    <div class="background-container">
      <div class="anime-overlay"></div>
      <el-image class="background-image" fit="cover"
                src="welcomeimagebackground.jpg"/>
    </div>
    <div class="anime-welcome-title">
      <div class="title-text">
        <div class="main-title">欢迎来到Godplace的博客</div>
        <div class="subtitle">纯AI开发个人博客网站</div>
        <div class="fun-text">如果您对本博客开发风格感兴趣不妨至Github点个Star</div>
      </div>
    </div>
    <div class="right-card">
      <div class="card-decoration">
        <div class="decoration-item"></div>
        <div class="decoration-item"></div>
        <div class="decoration-item"></div>
      </div>
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped>
.welcome-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  position: relative;
  font-family: 'Comic Sans MS', 'Arial', sans-serif;
}

.background-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.background-image {
  width: 100%;
  height: 100%;
  filter: brightness(0.8) saturate(1.2);
}

.anime-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, transparent 60%, rgba(255, 105, 180, 0.2) 100%);
  z-index: 1;
  pointer-events: none;
}

.anime-welcome-title {
  position: absolute;
  bottom: 40px;
  left: 40px;
  color: white;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  gap: 30px;
}

.title-text {
  background: rgba(0, 0, 0, 0.6);
  padding: 20px 30px;
  border-radius: 20px;
  border: 2px solid #ff69b4;
  box-shadow: 0 0 20px rgba(255, 105, 180, 0.5);
  backdrop-filter: blur(5px);
  animation: float 3s ease-in-out infinite;
}

.main-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 15px;
  text-shadow: 0 0 10px #00ffff, 0 0 20px #00ffff;
  color: #fff;
  letter-spacing: 2px;
}

.subtitle, .fun-text {
  font-size: 16px;
  margin-top: 10px;
  text-shadow: 0 0 5px black;
  line-height: 1.5;
}

.subtitle {
  color: #aaffff;
}

.fun-text {
  color: #ffccff;
  font-style: italic;
}

.anime-character {
  position: relative;
  width: 80px;
  height: 120px;
  animation: bounce 2s ease-in-out infinite;
}

.character-head {
  width: 60px;
  height: 60px;
  background: #ffdbac;
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 10px;
  border: 2px solid #d4a76a;
  box-shadow: 0 0 10px rgba(255, 219, 172, 0.5);
}

.character-head::before {
  content: "";
  position: absolute;
  width: 15px;
  height: 15px;
  background: #333;
  border-radius: 50%;
  top: 20px;
  left: 15px;
  box-shadow: 20px 0 0 #333;
}

.character-head::after {
  content: "";
  position: absolute;
  width: 30px;
  height: 15px;
  border-bottom: 3px solid #ff6b6b;
  border-radius: 0 0 50% 50%;
  bottom: 15px;
  left: 15px;
}

.character-body {
  width: 70px;
  height: 60px;
  background: #6495ed;
  border-radius: 40px 40px 10px 10px;
  position: absolute;
  top: 55px;
  left: 5px;
  border: 2px solid #4169e1;
}

.right-card {
  width: 400px;
  z-index: 3;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(240, 248, 255, 0.95) 100%);
  box-shadow: -5px 0 25px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
  border-left: 3px solid #ff69b4;
}

.right-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(90deg, #ff69b4, #6495ed, #00ffff);
  animation: rainbow-move 3s linear infinite;
}

.card-decoration {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 8px;
  z-index: 1;
}

.decoration-item {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ff69b4;
  animation: pulse 1.5s ease-in-out infinite;
}

.decoration-item:nth-child(2) {
  background: #6495ed;
  animation-delay: 0.5s;
}

.decoration-item:nth-child(3) {
  background: #00ffff;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
}

@keyframes rainbow-move {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
