<script setup>
import {User, Lock} from '@element-plus/icons-vue'
import {reactive, ref, onBeforeUnmount} from "vue";
import {login, post} from "@/net";
import router from "@/router/index.js";
import { useUserStore } from '@/stores/user_store.js'

const userStore = useUserStore()
const formRef = ref()
const tiltCard = ref(null)
let clickGradient = null

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码'}
  ]
}

function userLogin() {
  formRef.value.validate((isValid) => {
    if(isValid) {
      login(form.username, form.password, form.remember,
          (data) => {
            // 登录成功后将用户信息保存到store
            userStore.setUser({
              username: data.username,
              email: data.email
            })

            // 登录成功后立即获取用户角色
            post('/api/auth/verify-role', { username: data.username }, (roleData) => {
              if (roleData) {
                userStore.setRole(roleData);
              } else {
                userStore.setRole('user');
              }
              router.push('/index')
            }, (error) => {
              console.error('获取用户权限失败:', error);
              userStore.setRole('user');
              router.push('/index')
            })
          })
    }
  });
}

function handleMouseMove(e) {
  if (!tiltCard.value) return

  const card = tiltCard.value
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2

  // 最大旋转角度
  const maxTilt = 18
  const rotateY = ((x - centerX) / centerX) * maxTilt
  const rotateX = -((y - centerY) / centerY) * maxTilt

  card.style.transform = `perspective(600px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.03)`
}

function handleMouseLeave() {
  if (!tiltCard.value) return

  const card = tiltCard.value
  card.style.transform = 'perspective(600px) rotateX(0deg) rotateY(0deg) scale(1)'
}

function handleClick(e) {
  if (!tiltCard.value) return

  // 移除已存在的波纹
  if (clickGradient) {
    clickGradient.remove()
  }

  // 创建新的波纹元素
  clickGradient = document.createElement('div')
  clickGradient.className = 'click-gradient'

  const card = tiltCard.value
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  clickGradient.style.left = `${x}px`
  clickGradient.style.top = `${y}px`

  card.appendChild(clickGradient)

  // 触发动画
  setTimeout(() => {
    if (clickGradient) {
      clickGradient.remove()
      clickGradient = null
    }
  }, 600)
}

// 清理工作
onBeforeUnmount(() => {
  if (clickGradient) {
    clickGradient.remove()
  }
})
</script>

<template>
  <div class="login-container animated-background">
    <!-- SVG滤镜库 -->
    <svg style="display: none">
      <filter id="glass-distortion" x="0%" y="0%" width="100%" height="100%" filterUnits="objectBoundingBox">
        <feTurbulence type="fractalNoise" baseFrequency="0.001 0.005" numOctaves="1" seed="17" result="turbulence" />
        <feComponentTransfer in="turbulence" result="mapped">
          <feFuncR type="gamma" amplitude="1" exponent="10" offset="0.5" />
          <feFuncG type="gamma" amplitude="0" exponent="1" offset="0" />
          <feFuncB type="gamma" amplitude="0" exponent="1" offset="0.5" />
        </feComponentTransfer>
        <feGaussianBlur in="turbulence" stdDeviation="3" result="softMap" />
        <feSpecularLighting in="softMap" surfaceScale="5" specularConstant="1" specularExponent="100" lighting-color="white" result="specLight">
          <fePointLight x="-200" y="-200" z="300" />
        </feSpecularLighting>
        <feComposite in="specLight" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litImage" />
        <feDisplacementMap in="SourceGraphic" in2="softMap" scale="200" xChannelSelector="R" yChannelSelector="G" />
      </filter>
    </svg>

    <div
        ref="tiltCard"
        class="glass-component login-card"
        @mousemove="handleMouseMove"
        @mouseleave="handleMouseLeave"
        @click="handleClick"
    >
      <div class="glass-effect"></div>
      <div class="glass-tint"></div>
      <div class="glass-shine"></div>
      <div class="glass-content">
        <div class="anime-header">
          <div class="anime-logo">
            <div class="logo-container">
              <div class="anime-character">
                <div class="character-head">
                  <div class="character-eye left-eye"></div>
                  <div class="character-eye right-eye"></div>
                  <div class="character-mouth"></div>
                </div>
                <div class="character-body">
                  <div class="thinking-bubble">
                    <div class="question-mark">🔑</div>
                    <div class="bubble-pulse"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="header-text">
            <div class="main-title">登录</div>
            <div class="subtitle">在进入系统之前请先输入用户名和密码进行登录</div>
          </div>
        </div>
        <div class="login-form-container">
          <el-form :model="form" :rules="rules" ref="formRef">
            <div class="input-group">
              <el-form-item prop="username">
                <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名/邮箱" class="anime-input glass-input">
                  <template #prefix>
                    <el-icon class="input-icon">
                      <User/>
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            <div class="input-group">
              <el-form-item prop="password">
                <el-input v-model="form.password" type="password" maxlength="20" placeholder="密码" class="anime-input glass-input">
                  <template #prefix>
                    <el-icon class="input-icon">
                      <Lock/>
                    </el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            <el-row class="options-row">
              <el-col :span="12" class="remember-col">
                <el-form-item prop="remember">
                  <el-checkbox v-model="form.remember" label="记住我" class="anime-checkbox"/>
                </el-form-item>
              </el-col>
              <el-col :span="12" class="forget-col">
                <el-link @click="router.push('/forget')" class="anime-link">忘记密码？</el-link>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="button-container">
          <el-button @click="userLogin()" class="anime-button login-button glass-button" plain>立即登录</el-button>
        </div>
        <div class="divider">
          <span class="divider-text">没有账号</span>
        </div>
        <div class="button-container">
          <el-button @click="router.push('/register')" class="anime-button register-button glass-button" plain>注册账号</el-button>
        </div>
      </div>
    </div>
    <div class="floating-elements">
      <div class="floating-element" style="top: 10%; left: 10%;"></div>
      <div class="floating-element" style="top: 20%; right: 15%;"></div>
      <div class="floating-element" style="bottom: 30%; left: 20%;"></div>
      <div class="floating-element" style="bottom: 15%; right: 10%;"></div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  margin: 0;
  padding: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.animated-background {
  width: 100%;
  height: 100%;
}

