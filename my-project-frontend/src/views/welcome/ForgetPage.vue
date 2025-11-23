<template>
  <div class="forget-container">
    <div class="anime-header">
      <div class="anime-logo">
        <div class="logo-container">
          <div class="anime-character">
            <div class="character-head">
              <div class="character-eye left-eye"></div>
              <div class="character-eye right-eye"></div>
              <div class="character-mouth"></div>
            </div>
            <div class="character-body"></div>
            <div class="thinking-bubble">
              <div class="bubble-content">?</div>
            </div>
          </div>
        </div>
      </div>
      <div class="header-text">
        <div class="main-title">重置密码</div>
        <div class="subtitle">不用担心，我们会帮助你恢复账号访问</div>
      </div>
    </div>

    <div class="steps-container">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件" />
        <el-step title="重新设定密码" />
      </el-steps>
    </div>

    <transition name="el-fade-in-linear" mode="out-in">
      <div class="form-container" v-if="active === 0">
        <div class="form-header">
          <div class="form-title">验证电子邮件</div>
          <div class="form-subtitle">请输入需要重置密码的电子邮件地址</div>
        </div>
        <div class="form-content">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <div class="input-group">
              <el-form-item prop="email">
                <el-input v-model="form.email" type="email" placeholder="电子邮件地址" class="anime-input">
                  <template #prefix>
                    <el-icon class="input-icon"><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            <div class="input-group">
              <el-form-item prop="code">
                <el-row :gutter="10" style="width: 100%">
                  <el-col :span="17">
                    <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码" class="anime-input">
                      <template #prefix>
                        <el-icon class="input-icon"><EditPen /></el-icon>
                      </template>
                    </el-input>
                  </el-col>
                  <el-col :span="5">
                    <el-button class="anime-button code-button"
                               :class="{ 'disabled': !isEmailValid || coldTime > 0 }"
                               @click="validateEmail"
                               :disabled="!isEmailValid || coldTime > 0">
                      {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                    </el-button>
                  </el-col>
                </el-row>
              </el-form-item>
            </div>
          </el-form>
        </div>
        <div class="button-container">
          <el-button @click="confirmReset()" class="anime-button reset-button" plain>开始重置密码</el-button>
        </div>
      </div>
    </transition>

    <transition name="el-fade-in-linear" mode="out-in">
      <div class="form-container" v-if="active === 1">
        <div class="form-header">
          <div class="form-title">重新设定密码</div>
          <div class="form-subtitle">请填写您的新密码，务必牢记，防止丢失</div>
        </div>
        <div class="form-content">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <div class="input-group">
              <el-form-item prop="password">
                <el-input v-model="form.password" :maxlength="16" type="password" placeholder="新密码" class="anime-input">
                  <template #prefix>
                    <el-icon class="input-icon"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            <div class="input-group">
              <el-form-item prop="password_repeat">
                <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复新密码" class="anime-input">
                  <template #prefix>
                    <el-icon class="input-icon"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
          </el-form>
        </div>
        <div class="button-container">
          <el-button @click="doReset()" class="anime-button reset-button" plain>立即重置密码</el-button>
        </div>
      </div>
    </transition>

    <div class="back-to-login">
      <span>想起密码了？</span>
      <el-link type="primary" @click="router.push('/login')">立即登录</el-link>
    </div>

    <div class="floating-elements">
      <div class="floating-element" style="top: 10%; left: 10%;"></div>
      <div class="floating-element" style="top: 20%; right: 15%;"></div>
      <div class="floating-element" style="bottom: 30%; left: 20%;"></div>
      <div class="floating-element" style="bottom: 15%; right: 10%;"></div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

const active = ref(0)

const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: '',
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

const validateEmail = () => {
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=reset`, () => {
    ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`)
    const handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}

const confirmReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/reset-confirm', {
        email: form.email,
        code: form.code
      }, () => active.value++)
    }
  })
}

const doReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/reset-password', {
        email: form.email,
        code: form.code,
        password: form.password
      }, () => {
        ElMessage.success('密码重置成功，请重新登录')
        router.push('/login')
      })
    }
  })
}

</script>

