<template>
  <div class="register-container">
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
                <div class="bubble-circle big"></div>
                <div class="bubble-circle medium"></div>
                <div class="bubble-circle small"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="header-text">
        <div class="main-title">注册新用户</div>
        <div class="subtitle">采用Hash加密算法，请放心注册</div>
      </div>
    </div>

    <div class="form-container">
      <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
        <div class="input-group">
          <el-form-item prop="username">
            <el-input v-model="form.username" :maxlength="8" type="text" placeholder="用户名" class="anime-input">
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <div class="input-group">
          <el-form-item prop="password">
            <el-input v-model="form.password" :maxlength="16" type="password" placeholder="密码" class="anime-input">
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <div class="input-group">
          <el-form-item prop="password_repeat">
            <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复密码" class="anime-input">
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </div>

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
      <el-button class="anime-button register-button" @click="register" plain>立即注册</el-button>
    </div>

    <div class="login-link">
      <span>已有账号? </span>
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
import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
import router from "@/router";
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";

const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

const validateUsername = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  } else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
    callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
  } else {
    callback()
  }
}

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
  username: [
    { validator: validateUsername, trigger: ['blur', 'change'] },
    { min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ]
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

const register = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/register', {
        username: form.username,
        password: form.password,
        email: form.email,
        code: form.code
      }, () => {
        ElMessage.success('注册成功，欢迎加入我们，请登录')
        router.push("/login")
      })
    } else {
      ElMessage.warning('请完整填写注册表单内容！')
    }
  })
}

const validateEmail = () => {
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
    ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`)
    const handle = setInterval(() => {
      coldTime.value--
      if(coldTime.value === 0) {
        clearInterval(handle)
      }
    }, 1000)
  }, undefined, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}
</script>

<style scoped>
.register-container {
  text-align: center;
  margin: 0 20px;
  padding: 20px 0;
  height: 100%;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
}

.anime-header {
  margin-top: 30px;
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
  top: -60px;
  right: -30px;
  z-index: 3;
  display: flex;
  justify-content: center;
  align-items: center;
}

.bubble-circle {
  position: absolute;
  background: linear-gradient(135deg, #ffffff, #f0f9ff);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0f0ff;
}

.bubble-circle.big {
  width: 35px;
  height: 35px;
  right: -5px;
  animation: bubble-float 3s ease-in-out infinite;
}

.bubble-circle.medium {
  width: 25px;
  height: 25px;
  top: 20px;
  right: -1px;
  animation: bubble-float 3s ease-in-out infinite 0.3s;
}

.bubble-circle.small {
  width: 15px;
  height: 15px;
  top: 45px;
  right: 15px;
  animation: bubble-float 3s ease-in-out infinite 0.6s;
}

@keyframes bubble-float {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(2px, -10px);
  }
}
@keyframes character-float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
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
  background: linear-gradient(45deg, #6495ed, #00ffff);
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

.form-container {
  margin-top: 30px;
  padding: 0 20px;
  animation: fadeIn 1s ease-out 0.3s both;
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
  border-color: #6495ed !important;
  box-shadow: 0 4px 25px rgba(100, 149, 237, 0.3) !important;
}

.input-icon {
  font-size: 18px !important;
  color: #6495ed;
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
  height: 55px; /* 增加按钮高度 */
  width: 75px; /* 保持宽度为100% */
  font-size: 12px; /* 调整字体大小 */
  padding: 0 10px; /* 调整内边距 */
}

.code-button:hover:not(.disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(100, 149, 237, 0.4);
}

.register-button {
  background: linear-gradient(45deg, #98fb98, #66cd66) !important;
  border-color: #98fb98 !important;
  color: white !important;
}

.register-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(152, 251, 152, 0.4);
}

.button-container {
  margin-top: 40px;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  color: #666;
}

.login-link .el-link {
  margin-left: 5px;
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