.login-card {
  width: 450px;
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.10), 0 1.5px 6px 0 rgba(0,0,0,0.08);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.6);
  cursor: pointer;
  background: transparent;
  margin: 20px;
}

.glass-effect {
  position: absolute;
  inset: 0;
  z-index: 0;
  backdrop-filter: blur(5px);
  filter: url(#glass-distortion);
  isolation: isolate;
  border-radius: 24px;
}

.glass-tint {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: rgba(0, 0, 0, 0.15);
  border-radius: 24px;
}

.glass-shine {
  position: absolute;
  inset: 0;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.13);
  border-radius: 24px;
  box-shadow:
      inset 1px 1px 8px 0 rgba(255, 255, 255, 0.18),
      inset -1px -1px 8px 0 rgba(255, 255, 255, 0.08);
  pointer-events: none;
}

.glass-content {
  position: relative;
  z-index: 3;
  padding: 2rem;
  color: white;
}

.glass-input {
  width: 100%;
  padding: 12px 20px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: #000 !important; /* 改为黑色 */
  font-size: 1rem;
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
  height: 50px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.glass-input :deep(input) {
  color: #000 !important;
}

.glass-input :deep(.el-input__inner) {
  color: #000 !important;
}

.glass-input::placeholder {
  color: rgba(0, 0, 0, 0.7);
}

.glass-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.1);
}

.glass-button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  position: relative;
  overflow: hidden;
  height: 50px;
  border: 2px solid;
}

.glass-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.glass-button:active {
  transform: translateY(0);
}

.click-gradient {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, rgba(180,180,255,0.2) 40%, rgba(100,100,255,0.1) 70%, rgba(50,50,255,0) 100%);
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  pointer-events: none;
  z-index: 4;
}

.glass-component.clicked .click-gradient {
  animation: gradient-ripple 0.6s ease-out;
}

@keyframes gradient-ripple {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(3);
    opacity: 0;
  }
}

.glass-component {
  transition: transform 0.25s cubic-bezier(0.22, 1, 0.36, 1);
  will-change: transform;
}

/* 原有动画元素样式 */
.anime-header {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  animation: slideInDown 1s ease-out;
}