<style scoped>
.forget-container {
  height: 100%;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
}

.anime-header {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
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
  width: 30px;
  height: 30px;
  background: white;
  border-radius: 50%;
  top: -10px;
  right: -15px;
  z-index: 3;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.2);
  animation: bubble-float 3s ease-in-out infinite;
}

.thinking-bubble::before {
  content: "";
  position: absolute;
  width: 15px;
  height: 15px;
  background: white;
  border-radius: 50%;
  top: 15px;
  right: 5px;
  z-index: -1;
}

.thinking-bubble::after {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  top: 25px;
  right: 15px;
  z-index: -1;
}

.bubble-content {
  font-weight: bold;
  color: #6495ED;
  font-size: 18px;
  animation: text-pulse 2s ease-in-out infinite;
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

/* ... 其他样式保持不变 ... */



.header-text {
  text-align: left;
}

.main-title {
  font-size: 32px;
  font-weight: bold;
  background: linear-gradient(45deg, #ff69b4, #6495ed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: 15px;
  color: #666;
  margin-top: 8px;
  font-style: italic;
}

.steps-container {
  margin: 30px 20px;
}

.steps-container :deep(.el-step__title.is-process) {
  color: #ff69b4;
  font-weight: bold;
}

.steps-container :deep(.el-step__head.is-process) {
  color: #ff69b4;
  border-color: #ff69b4;
}

.steps-container :deep(.el-step__title.is-success) {
  color: #67c23a;
}

.steps-container :deep(.el-step__head.is-success) {
  color: #67c23a;
  border-color: #67c23a;
}

.form-container {
  text-align: center;
  margin: 0 20px;
  animation: fadeIn 0.5s ease-out;
}

.form-header {
  margin-top: 30px;
}

.form-title {
  font-size: 25px;
  font-weight: bold;
  color: #333;
}

.form-subtitle {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.form-content {
  margin-top: 30px;
  padding: 0 20px;
}

.input-group {
  margin-bottom: 20px;
}

.anime-input {
  height: 50px;
  border-radius: 25px !important;
  border: 2px solid #e0e0e0 !important;
  background: rgba(255, 255, 255, 0.7) !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
  transition: all 0.3s ease;
  color: #000000 !important;
}

.anime-input :deep(input) {
  color: #000000 !important;
}

.anime-input :deep(.el-input__inner) {
  color: #000000 !important;
}

.anime-input:hover, .anime-input:focus {
  border-color: #ff69b4 !important;
  box-shadow: 0 4px 25px rgba(255, 105, 180, 0.3) !important;
}

.input-icon {
  font-size: 18px !important;
  color: #ff69b4;
}

.anime-button {
  width: 270px;
  height: 50px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  border: 2px solid;
  transition: all 0.3s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.code-button {
  background: linear-gradient(45deg, #6495ed, #5bc0de) !important;
  border-color: #6495ed !important;
  color: white !important;
  height: 50px; /* 增加按钮高度 */
  width: 75px; /* 保持宽度为100% */
  font-size: 12px; /* 调整字体大小 */
  padding: 0 10px; /* 调整内边距 */
}

.code-button:hover:not(.disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(100, 149, 237, 0.4);
}

.reset-button {
  background: linear-gradient(45deg, #ff69b4, #ff8ec7) !important;
  border-color: #ff69b4 !important;
  color: white !important;
}

.reset-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(255, 105, 180, 0.4);
}

.button-container {
  margin-top: 50px;
}

.back-to-login {
  margin-top: 30px;
  text-align: center;
  color: #666;
}

.back-to-login .el-link {
  margin-left: 10px;
  font-weight: bold;
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
  background: rgba(255, 105, 180, 0.2);
  animation: float 6s ease-in-out infinite;
}

.floating-element:nth-child(2) {
  background: rgba(100, 149, 237, 0.2);
  width: 15px;
  height: 15px;
  animation-delay: 1s;
}

.floating-element:nth-child(3) {
  background: rgba(91, 192, 222, 0.2);
  width: 25px;
  height: 25px;
  animation-delay: 2s;
}

.floating-element:nth-child(4) {
  background: rgba(255, 142, 199, 0.2);
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