.anime-logo {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-container {
  position: relative;
  width: 80px;
  height: 80px;
}

.anime-character {
  position: relative;
  width: 100%;
  height: 100%;
  animation: character-float 4s ease-in-out infinite;
}

.character-head {
  position: absolute;
  width: 50px;
  height: 50px;
  background: #FFDAB9;
  border-radius: 50%;
  top: 0;
  left: 15px;
  z-index: 2;
  box-shadow: 0 0 10px rgba(255, 218, 185, 0.8);
  border: 2px solid #FFA07A;
}

.character-eye {
  position: absolute;
  width: 10px;
  height: 15px;
  background: #333;
  border-radius: 50%;
  top: 18px;
}

.left-eye {
  left: 12px;
}

.right-eye {
  right: 12px;
}

.character-eye::before {
  content: "";
  position: absolute;
  width: 4px;
  height: 4px;
  background: white;
  border-radius: 50%;
  top: 3px;
  left: 3px;
}

.character-mouth {
  position: absolute;
  width: 20px;
  height: 10px;
  border-bottom: 2px solid #333;
  border-radius: 0 0 50% 50%;
  bottom: 8px;
  left: 15px;
}

.character-body {
  position: absolute;
  width: 60px;
  height: 40px;
  background: #87CEEB;
  border-radius: 30px 30px 10px 10px;
  top: 45px;
  left: 10px;
  z-index: 1;
  border: 2px solid #5F9EA0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.thinking-bubble {
  position: absolute;
  top: -55px;
  right: -30px;
  z-index: 3;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6495ED, #87CEEB);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(100, 149, 237, 0.6);
  animation: bubble-float 3s ease-in-out infinite;
}

.question-mark {
  font-weight: bold;
  color: white;
  font-size: 24px;
  font-family: 'Arial Rounded MT Bold', 'Arial', sans-serif;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  animation: pulse 2s ease-in-out infinite;
}

.bubble-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgba(100, 149, 237, 0.5);
  animation: bubble-pulse 2s ease-out infinite;
}

@keyframes bubble-pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.thinking-bubble::before {
  display: none;
}

.thinking-bubble::after {
  display: none;
}

.bubble-content {
  display: none;
}

@keyframes character-float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes heartbeat {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

@keyframes bubble-float {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(2px, -5px);
  }
}

@keyframes text-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

.header-text {
  text-align: left;
}

.main-title {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(45deg, #6495ed, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
}

.subtitle {
  font-size: 15px;
  color: white;
  margin-top: 8px;
  font-style: italic;
}

.login-form-container {
  margin-top: 30px;
  padding: 0 20px;
  animation: fadeIn 1s ease-out 0.3s both;
}

.input-group {
  margin-bottom: 20px;
}

.input-icon {
  font-size: 18px !important;
  color: #6495ed;
}

.options-row {
  margin-top: 10px;
}

.remember-col {
  text-align: left;
  padding-left: 20px;
}

.forget-col {
  text-align: right;
  padding-right: 20px;
}

.anime-checkbox {
  color: #e0e0e0;
}

.anime-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #6495ed;
  border-color: #6495ed;
}

.anime-checkbox :deep(.el-checkbox__input.is-checked+.el-checkbox__label) {
  color: #e0e0e0;
}

.anime-link {
  color: #6495ed !important;
  font-weight: 500;
  transition: all 0.3s ease;
}

.anime-link:hover {
  color: #4169e1 !important;
  text-shadow: 0 0 8px rgba(100, 149, 237, 0.5);
}

.button-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.anime-button {
  width: 270px;
  height: 50px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: bold;
  border: 2px solid;
  transition: all 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.login-button {
  background: linear-gradient(45deg, #6495ed, #5bc0de) !important;
  border-color: #6495ed !important;
  color: white !important;
}

.register-button {
  background: linear-gradient(45deg, #ff69b4, #ff8ec7) !important;
  border-color: #ff69b4 !important;
  color: white !important;
}

.divider {
  margin: 30px auto;
  position: relative;
  width: 270px;
  height: 20px;
  display: flex;
  align-items: center;
}

.divider::before {
  content: "";
  position: absolute;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, #ccc, transparent);
}

.divider-text {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 14px;
  padding: 0 15px;
  z-index: 1;
  backdrop-filter: blur(5px);
  border-radius: 10px;
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
}

.floating-element {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(100, 149, 237, 0.2);
  animation: float 6s ease-in-out infinite;
}

.floating-element:nth-child(2) {
  background: rgba(255, 105, 180, 0.2);
  width: 15px;
  height: 15px;
  animation-delay: 1s;
}

.floating-element:nth-child(3) {
  background: rgba(0, 255, 255, 0.2);
  width: 25px;
  height: 25px;
  animation-delay: 2s;
}

.floating-element:nth-child(4) {
  background: rgba(91, 192, 222, 0.2);
  width: 12px;
  height: 12px;
  animation-delay: 3s;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes headFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(10px, 10px);
  }
  50% {
    transform: translate(0, 15px);
  }
  75% {
    transform: translate(-10px, 10px);
  }
}
</style>
